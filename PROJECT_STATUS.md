# Project status

**Last update:** 2026-05-28 - **Projects local cache:** DataStore cache of last successful API response; cache-first load + background refresh; soft error banner when offline with cache; "Last updated" label; pull-to-refresh preserved. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Projects screen polish:** Header title + subtitle; 130dp images; **BINGO! Project** chip; compact location/date chips; Read More row; wider card spacing (`AppSectionSurface`, `AppInsetDivider`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Projects tab:** Main bottom nav **Projects** tab; remote fetch; loading/error/empty; tap opens `source_url`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home Active Ticket navigation:** Active ticket card tap → `historyDetail/{sessionId}` (same as History list); blank id no-op; unknown id falls back to `ticket/{id}`. View All unchanged → History list. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home trim + FAB ripple:** Removed Upcoming strip and Eco News from Home; staggered dual-ring FAB pulse burst. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Quick Actions + FAB polish.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home polish:** Time-aware greeting + “Ready for the next draw?”; Draw Status strip (Upcoming + countdown); Active Tickets summary from local tickets (match/marked progress, almost bingo via `BingoWinChecker`); real ticket preview row. No player-online stats. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home layout trim:** Removed Active Tickets preview section (header, View All, horizontal ticket row); Quick Actions → Green Impact flow unchanged otherwise. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home CurrentJackpotCard:** Premium green gradient hero (`CurrentJackpotCard`) with remote EUR jackpot, Sunday Berlin countdown, latest-number chips, Scan Ticket; removed duplicate `HomeNextDrawCard`; `HomeViewModel` → `viewmodel/`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Home Next Draw card:** Replaced debug draw card with `HomeNextDrawCard` — Sunday 17:00 Europe/Berlin live countdown, remote EUR jackpot, compact latest-numbers row; loading/error in-card. Active Tickets unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Avatar delete UI cache fix:** Immediate local `avatarUrl` clear + Coil eviction; `normalizeAvatarUrl` (null/blank/`"null"`); upload-only cache buster; `ProfileAvatar` `key()` forces initials after delete. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Profile avatar delete:** `ProfileRepository.deleteAvatar()` removes `avatars/{userId}.jpg` and clears `profiles.avatar_url`; `ProfileViewModel.deleteAvatar()`; `ProfileAvatar` edit badge when empty, delete badge when set; confirm dialog on Profile + My Account; `AppAuthMessage` success/error. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Profile avatar upload (Supabase Storage):** `storage-kt` + `profile-avatars` bucket upload (`avatars/{userId}.jpg`), URL saved to `profiles.avatar_url`; gallery picker on Profile + My Account; shared `ProfileAvatar` with Coil; `AppAuthMessage` feedback; bucket policy TODO in `ProfileAvatarStorage`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-28 - **Password policy + strength meter:** Added reusable `PasswordStrengthMeter` (Weak/Medium/Strong) for new-password fields; min password length standardized to 8 in Register, Profile Change Password, and recovery update validation. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Logout → login navigation:** `signOut()` sets `SignedOut` immediately, clears recovery flags, blocks stale `SignedIn` during sign-out; `navigateToLoginClearingBackStack()` pops full graph. Profile/Settings unchanged (await `performLogout`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Login → main navigation:** After `signInWithEmail`, `syncAuthStateFromCurrentSession()` sets `SignedIn` immediately; `navigateToMainFromAuth` clears auth back stack to `main`. Session observer restore unchanged.

**Previous:** 2026-05-27 - **Auth inline messages UI:** `AppAuthMessage` (Error/Success/Info) on Login, Register, ForgotPassword; theme tokens (`SecondaryContainer`, `IconContainerBg`, `PrimaryContainer`). Auth logic unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Auth signup rate-limit UX:** `AuthRepository.mapAuthError` maps `over_email_send_rate_limit` to a friendly message; strips raw Supabase URLs/headers/tokens from UI (`Log.w` keeps full errors). Register screen unchanged (uses `authActionError`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Fresh-install auth startup crash fix:** `AuthRepository.startup()` after app init (no eager object init); session observe on `Dispatchers.Default` + timeout → `SignedOut`; `getClientOrNull()` + try/catch; guarded deep links; `SupabaseClientProvider.ensureInitialized` + `lifecycle-process`/`startup-runtime`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Password reset step 2 complete:** `ForgotPasswordScreen` set-new-password UI; NavGraph success routing.

**Previous:** 2026-05-27 - **Supabase email verification deep link:** `mamunbingo://auth/callback` intent-filter; Auth `scheme`/`host`; `SupabaseAuthDeepLink` + `AuthRepository.handleAuthDeepLink`; `MainActivity` dispatches before ticket import; sign-up uses redirect URL; dashboard Site URL + Redirect URLs documented. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Supabase app-side auth foundation complete:** Config URL/key validation; network + credential error mapping; session restore on cold start; nav loop guards; logout clears stack; TODOs for email verify / reset password / profiles / access status; `SupabaseAuthPlan` dashboard checklist. No DB tables; Room unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Supabase auth gate stabilization:** Split session state (`authState`) from transient `authActionError` so wrong-password/missing-keys errors persist and retry works; `authActionInProgress` disables login/register buttons; splash waits on session `Loading` before routing (signed-in restart → main); protected routes redirect on `SignedOut` only (prevents login/register redirect loops); sign-out no longer forces global Loading. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Supabase auth wired to UI + NavGraph gate:** `LoginViewModel` / `RegisterViewModel` call `AuthRepository`; existing login/register screens show loading + errors; splash/onboarding route by `AuthState`; signed-out blocked from app routes; Profile/Settings logout calls `signOut()`. Room unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **AuthRepository (session layer):** `AuthState` sealed types; `AuthRepository` exposes `authState` StateFlow from Supabase `sessionStatus`, plus `signInWithEmail` / `signUpWithEmail` / `signOut` with readable errors; uses `SupabaseClientProvider.requireConfigured()`. No NavGraph or login UI wiring; Room unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Supabase client provider (Auth-only):** `BuildConfig.SUPABASE_URL` / `SUPABASE_ANON_KEY` from gitignored `local.properties`; `SupabaseClientProvider` lazy singleton with Auth plugin only (Ktor OkHttp on classpath); `requireConfigured()` + clear error when keys missing. No login UI or Room changes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Supabase Auth plan (identity only, Bingo data local):** Architecture decision: Supabase stores user account/session/profile/access only; all Bingo tickets, sessions, history, and called numbers remain in on-device Room. Added Auth-only Gradle deps (`supabase-bom`, `auth-kt`, `ktor-client-okhttp`); `data/auth/SupabaseAuthPlan.kt` documents boundary, phases, and future NavGraph access gate. No Auth UI/client wiring yet. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Real OCR progress in analyzing screen:** `DetectionStatus` + `ImportOcrProgressUiState` in ViewModel; `_ocrProgress` StateFlow emits at 4 stages: "Checking for QR code…" → "Detecting ticket grid…" → "Reading bingo numbers…" → "Finalizing result…" (with real cell count + LOS/serial Found/NotFound). `BingoOcrStatusCard` shows live stage label, real GRID CELLS count, LOS and SERIAL detection status with green/muted colouring. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Analyzing screen — ticket photo bg + bingo-specific card:** `ImportTicketAnalyzingFullScreen` shows ticket photo at 0.20 alpha (surface bg shows through), `BingoOcrStatusCard` with rotating titles ("Reading bingo numbers…" / "Detecting ticket grid…" / "Checking serial and LOS…"), bingo body text, shimmer-gradient progress bar, honest GRID CELLS / OCR STATUS / META DATA stats. Crosshair updated to medium green (`#2E9B5E`), scan line softened to 0.68 alpha. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Analyzing full-screen:** When `isAnalyzingUi`, `HistoryPhotoImportScreen` content area replaced by `ImportTicketAnalyzingFullScreen` — dark green gradient + scan-line/crosshair/rect animation + bottom "Processing Data..." card. Thumbnail, Take Photo, and Gallery completely hidden during loading. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Analyzing overlay redesign (code.html match):** Replaced radar with vertical scan-line sweep + floating crosshair (4 brackets + dashed ring + cross) + soft green scanning rectangle. New bottom "Processing Data..." card (white surface, ping dot, animated progress bar 35→96%, NODES/CONFIDENCE stats). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Analyzing overlay polish:** Larger radar with pulse ring + stronger sweep; gradient vertical scrim; glass text card with border + 3-dot progress; radar avoids center text band. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **Scanner log cleanup:** Removed temporary `scan-entry-handoff` / OCR route debug spam; dead raw-stage string builders; kept per-type OCR routing and VM/gallery failure warnings only. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **MAIN_SHEET gallery routing:** Scan type captured when cropped gallery image arrives; Apply passes resolved type into `analyzeTicketFromUri`; `MAIN_SHEET` always calls `MainSheetBingoOcr` with internal grid crop bypass. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-27 - **MAIN_SHEET routing guard fix:** too-zoomed pre-OCR guard now runs only for `PLAY_PAPER`; `MAIN_SHEET` and `ONLINE` skip guard so close-up formats reach their own OCR paths. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **OCR isolation:** per-type OCR files + VM `when` branches. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **MAIN_SHEET rollback:** Experimental camera/OCR removed; stable `ImportTicketImageOcr` path. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **MAIN_SHEET OCR (reverted):** Receipt close-up experiment in `ImportTicketImageOcr`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Settings persistence:** notification/security/privacy toggles in DataStore. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Profile header sync:** Profile shows saved account name/email/initials. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **My Account persistence:** DataStore profile save/load. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **My Account form:** `AccountFormScreen` auth/profile styling. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Online OCR path:** `OnlineBingoOcr` for `BingoScanType.ONLINE` only; main-sheet `ImportTicketImageOcr` unchanged; scan type passed to `analyzeTicketFromUri`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Scan type selection:** premium sheet on Scan tab before camera; `bingoLiveCameraImport?scanType=`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **QR sheet name:** encode/decode `sheetName`; manual entry prefill + "Bingo name" rename UI from QR. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Live play called numbers** (bottom sheet only). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Circular board chip polish.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Called numbers sheet spacing.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Row-grid dot matrix board.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **Circular chip board premium polish.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **TV board 5-cap + right overflow.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-24 - **TV board single-stack polish.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board 5-column layout:** equal-weight lanes, adaptive scale. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **Manual entry sheet rename crash fix:** VM draft/commit rename. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board scoreboard polish.** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board column clarity:** separators + centered lanes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board clean surface:** single app-green board, no inset panels. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board premium polish:** (reverted heavy panels). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board tune:** app primary-green; 5 main / overflow right. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-20 - **TV board readability:** text-only numbers; sheet recent chips + board only. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **TV board + sheet premium polish:** scoreboard surface, tiered numbers, cleaner tracks; sheet smaller hero, compact recent chips, board focus. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **Shared TvBingoBoard** in detail sheet + history panel. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **CalledHistoryPanel TV board:** latest/recent row + solid-green track board. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **Live list bottom padding fix:** list `spacing8` bottom inset; scaffold handles dock. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **Live keypad dock compact:** digit keys 40dp; tighter gaps; metrics synced. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **Live Play list density:** ticket list inter-card gap `spacing4`; sticky-to-first card gap unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-18 - **Live Play list bottom padding:** `LivePlayCallKeypadMetrics` drives animated list/empty scroll bottom inset from keypad collapsed vs expanded (`showNumberKeypad`, 200ms). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-13 - **Live compact top bar polish:** `LiveLastCalledPremiumChip` (muted LAST + primary number), circular green-tint history toggle, tighter vertical padding / 44dp row, hairline divider before expanded history. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-13 - **Live Play keypad + history toggles.**

**Previous:** 2026-05-13 - **Live called-numbers detail sheet:** New `CalledNumbersDetailSheet` (modal bottom sheet) shows full B/I/N/G/O grid with tiered styling for latest vs recent vs older calls; `LivePlayScreen` opens it via `appClickable` on `LiveRoomWithHistoryCard` (both list and card layouts). No calling/navigation logic changes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **History Detail called-status balance tweak:** waiting/called-status block now uses a compact 44dp circular status tile (`primary @ 0.12`) with centered primary icon (`PlayArrow`) beside the existing status copy, replacing the oversized visual treatment. Wrapper (`AppSectionSurface`), spacing, and behavior unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **History Detail hierarchy fine-tuning:** ticket info section got subtle inner vertical breathing room, softer section title tint (`Outline @ 0.9`), and explicit inset `AppInsetDivider` row separators (`outlineVariant @ 0.24`). Active status row is now wrapped in `AppSectionSurface` and live pill height slightly reduced. Grid area now sits inside `AppSectionSurface`; pre-grid win spacer reduced (`8dp -> 4dp`). No logic/grid-internal/action/navigation changes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **History Detail premium section alignment:** `HistoryDetailScreen` now uses `AppSectionSurface` for the Ticket Information block and the ?Waiting for live data?? panel (replacing ad-hoc outer shell on waiting panel). Section gap normalized to `Dimens.spacing12`. Bingo grid scaling, actions (live/QR/delete), and navigation/content flow unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **History list vertical rhythm tuning:** `HistoryListScreen` `LazyColumn` card spacing increased from `Dimens.spacing12` to `Dimens.spacing16` (+4dp) to improve item separation. Card visuals, borders, content, actions, and horizontal padding unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **History list card-shell alignment:** `HistorySheetCard` outer wrapper migrated to `AppSectionSurface` (same shape, dynamic selection/default border color, flat elevation). Internal history card layout, mini grid, status pills, footer cells, divider positions/colors, and actions (view/join/leave/delete/select) remain unchanged. `HistoryListScreen` behavior/flow untouched. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **Home premium card-shell alignment:** `HomeScreen` migrated ad-hoc card containers to shared UI shells while preserving behavior/layout: `QuickActionButton` uses `AppCard` (circular, clickable), `GreenImpactCard` uses `AppSectionSurface` (`GreenImpactBg` preserved), `EcoNewsItem` uses `AppSectionSurface` (replacing custom shadow/background/border stack). No navigation/content-structure changes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **Divider clarity tuning (Settings grouped rows):** `AppInsetDivider` default alpha is now explicit `0.28f` (`outlineVariant`), preserving existing inset/thickness defaults. `SettingsScreen` grouped-section row dividers increased from `0.20f` to `0.30f` for clearer in-card separation; no layout/row-height/border changes. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **Premium micro-tuning pass (Profile + Settings):** `AppSectionSurface` border alpha reduced to `0.14f` for subtler separation. `SettingsScreen`: section titles now consistent (`bottom = 6.dp`) with softer `primary @ 0.9f`; grouped rows vertical padding `spacing10 -> spacing8` for tighter structure. `ProfileScreen`: menu inset dividers softened to `outlineVariant @ 0.10f`; invite inner `surfaceVariant` blocks reduced to `0.75f` alpha. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **Flat premium section cards (Profile + shared surface):** `AppSectionSurface` default `shadowElevation` ? `0.dp` (primary border alpha 0.18f unchanged). `ProfileScreen`: dropped card `iosElevatedShadow` on stats row, invite shell, menu rows; avatar shadow 8.dp?2.dp; inset dividers (`outlineVariant` @ 0.12f) between menu rows. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-05 - **Icon-tile migration (targeted):** `HistoryListScreen` add-from-photo top-bar action now uses `AppIconTile` with explicit circular visual params (40dp tile, 24dp icon, primary/onPrimary). Other requested files (`HistoryDetailScreen`, `RoomInfoBottomSheet`, `MyTicketsBottomSheet`) had no local Box+background icon-tile pattern to replace. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **`AppSectionTitle` rollout:** Room Info "Session"; LivePlay sheet preview meta labels + room settings subsection titles; My Tickets `TicketInfoCell` labels; History detail "TICKET INFORMATION". `HistoryListScreen` unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **`AppInsetDivider` + sites:** `endInset`/`thickness` params; `HistorySheetCard` (components) footer hairline, `LivePlayScreen` dividers, `RoomInfoBottomSheet`, `SearchFilterSortHeader`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Bottom sheets ? `AppBottomSheetSurface`:** History list/detail room pickers, LivePlay sheet preview + room settings, Manual Entry room picker, My Tickets sheet, Room Info sheet now use shared wrapper + `rememberAppBottomSheetState`; prior `windowInsets` / `shape` / `containerColor` / state preserved. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Profile shared primitives:** `ProfileMenuItem` leading `AppIconTile` (was `AppIconContainer`); stats / invite outer / menu rows align shadow, clip, and `background` shape with `profileSectionCardShape` + existing `appPremiumCardBorder`; dead imports removed. QR inner placeholder + code row tiles unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Settings shared UI kit:** `SettingsScreen` uses `AppIconTile` (42dp / 22dp icon / `primaryContainer` 0.5f), `AppInsetDivider` (same start inset + 0.20f alpha as before), `AppSectionTitle` (primary, `uppercase=false`). Removed local `SettingsIconTile` / `SettingsInsetDivider`. Row composables unchanged for layout parity vs `AppListRow`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Shared UI kit (components only, no screen migration):** Added `AppIconTile`, `AppListRow`/`AppListRowDensity`, `AppInsetDivider`, `AppSectionTitle`, `AppBottomSheetSurface` + `rememberAppBottomSheetState`; extended `AppCard` (`shape`, `contentPadding`, proper `Card(onClick)`); `AppSectionSurface` KDoc cross-links kit; `AGENTS.md` shared list. Profile/Settings untouched. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **AppSectionSurface premium defaults + all Settings shells:** Defaults `surface`, primary border alpha 0.18f, `shadowElevation = cardElevationSubtle`; `appPremiumCardBorder` replaces `appSectionBorder`. `SettingsSection` (DATA, PRIVACY, ABOUT, Developer) wraps content in `AppSectionSurface`. Profile uses `appPremiumCardBorder`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Shared `AppSectionSurface` + primary border:** New `ui/components/AppSectionSurface.kt` ? default border `primary.copy(alpha=0.28f)`, `APP_SECTION_BORDER_ALPHA`; `appSectionBorder(shape)` for Profile Row/Column shells. `SettingsGroupedSection` uses `AppSectionSurface` instead of raw `Surface`/`outlineVariant`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Profile outer cards border-only:** `ProfileScreen` adds `profileSectionCardBorder()` after `surface` on stats row, invite card shell, each settings menu row; keeps `iosElevatedShadow` and per-card layout; inner invite tiles unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-04 - **Settings grouped cards refinement:** `settingsIconTileSize` 48?42dp; tile radius 14?12dp; icon 24?22dp; `primaryContainer` alpha 0.50f; card fill `surface`?`surfaceContainerLowest`, border alpha 0.25f, tonal elevation 0; divider alpha 0.20f; row min height 84?72dp, vertical pad spacing12?spacing10; section label `start+4dp, bottom+6dp`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **Settings premium grouped cards:** `SettingsScreen` (`profile/`) ? LIVE PLAY, APPEARANCE, NOTIFICATIONS, SECURITY use `SettingsGroupedSection` (`Surface`, `Dimens.radiusLarge`, `BorderStroke` `outlineVariant` @ 0.35f, subtle tonal elevation). Rows with `groupedInCard`: `heightIn(min = 84.dp)`, inner `spacing16` padding, `SettingsIconTile` 48dp / `radiusSearchField` / `primaryContainer` @ 0.45f; `HorizontalDivider` inset from icon+gap. DATA / PRIVACY / ABOUT / Developer keep prior `SettingsSection`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **Ticket QR dialog copy:** `TicketQrDialog` shows muted `bodySmall` centered hint under the QR image (success state only): scan with another phone?s camera, tap link to open Mamun Bingo. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **HTTPS App Link QR (`bingoapp.itconsultingfirma.com`):** `encodeDeepLink` outputs `https://bingoapp.itconsultingfirma.com/import-ticket?data=?`. `QrTicketCodec` adds `APP_LINK_HOST`, `isImportTicketDeepLinkUri` (https path `/import-ticket` + `data` query, plus legacy mamunbingo / intent). `decode` / `isLikelyBingoTicketQrString` unchanged legacy support. `AndroidManifest`: `intent-filter` `autoVerify` for https host + `pathPrefix="/import-ticket"`. `ImportTicketDeepLinkViewModel` gates pending URI with `isImportTicketDeepLinkUri`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **QR encode rollback (stable `mamunbingo://`):** superseded by HTTPS App Link above.

**Previous:** 2026-05-03 - **Camera QR direct app open (intent:// format):** reverted ? see above.

**Previous:** 2026-05-03 - **QR decode cell-position fix:** `buildManualEntryForRoomRoute` now accepts `prefillAsRowMajor: Boolean = false` and encodes `&prefillOrder=rowMajor` when true; `manualEntryForRoom` route adds `prefillOrder` nav argument; `parseManualEntryForRoomFromNav` reads it and sets `prefillAsRowMajor` on `ManualEntryForRoomFromNavArgs`; `ManualEntryScreen` in the ForRoom composable receives `prefillAsRowMajor = mer.prefillAsRowMajor`; live-camera QR callback and history photo import both pass `prefillAsRowMajor = true`. Added `Log.d("QR_DEBUG", ?)` in `decodeBingoFromBarcodes`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **History in-room clarity UX:** `HistorySheetCard` pill text is now `"In: <roomName>"` (fallback `"In room"`); in `selectionMode && inRoom` adds muted `labelSmall` hint "Already added ? remove first to move rooms" below subtitle. `BulkSelectionActionBar` gained `inRoomInfoText: String?` shown above action row; Add label renamed "Add eligible (N)". `HistoryListScreen` derives `allSelectedInRoom`; passes info text when all-in-room; passes `roomName = item.roomName` to each card. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **History bulk "Add to room" wired end-to-end:** `HistoryViewModel.addSessionsToRoom(roomId, sessionIds)` looks up `ticketId` per session, skips already-assigned via `RoomRepository.findAssignedRoomId`, calls `assignTicketToRoom` for eligible; state refreshes via DB flow. `NavGraph` obtains back-stack-scoped VM with `viewModel(backStackEntry)` and wires `onBulkAddToRoom`. `HistoryListScreen` guards picker open with `isNotEmpty()` check; button shows eligible count. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-03 - **History bulk "Add to room" room picker:** `HistoryViewModel` exposes `liveRooms: StateFlow<List<LiveRoom>>`. `HistoryListScreen` shows `AddToRoomPickerSheet` (`ModalBottomSheet`) on "Add to room" tap; sheet lists active rooms (empty state + "Create live room" CTA when none); tapping a room fires `onBulkAddToRoom(roomId, pendingIds)` with only non-room session IDs and exits selection. `onBulkAddToRoom` sig changed to `(roomId, sessionIds)`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **History "In room" pill + bulk Add to room:** `HistorySheetCard` shows `primaryContainer @ 0.45` pill "In room" (primary text) inline with title when `inRoom = true`. `HistoryListScreen` derives `selectedSessionsNotInRoom`; `showAddToRoom` wired to bulk bar with `addToRoomEnabled` + `addCount`; `showJoinLive` shown only when in-room selection is non-empty; `onBulkAddToRoom(ids)` fires only non-room session IDs; `inRoom` passed per card. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **History card alignment fix:** `HistorySheetCard` 42.dp leading slot now gated behind `if (selectionMode)`, so normal-mode cards keep their original x-position (no leading gap) and only selection mode adds the slot + centered checkbox. Mini-grid -> title `spacedBy(spacing10)` and bulk action bar polish unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **History card alignment + bulk bar polish:** `HistorySheetCard` reserves a fixed `42.dp` leading slot (`Box`) for the checkbox in both selection and normal modes, so mini-grid + title sit at the same x-position regardless of mode; mini-grid -> title gap `spacedBy(spacing12)` -> `spacedBy(spacing10)`; footer already 3x `weight(1f)` with centered cells. Shared `BulkSelectionActionBar` now uses `padding(start=16,end=16,top=12,bottom=16)` + `verticalArrangement = spacedBy(spacing10)` (Spacers removed); Remove + Delete get `disabledContainerColor = surfaceContainerHighest` / `disabledContentColor = onSurfaceVariant @ 0.55`; Delete keeps `error`/`onError` only when enabled. Behavior unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **History selected card softer style:** `HistorySheetCard` selected border 2dp -> 1dp, color `primary @ 0.65` -> `primary @ 0.55`; new `selectedTintBg` (only when `selectionMode && selected`) overlays `primaryContainer @ 0.10` on header (`surface`) and footer (`surfaceContainerLow`) rows for a subtle highlight. Selection behavior + checkbox color unchanged. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **History list spacing + search distinction:** in `HistoryListScreen` the sticky search overlay band background changed from `surface @ 0.96` to `surfaceContainerHigh` (clearer contrast vs `surface` list area); `historyHeaderHeight` 136dp ? 148dp adds a 12dp `surface` gap between band and first card; LazyColumn bottom contentPadding `Dimens.spacing8` ? `Dimens.spacing24` for end breathing room; item rhythm kept at `Dimens.spacing12`. Shared `SearchFilterSortHeader` not modified. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-05-02 - **Manual Entry sheet title single-tap edit:** added safe `FocusRequester` inside `ManualEntryBingoCard` (in `BingoCardGrid.kt`) and a `LaunchedEffect(isEditingSheetName)` that calls `requestFocus()` (via `runCatching`) only when entering edit mode. Title `BasicTextField` now uses `.focusRequester(titleFocusRequester)`. No changes to `ManualEntryScreen` flush/blur logic. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-28 - **Live camera final premium scanner polish (style-only):** `BingoLiveCameraImportScreen` now includes 4dp glowing corner brackets, animated gradient scan line (~2.5s loop), stronger focus contrast (brighter inside frame/darker outside), refined helper text shadow/placement, tighter bottom control spacing, and gradient CTA with subtle pressed scale. Camera logic/navigation/layout flow unchanged. `./gradlew :app:assembleDebug` successful.

**Previous:** 2026-04-28 - **16KB support follow-up complete:** removed GMS Document Scanner beta dependency; **Take photo** stays on existing non-GMS CameraX/import path; bumped ML Kit text recognition `16.0.0` -> `16.0.1`; barcode scanning + offline OCR kept. `./gradlew :app:clean :app:assembleDebug` and `./gradlew :app:assembleDebug` successful. Native strip log may still appear, but the 16KB alignment fix path is applied.

**Previous:** 2026-04-28 ? **Removed GMS Document Scanner:** dropped `play-services-mlkit-document-scanner` and deleted `rememberImportTicketGmsDocumentScanLauncher`; **Take photo** now uses existing CameraX/import route (`bingoLiveCameraImport` -> `historyPhotoImport`) with no scanner/OCR redesign. `./gradlew :app:clean :app:assembleDebug` OK.

**Last update:** 2026-04-28 ? **AI cleanup + stability:** deleted API_Key_OpenAI.txt and Special_commands.txt intentionally; workspace search found **no references**; ML Kit OCR scanner remains intact (offline flow unchanged); ./gradlew :app:assembleDebug successful (up-to-date).

**Last update:** 2026-04-27 ? **Ticket QR deep link:** `mamunbingo://import-ticket?data=?` + `QrTicketCodec.encodeDeepLink` / unified `decode` (legacy `MAMUN_BINGO_TICKET:` kept); **Manifest** VIEW + **`singleTask` `MainActivity`**; **`ImportTicketDeepLinkViewModel`** ? **`NavGraph`** ? **Manual Entry** prefill; new QRs use deep link. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live keypad invalid input:** `LiveCallInputBar` **Add** enabled for any **non-blank** draft (empty still off); `handleCallClick` snackbars: **Invalid Bingo number** (unparseable), **Enter a number between 1 and 75** (out of range); duplicate line uses **`Short`** like invalid. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Keep screen on (optional):** DataStore **`keep_screen_on_during_game`** (default **true**); **Settings** ? **LIVE PLAY** toggle; **`LivePlayScreen`** applies **`FLAG_KEEP_SCREEN_ON`** only when on, always **clears** on dispose / when off. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live play ? keep screen on (always):** supersedes with setting above.

**Previous:** 2026-04-27 ? **Live play keypad:** `LiveCallInputBar` ? digit / **Add** haptic behavior. **Build** OK.

**Previous:** 2026-04-27 ? **Live sheet preview bottom sheet ? ticket QR:** `SheetDetailBottomSheet` (`LivePlayScreen`) header **QrCode2** 48dp ? `TicketQrDialog` (encode `QrTicketPayload` from preview `cells` + `serial`/`los`, `QrTicketCodec` + `QrTicketImageGenerator` on **Default** dispatcher); **`TicketQrDialog`** optional **`isLoading`**. No scroll, no in-sheet QR, **?View full detail?** unchanged. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **`AppPrimaryButton` loading state:** `loading` uses centered **Row** (18dp `CircularProgressIndicator`, 2dp stroke, 8dp gap, **onPrimary** label from `text`); while loading, **disabled** colors keep primary + onPrimary (no washed-out capture CTA). Camera label **?Capturing?** (not ellipsis). **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live import camera ? capture feedback:** `BingoLiveCameraImportScreen` ? ~120ms **shutter** white flash (0?0.8?0), **preview/viewfinder** scale pulse (1?0.97?1) via `Animatable` + `graphicsLayer`, `Scan ticket` + `loading` + back **disabled** while `capturing`; `takePicture` + QR path unchanged. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Custom CameraX + frame crop ? stable (device QA + build):** (1) **QR auto-import** ? OK. (2) **Scan ticket** ? CameraX still **cropped to green frame** (`CameraXCaptureCrop` / `VIEWFINDER_*`), `historyPhotoImport` ? OK. (3) **GMS fallback** ? OK. (4) **Gallery / uCrop editor** ? unchanged, OK. **Build:** `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **`BingoLiveCameraImportScreen` full-ticket CTA:** `animateFloatAsState` preview fade (220ms) + `AppPrimaryButton(loading)` + 240ms delay then existing `onScanFullTicket` (nav unchanged). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live camera Bingo QR:** `bingoLiveCameraImport` (CameraX + `tryDecodeBingoQrFromInputImage`, throttled ?no QR? log) before GMS; Scan/Jackpot/History **Take photo** open it; success ? same Manual Entry route as import (incl. room). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **History Detail QR:** `TicketQrDialog` under `ui/components/qr/`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **History photo import:** ML Kit **QR** before **OCR** (`ImportTicketQrPreOcr` + `QrTicketCodec`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live sheet detail bingo** matches **History** (`BingoDetailGridCard` / compact row / win banner). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 ? **Live device QA complete**; **whole-app UI consistency audit** in `NEXT_TASK.md`. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-27 (earlier) ? **Handoff docs:** Regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from codebase scan; `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-20 ? **Live cards-view overlay fix**: `LivePlayScreen` keeps the standard live bottom area visible for ? bottom sheets (Info/Settings), so opening those sheets no longer reflows the cards-view background into a broken layout; root content animation remains removed. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-19 ? `CalledHistoryPanel`: empty state uses same **72dp** row as active calls (`LatestCallCircle` + compact copy) so the live card does not jump when the first number is called. `./gradlew :app:compileDebugKotlin` OK.

**Previous:** 2026-04-19 ? Live play ? menu: **Reset game** opens the existing reset confirm; on confirm, calls clear to 0 and play can continue (`LiveRoomTopBar` + `LivePlayScreen`). `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-16 - History card micro polish: `HistorySheetCard.kt` now keeps the real Bingo preview/count while tightening spacing, shrinking the preview slightly, and balancing View/Join/Delete actions. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-15 ??? **Live list row polish (superseded):** elevated `Surface` + inset plate ??? replaced by flat reference-aligned panel above.

**Previous:** 2026-04-15 ??? **Detail sheet preview typography + alignment:** `SheetDetailBottomSheet` meta columns remain equal-width but are start-aligned; title uses stronger hierarchy vs softer meta labels; values are slightly larger with safer reserved-height estimate for short devices. `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 ??? **Handoff docs:** Regenerated `PROJECT_SNAPSHOT.md`, `LAST_SESSION.md`, `TECH_DEBT.md` from codebase; `./gradlew :app:assembleDebug` OK.

**Previous:** 2026-04-12 ??? **Premium UI polish (tokens)**: `Dimens.outlineDividerAlpha` / `outlineBorderAlpha` for shared hairlines. `BulkSelectionActionBar` ??? top-rounded dock shape, soft border, no inner divider. `AppBottomBar` top hairline uses divider alpha. `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog` ??? `surfaceContainerHigh`, `radiusXL`, `titleLarge` + `bodyMedium`.

**Previous:** 2026-04-12 ??? **Bulk add to room (My Tickets)**: `BulkSelectionActionBar` optional `FilledTonalButton` ???Add to room (n)??? for unassigned selected tickets; `onBulkAddToRoom` from `LivePlayScreen`; per-row Add/Go Live hidden in selection mode.

**Previous:** 2026-04-12 ??? **Standard bulk selection UX**: Shared `BulkSelectionActionBar` (outline ???Remove from room???, destructive ???Delete???) + `LeaveRoomBulkConfirmDialog` / `DeleteFromHistoryBulkConfirmDialog`. History: selection title (???Select items??? / ???n selected???), back clears selection, actions bar replaces tab bar when selecting, snackbars on success. My Tickets: header ???Done???, bottom action bar + snackbars.

**Previous:** 2026-04-12 ??? **Bulk leave vs bulk delete**: `RoomRepository.unassignTickets`. **My Tickets** (live): **Leave (n)** = unassign from current room only; **Delete (n)** = history only (copy clarified). **History list**: same **Leave** / **Delete** split; `onBulkLeaveSessions` (default loops `onLeaveRoom`).

**Previous:** 2026-04-12 ??? **Bulk delete (history + live My Tickets)**: `HistoryRepository.deleteSessions`. `RoomSessionCard` ??? selection mode (checkbox, primary border), hides row actions in select mode. `HistoryListScreen` ??? **Select** / **Cancel** / **Delete (n)** + confirm dialog; optional `onBulkDeleteSessions`. `MyTicketsBottomSheet` ??? same pattern; `LivePlayScreen` passes `HistoryRepository.deleteSessions`.

**Previous:** 2026-04-09 ??? **Shared interaction system**: Created `ui/core/interaction/` ??? `AppAnimation` (duration/easing tokens), `AppRipple` (shared ripple factory), `AppClick` (`Modifier.appClickable`). Migrated `RoomSessionCard` (2 clickables) and `RoundProgressCards` to `appClickable`.

**Previous:** 2026-04-09 ??? **Typography hierarchy audit**: Added `titleMedium` (SemiBold, 16sp) to custom `Typography` ??? was falling to M3 default (Medium). Removed redundant `.copy(fontWeight = SemiBold)` in `AppSectionHeader`, `ImportTicketScanResultContent`, `LiveRoomsScreen`. Fixed `LiveRoomsScreen` "Recent Rooms" Bold ??? theme SemiBold to match "Quick Actions".

**Previous:** 2026-04-09 ??? **Controls consistency pass**: `StatusPill`/`RoomStatusPill` ??? removed `heightIn(48dp)`, `radiusSmall` ??? `radiusPill`, vertical padding `spacing8` ??? `spacing4` (matches `StatusChip`). `RoomSessionCard` ??? `cardElevationDefault` (6dp) ??? `cardElevationSubtle` (2dp); footer divider `alpha 0.4f` ??? `0.28f`; `MiniBingoGrid` border softened to `alpha 0.34f`.

**Previous:** 2026-04-08 ??? App-wide: screens with **`AppHeaderBackground`** use **`MaterialTheme.colorScheme.surface`** on root **`Scaffold`** or **`Box`/`Column`** (Profile, Settings, Manual Entry, Live rooms, Ticket detail, Live sheet detail, History photo import, legal, profile sub-screens, auth register/forgot, **`MyAccountScreen`**). **`HistoryDetailScreen`** same pattern. **`BingoSheetSection` `premiumLayered`**: **`shadowElevation` 0**; soft tonal outer/inner strokes; **`BingoCardGrid`** history compact enables it. **History detail** bingo: **`BingoCardGrid`** `historyDetailCompact` wraps sheet in **`width(bingoGridWidth + 32dp)`** centered; **`BingoGridCard`** no double-wrap on success. **`LabelValueInfoRow`** + **`LabelValueInfoRowVariant`** (Default / Compact): **`labelSmall`** muted **`onSurfaceVariant`**; **`outlineVariant`** divider; History **Ticket Information** uses it + **bodyMedium**/**SemiBold** values, Status **Primary** accent. **History detail** bingo slot: **`Column`** + **`weight(1f)`** ??? **`BoxWithConstraints`** ??? **`BingoGridCard`**; **`BingoCardGrid`** uses inner max minus sheet padding for cell sizing; cell = **min** of width- and height-derived size (**22???44dp**). No breakpoint / **`graphicsLayer`**. **History detail** `AppTopBar`: default title; live CTA + delete in **`actions`** row (`HistoryDetailHeaderActions`; under **360dp** screen: icon-only live). **History detail** `BingoCardGrid` **`historyDetailCompact`**: `BoxWithConstraints` width-based cell size (22???44dp), tight gaps; **`BingoGrid5x5`** **`fixedLayoutCellSize`** + **`FixedPlayModeGrid`** (no 1:1 aspect block). **History detail** top: compact spec ??? **IconContainerBg**/**Primary** active + live/leave pills; flat **TICKET INFORMATION** (**Outline** 9sp label, **OnSurface** rows, **Outline** hairlines); stats **`SpanStyle`** bold sheet count + tail; **WarningBorder** top strip + **WarningContainer** row + **28dp** **WarningIcon** slot + **`MiniBingoPreview`** + score pill (**OnPrimary** on **WarningIcon**); **`CalledHistoryPanel`** after strip (not in ticket block). **Live card:** light **F6F8F4 ??? F0F5EC**; **no** shadow. **Pills:** **LIVE** ??? soft green fill + green rim; **6dp** dot **`Color.Red`**, infinite **scale 1???1.4** + **? 1???0.6** (**1200ms** `FastOutSlowIn`, reverse), ?? parent **`dotAlpha`**; text **SemiBold**. **Counter** ??? lighter vs LIVE. **LazyRow:** **12dp** horizontal **contentPadding**; **`spacedBy(8dp)`**; **`rememberSnapFlingBehavior`**; **`animateScrollToItem`** with **~???10dp** `scrollOffset`. **`CalledHistoryPanelContext.HistoryDetail`** uses **`takeLast(MAX_LIVE_CALLS)`**. No **`LiveCallNumberCircles.kt`**.

## Completed features`r`n`r`n- **Scan type selection (Direct Scan):** `ScanTypeSelectionSheet` on Scan tab; `BingoScanType` nav arg on `bingoLiveCameraImport`; History/Jackpot default PLAY_PAPER.
- **Scan OCR (isolated):** `OnlineBingoOcr` (ONLINE), `PlayPaperBingoOcr` + `MainSheetBingoOcr` (wrappers ? `ImportTicketImageOcr`); VM routes by `BingoScanType`.
- **Import ? all 3 options:** Live **QR** auto-detect; **Scan ticket** CameraX + **viewfinder-matched crop** ? `historyPhotoImport` OCR; **GMS** internal fallback; **gallery/editor** (Apply / uCrop) ? device QA + build OK.
- **Import pre-OCR zoom guard:** over-zoomed ticket captures are blocked before OCR and show camera distance guidance to keep full ticket in frame.
- **OCR reliability (minimal):** `filterToCentralGridRegion()` adaptive inset mode added to preserve edge candidates when valid count is low/near threshold, while keeping prior strong behavior for robust reads.
- **Live import camera ? full-ticket still:** `ImageCapture` on `BingoLiveCameraImportScreen` + `FileProvider` ? same pending-URI + `historyPhotoImport` as GMS; QR `ImageAnalysis` path unchanged; GMS secondary.
- **Live import camera ? capture feedback (premium):** shutter flash + viewfinder scale pulse + button/back disabled + ?Capturing?? during capture (`BingoLiveCameraImportScreen`); no capture/QR logic change.
- **Planning (2026-04-27):** Custom CameraX full-ticket scanner phased plan (4 phases, GMS + current OCR until stable); `NEXT_TASK.md` / `LAST_SESSION.md`.
- **Live QR ? GMS handoff polish:** `BingoLiveCameraImportScreen` ?Full ticket scan? uses preview `graphicsLayer` alpha + primary button loading; back disabled during handoff; navigation callback unchanged.
- **CalledHistoryPanel stable height**: empty ?no calls? UI matches active call row height (`CalledHistoryMainRowHeight` / `LatestCallCircle` size).
- **Live ? Reset game**: top bar overflow includes **Reset game** (same flow as existing Reset ? confirm dialog, `RoomRepository.resetCalledNumbers`, un-archive room).
- **History list cards (`HistorySheetCard`)**: Live-style two-row panel; denser list spacing; overflow menu for join/leave/delete.
- **Live play sheet detail preview (`SheetDetailBottomSheet`)**: left-aligned meta columns + clearer title vs label hierarchy while preserving unified grouped card + responsive full-grid fit.
- **Shared `LabelValueInfoRow`**: reusable ticket-style label/value rows with optional compact vertical rhythm.
- **History detail layout**: fixed column (no page scroll); root **`Scaffold`** **`surface`** page fill (no header/background seam); bingo slot **`weight(1f)`** + constraint-based cell size (no breakpoint **`graphicsLayer`**).
- **History detail header actions**: live navigation in top bar; no duplicate-session row under grid.
- **History detail bingo grid**: compact layout from available width; proportional header letters; full grid visible without square aspect-ratio block (History only); optional **`BingoSheetSection` `premiumLayered`** (subtle elevation + nested inner plate).
- **Navigation cold start**: `splash` (rain every launch; ~3.45s) ??? `onboarding` once (`SettingsRepository.onboarding_completed`), then `auth/login`; faster rain + 2.4s slides; splash logo: Eco mark with halo pulse + breathe/sway; gradient ???BINGO??? title with left-to-right stagger.
- **Import ticket scan UI count**: `finalUiGridRowMajor` normalizes OCR row-major to 25 cells in `setScanResult`; `displayedCount` / quality card / History photo import summary use non-zero count on that grid only; debug log `ImportTicketFinalUi` at VM handoff.
- **`ImportTicketImageOcr`**: analyzer grid crop via `BingoNumberAnalyzer.tryDetectBingoGridCropForOcr`; heuristic fallback + `heuristicGridCrop` log; gallery `bypassInternalGridCrop`; final dedup; `rankRawStagesForFallback` / `fallbackPick`; `finalGridSanity` / tiers; `finalQualityCheck`; `stagePickAdjust`; +4 guard; `finalDuplicateCheck`; `attachLeftStripMeta` + `metaDebug`.
- **`LeftStripMetaOcr`**: 0?/90?/270?; line-ordered text from blocks/lines; Losnummer/Seriennummer centers + digit runs (line/element + vertical single-digit merge); proximity assignment; 5/4 length fallback; then existing regex/heuristic on normalized text; `metaRaw`/`metaNormalized`/`metaCandidates`/`metaChosen` ??? isolated from grid OCR.
- **Live Navigation header** (`LiveRoomsScreen`): `AppTopBar` `titleContent` = title + `Spacer(Modifier.weight(1f))` + 40dp primary `Add` (same pattern as History) ??? `showCreateDialog = true`.
- **Import Ticket UI**: shared **`ImportTicketMainContent`**; success ??? **Manual Entry** prefill via nav (no inline review card); **`ScanResultSummaryCard`** on error/loading only.
- **Import / History photo header polish**: **`AppHeaderBackground`** vertical greens (light) + quieter strokes; **`HeroBannerCard`** no shadow, tinted fill, **outline** rim + soft dashed; **`ImportTicketFailedScanContent`** / info row / prescan tips surfaces aligned; **`HistoryPhotoImportScreen`** content inset + calmer info icon.
- **`TicketInfoCard`** / **`TicketInfoStatusChip`**: **#F7F8F6** fill, **black 0.05** border, **18dp** corners, no elevation; **`HistoryDetailScreen`** ticket block = divider-separated **label / value** rows (no chips); **`LiveSheetDetailScreen`** uses **`TicketInfoCard`** for ticket block; **bingo grid** uses same **`BingoDetailGridCard`** path as History detail.
- **Import gallery / take photo**: Gallery pick ??? **uCrop** (freestyle, 3:4, bottom controls on, ALL gestures, max scale 28) ??? **`setGalleryPendingEdit`** ??? **Apply** / **Discard** ??? OCR. Camera/GMS unchanged.
- **Live play header**: compact **`GreenCard`** / **`GreenCardCompact`** ??? neutral container, **LIVE** + bordered count chip; duplicate big-call UI removed in favor of **`CalledHistoryPanel`**.
- **Live status pills** (`LiveRoomTopSection`): LIVE green-tint chip + green rim, stronger dot/text, slow dot breath; counter de-emphasized vs LIVE.
- **`CalledHistoryPanel` motion**: new last-call only ??? main circle scale+alpha in; LazyRow ???latest??? soft fade in when it becomes latest; inactive circles static.
- **`CalledHistoryPanel` row**: horizontal **content** inset + scroll to active (last) call with breathing offset; no gradient fade overlay.
- **`AppHeaderPageLayout`**: shared screen wrapper for `AppHeaderBackground`-based screens ??? `surface` root fill + gradient band (top 40%) + `Column(topBar, content)`; migrated HomeScreen, HistoryListScreen, ProfileScreen, SettingsScreen, MyAccountScreen, LiveRoomsScreen, LoginScreen, LiveSheetDetailScreen.

## In progress

- **Supabase:** dashboard setup (keys, email auth) — see `SupabaseAuthPlan.kt`; app-side auth + verify + full password reset flow complete.
- Device validation: verify too-zoomed captures trigger guidance and skip OCR; verify normal captures still run OCR.
- Device validation: run 3-5 known fail-prone ticket images and compare 25-cell completion vs baseline with new adaptive inset mode.
- Device QA: `BingoLiveCameraImportScreen` **Scan ticket** ? feel shutter + scale, confirm no QR regression; GMS + gallery still OK. Build: `./gradlew :app:assembleDebug`.
- Device QA: `AppHeaderPageLayout` on Home, HistoryList, Profile, Settings, MyAccount, LiveRooms ? no gray seam, no flat white header.

## Pending tasks

- Supabase: optional `profiles` upsert (no ticket/history sync).
- Tune zoom threshold only if needed based on device QA (keep OCR pipeline unchanged).
- If edge-preserve mode is stable, tune only inset thresholds/fractions incrementally (no crop/consensus redesign).
- **Roadmap (see `NEXT_TASK` table):** optional Phase 3 **crop/confirm** UI, Phase 4 **drop GMS** after sustained QA ? **do not** remove GMS or OCR early.

- Revisit **Settings ??? Live header style** (orphaned for live header) ??? remove UI or repurpose.
- Device QA: **PickVisualMedia** Gallery + GMS Take photo on `historyPhotoImport`; NavGraph pending-URI handoff from main/Jackpot.
- Device QA: hero suppressed on auto Manual Entry (unchanged).
- Rescan tickets; confirm `ImportTicketOcr` / `ImportTicketFinalUi` logs vs grid if issues.
- Tune `MIN_VALID_CELLS_FOR_MANUAL_ENTRY_NAV` if needed (currently 20).







