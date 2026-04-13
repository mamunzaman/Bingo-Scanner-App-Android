# Tech debt

## `runBlocking`

- **None** in `app/src/main` Kotlin sources (`rg runBlocking` on `*.kt`).

## Demo / seed data

- **`BuildConfig.DEMO_MODE`:** `true` debug, `false` release (`app/build.gradle.kts`).
- **`DemoSeeder.seedIfNeeded()`** (`MainActivity` `lifecycleScope` + `Dispatchers.IO`): runs when `DEMO_MODE`, seeds once.
- **`DemoDataFactory`:** demo room/session/ticket IDs, grids, called numbers, history sessions.
- **`SettingsRepository.showDemoData`:** user toggle; **`HistoryRepository`** merges demo sessions when on.
- **`RoomRepository.seedDemoData`:** demo rooms/tickets/called numbers (debug seed path).

## Architectural shortcuts

- **Repositories as `object` singletons** with **`DatabaseProvider`** — no DI (Hilt/Koin).
- **Demo layering:** in-memory demo sessions in `HistoryRepository` combined with DB-backed flows when demo flag is on.
- **Release:** `isMinifyEnabled = false` (ProGuard files present; shrink not enabled).
- **Import entry:** primary user-facing import route is **`historyPhotoImport`** + shared **`ImportTicketViewModel`**; `ImportTicketScreen.kt` hosts **`ImportTicketMainContent`** — avoid duplicating scan UI on a separate route without updating `NavGraph` / `AGENTS.md`.

## UI / code inconsistencies

- **`AppHeaderPageLayout` vs raw `AppHeaderBackground`:** `AppHeaderPageLayout` used on Home, History list, Profile, Settings, MyAccount, Login, Live rooms, Live sheet detail; many other screens still compose **`AppHeaderBackground`** only (e.g. Register, Forgot, Manual entry, History detail, History photo import, legal, profile sub-pages, ticket detail) — pattern not unified.
- **`PlaceholderScreen.kt`:** generic placeholder; not all profile-style destinations use it consistently.
- **`HistoryPhotoImportScreen`:** `selectedImageUri` (and related nav params) partially **unused / suppressed** — VM (`selectedImageUri`, `galleryPendingEditUri`) owns display state; nav keeps args for compatibility.
- **`NavGraph` → `HistoryPhotoImportScreen`:** some boolean args passed as constants / suppressed; live analyzing state comes from **`ImportTicketViewModel`** inside the screen.
- **Strings:** mix of `stringResource` and inline English (dialogs, search placeholders, some labels).
- **`@Preview` composables:** hardcoded sample data (e.g. live play, bottom sheets) — dev-only.
- **Docs drift:** `AGENTS.md` mentions **`IMPORT_TICKET_UI_RENDER_FAKE_ONLY`** in `ImportTicketScreen.kt`; **no matching flag** in current `ImportTicketScreen.kt` — treat AGENTS as stale until reintroduced or doc fixed.
- **Resolved (dead code):** **`LiveHeaderStyle`** + **`UserPreferencesRepository`** (`user_prefs` / `live_header_style`) removed.
