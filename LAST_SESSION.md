# Last session

## 2026-04-27 — `mamunbingo://import-ticket` + deep-link ticket QR

- **`QrTicketCodec`:** `encodeDeepLink`, `decode` accepts prefix + URI, `isLikelyBingoTicketQrString` for ML Kit; **generate** QR with deep link (History / Ticket / Live). **`ImportTicketDeepLinkViewModel`** + **`MainActivity`** `onNewIntent`; **`NavGraph`** `ImportTicketDeepLinkHandler` → `buildManualEntryRoute` (row major, los/serial) after non-auth route. **Manifest** `singleTask` + `VIEW` `mamunbingo` / `import-ticket`. **`./gradlew :app:assembleDebug`** OK.

---

## 2026-04-27 — Live keypad: snackbar for invalid (same path as duplicate)

- **`LiveCallInputBar`:** `callEnabled` when `draft.trim()` non-empty (so **Add** runs validation); **empty** still no Add. **`LivePlayScreen` `handleCallClick`:** `n == null` → *Invalid Bingo number*; out of 1..75 → *Enter a number between 1 and 75*; duplicate snackbar + **`SnackbarDuration.Short`**. **Build** OK.

---

## 2026-04-27 — **Keep screen on** setting (DataStore + Settings + Live)

- **`SettingsRepository`:** `keep_screen_on_during_game` (default true), Flow + `setKeepScreenOnDuringGame`. **`SettingsViewModel`:** `keepScreenOnDuringGame` + setter. **Settings** (profile): **LIVE PLAY** section, **StayCurrentPortrait** icon, title/subtitle as spec. **`LivePlayScreen`:** `collectAsStateWithLifecycle` + `DisposableEffect(keepScreenOnDuringGame)` — add flag only if true; **onDispose** always clear. **`./gradlew :app:assembleDebug`** OK.

---

## 2026-04-27 — `LivePlayScreen` keep screen on

- **`FLAG_KEEP_SCREEN_ON`** via `activity?.window` in `DisposableEffect(Unit)`; **`onDispose`** clears. **Only** this screen. **`./gradlew :app:assembleDebug`** OK.

---

## 2026-04-27 — Live play keypad haptics (`LiveCallInputBar.kt`)

- Digits: **`TextHandleMove`** only when a digit is **appended** (not when draft is already 2 chars). **Add (return):** removed duplicate haptic; success still from **`LivePlayScreen`** `handleCallClick` when the call is **accepted** (or random). **`./gradlew :app:assembleDebug`** OK.

---

## 2026-04-27 — Live `SheetDetailBottomSheet` — QR in dialog (not in sheet)

- **Header:** `Icons.Filled.QrCode2` at **48dp** (between title and **Marked n/25**), opens **`TicketQrDialog`**. Payload: **`cellsToQrGrid5x5(gridCells)`**, `serial` / `los` from `LiveSheetUi`, **`QrTicketCodec.encode`** + **`QrTicketImageGenerator.generateBitmap`**. **`TicketQrDialog`:** added **`isLoading`** (spinner) before error/bitmap. No bottom-sheet scroll, no embedded QR. **Files:** `LivePlayScreen.kt` (private `SheetDetailBottomSheet`), `TicketQrDialog.kt`. **`./gradlew :app:assembleDebug`** OK.

**Files:** `LivePlayScreen.kt`, `TicketQrDialog.kt`, `PROJECT_STATUS.md`, `NEXT_TASK.md`, `LAST_SESSION.md`.

---

## 2026-04-27 — `AppPrimaryButton` loading: centered Row + primary colors while capturing

- **Loading row:** `fillMaxWidth` + `Center` H/V; 18dp `CircularProgressIndicator` (2dp), **“Capturing”** from `text`; `onPrimary` on text + indicator; M3 `disabled*Color` = primary / onPrimary when `loading` so the button does not look grayed. **`BingoLiveCameraImportScreen`** `text` when capturing → **“Capturing”** (not ellipsis). Capture logic unchanged. **`./gradlew :app:assembleDebug`** OK.

**Files:** `AppPrimaryButton.kt`, `BingoLiveCameraImportScreen.kt`, `PROJECT_STATUS.md`, `NEXT_TASK.md`, `LAST_SESSION.md`.

---

## 2026-04-27 — `BingoLiveCameraImportScreen` capture feedback (shutter + scale + CTA)

- **Shutter:** full-screen white overlay alpha 0→0.8→0 (~60ms + ~60ms = ~120ms).
- **Frame:** `Animatable` + `graphicsLayer` scale 1→0.97→1 on preview+viewfinder `Box` (in/out <200ms).
- **UI while capturing:** back `IconButton` disabled; `AppPrimaryButton` text “Capturing…”, `loading = true`. `takePicture` / QR unchanged.
- **Build:** `./gradlew :app:assembleDebug` OK. **Next:** device QA (see `NEXT_TASK.md`).

**Files:** `BingoLiveCameraImportScreen.kt`, `PROJECT_STATUS.md`, `NEXT_TASK.md`, `LAST_SESSION.md`.

---

## 2026-04-27 — Docs: CameraX + frame crop **stable**; next = scanner premium polish

- **Status (device QA + build):** (1) **QR** auto-import — works. (2) **Scan ticket** — CameraX + **green-frame crop** → `historyPhotoImport` — works. (3) **GMS** fallback — works. (4) **Gallery / editor (uCrop)** — unchanged, works. **Build** `./gradlew :app:assembleDebug` OK.
- **Next task:** premium **transition** + **capture feedback** on the live scanner (see `NEXT_TASK.md`).

