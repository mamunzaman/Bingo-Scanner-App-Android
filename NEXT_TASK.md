# Next task

**Goal:** Device QA — app language dropdown + locale persistence.

**Verify:** Fresh install on German device → Settings → APPEARANCE shows **Deutsch**. English/other locale → **English**. Change language in Settings → restart → choice persists. My Account has no language text field. `./gradlew :app:assembleDebug` OK.

**Previous:** Login IME layout + scroll-to-submit on real device.
