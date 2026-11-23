# SDG and digital-public-goods evidence

## Conclusion
MarketFlow has plausible educational links to SDGs 8, 9 and 10 and demonstrates several DPG-related design practices. These are **design alignments**, not measured social outcomes, formal DPG recognition or compliance certification. The supplied rubric awards five marks for DPG standards alignment; it does not require achieving every SDG.

## SDG mapping

| Goal / target | Relevant official theme | Implemented feature | Evidence and limit |
|---|---|---|---|
| SDG 8, target 8.2 | Productivity through technological upgrading and innovation | Digital inventory, exact sale totals, persistent receipts | `Domain.Cart`, `Store.checkout`, live demonstration. May reduce manual bookkeeping errors; no retailer productivity study has measured that effect. [1] |
| SDG 8, target 8.3 | Entrepreneurship and development of micro-, small- and medium-sized enterprises | Low-stock visibility, product management, simple small-shop workflow | `MainFrame.filter`, `Store.save`, screenshots. A software tool supports operations; it does not itself implement economic policy, guarantee jobs or formalize a business. [1] |
| SDG 9 | Innovation and resilient infrastructure | Modular Java source and transactional MySQL storage | `App`, `MainFrame`, `Domain`, `Store`, `Tables`; rollback and concurrency tests. Educational digital capability, not public infrastructure deployment. [2] |
| SDG 9, target 9.3 (indirect only) | Small enterprises' access to financial services and integration into value chains/markets | Retrievable sales records and CSV summaries | Record keeping may support future business reporting, but the app offers no credit, bank connection or market integration. Do not present it as directly achieving target 9.3. [2] |
| SDG 10, target 10.2 | Inclusion irrespective of disability and other characteristics | Keyboard navigation, accessible names/descriptions, label associations, text scaling and high contrast | `MainFrame.accessible`, `labelFor`, help options; automated label/name checks. Real users and screen readers still need testing; no reduction in inequality has been measured. [3] |

**Presentation wording:** “The strongest potential connection is SDG 8: a small-shop tool can support more reliable digital bookkeeping. SDG 10 relates to the accessibility choices, and SDG 9 relates to reusable technical capability. We demonstrate features; we have not measured SDG outcomes.”

## DPG-related practices and honest gaps

The DPGA standard includes SDG relevance, open licensing, ownership, platform independence, documentation, data extraction, privacy/applicable laws, open standards and do-no-harm safeguards. Its current standard, not this university rubric, governs formal recognition. [4]

| Practice | Project evidence | Remaining limitation |
|---|---|---|
| Open licensing | MIT `LICENSE`; dependency notices and source archives | Repository is private. Public availability and any lecturer access must be handled separately; license does not make a private repository publicly discoverable. |
| Clear ownership | Git history identifies automation and real account actions | Student identities/contributions remain unverified; fill the contribution log truthfully. Review ownership and university policy before external reuse. |
| Platform independence | Java 17 target, ordinary JDBC, native desktop code; Windows and Unix launchers | Requires Java and MySQL; Windows is user-reported working. Not verified on every OS. Docker is optional, not a proprietary runtime requirement. |
| Documentation | README, source map, schema, tests, study guide, upgrade instructions | Group must understand the material and keep evidence current. |
| Extractable data | Receipt TXT, current history page CSV, documented MySQL backup requirement | Page CSV is not a complete backup. Full migration requires private database export and restoration testing. |
| Privacy minimization | No customer names, card numbers or customer tracking; local credentials excluded from Git | Sales and business inventory can still be sensitive. Restrict DB access and keep backups private. No authentication/roles are implemented in the UI. |
| Open interfaces / portability | Java/JDBC/MySQL SQL schema and UTF-8 text/CSV exports | SQL is MySQL-specific in places; moving to another database requires adaptation and tests. |
| Do no harm / reliability | Validation, prepared statements, stock checks, transaction rollback, immutable receipt snapshots | Not a production fiscal/payment system. Requires secure deployment review, backup policy and real accessibility testing. |
| Inclusion | Keyboard shortcuts, named controls, text sizing and high contrast | Local-language support and screen-reader testing with users remain future work. |

Do not say “DPGA certified”, “fully privacy-law compliant”, “fully accessible”, or “supports every SDG”. None has been established. The rubric asks for strong alignment with multiple principles; explain specific choices with code and acknowledge their boundaries.

## Suggested impact evaluation (not yet conducted)

With consent, test only synthetic workflows with a small group of users. Record task completion, calculation errors, time to enter a sale, keyboard-only success, and accessibility obstacles. Compare against the same manual task; disclose sample size and limitations. Do not collect identity or disability information unnecessarily. Any improved result would apply to that limited study, not prove national SDG progress.

## Sources

Authoritative definitions were checked against official UN and DPGA sources for this improvement round. Feature mappings are project analysis based on the code, not endorsements by these organizations.

1. UN DESA, Goal 8 and targets: https://sdgs.un.org/goals/goal8
2. UN DESA, Goal 9 and targets: https://sdgs.un.org/goals/goal9
3. UN DESA, Goal 10 and targets: https://sdgs.un.org/goals/goal10
4. Digital Public Goods Alliance, Digital Public Goods Standard: https://www.digitalpublicgoods.net/standard
5. DPGA, Privacy and Data Security Framework: https://www.digitalpublicgoods.net/blog/privacy-and-data-security-framework-for-dpg-standard