# Next task

**Goal:** Migrate **My Tickets** bottom sheet rows to `ActiveTicketCompactSheetPreview` (needs 25-cell data on `TicketUiModel`).

**Verify:** Ticket in Room 1 hidden from Room 2 picker; filter counts = unassigned only. `./gradlew :app:assembleDebug` OK.

**Previous:** My Tickets picker hides sheets assigned to any live room (one sheet = one room).

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
