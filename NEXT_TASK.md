# Next task

**Goal:** Device QA — Projects local cache (instant load, offline, pull-to-refresh).

**Verify:** Open Projects online once → force-close → reopen (cached list instant). Airplane mode → cached content + soft refresh banner on pull. No cache + offline → full error state. "Last updated" shows after first fetch.

**Done (status):** DataStore cache (`ProjectsCache`); cache-first VM; soft error when API fails with cache; `MainActivity` init.

**Previous:** Projects premium UI + featured slider.
