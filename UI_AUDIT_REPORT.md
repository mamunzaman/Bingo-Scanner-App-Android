# UI Audit Report — Mamun Bingo App

## Summary

Audit focused on visual consistency across Live Room, Live Play, My Tickets bottom sheet, and bottom navigation. All fixes use Material 3 theme tokens; no hardcoded hex colors were added. Shared components were updated so screens inherit consistent styling.

---

## A) What Was Inconsistent

| Area | Issue |
|------|--------|
| **Bottom navigation** | Selected tab used `primary` (green) for icon and label in both themes, so in dark mode the selected item was green instead of white. |
| **Bottom navigation** | Selected pill used `primaryContainer`, which read as a strong green tint in dark mode. |
| **AppTopBar** | Navigation and action icon colors were not set explicitly; they could inherit defaults that didn’t match `onSurface`. |
| **My Tickets sheet** | List row cards used `surfaceVariant.copy(alpha = 0.6f)` with no border, differing from other list/card surfaces. |
| **My Tickets sheet** | Search field used semi-transparent container and `outline` for border instead of solid `surfaceVariant` and `outlineVariant`. |
| **Bottom sheets** | Room Settings and My Tickets used default sheet container; no shared token for sheet background. |
| **Theme** | No `surfaceContainer` or `scrim` tokens, which are useful for sheets and overlays. |

**Hardcoded colors:** Design system lives in `theme/Color.kt` (e.g. Primary, Slate200, GreenImpactBg). Screens use `MaterialTheme.colorScheme.*` and `.copy(alpha)`; no new raw hex/Color(0x…) was introduced. Existing theme colors were kept; tokens were added and mapped in Theme.kt.

---

## B) Theme Tokens (Single Source of Truth)

**Added in `Color.kt`:**
- `SurfaceContainer` (light), `DarkSurfaceContainer` (dark) — for sheet and container surfaces.
- `Scrim`, `DarkScrim` — for overlay/scrim.

**Updated in `Theme.kt`:**
- Light and dark `ColorScheme` now set `surfaceContainer` and `scrim`.
- Existing tokens unchanged: `background`, `surface`, `surfaceVariant`, `outlineVariant`, `primary`, `onPrimary`, `secondaryContainer`, `onSecondaryContainer`, `error`, `onError`.

**Primary vs navigation selection:**
- Primary green remains for primary actions (e.g. “Add to Room”, “Go Live”).
- Bottom bar selected state no longer uses primary in dark mode; it uses `onSurface` (white) with a `surfaceVariant` pill so the selected tab is clear without a green icon.

---

## C) Component Updates (Reusable)

### 1) AppTopBar
- **Container:** Already `colorScheme.surface`.
- **Title:** Already `onSurface`.
- **Change:** Set `navigationIconContentColor` and `actionIconContentColor` to `onSurface` so back arrow and actions (e.g. settings, +) match the title.

### 2) AppBottomBar
- **Container:** Unchanged (`surface`).
- **Selected tab:**
  - Background: `primaryContainer` → `surfaceVariant` (neutral pill in both themes).
  - Icon and label: `primary` → `onSurface` (white in dark mode, dark in light mode).
- **Unselected:** Still `onSurfaceVariant`.

### 3) Buttons
- No code change. Primary actions already use `primary` / `onPrimary`; secondary/tonal use theme defaults. `AppPrimaryButton` already uses `colorScheme.primary` and `onPrimary`.

### 4) Chips (My Tickets: All / Today / This Week)
- Unchanged. Already use `primaryContainer` / `onPrimaryContainer` when selected and `surfaceVariant` / `onSurfaceVariant` when unselected.

### 5) Search field (My Tickets)
- Container: `surfaceVariant.copy(alpha = 0.5f)` → `surfaceVariant` (opaque).
- Border: `outline` / `outline.copy(alpha = 0.5f)` → `outlineVariant` for focused and unfocused.

### 6) List rows (My Tickets — TicketRowCard)
- Card background: `surfaceVariant.copy(alpha = 0.6f)` → `surface`.
- Border: added `1.dp` `outlineVariant` with `RoundedCornerShape(12.dp)`.
- “Already in Room” / disabled button: already `surfaceVariant` / `onSurfaceVariant`; no change.

---

## D) Screen-Level Application

### Live Room / Room Settings
- Screen background already `colorScheme.background`.
- AppScreenHeader already `surface` and `onSurface` / `onSurfaceVariant`.
- Room Settings bottom sheet: `containerColor` set to `surfaceContainer` so it matches other sheets.
- Top bar icons (settings, +) now use AppTopBar’s `actionIconContentColor` = `onSurface`.

### Live Play
- Leave Room button already uses `surfaceVariant` / `onSurfaceVariant` (tonal style).
- Room Settings sheet uses `surfaceContainer` as above.

### My Tickets bottom sheet
- Sheet: `containerColor` = `surfaceContainer`.
- Header/drag handle: unchanged.
- Chips and search: updated as in (C).
- Ticket row cards: surface + outlineVariant border as in (C).

### Other screens
- Login/Register/Forgot: use theme and shared components; no structural or color changes in this audit.
- History, Profile, etc.: already use `colorScheme` and shared components; no changes.

---

## E) Before / After Summary

| Component / Area | Before | After |
|------------------|--------|--------|
| **TopBar** | Icons could differ from title color | Back + actions use `onSurface` |
| **BottomBar** | Selected = green icon/text + green pill | Selected = `onSurface` icon/text + `surfaceVariant` pill (white in dark) |
| **Bottom sheet (Room Settings)** | Default sheet container | `surfaceContainer` |
| **Bottom sheet (My Tickets)** | Default sheet container | `surfaceContainer` |
| **My Tickets list row** | Translucent surfaceVariant, no border | `surface` + `outlineVariant` border |
| **My Tickets search** | Translucent container, outline border | `surfaceVariant` container, `outlineVariant` border |
| **Theme** | No surfaceContainer/scrim | `surfaceContainer` and `scrim` in light and dark |

---

## Files Changed

- **theme/Color.kt** — Added `SurfaceContainer`, `Scrim`, `DarkSurfaceContainer`, `DarkScrim`.
- **theme/Theme.kt** — Wired `surfaceContainer` and `scrim` in light and dark schemes.
- **ui/components/AppTopBar.kt** — Set `navigationIconContentColor` and `actionIconContentColor` to `onSurface`; added dark preview.
- **ui/components/AppBottomBar.kt** — Selected state: `surfaceVariant` pill, `onSurface` icon and label; added dark preview.
- **ui/screens/live/LivePlayScreen.kt** — Room Settings sheet `containerColor` = `surfaceContainer`.
- **ui/screens/live/MyTicketsBottomSheet.kt** — Sheet `containerColor` = `surfaceContainer`; search colors; TicketRowCard surface + border; `border` import.

---

## Previews

- **AppTopBar:** Existing light preview; added `AppTopBarDarkPreview` (dark theme).
- **AppBottomBar:** Existing light preview; added `AppBottomBarDarkPreview` (dark theme, Jackpot selected) to confirm selected = white in dark mode.

No navigation or business logic was changed; only theming and component styling.
