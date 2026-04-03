# Mamun Bingo App Repository Map

## Root Purpose
- Bingo game application with live rooms, ticket management, and game history

## Important Folders
- `app/src/main/java/com/example/mamunbingoapp/`
  - `ui/`: Screens and components
  - `viewmodel/`: ViewModels
  - `repository/`: Data layer
  - `theme/`: Design tokens
  - `navigation/`: Navigation setup

## Key Screens
- `MainTabsScreen.kt`: Root screen with bottom tabs
- `LiveRoomsScreen.kt`: Game lobby browser
- `LivePlayScreen.kt`: Active game session
- `HistoryListScreen.kt`: Past game records
- `TicketDetailScreen.kt`: Ticket viewer

## Shared UI Components
- `AppTopBar.kt`: Reusable top bar
- `BingoCardGrid.kt`: Main game grid
- `LiveCallInputBar.kt`: Live call input bar (number entry, call, undo)
- `RoomSessionCard.kt`: Room list items
- `CalledHistoryPanel.kt`: Called numbers

## ViewModels
- `LivePlayViewModel.kt`: Game session logic
- `LiveRoomsViewModel.kt`: Room management
- `HistoryViewModel.kt`: Game history
- `TicketDetailViewModel.kt`: Ticket viewing

## Repositories
- `TicketRepository.kt`: Ticket operations
- `HistoryRepository.kt`: Game records
- `RoomRepository.kt`: Room management

## Design Token Files
- `Color.kt`: Color palette
- `Dimens.kt`: Spacing/sizing
- `Typography.kt`: Text styles

## Navigation Entry Points
- `MainActivity.kt`: Single activity host
- `NavGraph.kt`: Main navigation graph

## Notes for AI Assistants
- Follow Material 3 design system
- Maintain 8/12/16 spacing pattern
- Reuse existing components first
- Prefer small composable updates

## AI Navigation Hints

Primary folders to inspect first:
- `ui/screens/`: Main screen implementations
- `ui/components/`: Reusable UI components
- `viewmodel/`: ViewModel classes
- `repository/`: Data repositories
- `theme/`: Design tokens

Secondary folders (inspect only if needed):
- `navigation/`: Navigation setup
- `data/db/`: Database operations
- `domain/`: Business logic
- `scanner/`: Ticket scanning logic