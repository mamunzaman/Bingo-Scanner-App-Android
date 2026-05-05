# Next task

**Goal:** Device QA — verify new compact called-status icon tile balance in History Detail waiting block.

**Verify:** Waiting/called-status block shows compact 44dp circular status tile (`primary @ 0.12`) with centered icon and balanced text rhythm; section wrapper/spacing unchanged; top-bar actions and grid behavior unchanged; `./gradlew :app:assembleDebug` green.

**Done (status):** `HistoryDetailScreen` called-status waiting block icon was compacted: replaced oversized visual with a 44dp circular status tile (`primary.copy(alpha = 0.12f)`) and centered `PlayArrow` icon in `primary`; title/helper copy preserved and aligned with icon in a centered `Row`. `AppSectionSurface` wrapper and section spacing kept intact. No logic changes. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Optional icon-tile follow-up in remaining screens if needed; `./gradlew :app:assembleDebug` after edits.

**Verify:** Add-from-photo icon action keeps same circular look/size/tap behavior; build green.

**Done (status):** `HistoryListScreen` replaced the manual circular add icon `Box` in `AppTopBar` action with `AppIconTile` (explicit `size=40.dp`, `iconSize=24.dp`, `containerColor=primary`, `iconTint=onPrimary`, circular shape) to preserve visuals and click behavior. Other requested files had no manual icon-box tile pattern to migrate. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Optional: more horizontal dividers → `AppInsetDivider` (e.g. `HistoryListScreen` room picker); other UI polish; `./gradlew :app:assembleDebug` after edits.

**Verify:** Divider lines match prior; build green.

