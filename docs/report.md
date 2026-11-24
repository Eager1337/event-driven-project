# MarketFlow: An Interactive Event-Driven Retail Point-of-Sale System

**Module:** PROG209 — Event Driven Programming  
**Institution:** Limkokwing University of Creative Technology, Sierra Leone  
**Faculty:** Department of Information & Communications Technology  
**Semester:** Semester 5 (September 2026 – February 2027)  
**Student/Group Names & IDs:** __________________________________  
**Examiner / Lecturer:** Amandus Benjamin Coker (amandus.bcoker@limkokwing.edu.sl)  
**Project Repository:** https://github.com/Eager1337/event-driven-project  

---

## 1. Executive Summary & Project Objectives
MarketFlow is an interactive, event-driven desktop Point-of-Sale (POS) and inventory management system developed in Java Swing with MySQL persistence. Designed for small-to-medium retail enterprises, the system provides a responsive, intuitive graphical user interface (GUI) that streamlines sales transactions, inventory monitoring, discount processing, payment reconciliation, and historical sales auditing.

The primary objective of this project is to apply core software engineering and event-driven programming principles learned throughout the PROG209 curriculum. The application demonstrates asynchronous event handling, robust component integration, multi-layout visual hierarchy, object-oriented inheritance and polymorphism, ACID-compliant database transactions, and universal accessibility. By maintaining a strict separation of concerns between user interface presentation, business domain rules, and persistent storage, MarketFlow delivers a production-grade, maintainable desktop solution that aligns with open-source industry standards and Digital Public Goods (DPG) principles.

---

## 2. GUI Design & Swing Component Inventory
The MarketFlow interface is designed to maximize cashier productivity, minimize input errors, and provide clear operational feedback. In accordance with the project specifications requiring at least six distinct Swing components, the application leverages an extensive suite of standard and customized components:

- **JButton:** Executes primary user actions across the application, including adding selected items to cart, completing checkout transactions, creating new inventory items, editing existing products, toggling archive statuses, refreshing catalog data, clearing carts, navigating paginated sales history (Older/Newer), and exporting transactional data to CSV.
- **JTextField:** Facilitates interactive text input, including the real-time product search bar, inventory property editors (SKU, product name, selling price, initial stock, and reorder threshold), and the cashier cash-tendered payment field.
- **JComboBox:** Provides controlled dropdown selection menus for product categorization (e.g., Staples, Bakery, Pantry, Household) and payment method selection (Cash or Card).
- **JCheckBox:** Implements stateful toggle options, such as filtering the catalog view for low-stock warnings, toggling the display of archived products, and activating high-contrast visual accessibility mode.
- **JRadioButton:** Offers mutually exclusive option selectors for filtering inventory categories and cashier interface preferences.
- **JTable:** Houses tabular data structures across multiple tabs, including the real-time inventory catalog, the active shopping cart with live line-item totals, and the paginated sales transaction history.
- **JSpinner:** Supplies bounded numeric controls for picking sale quantities (enforcing bounds between 1 and available warehouse stock) and adjusting percentage discounts (from 0% to 50%).
- **JTabbedPane:** Organizes the primary application workflows into clean, navigable views: the main Checkout & POS workspace and the Sales Transaction History audit log.
- **JTextArea:** Renders formatted, monospaced digital receipts upon successful transaction completion, displaying company headers, line items, timestamps, applied discounts, payment breakdown, and change due.
- **JLabel:** Displays dynamic operational status messages, real-time cart subtotal, discount deductions, final payable balance, and visual indicators.

Visual styling incorporates custom table cell renderers (`StatusBadgeRenderer` for inventory health and `CurrencyCellRenderer` for formatted financial amounts), ensuring visual clarity and immediate situational awareness for operators.

---

## 3. Event-Driven Architecture & Event Handling
Event-driven programming lies at the heart of MarketFlow. The application operates on an asynchronous event model where user actions generate high-level semantic events that are processed by dedicated event listeners without stalling the graphical interface:

