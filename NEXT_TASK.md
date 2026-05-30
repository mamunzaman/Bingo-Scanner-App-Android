# Next task

**Goal:** Device QA — Login visual layout + scroll-to-submit on real device.

**Verify:** Login keyboard closed — compact wave behind button/footer, no blank area below footer. Password focused — drag up → Login button reachable. Other form screens unchanged.

**Done (status):** LoginScreen restored from git `6f91d05` layout (single Box + wave + fillMaxSize column + weight spacer). Minimal add: `scrollableContent = true`, `heightIn(min)` preserves weight layout in scroll, trailing `AppImeFormScrollBottomSpacer` outside Box.

**Previous:** Split Login fields/wave sections — reverted.
