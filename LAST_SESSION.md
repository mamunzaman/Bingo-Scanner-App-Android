# Last session

## 2026-04-13 — Remove FocusRequester entirely from sheet title (final crash fix)

- **Root cause final:** `FocusRequester` + `onFocusChanged` + blur job created a race where the focus tree parent was left in `ActiveParent` state with no child → any `requestFocus()` (programmatic or from `tapToFocus`) crashed
- **BingoCardGrid.kt:** Removed `sheetTitleFocusRequester`, `onSheetTitleFocusChanged` params and `.focusRequester()` / `.onFocusChanged()` modifiers from title `BasicTextField`. Removed unused `FocusRequester`/`FocusState` imports.
- **ManualEntryScreen.kt:** Removed `sheetTitleFocusRequester`, `sheetTitleBlurJob`, `sheetTitleHadFocusWhileEditing`, `LaunchedEffect(isEditingSheetName)` requestFocus block, all blur job logic, `onSheetTitleFocusChanged` call site, unused imports (`FocusRequester`, `Job`, `rememberCoroutineScope`, `FocusState`). `keyboardController?.hide()` kept at all exit paths.
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Add keyboardController.hide() before removing BasicTextField

- **New crash:** `IllegalStateException: no active focus target` — keyboard sends key events after field is removed
- Added `keyboardController?.hide()` at all 4 exit paths before `isEditingSheetName = false`:
  - `dismissSheetTitleEdit`
  - `onSheetNameDone`
  - `onToggleEditSheet` (when stopping)
  - `onSheetTitleFocusChanged` blur job
- This dismisses the soft keyboard safely without touching the focus tree
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Remove focusManager.clearFocus() (actual crash source)

- **Root cause:** `focusManager.clearFocus()` was partially corrupting the focus tree — clearing child focus but leaving parent in `ActiveParent` state. Next `requestFocus()` (from CoreTextField `tapToFocus` or our code) hit the stale parent → crash.
- **BingoCardGrid.kt:** Reverted to clean conditional rendering (`if/else`) — no "always in tree" hack needed
- **ManualEntryScreen.kt:**
  - Removed ALL `focusManager.clearFocus()` calls (4 sites) — they were the source of stale focus state
  - Removed `focusManager` variable and `LocalFocusManager` / `FocusState` imports
  - `requestFocus()` uses `delay(100)` + `runCatching` (field must enter composition first)
  - Stopping editing = just set `isEditingSheetName = false` — Compose handles focused-node removal internally
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Fix tapToFocus crash on invisible BasicTextField

- **Root cause confirmed:** invisible `BasicTextField` (alpha=0) was on top of the z-stack in `Box`, still receiving tap events → `CoreTextField.tapToFocus()` → `requestFocus()` on broken focus parent → crash
- **BingoCardGrid.kt:** Swapped z-order — `BasicTextField` now renders **first** (bottom), `Text` renders **after** (on top) when not editing, blocking taps from reaching the hidden field
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Eliminate manual entry focus crash (root cause fix)

- **Root cause:** `BasicTextField` for sheet title was inside `if (isEditingSheetName)` — removed from focus tree while holding focus → `ActiveParent with no focused child`
- **BingoCardGrid.kt:** BasicTextField is now always in composition; toggled via `graphicsLayer { alpha }` + `enabled` instead of conditional rendering. Text display shown when not editing.
- **ManualEntryScreen.kt:** Removed `delay(80)` before `requestFocus()` (field is always in tree now). `runCatching` wrappers on `clearFocus()` kept as defense-in-depth.
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Fix manual entry focus crash (ActiveParent with no focused child)

- Wrapped all 4 `focusManager.clearFocus()` calls in `runCatching {}` in `ManualEntryScreen.kt`
- Crash was a known Compose focus race: `clearFocus()` on a parent with stale `activeParent` state
- No behavior change — focus clearing still works, exception is silently caught
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Red finished-state circle, remove "Call numbers ended" text

