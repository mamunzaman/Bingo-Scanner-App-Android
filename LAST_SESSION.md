# Last session

## 2026-04-16 - History card micro polish + whole-card view-ready

- HistorySheetCard.kt: reduced row bulk, lighter preview weight, cleaner title/status spacing, smaller Join action, tighter action alignment
- Real Bingo preview + real called/total count remain intact
- ./gradlew :app:assembleDebug - BUILD SUCCESSFUL

---

## 2026-04-16 — History compact single-row card (exact paths)`r`n`r`n- `HistorySheetCard.kt` added under `ui/screens/history/components` and used by `HistoryListScreen.kt``r`n- Replaced multi-row card content with compact single-row panel (left icon, title + ACTIVE row, right View/Join/Delete)`r`n- Removed progress, divider, footer, room name, and leave-room render from history row UI (handlers preserved where required)`r`n- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**`r`n`r`n---

# Last session

## 2026-04-15 â€” Live list: tighter scroll tail + slightly stronger row edge

- `LivePlayScreen.kt`: list `LazyColumn` **bottom `contentPadding`** no longer uses manual-entry **`estimatedDockHeight`** (scaffold content already clears bottom bar) â€” tail = **`Dimens.spacing16`**
- `ListSheetRow`: outer border alpha **0.3**; section divider **0.22**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-15 â€” Live list rows: row vs page separation (still flat)

- `LivePlayScreen.kt` (`ListSheetRow`): top block **`surface`** again (clear step above page **`background`**); strip still **`surfaceContainer`**
- Outer border **`outlineVariant`** alpha **0.24**; section divider line alpha **0.2** â€” edges readable, no elevation
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-15 â€” Live list rows: final tone + compactness pass

- `LivePlayScreen.kt` (`ListSheetRow`): top block **`background`** (light neutral vs stark `surface`); strip unchanged **`surfaceContainer`**; border **`outlineVariant`** alpha **0.16**; divider margin **3.dp** / line alpha **0.14**
- Row padding: top block **vertical `spacing5`**; meta strip **`spacing4`**; list item gaps + sticky spacer **`spacing8` â†’ `spacing5`**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-15 â€” Live list rows: flat neutral panel + tighter list spacing

- `LivePlayScreen.kt` (`ListSheetRow`): removed elevated `Surface` and inset inner plate; single flat column â€” **`surface`**, hairline **`outlineVariant`** border (~0.22), **`radiusSmall`**, no shadow
- Top block stays **`surface`**; bottom meta strip **`surfaceContainer`** (theme warm neutral); divider spacing tightened (`spacing4` around rule)
- Typography toned down (**`bodyMedium` SemiBold** title, muted scanned); **MARKED** = **`primary`** + **`SemiBold`** only (no heavy weight)
- Lazy list: row gaps **`spacing16` â†’ `spacing8`**; post-sticky spacer **`spacing12` â†’ `spacing8`**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-15 â€” Live list `ListSheetRow` premium depth (no logic change)

