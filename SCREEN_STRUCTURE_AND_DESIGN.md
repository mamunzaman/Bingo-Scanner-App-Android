# Screen Structure & Design — Canonical Rules

This document defines the **mandatory** structure and design patterns for every screen. Any new page or screen **must** follow these rules so the app stays consistent (top bar, status bar, no gray strip, no double padding).

---

## 1. MainActivity (once, app-wide)

- **Edge-to-edge:** `WindowCompat.setDecorFitsSystemWindows(window, false)` in `onCreate`.
- **Status & navigation bar color:** Set from Compose theme inside `MamunBingoTheme { }`:
  - `window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()`
  - `window.navigationBarColor = MaterialTheme.colorScheme.surface.toArgb()`
- **Icon appearance:** `WindowInsetsControllerCompat`: `isAppearanceLightStatusBars = !darkTheme`, same for navigation bar.
- Do **not** set `Color.TRANSPARENT` for system bars; use theme surface so the status bar area matches the app.

---

## 2. Top bar — single source of truth

- **One component:** `AppTopBar` in `ui/components/AppTopBar.kt`. Use it on **every** screen that has a top bar (tabs and inner screens).
- **One variant only:** `CenterAlignedTopAppBar`. Do not mix with `SmallTopAppBar`, `TopAppBar`, or custom headers that look different.
- **Status bar inset in one place only:**  
  In `AppTopBar`, use `windowInsets = WindowInsets.statusBars` on the TopAppBar.  
  Do **not** add `Modifier.windowInsetsPadding(WindowInsets.statusBars)` or `statusBarsPadding()` to the **bar container** (that causes a double gap). The bar starts at vertical 0; only the **content** (title, icons) is inset.
- **Colors:** `containerColor = MaterialTheme.colorScheme.surface` (opaque, no transparency).
- **Custom content:** Use `titleContent: (@Composable () -> Unit)?` and `actions: @Composable () -> Unit` for per-screen title/actions. Use `showBack` / `onBackClick` for back-arrow screens.

**Screens with a top bar must:**  
Use `AppTopBar(...)` (or a thin wrapper like `LiveRoomTopBar` that composes `AppTopBar`). Do **not** introduce another top bar component or duplicate status bar padding.

---

## 3. No duplicate insets

- **Do not use** in screen content or in another top bar:
  - `statusBarsPadding()`
  - `systemBarsPadding()`
  - `windowInsetsPadding(WindowInsets.statusBars)` (or safeDrawing, etc.)
  - Manual `padding(top = …)` to “fix” top spacing
- **Content** under the top bar should rely only on:
  - `Scaffold`’s `innerPadding` (e.g. `Modifier.padding(innerPadding)` on the content root), and
  - The fact that the top bar is in `Scaffold.topBar` so `innerPadding` already accounts for it.

---

## 4. Scaffold usage

**Tabs host (e.g. MainTabsScreen):**

- `contentWindowInsets = WindowInsets(0)` so the host does **not** add top insets again.
- Only bottom bar padding is applied to content (for the tab bar).

**Screen-level Scaffold (e.g. LivePlayScreen, detail screens):**

- `topBar = { AppTopBar(...) }` or `{ LiveRoomTopBar(...) }` (which uses `AppTopBar`).
- `contentWindowInsets = WindowInsets(0)` so the only top inset comes from the top bar’s own `windowInsets`.
- Content: single root (e.g. `Box`) with `Modifier.padding(innerPadding)` and nothing else for system/top insets.
- Optional `bottomBar` when the screen shows the main app bottom bar.

**Nested Scaffolds:**  
Avoid double top inset: only the **screen** that owns the top bar should apply top bar + content padding. The tabs Scaffold should **not** add status bar padding to content.

---

## 5. Bottom bar

- Shown via `Scaffold.bottomBar = { AppBottomBar(...) }` where applicable (e.g. LivePlayScreen).
- Content uses `Modifier.padding(innerPadding)` so it stays above the bottom bar; no extra bottom inset in content.

---

## 6. Checklist for a new screen

When adding a **new** page or screen:

1. **Top bar:** Use `AppTopBar` (or a wrapper that uses it). Pass `title` or `titleContent`, optional `showBack`/`onBackClick`, optional `actions`. Do **not** add any status bar padding around it.
2. **Scaffold:** If the screen has its own Scaffold, set `contentWindowInsets = WindowInsets(0)` and put the top bar in `topBar`. Content root uses only `Modifier.padding(innerPadding)`.
3. **No extra insets:** Do not use `statusBarsPadding()`, `systemBarsPadding()`, or `windowInsetsPadding(WindowInsets.*)` in the new screen’s content or around its top bar.
4. **Colors:** Use `MaterialTheme.colorScheme.surface` for the top bar and `MaterialTheme.colorScheme.background` for content where appropriate; do not introduce a different “scrim” or root color that clashes with the status bar.
5. **Consistency:** Same typography and icon sizes as other screens (titleLarge for title, same icon touch targets).

---

## 7. File / component reference

| Purpose              | Location / component        |
|----------------------|-----------------------------|
| App top bar (shared) | `ui/components/AppTopBar.kt` |
| Live room top bar    | `ui/components/LiveRoomTopBar.kt` (uses AppTopBar) |
| Bottom bar           | `ui/components/AppBottomBar.kt` |
| Top bar height       | `Dimens.topBarHeight` (64.dp) — used by TopAppBar sizing where applicable |
| Theme / status bar   | `MainActivity` + `MamunBingoTheme` |

---

Following this structure and design ensures:

- Status bar area and TopAppBar are the same color (no gray strip).
- App top bar starts at the device top (no extra white gap).
- One place handles status bar inset (AppTopBar’s `windowInsets`).
- Every screen has a consistent top bar height and alignment.