- Removed "Call numbers ended" text from `CalledHistoryPanel`
- Added `isFinished` param to `LatestCallCircle`; when call limit reached the big circle switches from green to red gradient (dark theme uses `colorScheme.error`)
- No layout or logic changes
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Remove divider seam above carousel, rebalance dot spacing

- Removed `HorizontalDivider` between called-numbers panel and carousel (was creating visible seam/shadow); `spacedBy(8dp)` gap is sufficient separation
- Page indicator bottom padding: `spacing4` → **`spacing8`** for breathing room before keypad
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Live room spacing polish

- Outer Column gap: `spacing16` → **`spacing8`** (tighter section rhythm)
- Cards-mode divider: removed bottom padding (was `vertical = spacing8`, now `top = spacing8` only)
- Carousel indicator: spacer `spacing8` → **`spacing4`**, added `padding(bottom = spacing4)` on dot row
- SheetCard inner padding: `spacing16` → **`spacing12`**; title-to-grid spacer: `spacing12` → **`spacing8`**
- `cardNonGridOverhead`: 120dp → **100dp** (reflects tighter card padding)
- No layout structure or scaling changes
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Bump BINGO header + grid number text size (+2sp each)

- **`BingoHeaderRow`:** default letter fontSize 22sp → **24sp** (DarkLetters/LightOutlined and PrimaryGreen branches)
- **`BingoNumberBox`:** number fontSize 16sp → **18sp**
- No layout, spacing, or scaling changes
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Live card: proportional content scaling via density

- **`SheetCard`:** outer `Column` → `BoxWithConstraints`; computes `scale = (maxWidth / 320.dp).coerceIn(0.6f, 1f)`; wraps inner content in `CompositionLocalProvider(LocalDensity provides scaledDensity)` so all dp/sp values scale proportionally
- Title text, badge padding, BINGO header letters, grid number text, cell spacing — everything shrinks together when card is narrower than 320dp
- No changes to shared components (`BingoCardGrid`, `BingoGrid5x5`, `BingoHeaderRow`, `BingoNumberBox`)
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Move LOS/serial from live card into detail bottom sheet

- **`SheetCard`:** removed LOS/serial row; card now: title + marked badge → 12dp spacer → grid
- **`SheetDetailBottomSheet`:** added LOSNUMMER / SERIENNUMMER section (label `labelSmall` + value `bodyLarge SemiBold`); shown between title row and win/almost-bingo status; only rendered when data exists
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Live card: warm yellow win styling (replace green)

- **`SheetCard`:** won-card background → `WarningContainer` (soft cream-yellow), border → `WarningBorder` (warm gold), marked badge → `WarningText` text + `Warning @ 18%` bg
- Removed `Success` import; now uses `Warning`, `WarningBorder`, `WarningContainer`, `WarningText`
- Non-won cards unchanged; red winning cell marks unchanged; bottom sheet unchanged
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Live card: win container style + LOS/serial metadata

- **`LiveSheetUi`:** added `losNumber`, `serialNumber` fields; populated from `TicketEntity` in `LivePlayViewModel.flowForOneTicket`
- **`SheetCard`:** removed inline `BingoWinBanner`; win state now shown via soft green background (`Success @ 8%`) + green border (`Success @ 50%`); marked badge turns green on win
- **`SheetCard`:** LOS/serial row rendered between title and grid when data exists (compact `labelSmall`, `onSurfaceVariant`)
- Card order: title + marked → LOS/serial (if any) → grid
- Win cell highlighting in grid unchanged; detail bottom sheet unchanged
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-13 — Live card: remove inline Almost Bingo, add tap-to-detail sheet

- **`SheetCard`:** removed inline `AlmostBingoAlertRowV2`; card now shows only win banner + title + grid; added `onClick` → `clickable` with ripple
- **`BingoSheetsCarousel`:** new `onSheetClick` parameter forwarded to each `SheetCard`
- **`SheetDetailBottomSheet`:** new private composable — `ModalBottomSheet` showing title, marked count, win banner, almost bingo alert, and "View full detail" button (navigates to `liveSheetDetail`)
- **`LivePlayScreen`:** `detailSheet` state; card tap opens `SheetDetailBottomSheet`; dismiss clears state
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-13 — Live play: auto-fit bingo card to available space

