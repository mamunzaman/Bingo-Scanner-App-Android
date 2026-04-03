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

    $metaExtra = trim($ticketMetaInstructions);
    if ($metaExtra === '') {
        $metaExtra = <<<'META'
For losNumber and serialNumber: read digits from the vertical side strip on the ticket edge if visible (may be rotated).
META;
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

Ignore logos, branding, prices, dates, and decorative text in the grid area.

Ticket meta (strict, separate from the grid):
{$metaExtra}

Rules for losNumber and serialNumber only:
- losNumber: exactly 5 digit characters (0-9), nothing else. If unsure or not clearly the ticket lot number, use "".
- serialNumber: exactly 4 digit characters (0-9), nothing else. If unsure, use "".
- Never return column labels, words, promotional text, or nearby unrelated numbers. Prefer empty string over guessing.
- Output these two fields only as digit-only strings of the required length, or "".

Return JSON only (no markdown fences), with exactly these keys:
{
  "flatNumbers":[int],
  "detectedColumns":{
    "B":[int],
    "I":[int],
    "N":[int],
    "G":[int],
    "O":[int]
  },
  "confidence":0.0,
  "summary":"short text",
  "losNumber":"",
  "serialNumber":""
}
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
            'responseMimeType' => 'application/json',
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

    $losOut = '';
    $losRaw = $response['losNumber'] ?? '';
    if (is_string($losRaw) || is_numeric($losRaw)) {
        $losDigits = preg_replace('/\D/', '', (string) $losRaw);
        if (strlen($losDigits) === 5) {
            $losOut = $losDigits;
        }
    }

    $serOut = '';
    $serRaw = $response['serialNumber'] ?? '';
    if (is_string($serRaw) || is_numeric($serRaw)) {
        $serDigits = preg_replace('/\D/', '', (string) $serRaw);
        if (strlen($serDigits) === 4) {
            $serOut = $serDigits;
        }
    }

    return [
        'flatNumbers' => $flat,
        'detectedColumns' => $hasFullColumns ? $columnsOut : null,
        'confidence' => $confidence,
        'summary' => $summary,
        'losNumber' => $losOut === '' ? null : $losOut,
        'serialNumber' => $serOut === '' ? null : $serOut,
    ];
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

    file_put_contents(
        __DIR__ . '/gemini-last-raw.json',
        json_encode($geminiRaw, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT)
    );
    tlog('gemini raw API response written to gemini-last-raw.json');

    tlog('extracting gemini text');
    $geminiText = extract_text_from_gemini($geminiRaw);
    tlog('gemini model text chars=' . strlen($geminiText));

    tlog('parsing structured json');
    $structured = parse_json_block($geminiText);

    tlog('sanitizing response');
    $clean = sanitize_response($structured);

    tlog('response ready');
    respond_json(200, $clean);
} catch (Throwable $e) {
    tlog('request failed: ' . $e->getMessage());
    respond_json(500, [
        'error' => 'AI request failed',
        'details' => $e->getMessage(),
        'filename' => $filename,
    ]);
}