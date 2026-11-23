# Group work allocation and evidence log

**Status: proposed assignments, not completed contributions.** Member 1–5 are placeholders requested by the group. Replace them with actual names and student IDs before submission. Do not change “Not yet verified” to “Completed” until there is real work and evidence. Existing automation commits are not student work.

## Proposed responsibilities

| Member | Assigned work to perform | What to produce personally | Status |
|---|---|---|---|
| 1 | GUI and layout review | Review Checkout at normal/large display scale; identify and fix or document a usability issue; explain three layout managers; attach before/after screenshots if changed | Not yet verified |
| 2 | Events and validation | Trace Add to cart and checkout listeners; run synthetic invalid-quantity/insufficient-cash cases; add or review a focused test; explain the event-to-view path | Not yet verified |
| 3 | Database and pagination | Review schema, row locks and keyset query; test newest/older/newer navigation and persistence; explain EXPLAIN evidence; record exact results | Not yet verified |
| 4 | OOP and accessibility | Review TypedModel overrides and numeric column classes; perform keyboard/label/text-size checks; document screen-reader checks honestly | Not yet verified |
| 5 | Documentation, SDG/DPG and integration | Check README/report/source consistency; review SDG claims and limits; organize evidence and a timed mock presentation; record real observations | Not yet verified |

All five members should understand the complete checkout path and answer questions outside their assigned area. Each should also review one other member's work. Allocation is not proof of contribution.

## Genuine GitHub workflow

1. Each member uses their own GitHub account. The repository owner grants access only to confirmed members.
2. Open an issue describing the actual task, acceptance criteria and owner. Suggested titles: “Review GUI at large text”, “Validate event failure cases”, “Verify history pagination”, “Review accessibility and OOP”, “Review submission docs and rehearse”.
3. Create a branch for the real task, such as `member-3/history-review`. Do not create empty branches solely to simulate collaboration.
4. Make a meaningful code, test or documentation change. Commit under the actual author identity with an accurate message.
5. Run relevant tests on the isolated test database; never clear the shop database.
6. Open a pull request with what changed, why, and test/manual evidence. Another member reviews it.
7. Merge when justified, close the real issue and fill the log below with links. Do not rewrite automation history or backdate work.

## Contribution evidence log — fill after doing the work

| Member / actual name and ID | Date | Work actually performed | Issue / branch / commit / PR | Test or review evidence | Reviewer |
|---|---|---|---|---|---|
| 1 / ______ | ______ | ______ | ______ | ______ | ______ |
| 2 / ______ | ______ | ______ | ______ | ______ | ______ |
| 3 / ______ | ______ | ______ | ______ | ______ | ______ |
| 4 / ______ | ______ | ______ | ______ | ______ | ______ |
| 5 / ______ | ______ | ______ | ______ | ______ | ______ |

Testing, debugging, code review and documentation can be genuine contributions if they were actually performed and recorded. Do not invent programming changes because the initial implementation already exists.

## Individual explanation template

“My assigned area was ____. I personally performed ____. The evidence is ____. I changed/reviewed ____ because ____. I tested it using ____. One problem I encountered was ____, and I addressed it by ____. I received assistance with ____, and I can explain ____ myself.”

## Assistance record

The initial implementation and this improvement round were generated with agent assistance. Git history retains that origin. Actual student setup, edits, reviews, testing and learning should be listed separately above. Follow the lecturer's academic-integrity and AI-use policy; do not present generated source as wholly unaided student authorship.