# MarketFlow: presentation script and code study guide

Use this guide to understand and rehearse, not to claim work you did not do. Replace bracketed details with truthful information. Follow your lecturer's rules on AI assistance. Marks depend on your explanation and the lecturer's assessment; this guide cannot guarantee a pass.

## 1. Before presenting

- Open the **whole project folder** in VS Code or your preferred editor. Source is in `src/main/java/edu/limkokwing/marketflow/`.
- Open `App.java`, `MainFrame.java`, `Domain.java`, `Tables.java`, `Database.java`, `Store.java` and `database/01-schema.sql` in separate tabs.
- Start the application from the project root using `scripts\run.bat`. This runs the packaged JAR; it does not automatically compile source edits. Use `mvn clean package` before demonstrating changed Java code.
- Confirm inventory and sales load successfully. An empty screen with an access-denied error is **not** a working database demo. Resolve the current PC login issue before your presentation.
- Preserve existing data: never use `docker compose down -v`, delete either MySQL volume, or point automated tests at your business database. Make a private database backup before repairs or migration.
- Use synthetic demonstration products. Completing checkout creates a real saved sale and reduces stock; do not use important business stock for rehearsal.
- Keep `docs/Project-Report.pdf`, `docs/TESTING.md`, the repository and this guide ready. Fill in actual names and contributions in the report. Lecturer invitation remains a separate action.
- Docker and WSL are optional setup choices, not rubric categories. MySQL/Supabase persistence is graded. VS Code is an editor, not the database or application framework.

## 2. Your 10–12-minute spoken demonstration

Speak naturally. The quoted passages are suggested wording, not claims that you personally authored everything. If given only eight minutes, shorten the overview and show only one example of each concept.

### A. Introduce the problem — 45 seconds

**Show:** Main application, Checkout tab.

**Say:** “Good [morning/afternoon]. I am [name], student ID [ID]. This project is MarketFlow, a desktop point-of-sale application for a small shop. It helps an operator manage products, calculate a sale, reduce stock and retrieve historical receipts. It is written in Java using Swing, and it stores its records in MySQL.”

“My actual contribution was [describe only work you genuinely completed]. I received [describe assistance according to university policy]. During preparation I learned [give something you can explain and demonstrate].”

**Understand:** The saved product inventory and sales are in MySQL. The current unfinished cart is in application memory; it is not automatically saved when you close the app.

### B. Show GUI components and layouts — 1 minute

**Show:** Product table, search, quantity, discount, payment options and tabs. Open `MainFrame.checkoutPanel()`.

**Say:** “The interface uses JTable for inventory and cart rows, JTextField for search and cash received, JButton for actions, JComboBox for categories, JSpinner for quantity and discount, JRadioButton for payment, JCheckBox for options, and JTabbedPane for sections. These are more than the six components required in the original brief.”

“BorderLayout divides larger areas into regions, FlowLayout arranges short rows of controls, and GridLayout gives forms and payment sections a regular structure. Nested panels let different parts of the interface use the most suitable layout.”

**Point to:** `new BorderLayout(8,8)`, `new FlowLayout(FlowLayout.LEFT,8,0)` and `new GridLayout(0,1,4,8)`.

### C. Demonstrate events — 1 minute

**Do:** Search for a demo product, change category, select its row, choose quantity 1 and click Add to cart. Change discount.

**Say:** “This is event-driven programming: the application responds to user actions through listeners. Typing triggers a DocumentListener that calls filter. Selecting a category triggers an ItemListener. Clicking Add to cart triggers an ActionListener, which calls addSelected. Changing the discount triggers a ChangeListener and updates the totals.”

**Show:** The listeners in the `MainFrame` constructor, the button helper `button(...)`, and `addSelected()`.

**Understand:** Search filters already-loaded rows with a `TableRowSorter`; it does not run a SQL query on every keystroke. Refresh loads database data again.

### D. Validate and complete a sale — 2 minutes

**Do:** Use one synthetic item priced SLE 185.00, with available stock. Set discount 10%. Enter SLE 100.00 and attempt checkout. Show rejection, then enter SLE 200.00 and confirm checkout. Expected total: SLE 166.50; change: SLE 33.50. If using another price, calculate and rehearse its totals first.

