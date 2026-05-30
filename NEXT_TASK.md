# Next task

**Goal:** Device QA — profile photo delete persists after refresh.

**Verify:** Delete photo → pull-to-refresh → initials/no image; re-upload still works; delete failure shows error. `./gradlew :app:assembleDebug` OK.

**Previous:** Fix remote `avatar_url` null via Postgrest `set()`; VM clears cache + local state on success.
