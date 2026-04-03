# Status bar insets audit

## A) Header inventory

| Location | Header component | Insets applied |
|----------|-------------------|----------------|
| **MainTabsScreen** | None (no topBar) | Tab content draws from y=0; each tab’s first child is the header |
| **HomeScreen** | AppTopBar | AppTopBar only (`WindowInsets.statusBars`) |
| **ScanScreen** | AppTopBar | AppTopBar only |
| **LiveRoomsScreen** (Jackpot tab) | AppTopBar (in content) | AppTopBar only; Scaffold `contentWindowInsets=0`, only bottom padding used |
| **ProfileScreen** (Profile tab) | AppTopBar (in content) | AppTopBar only; Scaffold `contentWindowInsets=0`, `padding(innerPadding)` on Column |
| **HistoryListScreen** | AppTopBar | AppTopBar only |
| **HistoryDetailScreen** | AppTopBar (in content) | AppTopBar only; Scaffold `contentWindowInsets=0`, `padding(paddingValues)` on Column |
| **LivePlayScreen** | LiveRoomTopBar → AppTopBar (topBar slot) | AppTopBar only; Scaffold `contentWindowInsets=0`, `padding(innerPadding)` on Box |
| **LiveSheetDetailScreen** | AppTopBar | AppTopBar only |
| **TicketDetailScreen** | AppTopBar (in content) | AppTopBar only; Scaffold `contentWindowInsets=0`, `padding(innerPadding)` on Column |
| **ManualEntryScreen** | AppTopBar | AppTopBar only |
| **SettingsScreen** | AppTopBar | AppTopBar only |
| **AppScreenHeader** (unused) | Custom Row | `windowInsetsPadding(WindowInsets.statusBars)` + Dimens padding |

**Scaffold(topBar=...):** Only LivePlayScreen (topBar = LiveRoomTopBar). LiveRoomTopBar delegates to AppTopBar.

**statusBarsPadding() / windowInsetsPadding(WindowInsets.statusBars):** Only in AppTopBar (via CenterAlignedTopAppBar) and AppScreenHeader. No screen applies these again.

## B) Diagnosis

- **Missing inset:** None. Every screen that shows a header uses AppTopBar or AppScreenHeader; both apply status bar insets.
- **Double inset:** None. All Scaffolds use `contentWindowInsets = WindowInsets(0,0,0,0)`; no screen adds extra top padding for the status bar.
- **innerPadding ignored:** None. ProfileScreen, TicketDetailScreen, HistoryDetailScreen, LivePlayScreen apply Scaffold content padding. MainTabsScreen and LiveRoomsScreen only need bottom padding (no topBar); they use it correctly.

## C) Single source of truth (current)

- **AppTopBar** is the single place that applies status bar insets for TopAppBar-style screens (via `windowInsets = WindowInsets.statusBars`).
- **LiveRoomTopBar** uses AppTopBar → same behavior.
- **AppScreenHeader** applies insets only when used (currently unused).
- Screens must not add `statusBarsPadding()` or equivalent; Scaffolds use zero content window insets so the header is the only consumer.

## D) Main tabs vs inner screens

- Main tabs: No separate “MainTabsScreen header”. Each tab (Home, Scan, Jackpot, Profile) is full-size; first child is AppTopBar. Same inset behavior as inner screens.
- Inner screens: Same pattern (Column/Scaffold content → AppTopBar as first or in topBar). No extra top padding.

## E) Final rule

**Status bar inset is applied ONLY in AppTopBar (and in LiveRoomTopBar only via AppTopBar; and in AppScreenHeader when that component is used). Screens must not add extra top padding for the status bar.**
