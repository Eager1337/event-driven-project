# Individual demonstration guide (8–10 minutes)

Before presenting: run setup, open the application, verify MySQL is available, keep the repository and printed report ready. Use synthetic products only. Fill in your actual identities and contribution details. Do not invent a team history.

1. **Problem and architecture (1 minute).** Explain how a small shop records sales and keeps stock consistent. Point to App, MainFrame, Domain, Store, Database and Tables.
2. **Inventory events (1 minute).** Type rice into search (DocumentListener); change category (ItemListener); select quantity; Add to cart (ActionListener). Explain sorted view-to-model row conversion.
3. **Validation and checkout (2 minutes).** Add one rice pack at SLE 185.00. Choose 10% discount. Enter SLE 100.00 cash and show that checkout is rejected. Correct to SLE 200.00, confirm sale and show SLE 166.50 total / 33.50 change. Note stock decreases by one only after commit.
4. **Persistence (1 minute).** Open Sales history and the receipt. Close/reopen the app and show records remain. Explain the persistent MySQL volume and immutable item snapshots.
5. **Product management (1 minute).** Create DEMO-001 with a unique SKU, valid price and stock; edit it; archive it; enable Include archived and reactivate via Edit. Never delete real inventory data during presentation.
6. **Inheritance and concurrency (1 minute).** Open Tables.java. Show TypedModel extends AbstractTableModel and concrete overrides. Open Store.checkout: row locks → validate → insert → stock update → commit/rollback. Explain why SwingWorker keeps the UI responsive and why edits use original values.
7. **Accessibility (1 minute).** Demonstrate Ctrl+F, Tab navigation, Alt tabs, text size and high contrast. Show setAccessibleName / setAccessibleDescription and setLabelFor in MainFrame. Distinguish implemented support from untested screen-reader compatibility.
8. **Evidence and limits (1 minute).** Show 25-test results, screenshots, MIT license and GitHub workflow. State card payments are simulated bookkeeping, not real charges; taxes, refunds and authentication are out of scope.

## Likely questions
- Why BigDecimal? Exact decimal representation avoids binary floating-point money errors; totals use explicit two-decimal rounding.
- Why a transaction? A receipt and its stock reductions must succeed together or both roll back.
- Why SELECT FOR UPDATE? Competing sales must serialize access to the same inventory rows.
- Why archive rather than delete? Historical sale-item foreign keys and snapshots must remain valid.
- Where is polymorphism? JTable calls the appropriate getValueAt override through the shared TableModel interface.
- What if a price changes after cart creation? Checkout rejects the stale price and asks the operator to refresh.
- What is a DPG claim here? Alignment with openness and inclusion principles, not formal certification.

## Final submission checklist
- [ ] Insert real names, IDs and genuine contribution details into the report.
- [ ] Confirm the lecturer's source-language expectation because the brief mentions both Java and Python.
- [ ] Invite the lecturer's confirmed GitHub account as collaborator.
- [ ] Keep the repository private unless the lecturer explicitly requires public access.
- [ ] Print the Tahoma 10 / justified / 1.15-spacing report and screenshot appendix.
- [ ] Run the demonstration on your own PC and disclose assistance per university policy.
- [ ] Submit by the lecturer's actual Week 11 deadline; no exact calendar deadline is stated in the brief.