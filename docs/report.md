# MarketFlow: An Interactive Event-Driven Retail Point-of-Sale System

**Module:** PROG209 — Event Driven Programming  
**Institution:** Limkokwing University of Creative Technology, Sierra Leone  
**Semester:** 5, September 2026–February 2027  
**Student/group names and IDs:** __________________________________  
**Lecturer:** Amandus Benjamin Coker  
**Project repository:** https://github.com/Eager1337/event-driven-project  
**Prepared:** 14 November 2026

## 1. Executive summary
MarketFlow is a Java Swing desktop application for a small retail shop. Staff manage product inventory, create a cart, apply a discount, record cash or card bookkeeping, complete a sale, and retrieve receipts. MySQL persists products, sales and immutable item snapshots. A single database transaction records checkout and reduces stock, preventing partial sales and overselling.

The implementation addresses the supplied brief's Java Swing, event handling, component, layout, inheritance, polymorphism, accessibility, database, documentation and openness requirements. Twenty-five automated tests passed locally against MySQL 8.4.8 with Java 21 compiling to Java 17 bytecode. Screenshots were captured from the actual Swing interface. GitHub collaboration history and individual presentation marks depend on genuine student participation; they cannot be manufactured by this implementation.

## 2. Problem, objectives and boundaries
A small retailer needs consistent sales totals and stock updates without duplicate manual bookkeeping. The project aims to provide a keyboard-operable interface, decimal-safe financial arithmetic, input validation, durable receipts and a maintainable event-driven design.

Retail POS was chosen because it demonstrates a broad range of events and database interactions without collecting sensitive customer medical or banking data. All seeded product names and prices are demonstration data. SLE is the display currency. Discounts range from 0 to 50 percent; taxation is deliberately outside scope. A card sale only records a payment label: it never reads card numbers or contacts a processor.

This is an academic, single-operator application. Authentication, staff roles, refunds, fiscal reporting, audit-grade stock adjustments, encrypted remote deployment and certified assistive-technology support would be required before production use.

## 3. Architecture and separation of concerns
The architecture separates the event-driven view from domain rules and persistence:

- App configures the look and feel and constructs the interface on Swing's event-dispatch thread (EDT).
- MainFrame composes the views and routes user events. SwingWorker executes database and file operations off the EDT. Its done callback updates widgets on the EDT.
- Domain contains validated Product and Line records, a Cart, Payment enumeration, Sale and Receipt records. BigDecimal represents money; totals round using HALF_UP to two decimal places.
- Store owns prepared JDBC queries and the checkout transaction boundary.
- Database loads environment variables or a private local properties file and opens short-lived connections.
- Tables provides an abstract generic TypedModel and concrete product, cart and sales models.

Dependency direction is UI → domain/repository → database. Domain rules do not depend on Swing or JDBC. Store methods can be exercised independently by automated tests. This modularity makes later addition of staff roles, stock movements or alternate persistence implementations possible without replacing the entire UI.

## 4. Event-driven programming and interface
ActionListener handles buttons, archive confirmation, checkout, exports and refresh. DocumentListener responds to product search edits. ItemListener responds to category, stock filters, payment choices and high contrast. ChangeListener responds to discount changes. MouseAdapter supplies a double-click product action. WindowAdapter prevents closing during a transaction and confirms discarding a nonempty cart. InputMap and ActionMap implement keyboard shortcuts.

The interface uses JButton, JTextField, JComboBox, JCheckBox, JRadioButton, JTable, JSpinner, JTabbedPane, JTextArea and JLabel. BorderLayout composes sections, FlowLayout groups toolbar actions, GridLayout builds product forms and payment rows, and JSplitPane separates inventory from the current sale. Selection indexes are converted from sorted view rows to model rows before acting on products.

Checkout flow: select a product → choose quantity → add to cart → choose discount/payment → enter cash tendered → confirm checkout → validate database rows → commit → show receipt → refresh stock/history. Controls are disabled while asynchronous work is in flight to prevent duplicate checkout. Successful checkout clears the cart; failed checkout preserves it for correction.

## 5. Inheritance and polymorphism
TypedModel extends Swing's AbstractTableModel and provides shared row storage, column names and read-only cells. Products, CartLines and Sales extend TypedModel and override getValueAt. JTable calls the overridden methods through the TableModel interface: the same widget renders different business records polymorphically. Products also overrides getColumnClass to support numeric sorting.

MainFrame extends JFrame, demonstrating custom GUI component inheritance. The implementation additionally overrides doInBackground and done in SwingWorker subclasses, event-adapter methods and AbstractAction.actionPerformed. These are operational uses of overriding, not unused demonstration classes.

## 6. Persistence, integrity and concurrency
The InnoDB schema has three related tables: products, sales and sale_items. Products have a unique SKU, decimal price, stock, reorder threshold and active status. Sales store timestamp, subtotal, discount, total, payment, tendered and change. Sale_items references both sales and products and snapshots SKU, product name and unit price. A unique sale/product pair prevents duplicate item rows. Foreign keys and CHECK constraints provide a second layer of validation.