**Files:** `PROJECT_STATUS.md`, `NEXT_TASK.md`, `LAST_SESSION.md`.

---

## 2026-04-27 — `BingoLiveCameraImportScreen` one-button UI

- Single **Scan ticket** → same CameraX still + `historyPhotoImport`. **GMS** only: internal fallback on capture/temp/URI errors, and **Use document camera** when camera permission off. Removed dual CTAs, preview-fade, handoff delay. Text: “Scan a Bingo QR to import instantly.” + “Or scan your ticket.”

**Files:** `BingoLiveCameraImportScreen.kt` (+ `PROJECT_STATUS` / `NEXT_TASK`).

---

## 2026-04-27 — `BingoLiveCameraImportScreen` full-ticket still (CameraX) + `historyPhotoImport` handoff

- **`ImageCapture`** bound with existing Preview + `ImageAnalysis` (QR unchanged). Manual **Capture full ticket** → temp JPEG in **cache** + `FileProvider` URI. **`onFullTicketPhotoCaptured`** in `NavGraph` sets `PENDING_HISTORY_PHOTO_IMPORT_URI_KEY` and navigates to `historyPhotoImport` (same pattern as GMS). **Full ticket scan** = GMS secondary (`TextButton` + existing fade). **`fullTicketImportLocked`** + `handled` guard: no capture or GMS after **QR** success.
- **`./gradlew :app:assembleDebug`** OK. Next: **device QA**; roadmap Phase 3 = **crop/confirm** (see `NEXT_TASK`).

**Files:** `BingoLiveCameraImportScreen.kt`, `NavGraph.kt` (+ `PROJECT_STATUS` / `NEXT_TASK` / `LAST_SESSION`).

---

## 2026-04-27 — Custom CameraX full-ticket scanner plan (docs only)

**Planning only — no code changes.**

- **Why:** GMS Document Scanner is opaque; custom CameraX enables live QR + full control and a unified pro scan UX long-term.
- **Rules:** Keep existing GMS entry points and the full `ImportTicketViewModel` / `historyPhotoImport` OCR path until the new flow is device-proven. Do not remove or rewrite OCR in the roadmap phases 1–3.

**Phases (see `NEXT_TASK.md` table)**

1. **Phase 1 — next implementation focus:** Custom CameraX **capture** screen: live preview, **reuse** existing QR auto-detect, manual **capture** button, output **bitmap/URI**.
2. **Phase 2:** Feed capture into the **existing** history photo import + VM pipeline — no OCR rewrite.
3. **Phase 3:** Optional **crop/confirm**; replace GMS crop gradually; **gallery + uCrop unchanged**.
4. **Phase 4:** Remove GMS document scanner only after **device QA** passes on the custom path.

**Files updated:** `NEXT_TASK.md` (goal + roadmap + next task = Phase 1), `PROJECT_STATUS.md`, `LAST_SESSION.md`.

**Validation:** `./gradlew :app:assembleDebug` (OK — docs only).

**Resume:** Implement **Phase 1** (Custom CameraX full-ticket capture screen) when starting feature work; keep GMS as the shipped handoff until Phase 3/4 are ready.

---

## 2026-04-27 — whole-app UI consistency audit (read-only) + handoff files

- **Live overlay QA** — stable.
- **Live list bulk select** — OK.
- **Next phase started** — **whole-app UI consistency audit** (findings in `NEXT_TASK.md`; no code changes in this pass).

**Today’s changes**

- **`NEXT_TASK.md`** — Replaced “device QA” focus with a structured **audit** (headers, cards, buttons, spacing, typography, bottom sheets) and short **actionable** follow-ups; no implementation.
- **`LAST_SESSION.md`** — this update.
- **`PROJECT_STATUS.md`** — top line: Live device QA complete; UI consistency audit started.

**Files modified (this pass)**

- `NEXT_TASK.md`
- `LAST_SESSION.md`
- `PROJECT_STATUS.md`

**What is unfinished**

- **Pick a single minimal UI patch** from the audit groups in `NEXT_TASK.md` (e.g. header shell for `JackpotScreen` vs sheet `containerColor` / title typography) — then implement and re-verify on device.
- Deeper list-card unification (hero vs row) is optional second wave.

**Exact resume point**

1. Read `NEXT_TASK.md` → “UI consistency audit” section; choose one cluster to patch.
2. Implement the smallest change that matches existing tokens (`Dimens`, `MamunBingoTheme`); avoid drive-by refactors.
3. `./gradlew :app:assembleDebug` then **visual** pass on device.
4. Update `PROJECT_STATUS.md` + `LAST_SESSION.md` + `NEXT_TASK.md` after the patch.

---

## 2026-04-27 — project documentation refresh (earlier in day)

- Regenerated `PROJECT_SNAPSHOT.md`, `TECH_DEBT.md` from full-repo scan; `./gradlew :app:assembleDebug` OK.

**Prior implementation context**

- **History list:** `HistorySheetCard`, `MiniBingoGrid`, bulk + `BulkSelectionActionBar` + optional **Join live**; `NavGraph` `onJoinLiveRoom`.
- **Live list:** `ListSheetRow` selection, `LiveRoomTopBar` bulk, `BulkSelectionActionBar` (in-room: Join live hidden on bulk bar as designed).
- **Live play:** `LivePlayScreen` + bottom sheets (`MyTicketsBottomSheet`, `RoomInfoBottomSheet`, `RoomSettingsBottomSheet`, `SheetDetailBottomSheet`).

Use `git log` for older history.
