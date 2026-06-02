# Next task

**Goal:** Airplane-mode Master Sheet QA — `MainSheetMetaOcr` `emergencyFilled` / `emergencySelectedRowY`; both layout types prefilled in Manual Entry.

**Verify:** MAIN_SHEET always opens Manual Entry (prefilled or empty); PLAY_PAPER + ONLINE unchanged; no scan image on Manual Entry. `./gradlew :app:assembleDebug` OK.

**Previous:** **Master Sheet → Manual Entry** — removed review screen; MAIN_SHEET routes to existing Manual Entry with prefill. Build OK.

**Previous:** **Ktor 3 + Gemini KMP SDK** — Supabase + `generativeai-google` for Master Sheet AI. Build OK.
