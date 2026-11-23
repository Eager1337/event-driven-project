# Release notes

## 1.1.0 — 23 November 2026

- Sales history now uses 50-row keyset pages with Older/Newer navigation, bounded SQL and refresh-to-newest. CSV export is explicitly labelled as current-page export.
- Cart/sales numeric and date columns support correct sorting through reusable model overrides.
- Quantity, discount and tendered labels are associated with controls. History status has an accessible context. Font scaling preserves original heading/body proportions and restores the base sizes.
- Full local verification passed 31 tests (16 unit, 13 MySQL, 2 Swing), zero failures/errors/skips. New GUI checks cover pagination, numeric sorting, labels, relative text scaling and contrast changes.
- Seven synthetic-data screenshots, refreshed report and an isolated 5,000-header EXPLAIN measurement support the evidence.
- Added Excellent-band audit, SDG/DPG evidence, proposed member 1–5 allocation with blank completion log, timed mock-viva practice and safe upgrade instructions.
- CI generated-file staging is limited to intended build outputs. User-uploaded duplicate documents are preserved; cleanup is recommended but not performed.
- **No schema migration, password regeneration or database-volume reset.** Use `UPGRADING-SAFELY.md` to update your original working installation.

These improvements strengthen rubric evidence but do not guarantee an Excellent grade. Students still need genuine contributions, lecturer access, manual PC checks and confident individual explanation. SDG links are potential design alignment, not measured outcomes or DPG certification.

## 1.0.0

Initial Java Swing/MySQL retail POS, validated cart/checkout, transactional stock updates, historical receipts, product archiving, accessibility controls, documentation and 25-test evidence.