# Verification evidence

## Local full run — 14 November 2026
Environment: Debian Linux, OpenJDK 21.0.13, Maven, **MySQL Community Server 8.4.8**, Connector/J 26.10.0, Xvfb 1440×1000. Compiler target: Java 17. MySQL was run as a native binary in the sandbox; Docker Compose itself and Windows/macOS scripts were not executed locally.

Result: **25 tests passed; 0 failures; 0 errors; 0 skips.**
- DomainTest: 14 unit tests.
- StoreTest: 10 real MySQL persistence/transaction/concurrency tests.
- GuiTest: 1 multi-step Swing checkout, accessibility-name check and five Robot screenshots.

Maven XML/text results are copied into docs/test-results/ in the completed bundle. GitHub Actions performs an independent MySQL 8.4 service-container run; use its actual run status rather than assuming a pass.

## Reproduce
Ordinary `mvn clean verify` requires no database and runs only the 14 unit tests; environment-dependent suites are skipped by design.

Full tests use a separate **marketflow_test** database. Create it with an administrator, apply database/01-schema.sql after replacing `USE marketflow;` with `USE marketflow_test;`, and grant a test user access to that database. Never point tests at a business database: fixtures clear all three test tables.

Bash example (use your actual test credentials; password read from a prompt):
```sh
export RUN_DB_TESTS=true RUN_GUI_TESTS=true
export TEST_DB_URL='jdbc:mysql://127.0.0.1:3307/marketflow_test?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=LOCAL'
export TEST_DB_USER=marketflow_test
read -r -s -p 'Test database password: ' TEST_DB_PASSWORD; echo
export TEST_DB_PASSWORD
xvfb-run -a -s '-screen 0 1440x1000x24' mvn -B clean verify
unset TEST_DB_PASSWORD
```
On Windows/macOS with a graphical desktop, set equivalent environment variables and run Maven without Xvfb. The guard checks that TEST_DB_URL names marketflow_test.

## Manual checks still required on your PC
- Installation, JDK/Maven PATH and Docker Desktop availability.
- Windows/macOS launch script execution.
- Printer selection and physical receipt output.
- Screen-reader behavior, keyboard mnemonics and large-text layout at your display scale.
- Receipt TXT/CSV destination, overwrite prompt and file permissions.
- Database restart and app restart with preserved records.
- Database-unavailable error handling, duplicate SKU message, archived product reactivation.

The GUI test covers the core successful checkout path, not every screen-reader, printer or export interaction. Seed prices are synthetic and no live payments are tested.