**Done (status):** `AppInsetDivider`: added `endInset`, `thickness` (default 1.dp). Used in `HistorySheetCard` (hairline only), `LivePlayScreen` (sticky header + room settings sheet), `RoomInfoBottomSheet`, `SearchFilterSortHeader`. `MyTicketsBottomSheet` unchanged (vertical `VerticalInfoDivider` only). `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Next UI kit or polish pass as needed (e.g. more `AppCard` / `AppListRow` adoption); `./gradlew :app:assembleDebug` after edits.

**Verify:** Targeted screen QA; build green.

**Done (status):** Replaced `ModalBottomSheet` chrome with `AppBottomSheetSurface` / `rememberAppBottomSheetState` in `HistoryListScreen` (`AddToRoomPickerSheet`), `HistoryDetailScreen` (`RoomPickerBottomSheet`), `LivePlayScreen` (sheet preview + `RoomSettingsBottomSheet`), `ManualEntryScreen` (`RoomPickerBottomSheet`), `MyTicketsBottomSheet`, `RoomInfoBottomSheet`. Preserved `windowInsets`, `shape`, `containerColor`, `sheetState` / `skipPartiallyExpanded` where applicable; inner content untouched. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Migrate next surface to shared UI kit (incremental): e.g. `ModalBottomSheet` → `AppBottomSheetSurface` on History/Live/Manual sheets — one cluster at a time; `./gradlew :app:assembleDebug` after each.

**Verify:** Visual parity; build green.

**Done (status):** `ProfileScreen`: menu leading `AppIconTile` (replaces `AppIconContainer`, same 40/24/surfaceVariant); outer stats / invite / menu use `profileSectionCardShape` for shadow, clip, background with existing `appPremiumCardBorder`; removed unused imports (`width`, `Slate200`, `Slate400`). Invite QR placeholder + code strip inner shapes unchanged. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Migrate screens to shared UI kit (incremental): replace ad-hoc `ModalBottomSheet` with `AppBottomSheetSurface` / `rememberAppBottomSheetState`, then list rows (`AppListRow` + `AppIconTile` + `AppInsetDivider`), then `AppSectionTitle` / `AppCard` where it reduces duplication. Do not batch-rewrite; keep Profile/Settings until a dedicated pass.

**Verify:** After each migrated screen: visual parity on device/emulator; `./gradlew :app:assembleDebug` green.

**Done (status):** New `ui/components/`: `AppIconTile`, `AppListRow` (+ `AppListRowDensity`), `AppInsetDivider`, `AppSectionTitle`, `AppBottomSheetSurface` + `rememberAppBottomSheetState`. `AppCard`: `shape`, `contentPadding`, M3 `Card(onClick = …)` when clickable. `AppSectionSurface` KDoc links kit. `AGENTS.md` list updated. No `ProfileScreen` / `SettingsScreen` edits. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Profile + Settings all section shells look consistent (surface + border + subtle shadow)

**Verify:** DATA / PRIVACY / ABOUT / Developer blocks match grouped sections visually; Profile stats/invite/menu borders match `appPremiumCardBorder` alpha 0.18f; no inner QR tile border.

**Done (status):** `AppSectionSurface`: default `color = surface`, `borderColor = primary @ APP_SECTION_BORDER_ALPHA` (0.18f), `shadowElevation = cardElevationSubtle`, tonal 0. Renamed modifier to `appPremiumCardBorder` (removed `appSectionBorder`). `SettingsSection` uses `AppSectionSurface` (same shape as grouped). `ProfileScreen` uses `appPremiumCardBorder`. `AGENTS.md` updated. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — green-tinted section borders on Profile + Settings grouped cards

**Verify:** Stats / invite / menu rows and Settings LIVE PLAY+ sections show clearer primary-tinted border (~28% alpha); inner invite QR tile unchanged.

**Done (status):** New `AppSectionSurface.kt`: `AppSectionSurface` (`BorderStroke` `cardBorderDefault`, default `borderColor = primary.copy(alpha=APP_SECTION_BORDER_ALPHA)` 0.28f, `surfaceContainerLowest`, 0 elevation); `appSectionBorder(shape)` for non-`Surface` shells. `SettingsGroupedSection` uses `AppSectionSurface`. `ProfileScreen` uses `appSectionBorder(profileSectionCardShape)`; removed local `profileSectionCardBorder`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Profile card borders on device (stats, invite, menu)

**Verify:** Outer stats / invite / four menu rows show thin `outlineVariant` border; shadows and `surface` fills unchanged; inner QR and code strip tiles have no new border.

**Done (status):** `ProfileScreen.kt` — `Modifier.profileSectionCardBorder()` (`composed` + `border` `cardBorderDefault` / `outlineVariant @ 0.25f` / 12dp shape) chained after `background(surface)` on stats row, `InviteParticipantsCard` outer column, each `ProfileMenuItem`. No grouping/Surface refactor. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Settings grouped cards refinement on device

**Verify:** Sections LIVE PLAY / APPEARANCE / NOTIFICATIONS / SECURITY use 72dp rows, 42dp icon tiles (12dp radius, `primaryContainer` @ 0.50f), `surfaceContainerLowest` card fill, lighter border + lighter dividers (alpha 0.20f), section label shifted `start=4dp`.

**Done (status):** Tuned `SettingsGroupedSection` + row composables: `settingsIconTileSize` 48→42dp; icon radius `radiusSearchField`→12dp; icon size 24→22dp; `primaryContainer` alpha 0.45f→0.50f; card `surface`→`surfaceContainerLowest`, border alpha 0.35f→0.25f, tonal elevation removed; divider alpha 0.35f→0.20f; all three grouped rows min height 84→72dp, vertical pad spacing12→spacing10; section label `padding(start=4dp, bottom=6.dp)`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Settings grouped cards on device (LIVE PLAY / APPEARANCE / NOTIFICATIONS / SECURITY)

**Verify:** Section labels unchanged; grouped sections show 20dp rounded `Surface` + outline border; rows ≥84dp with 48dp icon tiles; inset dividers between multi-rows; DATA / PRIVACY / ABOUT unchanged.

**Done (status):** `SettingsScreen.kt` (under `ui/screens/profile/`): `SettingsGroupedSection` + `Surface` (`radiusLarge`, `outlineVariant` border, tonal elevation); `SettingsIconTile` (48dp, `radiusSearchField`, `primaryContainer` @ 0.45f); `SettingsInsetDivider`; `groupedInCard` on toggle/theme/nav rows. Applied to LIVE PLAY, APPEARANCE, NOTIFICATIONS, SECURITY only. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — HTTPS ticket QR opens app (after `assetlinks.json` on host)

**Verify:** (1) Host `https://bingoapp.itconsultingfirma.com/.well-known/assetlinks.json` serves correct Digital Asset Links for this app’s signing cert + package; (2) new QR encodes `https://bingoapp.itconsultingfirma.com/import-ticket?data=…`; (3) device camera tap opens Mamun Bingo (verified link) or chooser until verified; (4) in-app scanner still reads HTTPS / `mamunbingo://` / `intent://` / legacy prefix; (5) `ImportTicketDeepLinkViewModel` accepts HTTPS `ACTION_VIEW` URIs; (6) ticket QR dialog shows muted helper under QR when bitmap visible; hidden while loading/error.

