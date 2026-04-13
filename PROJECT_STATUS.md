# Project status

**Last update:** 2026-04-12 — **History bulk bar:** `BulkSelectionActionBar` + `navigationBarsPadding()` when replacing bottom bar on `HistoryListScreen`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 — **Handoff docs:** Regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from codebase; `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 — **Premium UI polish (tokens)**: `Dimens.outlineDividerAlpha` / `outlineBorderAlpha` for shared hairlines. `BulkSelectionActionBar` — top-rounded dock shape, soft border, no inner divider. `AppBottomBar` top hairline uses divider alpha. `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog` — `surfaceContainerHigh`, `radiusXL`, `titleLarge` + `bodyMedium`.

**Previous:** 2026-04-12 — **Bulk add to room (My Tickets)**: `BulkSelectionActionBar` optional `FilledTonalButton` “Add to room (n)” for unassigned selected tickets; `onBulkAddToRoom` from `LivePlayScreen`; per-row Add/Go Live hidden in selection mode.

**Previous:** 2026-04-12 — **Standard bulk selection UX**: Shared `BulkSelectionActionBar` (outline “Remove from room”, destructive “Delete”) + `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog`. History: selection title (“Select items” / “n selected”), back clears selection, actions bar replaces tab bar when selecting, snackbars on success. My Tickets: header “Done”, bottom action bar + snackbars.

**Previous:** 2026-04-12 — **Bulk leave vs bulk delete**: `RoomRepository.unassignTickets`. **My Tickets** (live): **Leave (n)** = unassign from current room only; **Delete (n)** = history only (copy clarified). **History list**: same **Leave** / **Delete** split; `onBulkLeaveSessions` (default loops `onLeaveRoom`).

**Previous:** 2026-04-12 — **Bulk delete (history + live My Tickets)**: `HistoryRepository.deleteSessions`. `RoomSessionCard` — selection mode (checkbox, primary border), hides row actions in select mode. `HistoryListScreen` — **Select** / **Cancel** / **Delete (n)** + confirm dialog; optional `onBulkDeleteSessions`. `MyTicketsBottomSheet` — same pattern; `LivePlayScreen` passes `HistoryRepository.deleteSessions`.

**Previous:** 2026-04-09 — **Shared interaction system**: Created `ui/core/interaction/` — `AppAnimation` (duration/easing tokens), `AppRipple` (shared ripple factory), `AppClick` (`Modifier.appClickable`). Migrated `RoomSessionCard` (2 clickables) and `RoundProgressCards` to `appClickable`.

**Previous:** 2026-04-09 — **Typography hierarchy audit**: Added `titleMedium` (SemiBold, 16sp) to custom `Typography` — was falling to M3 default (Medium). Removed redundant `.copy(fontWeight = SemiBold)` in `AppSectionHeader`, `ImportTicketScanResultContent`, `LiveRoomsScreen`. Fixed `LiveRoomsScreen` "Recent Rooms" Bold → theme SemiBold to match "Quick Actions".

**Previous:** 2026-04-09 — **Controls consistency pass**: `StatusPill`/`RoomStatusPill` — removed `heightIn(48dp)`, `radiusSmall` → `radiusPill`, vertical padding `spacing8` → `spacing4` (matches `StatusChip`). `RoomSessionCard` — `cardElevationDefault` (6dp) → `cardElevationSubtle` (2dp); footer divider `alpha 0.4f` → `0.28f`; `MiniBingoGrid` border softened to `alpha 0.34f`.

**Previous:** 2026-04-08 — App-wide: screens with **`AppHeaderBackground`** use **`MaterialTheme.colorScheme.surface`** on root **`Scaffold`** or **`Box`/`Column`** (Profile, Settings, Manual Entry, Live rooms, Ticket detail, Live sheet detail, History photo import, legal, profile sub-screens, auth register/forgot, **`MyAccountScreen`**). **`HistoryDetailScreen`** same pattern. **`BingoSheetSection` `premiumLayered`**: **`shadowElevation` 0**; soft tonal outer/inner strokes; **`BingoCardGrid`** history compact enables it. **History detail** bingo: **`BingoCardGrid`** `historyDetailCompact` wraps sheet in **`width(bingoGridWidth + 32dp)`** centered; **`BingoGridCard`** no double-wrap on success. **`LabelValueInfoRow`** + **`LabelValueInfoRowVariant`** (Default / Compact): **`labelSmall`** muted **`onSurfaceVariant`**; **`outlineVariant`** divider; History **Ticket Information** uses it + **bodyMedium**/**SemiBold** values, Status **Primary** accent. **History detail** bingo slot: **`Column`** + **`weight(1f)`** → **`BoxWithConstraints`** → **`BingoGridCard`**; **`BingoCardGrid`** uses inner max minus sheet padding for cell sizing; cell = **min** of width- and height-derived size (**22–44dp**). No breakpoint / **`graphicsLayer`**. **History detail** `AppTopBar`: default title; live CTA + delete in **`actions`** row (`HistoryDetailHeaderActions`; under **360dp** screen: icon-only live). **History detail** `BingoCardGrid` **`historyDetailCompact`**: `BoxWithConstraints` width-based cell size (22–44dp), tight gaps; **`BingoGrid5x5`** **`fixedLayoutCellSize`** + **`FixedPlayModeGrid`** (no 1:1 aspect block). **History detail** top: compact spec — **IconContainerBg**/**Primary** active + live/leave pills; flat **TICKET INFORMATION** (**Outline** 9sp label, **OnSurface** rows, **Outline** hairlines); stats **`SpanStyle`** bold sheet count + tail; **WarningBorder** top strip + **WarningContainer** row + **28dp** **WarningIcon** slot + **`MiniBingoPreview`** + score pill (**OnPrimary** on **WarningIcon**); **`CalledHistoryPanel`** after strip (not in ticket block). **Live card:** light **F6F8F4 → F0F5EC**; **no** shadow. **Pills:** **LIVE** — soft green fill + green rim; **6dp** dot **`Color.Red`**, infinite **scale 1→1.4** + **α 1↔0.6** (**1200ms** `FastOutSlowIn`, reverse), × parent **`dotAlpha`**; text **SemiBold**. **Counter** — lighter vs LIVE. **LazyRow:** **12dp** horizontal **contentPadding**; **`spacedBy(8dp)`**; **`rememberSnapFlingBehavior`**; **`animateScrollToItem`** with **~−10dp** `scrollOffset`. **`CalledHistoryPanelContext.HistoryDetail`** uses **`takeLast(MAX_LIVE_CALLS)`**. No **`LiveCallNumberCircles.kt`**.

## Completed features

- **Shared `LabelValueInfoRow`**: reusable ticket-style label/value rows with optional compact vertical rhythm.
- **History detail layout**: fixed column (no page scroll); root **`Scaffold`** **`surface`** page fill (no header/background seam); bingo slot **`weight(1f)`** + constraint-based cell size (no breakpoint **`graphicsLayer`**).
- **History detail header actions**: live navigation in top bar; no duplicate-session row under grid.
- **History detail bingo grid**: compact layout from available width; proportional header letters; full grid visible without square aspect-ratio block (History only); optional **`BingoSheetSection` `premiumLayered`** (subtle elevation + nested inner plate).
- **Navigation cold start**: `splash` (rain every launch; ~3.45s) → `onboarding` once (`SettingsRepository.onboarding_completed`), then `auth/login`; faster rain + 2.4s slides; splash logo: Eco mark with halo pulse + breathe/sway; gradient “BINGO” title with left-to-right stagger.
- **Import ticket scan UI count**: `finalUiGridRowMajor` normalizes OCR row-major to 25 cells in `setScanResult`; `displayedCount` / quality card / History photo import summary use non-zero count on that grid only; debug log `ImportTicketFinalUi` at VM handoff.
- **`ImportTicketImageOcr`**: analyzer grid crop via `BingoNumberAnalyzer.tryDetectBingoGridCropForOcr`; heuristic fallback + `heuristicGridCrop` log; gallery `bypassInternalGridCrop`; final dedup; `rankRawStagesForFallback` / `fallbackPick`; `finalGridSanity` / tiers; `finalQualityCheck`; `stagePickAdjust`; +4 guard; `finalDuplicateCheck`; `attachLeftStripMeta` + `metaDebug`.
- **`LeftStripMetaOcr`**: 0°/90°/270°; line-ordered text from blocks/lines; Losnummer/Seriennummer centers + digit runs (line/element + vertical single-digit merge); proximity assignment; 5/4 length fallback; then existing regex/heuristic on normalized text; `metaRaw`/`metaNormalized`/`metaCandidates`/`metaChosen` — isolated from grid OCR.
- **Live Navigation header** (`LiveRoomsScreen`): `AppTopBar` `titleContent` = title + `Spacer(Modifier.weight(1f))` + 40dp primary `Add` (same pattern as History) → `showCreateDialog = true`.
- **Import Ticket UI**: shared **`ImportTicketMainContent`**; success → **Manual Entry** prefill via nav (no inline review card); **`ScanResultSummaryCard`** on error/loading only.
- **Import / History photo header polish**: **`AppHeaderBackground`** vertical greens (light) + quieter strokes; **`HeroBannerCard`** no shadow, tinted fill, **outline** rim + soft dashed; **`ImportTicketFailedScanContent`** / info row / prescan tips surfaces aligned; **`HistoryPhotoImportScreen`** content inset + calmer info icon.
- **`TicketInfoCard`** / **`TicketInfoStatusChip`**: **#F7F8F6** fill, **black 0.05** border, **18dp** corners, no elevation; **`HistoryDetailScreen`** ticket block = divider-separated **label / value** rows (no chips); **`LiveSheetDetailScreen`** still uses **`TicketInfoCard`** where applicable.
- **Import gallery / take photo**: Gallery pick → **uCrop** (freestyle, 3:4, bottom controls on, ALL gestures, max scale 28) → **`setGalleryPendingEdit`** → **Apply** / **Discard** → OCR. Camera/GMS unchanged.
- **Live play header**: compact **`GreenCard`** / **`GreenCardCompact`** — neutral container, **LIVE** + bordered count chip; duplicate big-call UI removed in favor of **`CalledHistoryPanel`**.
- **Live status pills** (`LiveRoomTopSection`): LIVE green-tint chip + green rim, stronger dot/text, slow dot breath; counter de-emphasized vs LIVE.
- **`CalledHistoryPanel` motion**: new last-call only — main circle scale+alpha in; LazyRow “latest” soft fade in when it becomes latest; inactive circles static.
- **`CalledHistoryPanel` row**: horizontal **content** inset + scroll to active (last) call with breathing offset; no gradient fade overlay.
- **`AppHeaderPageLayout`**: shared screen wrapper for `AppHeaderBackground`-based screens — `surface` root fill + gradient band (top 40%) + `Column(topBar, content)`; migrated HomeScreen, HistoryListScreen, ProfileScreen, SettingsScreen, MyAccountScreen, LiveRoomsScreen, LoginScreen, LiveSheetDetailScreen.

## In progress

- Device QA: verify `AppHeaderPageLayout` on Home, HistoryList, Profile, Settings, MyAccount, LiveRooms — no gray seam, no flat white header.

## Pending tasks

- Revisit **Settings → Live header style** (orphaned for live header) — remove UI or repurpose.
- Device QA: **PickVisualMedia** Gallery + GMS Take photo on `historyPhotoImport`; NavGraph pending-URI handoff from main/Jackpot.
- Device QA: hero suppressed on auto Manual Entry (unchanged).
- Rescan tickets; confirm `ImportTicketOcr` / `ImportTicketFinalUi` logs vs grid if issues.
- Tune `MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV` if needed (currently 20).