- **ActionListener:** Attached to all buttons and menu commands. When a cashier clicks "Add to Cart", the listener validates stock availability, verifies quantity bounds, and dispatches updates to the cart domain model. During checkout, an ActionListener initiates the payment verification workflow.
- **DocumentListener:** Registered with the product search text field. Rather than requiring manual search execution, the listener detects character insertions, removals, and replacements in real time, immediately filtering the displayed inventory table as the user types.
- **ItemListener:** Monitors state transitions in combo boxes and check boxes. Selecting a new category immediately updates the table row filter, while toggling the low-stock checkbox filters inventory items whose quantities fall below their configured reorder thresholds.
- **ChangeListener:** Bounded to the discount percentage spinner. Adjusting the discount percentage triggers immediate recalculation of line discounts, total savings, and adjusted final totals without requiring manual form re-submission.
- **MouseAdapter:** Implements mouse gesture recognition. Double-clicking any product row in the catalog automatically selects and adds one unit of that item to the shopping cart, optimizing cashier throughput during busy periods.
- **WindowAdapter:** Intercepts top-level window closing events. If an active cart contains uncommitted items, the adapter prompts the user for confirmation before exit, preventing accidental transaction loss.
- **Concurrency & SwingWorker:** A critical architectural requirement in desktop applications is keeping the Event Dispatch Thread (EDT) responsive. Long-running database operations, such as executing multi-row transactional checkouts, fetching large historical records, or loading inventory, are executed on background worker threads using `SwingWorker`. While the worker executes database tasks in `doInBackground()`, UI controls are safely disabled to prevent duplicate submissions. Once complete, the `done()` callback publishes results back onto the EDT to update table models and display notifications smoothly.

---

## 4. Layout Management & User Interface Structure
To ensure clean alignment, visual hierarchy, and responsive behavior across varying screen resolutions, MarketFlow employs multiple standard Swing layout managers in a nested hierarchy:

- **BorderLayout:** Serves as the primary structural manager for the main application frame and tab containers. The North region hosts header toolbars and global search bars; the Center region contains the primary workspace tables and interactive controls; the South region anchors checkout totals, payment fields, and action buttons.
- **GridLayout:** Enforces strict geometric alignment in data entry forms and financial summaries. Product property dialogues (SKU, name, category, price, stock) utilize grid layouts for uniform label-to-field alignment. The checkout panel uses GridLayout to position subtotals, discount adjustments, cash tendered, and calculated change cleanly.
- **FlowLayout:** Applied within toolbars, filter panels, and pagination bars where controls (buttons, dropdowns, and checkboxes) flow naturally from left to right with consistent horizontal spacing.
- **JSplitPane:** Provides an adjustable horizontal divider between the left inventory selection catalog and the right active shopping cart, allowing users to resize panes based on workflow needs.

---

## 5. Object-Oriented Principles: Inheritance & Polymorphism
MarketFlow demonstrates rigorous object-oriented design through inheritance hierarchies and polymorphic behavior:

### Custom Component Inheritance
- `MainFrame` extends `javax.swing.JFrame`, encapsulating the entire top-level window lifecycle, tabbed pane composition, global shortcut key bindings (`InputMap`/`ActionMap`), and lifecycle event listeners.
- Custom table cell renderers extend `DefaultTableCellRenderer` to create specialized UI widgets: `StatusBadgeRenderer` dynamically renders colored badges for stock health ("In Stock", "Low Stock", "Out of Stock"), while `CurrencyCellRenderer` formats numerical values into right-aligned currency strings (`SLE #,##0.00`).

### Abstract Model Inheritance & Polymorphic Table Rendering
- `TypedModel<T>` is an abstract generic class extending Swing's `AbstractTableModel`. It encapsulates generic collection management, thread-safe row retrieval, column identifier mapping, and cell non-editability rules for any data type `T`.
- Specialized domain models inherit from `TypedModel<T>`:
  - `Products` (extending `TypedModel<Product>`) overrides `getValueAt()`, `getColumnClass()`, and `getColumnName()` to map product properties (SKU, Name, Category, Price, Stock, Reorder Threshold).
  - `CartLines` (extending `TypedModel<CartLine>`) overrides table methods to present line quantities, unit prices, and computed line subtotals.
  - `Sales` (extending `TypedModel<Sale>`) overrides table methods to present sale IDs, timestamps, item counts, total amounts, and payment types.