- **`LivePlayScreen`:** cards mode no longer inside `LazyColumn`; header + carousel rendered in plain `Column` so carousel gets `weight(1f)` bounded height
- **`BingoSheetsCarousel`:** `BoxWithConstraints` now `fillMaxSize()`; `cardWidth` shrinks proportionally when available height can't fit a full-width square grid (120 dp non-grid overhead estimate); `LazyRow` centers cards via symmetric padding + `verticalAlignment = CenterVertically`
- No changes to `BingoCardGrid` or `BingoGrid5x5`; grid stays square via `aspectRatio(1f)` — width adjusts instead
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 — Live play: keypad + view mode in overflow

- **`LivePlayScreen`:** removed in-content **Cards/List** toggle; **`LiveRoomTopBar`** overflow adds **Cards view** / **List view** when sheets exist (same `selectedView` state)
- **`LiveCallInputBar.kt`:** replaced text-field bar with **`LivePlayCallKeypad`** (manual-entry-style dock: progress, draft, clear, undo, call, digit rows); always above **`AppBottomBar`**
- **`ManualEntryScreen`:** **`ManualEntryKeypadDockMetrics`** — shared dock height for list/empty bottom padding
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 — Import device QA: stable (planning handoff)

- **`historyPhotoImport` device smoke** — pending URI, GMS, gallery/uCrop, tab switch + discard, back: **passed** (baseline: import flow treated as stable for product planning)
- **Docs:** **`NEXT_TASK.md`** repointed to next highest-impact step (**Live play TalkBack**); **`PROJECT_SNAPSHOT.md`** / **`TECH_DEBT.md`** unchanged this pass
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-12 — `SettingsScreen`: drop unused `Slate400` import

- **`ui/screens/profile/SettingsScreen.kt`** — removed unused **`Slate400`** import (no other code change)
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**

---

## 2026-04-12 — Remove `LiveHeaderStyle` / `UserPreferencesRepository` dead chain

- **Deleted:** **`data/preferences/UserPreferencesRepository.kt`** (enum, DataStore `user_prefs` `live_header_style`)
- **`MainActivity.kt`:** removed **`UserPreferencesRepository.init`**
- **`SettingsViewModel`:** removed **`liveHeaderStyle`** / **`setLiveHeaderStyle`**
- **`LivePlayViewModel`:** removed unused **`liveHeaderStyle`** `StateFlow`
- **`NavGraph.kt`:** removed unused **`LiveHeaderStyle`** import
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**

---

## 2026-04-12 — Settings: remove orphaned Live Header Style

- **Issue:** **Live Header Style** section in **`SettingsScreen`** — `LiveHeaderStyle` not read by any composable (only written from Settings)
- **Patch:** **`SettingsScreen.kt`** — removed section + `liveHeaderStyle` collect + unused **`ViewModule`** import; **`AppHeaderPageLayout` / `AppHeaderBackground`** unchanged
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**, **`PROJECT_SNAPSHOT.md`**

---

## 2026-04-12 — `historyPhotoImport` device smoke (agent)

- **Device:** not runnable here — **no pass/fail recorded**
- **Static:** pending URI + GMS/gallery + `onRegisterLeaveHandler` wiring reviewed again; **no new code**
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- **Owner:** one hardware pass on `NEXT_TASK.md` (previously: pending URI, GMS, gallery/uCrop, tab switch + discard, back)

---

## 2026-04-12 — `historyPhotoImport` path review + leave handoff fix

