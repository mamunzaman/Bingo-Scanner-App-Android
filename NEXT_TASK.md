# Next task

**Goal:** Optional: uCrop status bar overlap on device if still reported.

**Verify:** Bottom nav + Live Rooms top bar show “Live Play” (EN) / “Live-Spiel” (DE); tab behavior unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** Bottom nav label Jackpot → Live Play (`tab_jackpot`, `live_nav_title` EN/DE); routes/icons/logic unchanged.

**Previous:** My Tickets `TicketRowCard` uses shared list preview; `TicketPickerMiniGridCell` on picker model.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
