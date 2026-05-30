# Next task

**Goal:** Device QA — Home scan shows type sheet before camera.

**Verify:** Home Scan Ticket, jackpot card scan, FAB → type sheet → camera with selected type; Jackpot/Scan tab unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** Home restored `ScanTypeSelectionSheet`; `onLaunchCamera(selectedType)` via existing NavGraph staging.
