# Next task

**Goal:** Device QA — Projects tab from BingoBlogs (no 403 diagnostic logs).

**Verify:**
- Projects tab shows the published CMS card (title, Rheinland-Pfalz, €71,680, image).
- Featured slider only if `is_featured` is true; current live item is Recent.
- Card tap opens `source_url` in the browser.
- Pull-to-refresh and airplane-mode still show the last cached list.

**Previous:** Removed 403 bodyPreview/header diagnostics. assembleDebug OK.
