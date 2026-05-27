# Next task

**Goal:** Device QA — full-screen analyzing state on a real device scan.

**Verify:** Scan/import → OCR running → thumbnail + Take Photo + Gallery all hidden; dark green gradient full screen; scan line sweeps; crosshair floats; bottom white "Processing Data..." card with progress bar and NODES/CONFIDENCE stats. After OCR completes → result/error screens appear normally. `./gradlew :app:assembleDebug` green.

**Done (status):** `HistoryPhotoImportScreen` — when `isAnalyzingUi`, content area replaced by `ImportTicketAnalyzingFullScreen` (dark gradient + `ScanningAnalysisAnimation` + `ProcessingDataCard`); normal content skipped entirely. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — all three scan types after log cleanup.

**Verify:** Scan tab → PLAY_PAPER / ONLINE / MAIN_SHEET → capture → grid; gallery preserves type; no handoff log spam. `./gradlew :app:assembleDebug` OK.

**Done (status):** Scanner debug log cleanup; per-type OCR routing unchanged. `./gradlew :app:assembleDebug` OK.
