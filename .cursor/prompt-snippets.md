CURSOR_UI_FIX

Use .cursor/rules/project-rule.mdc, .cursor/rules/ui-rule.mdc, and .cursor/repo-map.md as project guidance.

Task:
Fix or improve the UI in the specified composable or screen.

Rules:
- Prefer minimal edits
- Do not rewrite full screens
- Reuse existing components
- Use Color.kt, Dimens.kt, Typography.kt tokens
- Maintain consistent left/right spacing

Output format:

FILES
List files to modify

PLAN
Short explanation

PATCH
Minimal code change only


CURSOR_BUG_FIX

Use .cursor/rules/project-rule.mdc and .cursor/repo-map.md as project guidance.

Task:
Identify and fix the issue in the provided code.

Rules:
- Inspect only relevant files first
- Do not rewrite large files unnecessarily
- Keep the existing architecture
- Prefer minimal patch changes

Output format:

CAUSE
Explain the root problem

FILES
Files to modify

PATCH
Minimal code fix

CURSOR_FEATURE

Use .cursor/rules/project-rule.mdc, .cursor/rules/ui-rule.mdc, and .cursor/repo-map.md.

Task:
Implement the requested feature while following the existing architecture.

Rules:
- Reuse existing ViewModels and repositories
- Reuse shared UI components
- Keep changes localized
- Follow existing navigation patterns

Output format:

FILES
Files to modify

PLAN
Implementation steps

PATCH
Code additions or edits