# Demo — Full Live Flow

This document describes the current working Live flow of the Mamun Bingo App.

System currently uses session-based live logic (HistorySession.isCompleted).
RoomRepository may exist but is not the primary live authority yet.

---

## 1. Entry Point

Jackpot tab → LiveRoomsScreen

LiveRoomsScreen may display:
- Create Room
- Join Room
- Quick Actions
- Recent Rooms

However, Live play itself is still session-based.

---

## 2. Live Play Entry

Route:
livePlaySession/{ticketId}

Flow:
1. Navigation passes ticketId
2. LivePlayViewModel receives ticketId from SavedStateHandle
3. loadState() loads all live sessions:
   HistoryRepository.getLiveSessions()

Live sessions = sessions where isCompleted == false

---

## 3. Live Session Logic

HistorySession.isCompleted == false → LIVE  
HistorySession.isCompleted == true → COMPLETED  

LivePlayViewModel:
- Maps sessions to LiveSheetUi
- Uses session.id as ticketId
- selectedIndex based on route ticketId
- primaryData = selected sheet

If ticket not found → index = 0

---

## 4. Called Numbers

Stored inside:
HistorySession.calledNumbersFull

Read via:
HistoryRepository.getTicketData(ticketId)

There is currently no fully reactive flow for called numbers update.

---

## 5. Manual Entry → Save & Play

Route:
manualEntry

Flow:
1. User fills grid
2. saveAndPlay()
3. HistoryRepository.saveManualEntrySheet()
4. Emits NavigateToLivePlay(ticketId)
5. NavGraph:
   navigate("livePlaySession/$ticketId")

New manual sheet automatically becomes LIVE (isCompleted = false)

---

## 6. History → Add To Live

Currently:
- HistoryDetailScreen shows "Leave Live" if session is live
- removeFromLive(sessionId) sets isCompleted = true

There is no strict room-scoped add logic enforced.

---

## 7. Remove From Live

HistoryRepository.removeFromLive(sessionId):
- session.copy(isCompleted = true)
- Update sessionsFlow

UI updates via sessionsFlow emission.

---

## 8. Known Structural Limitations

- Room logic and session logic mixed
- LivePlayViewModel only loads once (no continuous observation)
- No explicit Leave Room navigation
- Called numbers stored in session instead of dedicated live store
- No persistent database
- No win detection
- No ticket removal from live

---

## 9. Build

.\gradlew assembleDebug