**Done (status):** `encodeDeepLink` → `https://bingoapp.itconsultingfirma.com/import-ticket?data=…`. `QrTicketCodec`: `APP_LINK_HOST`, `isImportTicketDeepLinkUri`, path `/import-ticket`; `decode` / `isLikelyBingoTicketQrString` support https + mamunbingo + intent + prefix. Manifest: second `intent-filter` `android:autoVerify="true"` for `https` + host + `pathPrefix="/import-ticket"`. `ImportTicketDeepLinkViewModel` uses `isImportTicketDeepLinkUri`. `TicketQrDialog`: external-camera hint (`bodySmall`, `onSurfaceVariant`, centered) when QR shown. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — verify QR decode cell positions are correct after fix

**Verify:** (1) Open any saved ticket → QR → scan with live camera → imported manual entry shows exact same B/I/N/G/O numbers in exact same cells; (2) no "73 is not allowed in Column G" or similar column errors; (3) scan with an active live room (room context) → same result; (4) logcat shows `QR_DEBUG: decoded cells rowMajor=…` with expected order.

**Done (status):** Root cause: `buildManualEntryForRoomRoute` did not encode `prefillOrder`, so `ManualEntryScreen` defaulted to `prefillAsRowMajor=false` and wrongly applied `storedColumnOrderToRowMajor` on already row-major QR data. Fixed: added `prefillAsRowMajor: Boolean = false` to `buildManualEntryForRoomRoute`, included `&prefillOrder=rowMajor` in URL when true; added `prefillOrder` nav argument to `manualEntryForRoom` route; `parseManualEntryForRoomFromNav` reads it; `ManualEntryScreen` in the ForRoom composable now receives `prefillAsRowMajor = mer.prefillAsRowMajor`. QR decode callback and history photo import both pass `prefillAsRowMajor = true`. Added `Log.d("QR_DEBUG", …)` in `decodeBingoFromBarcodes`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — verify in-room sheet clarity improvements

**Verify:** `./gradlew :app:assembleDebug`; in History: (1) cards with a room show "In: <room name>" pill (fallback "In room" if name unavailable); (2) tap Select → in-room cards show tiny muted hint "Already added — remove first to move rooms" under date; (3) select only in-room cards → "Add eligible" button hidden, muted info text "Selected sheets are already in a room. Remove first to add to another room." visible above delete row; (4) mixed selection → "Add eligible (N)" shows correct count of non-in-room only; (5) room picker receives only non-in-room IDs.

**Done (status):** `HistorySheetCard` gained `roomName: String?` param; pill text is now `"In: $roomName"` / `"In room"` fallback; in `selectionMode && inRoom` shows muted `labelSmall` hint below subtitle. `BulkSelectionActionBar` gained `inRoomInfoText: String?` which renders above action buttons when set; "Add to room" label changed to "Add eligible". `HistoryListScreen` derives `allSelectedInRoom`; passes `inRoomInfoText` when all-in-room; `roomName = item.roomName` passed to each card. `./gradlew :app:assembleDebug` OK.

