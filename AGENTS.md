# MamunBingoApp — AI / engineer map

Kotlin **Jetpack Compose** + **Material 3** bingo ticket app. Package: `com.example.mamunbingoapp`.

## Quick pointers

| Doc | Use |
|-----|-----|
| `NEXT_TASK.md` | Current MVP goal + manual verify checklist |
| `PROJECT_STATUS.md` | Completed / pending features (short) |
| `TECH_DEBT.md` | Known gaps, route/VM alignment notes |
| `.cursor/rules/*.mdc` | UI tokens, shared components, minimal-patch rules |

## Entry & shell

- **`MainActivity.kt`** — `DatabaseProvider`, `SettingsRepository`, `DemoSeeder`; theme via `ThemeViewModel`; **`NavGraph`** root.
- **Theme** — `theme/MamunBingoTheme.kt`, **`theme/Color.kt`**, **`theme/Dimens.kt`**, **`theme/Typography.kt`** (`AppTextStyles` for shared text like `importTicketCapsLabel`).

## Navigation (`navigation/NavGraph.kt`)

**Cold start:** `splash` → `onboarding` (once) → `auth/login` → **`main`** (tabs).

**Registered routes (high level):**

- `main` — **`MainTabsScreen`** (bottom nav: Home, Scan, Jackpot, Profile). Scan/Jackpot camera uses GMS + pending URI → **`historyPhotoImport`** (no `directScan` route).
- **`historyPhotoImport`** — **`HistoryPhotoImportScreen`** + shared **`ImportTicketViewModel`**; scan/import UI is **`ImportTicketMainContent`** (not a separate `ImportTicketScreen` route — see `TECH_DEBT.md`).
- `manualEntry` / `manualEntryForRoom/{roomId}` — **`ManualEntryScreen`**.
- `history` — **`HistoryListScreen`**.
- `historyDetail/{sessionId}` — **`HistoryDetailScreen`**.
- `livePlayRoom/{roomId}` — **`LivePlayScreen`**.
- `liveSheetDetail/{roomId}/{ticketId}` — **`LiveSheetDetailScreen`**.
- `ticket/{ticketId}` — **`TicketDetailScreen`**.
- Profile stack: `settings`, `myAccount`, `paymentMethods`, `support`, `changePassword`, `locationServices`, `environmentalImpact`, `termsOfService`, `privacyPolicy`.

**NavGraph KDoc** at top of file documents scan vs history import handoff (`SCAN_ENTRY_HANDOFF_TAG`, `savedStateHandle` tab switching).

## UI screens (`ui/screens/`)

| Area | Folder / files | Notes |
|------|------------------|--------|
| Tabs shell | `MainTabsScreen.kt` | Switches `HomeScreen`, **`scan/ScanScreen`**, **`live/LiveRoomsScreen`**, **`profile/ProfileScreen`**. |
| Scan tab | `scan/ScanScreen.kt` | Launch camera → `historyPhotoImport`; number pad → `manualEntry`. |
| Import / OCR review | `history/HistoryPhotoImportScreen.kt` | Uses **`ImportTicketMainContent`**; same VM as import flow in `NavGraph`. |
| Import UI (shared) | `ImportTicketScreen.kt` | Defines **`ImportTicketMainContent`**, **`ImportTicketScreen`** (preview / optional embed); post-scan UI in **`ui/components/ImportTicketScanResultContent.kt`**. |
| Manual grid | `manual/ManualEntryScreen.kt` | Handoff: `ManualEntryRouteHandoff.kt`. |
| History | `history/HistoryListScreen.kt`, `HistoryDetailScreen.kt` | |
| Live / Jackpot | `live/LiveRoomsScreen.kt`, `LivePlayScreen.kt`, `LiveSheetDetailScreen.kt` | |
| Home | `HomeScreen.kt` | |
| Auth | `LoginScreen.kt`, `RegisterScreen.kt`, `ForgotPasswordScreen.kt` | |
| Splash / onboarding | `SplashScreen.kt`, `OnboardingScreen.kt` | |
| Legal / profile detail | `legal/*`, `profile/*` | |