- `LivePlayScreen.kt` (`ListSheetRow`): outer **`Surface`** â€” `surface`, **`Dimens.cardElevationSubtle`** (2dp), hairline **`outlineVariant`** border (~0.3 alpha), corner **`radiusMedium`**
- Inset inner plate â€” **`surfaceContainerLowest`** + **`radiusSmall`**, same horizontal padding as before (`spacing16`); slightly more vertical rhythm above horizontal divider (`spacing12` / `spacing10`)
- Softer horizontal divider + vertical meta dividers; title **`titleSmall` SemiBold**; scanned line more muted; **MARKED** value **`ExtraBold`** on **`primary`**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-15 â€” Detail sheet info left-align + stronger title hierarchy

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): `SERIAL` / `LOS` / `SCANNED` columns stay equal-width but each columnâ€™s label/value is start-aligned for natural reading
- Title row: stronger `titleMedium` + Bold vs softer `labelMedium` meta labels; values use slightly larger `titleLarge` emphasis with single-line ellipsis + full-width alignment
- Slightly increased reserved info-block estimate in responsive fit math to reduce risk of vertical crowding after typography bump (still constraint-driven; no scroll added)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Unified premium container polish for detail sheet

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): merged top info + Bingo grid into one shared-width parent container with a single subtle border/radius language
- Standardized alignment and spacing: equal inner paddings, consistent center axis, normalized gaps (`titleâ†’meta`, `metaâ†’grid`, `gridâ†’button`)
- Kept readable `SERIAL`/`LOS`/`SCANNED` typography and responsive full-grid visibility without scroll
- `View full detail` button now uses matching width and shape for cohesive composition
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Detail info readability + width alignment with grid

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): top info card now uses the same centered effective width as the Bingo grid card (`compactGridWidth`) for clean edge alignment
- Increased readability for `SERIAL`/`LOS`/`SCANNED` and title/marked row (stronger/larger typography) while preserving responsive no-scroll fit logic
- Grid remains constraint-derived and full-visible; no fixed-height wrappers introduced
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Responsive-fit audit (removed fixed fit assumptions)

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`) fit math now derives from `BoxWithConstraints` (`maxHeight`/`maxWidth`) with clamped ratios, replacing fixed fit assumptions (`infoBlockHeight`/min grid constants)
- Confirmed no fixed `height(...)`/`requiredHeight(...)` wrappers force final sheet content height in this flow
- Added short code comments to mark state-based expansion and responsive grid-fit sizing from available constraints
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Expanded sheet composition rebalance (less sparse)

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): removed tall inner wrapper (`fillMaxHeight`) so only sheet stays expanded while content cluster uses `wrapContentHeight()`
- Pinned cluster higher and tightened inter-section spacing; reduced dead area below action
- `View full detail` button width now aligns with grid width and is centered for stronger composition
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Force expanded sheet + tighter height-fit grid sizing

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): now uses `rememberModalBottomSheetState(skipPartiallyExpanded = true)` + `sheetState.expand()` to avoid partial/peek presentation
- Sheet content container switched to `fillMaxHeight(0.96f)` and grid fit tuned with stricter height-based target (`remainingGridHeight * 0.80f`)
- Reduced reserved/spacing constants to prioritize full 5x5 visibility (`infoBlockHeight`, min fit width) while keeping info + action visible
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Explicit no-scroll fitting for detail sheet grid

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): wrapped content in `BoxWithConstraints` and computed remaining grid height after handle/info/button/paddings
- Grid width now constrained by `min(widthTarget, remainingHeightTarget)` so full 5x5 card remains visible without internal scroll on common device heights
- Tightened compact spacing (`sectionSpacing = spacing8`, shorter info block rhythm) and kept same preview structure (`info + grid + action`)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Sheet detail compact reorder (info top, smaller grid)

- `LivePlayScreen.kt` (`SheetDetailBottomSheet`): reordered to compact info-first block (title + marked + `SERIAL`/`LOS`/`SCANNED`), with centered reduced-width Bingo grid below
- Increased metadata readability via stronger `SheetPreviewInfoCell` typography; tightened vertical spacing for one-screen fit
- Removed win/almost-bingo preview sections from this compact sheet layout to avoid overflow; kept existing sheet flow and data source unchanged
- `View full detail` returned to primary app button style in compact bottom placement
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live list row refinement + bottom-sheet detail tap

- `LivePlayScreen.kt` (`ListSheetRow`): added muted scanned date under title; removed chevron; increased top horizontal air to better match reference
- Bottom strip values (`SERIAL` / `LOS` / `MARKED`) strengthened to bold hierarchy while labels stay light/small
- List-row tap now opens existing slide-up `SheetDetailBottomSheet` (reused `detailSheet = sheet`, no new screen/navigation)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live list mode row branch patched (actual rendered path)

- Traced visible branch: `LivePlayScreen` list mode (`selectedView == true`) renders `ListSheetRow`, not `TicketRowCard`
- `LivePlayScreen.kt`: redesigned `ListSheetRow` to 2-part layout (top: thumbnail + title + chevron, bottom: equal `SERIAL` / `LOS` / `MARKED` cells with soft dividers)
- Wired `serialNumber` / `losNumber` from `LiveSheetUi`; `MARKED` value shown as `${markedCount}/25` with primary emphasis only on value
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live My Tickets row redesign (2-section card layout)

- `MyTicketsBottomSheet.kt`: redesigned `TicketRowCard` into a compact 2-part card (top: grid preview + title + chevron, bottom: `SERIAL` / `LOS` / `MARKED` 3-cell strip with subtle dividers)
- Kept existing behavior and list logic: selection mode, add/go-live action mapping, and room-state handling unchanged
- `TicketCard.kt`: added reusable `TicketGridThumbnailPreview` for the small bingo thumbnail look
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live detail sheet label size fine-tune (1.15x)

- `LivePlayScreen.kt`: `sheetMetaLabelTextStyle()` now uses `labelSmall * 1.15f` for slightly improved metadata label readability
- Value sizing remains unchanged (`bodyLarge * 1.5f`)
- No layout/logic changes

---

## 2026-04-13 â€” Live keypad top-row compact refresh (latest-called badge)

- `LivePlayCallKeypad` now takes `latestCalled: Int?` (instead of progress text) and renders a compact left badge (`B12` / `--`)
- Top control row tightened (reduced vertical/top spacing) and input container refined to a lighter single rounded style
- Input typography and placeholder made cleaner/stronger; divider + clear icon sizing aligned for balanced vertical centering
- `LivePlayScreen` now passes `calledNumbers.lastOrNull()` into keypad (count not duplicated in keypad bar)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Dynamic bingo number font from cell size

- `BingoGrid5x5.kt`: added responsive number font scaling derived from computed cell size (no fixed text jump)
- Formula used per cell: `(cellSize * 0.48).sp` clamped to `14.sp..32.sp`, converted to safe local fontScale
- Applied in both `PlayModeGrid` and `FixedPlayModeGrid` via `CompositionLocalProvider(LocalDensity)` so numbers scale while grid layout/spacing stay unchanged
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Larger live BINGO typography (responsive, no layout break)

- `BingoNumberBox.kt`: grid number font 18sp â†’ **22sp**
- `BingoHeaderRow.kt`: BINGO letter font 24sp â†’ **28sp** (both DarkLetters/LightOutlined and PrimaryGreen branches)
- `LivePlayScreen.kt` `SheetCard`:
  - Card title: `titleMedium` â†’ **`titleLarge`**; badge: `labelSmall` â†’ **`labelMedium`**
  - Scale reference width: 320dp â†’ **300dp** (renders slightly larger on typical phones)
  - Scale upper bound: 1f â†’ **1.15f** (wider screens scale up); lower bound: 0.6f â†’ **0.65f**
- Existing `CompositionLocalProvider(LocalDensity)` scaler in SheetCard ensures fonts automatically shrink on small cards
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Title row full-width tap target for edit mode

- `BingoCardGrid.kt`: title `Row` now has `clickable(indication=null)` â†’ `onRequestEditTitle()` when not editing; fills dead zone between text and pen icon
- Removed individual `.clickable` from the `Text` (Row handles it)
- No focus logic, no FocusRequester â€” stable
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Remove FocusRequester entirely from sheet title (final crash fix)

- **Root cause final:** `FocusRequester` + `onFocusChanged` + blur job created a race where the focus tree parent was left in `ActiveParent` state with no child â†’ any `requestFocus()` (programmatic or from `tapToFocus`) crashed
- **BingoCardGrid.kt:** Removed `sheetTitleFocusRequester`, `onSheetTitleFocusChanged` params and `.focusRequester()` / `.onFocusChanged()` modifiers from title `BasicTextField`. Removed unused `FocusRequester`/`FocusState` imports.
- **ManualEntryScreen.kt:** Removed `sheetTitleFocusRequester`, `sheetTitleBlurJob`, `sheetTitleHadFocusWhileEditing`, `LaunchedEffect(isEditingSheetName)` requestFocus block, all blur job logic, `onSheetTitleFocusChanged` call site, unused imports (`FocusRequester`, `Job`, `rememberCoroutineScope`, `FocusState`). `keyboardController?.hide()` kept at all exit paths.
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Add keyboardController.hide() before removing BasicTextField

- **New crash:** `IllegalStateException: no active focus target` â€” keyboard sends key events after field is removed
- Added `keyboardController?.hide()` at all 4 exit paths before `isEditingSheetName = false`:
  - `dismissSheetTitleEdit`
  - `onSheetNameDone`
  - `onToggleEditSheet` (when stopping)
  - `onSheetTitleFocusChanged` blur job
- This dismisses the soft keyboard safely without touching the focus tree
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Remove focusManager.clearFocus() (actual crash source)

- **Root cause:** `focusManager.clearFocus()` was partially corrupting the focus tree â€” clearing child focus but leaving parent in `ActiveParent` state. Next `requestFocus()` (from CoreTextField `tapToFocus` or our code) hit the stale parent â†’ crash.
- **BingoCardGrid.kt:** Reverted to clean conditional rendering (`if/else`) â€” no "always in tree" hack needed
- **ManualEntryScreen.kt:**
  - Removed ALL `focusManager.clearFocus()` calls (4 sites) â€” they were the source of stale focus state
  - Removed `focusManager` variable and `LocalFocusManager` / `FocusState` imports
  - `requestFocus()` uses `delay(100)` + `runCatching` (field must enter composition first)
  - Stopping editing = just set `isEditingSheetName = false` â€” Compose handles focused-node removal internally
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Fix tapToFocus crash on invisible BasicTextField

- **Root cause confirmed:** invisible `BasicTextField` (alpha=0) was on top of the z-stack in `Box`, still receiving tap events â†’ `CoreTextField.tapToFocus()` â†’ `requestFocus()` on broken focus parent â†’ crash
- **BingoCardGrid.kt:** Swapped z-order â€” `BasicTextField` now renders **first** (bottom), `Text` renders **after** (on top) when not editing, blocking taps from reaching the hidden field
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Eliminate manual entry focus crash (root cause fix)

- **Root cause:** `BasicTextField` for sheet title was inside `if (isEditingSheetName)` â€” removed from focus tree while holding focus â†’ `ActiveParent with no focused child`
- **BingoCardGrid.kt:** BasicTextField is now always in composition; toggled via `graphicsLayer { alpha }` + `enabled` instead of conditional rendering. Text display shown when not editing.
- **ManualEntryScreen.kt:** Removed `delay(80)` before `requestFocus()` (field is always in tree now). `runCatching` wrappers on `clearFocus()` kept as defense-in-depth.
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Fix manual entry focus crash (ActiveParent with no focused child)

- Wrapped all 4 `focusManager.clearFocus()` calls in `runCatching {}` in `ManualEntryScreen.kt`
- Crash was a known Compose focus race: `clearFocus()` on a parent with stale `activeParent` state
- No behavior change â€” focus clearing still works, exception is silently caught
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Red finished-state circle, remove "Call numbers ended" text

- Removed "Call numbers ended" text from `CalledHistoryPanel`
- Added `isFinished` param to `LatestCallCircle`; when call limit reached the big circle switches from green to red gradient (dark theme uses `colorScheme.error`)
- No layout or logic changes
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Remove divider seam above carousel, rebalance dot spacing

- Removed `HorizontalDivider` between called-numbers panel and carousel (was creating visible seam/shadow); `spacedBy(8dp)` gap is sufficient separation
- Page indicator bottom padding: `spacing4` â†’ **`spacing8`** for breathing room before keypad
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live room spacing polish

- Outer Column gap: `spacing16` â†’ **`spacing8`** (tighter section rhythm)
- Cards-mode divider: removed bottom padding (was `vertical = spacing8`, now `top = spacing8` only)
- Carousel indicator: spacer `spacing8` â†’ **`spacing4`**, added `padding(bottom = spacing4)` on dot row
- SheetCard inner padding: `spacing16` â†’ **`spacing12`**; title-to-grid spacer: `spacing12` â†’ **`spacing8`**
- `cardNonGridOverhead`: 120dp â†’ **100dp** (reflects tighter card padding)
- No layout structure or scaling changes
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Bump BINGO header + grid number text size (+2sp each)

- **`BingoHeaderRow`:** default letter fontSize 22sp â†’ **24sp** (DarkLetters/LightOutlined and PrimaryGreen branches)
- **`BingoNumberBox`:** number fontSize 16sp â†’ **18sp**
- No layout, spacing, or scaling changes
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live card: proportional content scaling via density

- **`SheetCard`:** outer `Column` â†’ `BoxWithConstraints`; computes `scale = (maxWidth / 320.dp).coerceIn(0.6f, 1f)`; wraps inner content in `CompositionLocalProvider(LocalDensity provides scaledDensity)` so all dp/sp values scale proportionally
- Title text, badge padding, BINGO header letters, grid number text, cell spacing â€” everything shrinks together when card is narrower than 320dp
- No changes to shared components (`BingoCardGrid`, `BingoGrid5x5`, `BingoHeaderRow`, `BingoNumberBox`)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Move LOS/serial from live card into detail bottom sheet

- **`SheetCard`:** removed LOS/serial row; card now: title + marked badge â†’ 12dp spacer â†’ grid
- **`SheetDetailBottomSheet`:** added LOSNUMMER / SERIENNUMMER section (label `labelSmall` + value `bodyLarge SemiBold`); shown between title row and win/almost-bingo status; only rendered when data exists
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live card: warm yellow win styling (replace green)

- **`SheetCard`:** won-card background â†’ `WarningContainer` (soft cream-yellow), border â†’ `WarningBorder` (warm gold), marked badge â†’ `WarningText` text + `Warning @ 18%` bg
- Removed `Success` import; now uses `Warning`, `WarningBorder`, `WarningContainer`, `WarningText`
- Non-won cards unchanged; red winning cell marks unchanged; bottom sheet unchanged
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live card: win container style + LOS/serial metadata

- **`LiveSheetUi`:** added `losNumber`, `serialNumber` fields; populated from `TicketEntity` in `LivePlayViewModel.flowForOneTicket`
- **`SheetCard`:** removed inline `BingoWinBanner`; win state now shown via soft green background (`Success @ 8%`) + green border (`Success @ 50%`); marked badge turns green on win
- **`SheetCard`:** LOS/serial row rendered between title and grid when data exists (compact `labelSmall`, `onSurfaceVariant`)
- Card order: title + marked â†’ LOS/serial (if any) â†’ grid
- Win cell highlighting in grid unchanged; detail bottom sheet unchanged
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-13 â€” Live card: remove inline Almost Bingo, add tap-to-detail sheet

- **`SheetCard`:** removed inline `AlmostBingoAlertRowV2`; card now shows only win banner + title + grid; added `onClick` â†’ `clickable` with ripple
- **`BingoSheetsCarousel`:** new `onSheetClick` parameter forwarded to each `SheetCard`
- **`SheetDetailBottomSheet`:** new private composable â€” `ModalBottomSheet` showing title, marked count, win banner, almost bingo alert, and "View full detail" button (navigates to `liveSheetDetail`)
- **`LivePlayScreen`:** `detailSheet` state; card tap opens `SheetDetailBottomSheet`; dismiss clears state
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-13 â€” Live play: auto-fit bingo card to available space

- **`LivePlayScreen`:** cards mode no longer inside `LazyColumn`; header + carousel rendered in plain `Column` so carousel gets `weight(1f)` bounded height
- **`BingoSheetsCarousel`:** `BoxWithConstraints` now `fillMaxSize()`; `cardWidth` shrinks proportionally when available height can't fit a full-width square grid (120 dp non-grid overhead estimate); `LazyRow` centers cards via symmetric padding + `verticalAlignment = CenterVertically`
- No changes to `BingoCardGrid` or `BingoGrid5x5`; grid stays square via `aspectRatio(1f)` â€” width adjusts instead
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 â€” Live play: keypad + view mode in overflow

- **`LivePlayScreen`:** removed in-content **Cards/List** toggle; **`LiveRoomTopBar`** overflow adds **Cards view** / **List view** when sheets exist (same `selectedView` state)
- **`LiveCallInputBar.kt`:** replaced text-field bar with **`LivePlayCallKeypad`** (manual-entry-style dock: progress, draft, clear, undo, call, digit rows); always above **`AppBottomBar`**
- **`ManualEntryScreen`:** **`ManualEntryKeypadDockMetrics`** â€” shared dock height for list/empty bottom padding
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 â€” Import device QA: stable (planning handoff)

- **`historyPhotoImport` device smoke** â€” pending URI, GMS, gallery/uCrop, tab switch + discard, back: **passed** (baseline: import flow treated as stable for product planning)
- **Docs:** **`NEXT_TASK.md`** repointed to next highest-impact step (**Live play TalkBack**); **`PROJECT_SNAPSHOT.md`** / **`TECH_DEBT.md`** unchanged this pass
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-12 â€” `SettingsScreen`: drop unused `Slate400` import

- **`ui/screens/profile/SettingsScreen.kt`** â€” removed unused **`Slate400`** import (no other code change)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**

---

## 2026-04-12 â€” Remove `LiveHeaderStyle` / `UserPreferencesRepository` dead chain

- **Deleted:** **`data/preferences/UserPreferencesRepository.kt`** (enum, DataStore `user_prefs` `live_header_style`)
- **`MainActivity.kt`:** removed **`UserPreferencesRepository.init`**
- **`SettingsViewModel`:** removed **`liveHeaderStyle`** / **`setLiveHeaderStyle`**
- **`LivePlayViewModel`:** removed unused **`liveHeaderStyle`** `StateFlow`
- **`NavGraph.kt`:** removed unused **`LiveHeaderStyle`** import
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**

---

## 2026-04-12 â€” Settings: remove orphaned Live Header Style

- **Issue:** **Live Header Style** section in **`SettingsScreen`** â€” `LiveHeaderStyle` not read by any composable (only written from Settings)
- **Patch:** **`SettingsScreen.kt`** â€” removed section + `liveHeaderStyle` collect + unused **`ViewModule`** import; **`AppHeaderPageLayout` / `AppHeaderBackground`** unchanged
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**

---

## 2026-04-12 â€” `historyPhotoImport` device smoke (agent)

- **Device:** not runnable here â€” **no pass/fail recorded**
- **Static:** pending URI + GMS/gallery + `onRegisterLeaveHandler` wiring reviewed again; **no new code**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- **Owner:** one hardware pass on `NEXT_TASK.md` (previously: pending URI, GMS, gallery/uCrop, tab switch + discard, back)

---

## 2026-04-12 â€” `historyPhotoImport` path review + leave handoff fix

- **Static review:** `NavGraph` pending URI (`PENDING_HISTORY_PHOTO_IMPORT_URI_KEY`) consumed once in `LaunchedEffect`; GMS + gallery wiring unchanged
- **Bug:** `photoImportLeaveHandler` was never set â€” **`HistoryPhotoImportScreen`**â€™s **`onRegisterLeaveHandler`** defaulted to no-op, so bottom-tab switches skipped discard confirmation
- **Patch:** `NavGraph.kt` â€” pass **`onRegisterLeaveHandler = { photoImportLeaveHandler = it }`** on **`HistoryPhotoImportScreen`**
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`PROJECT_SNAPSHOT.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 â€” `AppHeaderBackground` seam (NEXT_TASK 1)

