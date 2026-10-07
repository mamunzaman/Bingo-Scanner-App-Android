# Bingo draw import (n8n)

Separate n8n workflow that pulls **official BINGO Umweltlotterie JSON**, validates jackpot and 22 winning numbers, then POSTs a normalized payload to BingoBlogs.

It is **not** the Projects / blog import workflow. Do not merge the two.

`sample-response.json` is **synthetic test data**. It is not a live lottery payload.

The Android app still reads `bingo_draws` from Supabase. Keep `supabase/functions/scrape-bingo` until Home and History week results are verified against BingoBlogs.

## What it does

1. Runs on Berlin schedules (or Manual Trigger).
2. Picks the official `gewinnzahlen/{yyyy-MM-dd}` date in **Europe/Berlin** (DST-aware).
3. GETs the official API (never from the Android app).
4. Builds a safe body for `POST https://blogs.bingo-hub.de/api/import-bingo`.
5. Skips the CMS POST on 404, bad JSON, or nothing safe to import (no-op).
6. Dry run validates without calling BingoBlogs.

This repository has no deployed BingoBlogs PHP contract. The POST body matches the audit contract:

- `current_jackpot_eur` (omitted if invalid)
- `next_draw_at` (omitted on manual backfill unless `updateStatusDuringBackfill`)
- `draw` (omitted unless 22 unique integers 1–75, sorted by `drawIndex`)

## Import into n8n

1. n8n → **Workflows** → **Add workflow** (or Import).
2. Import **from file**: `bingo-draw-import.json`.
3. Confirm the workflow is named **Bingo draw import** and stays **inactive**.
4. Set workflow timezone to **Europe/Berlin** if the import does not apply settings (the export includes `settings.timezone`).

## Header Auth credential (required)

Never put the token in this JSON, README, Code nodes, URLs, or logs.

1. n8n → **Credentials** → **Add credential** → **Header Auth**.
2. Name: `BingoBlogs Bingo Import`
3. Header name: `X-Bingo-Import-Token`
4. Header value: the production `BINGO_IMPORT_TOKEN` from your secret store (not this repo).
5. Open node **Import into BingoBlogs**.
6. Authentication: **Generic Credential Type** → **Header Auth**.
7. Select **BingoBlogs Bingo Import**.
8. Save. If the imported node shows a missing credential, re-attach it.

## Dry run

1. Open **Import Configuration**.
2. Set `dryRun` to `true`.
3. Run **Manual Trigger**.
4. Confirm **Report Success** shows `dryRun: true` and a payload, and that **Import into BingoBlogs** did not run.
5. Set `dryRun` back to `false`.

## One-date manual backfill

1. **Import Configuration**: set `manualDrawDate` to `YYYY-MM-DD` (must be a Sunday, not in the future).
2. Leave `updateStatusDuringBackfill` `false` unless you intentionally want jackpot / `next_draw_at` updated from that historical fetch.
3. Optional: `dryRun` `true` first.
4. Run **Manual Trigger**.
5. Restore automatic mode: `manualDrawDate` `""`, `updateStatusDuringBackfill` `false`, `dryRun` `false`.

## Activate

Only after the credential is attached and a dry run looks correct:

1. Save the workflow.
2. Toggle **Active**.
3. Confirm timezone **Europe/Berlin**.

Do not activate from this repository. This export is **inactive**.

## Schedules (Europe/Berlin)

| Node | When |
|------|------|
| Daily 08:00 Berlin | Every day 08:00 |
| Sunday Draw Checks | Sun 17:10, 17:25, 17:45, 18:15, 20:00 |

Plus **Manual Trigger**.

## Public checks (after a real import)

- `GET https://blogs.bingo-hub.de/api/bingo/status`
- `GET https://blogs.bingo-hub.de/api/bingo/draws/{yyyy-mm-dd}`

## Official source

`GET https://www.bingo-umweltlotterie.de/api/gewinnzahlen/{yyyy-MM-dd}`

Use `bingo.jackpotNew` (else `bingo.jackpotCurrently`), `bingo.drawDate`, `bingo.lastDataChange`, and numbers ordered by `drawIndex` (not `index`).