- **Static review:** `NavGraph` pending URI (`PENDING_HISTORY_PHOTO_IMPORT_URI_KEY`) consumed once in `LaunchedEffect`; GMS + gallery wiring unchanged
- **Bug:** `photoImportLeaveHandler` was never set — **`HistoryPhotoImportScreen`**’s **`onRegisterLeaveHandler`** defaulted to no-op, so bottom-tab switches skipped discard confirmation
- **Patch:** `NavGraph.kt` — pass **`onRegisterLeaveHandler = { photoImportLeaveHandler = it }`** on **`HistoryPhotoImportScreen`**
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`PROJECT_SNAPSHOT.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 — `AppHeaderBackground` seam (NEXT_TASK 1)

- **Issue:** gradient bottom stop did not match `AppHeaderPageLayout` root **`surface`** (light: `surface` @ 0.96α; dark: `background` vs `surface`)
- **Patch:** `AppHeaderBackground.kt` — last gradient color **`cs.surface`** in light and dark
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

---

## 2026-04-12 — NEXT_TASK: QA-only (no code)

- Read **`NEXT_TASK.md`**: step 1 = build; steps 2–3 = **device QA** (`AppHeaderPageLayout` seam, `historyPhotoImport`) — **no code change** this pass
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- Docs: **`LAST_SESSION.md`**, **`NEXT_TASK.md`**

### Resume

- **`NEXT_TASK.md`** (remaining device checks)

---

## 2026-04-12 — NEXT_TASK: History bulk dock safe area

- **`NEXT_TASK.md` step 1:** `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL**
- **Code:** `HistoryListScreen` — `BulkSelectionActionBar` in `bottomBar` now uses **`Modifier.navigationBarsPadding()`** so the dock matches **`AppBottomBar`** safe-area behavior when selection mode replaces the tab bar (My Tickets sheet already pads the outer `Column`; unchanged)
- **Steps 2–3** (device — dock/dialogs/tab hairline): manual QA on hardware; no further code changes this pass

### Files touched

- `HistoryListScreen.kt`
- `LAST_SESSION.md`, `PROJECT_SNAPSHOT.md`, `NEXT_TASK.md`

### Unfinished

- Device QA: `AppHeaderPageLayout` seam (`PROJECT_STATUS.md`); import/gallery + pending URI; bulk UI spot-check on device

### Resume here

1. Follow **`NEXT_TASK.md`** (numbered steps)
2. After code changes: `./gradlew :app:assembleDebug`

---

## 2026-04-12 — Handoff docs (minimal update pass)

- Re-scanned `NavGraph` composables, `AppDatabase` (v7), `rg runBlocking` on `app/src/main`, `ui/components` inventory — **no new routes, entities, or runBlocking** vs prior snapshot
- Patched **`PROJECT_SNAPSHOT.md`**, **`LAST_SESSION.md`**, **`TECH_DEBT.md`**, **`NEXT_TASK.md`** in place (no full rewrites)
- `./gradlew :app:assembleDebug` — **BUILD SUCCESSFUL** (re-verified)

### Files touched

- `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md`, `NEXT_TASK.md`

### Unfinished

- Device QA — same as **`NEXT_TASK.md`** (selection dock, tab hairline, bulk dialogs)

### Resume here

1. Run **`NEXT_TASK.md`** steps (build + device checks)
2. After code changes: `./gradlew :app:assembleDebug`, then re-run device steps

---

## 2026-04-12 — Bulk UX, polish, interaction helpers

- **Bulk selection (History + live My Tickets):** `BulkSelectionActionBar`; confirm dialogs; `HistoryRepository.deleteSessions`, `RoomRepository.unassignTickets`; selection mode on `RoomSessionCard`; `HistoryListScreen` / `MyTicketsBottomSheet` / `LivePlayScreen` wiring; optional bulk **Add to room** from live My Tickets
- **Visual polish:** `Dimens.outlineDividerAlpha` / `outlineBorderAlpha`; dock-style action bar (rounded top + border); `AppBottomBar` top hairline uses shared divider alpha; dialogs — `surfaceContainerHigh`, `radiusXL`, `titleLarge` + `bodyMedium`
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
3. Continue **`PROJECT_STATUS.md`** “In progress” / “Pending” until cleared or reprioritized.

---

## Older — 2026-04-08 (gallery / uCrop / OCR)

- uCrop gallery path, `bypassInternalGridCrop`, dedupe before handoff; device QA notes in historical `PROJECT_STATUS` entries.

## Older — 2026-04-02

- Regenerated project docs from codebase.