**Say:** “Validation prevents an empty checkout, invalid quantities and insufficient cash. Domain rules also reject unavailable products and invalid monetary values. Money uses BigDecimal with explicit rounding rather than double.”

“After confirmation, checkout checks the current database stock and price. It saves the sale and its line items, updates stock, and commits these changes together. Only successful checkout clears the cart and displays the saved receipt.”

**Show:** `MainFrame.completeSale()`, `Domain.Cart.total()` and `Store.checkout()`.

**Understand:** Adding to cart alone does not reserve or reduce database stock. Checkout validates again because another operation may have changed inventory since the cart was created.

### E. Prove persistence and database structure — 1 minute

**Do:** Open Sales history and retrieve the saved receipt. Close and reopen the app, then show the same sale again.

**Say:** “The sale remains after reopening because it was saved in MySQL, not just in a Java variable. The database has products, sales and sale_items tables. Each sale can have multiple line items. Foreign keys maintain their relationships.”

“The receipt stores historical SKU, product name and unit price. Editing the current product later does not rewrite the old receipt.”

**Show:** `database/01-schema.sql`, `Store.sales()` and `Store.receipt(long id)`.

**Understand:** Closing and reopening proves persistence without restarting MySQL. Docker's named volume preserves MySQL files across ordinary container restarts; deleting that volume is different and destructive.

### F. Explain inheritance and polymorphism — 1 minute

**Show:** `Tables.java`.

**Say:** “TypedModel extends Swing's AbstractTableModel and supplies shared row and column behaviour. Products, CartLines and Sales extend TypedModel and provide different getValueAt implementations.”

“This demonstrates inheritance because subclasses reuse the shared model behaviour. It demonstrates polymorphism because JTable works through the TableModel interface, while the actual model object's overridden methods provide the correct cell data.”

**Understand:** `@Override` confirms that a method replaces an inherited method. These are custom table models, not custom-painted table widgets. `MainFrame extends JFrame` is another inheritance example, but the reusable table models are the stronger design example.

### G. Manage a product — 1 minute

**Do:** Create a synthetic product with a unique SKU, edit it, archive it, tick Include archived, and edit it to make it active again. Avoid changing real records.

**Say:** “Product management supports creating, reading and updating products. Instead of physically deleting products, the application archives them by setting active to false. This removes them from normal sale availability while preserving history.”

“Edits include the original field values in the update condition. If the record changed since it was opened, the save is rejected instead of silently overwriting newer data.”

**Show:** `MainFrame.editProduct()`, `Store.save(Product, Product)` and `Store.archive(long id)`.

**Understand:** Archiving is a logical deletion, not a SQL DELETE operation. Explain that choice honestly if asked about CRUD.

### H. Accessibility and openness — 1 minute

**Do:** Press Ctrl+F, use Tab navigation, and show text-size and high-contrast options on Help & accessibility.

**Say:** “Accessible names and descriptions identify controls, and setLabelFor connects labels to inputs. Keyboard shortcuts and text-size options help users with different interaction needs.”

“The source has an MIT license and a modular structure, supporting openness and reuse. These are examples of alignment with DPG principles; I am not claiming formal DPG certification or fully tested screen-reader compatibility.”

**Show:** `MainFrame.accessible(...)`, `addField(...)`, the help controls and `LICENSE`.

**Understand:** The repository is currently private. An open-source license permits reuse when source is made available; private GitHub access is not the same as public discoverability. Follow the lecturer's repository-access requirements.

### I. Tests, limits and conclusion — 1 minute

**Show:** `docs/TESTING.md`, test result files, README and GitHub workflow.

**Say:** “The recorded full test run passed 25 tests: 14 domain tests, 10 database tests and one multi-step GUI test. Database and GUI tests require explicit settings and an isolated test database; an ordinary Maven run does not run all 25 automatically.”

“The project includes documentation, setup instructions and a build workflow. Actual teamwork must be demonstrated by genuine contributions, branches and issues, not invented activity.”

“Limitations include no user authentication, tax or refund module. Card payments only record a payment method; they do not charge a real card. Future improvements could include access control, a larger history search and database connection pooling.”

“The main lesson I can demonstrate is how a user event passes through validation, database work and a screen update. Thank you; I am ready for questions.”

## 3. Code walkthrough: understand the whole path

