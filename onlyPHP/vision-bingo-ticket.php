<?php
require_once __DIR__ . '/config.php';

header('Content-Type: application/json; charset=utf-8');

$REQ_START = microtime(true);

function tlog(string $message): void {
    global $REQ_START;

    $elapsedMs = (int) round((microtime(true) - $REQ_START) * 1000);
    $line = "[vision-bingo-ticket] {$elapsedMs}ms - {$message}\n";

    file_put_contents(
        __DIR__ . '/vision-log.txt',
        $line,
        FILE_APPEND
    );
}

function respond_json(int $status, array $payload): void {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
    exit;
}

function rate_limit_check(): void {
    $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';

    $dir = __DIR__ . '/ratelimit';
    if (!is_dir($dir)) {
        mkdir($dir, 0755, true);
    }

    $file = $dir . '/' . md5($ip) . '.txt';
    $now = time();

    $window = 60;
    $maxRequests = 20;

    $timestamps = [];

    if (file_exists($file)) {
        $timestamps = array_filter(
            explode("\n", file_get_contents($file)),
            fn($t) => ($now - (int)$t) < $window
        );
    }

    if (count($timestamps) >= $maxRequests) {
        tlog('rate limit exceeded');
        respond_json(429, [
            'error' => 'Too many requests',
        ]);
    }

    $timestamps[] = $now;
    file_put_contents($file, implode("\n", $timestamps));
}

function call_gemini_vision(string $imageBase64, string $mimeType = 'image/jpeg', string $model = 'gemini-2.5-flash-lite', string $ticketMetaInstructions = ''): array {
    $apiKey = defined('GEMINI_API_KEY') ? GEMINI_API_KEY : '';
    if ($apiKey === '' || $apiKey === 'PASTE_NEW_GEMINI_API_KEY_HERE') {
        throw new RuntimeException('Gemini API key not configured');
    }

    $url = 'https://generativelanguage.googleapis.com/v1/models/' . $model . ':generateContent?key=' . urlencode($apiKey);

    $metaAppend = '';
    if ($ticketMetaInstructions !== '') {
        $metaAppend = "\n\nAdditional ticket-meta instructions from client:\n" . $ticketMetaInstructions;
    }

    $prompt = <<<PROMPT
Extract the main 5x5 bingo grid from this ticket image.

If the image is rotated, mentally rotate it upright first.

Use these column ranges:
B = 1-15
I = 16-30
N = 31-45
G = 46-60
O = 61-75

Ignore logos, branding, prices, dates, side text, and decorative text in the grid area.

Ticket meta (los / serial): read from the vertical side-strip on the ticket edge if visible (may be rotated). Do not confuse with grid numbers.

Rules for losNumber and serialNumber:
- losNumber must be exactly 5 digits, or empty string ""
- serialNumber must be exactly 4 digits, or empty string ""
- Never return labels, words, prices, dates, or promotional text
- If unsure, return ""

Return JSON only:
{
  "flatNumbers":[int],
  "detectedColumns":{"B":[int],"I":[int],"N":[int],"G":[int],"O":[int]},
  "losNumber":"string",
  "serialNumber":"string",
  "confidence":0.0,
  "summary":"short text"
}
{$metaAppend}
PROMPT;

    $payload = [
        'contents' => [[
            'parts' => [
                [
                    'text' => $prompt,
                ],
                [
                    'inline_data' => [
                        'mime_type' => $mimeType,
                        'data' => $imageBase64,
                    ],
                ],
            ],
        ]],
        'generationConfig' => [
            'temperature' => 0.1,
        ],
    ];

    tlog("model={$model} request start");

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
        CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_SLASHES),
        CURLOPT_TIMEOUT => 90,
    ]);

    $response = curl_exec($ch);
    $curlError = curl_error($ch);
    $httpCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($response === false) {
        throw new RuntimeException('cURL error: ' . $curlError);
    }

    tlog("model={$model} request finish httpCode={$httpCode}");

    if ($httpCode < 200 || $httpCode >= 300) {
        throw new RuntimeException('Gemini HTTP ' . $httpCode . ': ' . $response);
    }

    $json = json_decode($response, true);
    if (!is_array($json)) {
        throw new RuntimeException('Invalid Gemini JSON response');
    }

    return $json;
}

