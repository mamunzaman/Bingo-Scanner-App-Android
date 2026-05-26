# Next task

**Goal:** Device QA — Settings toggles persist after app restart.

**Verify:** Toggle Push / Daily reminders / FaceID / Data sharing / demo data / keep screen on → kill app → reopen Settings → values kept; theme unchanged; `./gradlew :app:assembleDebug` green.

**Done (status):** Four notification/security/privacy toggles moved to `SettingsRepository` DataStore; `SettingsViewModel` flows + save on change. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Profile header reflects saved My Account name/email/initials after save and back.

**Verify:** Empty profile → Guest + guest email + Person icon; save name/email in My Account → Profile updates without app restart; initials from name; `./gradlew :app:assembleDebug` green.

**Done (status):** `AccountViewModel.profile` + Profile header wired; shared VM scoped to `main`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — My Account persistence: save, leave, reopen shows saved fields.

**Verify:** Fill profile → Save → snackbar “Account saved locally” → back → reopen My Account → values restored; invalid save still shows errors; `./gradlew :app:assembleDebug` green.

**Done (status):** `AccountProfile` + `AccountRepository` (DataStore) + `AccountViewModel`; `AccountFormScreen` wired. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — My Account form styling.

**Done (status):** `AccountFormScreen` auth/profile UI. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — ONLINE position fallback on partial/cropped digital tickets (no visible header).

**Verify:** Cropped grid screenshot → Logcat `OnlineBingoOcr`: raw candidates, after footer filter, grouped row count, final valid ≥15 → preview/manual prefill; serie/los still logged; `./gradlew :app:assembleDebug` green.

**Done (status):** Position-based ML Kit grid fallback in `OnlineBingoOcr` (Y-rows, X-columns, footer exclusion); prefers fallback when ≥15 valid cells. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — ONLINE bingo OCR on real digital ticket screenshots.

**Done (status):** `OnlineBingoOcr` + VM ONLINE branch + scan type handoff. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Wire `BingoScanType` into OCR pipeline.

**Done (status):** Scan type sheet + nav arg + debug toast. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Live Grid View bingo card breathing room.

**Done (status):** `BingoSheetsCarousel` padding top/bottom 12/8 → 16/16. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — premium clean TV board.

**Done (status):** Single centered stack version. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — TV board 5 columns visible on small phones.

**Done (status):** Equal-weight columns, adaptive scale. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — manual entry sheet-title focus/keyboard stress.

**Done (status):** Screen-owned FocusRequester + safe dismiss. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — manual entry rename Logcat traceability.

**Done (status):** `logRenameState` in ViewModel. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — manual entry sheet rename (5× quick) must not crash.

**Done (status):** VM draft/commit rename flow. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — polished TV scoreboard look.

**Done (status):** TV board scoreboard polish. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — TV board column clarity + vertical spacing.

**Done (status):** Column separators + centered lanes. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — clean TV board (no inset panels).

**Done (status):** Single app-green surface; no inset panels. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — premium TV board depth + underline accents.

**Done (status):** Superseded by clean single-surface board. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — TV board app-green + 5-lane overflow readability.

**Done (status):** Primary-green surface; 5 main / overflow right. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Called Numbers sheet + TV board text readability.

**Done (status):** Removed `LatestCallPill`; text-only tiers; centered lanes; no empty-column lines. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Called Numbers sheet: compact latest pill + premium board numbers.

