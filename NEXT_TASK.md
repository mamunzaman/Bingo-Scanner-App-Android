# Next task

**Goal:** Pick **one** cluster from the audit below (e.g. `JackpotScreen` + `AppHeaderPageLayout`, or sheet `containerColor` + handle) and apply a **minimal** patch.

**Verify:** `./gradlew :app:assembleDebug`; quick visual on touched screen(s).

**Done this session:** `BingoLiveCameraImportScreen` — “Full ticket scan” handoff: preview fade + `AppPrimaryButton` loading (~240ms) before GMS; `BackHandler` off during exit. Earlier: `bingoLiveCameraImport` QR + document path, gallery QR, `TicketQrDialog`.

---

## UI consistency audit — 2026-04-27 (read-only, code-sampled)

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

1. Re-run the same **visual** checklist on device after the patch.
2. Keep `./gradlew :app:assembleDebug` green after any change.
