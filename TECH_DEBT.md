# Tech debt

## `runBlocking`

- **None** in the repo (`*.kt` grep for `runBlocking` returns no matches).

## Demo / seed data

- **`BuildConfig.DEMO_MODE`:** `true` debug, `false` release (`app/build.gradle.kts`).
- **`DemoSeeder.seedIfNeeded()`** (via `MainActivity` lifecycle): runs when `DEMO_MODE`.
- **`DemoDataFactory`:** demo room / session / ticket IDs, grids, called numbers.
- **`SettingsRepository.showDemoData`:** user toggle; **`HistoryRepository`** merges demo sessions when enabled.
- **`RoomRepository.seedDemoData`:** debug seed path for rooms / tickets / calls.

## Architectural shortcuts

- **Repositories as `object` singletons** + **`DatabaseProvider`** — no DI framework (Hilt/Koin).
- **Demo + DB:** in-memory demo sessions in `HistoryRepository` combined with DB flows when demo flag is on.
- **Release:** `isMinifyEnabled = false` (ProGuard file present; shrinking not turned on for production yet).
- **Import:** primary import route is **`historyPhotoImport`** + shared **`ImportTicketViewModel`**; `ImportTicketScreen.kt` holds **`ImportTicketMainContent`** — keep `NavGraph` + VM alignment when changing import surfaces.

## UI / code inconsistencies

- **`AppHeaderPageLayout` vs `AppHeaderBackground` alone:** not all screens use the same header wrapper; mix across auth, manual entry, history detail, import, legal, etc.
- **`HistoryPhotoImportScreen`:** some nav / `selectedImageUri` args exist for handoff; VM also owns `selectedImageUri` / `galleryPendingEditUri` (dual source risk — see existing `TECH_DEBT` / comments in `NavGraph`).
- **Strings:** mix of `stringResource` and hardcoded English (dialogs, placeholders).
- **`@Preview`:** sample data in previews only; not app behavior.
- **Docs vs code:** `AGENTS.md` can drift (e.g. flags / routes) — verify against `NavGraph.kt` and source before relying on a single line.
- **Resolved in past work (do not re-add without intent):** **`LiveHeaderStyle` / `UserPreferencesRepository`** removed; history/live list UIs have evolved — compare screenshots to `HistorySheetCard` / `ListSheetRow` if “looks wrong” reports return.

## UI / platform QA (not always “debt” but open)

- Device QA for **bottom sheets + cards view** on Live play (`LivePlayScreen`, `RoomSettingsBottomSheet`, `RoomInfoBottomSheet`, `MyTicketsBottomSheet`) — see `NEXT_TASK.md`.
- `PlaceholderScreen` exists; not all destinations are consistent (if still referenced in nav / profile stack).
