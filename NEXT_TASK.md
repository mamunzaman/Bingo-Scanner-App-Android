# Next task

**Goal:** Device QA — Home Active Ticket cards match History Detail result source (test date + archived + live room).

**Verify:** Old ticket → set test date in History Detail (last week) → Home card shows same marks + “Test date result”. Reset live room → Home shows archived marks + “Archived result”. Re-add to room → live numbers again. `./gradlew :app:assembleDebug` OK.

**Previous:** Shared `TicketCalledNumbersResolver`; `HomeViewModel.activeTicketsUi`; optional result-source label on `ActiveTicketCard`.
