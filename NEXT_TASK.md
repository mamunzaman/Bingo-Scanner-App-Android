# Next task

**Goal:** Device QA — all three scan types after log cleanup (camera + gallery handoff unchanged).

**Verify:** Scan tab → each of PLAY_PAPER / ONLINE / MAIN_SHEET → capture → import screen shows grid; gallery path preserves type through uCrop Apply; Logcat has no `scan-entry-handoff` / `ImportTicketAnalyze` spam (only `ImportTicket` / `ImportTicketGallery` warnings on real failures); `./gradlew :app:assembleDebug` green.

**Done (status):** Removed temporary scanner debug logs and dead handoff trace code; kept VM `when` routing to `PlayPaperBingoOcr` / `OnlineBingoOcr` / `MainSheetBingoOcr`; failure logs only in VM + camera. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — MAIN_SHEET gallery import matches camera OCR routing.

**Verify:** History → Gallery → Bingo Main Sheet → uCrop → Apply → OCR uses `MainSheetBingoOcr`; `./gradlew :app:assembleDebug` green.

**Done (status):** Gallery flow snapshots scan type when uCrop returns; Apply passes explicit `scanType` into `onPhotoTaken` and `analyzeTicketFromUri`; `MAIN_SHEET` OCR always uses `bypassInternalGridCrop=true` at VM boundary. `./gradlew :app:assembleDebug` OK.
