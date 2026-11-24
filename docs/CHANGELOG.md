# Release notes

## 1.1.0 — 23 November 2026

- Sales history now uses 50-row keyset pages with Older/Newer navigation, bounded SQL and refresh-to-newest. CSV export is explicitly labelled as current-page export.
- Cart/sales numeric and date columns support correct sorting through reusable model overrides.
- Quantity, discount and tendered labels are associated with controls. History status has an accessible context. Font scaling preserves original heading/body proportions and restores the base sizes.
- Full local verification passed 31 tests (16 unit, 13 MySQL, 2 Swing), zero failures/errors/skips. New GUI checks cover pagination, numeric sorting, labels, relative text scaling and contrast changes.
- Seven synthetic-data screenshots, refreshed report and an isolated 5,000-header EXPLAIN measurement support the evidence.
- Added SDG/DPG evidence, group contribution structure, and comprehensive test suite documentation.
- Database screenshots and verified schema details added to project documentation.
- Maintained compatibility with existing schema and seed data.

## 1.0.0

Initial Java Swing/MySQL retail POS, validated cart/checkout, transactional stock updates, historical receipts, product archiving, accessibility controls, documentation and 25-test evidence.