function extract_text_from_gemini(array $geminiRaw): string {
    $parts = $geminiRaw['candidates'][0]['content']['parts'] ?? null;
    if (!is_array($parts)) {
        throw new RuntimeException('Gemini response missing content parts');
    }

    $text = '';
    foreach ($parts as $part) {
        if (isset($part['text']) && is_string($part['text'])) {
            $text .= $part['text'];
        }
    }

    $text = trim($text);
    if ($text === '') {
        throw new RuntimeException('Gemini response text is empty');
    }

    return $text;
}

function parse_json_block(string $text): array {
    $clean = trim($text);
    $clean = preg_replace('/^```json\s*/i', '', $clean);
    $clean = preg_replace('/^```\s*/', '', $clean);
    $clean = preg_replace('/\s*```$/', '', $clean);

    $decoded = json_decode($clean, true);
    if (is_array($decoded)) {
        return $decoded;
    }

    $start = strpos($clean, '{');
    $end = strrpos($clean, '}');
    if ($start !== false && $end !== false && $end > $start) {
        $slice = substr($clean, $start, $end - $start + 1);
        $decoded = json_decode($slice, true);
        if (is_array($decoded)) {
            return $decoded;
        }
    }

    throw new RuntimeException('Could not parse JSON from Gemini text');
}

function sanitize_int_list($values, int $min, int $max): array {
    if (!is_array($values)) {
        return [];
    }

    $out = [];
    $seen = [];
    foreach ($values as $v) {
        if (!is_numeric($v)) {
            continue;
        }
        $n = (int) $v;
        if ($n < $min || $n > $max) {
            continue;
        }
        if (isset($seen[$n])) {
            continue;
        }
        $seen[$n] = true;
        $out[] = $n;
    }
    return $out;
}

function sanitize_response(array $response): array {
    $flat = sanitize_int_list($response['flatNumbers'] ?? [], 1, 75);

    $ranges = [
        'B' => [1, 15],
        'I' => [16, 30],
        'N' => [31, 45],
        'G' => [46, 60],
        'O' => [61, 75],
    ];

    $columnsIn = $response['detectedColumns'] ?? null;
    $columnsOut = [];

    if (is_array($columnsIn)) {
        foreach ($ranges as $key => [$min, $max]) {
            $columnsOut[$key] = sanitize_int_list($columnsIn[$key] ?? [], $min, $max);
        }
    }

    $hasFullColumns = count($columnsOut) === 5;
    foreach ($ranges as $key => $_) {
        if (!isset($columnsOut[$key]) || count($columnsOut[$key]) !== 5) {
            $hasFullColumns = false;
            break;
        }
    }

    if (count($flat) === 0 && $hasFullColumns) {
        $rebuilt = [];
        for ($row = 0; $row < 5; $row++) {
            foreach (['B', 'I', 'N', 'G', 'O'] as $col) {
                $rebuilt[] = $columnsOut[$col][$row];
            }
        }
        $flat = $rebuilt;
    }

    if (count($flat) < 12) {
        throw new RuntimeException('Structured response rejected: fewer than 12 valid numbers');
    }

    $confidence = null;
    if (isset($response['confidence']) && is_numeric($response['confidence'])) {
        $confidence = max(0.0, min(1.0, (float) $response['confidence']));
    }

    $summary = isset($response['summary']) && is_string($response['summary'])
        ? trim($response['summary'])
        : '';

    if ($summary === '') {
        $summary = 'Detected ' . count($flat) . ' bingo numbers';
    }

    $losRaw = isset($response['losNumber']) ? trim((string) $response['losNumber']) : '';
    $losRaw = preg_replace('/\D/', '', $losRaw);
    $losNumber = preg_match('/^\d{5}$/', $losRaw) ? $losRaw : null;

    $serRaw = isset($response['serialNumber']) ? trim((string) $response['serialNumber']) : '';
    $serRaw = preg_replace('/\D/', '', $serRaw);
    $serialNumber = preg_match('/^\d{4}$/', $serRaw) ? $serRaw : null;

    return [
        'flatNumbers' => $flat,
        'detectedColumns' => $hasFullColumns ? $columnsOut : null,
        'losNumber' => $losNumber,
        'serialNumber' => $serialNumber,
        'confidence' => $confidence,
        'summary' => $summary,
    ];
}



