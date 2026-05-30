# Next task

**Goal:** Tag **v0.10** after device QA on DE locale + error-message paths.

**Verify:** Settings → Deutsch — active ticket chips, My Tickets filters (Alle/Heute/Woche), called-numbers sheet title, home remote errors, QR failure dialogs (no raw exception text). `./gradlew :app:assembleDebug` OK.

**Previous:** v0.10 pre-tag i18n/runtime cleanup — ActiveTicketCard, TicketFilter, CalledNumbersDetailSheet, HomeViewModel + QR safe errors.