All Java paths below are relative to `src/main/java/edu/limkokwing/marketflow/`. Search for the method name using Ctrl+F rather than relying on line numbers, which can change.

### File responsibilities

| File | Responsibility | Simple explanation |
|---|---|---|
| `App.java` | Entry point and Swing startup | Starts the program and opens its window |
| `MainFrame.java` | Components, listeners and UI workflow | Responds to the operator and displays results |
| `Domain.java` | Product/cart/receipt data and rules | Defines valid data and calculates money |
| `Tables.java` | Reusable table models | Tells each JTable what data belongs in its cells |
| `Database.java` | Configuration and JDBC connections | Connects Java to the selected MySQL database |
| `Store.java` | Prepared SQL and transactions | Reads and saves persistent records |

This separation is modular design, not a claim that the app uses a full MVC framework.

### 3.1 Startup: `App.main()`

```java
SwingUtilities.invokeLater(()->{
    try {
        configureUI();
        MainFrame frame=new MainFrame(new Store(Database.configured()));frame.setVisible(true);
    }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"MarketFlow setup required",JOptionPane.ERROR_MESSAGE);}
});
```

- `main` is the Java entry point.
- `invokeLater` schedules window creation on Swing's Event Dispatch Thread (EDT).
- `Database.configured()` reads connection settings; `Store` uses them for SQL operations.
- `MainFrame` receives that Store and constructs the interface.
- `setVisible(true)` shows the window. Configuration failures show a dialog.

**Say:** “The GUI starts on the EDT because Swing components should normally be created and updated on that thread.”

### 3.2 Button → action: `button(...)` and `addSelected()`

Exact excerpt from the button helper:

```java
button.addActionListener(e->{if(!busy)action.run();});
```

The Add to cart control supplies `this::addSelected` as that action. A method reference names the method to run when the event occurs; it does not run that method while constructing the button.

```java
cart.add(p,(int)quantity.getValue());
```

This excerpt from `addSelected()` passes the selected product and quantity to the Cart. The method then updates the table and totals. Exceptions become a visible error message.

`selectedProduct()` uses `convertRowIndexToModel(r)`: the displayed row can move after sorting, so it must be translated to the underlying model row before retrieving the Product.

**Say:** “The listener connects the click to a method; the domain object validates the requested cart change; then the view refreshes.”

### 3.3 Live search: constructor → `filter()`

```java
public void insertUpdate(DocumentEvent e){filter();}
```

The constructor also handles removal and document changes. `filter()` reads the search text and selected category, then sets a RowFilter on the sorter. Matching rows remain visible; nonmatching rows are hidden. The saved products are not deleted or changed.

### 3.4 Money: `Domain.Cart.total()`

```java
return subtotal().multiply(BigDecimal.valueOf(100L - discountPercent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
```

- A 10% discount leaves 90% to pay.
- Multiply the subtotal by 90, then divide by 100.
- Keep two decimal places with HALF_UP rounding.
- A preceding check restricts discount to 0–50%.

**Example:** 185.00 × 90 ÷ 100 = 166.50. Cash change is 200.00 − 166.50 = 33.50.

### 3.5 OOP: `Tables.TypedModel` and its subclasses

```java
public abstract static class TypedModel<T> extends AbstractTableModel
```

This declaration inherits Swing table behaviour. `T` is a generic type parameter: Products uses Product rows, CartLines uses Line rows, and Sales uses Sale rows. The shared model provides row count, column count, column names, noneditable cells and row replacement.

```java
public void setRows(List<T> rows) { this.rows = List.copyOf(rows); fireTableDataChanged(); }
```

The model copies the supplied list and notifies Swing that the displayed data changed. This is not a database save.

**Polymorphism example:** Each concrete model overrides `getValueAt(int r,int c)`. JTable asks for a cell through the common model API, and Java dispatches to the actual object's implementation. Products returns product fields; Sales returns receipt fields.

### 3.6 Configuration: `Database.configured()` and `open()`

```java
public Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }
```

The JDBC driver connects using the selected URL, username and password. Configuration is read from root `config.properties`; environment variables `DB_URL`, `DB_USER` and `DB_PASSWORD` override file settings. Passwords are not embedded in source or committed to GitHub.

**Explain the current problem honestly:** “The PC currently has a database authentication mismatch. Access denied indicates login failure; it does not prove that saved rows have disappeared. Opening the same code in another editor does not repair credentials.” Do not say the issue is fixed until the app actually loads saved data.