function send_ga4_event(string $eventName, array $params = []): void {
    tlog('GA4 sending event: ' . $eventName);
    $measurementId = defined('GA4_MEASUREMENT_ID') ? GA4_MEASUREMENT_ID : '';
    $apiSecret = defined('GA4_API_SECRET') ? GA4_API_SECRET : '';

    if ($measurementId === '' || $apiSecret === '') {
        tlog('GA4 not configured');
        return;
    }

    $url = 'https://region1.google-analytics.com/mp/collect?measurement_id='
        . urlencode($measurementId)
        . '&api_secret='
        . urlencode($apiSecret);

    $payload = [
        'client_id' => uniqid('bingo_', true),
        'events' => [[
            'name' => $eventName,
            'params' => array_merge([
                'engagement_time_msec' => 100,
                'session_id' => time(),
                'debug_mode' => 1,
                'source' => 'bingo_api'
            ], $params),
        ]],
    ];

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_POST => true,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => ['Content-Type: application/json'],
        CURLOPT_POSTFIELDS => json_encode($payload, JSON_UNESCAPED_SLASHES),
        CURLOPT_TIMEOUT => 10,
    ]);

    //curl_exec($ch);
    $response = curl_exec($ch);
    tlog('GA4 response: ' . $response);
    
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    tlog('GA4 HTTP code: ' . $httpCode);
    
    if (curl_errno($ch)) {
        tlog('GA4 CURL ERROR: ' . curl_error($ch));
    }

    curl_close($ch);
}

$appSecret =
    $_SERVER['HTTP_X_APP_SECRET'] ??
    $_SERVER['REDIRECT_HTTP_X_APP_SECRET'] ??
    $_SERVER['X_APP_SECRET'] ??
    '';

if ($appSecret !== 'bingo_secure_2026') {
    tlog('unauthorized request');
    respond_json(403, [
        'error' => 'Unauthorized',
    ]);
}

rate_limit_check();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    tlog('invalid method');
    respond_json(405, ['error' => 'Method not allowed']);
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);

if (!is_array($data)) {
    tlog('invalid json body');
    respond_json(400, ['error' => 'Invalid JSON body']);
}

$imageBase64 = isset($data['imageBase64']) ? trim((string) $data['imageBase64']) : '';
$mimeType = isset($data['mimeType']) ? trim((string) $data['mimeType']) : 'image/jpeg';
$filename = isset($data['filename']) ? trim((string) $data['filename']) : null;
$ticketMetaInstructions = isset($data['ticketMetaInstructions']) ? trim((string) $data['ticketMetaInstructions']) : '';

if ($imageBase64 === '') {
    tlog('missing imageBase64');
    respond_json(400, ['error' => 'imageBase64 is required']);
}

if (!in_array($mimeType, ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'], true)) {
    tlog("unsupported mimeType={$mimeType}");
    respond_json(400, ['error' => 'Unsupported mimeType']);
}

$imageBase64 = preg_replace('/^data:image\/[a-zA-Z]+;base64,/', '', $imageBase64);
$imageBase64 = preg_replace('/\s+/', '', $imageBase64);

if (strlen($imageBase64) > 8000000) {
    tlog('image too large');
    respond_json(400, ['error' => 'Image too large']);
}

if ($imageBase64 === '' || base64_decode($imageBase64, true) === false) {
    tlog('invalid base64');
    respond_json(400, ['error' => 'imageBase64 is not valid base64']);
}

tlog('request received');

try {
    tlog('model=flash start');
    $geminiRaw = call_gemini_vision($imageBase64, $mimeType, 'gemini-2.5-flash', $ticketMetaInstructions);
    tlog('model=flash finish');

    tlog('extracting gemini text');
    $geminiText = extract_text_from_gemini($geminiRaw);

    tlog('parsing structured json');
    $structured = parse_json_block($geminiText);

    tlog('sanitizing response');
    $clean = sanitize_response($structured);

    send_ga4_event('gemini_scan_success', [
    'image_size' => strlen($imageBase64),
    'mime_type' => $mimeType,
    'debug_mode' => 1
    ]);

    tlog('response ready');
    tlog('meta result los=' . ($clean['losNumber'] ?? 'null') . ' serial=' . ($clean['serialNumber'] ?? 'null'));
    respond_json(200, $clean);
} catch (Throwable $e) {
    tlog('request failed: ' . $e->getMessage());
    respond_json(500, [
        'error' => 'AI request failed',
        'details' => $e->getMessage(),
        'filename' => $filename,
    ]);
}