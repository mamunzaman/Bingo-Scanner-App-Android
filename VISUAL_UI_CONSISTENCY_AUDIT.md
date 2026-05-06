# Visual UI Consistency Audit — Mamun Bingo Android

Date: 2026-05-06  
Scope: static Compose UI audit of theme tokens, shared components, tab screens, scan/manual/import/history/live/profile surfaces, typography, spacing, and color usage.

## Executive summary

The app has a strong foundation: Material 3 theme tokens, `Dimens`, shared `AppTopBar`, `AppBottomBar`, `AppHeaderPageLayout`, `AppPrimaryButton`, bingo-grid components, and several reusable cards. The main inconsistency risk is that multiple screens still create their own shells, top bars, cards, gradients, typography sizes, and spacing instead of routing those choices through the shared system.

Fix priority should be:

1. Standardize page shells/backgrounds across tab and detail screens.
2. Unify top-bar behavior, especially the Scan tab custom green top bar.
3. Make typography use one app font/style system.
4. Replace raw spacing/radius/elevation values with tokens or shared components.
5. Reduce screen-specific card/list/chip styling by extending shared components.
6. Audit dark-mode semantic colors, especially warning/success/static green surfaces.

---

## Priority audit findings

### P0 — Page background and shell are inconsistent across primary tabs

**Evidence**

- `AppHeaderPageLayout` provides a shared surface-filled root plus a decorative top gradient band.
- Home, Live Rooms, and Profile use `AppHeaderPageLayout`, but their surrounding `Scaffold`/container choices differ.
- `MainTabsScreen` uses a transparent scaffold container and delegates different shell behavior to each tab.
- Scan bypasses `AppHeaderPageLayout` completely and paints a slanted `primary` / `surfaceVariant` split background.
- Manual Entry also implements its own header background inside a custom `Scaffold`/`Box` stack.

**Why this looks inconsistent**

Users will see the app jump between:

- soft gradient header pages,
- full-surface pages,
- a bold diagonal green Scan page,
- custom manual-entry header treatment.

This can make the app feel like separate prototypes stitched together rather than one product.

**Cursor prompt**

```text
Audit and standardize Mamun Bingo page shells. Use AppHeaderPageLayout as the default shell for Home, Live Rooms, Profile, History, Ticket Detail, Import Review, and Manual Entry where possible. Keep Scan visually special only if needed, but align its top bar height, safe-area handling, content padding, and bottom transition with AppHeaderPageLayout. Avoid business-logic changes. Replace screen-level containerColor/background choices with MaterialTheme.colorScheme.surface/background tokens and Dimens spacing.
```

---

### P0 — Scan screen top bar and background do not match the shared app header system

**Evidence**

- `ScanScreen` manually uses `TopAppBar` instead of `AppTopBar`.
- Its title/back/icon colors are `onPrimary` because the top area is a solid `primary` slanted panel.
- Other major screens use `AppTopBar` with `onSurface` icon/title colors.

**Why this looks inconsistent**

The Scan tab is a primary tab, but its navigation bar has a different color model and visual weight from Home/Jackpot/Profile. It may be intentional as a hero action screen, but it should still share top-bar spacing, typography, and icon treatment with the rest of the app.

**Cursor prompt**

```text
Refactor ScanScreen visual shell without changing navigation or OCR behavior. Either make ScanScreen use AppTopBar/AppHeaderPageLayout, or create a small AppTopBar variant that supports an onPrimary title/icon color on hero backgrounds. Keep the diagonal scan hero if desired, but ensure top-bar height, status bar insets, back icon size, typography, and bottom content padding match AppTopBar and the other primary tabs.
```

---

### P1 — Typography system is split between default font, LiveFonts, Material defaults, and inline font sizes

**Evidence**

- `Typography.kt` sets `AppFontFamily = FontFamily.Default`.
- `LiveFonts.kt` defines Nunito and DM Mono families from bundled font resources.
- Live play uses `LiveFonts.DMMono` for some number readouts, while History Detail uses `FontFamily.Monospace` directly.
- Several UI files use inline `fontSize = ...sp` or typography slots that are not customized in the app typography scale, such as `headlineSmall`/`displayMedium` defaults.

**Why this looks inconsistent**

The app ships Nunito and DM Mono, but the global text theme uses platform default. Some number-heavy screens use DM Mono, others use platform monospace. This creates subtle differences in text shape, number alignment, and visual brand.

**Cursor prompt**

