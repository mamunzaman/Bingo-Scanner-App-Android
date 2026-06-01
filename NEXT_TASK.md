# Next task

**Goal:** Optional: persist full grid snapshot on archive (true point-in-time ticket without live DB).

**Verify:** Archived game → ticket row → read-only grid; `./gradlew :app:assembleDebug` OK.

**Previous:** Bottom nav audit — tab highlight via route/VM/handle; `stageMainShellTab` on entry routes; shell `onMainBottomBarTabSelected` wired on pushed screens.

**Verify:** All tabs from Home/archived/history/live/settings; back + camera; `./gradlew :app:assembleDebug` OK.
