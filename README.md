# MarketFlow — Java Swing Retail POS

PROG209 Event Driven Programming, Limkokwing University of Creative Technology (Sierra Leone).
A complete academic desktop point-of-sale application: searchable inventory, cart, cash/card bookkeeping, discounts, receipts, sales history and MySQL persistence.

**Repository:** https://github.com/Eager1337/event-driven-project (private — sign in as the owner).

## Start on your PC

Install **JDK 17 or newer**, **Docker Desktop** (Windows/macOS) or Docker Engine with Compose v2 (Linux). Start Docker. Maven 3.9+ is needed only if rebuilding from source; the built bundle includes `dist/marketflow.jar`.

Download the repository ZIP (Code → Download ZIP), or clone:
```sh
git clone https://github.com/Eager1337/event-driven-project.git
cd event-driven-project
```
Extract the ZIP before running any scripts. Avoid running from inside a ZIP viewer.

### Windows
Open PowerShell in the project folder:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\setup.ps1
scripts\run.bat
```
### macOS / Linux
```sh
bash scripts/setup.sh
bash scripts/run.sh
```
Setup generates random local passwords in `.env`, creates matching `config.properties`, starts MySQL on **127.0.0.1:3307**, waits for schema and seed data, and preserves an existing database. Do not share or commit these two credential files. The Docker volume survives restarts. The first setup needs internet to download MySQL.

The run scripts use the included JAR; if it is not available, they build with Maven. You can also run `java -jar dist/marketflow.jar` from the project root after setup.

## Use the application

1. Choose a product in Checkout; select quantity and **Add to cart**. Double-click a product also adds it.
2. Choose a 0–50% discount and Cash or Card. For cash, enter the amount received.
3. **Complete sale** saves the sale, item snapshots and stock reduction atomically. Confirm the amount, then save or print the receipt.
4. **Sales history** shows the latest 1,000 sales. Select a sale to view its receipt or export the loaded history as CSV.
5. **New product / Edit** manage SKU, category, price, stock and reorder threshold. **Archive** hides a product without destroying old receipts. Enable Include archived and Edit to reactivate.
6. **Help & accessibility** offers high contrast, text size controls and keyboard guidance.

Currency is SLE; sample prices are synthetic. No taxes, refunds, authentication, customer records or real card processing are implemented. Card is a recorded payment method, not a financial charge. Do not use this academic app for live financial operations.

**Shortcuts:** Ctrl+F search; F5 refresh; Ctrl+Enter checkout; Alt+1 Checkout; Alt+2 History; Tab/Shift+Tab focus; Space toggles options. Buttons have Alt mnemonics. Platform behavior may differ. Accessible names/descriptions and label associations are set in code; verify your own screen reader before claiming assistive-technology compatibility.

## Build / test
```sh
mvn -B clean verify
java -jar target/marketflow.jar
```
The ordinary build runs 14 unit tests and intentionally skips the database/GUI suites. Full verification requires an **isolated `marketflow_test` database**, never your real inventory database. The CI workflow provisions one, runs all 25 tests under Xvfb, captures screenshots, renders the report and publishes a complete bundle. See [testing evidence](docs/TESTING.md).

Download built files from GitHub → **Actions → Build and package → successful run → marketflow-complete-project**. The workflow also checks in the ready-to-run JAR, report and screenshots. Generated commits made using GitHub's workflow token do not retrigger the workflow. To rebuild manually, use Actions → Run workflow.

## Existing MySQL instead of Docker
Run `database/01-schema.sql` and `database/02-seed.sql` after creating a `marketflow` database. Create a separate application user with SELECT, INSERT, UPDATE and DELETE permissions on that database. Copy `config.example.properties` to `config.properties` and fill in the real connection URL, username and password. Do not commit it. Environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD` override the properties file.

The example URL disables TLS **only for local loopback Docker**. For a remote database use `sslMode=VERIFY_IDENTITY` and a trusted CA; do not expose port 3307 publicly or use root as the desktop user.

## Troubleshooting / data safety

- **Docker not running:** start Docker Desktop and retry setup. On Linux ensure your account has Docker access.
- **Port 3307 already used:** stop the other service or edit Compose and the JDBC URL together.
- **Access denied:** `.env` and `config.properties` must match the credentials used when the volume was first created. Editing `.env` alone does not change MySQL passwords in an existing volume. Restore the original credentials or change the database user password explicitly.
- **Unknown table/database:** inspect `docker compose logs mysql`. Initialization SQL runs only on a new volume. Apply schema manually if you have an older volume.
- **Changed price/stock:** clear the stale cart and refresh. Checkout revalidates every row. Stale inventory edits are rejected to protect checkout stock.
- **No Maven:** use the supplied JAR, or install Maven and reopen the terminal.
- **No graphical desktop:** Swing requires Windows/macOS/Linux desktop; a headless server cannot display it.
- **Check database independently:** `java -jar dist/marketflow.jar --check-db` verifies the private configuration without opening the GUI.
- **Rebuild after editing:** `mvn clean package`; replace `dist/marketflow.jar` with `target/marketflow.jar` or launch the target JAR directly.
- **Stop database:** `docker compose stop`. Start it again using setup or `docker compose up -d --wait`.
- **Back up:** `docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysqldump --no-tablespaces -u marketflow marketflow' > backup.sql` (Bash). Keep backups private. Restore with an administrator-approved account and test database first.
- **Never run `docker compose down -v` unless you intentionally want to erase all database data.**

## Project map
```text
src/main/java/edu/limkokwing/marketflow/
  App.java         startup, look and feel, EDT
  Database.java    local/environment configuration and JDBC connections
  Domain.java      product/cart/money/payment/receipt rules
  Store.java       prepared SQL and transactional checkout
  Tables.java      inherited, polymorphic Swing table models
  MainFrame.java   views, event handlers, accessibility, SwingWorker
src/test/java/...  domain, MySQL, concurrency and GUI tests
database/          schema + synthetic products
scripts/           Windows/macOS/Linux launchers and report builder
docs/              report source, rendered report, demo, tests, screenshots
dist/marketflow.jar ready-to-run executable with JDBC dependencies
compose.yaml       MySQL 8.4 service and persistent volume
.github/           CI workflow and issue templates
```

## Submission
For beginner preparation, start with the [presentation script, code walkthrough and viva study guide](docs/PRESENTATION-STUDY-GUIDE.md). It includes what to say, click and show for each rubric category, plus practice answers and honest limitations.

Read [demonstration guide](docs/DEMONSTRATION.md), [project report](docs/report.md), [rubric mapping](docs/REQUIREMENTS.md), and [contribution guidance](CONTRIBUTING.md). Fill in your real student/group identities in the editable report before printing. No lecturer invitation has been sent: the brief gives an email, not a confirmed GitHub username. Invite the correct account via Settings → Collaborators → Add people after confirming it. Understand and present the code yourself, disclose assistance according to university policy, and do not fabricate group contributions. The brief's “Python file” instruction conflicts with its explicit Java Swing framework requirement; confirm the expected source submission with your lecturer.

## License
Original application code is MIT licensed. JDBC dependencies keep their own licenses; see THIRD_PARTY_NOTICES.md. This is designed with digital-public-goods principles in mind, **not a certified DPG**.