```text
Unify app typography. Use LiveFonts.Nunito (or rename it AppFonts.Nunito) as the global AppFontFamily in Typography.kt, and use one shared monospace token for bingo numbers, LOS/serial values, QR IDs, and called-number displays. Add missing typography slots that are used in screens (headlineSmall, displayMedium, etc.) so Compose does not fall back to Material defaults. Replace direct FontFamily.Monospace usages with the shared mono token. Do not change text content or layout behavior.
```

---

### P1 — Raw dp/sp values make spacing, radius, and elevation inconsistent

**Evidence**

- `Dimens.kt` defines app spacing, radius, card, icon, button, and input tokens.
- Many screens/components still use raw values like `24.dp`, `32.dp`, `112.dp`, `28.dp`, `36.sp`, `13.sp`, `RoundedCornerShape(12.dp)`, and `RoundedCornerShape(100.dp)`.
- This is especially visible in Profile cards/avatar, dialogs, Scan-specific constants, live cards, and mini bingo widgets.

**Why this looks inconsistent**

Even when values are close, hardcoded spacing/radius creates tiny visual mismatches between cards, list rows, CTA buttons, chips, and bottom sheets. It also makes future redesign harder.

**Cursor prompt**

```text
Run a visual-token cleanup pass. Replace raw dp/sp values in UI screens/components with Dimens and AppTextStyles where a matching token exists. Add only a few new semantic tokens when needed (avatar size, dialog radius, hero amount text, compact chip height). Do not blindly replace canvas/math-specific values. Keep layout visually equivalent but make spacing/radius/elevation come from one source of truth.
```

---

### P1 — Card styles are not fully centralized

**Evidence**

- `AppCard` exists and uses `MaterialTheme.shapes.large`, `surface`, and subtle elevation.
- Many feature cards implement their own `.iosElevatedShadow`, `.background(surface, shape)`, `.border(...)`, and padding stacks.
- Home quick actions, profile sections, room cards, history cards, ticket cards, and import-result cards each have slightly different shadow/border/radius decisions.

**Why this looks inconsistent**

Cards across the app may differ in border visibility, elevation depth, radius, and internal padding. On light mode these differences may look like polish issues; on dark mode they can become contrast issues.

**Cursor prompt**

```text
Create a small card-style system and migrate duplicated card visuals. Extend AppCard or add AppOutlinedCard, AppElevatedCard, and AppSectionCard variants with shared radius, border, elevation, and padding tokens. Then update Home quick actions, Profile sections, Live room cards, History cards, Ticket detail cards, and Import result cards to use these variants where practical. Do not change ViewModels, data models, navigation, or text content.
```

---

### P1 — Header gradients include hardcoded light colors

**Evidence**

- `AppHeaderBackground` uses semantic colors in dark mode, but light mode includes raw `Color(0xFFF6F8F4)` and `Color(0xFFF0F5EC)` values.
- The same component otherwise depends on `MaterialTheme.colorScheme`.

**Why this looks inconsistent**

The gradient is a core app visual. Keeping its light-mode colors outside the theme makes it harder to update brand colors or guarantee contrast consistency with new surface tokens.

**Cursor prompt**

```text
Move AppHeaderBackground light gradient colors into theme Color.kt as semantic tokens (for example HeaderGradientTop, HeaderGradientMid). Wire dark/light equivalents through the theme or a small helper. Keep the rendered gradient visually the same, but remove raw hex colors from AppHeaderBackground.
```

---

### P1 — Dark-mode semantic color risks in alert/status components

**Evidence**

- Theme includes dark surface/background tokens, but status colors like `WarningContainer`, `WarningBorder`, `WarningIcon`, `WarningText`, `WarningSubText`, `Success`, and `Info` are single light-oriented constants.
- Import and almost-bingo components use these warning constants directly.

**Why this looks inconsistent**

Light warning yellows/browns can look too bright or low-contrast in dark mode. Some cards may appear pasted on top of the dark surface instead of belonging to the theme.

**Cursor prompt**

```text
Add semantic light/dark status color tokens for warning, success, info, and alert containers. Replace direct use of WarningContainer/WarningText/Success/Info in UI components with MaterialTheme-aware helper colors or ColorScheme extension values. Verify AlmostBingo, ImportTicketScanResultContent, History status cards, and Live play alerts in dark mode.
```

---

### P2 — Top-level tab spacing differs by screen

**Evidence**

- Home content starts with `top = Dimens.spacing8`.
- Live Rooms content starts with `top = Dimens.spacing24`.
- Profile header uses raw `top = 24.dp, bottom = 32.dp`.
- Scan top/bottom sections use their own spacing constants and weighted split layout.

**Why this looks inconsistent**