## Shared UI (`ui/components/`)

Reused building blocks: **`AppTopBar`**, **`AppBottomBar`**, **`AppTab`**, **`BingoCardGrid`**, **`BingoGrid5x5`**, **`BingoHeaderRow`**, **`LiveCallInputBar`**, **`CalledHistoryPanel`**, **`RoomSessionCard`**, **`SearchFilterSortHeader`**, **`HeroBannerCard`**, import: **`ScanResultSummaryCard`**, **`ImportTicketLosSerialCard`**, **`ImportTicketScanResultContent`**, etc.

## ViewModels (`viewmodel/`)

| VM | Typical use |
|----|-------------|
| `ImportTicketViewModel` | Photo URI, OCR pipeline, `ScanResultUiState` |
| `ManualEntryViewModel` | 5×5 entry, save |
| `HistoryViewModel` / `HistoryDetailViewModel` | History lists / detail |
| `LivePlayViewModel` | In-room play |
| `LiveRoomsViewModel` | Room list / create |
| `MainTabsViewModel` | Tab state scoped to `main` |
| `ThemeViewModel` | Light / dark / system |
| `MyTicketsViewModel`, `TicketDetailViewModel`, `JackpotViewModel`, `SettingsViewModel` | Feature-specific |

## Data (`data/`)

- **`TicketRepository`**, **`HistoryRepository`**, **`RoomRepository`**, **`SettingsRepository`**
- **`data/db/`** — Room (`AppDatabase`, `TicketEntity`, `DatabaseProvider`)
- **`data/preferences/`** — User prefs, live header style, etc.

## Scanner / OCR (`scanner/`)

- **`ImportTicketImageOcr.kt`** — ML Kit / Vision pipeline for ticket grid; logs `ImportTicketOcr` / `ImportTicketFinalUi`.
- **`LeftStripMetaOcr.kt`** — LOS / serial strip meta.
- **`BingoNumberAnalyzer.kt`**, **`BingoTicketParser.kt`** — Parsing helpers.

## History OCR bridge

- **`history/HistoryImageOcrProvider.kt`** — Wiring for history import sources.

## Core game logic

- **`core/BingoWinChecker.kt`** (and related) — Win detection for grids.

## Where to change what

- **Import ticket UI after scan** — `ImportTicketScanResultContent.kt`, `ImportTicketScreen.kt` (`ImportTicketMainContent`), `ScanResultSummaryCard.kt`, `ImportTicketLosSerialCard.kt`.
- **OCR accuracy / grid** — `ImportTicketViewModel.kt`, `scanner/ImportTicketImageOcr.kt`, `LeftStripMetaOcr.kt`.
- **Navigation / new route** — `NavGraph.kt` (+ deep links if any).
- **Tab / bottom bar** — `MainTabsScreen.kt`, `AppBottomBar.kt`, `AppTab`.
- **Live rooms / play** — `live/*` screens, `LivePlayViewModel`, `LiveRoomsViewModel`, `RoomRepository`.
- **History** — `history/*`, `HistoryViewModel`, `HistoryRepository`.
- **Theme / colors / spacing** — `theme/Color.kt`, `Dimens.kt`, `Typography.kt`, `MamunBingoTheme.kt`.

## Debug / temp flags (search in repo)

- **`IMPORT_TICKET_UI_RENDER_FAKE_ONLY`** — `ImportTicketScreen.kt`: forces fake success UI for layout work; turn **off** for real OCR.
- **`importTicketUiFakeSuccess`** — paired test data in same file.
- **`FORCE_IMPORT_TICKET_TEST_DATA`** — if present in `ImportTicketViewModel`, disable for production testing.

## Build

- Module: **`app`**. Typical: `./gradlew :app:assembleDebug`.
