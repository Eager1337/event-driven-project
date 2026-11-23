# MarketFlow: Excellent-band readiness audit

## Outcome
The project now has stronger evidence for the technical rubric categories: working Java Swing/MySQL integration, reusable polymorphic table models, multiple listeners/layouts, indexed paginated history, and **31 passing automated tests**. That supports a credible Excellent-band submission; it does **not** establish that the lecturer will award Excellent in every category.

The main remaining risks are individual explanation, genuine group collaboration, lecturer repository access, and manual accessibility/Windows checks. The user reports their Windows installation now connects and displays saved products/sales; that is user-reported evidence, not a remotely observed Windows test.

## Scope and evidence
This audit uses the supplied `EDP_Group_Project_Rubric_(1)_GRvYDy.pdf`, original assignment, current Java/SQL source, fresh Maven results, new Robot screenshots and official UN/DPGA definitions. No business database or saved Windows records were altered. No fabricated student contributions were added and no user files were deleted.

## Rubric-by-rubric assessment

| Category | Excellent descriptor / band | Project evidence | Readiness and next action |
|---|---|---|---|
| GUI — 15 | Well-designed, user-friendly GUI with multiple components; 12–15 | Ten component types, three tabs, inventory/cart split, visible status and validation, new page status/buttons; fresh screenshots | Strong technical basis. User-friendliness remains a lecturer/user judgement. Check Windows display scaling and long names manually. |
| Event handling — 10 | Robust, efficient handling across multiple components; 9–10 | Action/Document/Item/Change listeners, mouse/window adapters, keyboard actions, busy-state protection, background jobs; GUI event tests | Strong basis. Demonstrate at least four event types and one rejected action, and explain the thread model. |
| Layouts — 10 | Multiple layouts with professional structure; 9–10 | Nested BorderLayout, FlowLayout and GridLayout; JSplitPane; current screenshots | Strong basis. Explain the purpose of each; test the actual projector/display resolution. |
| Inheritance / polymorphism — 15 | Strong reusable OOP design; 12–15 | Abstract generic TypedModel extends AbstractTableModel; three concrete models override cell data/types; JFrame and SwingWorker inheritance; sorting/notification tests | Strong basis. Show runtime dispatch through TableModel, not just the word `extends`. |
| Database — 15 | Fully integrated database, optimized queries and scalability; 12–15 | MySQL CRUD-like product workflow with archive, prepared SQL, transaction, ordered row locks, stale-edit checks, receipt snapshots; primary-key keyset pages and measured EXPLAIN evidence | Improved evidence. No physical product DELETE by design; explain archive. Inventory still loads in memory; no pool, production load study, login/roles or complete UI backup. Do not claim unlimited scaling. |
| DPG alignment — 5 | Multiple DPG principles; 5 | MIT license, modular portable Java source, documentation, privacy minimization, UTF-8 extracts, accessibility; SDG/DPG evidence guide | Several concrete principles. Private repository is not public availability. No formal recognition or compliance certification. Explain limits honestly. |
| Docs / GitHub — 5 | Comprehensive docs, well-structured repository; 5 | Setup/source/tests/report/screenshots/license/CI; study guide, upgrade safety, proposed task allocation and evidence logs | Documentation strengthened. Real member issues/branches/PRs and lecturer access remain unverified and cannot be manufactured. |
| Individual demo — 25 | Confident demonstration and Q&A; 20–25 | Spoken guide, timed mock viva, code drills, failure scenarios, peer scorecard | Not established by code generation. Each student must rehearse, answer without prompting and explain actual contributions. |

## Improvements implemented in this round

1. **History beyond the previous UI limit:** 50-row pages, older/newer navigation, page status and refresh-to-newest. Keyset SQL uses an exclusive ID cursor instead of deep OFFSET scans.
2. **Bounded history queries:** at most 51 rows fetched per page, one for look-ahead. EXPLAIN on 5,000 synthetic headers confirmed primary-key range access. See `docs/PERFORMANCE-EVIDENCE.md`.
3. **Reusable numeric models:** cart quantities/totals and receipt IDs/dates/totals have appropriate column classes; sorting uses numbers and dates, not textual ordering.
4. **Accessibility associations:** quantity, discount and tendered labels now identify their inputs; history status has an accessible name/description. Text scaling preserves the original heading/body proportions and restores base sizes; GUI tests exercise resizing and contrast changes.
5. **Six additional tests:** two unit tests, three database tests and one GUI test. New coverage includes numeric sorting, table notifications, empty/invalid pages, no-duplicate page boundaries, newer inserts, query plans, page navigation and label associations.
6. **Evidence and preparation:** SDG/DPG mapping, genuine contribution template for members 1–5, timed mock-viva practice, safe update instructions and this audit.

The schema is unchanged. No credential regeneration, volume recreation or business-data conversion is required for this update.

## Repository cleanup recommendations — approval required

No files have been removed. The following are recommendations, not actions already taken:

| Candidate | Recommendation | Reason |
|---|---|---|
| `docs/test-results/Event Driven Script.pdf` | Move to a study-material folder after approval, or replace with a freshly rendered guide | This is a user-uploaded earlier guide copy, not a test result; it contains old counts/history wording. |
| `docs/test-results/Project-Report.docx` | Review before removing or moving; preserve any personal edits first | User-uploaded duplicate outside the canonical `docs/Project-Report.docx` location. It may contain work that must not be lost. |
| Local untracked `docs/PRESENTATION-STUDY-GUIDE.pdf` | Keep locally; do not silently publish as current | It predates this round and can disagree with updated Markdown. Not part of the committed new guide. |
| Source, tests, schema, workflows, dependency notices/sources, MIT license | Keep | Required implementation, reproducibility and licensing evidence. |
| Automation history and truthful assistance notes | Keep | Removing origin to imply student authorship would misrepresent contributions. |
| Blank student identities and contribution fields | Fill only with real information | No invented names, IDs, authors or completion claims. |

Generated-file commits are being narrowed to known build outputs rather than staging every workspace file. This reduces accidental publication of unrelated user material.

## Before calling the submission ready

- Each member completes and records genuine assigned work, with actual names/IDs.
- Confirm the lecturer's account and grant the required repository access. Do not publish the private repo merely to look open without authorization.
- Resolve the brief's Java/Python wording discrepancy and verify any separate lecturer instructions.
- Use the upgraded JAR in the original working folder; preserve existing credentials/volumes. See `docs/UPGRADING-SAFELY.md`.
- Test the new page controls, receipt retrieval and exports on Windows with synthetic data. A page CSV exports only that page.
- Test keyboard use, display scaling, text size, contrast and any claimed screen-reader support on the presentation PC.
- Fill the report identities, print the required report/screenshots, and check actual deadline/submission rules.
- Complete at least one timed mock viva per member and revisit every weak code explanation.

## References

- Supplied EDP Group Project Rubric and original PROG209 brief.
- Repository evidence: `docs/TESTING.md`, `docs/PERFORMANCE-EVIDENCE.md`, `docs/REQUIREMENTS.md`, Java and SQL source, Maven XML results.
- Official SDG/DPG definitions and sources are listed in `docs/SDG-DPG-EVIDENCE.md`.

This is a readiness assessment, not an official mark estimate or certification.