When switching bottom tabs, the first visible content block moves vertically by different amounts. The user notices this as a “jump” in header density.

**Cursor prompt**

```text
Normalize primary tab vertical rhythm. Define shared tokens for top-of-content padding after AppTopBar, section spacing, and bottom-bar breathing room. Apply them to HomeScreen, LiveRoomsScreen, ProfileScreen, and ScanScreen so switching tabs keeps similar visual density. Preserve screen-specific hero content, but align the first content baseline and section gaps.
```

---

### P2 — Text fields/search/filter controls have multiple visual implementations

**Evidence**

- `AppTextField`, `SearchFilterBar`, `SortDropdown`, and bottom-sheet search fields each define their own `OutlinedTextFieldDefaults.colors` and shapes.
- Some search/list controls use `surfaceVariant`, others use transparent/outlined styling.

**Why this looks inconsistent**

Search/filter controls can look different across History, My Tickets, profile forms, auth, and room lists, even though users perceive them as the same input category.

**Cursor prompt**

```text
Create one shared AppSearchField/AppFilterField style using Dimens.radiusSearchField, surfaceVariant container, outlineVariant border, and onSurface/onSurfaceVariant text/icon colors. Migrate History search/filter, My Tickets search, SortDropdown display field, and profile/auth text inputs where appropriate. Keep keyboard behavior and state handling unchanged.
```

---

### P2 — Bottom sheets and dialogs need one surface/elevation/radius standard

**Evidence**

- Some bottom sheets now use `surfaceContainer`, but dialogs and sheet-like panels still hand-roll radius, shadow, padding, and container color.
- `AppConfirmDialog` uses raw 28.dp radius/shadow and custom buttons rather than shared button/card tokens.

**Why this looks inconsistent**

Sheets/dialogs are high-attention UI. Inconsistent radius/elevation/button spacing is easy to notice and reduces trust.

**Cursor prompt**

```text
Standardize modal surfaces. Create AppDialogSurface/AppBottomSheetSurface tokens for containerColor, radius, elevation, drag handle, and content padding. Update AppConfirmDialog, room settings sheet, create-room dialog, My Tickets sheet, QR dialog, and manual-entry save dialogs to use these tokens. Keep copy, validation, and callbacks unchanged.
```

---

## Suggested Cursor work order

1. **Safe theme/token pass**: typography font family, gradient tokens, status color tokens, missing typography slots.
2. **Shell pass**: align AppHeaderPageLayout usage and tab content padding.
3. **Top bar pass**: refactor Scan top bar or create an `AppHeroTopBar` variant.
4. **Card pass**: introduce AppCard variants and migrate obvious duplicated card stacks.
5. **Form/filter pass**: unify search/text-field visuals.
6. **Dark-mode QA pass**: manually review Home, Scan, Jackpot/Live Rooms, Profile, Manual Entry, Import Review, History Detail, and Live Play.

---

## Manual visual QA checklist

Use this after Cursor applies fixes:

- [ ] Bottom tab switching does not create visible vertical jump between Home, Scan, Jackpot, and Profile.
- [ ] Every top bar has the same height, status-bar handling, typography weight, and icon touch target.
- [ ] Cards use one of the approved variants: flat surface card, outlined card, or elevated hero card.
- [ ] Primary buttons are the same height/radius across auth, import, live, manual entry, and profile flows.
- [ ] Search/filter fields look the same in History, My Tickets, and room lists.
- [ ] Warning/success/info banners are readable and not too bright in dark mode.
- [ ] Bingo numbers and serial/LOS/QR identifiers use the same monospace style.
- [ ] No new raw `Color(0x...)` values are added outside theme/token files.
- [ ] Raw `dp`/`sp` values are either replaced by tokens or intentionally documented as component-specific constants.

---

## Fast repo commands for future audits

```bash
# Find raw colors outside theme files
rg -n "Color\(0x|#[0-9A-Fa-f]{6}" app/src/main/java/com/example/mamunbingoapp -g '*.kt'

# Find alpha-heavy surface/status colors that may need dark-mode review
rg -n "copy\(alpha|Warning|Success|Info|surfaceVariant" app/src/main/java/com/example/mamunbingoapp/ui -g '*.kt'

# Find raw dp/sp in UI files
rg -n "\b[0-9]+\.dp|\b[0-9]+\.sp" app/src/main/java/com/example/mamunbingoapp/ui -g '*.kt'

# Find custom scaffold/top-bar implementations
rg -n "Scaffold\(|TopAppBar\(|AppTopBar\(|AppHeaderPageLayout" app/src/main/java/com/example/mamunbingoapp/ui -g '*.kt'
```
