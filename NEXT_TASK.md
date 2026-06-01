# Next task

**Goal:** Supabase: deploy `scrape-bingo`, invoke once, confirm `scraper_logs` + `bingo_draws.updated_at` (see `supabase/README.md`).

**Verify:** Cron scheduled; Home jackpot matches DB after cold start.

**Previous:** `supabase/README.md` — deploy, env vars, SQL checks, cron; `.env.local` gitignored.

**Verify:** Live Play — top scan + create icons; Quick Actions 4 tiles (incl. Archived); horizontal scroll on narrow width. `./gradlew :app:assembleDebug` OK.

**Previous:** Live Rooms Quick Actions + top bar polish (Archived in row; direct scan + create room icons).

**Previous:** My Tickets `TicketRowCard` uses shared list preview; `TicketPickerMiniGridCell` on picker model.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