- **Issue:** gradient bottom stop did not match `AppHeaderPageLayout` root **`surface`** (light: `surface` @ 0.96Î±; dark: `background` vs `surface`)
- **Patch:** `AppHeaderBackground.kt` â€” last gradient color **`cs.surface`** in light and dark
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 â€” NEXT_TASK: QA-only (no code)

- Read **`NEXT_TASK.md`**: step 1 = build; steps 2â€“3 = **device QA** (`AppHeaderPageLayout` seam, `historyPhotoImport`) â€” **no code change** this pass
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

### Resume

- **`NEXT_TASK.md`** (remaining device checks)

---

## 2026-04-12 â€” NEXT_TASK: History bulk dock safe area

- **`NEXT_TASK.md` step 1:** `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL**
- **Code:** `HistoryListScreen` â€” `BulkSelectionActionBar` in `bottomBar` now uses **`Modifier.navigationBarsPadding()`** so the dock matches **`AppBottomBar`** safe-area behavior when selection mode replaces the tab bar (My Tickets sheet already pads the outer `Column`; unchanged)
- **Steps 2â€“3** (device â€” dock/dialogs/tab hairline): manual QA on hardware; no further code changes this pass

### Files touched

- `HistoryListScreen.kt`
- `LAST_SESSION.md`, `PROJECT_SNAPSHOT.md`, `NEXT_TASK.md`

### Unfinished

- Device QA: `AppHeaderPageLayout` seam (`PROJECT_STATUS.md`); import/gallery + pending URI; bulk UI spot-check on device

### Resume here

1. Follow **`NEXT_TASK.md`** (numbered steps)
2. After code changes: `./gradlew :app:assembleDebug`

---

## 2026-04-12 â€” Handoff docs (minimal update pass)

- Re-scanned `NavGraph` composables, `AppDatabase` (v7), `rg runBlocking` on `app/src/main`, `ui/components` inventory â€” **no new routes, entities, or runBlocking** vs prior snapshot
- Patched **`PROJECT_SNAPSHOT.md`**, **`LAST_SESSION.md`**, **`TECH_DEBT.md`**, **`NEXT_TASK.md`** in place (no full rewrites)
- `./gradlew :app:assembleDebug` â€” **BUILD SUCCESSFUL** (re-verified)

### Files touched

- `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md`, `NEXT_TASK.md`

### Unfinished

- Device QA â€” same as **`NEXT_TASK.md`** (selection dock, tab hairline, bulk dialogs)

### Resume here

1. Run **`NEXT_TASK.md`** steps (build + device checks)
2. After code changes: `./gradlew :app:assembleDebug`, then re-run device steps

---

## 2026-04-12 â€” Bulk UX, polish, interaction helpers

- **Bulk selection (History + live My Tickets):** `BulkSelectionActionBar`; confirm dialogs; `HistoryRepository.deleteSessions`, `RoomRepository.unassignTickets`; selection mode on `RoomSessionCard`; `HistoryListScreen` / `MyTicketsBottomSheet` / `LivePlayScreen` wiring; optional bulk **Add to room** from live My Tickets
- **Visual polish:** `Dimens.outlineDividerAlpha` / `outlineBorderAlpha`; dock-style action bar (rounded top + border); `AppBottomBar` top hairline uses shared divider alpha; dialogs â€” `surfaceContainerHigh`, `radiusXL`, `titleLarge` + `bodyMedium`
- **Interaction tokens:** `AppAnimation`, `AppRipple`, `AppClick`; `RoomSessionCard` + `RoundProgressCards` use `appClickable`
- **Typography:** `titleMedium` SemiBold in `Typography.kt`; redundant SemiBold copies removed where aligned (`AppSectionHeader`, `ImportTicketScanResultContent`, `LiveRoomsScreen`)
- **Controls:** `StatusPill` / `RoomSessionCard` spacing and elevation tweaks (see `PROJECT_STATUS.md` detail lines)

### Files touched (representative)

- `theme/Typography.kt`, `theme/Dimens.kt`
- `ui/core/interaction/AppAnimation.kt`, `AppRipple.kt`, `AppClick.kt`
- `ui/components/BulkSelectionActionBar.kt`, `BulkSelectionConfirmDialogs.kt`, `RoomSessionCard.kt`, `RoundProgressCards.kt`, `AppSectionHeader.kt`, `AppBottomBar.kt`, `ImportTicketScanResultContent.kt`
- `ui/screens/history/HistoryListScreen.kt`, `ui/screens/live/MyTicketsBottomSheet.kt`, `LivePlayScreen.kt`, `LiveRoomsScreen.kt`
- `data/repository/HistoryRepository.kt`, `RoomRepository.kt`
- Docs: `PROJECT_STATUS.md`, `NEXT_TASK.md` (as updated in session)

### Unfinished

- Device QA per **`NEXT_TASK.md`** (selection dock, tab bar hairline, dialogs on device)
- **`PROJECT_STATUS.md`:** `AppHeaderPageLayout` seam check on listed screens; other pending QA rows

### Resume here

1. Run **`./gradlew :app:assembleDebug`** after code changes (last run: success 2026-04-12).
2. Execute **`NEXT_TASK.md`** manual checks on a physical device.
3. Continue **`PROJECT_STATUS.md`** â€œIn progressâ€ / â€œPendingâ€ until cleared or reprioritized.

---

## Older â€” 2026-04-08 (gallery / uCrop / OCR)

- uCrop gallery path, `bypassInternalGridCrop`, dedupe before handoff; device QA notes in historical `PROJECT_STATUS` entries.

## Older â€” 2026-04-02

- Regenerated project docs from codebase.



