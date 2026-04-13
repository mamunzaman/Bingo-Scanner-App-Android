# Project snapshot

- **Build:** `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (verified 2026-04-12; re-verified after `SettingsScreen` unused import). Release: `isMinifyEnabled = false`. Debug `BuildConfig.DEMO_MODE = true`; release `DEMO_MODE = false`.

## Core tech stack

- **Language / UI:** Kotlin 2.2.10, Jetpack Compose (BOM `2024.02.00`), Material 3, Navigation Compose 2.7.7
- **AndroidX:** Activity Compose 1.9.0, Lifecycle 2.7.0, Room 2.7.0, DataStore Preferences 1.0.0
- **Media / ML:** CameraX 1.5.0, Coil Compose 2.5.0, ML Kit Text Recognition 16.0.0, Play Services ML Kit Document Scanner 16.0.0-beta1, uCrop 2.2.8 (gallery crop)
- **Build:** AGP 9.0.1, KSP for Room

## Implemented features (from code)

- **Cold start:** `splash` → `onboarding` (once via `SettingsRepository`) → `auth/login`
- **Auth:** login (`AppHeaderPageLayout`), register, forgot password
- **Main tabs (`main`):** Home, Scan (`ScanScreen`), Jackpot (`LiveRoomsScreen`), Profile; `AppBottomBar`
- **Live:** `livePlayRoom/{roomId}`, `liveSheetDetail/{roomId}/{ticketId}`; calls, sheets, My Tickets bottom sheet; bulk selection with leave-room / delete / add-to-room where wired
- **Manual entry:** `manualEntry` (optional query: scannedNumbers, ocrSource, ocrConfidence, prefillOrder, losNumber, serialNumber), `manualEntryForRoom/{roomId}` (optional query: scannedNumbers, losNumber, serialNumber)
- **Import / OCR:** `historyPhotoImport` (optional query args). GMS document scan + gallery → uCrop → `ImportTicketViewModel` pipeline; `ImportTicketImageOcr`, `LeftStripMetaOcr`, `BingoNumberAnalyzer`; pending URI handoff from main/Jackpot; **`photoImportLeaveHandler`** registered via **`onRegisterLeaveHandler`** so bottom-tab switches use the same discard flow as back
- **History:** `history`, `historyDetail/{sessionId}`; search/filter/sort; bulk delete / leave; `HistoryRepository.deleteSessions`; selection-mode **`BulkSelectionActionBar`** uses **`navigationBarsPadding`** when it replaces **`AppBottomBar`**
- **Rooms:** `RoomRepository` + Room entities; `unassignTickets` for bulk leave from room
- **Profile / settings:** `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`, `ticket/{ticketId}` — dead **`LiveHeaderStyle`** preference chain removed (**`UserPreferencesRepository.kt`** deleted, **`MainActivity`** no longer inits it)
- **Demo:** `DemoSeeder` + `DemoDataFactory` when `DEMO_MODE`; `SettingsRepository.showDemoData` merges demo history when enabled; `RoomRepository.seedDemoData`

## Active navigation routes

- `splash`, `onboarding`, `auth/login`, `auth/forgot`, `auth/register`
- `main`
- `livePlayRoom/{roomId}`, `liveSheetDetail/{roomId}/{ticketId}`
- `manualEntry?...`, `manualEntryForRoom/{roomId}?...`
- `history`, `historyPhotoImport?...`, `historyDetail/{sessionId}`
- `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`
- `ticket/{ticketId}`

## Database (Room)

- **DB:** `AppDatabase` v7 (`exportSchema = true`)
- **Entities:** `LiveRoomEntity`, `RoomTicketEntity`, `RoomCalledNumberEntity`, `RoomSettingsEntity`, `TicketEntity`, `TicketCellEntity`
- **DAOs:** `LiveRoomDao`, `RoomTicketDao`, `RoomCalledNumberDao`, `RoomSettingsDao`, `TicketDao`
- **Access:** `DatabaseProvider` + repository `object` singletons

## Shared UI components (representative)

- **Theme / interaction:** `theme/Color.kt`, `Dimens.kt`, `Typography.kt`; `ui/core/interaction/` — `AppAnimation`, `AppRipple`, `AppClick` (`appClickable`)
- **Chrome:** `AppTopBar`, `AppBottomBar`, `AppHeaderBackground`, `AppHeaderPageLayout`, `AppCard`, `AppPrimaryButton`, `AppConfirmDialog`, `AppScreenHeader`, `AppSectionHeader`, `AppIconContainer`, `AppTextField`
- **Bingo:** `BingoCardGrid`, `BingoGrid5x5`, `BingoHeaderRow`, `BingoCell`, `BingoSheetSection`, `BingoNumberBox`, `CalledHistoryPanel`, `LiveCallInputBar`
- **Live / rooms:** `RoomSessionCard`, `RoundProgressCards`, `CreateNewRoomCardUC2`, `LiveRoomTopBar`, `JackpotHeroCard`, `RoomConflictDialog`, `AlmostBingoAlertRowV2`, `AlmostBingoPill`, `BingoWinBanner`
- **Bulk selection:** `BulkSelectionActionBar`, `LeaveRoomBulkConfirmDialog`, `DeleteFromHistoryBulkConfirmDialog` (`BulkSelectionConfirmDialogs.kt`)
- **Import:** `ImportTicketMainContent` / screen file `ImportTicketScreen.kt`, `ImportTicketScanResultContent`, `ScanResultSummaryCard`, `ScanResultQualityCard`, `ImportTicketLosSerialCard`, `TicketMetaField`
- **History / filters:** `SearchFilterSortHeader`, `SearchFilterBar`, `SortDropdown`, `EmptyHistoryState`, `EmptyHistoryActionCards`, `LabelValueInfoRow`
- **Other:** `StatusPill` / chips, `TicketInfoCard`, `TicketCard`, `AuthFooterPrompt`, `AuthScreenDecoration`, `EmptyStateBlock`, `ModifierExt`

## Design rules in use

- Material 3; tokens from `Color.kt` / `Dimens.kt` / `Typography.kt` (e.g. `titleMedium` SemiBold, `outlineDividerAlpha` / `outlineBorderAlpha` for hairlines); spacing scale 8 / 12 / 16 / 24 / 32 (`.cursor/rules/ui-rule.mdc`, `project-rule.mdc`)

## Known bugs

- No in-repo issue tracker; OCR/grid and device-specific paths (GMS, gallery) need ongoing QA. **`PROJECT_STATUS.md` / `NEXT_TASK.md`** list open verification items.

## Next logical tasks

- **`NEXT_TASK.md`** (numbered steps)
- Optional: tune `MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV` (20) after validation
- **`PROJECT_STATUS.md` pending:** broader gallery/camera + `historyPhotoImport` device QA
