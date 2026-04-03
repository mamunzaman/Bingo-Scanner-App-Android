# Mamun Bingo App — UI Spacing Rules

Use this doc to keep horizontal padding and list alignment consistent across the app (Jetpack Compose + Material 3).

---

## 1. Single source of truth

- **Horizontal screen padding:** Always use `Dimens.screenHorizontalPadding` (currently 16.dp).
- Do **not** hardcode `16.dp` or other values for screen edges; use the token so future changes are in one place.

---

## 2. Never double-apply horizontal padding

- Only **one** level should apply horizontal padding for a given content area:
  - **Either** the parent (Column / LazyColumn / Box) **or**
  - The list (LazyRow / LazyColumn) via `contentPadding`.
- If both add horizontal padding, the first card/row will be over-indented and misaligned with the rest of the screen.

---

## 3. LazyRow / LazyColumn `contentPadding` usage

| Situation | Use `contentPadding` horizontal? |
|-----------|-----------------------------------|
| List is **inside** a container that already has `Modifier.padding(horizontal = Dimens.screenHorizontalPadding)` | **No** — use `PaddingValues(horizontal = 0.dp)` or omit. |
| List is the **top-level** scroll content with no padded parent | **Yes** — use `contentPadding = PaddingValues(horizontal = Dimens.screenHorizontalPadding)`. |

- Use `contentPadding` for **vertical** insets (e.g. bottom bar) as needed; keep horizontal rule above.

---

## 4. When to use parent padding vs list contentPadding

**Option 1 (recommended for screens with mixed content):**  
Parent (e.g. `LazyColumn` or `Column`) has horizontal padding; inner lists use **no** horizontal `contentPadding`.

- Use when: Multiple sections (header, list, cards, buttons) must share the same left/right edge.
- All content aligns; only one place controls the inset.

**Option 2:**  
Parent has **no** horizontal padding; the list owns padding via `contentPadding`.

- Use when: The screen is essentially one list and you want the list to control its own insets.
- Do not add another padded wrapper around the list.

---

## 5. Standard spacing tokens

Prefer `Dimens` when available:

| Token | Value | Use |
|-------|--------|-----|
| `Dimens.spacing8` | 8.dp | Small gaps (e.g. between label and value). |
| `Dimens.spacing16` | 16.dp | Default section / card padding; `screenHorizontalPadding`. |
| `Dimens.spacing24` | 24.dp | Large section spacing (e.g. between major blocks). |

For **12.dp** (e.g. between horizontal cards): use `12.dp` until/unless a token is added to `Dimens`. Keep usage consistent (e.g. one named constant per screen or component).

---

## 6. Examples

### a) Screen with Column padding + inner LazyRow (no contentPadding)

Parent applies padding; LazyRow must not add horizontal padding.

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(
        start = Dimens.screenHorizontalPadding,
        end = Dimens.screenHorizontalPadding,
        bottom = 48.dp
    ),
    verticalArrangement = Arrangement.spacedBy(Dimens.spacing16)
) {
    item { HeaderCard(...) }
    item { CalledHistoryPanel(...) }
    item {
        // Carousel: no horizontal contentPadding; parent already insets content
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cards) { Card(...) }
        }
    }
}
```

### b) Screen without Column padding + LazyRow handles contentPadding

No padded parent; list owns horizontal inset.

```kotlin
LazyRow(
    modifier = Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = Dimens.screenHorizontalPadding),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(cards) { Card(...) }
}
```

---

## 7. Common mistakes

- **Spacer / extra start padding before first item:** e.g. `item { Spacer(Modifier.width(16.dp)) }` or `Modifier.padding(start = 16.dp)` on the first item. Removes alignment with other content.
- **Item padding + contentPadding:** e.g. each item has `Modifier.padding(horizontal = 16.dp)` and the list also has `contentPadding(horizontal = 16.dp)` → double padding.
- **Nested padding:** e.g. `Column(Modifier.padding(horizontal = 16.dp))` containing a `LazyRow` with `contentPadding(horizontal = 24.dp)` → first card misaligned and inconsistent with design token.
- **Peek / “page” padding used as screen padding:** Using a visual peek value (e.g. 24.dp) for `contentPadding` when the parent already uses `screenHorizontalPadding` → first card indented too much.
- **Hardcoded values:** Using `16.dp` or `24.dp` instead of `Dimens.screenHorizontalPadding` or `Dimens.spacing*` for screen edges and standard gaps.

---

## 8. Checklist before PR (layout / padding)

- [ ] All horizontal screen insets use `Dimens.screenHorizontalPadding` (no hardcoded 16.dp for screen edges).
- [ ] For each screen/flow, only one level applies horizontal padding (parent **or** list `contentPadding`, not both).
- [ ] LazyRow / LazyColumn: horizontal `contentPadding` is `0.dp` when parent already pads; otherwise list uses `Dimens.screenHorizontalPadding`.
- [ ] No `Spacer(width = ...)` or `Modifier.padding(start = ...)` used to “align” the first item; alignment comes from the single padding source.
- [ ] First card/row in horizontal sliders aligns with other content (headers, panels, buttons) on the same screen.
- [ ] Right side padding is symmetric (same as left unless the design explicitly says otherwise).
- [ ] Spacing between cards/items uses a consistent value (e.g. 12.dp or `Dimens.spacing16`); no random per-item padding that changes alignment.

---

*Reference: LivePlayScreen cards carousel follows Option 1 (LazyColumn contentPadding; BingoSheetsCarousel LazyRow has `contentPadding = PaddingValues(horizontal = 0.dp)`).*