**Verify:** `./gradlew :app:assembleDebug`; in History: (1) select sessions without a room → tap "Add to room (N)" → picker opens with live rooms; (2) tap a room → sessions are assigned, selection clears, cards immediately show "In room" pill; (3) with mixed selection (some in-room) → picker shows, only the non-room count appears on button, already-in-room sheets are not duplicated; (4) no active rooms → empty state "No active live rooms" + "Create live room" CTA works; (5) select only in-room sheets → "Add to room" button not shown; (6) logcat: no `AlreadyInRoom` errors for newly-added sheets.

**Done (status):** `HistoryViewModel.addSessionsToRoom(roomId, sessionIds)` iterates session IDs, looks up `ticketId` via `sessions.first()`, checks `RoomRepository.findAssignedRoomId(ticketId)` to skip already-assigned, calls `assignTicketToRoom` for eligible ones (state refreshes via DB flow). `NavGraph` obtains the back-stack-scoped `HistoryViewModel` with `viewModel(backStackEntry)` and wires `onBulkAddToRoom = { roomId, ids -> historyVm.addSessionsToRoom(roomId, ids) }`. `HistoryListScreen` `onAddToRoomClick` guards `if (selectedSessionsNotInRoom.isNotEmpty())` before opening picker. Button label shows eligible count from `selectedSessionsNotInRoom.size`. `./gradlew :app:assembleDebug` OK.

**Verify:** `./gradlew :app:assembleDebug`; in History: (1) select sheets without a room → tap "Add to room" in bulk bar → `AddToRoomPickerSheet` opens, title "Choose live room"; (2) with no active rooms → shows "No active live rooms" + "Create live room" CTA (tapping dismisses sheet and navigates to Live); (3) with active rooms → each room shows name + "Add" label; (4) tap a room → `onBulkAddToRoom(roomId, ids)` fires with only non-room session IDs, selection clears, sheet closes; (5) mixed selection (some in-room, some not) → only not-in-room sheets in `pendingIds`, already-in-room sheets skipped; (6) tap Cancel → sheet closes, selection stays; (7) in-room-only selection → "Add to room" button absent, only "Join live" visible.

**Done (status):** `HistoryViewModel` now exposes `liveRooms: StateFlow<List<LiveRoom>>` via `RoomRepository.getRooms()`. `HistoryListScreen` adds `showRoomPicker` state; `onAddToRoomClick` sets it to `true`; `AddToRoomPickerSheet` (private composable, `ModalBottomSheet`) shows live rooms or empty state + "Create live room" CTA; on room tap fires `onBulkAddToRoom(roomId, pendingIds)` with only the non-room session IDs, then clears selection. `onBulkAddToRoom` signature updated to `(roomId: String, sessionIds: Collection<String>) -> Unit`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Verify "In room" pill + bulk Add to room on device — history sheets in a room should show the pill, and bulk selection should expose the "Add to room" button only for eligible (not-yet-in-room) sheets.

**Verify:** `./gradlew :app:assembleDebug`; in History: (1) sessions that have a `roomId` show a small muted-green "In room" pill next to their title, (2) tap **Select** and choose only in-room sheets → only "Join live" appears in bulk bar, no "Add to room", (3) select only non-in-room sheets → only "Add to room" appears, (4) mixed selection → both "Join live" + "Add to room" show, (5) tapping "Add to room" fires `onBulkAddToRoom` with only the non-room IDs (the callback is wired by the nav caller), (6) cards without a room show no pill.