### 3.7 Saving safely: `Store.checkout()`

Exact key excerpts, separated here for study; open the full method to see the intervening logic:

```java
c.setAutoCommit(false);
```

```sql
SELECT * FROM products WHERE id=? FOR UPDATE
```

```java
Sale sale = sale(c,id); c.commit(); return new Receipt(sale, List.copyOf(verified));
```

```java
} catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
```

**Explain the sequence:**
1. Begin a transaction by disabling automatic commits.
2. Sort requested items by product ID and lock their database rows.
3. Check the product still exists, is active, has enough stock, and still has the expected price.
4. Recalculate totals using current verified records and validate payment.
5. Insert one sales row and obtain its generated receipt ID.
6. Insert sale_items snapshots and reduce product stock.
7. Commit only after everything succeeds. On a handled SQL/runtime failure, roll back and report the error.

The `?` is a prepared-statement parameter. `setLong`, `setInt` and `setBigDecimal` bind values separately from SQL text. This avoids building queries by concatenating user input.

**Concurrency:** Row locks prevent two concurrent checkouts from both using the same old stock value. Sorting lock order reduces a common deadlock risk; it is not a promise that every possible deadlock is impossible. This remains an academic single-operator UI, not a complete production multi-terminal system.

### 3.8 Keep the UI responsive: `MainFrame.work(...)`

```java
protected T doInBackground()throws Exception{return job.call();}
```

This SwingWorker method executes the supplied database/file job off the EDT. Its `done()` method runs on the EDT, retrieves the result with `get()`, restores controls, and invokes the success callback or shows an error. The busy flag discourages overlapping actions in this window; it is not a substitute for database transactions.

**Say:** “Slow database work happens in a background worker, while Swing updates happen on the UI thread.”

### 3.9 Accessibility: `accessible(...)` and `addField(...)`

```java
c.getAccessibleContext().setAccessibleName(name);
c.getAccessibleContext().setAccessibleDescription(description);
```

```java
label.setLabelFor(field);
```

Names identify controls, descriptions explain their purpose, and label associations identify the corresponding input. These use Swing accessibility APIs. Actual assistive-technology behaviour must still be tested on the presentation PC.

## 4. Practice viva questions and model answers

Answer first in one sentence, then point to the relevant code. Practise without looking at the answer.

| Question | Answer to understand, not memorize |
|---|---|
| What is event-driven programming? | Program behaviour is triggered by events such as clicks, typing and selection changes; listeners respond to them. |
| What is an event source? | The object producing an event, such as the Add to cart JButton. |
| What is a listener? | Code registered to receive a type of event and perform a response. |
| Name four listeners here. | ActionListener, DocumentListener, ItemListener and ChangeListener; the app also uses mouse and window listeners. |
| Why not one layout for everything? | Different interface regions need different arrangements; nested panels combine suitable managers. |
| What is inheritance? | A subclass reuses or extends behaviour from a superclass; TypedModel extends AbstractTableModel. |
| What is polymorphism here? | Different model objects provide different getValueAt behaviour through the same TableModel API. |
| What is encapsulation here? | Cart keeps its mutable map private and exposes controlled add/remove/clear operations and copied list snapshots. |
| What is abstraction here? | JTable uses the table-model API without needing to know how each model stores and returns its rows. |
| What is a record? | A Java declaration for a data carrier with generated accessors, equality and related methods; Product adds validation in its constructor. |
| Does adding to cart save a sale? | No. It changes an in-memory cart; confirmed successful checkout persists the sale and updates stock. |
| Why validate both cart and checkout? | Immediate checks guide the user, while database checks catch stock or price changes since the cart was created. |
| Why BigDecimal? | Money needs decimal arithmetic and controlled rounding, avoiding binary floating-point surprises. |
| Why a transaction? | Sale, line items and stock changes must succeed as one unit or be rolled back together. |
| What does FOR UPDATE do? | It locks selected product rows for the transaction so competing updates cannot freely use stale stock. |
| What happens if cash is too small? | Checkout is rejected; no successful sale is committed. |
| What if the product price changed? | Checkout rejects the stale cart price and asks the operator to clear/refresh. |
| What is a primary key? | A unique row identifier; products and sales use IDs. |
| What is a foreign key? | A constraint connecting a referenced row, such as sale_items.sale_id to sales.id. |
| Why store old item names and prices? | Historical receipts must retain what was sold, even after inventory edits. |
| Where is CRUD? | Product creation, listing and editing are implemented; archiving is logical deletion rather than physical DELETE. |
| Is CSV export a full database backup? | No. It exports the loaded latest sales summary, not all product and item tables or database configuration. |
| Why SwingWorker? | To avoid freezing the GUI while database or file work runs. |
| What if the database fails? | The operation reports an error; checkout rolls back handled failures. Credentials/connectivity need repair, not deletion of saved data. |
| What does MIT provide? | Permission to use, modify and distribute the software subject to the license notice, with its warranty disclaimer. |
| Is this a certified DPG? | No. It demonstrates alignment with openness and inclusion principles, not formal certification. |
| Is card payment real? | No. CARD is recorded bookkeeping; there is no payment-gateway integration. |
| Did all tests run on Windows? | No. Recorded full tests ran on Linux/MySQL and GitHub CI; Windows execution and physical printing require separate checks. |
| Does the JAR show source changes immediately? | No. Rebuild the Java project and launch the newly built JAR. |
| Did you write all this independently? | Give a truthful account of assistance, your own changes, setup, study and testing, following university policy. |
| What would you improve? | Explain one realistic improvement such as authentication, pagination or refunds, and what code/data changes it would require. |

