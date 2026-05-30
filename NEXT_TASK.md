# Next task

**Goal:** Device QA — Live room reset/archive flow on `feature/live-room-reset-archive`.

**Verify:** Add 2 tickets → call numbers → reset room → room empty, tickets preserved, History/Ticket detail shows play log + archived called numbers. Re-add ticket → fresh game. `./gradlew :app:assembleDebug` OK.

**Previous:** Live room reset archives play logs, unassigns tickets, clears called numbers; `ticket_play_logs` DB v8.
