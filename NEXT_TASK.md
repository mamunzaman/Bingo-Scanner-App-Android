# Next task

**Goal:** Migrate **My Tickets** bottom sheet rows to `ActiveTicketCompactSheetPreview` (needs 25-cell data on `TicketUiModel`).

**Verify:** Live Play list view matches History (grid + LOS/SERIE); selection/checkbox/tap OK; carousel unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** Live Play `ListSheetRow` → shared compact preview + `ActiveTicketLosSerieRow`; removed SERIAL/LOS/MARKED footer.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