## 5. Rubric evidence and remaining responsibilities

| Criterion | Evidence to show | What still depends on you |
|---|---|---|
| GUI — 15 | Working interface and multiple Swing components | Clear explanation and usable live demo |
| Events — 10 | Listener registrations and observed responses | Trace one complete action in code |
| Layouts — 10 | BorderLayout, FlowLayout and GridLayout | Explain why each was chosen |
| OOP — 15 | TypedModel inheritance and concrete overrides | Distinguish inheritance from polymorphism |
| Database — 15 | Schema, prepared SQL, transaction and saved history | Restore PC login and prove persistence; scalability is bounded, not unlimited |
| DPG — 5 | License, accessibility and modular source | Explain concrete alignment without claiming certification |
| Docs/GitHub — 5 | README, report, tests, workflow and contribution guide | Real collaboration evidence and lecturer repository access |
| Individual demo — 25 | Script, code tour and Q&A practice | Your confidence, understanding and truthful account of contributions |

The rubric's highest database band mentions optimized queries and scalability. This project demonstrates prepared queries, bounded history retrieval and transaction correctness, but does not include connection pooling, a large-scale load benchmark or an unlimited history browser. Discuss these as limits rather than claiming complete production scalability.

## 6. Five rehearsal sessions

1. **Use the app:** practise synthetic product management and one sale; explain what persists and what does not.
2. **Trace one event:** without a script, follow Add to cart → button helper → addSelected → Cart.add → updateCart → table notification.
3. **Trace checkout:** identify validation, worker, row locks, inserts, stock update, commit and receipt display.
4. **Explain the rubric:** locate one concrete code example for every technical category. Answer the table of viva questions aloud.
5. **Mock presentation:** time yourself, reopen the app to prove persistence, and ask a friend to interrupt with “why?” questions.

If you cannot explain a section yet, say so during study and revisit that method. Do not replace understanding with memorizing words.

## 7. Pocket checklist for presentation day

- [ ] Correct project folder and updated study guide are open.
- [ ] Database login works; saved products and history load.
- [ ] Synthetic demo item has known price and sufficient stock.
- [ ] I can locate all six main Java files and the schema.
- [ ] I can explain an event, a layout, an override and a transaction.
- [ ] I can state real contributions, assistance and limitations.
- [ ] Report identities are filled in; lecturer has required GitHub access.
- [ ] I have checked the Java/Python wording inconsistency with the lecturer.
- [ ] I know the actual deadline and any separately stated editor requirements.

## Sources and scope

This guide was checked against the repository's Java source, SQL schema, `docs/DEMONSTRATION.md`, `docs/TESTING.md`, the original assignment and the supplied EDP Group Project Rubric. The code excerpts above are taken from the implementation; short excerpts are labelled and are not standalone runnable programs. It adds documentation only and does not repair the user's Windows database credentials or alter saved records.