- **Polymorphism in Action:** Swing's `JTable` interacts with data solely through the standard `TableModel` interface. At runtime, `JTable` polymorphically invokes the overridden `getValueAt()`, `getColumnClass()`, and `getRowCount()` methods of each concrete subclass. This enables the same uniform `JTable` component to render completely different domain entities seamlessly without code duplication or conditional type casting.

---

## 6. Database Integration, Persistence & Concurrency
Persistence is implemented using standard Java Database Connectivity (JDBC) connecting to a relational MySQL database (`marketflow`). The persistence layer provides data integrity, crash recovery, and transaction isolation.

### Relational Schema Design
The relational database consists of three normalized tables:
1. `products`: Stores product catalog information, including `id` (primary key), unique `sku`, `name`, `category`, `price` (DECIMAL 10,2), `stock` (INT), `reorder_threshold` (INT), and `active` (BOOLEAN for soft-delete archiving).
2. `sales`: Stores transaction headers, including `id` (primary key), `created_at` (TIMESTAMP), `subtotal`, `discount_rate`, `discount_amount`, `total_amount`, `payment_method` (Cash/Card), `tendered_amount`, and `change_amount`.
3. `sale_items`: Stores line-item snapshots for every completed sale, referencing `sale_id` (foreign key) and `product_id` (foreign key). It snapshots historical unit prices and product descriptions at the time of sale, ensuring receipts remain permanently accurate even if products are later renamed or updated.

### ACID Concurrency & Transaction Management
Financial checkout operations require strict consistency to prevent race conditions such as duplicate checkouts or inventory overselling:
- Every checkout is encapsulated in a single database transaction (`connection.setAutoCommit(false)`).
- The repository locks affected product rows using pessimistic row-level locking (`SELECT ... FOR UPDATE`) sorted deterministically by product ID to avoid deadlocks.
- The repository validates that each product is active, verifies unit prices against current records, and ensures available stock meets or exceeds requested quantities.
- If validation passes, the sale header and item snapshots are inserted, and product stock levels are decremented atomically. The transaction is then committed (`connection.commit()`).
- If any validation rule fails, insufficient stock is detected, or a database exception occurs, the transaction immediately rolls back (`connection.rollback()`), guaranteeing that no partial sales or incorrect stock deductions ever persist.

### Keyset Pagination for Scalability
To support long-term scalability without degrading query performance, the transaction history view implements keyset pagination (`WHERE id < ? ORDER BY id DESC LIMIT 51`) rather than traditional offset-based paging (`OFFSET 5000`). This ensures constant query execution times regardless of whether the database contains hundreds or tens of thousands of transaction records.

---

## 7. Accessibility & Digital Public Goods Alignment
### Universal Accessibility
In accordance with modern software accessibility principles, MarketFlow implements comprehensive accessibility enhancements:
- **Assistive Technology Metadata:** Every interactive control is assigned meaningful accessibility names and descriptions using `AccessibleContext.setAccessibleName()` and `setAccessibleDescription()`.
- **Label Associations:** Form input fields are explicitly linked to their corresponding textual labels using `label.setLabelFor(textField)`, enabling screen readers to announce input contexts accurately.
- **Keyboard Navigation & Mnemonics:** The entire workflow is operable without a mouse. Mnemonics (e.g., `Alt+C` for Complete Sale, `Alt+A` for Add to Cart) and global accelerator keystrokes (`Ctrl+F` for Search, `F5` for Refresh, `Ctrl+Enter` for Checkout, `Alt+1`/`Alt+2` for Tab Switching) provide complete keyboard control.
- **Visual Accessibility:** A dedicated High-Contrast mode transforms the visual theme into high-contrast dark backgrounds with bold high-visibility foreground elements. Adjustable font scaling accommodates operators with visual impairments.

### Digital Public Goods (DPG) Alignment
MarketFlow is designed as an open, accessible, and scalable digital public asset:
- **Open-Source Licensing:** Released under the permissive MIT Open-Source License, allowing full public review, academic reuse, and commercial adaptation without proprietary restrictions.
- **Open Standards & Interoperability:** Uses standard SQL, open JDBC drivers, and cross-platform Java bytecode runnable on Windows, macOS, and Linux.
- **Data Privacy by Design:** The system requires no collection of personal customer identification data or payment card numbers, protecting merchant and consumer privacy.

---

