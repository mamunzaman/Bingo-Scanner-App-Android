# PROJECT_SNAPSHOT

- **Current build status**
  - `./gradlew :app:assembleDebug` succeeds (verified).
  - Debug `DEMO_MODE=true`; release `DEMO_MODE=false`; release `isMinifyEnabled=false`.

- **Core tech stack**
  - Kotlin, Jetpack Compose, Material 3 (`app/build.gradle.kts`).
  - Navigation Compose, Room (KSP, schema v7 export), DataStore Preferences.
  - CameraX, GMS ML Kit Document Scanner, **ML Kit Text Recognition** (`text-recognition:16.0.0`), Coil Compose.

- **Implemented features**
  - Auth: `splash` → `auth/login` | `auth/forgot` | `auth/register` → `main`.
  - `MainTabsScreen`: Home, Scan, Jackpot, Profile tabs via `NavGraph` / `AppBottomBar`.
  - **Ticket OCR (on-device):** `ImportTicketImageOcr.analyzeUri` — grid crop, full-grid ML Kit + spatial/text parsers, optional tighter crop + deskew + perspective warp, **fixed 5×5 per-cell OCR** merged by score with whole-grid result, left-strip LOS/serial zone OCR + regex meta, `mergeStripMeta` into `HistoryImportOcrOutcome`.
  - **History photo import:** `historyPhotoImport` — GMS document scanner (Take Photo) → URI → `ImportTicketViewModel.analyzeTicketFromUri` → `ScanResultUiState.Success` (numbers, `losNumber`, `serialNumber`, `HistoryOcrSource.ML_KIT`); preview shows LOS/Serial when present; Continue → `buildManualEntryRoute` with row-major prefill + meta.
  - `directDocumentScan` → pending URI on `main` `SavedStateHandle` → `historyPhotoImport`.
  - `directScan` → `DirectScanScreen` (CameraX), separate from `historyPhotoImport`.
  - Manual entry: `manualEntry` / `manualEntryForRoom` with scanned numbers, `prefillOrder`, `ocrSource`, `ocrConfidence`, `losNumber`, `serialNumber` (`ManualEntryRouteHandoff.kt`).
  - Live: rooms, play, sheet detail, calls/tickets, `RoomRepository` + Room entities.
  - History: list, detail, add-from-photo → `historyPhotoImport`, CRUD via `HistoryRepository` (+ optional demo merge).
  - Profile/settings: `settings`, account, payments, support, password, legal/placeholder screens.
  - `ticket/{ticketId}` detail.
  - Debug: `DemoSeeder.seedIfNeeded()` from `MainActivity` when `DEMO_MODE`.

- **Active navigation routes** (`NavGraph.kt`)
  - `splash`, `auth/login`, `auth/forgot`, `auth/register`, `main`
  - `directScan`, `directDocumentScan`
  - `livePlayRoom/{roomId}`, `liveSheetDetail/{roomId}/{ticketId}`
  - `manualEntry?scannedNumbers=…&ocrSource=…&ocrConfidence=…&prefillOrder=…&losNumber=…&serialNumber=…`
  - `manualEntryForRoom/{roomId}?scannedNumbers=…&losNumber=…&serialNumber=…`
  - `history`, `historyPhotoImport?…`, `historyDetail/{sessionId}`
  - `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`
  - `ticket/{ticketId}`

- **Database entities + DAOs** (`AppDatabase` v7)
  - Entities: `LiveRoomEntity`, `RoomTicketEntity`, `RoomCalledNumberEntity`, `RoomSettingsEntity`, `TicketEntity`, `TicketCellEntity`.
  - DAOs: `LiveRoomDao`, `RoomTicketDao`, `RoomCalledNumberDao`, `RoomSettingsDao`, `TicketDao`.

- **Shared UI components** (non-exhaustive, `ui/components`)
  - Chrome: `AppTopBar`, `AppBottomBar`, `AppPrimaryButton`, `AppConfirmDialog`, `AppHeaderBackground`.
  - Bingo: `BingoCardGrid`, `BingoGrid5x5`, `BingoHeaderRow`, `CalledHistoryPanel`, `LiveCallInputBar`, `RoomSessionCard`.
  - Common: `SearchFilterSortHeader`, `EmptyHistoryState`, `TicketCard`, `ScanResultQualityCard`, etc.

- **Design rules currently used**
  - `.cursor/rules`: Material 3; tokens `theme/Color.kt`, `Dimens.kt`, `Typography.kt`; spacing scale 8/12/16/24/32; reuse shared composables; minimal unrelated edits.

- **Known bugs / inconsistencies**
  - `HistoryPhotoImportScreen` scan-result copy still includes a line implying automated read is “not available yet” while ML Kit OCR runs — **stale UX vs. behavior**.
  - No centralized bug tracker in repo.

- **Next logical tasks**
  - Align `HistoryPhotoImportScreen` placeholder/error copy with actual ML Kit OCR behavior.
  - OCR quality: tuning thresholds/fractions (`ImportTicketImageOcr`, `BingoNumberAnalyzer`); optional reduce ML Kit call count (cell grid = 25+ passes per pipeline).
  - Release: validate `assembleRelease`; enable R8 when ready (`isMinifyEnabled` currently false).
  - Optional: split scanner/import composables out of `NavGraph.kt`.