**Done (status):** `HistorySheetCard` gained optional `inRoom: Boolean` param; when true renders a `primaryContainer @ 0.45` pill with `primary` text "In room" inline with the title. `HistoryListScreen` derived `selectedSessionsNotInRoom`; `showAddToRoom` / `addToRoomEnabled` / `addCount` / `onAddToRoomClick` wired to the bulk bar (only eligible IDs passed to `onBulkAddToRoom`); `showJoinLive` now conditionally shown only when in-room selection is non-empty; `inRoom = item.roomId != null` passed to each card. `./gradlew :app:assembleDebug` OK.

**Verify:** `./gradlew :app:assembleDebug`; in History (with sessions): (1) **without selection** the mini-grid + title sit at their original x-position (no leading gap), (2) tap **Select** → 42dp slot appears, checkbox centered, mini-grid + title shift right consistently for all selected/unselected cards while in selection mode, (3) exit selection mode → cards return to original alignment immediately, (4) mini-grid → title gap stays 10dp, (5) bulk action bar spacing/disabled colors unchanged from previous patch.

**Done (status):** `HistorySheetCard` — wrapped the 42.dp leading-slot `Box` in `if (selectionMode) { … }`, so normal mode renders nothing leading (original alignment restored) while selection mode keeps the centered checkbox in the slot. Mini-grid → title `spacedBy(spacing10)` and bulk action bar polish (from previous patch) untouched. `./gradlew :app:assembleDebug` OK.

---

## Roadmap: pro-level unified full-ticket scan (CameraX, replace GMS later)

**Problem:** GMS Document Scanner is a black box — no live QR detection, limited camera control. **Custom CameraX** enables a single pro UX. **Do not** drop the current OCR stack or GMS handoff until the new path is stable on devices.

| Phase | Scope |
|-------|--------|
| **1** | **Custom CameraX capture screen** — live preview, QR auto-detect (reused), manual capture, output bitmap/URI. |
| **2** | **Ticket photo → existing OCR** — feed capture into `historyPhotoImport` / `ImportTicketViewModel`; **no OCR rewrite**. |
| **3** | **Crop/confirm screen** — replace GMS crop behavior **gradually**; **leave gallery + uCrop** flow unchanged. |
| **4** | **Remove GMS scanner** only after **device QA** on the custom flow passes. |

---

## UI consistency audit — 2026-04-27 (read-only, code-sampled)

*Backlog: pick one cluster for a minimal patch when not on CameraX work.*

### Headers — Home, Live, History, Settings, Import, Detail

- **Green band shell (`AppHeaderPageLayout` + `AppTopBar`):** `HomeScreen`, `LiveRoomsScreen`, `HistoryListScreen`, `SettingsScreen`, `ProfileScreen`, `LoginScreen`, `MyAccountScreen`, `LiveSheetDetailScreen` (pattern aligned).
- **Outlier — Jackpot tab:** `JackpotScreen` uses `Column` + `MaterialTheme.colorScheme.background` and **`AppTopBar` only** (no `AppHeaderPageLayout`), so the Live/Jackpot hub does not match the Home/History/Settings header treatment.
- **Import:** `ImportTicketScreen` uses `Box(statusBarsPadding())` + `AppTopBar` on `background` — no green band; differs from `AppHeaderPageLayout` tabs.
- **Details / subflows:** `HistoryDetailScreen` (and other flows) mix **`AppHeaderBackground` + `Scaffold`** with **`AppTopBar`** vs the `AppHeaderPageLayout` pattern — background/readability may match intent but **top-of-screen hierarchy** is not one pattern everywhere.

**Actionable:** Decide one rule — e.g. “all main/leaf surfaces use `AppHeaderPageLayout` unless full-bleed exception” — then align `JackpotScreen` and `ImportTicketScreen` first, then sweep detail screens.

### Cards / containers — border, radius, shadow, padding

- **Elevation:** `iosElevatedShadow` and/or tonal cards appear on Home/hero paths; `HistorySheetCard` / `RoomSessionCard` families use different elevation/border (see `PROJECT_STATUS` / recent polish notes).
- **Borders / radius:** `Dimens.radiusCard`, `outlineVariant` alpha, and `cardBorderDefault` show up in newer sheets; older cards may still use different shape/border weight.

