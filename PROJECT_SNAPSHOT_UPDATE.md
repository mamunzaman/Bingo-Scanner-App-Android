---

## Feature

**My Tickets bottom sheet** — Modal bottom sheet on Live Play screen to browse, filter, search, and select tickets to go live; includes empty state with "Create New Ticket" CTA.

## Files changed

**Created**
- `app/src/main/java/com/example/mamunbingoapp/ui/model/TicketUiModel.kt`
- `app/src/main/java/com/example/mamunbingoapp/ui/screens/live/MyTicketsBottomSheet.kt`

**Updated**
- `app/src/main/java/com/example/mamunbingoapp/data/HistoryRepository.kt` — added `getAllTicketsForPicker()`
- `app/src/main/java/com/example/mamunbingoapp/ui/screens/live/LivePlayScreen.kt` — added "+" button, sheet state, `onGoLive` callback
- `app/src/main/java/com/example/mamunbingoapp/navigation/NavGraph.kt` — wired `onGoLive` for `livePlaySession` route
- `app/build.gradle.kts` — added `ExperimentalMaterial3Api` opt-in

## Components

- `MyTicketsBottomSheet` — Modal bottom sheet (~80% height, drag handle, scrim)
- `TicketsBottomSheetContent` — Layout and filtering/search/sort logic
- `TicketsHeader` — Title "My Tickets", subtitle "Select a ticket to go live"
- `TicketsSearch` — OutlinedTextField with search icon, placeholder "Search tickets"
- `TicketsFilterRow` — FilterChips (All, Today, This Week) + sort dropdown
- `TicketRowCard` — Ticket row with thumbnail, name, date/time, "Go Live" button
- `TicketsEmptyState` — Centered illustration, "No tickets yet", body text, "Create New Ticket" button
- `@Preview` Light and Dark for `MyTicketsBottomSheet`

## State & Models

- **TicketUiModel**: `id`, `sessionId`, `title`, `createdAt`, `status?`
- **TicketFilter** enum: `All`, `Today`, `ThisWeek`
- **TicketSort** enum: `Newest`, `Oldest`, `NameAZ`
- **rememberSaveable**: `query`, `selectedFilter`, `selectedSort`, `isMyTicketsSheetOpen`
- **remember**: `sortExpanded`, `allTickets` (from `HistoryRepository.getAllTicketsForPicker()`)

## Navigation

- **Live Play screen** — "+" button opens sheet; no new route
- **onGoLive(sessionId)** — Closes sheet, calls `HistoryRepository.addToLive(sessionId)`, pops back, navigates to `livePlaySession/{sessionId}`
- **onCreateTicket** — Closes sheet, calls existing `onNavigateToManualEntry` → `manualEntry` route

## Behavior

- **Data**: `HistoryRepository.getAllTicketsForPicker()` flattens sessions → sheets into `TicketUiModel` list
- **Sort** (default Newest): `Newest` (createdAt DESC), `Oldest`, `Name A–Z` via DropdownMenu
- **Filter**: `All` (no filter), `Today` (createdAt in device today), `This Week` (current week, device timezone)
- **Search**: Case-insensitive filter on `title`
- **Empty state**: Shown when filtered list is empty; "Create New Ticket" button navigates to manual entry
- **ModalBottomSheet**: `rememberModalBottomSheetState(skipPartiallyExpanded = true)`, ~80% height, 24dp rounded corners

## Theme

- Uses only `MaterialTheme.colorScheme` (no hardcoded colors)
- FilterChip, OutlinedTextField, Button, surfaces — all theme-aware
- Previews for Light and Dark modes included

## Build

- **Command**: `.\gradlew assembleDebug` (with `GRADLE_USER_HOME` set)
- **Result**: BUILD SUCCESSFUL

## Next steps

1. **Grid thumbnail in TicketRowCard** — Replace icon placeholder with actual mini `BingoGrid5x5` preview per ticket
2. **Sort dropdown anchor** — Fix DropdownMenu positioning to anchor correctly to the "Sort by" control
3. **Ticket freshness** — Add "New" badge or indicator for recently created tickets

---
