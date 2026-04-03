# Mamun Bingo App Context

## Architecture
- MVVM with Jetpack Compose
- Single Activity architecture

## Main Packages
- `ui`: Screens and components
- `data`: Repositories and models
- `domain`: Business logic
- `navigation`: Compose navigation

## Key UI Components
- `AppTopBar`: Reusable top bar
- `BingoCard`: Main game component
- `NumberGrid`: Number display

## Design Tokens
- `Color.kt`: Theme colors
- `Dimens.kt`: Spacing/sizing
- `Typography.kt`: Text styles

## Navigation
- Single NavHost in MainActivity
- Uses `navigation-compose`

## Key Features
- Rooms: Game lobbies
- Tickets: Player cards
- Caller: Number generation
- History: Past games
- Scanner: Ticket validation

## Data Layer
- Repositories per feature
- Shared ViewModels

## Constraints
- Material 3 design system
- Follow existing spacing scale
- Reuse components first