**Actionable:** Pick one “list row card” spec (radius, border alpha, shadow 0 vs subtle) and one “hero/featured card” spec; diff `HistorySheetCard` vs `RoomSessionCard` / `JackpotHeroCard` in a single pass when patching.

### Buttons / actions — height, radius, emphasis, typography

- **Primary CTA:** `AppPrimaryButton` is `Dimens.buttonHeight` + `MaterialTheme.shapes.medium` + `typography.labelLarge` — use as the reference.
- **Live rooms:** header **Add** is a custom **40dp** primary circle, not a standard `IconButton` or FAB pattern — may be intentional but reads different from `AppTopBar` `actions` elsewhere.
- **Room settings bottom sheet** (`RoomSettingsBottomSheet` in `LivePlayScreen.kt`): `OutlinedButton` for chips and delete — not necessarily `AppPrimaryButton` height for destructive row.

**Actionable:** For the next patch, target **one** surface (e.g. settings sheet or import CTAs) and align to `AppPrimaryButton` / M3 `ButtonDefaults` where appropriate.

### Spacing rhythm — big gaps, bottom space, list rhythm

- **Top spacing:** e.g. `LiveRoomsScreen` content uses `padding(top = Dimens.spacing24)` + `spacedBy(20.dp)`; `JackpotScreen` uses `24.dp` top in scroll branch; `Settings`/`Home` use `Dimens.spacing8` top under bar — **not a single “section top” token** across primary tabs.
- **Scroll bottom:** Many screens use `Dimens.spacing16` bottom padding; `ImportTicketScreen` uses `spacing16` + `spacing24` split for main column — check **bottom bar** clearance vs list screens.

**Actionable:** Standardize `screen top inset below header` to one of `Dimens.spacing8` | `spacing16` | `spacing24` per shell type; document in one comment or `Dimens` alias when patching.

### Typography — title / label / value

- **Sheet titles:** `RoomInfoBottomSheet` uses **titleLarge + Bold** for “Room Info”; `RoomSettingsBottomSheet` uses **titleMedium** for “Room settings” (same app, different level).
- **Headlines in sheets:** `RoomInfoBottomSheet` uses `headlineSmall` for the room name; detail/list screens may use `titleLarge` / `titleMedium` differently for the same “primary name” role.

**Actionable:** One sheet title style (`titleLarge` vs `titleMedium`) and one “entity name” style for room/sheet name across Info/Settings/Detail.

### Bottom sheets — padding, radius, tone

- **Container color:** `MyTicketsBottomSheet` — `containerColor = surface`. `RoomInfoBottomSheet`, `SheetDetailBottomSheet`, `RoomSettingsBottomSheet` — **`surfaceContainer`**. Inconsistent **tone** between in-room “My tickets” and other modals.
- **Horizontal padding:** `RoomInfo` / `RoomSettings` use **24.dp** all around; `MyTicketsBottomSheet` content uses `Dimens.screenHorizontalPadding` (not necessarily 24); `SheetDetailBottomSheet` uses `screenHorizontalPadding` + `spacing16` bottom.
- **Drag handle:** `MyTicketsBottomSheet` has a **custom** 32×4 handle block; `SheetDetailBottomSheet` inlines a **40×4** handle; `RoomInfoBottomSheet` relies on default M3 handle (no custom block in file) — **handle presence/size differs**.

**Actionable:** Unify on **one** `containerColor` rule (e.g. all live play sheets = `surfaceContainer` or all = `surface`) and **one** handle + horizontal padding (token from `Dimens` vs hardcoded 24).

---

## After the next patch

1. After **scanner transition / capture feedback** work: re-check **QR**, **Scan ticket** (frame crop), **GMS**, **gallery** on device. For **UI audit** patches: same visual checklist.
2. Keep `./gradlew :app:assembleDebug` green after any change.
