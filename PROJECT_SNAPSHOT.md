# Project snapshot

- **Build:** `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (verified 2026-04-27 in this pass). Release: `isMinifyEnabled = false` (`app/build.gradle.kts`). Debug `BuildConfig.DEMO_MODE = true`; release `DEMO_MODE = false`.

## Core tech stack

- **Language / build:** Kotlin 2.2.10, AGP 9.0.1, KSP 2.3.6 (Room)
- **UI:** Jetpack Compose (BOM `2024.02.00`), Material 3, Activity Compose 1.9.0, Navigation Compose 2.7.7
- **Data:** Room 2.7.0, DataStore Preferences 1.0.0, Lifecycle 2.7.0
- **Media / ML:** CameraX 1.5.0, Coil Compose 2.5.0, ML Kit Text Recognition 16.0.0, GMS Document Scanner 16.0.0-beta1, uCrop 2.2.8

## Implemented features (from code)

- **App shell:** `MainActivity` → `NavGraph`; `DatabaseProvider`, `SettingsRepository`, `DemoSeeder` when `DEMO_MODE`
- **Cold start:** `splash` → `onboarding` (once via `SettingsRepository`) → `auth/login` → `main`
- **Main tabs (`main`):** `MainTabsScreen` — Home, Scan, Jackpot (live rooms), Profile; `AppBottomBar`
- **Auth:** `auth/login`, `auth/register`, `auth/forgot`
- **Scan:** `ScanScreen` — camera / pad handoff to `historyPhotoImport` and `manualEntry` (see `NavGraph` KDoc / `SCAN_ENTRY_HANDOFF_TAG`)
- **Manual entry:** `manualEntry?...` and `manualEntryForRoom/{roomId}?...` → `ManualEntryScreen`
- **Import / OCR:** `historyPhotoImport?...` → `HistoryPhotoImportScreen` + `ImportTicketViewModel` + `ImportTicketMainContent` pipeline
- **History:** `history`, `historyDetail/{sessionId}`; search/filter/sort; `HistorySheetCard` (list row, optional bulk selection, overflow menu, full-bleed stats strip); bulk delete/leave; optional bulk **Join live** when `NavGraph` passes `onJoinLiveRoom`
- **Live / Jackpot:** `LiveRoomsScreen`, `LivePlayScreen` (cards vs list view, `LiveRoomWithHistoryCard`, `BingoSheetsCarousel`, `ListSheetRow`, `MyTicketsBottomSheet`, `RoomInfoBottomSheet`, `RoomSettingsBottomSheet`, list bulk select + `BulkSelectionActionBar`); `LiveSheetDetailScreen` route `liveSheetDetail/{roomId}/{ticketId}`
- **Profile / legal:** `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`, `ticket/{ticketId}`

## Active navigation routes

- `splash`, `onboarding`, `auth/login`, `auth/forgot`, `auth/register`
- `main`
- `livePlayRoom/{roomId}`, `liveSheetDetail/{roomId}/{ticketId}`
- `manualEntry?...`, `manualEntryForRoom/{roomId}?...`
- `history`, `historyPhotoImport?...`, `historyDetail/{sessionId}`
- `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`, `ticket/{ticketId}`

(Exact query parameter names — `NavGraph.kt`.)

## Database (Room)

- **DB:** `AppDatabase` **v7**, `exportSchema = true` → `app/schemas/`
- **Entities:** `LiveRoomEntity`, `RoomTicketEntity`, `RoomCalledNumberEntity`, `RoomSettingsEntity`, `TicketEntity`, `TicketCellEntity`
- **DAOs:** `LiveRoomDao`, `RoomTicketDao`, `RoomCalledNumberDao`, `RoomSettingsDao`, `TicketDao`
- **Access:** `DatabaseProvider` + repository `object` singletons (`TicketRepository`, `HistoryRepository`, `RoomRepository`, `SettingsRepository`, …)

## Shared UI components (representative)

- **Theme / tokens:** `theme/Color.kt`, `Dimens.kt`, `Typography.kt`, `MamunBingoTheme.kt`; `Dimens.outlineDividerAlpha` / `outlineBorderAlpha` for hairlines
- **Layout chrome:** `AppTopBar`, `AppBottomBar`, `AppHeaderBackground`, `AppHeaderPageLayout`, `AppPrimaryButton`, `AppConfirmDialog`, `AppScreenHeader`, `AppSectionHeader`
- **Bingo:** `BingoCardGrid`, `BingoGrid5x5`, `BingoHeaderRow`, `BingoSheetSection`, `CalledHistoryPanel`, `LiveCallInputBar` / `LivePlayCallKeypad`, `MiniBingoGrid` (shared 5×5 preview)
- **Live / rooms:** `LiveRoomTopBar`, `RoomSessionCard`, `RoundProgressCards`, `JackpotHeroCard`, `RoomConflictDialog`, `BingoWinBanner`, `AlmostBingoAlertRowV2`
- **History / import:** `HistorySheetCard`, `SearchFilterSortHeader`, `EmptyHistoryState`, `EmptyHistoryActionCards`, `ImportTicketMainContent` (in `ImportTicketScreen.kt`), `ImportTicketScanResultContent`, `ScanResultSummaryCard`, `LabelValueInfoRow`
- **Bulk actions:** `BulkSelectionActionBar`, `LeaveRoomBulkConfirmDialog`, `DeleteFromHistoryBulkConfirmDialog` (`BulkSelectionConfirmDialogs.kt`)

## Design rules currently used

- Material 3; design tokens in `Color.kt` / `Dimens.kt` / `Typography.kt`
- **Cursor rules:** `.cursor/rules/ui-rule.mdc` (M3, tokens, reuse components, spacing scale 8/12/16/24/32, minimal diffs), `.cursor/rules/project-rule.mdc` (minimal patch, no unrelated refactors)

## Known bugs

- **No in-repo issue tracker** — treat `NEXT_TASK.md` / `PROJECT_STATUS.md` as the living QA / verification list (OCR, devices, GMS, gallery paths).

## Next logical tasks

- See **`NEXT_TASK.md`** (current device QA: Live sheet overlays on cards view; optional live list bulk regression)
- `PROJECT_STATUS.md` — “Pending tasks” for broader device QA and settings cleanup
- `TECH_DEBT.md` — technical follow-ups (docs vs code, import nav args, etc.)
