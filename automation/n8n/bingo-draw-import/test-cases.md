# Bingo draw import — test cases

Use **Manual Trigger**, `dryRun: true` first, and `sample-response.json` in a pinned HTTP node only in a private n8n copy (do not pin production data in the shared export).

| # | Case | Expected |
|---|------|----------|
| 1 | Completed draw with 22 valid numbers | `draw` present; `winning_numbers.length === 22`; route import or dry_run |
| 2 | Official 404 | No CMS POST; `status: no_op`, `reason: official_draw_not_available` |
| 3 | Fewer than 22 numbers | `draw` omitted; import only if jackpot and/or `next_draw_at` are safe; else no-op `no_safe_importable_data` |
| 4 | Duplicate `drawNumber` | `draw` omitted (`duplicate_draw_number`) |
| 5 | Number outside 1–75 | `draw` omitted |
| 6 | Scrambled array, correct `drawIndex` | Numbers sorted by `drawIndex` (see `sample-response.json`) |
| 7 | Duplicate unreliable `index` fields | Ignored; order still follows `drawIndex` |
| 8 | Valid `jackpotNew` | `current_jackpot_eur` from `jackpotNew` |
| 9 | Invalid `jackpotNew`, valid `jackpotCurrently` | Jackpot from `jackpotCurrently` |
| 10 | Both jackpots invalid | Omit `current_jackpot_eur`; do not send 0 |
| 11 | Sunday before 17:00 Berlin | Automatic date = **previous** Sunday |
| 12 | Sunday at/after 17:00 Berlin | Automatic date = **today** |
| 13 | CET date (e.g. March) | Berlin calendar + `next_draw_at` offset without hardcoded `+01:00` |
| 14 | CEST date (e.g. July) | Same; offset must follow DST |
| 15 | Manual historical Sunday | Fetch that date; omit jackpot/`next_draw_at` unless `updateStatusDuringBackfill` |
| 16 | BingoBlogs 401 | No throw to other workflows; `status: failed`, `message` credential rejected |
| 17 | BingoBlogs 422 | `status: failed`, payload rejected |
| 18 | Dry run | Fetch + normalize; **Import into BingoBlogs** does not run |
| 19 | Workflow JSON has no secret | No `BINGO_IMPORT_TOKEN`, no 64-char token, no Authorization secret |
| 20 | Android / Supabase unchanged | This package only under `automation/n8n/bingo-draw-import/` |
