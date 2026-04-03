# LAST_SESSION

- **Date**
  - 2026-04-02

- **Today’s changes** (this doc refresh)
  - Full-project scan; regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from **current** sources only (no invented features).

- **Files modified** (this request)
  - `PROJECT_SNAPSHOT.md`
  - `LAST_SESSION.md`
  - `TECH_DEBT.md`

- **Codebase facts captured**
  - On-device ML Kit OCR pipeline lives in `ImportTicketImageOcr.kt` (whole-grid + cell 5×5 merge, crop/deskew/perspective, LOS/serial strip); `ImportTicketViewModel.analyzeTicketFromUri`; `historyPhotoImport` → `buildManualEntryRoute` with LOS/Serial; `HistoryPhotoImportScreen` shows LOS/Serial when present.

- **What is unfinished**
  - Stale strings on `HistoryPhotoImportScreen` vs. real OCR (see `PROJECT_SNAPSHOT` known issues).
  - Upside-down ticket handling explicitly ignored in OCR comments/rules.
  - Release shrink/minify still off.

- **Exact resume point**
  - Fix outdated scan-result copy in `HistoryPhotoImportScreen.kt` (success branch text under “SCAN RESULT”).
  - Then rerun `./gradlew :app:assembleDebug` and manual spot-check: photo import → numbers + LOS/Serial → Manual Entry.
