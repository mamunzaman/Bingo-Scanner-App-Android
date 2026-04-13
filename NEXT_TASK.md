# Next task — Device QA: manual entry title editing (no FocusRequester)

1. **Device:** tap pen icon → field appears, tap it → keyboard opens, type name → no crash
2. **Device:** tap pen icon → type → press Done on keyboard → editing stops, keyboard hides
3. **Device:** tap pen icon → tap a grid cell → editing stops, keyboard hides, cell selected
4. **Device:** tap pen icon → tap outside card → editing stops
5. **Device:** rapidly tap pen icon multiple times → stable, no crash
6. **NOTE:** Field no longer auto-focuses when editing starts — user must tap the title text field to type. This is intentional (avoids all FocusRequester crashes).
