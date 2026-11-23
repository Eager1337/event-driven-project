# Brief-to-implementation mapping

| Requirement / marks | Implementation and evidence |
|---|---|
| Java Swing framework | App, MainFrame, Tables; no web framework replaces Swing |
| GUI design/components — 15 | JButton, JTextField, JComboBox, JCheckBox, JRadioButton, JTable, JSpinner, JTextArea, JTabbedPane, JLabel; actual screenshots |
| Event handling — 10 | ActionListener, DocumentListener, ItemListener, ChangeListener, MouseAdapter, WindowAdapter; InputMap/ActionMap |
| Layout management — 10 | BorderLayout, FlowLayout, GridLayout; JSplitPane |
| Inheritance/polymorphism — 15 | TypedModel extends AbstractTableModel; Products/CartLines/Sales override getValueAt; MainFrame extends JFrame; SwingWorker overrides |
| Database integration — 15 | MySQL 8.4, prepared SQL, products/sales/sale_items, transactions/locks/stale-edit checks; indexed 50-row keyset history pages and measured EXPLAIN on 5,000 isolated synthetic headers |
| Accessibility (core) | AccessibleContext names/descriptions, label associations, keyboard actions, contrast/text controls; no unsupported certification claim |
| DPG alignment — 5 | MIT original code, privacy minimization, extractable text/CSV, documentation, modularity and accessibility; SDG-DPG-EVIDENCE.md maps multiple principles with limits; no formal DPG certification |
| Documentation & GitHub — 5 | README, setup scripts, report, screenshots, CI, contribution guide, issue templates; lecturer invitation remains user action |
| Individual presentation/demonstration — 25 | DEMONSTRATION.md, PRESENTATION-STUDY-GUIDE.md and MOCK-VIVA-PRACTICE.md; students must present and explain their actual work |

See [Excellent-band audit](excellent-audit.report/content.md), [31-test evidence](TESTING.md), [query-plan evidence](PERFORMANCE-EVIDENCE.md) and [proposed group allocation / real evidence log](GROUP-CONTRIBUTIONS.md). No schema change is needed for this round.

**Important:** Code and documents support the rubric but do not guarantee a mark. Group branches, commits and issues must reflect real contributions. Author identities and student IDs have not been invented. The private repository owner is Eager1337. Confirm the lecturer's GitHub account before inviting anyone; their email in the scanned brief is not treated as a verified GitHub username.