Checkout begins a transaction and locks requested product rows using SELECT FOR UPDATE in product-ID order. The repository verifies active status, current price and available stock before inserting the sale and item snapshots and reducing stock. Any validation or SQL failure causes rollback. Only after every operation succeeds is the transaction committed. Two concurrent checkout attempts cannot both sell more than available stock. Deterministic locking reduces, but does not eliminate, the possibility of deadlocks; an SQL error is surfaced rather than silently retried.

Product edits from the GUI compare original field values in the UPDATE predicate. This optimistic check prevents a stale inventory form from overwriting stock reduced by a sale. Archive preserves referential integrity. Historical receipts read item snapshots rather than current product names or prices.

All input values are passed using PreparedStatement parameters. Connections, statements and result sets use try-with-resources. The desktop uses a restricted database user; Docker binds MySQL only to loopback and persists the database in a named volume. Random local passwords are generated at setup and excluded from Git. Local TLS is disabled for the loopback connection only; remote deployments require verified TLS and a security review.

## 7. Accessibility and digital-public-goods principles
Meaningful accessible names and descriptions are assigned using Swing AccessibleContext. Labels are associated with fields using setLabelFor. The application exposes keyboard shortcuts, button mnemonics, single-selection tables, high contrast and scalable text. Status text communicates outcomes without depending on color alone. Large text can require resizing the window. Screen-reader behavior must still be checked on the student's operating system; automated checks of accessible names do not establish full WCAG conformance.

Original code carries an MIT license, and third-party dependencies retain their own terms. The repository includes setup, usage, contribution, security and testing documentation. No customer personal data is collected, and synthetic seeds avoid publishing real retailer data. Modular code and indexed database fields support future extension. These choices align with openness, inclusion and scalability principles, but no official Digital Public Goods certification is claimed.

## 8. Testing and verified results
Local verification ran 25 tests with zero failures, zero errors and zero skips. Fourteen unit tests cover required fields, SKU normalization, numeric validation, decimal precision, cart merging, quantity bounds, discount rounding, clearing, immutable snapshots, archived products and polymorphic table behavior. Ten MySQL tests cover product persistence, cash checkout and receipt retrieval, rollback for insufficient cash, card totals, stale prices, historical receipt preservation, optimistic-edit protection, empty carts, duplicate SKUs and concurrent overselling prevention.

One end-to-end Swing test fires real search, button and discount events on the EDT, adds a rice product, confirms checkout, verifies persisted totals/history, checks an accessible name and captures five screenshots. Its expected sale is SLE 185.00 subtotal, 10 percent discount, SLE 166.50 total, SLE 200.00 tendered and SLE 33.50 change.

Ordinary mvn verify runs unit tests only, intentionally skipping environment-dependent suites. Full test execution requires explicit enable flags and an isolated marketflow_test database, protecting user inventory. GitHub CI provisions MySQL 8.4 and Xvfb and reproduces the full suite. Windows and macOS launch scripts are provided but were not executed in this Linux sandbox. Physical receipt printing and real screen readers remain manual checks.

## 9. Screenshots and demonstration evidence
The screenshots following this report show the live inventory view, populated cart, saved receipt, sales history and help/accessibility section. They originate from a separate test database; receipt IDs may vary between runs and do not represent a real business transaction. See docs/DEMONSTRATION.md for a reproducible presentation script and docs/TESTING.md for the test environment.

## 10. Conclusion and future work
MarketFlow demonstrates a complete event-driven academic workflow with a usable Swing interface and durable MySQL storage. The strongest design choices are clear separation of UI/domain/persistence, asynchronous database work, exact decimal arithmetic, transactional row locking and historical receipt snapshots. Future work should prioritize authentication and staff permissions, stock movement audit records, refunds, configurable taxes, pagination beyond 1,000 sales, localization and assistive-technology testing.

Before submission, add real student identities, invite the correct lecturer GitHub account, print the report and screenshots, and prepare to explain the event flow and transaction. Follow the university's academic-integrity policy and disclose assistance; do not claim generated code or automation as individual unaided work. The brief's isolated Python-file instruction conflicts with its explicit Java Swing requirements and should be clarified with the lecturer.

## References
- Supplied PROG209 project brief: Event_driven_Project__C__YEF.pdf, two scanned pages, September 2026–February 2027.
- Oracle, Creating a GUI with Swing: https://docs.oracle.com/javase/tutorial/uiswing/
- Oracle, SwingWorker API: https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/SwingWorker.html
- Oracle, BigDecimal API: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigDecimal.html
- MySQL, Connector/J compatibility: https://dev.mysql.com/doc/connector-j/en/connector-j-versions.html
- MySQL official Docker image: https://hub.docker.com/_/mysql
- MIT License: https://opensource.org/license/mit

Implementation evidence: source files, Maven Surefire reports, and generated screenshots. External documentation was consulted on 14 November 2026.