**Done (status):** Superseded by text-first board (no latest pill). `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Live Play list bottom padding / dock spacing.

**Done (status):** List `contentPadding.bottom` = `spacing8`; empty state `spacing16`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Live Play keypad dock + list density.

**Done (status):** Compact keypad dock; list ticket gap `spacing4`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Live Play list density + bottom dock spacing.

**Done (status):** List ticket gap `spacing4`; sticky→first `spacing5`; bottom dock padding logic untouched. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — Live Play list bottom spacing with keypad collapsed vs expanded.

**Verify:** List view: hide keypad → last ticket sits naturally; show keypad → last ticket scrolls clear of digit rows; `./gradlew :app:assembleDebug` green.

**Done (status):** `LivePlayCallKeypadMetrics`; animated `liveInputDockHeight` + `liveListLazyContentBottomPad` from `showNumberKeypad`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — compact live top bar polish: **LAST** chip + highlighted number, circular history button, tighter row height.

**Verify:** Collapsed card looks balanced; history expand unchanged; `./gradlew :app:assembleDebug` green.

**Done (status):** `LiveRoomWithHistoryCard` — `LiveLastCalledPremiumChip`, `LiveHistoryToggleButton`, tighter padding. `./gradlew :app:assembleDebug` OK.

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

**Done (status):** `SettingsScreen.kt` (under `ui/screens/profile/`): `SettingsGroupedSection` + `Surface` (`radiusLarge`, `outlineVariant` border, subtle tonal elevation); `SettingsIconTile` (48dp, `radiusSearchField`, `primaryContainer` @ 0.45f); `SettingsInsetDivider`; `groupedInCard` on toggle/theme/nav rows. Applied to LIVE PLAY, APPEARANCE, NOTIFICATIONS, SECURITY only. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — HTTPS ticket QR opens app (after `assetlinks.json` on host)

**Verify:** (1) Host `https://bingoapp.itconsultingfirma.com/.well-known/assetlinks.json` serves correct Digital Asset Links for this app’s signing cert + package; (2) new QR encodes `https://bingoapp.itconsultingfirma.com/import-ticket?data=…`; (3) device camera tap opens Mamun Bingo (verified link) or chooser until verified; (4) in-app scanner still reads HTTPS / `mamunbingo://` / `intent://` / legacy prefix; (5) `ImportTicketDeepLinkViewModel` accepts HTTPS `ACTION_VIEW` URIs; (6) ticket QR dialog shows muted helper under QR when bitmap visible; hidden while loading/error.

**Done (status):** `encodeDeepLink` → `https://bingoapp.itconsultingfirma.com/import-ticket?data=…`. `QrTicketCodec`: `APP_LINK_HOST`, `isImportTicketDeepLinkUri`, path `/import-ticket`; `decode` / `isLikelyBingoTicketQrString` support https + mamunbingo + intent + prefix. Manifest: second `intent-filter` `android:autoVerify="true"` for `https` + host + `pathPrefix="/import-ticket"`. `ImportTicketDeepLinkViewModel` uses `isImportTicketDeepLinkUri`. `TicketQrDialog`: external-camera hint (`bodySmall`, `onSurfaceVariant`, centered) when QR shown. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — verify QR decode cell positions are correct after fix

**Verify:** (1) Open any saved ticket → QR → scan with live camera → imported manual entry shows exact same B/I/N/G/O numbers in exact same cells; (2) no "73 is not allowed in Column G" or similar column errors; (3) scan with an active live room (room context) → same result; (4) logcat shows `QR_DEBUG: decoded cells rowMajor=…` with expected order.

**Done (status):** Root cause: `buildManualEntryForRoomRoute` did not encode `prefillOrder`, so `ManualEntryScreen` defaulted to `prefillAsRowMajor=false` and wrongly applied `storedColumnOrderToRowMajor` on already row-major QR data. Fixed: added `prefillAsRowMajor: Boolean = false` to `buildManualEntryForRoomRoute`, included `&prefillOrder=rowMajor` in URL when true; added `prefillOrder` nav argument to `manualEntryForRoom` route; `ManualEntryScreen` in the ForRoom composable now receives `prefillAsRowMajor = mer.prefillAsRowMajor`. QR decode callback and history photo import both pass `prefillAsRowMajor = true`. Added `Log.d("QR_DEBUG", …)` in `decodeBingoFromBarcodes`. `./gradlew :app:assembleDebug` OK.

---

**Goal (previous):** Device QA — verify in-room sheet clarity improvements

**Verify:** `./gradlew :app:assembleDebug`; in History: (1) cards with a room show "In: <room name>" pill (fallback "In room" if name unavailable); (2) tap Select → in-room cards show tiny muted hint "Already added — remove first to move rooms" under date; (3) select only in-room cards → "Add eligible" button hidden, muted info text "Selected sheets are already in a room. Remove first to add to another room." visible above delete row; (4) mixed selection → "Add eligible (N)" shows correct count of non-in-room only; (5) room picker receives only non-in-room IDs.

**Done (status):** `HistorySheetCard` gained `roomName: String?` param; pill text is now `"In: $roomName"` / `"In room"` fallback; in `selectionMode && inRoom` shows muted `labelSmall` hint below subtitle. `BulkSelectionActionBar` gained `inRoomInfoText: String?` which renders above action buttons when set; "Add to room" label changed to "Add eligible". `HistoryListScreen` derives `allSelectedInRoom`; passes `inRoomInfoText` when all-in-room; `roomName = item.roomName` passed to each card. `./gradlew :app:assembleDebug` OK.

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

## After the next patch

1. After **scanner transition / capture feedback** work: re-check **QR**, **Scan ticket** (frame crop), **GMS**, **gallery** on device. For **UI audit** patches: same visual checklist.
2. Keep `./gradlew :app:assembleDebug` green after any change.
