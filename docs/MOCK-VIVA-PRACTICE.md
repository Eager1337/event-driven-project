# Timed presentation and mock-viva practice

## Goal
Demonstrate a working system and explain its code confidently, with truthful contributions. This is preparation for the 25-mark individual presentation, not a substitute for your own understanding. Use `PRESENTATION-STUDY-GUIDE.md` for the longer script.

## Ten-minute rehearsal schedule

| Time | Action | Evidence to show |
|---|---|---|
| 0:00–0:45 | State problem, Java Swing/MySQL architecture, real contribution and assistance | Main window and file map |
| 0:45–1:30 | Identify six components and explain three layouts | `checkoutPanel()` |
| 1:30–2:30 | Search, select quantity, add product, change discount | Listener registrations, `addSelected`, `Cart.add` |
| 2:30–4:00 | Show insufficient cash rejection, then confirm one synthetic sale | `completeSale`, `Store.checkout`; correct total/change |
| 4:00–5:00 | Reopen receipt and demonstrate older/newer history if enough sample sales exist | `salesPage`, primary-key cursor, `receipt` |
| 5:00–6:00 | Explain inheritance and runtime method dispatch | `TypedModel`, overridden `getValueAt`, typed columns |
| 6:00–7:00 | Explain transaction, locks and stock integrity | Begin → verify → insert → stock update → commit/rollback |
| 7:00–8:00 | Show keyboard/text/contrast support and SDG/DPG evidence | Accessibility helpers, license and evidence guide |
| 8:00–9:00 | Show test evidence and actual GitHub contributions | 31-test results, real issues/PRs only |
| 9:00–10:00 | Summarize limits and one improvement; invite questions | No real card processing/authentication; real contribution log |

For pagination rehearsal, you need more than 50 **synthetic** sales in a separate demonstration database. Do not fill your business database with fake sales just for screenshots. If you have fewer, explain the code and show the isolated automated test evidence rather than pretending that a disabled Older button is broken.

## Round 1: code-location drill (8 minutes)

Allow 45 seconds per prompt. Open the correct method, point to the decisive lines, then explain the result without reading a paragraph.

1. Where does the program start? → `App.main`, EDT initialization.
2. Where does a button click become an operation? → `MainFrame.button`, supplied Runnable/method reference.
3. Where is quantity checked? → `Domain.Line`/`Cart.add`, then database recheck in checkout.
4. Where is polymorphism used? → `Tables` concrete model overrides through JTable's model API.
5. Where is partial checkout prevented? → `Store.checkout`, transaction/rollback.
6. Where are database credentials selected? → `Database.configured`; env overrides local file. Never display password contents.
7. Where is deep history made efficient? → `salesPage`, `WHERE id < ?`, `ORDER BY id DESC`, bounded `LIMIT`.
8. Where are accessible labels associated? → `labelFor`, `addField`, `setLabelFor`.

## Round 2: “why?” questions (10 minutes)

Use 30–60 seconds per answer. The expected points are prompts, not text to memorize.

| Lecturer question | Expected points |
|---|---|
| Why do you recheck stock at checkout? | Cart is a snapshot; inventory can change; database truth is checked under a lock. |
| Why not use OFFSET for older pages? | Deep OFFSET scans/skips preceding rows; ID range cursor uses the primary-key index and bounded page reads. |
| What happens if a new sale is inserted while browsing? | Older queries keep the exclusive existing ID boundary; refresh returns to newest. This is not a frozen transaction-wide browsing snapshot. |
| What does LIMIT 51 mean for a 50-row page? | One look-ahead row determines whether an older page exists; only 50 are displayed. |
| Is a sorted receipt table the same as chronological history retrieval? | SQL retrieves pages by descending receipt ID; clicking headers sorts the loaded page only. IDs are not a guaranteed global commit-time ordering. |
| Why declare numeric column types? | Sorter compares numbers as numbers rather than text, so 2 sorts before 10. |
| Why keep the cart when checkout fails? | Operator can correct the problem; unsuccessful persistence must not erase the unfinished selection. |
| Why archive instead of DELETE? | Keep referenced products/history, maintain availability through active status; logical deletion. |
| Can your CSV restore the entire database? | No; only current page sale summaries are exported. Full backup requires MySQL export/restore. |
| Does EXPLAIN prove the system works for a million users? | No; this measures one query plan on 5,000 synthetic headers, not overall throughput. |
| How does this relate to SDG 10? | Accessibility features can reduce interface barriers; no inequality impact study was done. |
| Who wrote this code? | Explain agent assistance and each member's genuine work using the log; do not claim imaginary authorship. |

## Round 3: safe failure scenarios (8 minutes)

Use synthetic data only. Do not stop the database while a real sale is in progress.

- Empty cart → attempt checkout; explain rejection before persistence.
- Quantity larger than stock → explain domain rejection; ensure no saved stock change.
- Insufficient cash → explain rejection and unchanged history/stock.
- Duplicate synthetic SKU → explain unique database constraint and error message.
- Cancel checkout confirmation → show no sale saved.
- Archived synthetic product → unavailable for sale; historic receipt survives.

Only practise connection failures in a separate test environment. Do not edit your working credentials, delete volumes or recreate the business database to produce an error for demonstration.

## Peer-review scorecard (practice only)

Rate each item 0–2: 0 = cannot show/explain; 1 = needs prompting; 2 = independently demonstrates and explains.

| Item | Score | Feedback / next action |
|---|---|---|
| Working app and persistent receipt | ____ / 2 | ____ |
| Components and layouts | ____ / 2 | ____ |
| Complete event-to-result trace | ____ / 2 | ____ |
| Inheritance versus polymorphism | ____ / 2 | ____ |
| Transaction and stock consistency | ____ / 2 | ____ |
| Pagination and its limitations | ____ / 2 | ____ |
| Accessibility and SDG/DPG evidence | ____ / 2 | ____ |
| Genuine contributions and assistance | ____ / 2 | ____ |
| Responds to unexpected “why?” question | ____ / 2 | ____ |
| Keeps to time without reading everything | ____ / 2 | ____ |

This 20-point rehearsal score is not the lecturer's 25-point grading scheme. Use low-scoring items to plan more study.

## Rehearsal log

| Member | Date / duration | Questions missed | Technical problems | Action before next rehearsal | Peer reviewer |
|---|---|---|---|---|---|
| 1 | ____ | ____ | ____ | ____ | ____ |
| 2 | ____ | ____ | ____ | ____ | ____ |
| 3 | ____ | ____ | ____ | ____ | ____ |
| 4 | ____ | ____ | ____ | ____ | ____ |
| 5 | ____ | ____ | ____ | ____ | ____ |