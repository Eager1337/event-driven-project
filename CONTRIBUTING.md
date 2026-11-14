# Contributing

Use Java 17-compatible code. Keep Swing mutations on the EDT and JDBC work off it. Use BigDecimal, prepared statements and try-with-resources. New checkout behavior must have transaction and rollback tests. Never commit credentials, private receipts or real customer information.

For genuine group work: open an issue, branch from main (for example feature/stock-audit), make scoped commits under your own identity, run tests, open a pull request and request a teammate review. Include the problem, approach, testing evidence and screenshots. Do not rewrite history or claim generated commits as student contributions.

Run `mvn clean verify` for unit tests. Enable isolated MySQL/GUI suites as described in docs/TESTING.md. The CI workflow builds the executable, report and screenshots. Update docs/REQUIREMENTS.md when changing assessment evidence.

Original contributions use the MIT license. Respect dependency licenses and university academic-integrity requirements.