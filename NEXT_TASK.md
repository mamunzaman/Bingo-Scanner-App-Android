# Next task

**Goal:** Alpha Phase 4B — tokenize disabled / text / scrim alphas (not divider/border); start with highest-drift screens from audit.

**Verify:** `./gradlew :app:assembleDebug`; spot-check Live Play + Settings unchanged aside from intended divider tweaks.

**Previous:** **Home spacing** — `HomeTopBar` aligned with Live Play (`screenHorizontalPadding`); content top/bottom insets match Profile shell; section gaps 16/8. Build OK.

**Previous:** Supabase `scrape-bingo` deploy + cron (see `supabase/README.md`).

**Verify:** Live Play — top scan + create icons; Quick Actions 4 tiles (incl. Archived); horizontal scroll on narrow width. `./gradlew :app:assembleDebug` OK.

**Previous:** Live Rooms Quick Actions + top bar polish (Archived in row; direct scan + create room icons).

**Previous:** My Tickets `TicketRowCard` uses shared list preview; `TicketPickerMiniGridCell` on picker model.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
