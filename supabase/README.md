# Supabase — `scrape-bingo`

Imports the latest Berlin Sunday draw from [bingo-umweltlotterie.de](https://www.bingo-umweltlotterie.de) into `bingo_draws` and `bingo_prizes`. The Android app reads `bingo_draws` via the anon key (`BingoRemoteRepository.getLatestDraw()`).

## Function config (`config.toml`)

- **Name:** `scrape-bingo`
- **Enabled:** `true`
- **`verify_jwt`:** `false` (allows cron and manual invoke with the anon key; do not expose the service role key in clients)

## Required Edge Function secrets

Set in the Supabase Dashboard (**Project Settings → Edge Functions → Secrets**) or via CLI. The function reads these first:

| Secret | Required |
|--------|----------|
| `SUPABASE_URL` | Yes — `https://<project-ref>.supabase.co` |
| `SUPABASE_SERVICE_ROLE_KEY` | Yes — service role (server only; never commit) |

Hosted runs inject `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` automatically. For local `supabase functions serve`, you may use a gitignored `.env.local` with the same names (see `.gitignore`).

Optional fallbacks in code (local only): `PROJECT_URL`, `SERVICE_ROLE_KEY`.

## Deploy

From the repo root (Supabase CLI logged in):

```bash
supabase link --project-ref YOUR_PROJECT_REF
supabase functions deploy scrape-bingo
```

Replace `YOUR_PROJECT_REF` with your project reference from the dashboard.

## Manual invoke

**CLI:**

```bash
supabase functions invoke scrape-bingo --project-ref YOUR_PROJECT_REF
```

**HTTP** (`verify_jwt = false` — use the **anon** key, not the service role):

```bash
curl -sS -X POST "https://YOUR_PROJECT_REF.supabase.co/functions/v1/scrape-bingo" \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json"
```

Success response includes `ok: true`, `drawDate`, `jackpot`, and `prizes`. On failure, check Edge Function logs and `scraper_logs` (below).

## SQL checks (Dashboard → SQL Editor)

Latest draw row (what the app should match):

```sql
select id, draw_date, jackpot, updated_at,
       coalesce(array_length(winning_numbers, 1), 0) as winning_number_count
from bingo_draws
order by draw_date desc
limit 3;
```

Recent scraper runs:

```sql
select status, error_message, created_at
from scraper_logs
order by created_at desc
limit 10;
```

After a successful invoke, expect a new `scraper_logs` row with a success message and `bingo_draws.updated_at` within the last few minutes for the current Berlin Sunday `draw_date`.

## Suggested cron schedule

Configure in **Dashboard → Edge Functions → scrape-bingo → Schedules** (or your project’s cron integration).

| Schedule | Cron | Purpose |
|----------|------|---------|
| Hourly | `0 * * * *` | Refresh `jackpot` / numbers during the week |
| After Sunday draw | `5 17 * * 0` | Run at 17:05 Sunday (Europe/Berlin) after the live draw |

Use **UTC** in the dashboard unless your scheduler is explicitly Berlin-local; adjust offsets accordingly.

The function targets the **latest Berlin Sunday** date when calling the public API (`/api/gewinnzahlen/{yyyy-MM-dd}`).

## App side (read-only)

- **Tables:** `bingo_draws`, `bingo_prizes`
- **Query:** `order=draw_date.desc&limit=1` with `SUPABASE_ANON_KEY` in `local.properties` (gitignored)
- **RLS:** anon role must be allowed `SELECT` on `bingo_draws` (and prizes if used)

If the app shows old jackpot values but no load error, the ingestion layer is stale — fix deploy/cron/logs above, not the Home UI.

## Troubleshooting

| Symptom | Check |
|---------|--------|
| `Missing Supabase credentials` in function response | Secrets / linked project; redeploy after setting secrets |
| `scraper_logs.status = failed` | Edge Function logs; API 404 for `draw_date`; DB permissions |
| App unchanged after DB update | Cold-start the app (draw loads once in `HomeViewModel.init`) |
| Empty `bingo_draws` | Run manual invoke once; confirm RLS allows service role upsert |
