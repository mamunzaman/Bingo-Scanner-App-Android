# Bingo App – Cursor Rules

This project follows a strict staged development workflow.

Navigation is stable.
UI must be finalized before logic expansion.
No architectural refactors without explicit instruction.

---

# SCREEN STRUCTURE & DESIGN (ALWAYS FOLLOW)

**Canonical reference:** `SCREEN_STRUCTURE_AND_DESIGN.md`

- **Every** new page or screen MUST follow the structure and design rules in that file.
- One shared **AppTopBar**; status bar inset only there; no duplicate insets in screens.
- Scaffold: tabs use `contentWindowInsets = WindowInsets(0)`; screen-level uses `topBar = { AppTopBar(...) }` and content only `padding(innerPadding)`.
- Do not add `statusBarsPadding()` or `windowInsetsPadding(WindowInsets.statusBars)` in screen content or in another top bar.
- Before adding a new screen, read the checklist in `SCREEN_STRUCTURE_AND_DESIGN.md`.

---

# PROJECT ARCHITECTURE STATUS

## Bottom Tabs (FINAL – DO NOT CHANGE)

1. Home
2. Scan
3. Jackpot
4. Profile

- There is NO LivePlay tab.
- Live Play starts only from Jackpot tab.
- Do NOT add or rename tabs.

---

# LIVE FLOW ARCHITECTURE (LOCKED)

Jackpot Tab
    ↓
LiveRoomsScreen (Hub)
    ↓
Start / Resume / Go Live
    ↓
livePlaySession/{ticketId}
    ↓
LivePlayScreen (Grid + Session UI)

Rules:
- There is NO composable("livePlay") route.
- Only livePlaySession/{ticketId} is valid.
- Do NOT introduce new Live routes.
- Do NOT move LivePlay into bottom navigation.

---

# DEVELOPMENT MODES

The project runs in controlled modes.

---

# UI_DESIGN_MODE (Current Mode)

## Goal
Finalize UI to match `design_reference/` before implementing logic.

## Allowed Modifications
- ui/screens/**
- ui/components/**
- ui/theme/**

## Forbidden in UI mode
- Changing navigation routes
- Modifying NavGraph structure
- Adding ViewModels
- Changing repository structure
- Adding business logic
- Refactoring data flow

## If data missing
Use fake demo data inside screen only.
Do NOT modify repositories.

## UI Output Rules
Every UI task must:
1. Keep function signatures stable
2. Compile successfully
3. Not modify unrelated screens
4. Add @Preview if possible
5. Match spacing and layout from reference

---

# LivePlayScreen UI Specification (LOCKED)

Layout order:

1) AppTopBar – "Live Play"
2) LIVE header card:
   - LIVE chip
   - Session title
   - Big last called number
   - Input + Enter + Scan + Undo row
3) Called History Section:
   - Title: CALLED HISTORY
   - B I N G O header row
   - Group numbers:
     B: 1–15
     I: 16–30
     N: 31–45
     G: 46–60
     O: 61–75
   - Max 25 numbers displayed
   - Last called highlighted
4) Bingo Sheets Section:
   - Cards/List toggle (UI only)
   - Horizontal carousel
   - Multi-sheet support
   - Each sheet:
       Title
       "Marked X/25"
       5x5 visible grid
   - Carousel snap paging feel

Entire screen must scroll vertically.

---

# CORE_LOGIC_MODE (Next Phase – NOT ACTIVE YET)

When activated:
- Implement BingoEngine
- Called numbers logic
- Auto-mark cells
- Win detection
- Session completion state

Do NOT start this until UI is finalized.

---

# REPOSITORY RULES

HistoryRepository:
- In-memory only (for now)
- Do not convert to Room yet
- Do not change session model structure

---

# NAVIGATION RULES (CRITICAL)

DO NOT:
- Add new composable routes without approval
- Add duplicate navigation layers
- Move screens between tabs
- Rename route strings randomly

Navigation is considered stable.

---

# CURSOR PROMPT TEMPLATE (UI Mode)

Task: Finalize UI for [ScreenName].

Constraints:
- UI only
- No navigation changes
- No repository changes
- No ViewModel changes

Deliver:
- Updated composable
- Helper composables if needed
- Must compile
- No architecture modifications

---

# CURRENT PRIORITY

Finalize:
- LivePlayScreen UI (carousel + called history)
- Ensure responsive layout
- Ensure no header clipping
- Ensure snap behavior for sheets

After that, switch to CORE_LOGIC_MODE.

---

END OF RULES
