# Project status

**Last update:** 2026-04-27 — **Live sheet preview bottom sheet — ticket QR:** `SheetDetailBottomSheet` (`LivePlayScreen`) header **QrCode2** 48dp → `TicketQrDialog` (encode `QrTicketPayload` from preview `cells` + `serial`/`los`, `QrTicketCodec` + `QrTicketImageGenerator` on **Default** dispatcher); **`TicketQrDialog`** optional **`isLoading`**. No scroll, no in-sheet QR, **“View full detail”** unchanged. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **`AppPrimaryButton` loading state:** `loading` uses centered **Row** (18dp `CircularProgressIndicator`, 2dp stroke, 8dp gap, **onPrimary** label from `text`); while loading, **disabled** colors keep primary + onPrimary (no washed-out capture CTA). Camera label **“Capturing”** (not ellipsis). **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **Live import camera — capture feedback:** `BingoLiveCameraImportScreen` — ~120ms **shutter** white flash (0→0.8→0), **preview/viewfinder** scale pulse (1→0.97→1) via `Animatable` + `graphicsLayer`, `Scan ticket` + `loading` + back **disabled** while `capturing`; `takePicture` + QR path unchanged. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **Custom CameraX + frame crop — stable (device QA + build):** (1) **QR auto-import** — OK. (2) **Scan ticket** — CameraX still **cropped to green frame** (`CameraXCaptureCrop` / `VIEWFINDER_*`), `historyPhotoImport` — OK. (3) **GMS fallback** — OK. (4) **Gallery / uCrop editor** — unchanged, OK. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **`BingoLiveCameraImportScreen` full-ticket CTA:** `animateFloatAsState` preview fade (220ms) + `AppPrimaryButton(loading)` + 240ms delay then existing `onScanFullTicket` (nav unchanged). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **Live camera Bingo QR:** `bingoLiveCameraImport` (CameraX + `tryDecodeBingoQrFromInputImage`, throttled “no QR” log) before GMS; Scan/Jackpot/History **Take photo** open it; success → same Manual Entry route as import (incl. room). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **History Detail QR:** `TicketQrDialog` under `ui/components/qr/`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **History photo import:** ML Kit **QR** before **OCR** (`ImportTicketQrPreOcr` + `QrTicketCodec`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **Live sheet detail bingo** matches **History** (`BingoDetailGridCard` / compact row / win banner). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 — **Live device QA complete**; **whole-app UI consistency audit** in `NEXT_TASK.md`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 (earlier) — **Handoff docs:** Regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from codebase scan; `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-20 — **Live cards-view overlay fix**: `LivePlayScreen` keeps the standard live bottom area visible for ⋮ bottom sheets (Info/Settings), so opening those sheets no longer reflows the cards-view background into a broken layout; root content animation remains removed. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-19 — `CalledHistoryPanel`: empty state uses same **72dp** row as active calls (`LatestCallCircle` + compact copy) so the live card does not jump when the first number is called. `./gradlew :app:compileDebugKotlin` OK.

**Previous:** 2026-04-19 — Live play ⋮ menu: **Reset game** opens the existing reset confirm; on confirm, calls clear to 0 and play can continue (`LiveRoomTopBar` + `LivePlayScreen`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-16 - History card micro polish: `HistorySheetCard.kt` now keeps the real Bingo preview/count while tightening spacing, shrinking the preview slightly, and balancing View/Join/Delete actions. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-15 â€” **Live list row polish (superseded):** elevated `Surface` + inset plate â€” replaced by flat reference-aligned panel above.

**Previous:** 2026-04-15 â€” **Detail sheet preview typography + alignment:** `SheetDetailBottomSheet` meta columns remain equal-width but are start-aligned; title uses stronger hierarchy vs softer meta labels; values are slightly larger with safer reserved-height estimate for short devices. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 â€” **Handoff docs:** Regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from codebase; `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 â€” **Premium UI polish (tokens)**: `Dimens.outlineDividerAlpha` / `outlineBorderAlpha` for shared hairlines. `BulkSelectionActionBar` â€” top-rounded dock shape, soft border, no inner divider. `AppBottomBar` top hairline uses divider alpha. `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog` â€” `surfaceContainerHigh`, `radiusXL`, `titleLarge` + `bodyMedium`.

**Previous:** 2026-04-12 â€” **Bulk add to room (My Tickets)**: `BulkSelectionActionBar` optional `FilledTonalButton` â€œAdd to room (n)â€ for unassigned selected tickets; `onBulkAddToRoom` from `LivePlayScreen`; per-row Add/Go Live hidden in selection mode.

**Previous:** 2026-04-12 â€” **Standard bulk selection UX**: Shared `BulkSelectionActionBar` (outline â€œRemove from roomâ€, destructive â€œDeleteâ€) + `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog`. History: selection title (â€œSelect itemsâ€ / â€œn selectedâ€), back clears selection, actions bar replaces tab bar when selecting, snackbars on success. My Tickets: header â€œDoneâ€, bottom action bar + snackbars.

**Previous:** 2026-04-12 â€” **Bulk leave vs bulk delete**: `RoomRepository.unassignTickets`. **My Tickets** (live): **Leave (n)** = unassign from current room only; **Delete (n)** = history only (copy clarified). **History list**: same **Leave** / **Delete** split; `onBulkLeaveSessions` (default loops `onLeaveRoom`).

**Previous:** 2026-04-12 â€” **Bulk delete (history + live My Tickets)**: `HistoryRepository.deleteSessions`. `RoomSessionCard` â€” selection mode (checkbox, primary border), hides row actions in select mode. `HistoryListScreen` â€” **Select** / **Cancel** / **Delete (n)** + confirm dialog; optional `onBulkDeleteSessions`. `MyTicketsBottomSheet` â€” same pattern; `LivePlayScreen` passes `HistoryRepository.deleteSessions`.

**Previous:** 2026-04-09 â€” **Shared interaction system**: Created `ui/core/interaction/` â€” `AppAnimation` (duration/easing tokens), `AppRipple` (shared ripple factory), `AppClick` (`Modifier.appClickable`). Migrated `RoomSessionCard` (2 clickables) and `RoundProgressCards` to `appClickable`.

**Previous:** 2026-04-09 â€” **Typography hierarchy audit**: Added `titleMedium` (SemiBold, 16sp) to custom `Typography` â€” was falling to M3 default (Medium). Removed redundant `.copy(fontWeight = SemiBold)` in `AppSectionHeader`, `ImportTicketScanResultContent`, `LiveRoomsScreen`. Fixed `LiveRoomsScreen` "Recent Rooms" Bold â†’ theme SemiBold to match "Quick Actions".

**Previous:** 2026-04-09 â€” **Controls consistency pass**: `StatusPill`/`RoomStatusPill` â€” removed `heightIn(48dp)`, `radiusSmall` â†’ `radiusPill`, vertical padding `spacing8` â†’ `spacing4` (matches `StatusChip`). `RoomSessionCard` â€” `cardElevationDefault` (6dp) â†’ `cardElevationSubtle` (2dp); footer divider `alpha 0.4f` â†’ `0.28f`; `MiniBingoGrid` border softened to `alpha 0.34f`.

**Previous:** 2026-04-08 â€” App-wide: screens with **`AppHeaderBackground`** use **`MaterialTheme.colorScheme.surface`** on root **`Scaffold`** or **`Box`/`Column`** (Profile, Settings, Manual Entry, Live rooms, Ticket detail, Live sheet detail, History photo import, legal, profile sub-screens, auth register/forgot, **`MyAccountScreen`**). **`HistoryDetailScreen`** same pattern. **`BingoSheetSection` `premiumLayered`**: **`shadowElevation` 0**; soft tonal outer/inner strokes; **`BingoCardGrid`** history compact enables it. **History detail** bingo: **`BingoCardGrid`** `historyDetailCompact` wraps sheet in **`width(bingoGridWidth + 32dp)`** centered; **`BingoGridCard`** no double-wrap on success. **`LabelValueInfoRow`** + **`LabelValueInfoRowVariant`** (Default / Compact): **`labelSmall`** muted **`onSurfaceVariant`**; **`outlineVariant`** divider; History **Ticket Information** uses it + **bodyMedium**/**SemiBold** values, Status **Primary** accent. **History detail** bingo slot: **`Column`** + **`weight(1f)`** â†’ **`BoxWithConstraints`** â†’ **`BingoGridCard`**; **`BingoCardGrid`** uses inner max minus sheet padding for cell sizing; cell = **min** of width- and height-derived size (**22â€“44dp**). No breakpoint / **`graphicsLayer`**. **History detail** `AppTopBar`: default title; live CTA + delete in **`actions`** row (`HistoryDetailHeaderActions`; under **360dp** screen: icon-only live). **History detail** `BingoCardGrid` **`historyDetailCompact`**: `BoxWithConstraints` width-based cell size (22â€“44dp), tight gaps; **`BingoGrid5x5`** **`fixedLayoutCellSize`** + **`FixedPlayModeGrid`** (no 1:1 aspect block). **History detail** top: compact spec â€” **IconContainerBg**/**Primary** active + live/leave pills; flat **TICKET INFORMATION** (**Outline** 9sp label, **OnSurface** rows, **Outline** hairlines); stats **`SpanStyle`** bold sheet count + tail; **WarningBorder** top strip + **WarningContainer** row + **28dp** **WarningIcon** slot + **`MiniBingoPreview`** + score pill (**OnPrimary** on **WarningIcon**); **`CalledHistoryPanel`** after strip (not in ticket block). **Live card:** light **F6F8F4 â†’ F0F5EC**; **no** shadow. **Pills:** **LIVE** â€” soft green fill + green rim; **6dp** dot **`Color.Red`**, infinite **scale 1â†’1.4** + **Î± 1â†”0.6** (**1200ms** `FastOutSlowIn`, reverse), Ã— parent **`dotAlpha`**; text **SemiBold**. **Counter** â€” lighter vs LIVE. **LazyRow:** **12dp** horizontal **contentPadding**; **`spacedBy(8dp)`**; **`rememberSnapFlingBehavior`**; **`animateScrollToItem`** with **~âˆ’10dp** `scrollOffset`. **`CalledHistoryPanelContext.HistoryDetail`** uses **`takeLast(MAX_LIVE_CALLS)`**. No **`LiveCallNumberCircles.kt`**.

## Completed features`r`n`r`n- **Import — all 3 options:** Live **QR** auto-detect; **Scan ticket** CameraX + **viewfinder-matched crop** → `historyPhotoImport` OCR; **GMS** internal fallback; **gallery/editor** (Apply / uCrop) — device QA + build OK.
- **Live import camera — full-ticket still:** `ImageCapture` on `BingoLiveCameraImportScreen` + `FileProvider` → same pending-URI + `historyPhotoImport` as GMS; QR `ImageAnalysis` path unchanged; GMS secondary.
- **Live import camera — capture feedback (premium):** shutter flash + viewfinder scale pulse + button/back disabled + “Capturing…” during capture (`BingoLiveCameraImportScreen`); no capture/QR logic change.
- **Planning (2026-04-27):** Custom CameraX full-ticket scanner phased plan (4 phases, GMS + current OCR until stable); `NEXT_TASK.md` / `LAST_SESSION.md`.
- **Live QR → GMS handoff polish:** `BingoLiveCameraImportScreen` “Full ticket scan” uses preview `graphicsLayer` alpha + primary button loading; back disabled during handoff; navigation callback unchanged.
- **CalledHistoryPanel stable height**: empty “no calls” UI matches active call row height (`CalledHistoryMainRowHeight` / `LatestCallCircle` size).
- **Live ⋮ Reset game**: top bar overflow includes **Reset game** (same flow as existing Reset — confirm dialog, `RoomRepository.resetCalledNumbers`, un-archive room).
- **History list cards (`HistorySheetCard`)**: Live-style two-row panel; denser list spacing; overflow menu for join/leave/delete.
- **Live play sheet detail preview (`SheetDetailBottomSheet`)**: left-aligned meta columns + clearer title vs label hierarchy while preserving unified grouped card + responsive full-grid fit.
- **Shared `LabelValueInfoRow`**: reusable ticket-style label/value rows with optional compact vertical rhythm.
- **History detail layout**: fixed column (no page scroll); root **`Scaffold`** **`surface`** page fill (no header/background seam); bingo slot **`weight(1f)`** + constraint-based cell size (no breakpoint **`graphicsLayer`**).
- **History detail header actions**: live navigation in top bar; no duplicate-session row under grid.
- **History detail bingo grid**: compact layout from available width; proportional header letters; full grid visible without square aspect-ratio block (History only); optional **`BingoSheetSection` `premiumLayered`** (subtle elevation + nested inner plate).
- **Navigation cold start**: `splash` (rain every launch; ~3.45s) â†’ `onboarding` once (`SettingsRepository.onboarding_completed`), then `auth/login`; faster rain + 2.4s slides; splash logo: Eco mark with halo pulse + breathe/sway; gradient â€œBINGOâ€ title with left-to-right stagger.
- **Import ticket scan UI count**: `finalUiGridRowMajor` normalizes OCR row-major to 25 cells in `setScanResult`; `displayedCount` / quality card / History photo import summary use non-zero count on that grid only; debug log `ImportTicketFinalUi` at VM handoff.
- **`ImportTicketImageOcr`**: analyzer grid crop via `BingoNumberAnalyzer.tryDetectBingoGridCropForOcr`; heuristic fallback + `heuristicGridCrop` log; gallery `bypassInternalGridCrop`; final dedup; `rankRawStagesForFallback` / `fallbackPick`; `finalGridSanity` / tiers; `finalQualityCheck`; `stagePickAdjust`; +4 guard; `finalDuplicateCheck`; `attachLeftStripMeta` + `metaDebug`.
- **`LeftStripMetaOcr`**: 0Â°/90Â°/270Â°; line-ordered text from blocks/lines; Losnummer/Seriennummer centers + digit runs (line/element + vertical single-digit merge); proximity assignment; 5/4 length fallback; then existing regex/heuristic on normalized text; `metaRaw`/`metaNormalized`/`metaCandidates`/`metaChosen` â€” isolated from grid OCR.
- **Live Navigation header** (`LiveRoomsScreen`): `AppTopBar` `titleContent` = title + `Spacer(Modifier.weight(1f))` + 40dp primary `Add` (same pattern as History) â†’ `showCreateDialog = true`.
- **Import Ticket UI**: shared **`ImportTicketMainContent`**; success â†’ **Manual Entry** prefill via nav (no inline review card); **`ScanResultSummaryCard`** on error/loading only.
- **Import / History photo header polish**: **`AppHeaderBackground`** vertical greens (light) + quieter strokes; **`HeroBannerCard`** no shadow, tinted fill, **outline** rim + soft dashed; **`ImportTicketFailedScanContent`** / info row / prescan tips surfaces aligned; **`HistoryPhotoImportScreen`** content inset + calmer info icon.
- **`TicketInfoCard`** / **`TicketInfoStatusChip`**: **#F7F8F6** fill, **black 0.05** border, **18dp** corners, no elevation; **`HistoryDetailScreen`** ticket block = divider-separated **label / value** rows (no chips); **`LiveSheetDetailScreen`** uses **`TicketInfoCard`** for ticket block; **bingo grid** uses same **`BingoDetailGridCard`** path as History detail.
- **Import gallery / take photo**: Gallery pick â†’ **uCrop** (freestyle, 3:4, bottom controls on, ALL gestures, max scale 28) â†’ **`setGalleryPendingEdit`** â†’ **Apply** / **Discard** â†’ OCR. Camera/GMS unchanged.
- **Live play header**: compact **`GreenCard`** / **`GreenCardCompact`** â€” neutral container, **LIVE** + bordered count chip; duplicate big-call UI removed in favor of **`CalledHistoryPanel`**.
- **Live status pills** (`LiveRoomTopSection`): LIVE green-tint chip + green rim, stronger dot/text, slow dot breath; counter de-emphasized vs LIVE.
- **`CalledHistoryPanel` motion**: new last-call only â€” main circle scale+alpha in; LazyRow â€œlatestâ€ soft fade in when it becomes latest; inactive circles static.
- **`CalledHistoryPanel` row**: horizontal **content** inset + scroll to active (last) call with breathing offset; no gradient fade overlay.
- **`AppHeaderPageLayout`**: shared screen wrapper for `AppHeaderBackground`-based screens â€” `surface` root fill + gradient band (top 40%) + `Column(topBar, content)`; migrated HomeScreen, HistoryListScreen, ProfileScreen, SettingsScreen, MyAccountScreen, LiveRoomsScreen, LoginScreen, LiveSheetDetailScreen.

## In progress

- Device QA: `BingoLiveCameraImportScreen` **Scan ticket** — feel shutter + scale, confirm no QR regression; GMS + gallery still OK. Build: `./gradlew :app:assembleDebug`.
- Device QA: `AppHeaderPageLayout` on Home, HistoryList, Profile, Settings, MyAccount, LiveRooms — no gray seam, no flat white header.

## Pending tasks

- **Roadmap (see `NEXT_TASK` table):** optional Phase 3 **crop/confirm** UI, Phase 4 **drop GMS** after sustained QA — **do not** remove GMS or OCR early.

- Revisit **Settings â†’ Live header style** (orphaned for live header) â€” remove UI or repurpose.
- Device QA: **PickVisualMedia** Gallery + GMS Take photo on `historyPhotoImport`; NavGraph pending-URI handoff from main/Jackpot.
- Device QA: hero suppressed on auto Manual Entry (unchanged).
- Rescan tickets; confirm `ImportTicketOcr` / `ImportTicketFinalUi` logs vs grid if issues.
- Tune `MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV` if needed (currently 20).




