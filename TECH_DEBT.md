# TECH_DEBT

- **Remaining `runBlocking`**
  - None in `app/src` (no matches).

- **Synchronous ML Kit / threading**
  - `ImportTicketImageOcr`: `Tasks.await(recognizer.process(...))` on caller thread (IO in `ImportTicketViewModel`); many sequential calls (full frame + up to **25 cell crops** + strip zones + retries) — **latency + main-thread risk** if ever called off `Dispatchers.IO`.

- **Demo data usage**
  - `BuildConfig.DEMO_MODE`: true debug, false release (`app/build.gradle.kts`).
  - `MainActivity`: `DemoSeeder.seedIfNeeded()` on `lifecycleScope` + `Dispatchers.IO`.
  - `HistoryRepository`: in-memory demo sessions + merge with DB when `SettingsRepository.showDemoDataFlow` enabled.

- **Architectural shortcuts**
  - `NavGraph.kt`: large file (auth, main tabs, GMS document scanner, `historyPhotoImport`, manual entry, history, live, settings).
  - Repositories as `object` singletons (`HistoryRepository`, `RoomRepository`, etc.) mixing Room, flows, demo state.
  - `ImportTicketScreen.kt`: composable + previews; **not** registered as a route in `NavGraph.kt` (scan/import flow uses `HistoryPhotoImportScreen` + `ImportTicketViewModel`).

- **UI inconsistencies**
  - Multiple empty-state patterns (`EmptyHistoryState`, `EmptyHistoryActionCards`, `EmptyStateBlock`, …).
  - `HistoryPhotoImportScreen`: success copy can contradict “ML Kit available” behavior (see `PROJECT_SNAPSHOT` known issues).
  - Profile/legal screens lighter than core live/history flows.
