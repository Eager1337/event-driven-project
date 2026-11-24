# MarketFlow — Event-Driven Retail Point-of-Sale (POS) System

**Module:** PROG209 — Event Driven Programming  
**Institution:** Limkokwing University of Creative Technology (Sierra Leone)  
**Semester:** Semester 5 (September 2026 – February 2027)  
**Lecturer:** Amandus Benjamin Coker  

MarketFlow is an interactive, event-driven desktop Point-of-Sale (POS) and inventory management application built using **Java Swing** and **MySQL**. It features real-time inventory searching and filtering, an active shopping cart with live discount calculations, cash and card checkout, itemized receipt generation, paginated transaction history, and universal accessibility features.

---

## Key Features & Architecture
- **Interactive Swing GUI:** Built with `JButton`, `JTextField`, `JComboBox`, `JCheckBox`, `JRadioButton`, `JTable`, `JSpinner`, `JTabbedPane`, `JTextArea`, and `JLabel`.
- **Event Handling:** Comprehensive event listeners including `ActionListener`, `DocumentListener`, `ItemListener`, `ChangeListener`, `MouseAdapter`, and `WindowAdapter`.
- **Multithreading with SwingWorker:** Executes database operations and financial transactions on background threads to ensure the Event Dispatch Thread (EDT) remains responsive.
- **Multi-Layout Management:** Combines `BorderLayout`, `GridLayout`, `FlowLayout`, and `JSplitPane` for responsive and structured user interfaces.
- **Inheritance & Polymorphism:** Custom `MainFrame` extending `JFrame`, abstract `TypedModel<T>` extending `AbstractTableModel`, and polymorphic `JTable` rendering for products, cart lines, and sales history.
- **ACID Database Persistence:** Transactional checkout with row-level locking (`SELECT ... FOR UPDATE`), referential integrity, and keyset pagination in MySQL.
- **Accessibility:** Full keyboard navigation (`Ctrl+F`, `F5`, `Ctrl+Enter`, `Alt+1`, `Alt+2`), mnemonics, accessible descriptions (`AccessibleContext`), font scaling, and high-contrast display mode.

---

## System Requirements
- **Java Development Kit (JDK):** Version 17 or higher
- **Database:** MySQL 8.0+ (via XAMPP or Docker)
- **Build Tool:** Apache Maven 3.9+ (optional if running the bundled JAR)

---

## Quick Start Guide

### Step 1: Database Setup (XAMPP or Docker)

#### Option A: Using XAMPP (Recommended for Local Desktop)
1. Start **Apache** and **MySQL** from the XAMPP Control Panel.
2. Open phpMyAdmin (`http://localhost/phpmyadmin`).
3. Create a new database named `marketflow`.
4. Import the SQL scripts in order:
   - `database/01-schema.sql` (Creates `products`, `sales`, and `sale_items` tables)
   - `database/02-seed.sql` (Populates starter retail inventory)
5. Configure your connection in `config.properties` (or copy from `config.example.properties`):
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/marketflow?sslMode=DISABLED&allowPublicKeyRetrieval=true
   db.user=root
   db.password=
   ```

#### Option B: Using Docker
If you prefer running MySQL in a Docker container:
```bash
docker compose up -d
```
The container binds to port `3307` and initializes the schema automatically.

---

### Step 2: Running the Application

#### Running the Pre-Built JAR
You can run the application directly using:
```bash
java -jar dist/marketflow.jar
```
Or use the convenience launcher scripts:
- **Windows:** Double-click `scripts\run.bat` or run `scripts\run.bat` in Command Prompt.
- **macOS / Linux:** Run `bash scripts/run.sh`.

#### Building and Running from Source (with Maven)
```bash
mvn clean package
java -jar target/marketflow.jar
```

---

## Keyboard Shortcuts
- `Ctrl + F`: Jump to product search field
- `F5`: Refresh product catalog and reset filters
- `Ctrl + Enter`: Complete current checkout transaction
- `Alt + 1`: Switch to Checkout / POS view
- `Alt + 2`: Switch to Sales Transaction History view
- `Tab / Shift + Tab`: Cycle focus between input fields and buttons
- `Space`: Toggle checkboxes and radio buttons

---

## Project Structure
```text
event-driven-project/
├── src/
│   ├── main/java/edu/limkokwing/marketflow/
│   │   ├── App.java            # Main entry point and Look & Feel setup
│   │   ├── Database.java       # JDBC connection manager
│   │   ├── Domain.java         # Business models, cart, and financial rules
│   │   ├── MainFrame.java      # Swing GUI layout, listeners, and SwingWorker
│   │   ├── Store.java          # Prepared SQL and transactional checkout repository
│   │   └── Tables.java         # Polymorphic table models and custom renderers
│   └── test/java/              # Domain unit tests and validation suites
├── database/
│   ├── 01-schema.sql           # MySQL database schema (InnoDB, constraints)
│   └── 02-seed.sql             # Retail product seed catalog
├── dist/
│   └── marketflow.jar          # Executable standalone application JAR
├── docs/
│   ├── Project-Report.docx     # Academic project report (Tahoma 10, line spacing 1.15)
│   ├── Project-Report.pdf      # Ready-to-print report PDF
│   ├── report.md               # Markdown report source
│   └── screenshots/            # System GUI and database demonstration captures
├── scripts/
│   ├── build_report.py         # Report generation script
│   ├── run.bat                 # Windows launcher script
│   └── run.sh                  # Linux / macOS launcher script
├── compose.yaml                # Docker Compose MySQL service definition
├── config.example.properties   # Template database configuration
├── pom.xml                     # Maven project descriptor and dependencies
└── README.md                   # Project overview and setup documentation
```

---

## License
This project is open-source and licensed under the [MIT License](LICENSE).
