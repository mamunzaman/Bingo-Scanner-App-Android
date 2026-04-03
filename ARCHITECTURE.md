# Mamun Bingo App — Architecture Overview

Current implementation state.

---

# 1. Navigation Structure

## Bottom Tabs

- Home
- Scan
- Jackpot
- Profile

---

## Core Routes

| Route | Screen |
|--------|--------|
| splash | SplashScreen |
| auth/login | LoginScreen |
| auth/register | RegisterScreen |
| main | MainTabsScreen |
| livePlaySession/{ticketId} | LivePlayScreen |
| liveSheetDetail/{ticketId} | LiveSheetDetailScreen |
| manualEntry | ManualEntryScreen |
| history | HistoryListScreen |
| historyDetail/{sessionId} | HistoryDetailScreen |
| ticket/{ticketId} | TicketDetailScreen |

Room-based route may exist:
livePlayRoom/{roomId}

But session-based route is primary live implementation.

---

# 2. Data Layer

## HistoryRepository

Singleton object.

State:
- _sessionsList
- _sessionsFlow (StateFlow<List<HistorySession>>)
- _ticketCells

Responsible for:
- saveManualEntrySheet()
- removeFromLive()
- getLiveSessions()
- getSessionById()

Live status determined by:
HistorySession.isCompleted

---

## HistorySession

Fields:
- id
- title
- isCompleted
- calledNumbersFull
- sheetsPlayed
- sheetName
- playedAtMillis

Live state:
isCompleted == false

---

## LivePlayViewModel

Receives:
ticketId via SavedStateHandle

On load:
- getLiveSessions()
- Map to LiveSheetUi
- Set selectedIndex
- Provide primaryData

No continuous observation of sessionsFlow.

---

# 3. Manual Entry Flow

ManualEntryViewModel:
- Maintains 25 cell grid
- Validates column ranges
- Prevents duplicates
- AutoFill logic per column B/I/N/G/O

Save:
HistoryRepository.saveManualEntrySheet()

Save & Play:
navigate("livePlaySession/{ticketId}")

---

# 4. History Flow

HistoryListScreen:
Observes sessionsFlow

HistoryDetailScreen:
- Shows session data
- Shows Leave Live if session is live
- removeFromLive() sets isCompleted = true

---

# 5. Room System (Partial / Experimental)

RoomRepository exists but is not fully authoritative.

Room logic and session logic currently overlap.

Live is primarily session-driven.

---

# 6. UI Component Structure

Shared components:
- AppTopBar
- AppBottomBar
- AppPrimaryButton
- AppConfirmDialog
- BingoGrid5x5
- BingoCell
- BingoCalledNumbers

LiveEmptyState shown when no live sessions exist.

---

# 7. Known Technical Debt

- Mixed room + session logic
- No persistent database
- No win detection logic
- LivePlayViewModel not fully reactive
- No full refresh when returning from ManualEntry
- Add-to-Live room selection inconsistent
- Delete session not implemented
- History filters not functional
- Scan not integrated

---

# 8. Future Refactor Direction

Recommended:
Choose ONE system:
- Session-based live only
OR
- Full Room-based architecture

Avoid mixing both.

---

# 9. Build

.\gradlew assembleDebug