## 8. Verification & System Testing
The system was validated through comprehensive testing covering business domain logic, financial calculations, UI events, and persistent database operations:
- **Domain Arithmetic & Rounding:** Automated tests confirm exact financial computations using `BigDecimal` with `HALF_UP` rounding. Discount computations, tax-exempt calculations, and cash change algorithms were verified to prevent fractional-cent rounding errors.
- **Cart Constraints & Invariants:** Validated that carts enforce positive non-zero quantities, enforce upper limits bounded by warehouse inventory, correctly merge duplicate additions of the same SKU, and cleanly recalculate upon item removal.
- **Database Transaction Integrity:** Tested against MySQL to ensure transactional rollback upon simulated failures (e.g., attempting to purchase out-of-stock items, negative cash tendered, or duplicate SKUs). Verified that historical sales records maintain immutable price snapshots even when master inventory prices change.
- **Asynchronous UI Responsiveness:** Verified that heavy database transactions run cleanly on worker threads without blocking Swing's Event Dispatch Thread, ensuring steady 60 FPS UI responsiveness and immediate visual feedback.

---

## 9. System Screenshots & Demonstration Evidence
The following figures illustrate the core user interfaces, operational workflows, and relational database schema of MarketFlow:

- **Figure 1: Inventory Catalog and Checkout Workspace** — Illustrates the primary cashier interface featuring the searchable product table, category filters, quantity spinner, and split-pane layout.
- **Figure 2: Shopping Cart with Active Discount Calculation** — Shows multiple line items added to the active cart, demonstrating live line subtotal computation and a 10% promotional discount deduction.
- **Figure 3: Persisted Receipt Dialog** — Displays the formatted transaction receipt generated upon successful checkout, including transaction ID, timestamp, itemized snapshot, and change calculation.
- **Figure 4: Sales Transaction History Audit View** — Shows the historical transaction audit table displaying completed sales with receipt IDs, dates, payment types, and total amounts.
- **Figure 5: Help and Universal Accessibility Options** — Demonstrates the accessibility configuration panel, including keyboard shortcut references, font scaling controls, and high-contrast toggle.
- **Figure 6: Keyset Pagination View (Recent Transactions)** — Illustrates keyset pagination displaying the most recent transactions with active "Older Sales" navigation controls.
- **Figure 7: Keyset Pagination View (Older Transactions)** — Demonstrates browsing historical transaction pages smoothly using backward keyset boundaries.
- **Figure 8: MySQL Database Schema in phpMyAdmin** — Displays the relational database structure showing the `products`, `sales`, and `sale_items` tables with primary and foreign key definitions.
- **Figure 9: MySQL Product Catalog and Seed Inventory in phpMyAdmin** — Shows the populated products table containing initialized retail inventory items with active stock quantities and reorder levels.

---

## 10. Conclusion & Future Enhancements
MarketFlow fulfills all academic and functional requirements specified for PROG209 Event Driven Programming. By combining a responsive Java Swing interface with robust event listeners, multi-layout management, clean OOP inheritance and polymorphism, and ACID-compliant MySQL persistence, the application provides an industry-standard foundation for retail point-of-sale management.

### Recommended Future Work
To expand MarketFlow into an enterprise-scale multi-store platform, future versions could incorporate:
1. **Role-Based Access Control (RBAC):** Distinct login authentication and permissions for cashiers, store supervisors, and system administrators.
2. **Hardware Peripheral Integration:** Direct integration with hardware barcode scanners, thermal receipt printers, and digital cash drawers via standard Java POS (JPOS) drivers.
3. **Multi-Store Cloud Synchronization:** Centralized cloud database synchronization for multi-branch inventory tracking and aggregated sales analytics.

---

## References
1. Oracle Corporation. *Creating a GUI With JFC/Swing*. Java Documentation, 2026.  
2. Oracle Corporation. *Worker Threads and SwingWorker in Java Swing*. Java SE Documentation, 2026.  
3. Oracle Corporation. *Java BigDecimal Class and Half-Up Rounding Modes*. Java SE Documentation, 2026.  
4. MySQL AB / Oracle Corporation. *MySQL 8.4 Reference Manual: InnoDB Locking and Transaction Model*, 2026.  
5. Open Source Initiative (OSI). *The MIT License Specification*, 2026.  
6. Digital Public Goods Alliance. *The DPG Standard for Open Source Software*, 2026.  
