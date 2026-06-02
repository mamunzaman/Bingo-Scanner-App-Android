# Next task

**Goal:** Verify Supabase auth + Home refresh after Ktor 3 restore; field-test Master Sheet Gemini (`generativeai-google` KMP SDK).

**Verify:** Login/auth; Home draw fetch; Master Sheet AI + offline fallback (`MainSheetAiOcr`); Player Sheet + Online unchanged.

**Previous:** **Master Sheet LOS/SERIE OCR** — multi-layout meta parser (footer + top star + spatial 4+5); `MainSheetMetaOcr` debug logs. Build OK.

**Previous:** **Live Play control console polish** — premium bottom dock, primary Call, outlined Undo/keypad, keypad card. Build OK.

**Previous:** **History Detail ticket LOS/SERIE** — plain label/value in ticket card; hero chips unchanged. Build OK.

**Previous:** **History Detail UI polish** — `AppSectionTitle`, premium LOS/SERIE chips, 16dp section rhythm, stats/called panels. Build OK.

**Previous:** **Duplicate sheet dialog v2** — compact sheet, `AppIconTile` duplicate icon, `AppSectionSurface` + themed LOS/SERIE chips, stronger outlined CTA. Build OK.

**Previous:** **Duplicate sheet dialog polish** — scrim + bottom sheet card, LOS/SERIE meta surface, primary/outlined/text actions. Build OK.

**Previous:** **Duplicate sheet scan guard** — LOS+SERIE weekly duplicate dialog on Manual Entry save; Open Existing → History Detail; Save Anyway bypass. Build OK.

**Previous:** **Shell revert** — bottom nav responsiveness restored; camera transition workaround removed. Build OK.

**Previous:** **Bottom nav touch fix** — clip shell content above bar. Build OK.

**Previous:** **Scan launch stability** — hero frozen on Launch Camera tap. Build OK.

**Previous:** **Scan hero animation** — soft thin scan line only; slow ease-in-out; reduced-motion preserved. Build OK.

**Previous:** **Projects top bar** — single-line `AppTopBar` title; subtitle moved to body below shell inset. Build OK.

**Previous:** **Projects root spacing** — `ProjectsList` LazyColumn `contentPadding.top` 16dp → 0dp (shell-only top inset). Build OK.

**Previous:** **Profile summary card inset** — `ProfileSummaryCard` padding 20dp → 16dp (root hero rhythm). Build OK.

**Previous:** **Scan tab polish** — no back arrow; premium scanner animation. Build OK.

**Previous:** **Scan curve + CTA readability** — upward arc; bolder labelLarge buttons. Build OK.

**Previous:** **Scan screen fit pass** — measured illustration budget, global typography, no clip/scroll. Build OK.

**Previous:** **Scan screen one-screen layout** — no scroll; 70/30 weighted hero/manual; curved green→white transition; FAB-safe manual section. Build OK.

**Previous:** **Scan screen proportions** — full-width tall hero (~62% screen), larger scanner/CTAs, FAB-safe manual section padding. Build OK.

**Previous:** **Scan screen polish** — unified rounded hero container, larger scanner illustration, premium CTAs, wave removed; logic unchanged. Build OK.

**Previous:** **Scan screen redesign** — premium card layout, hero scanner animation, AppTopBar + help tips; navigation/logic unchanged. Build OK.

**Previous:** **Inner page body spacing** — legal/profile Box shells → `AppHeaderPageLayout`; removed duplicate `spacing5` + leading 16dp spacer on auth/profile inner pages. Build OK.

**Previous:** **Home spacing** — `HomeTopBar` aligned with Live Play (`screenHorizontalPadding`); content top/bottom insets match Profile shell; section gaps 16/8. Build OK.

**Previous:** Supabase `scrape-bingo` deploy + cron (see `supabase/README.md`).

**Verify:** Live Play — top scan + create icons; Quick Actions 4 tiles (incl. Archived); horizontal scroll on narrow width. `./gradlew :app:assembleDebug` OK.

**Previous:** Live Rooms Quick Actions + top bar polish (Archived in row; direct scan + create room icons).

**Previous:** My Tickets `TicketRowCard` uses shared list preview; `TicketPickerMiniGridCell` on picker model.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
