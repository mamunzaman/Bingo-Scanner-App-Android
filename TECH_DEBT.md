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

## Compose BOM alignment

`implementation(enforcedPlatform(compose-bom))` keeps all `androidx.compose.*` artifacts on one version (e.g. foundation/runtime **1.6.1** with BOM `2024.02.00`). Without `enforcedPlatform`, `coil-compose` can pull `ui-tooling` **1.9.0** and cause `HorizontalPager` `NoSuchMethodError` at runtime.

Use **`lifecycle` / `lifecycleCompose` = 2.7.0** + `resolutionStrategy` on `androidx.lifecycle` (CameraX 1.5 can pull 2.10 and win the atomic group). `lifecycle-viewmodel-compose` 2.10 + Compose BOM `2024.02.00` (runtime **1.6.1**) causes `Composer.startReplaceGroup` `NoSuchMethodError`.

## Device install (Android Studio / ADB)

If **Run** fails but `./gradlew :app:assembleDebug` succeeds, check the **Run** or **Build** tool window for `INSTALL_FAILED_*`.

| Symptom | Fix |
|--------|-----|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` / signature mismatch | Uninstall old build: `adb uninstall com.example.mamunbingoapp` then Run again |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | Same uninstall, or use Gradle `installDebug` (project sets `-r` + `-d`) |
| App installs but crashes on launch | Logcat — auth startup: `AuthRepository`, `SupabaseClientProvider` (not an install failure) |

`app/build.gradle.kts` `installation { installOptions -r -d }` helps Gradle/Android Studio replace an existing debug APK.

## UI / platform QA (not always “debt” but open)

- Device QA for **bottom sheets + cards view** on Live play (`LivePlayScreen`, `RoomSettingsBottomSheet`, `RoomInfoBottomSheet`, `MyTicketsBottomSheet`) — see `NEXT_TASK.md`.
- `PlaceholderScreen` exists; not all destinations are consistent (if still referenced in nav / profile stack).
