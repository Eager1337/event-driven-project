# Security and privacy

This is an academic demonstration, not an approved production retail system. It has no user authentication or role enforcement. Access to the desktop implies permission to edit inventory and record sales. Card means bookkeeping only; no card data is processed.

Setup creates random passwords in ignored local files. MySQL binds to 127.0.0.1 and uses a non-root application account. Use verified TLS for any remote database. Keep local credentials and backups private, and limit desktop access. SQL is parameterized and checkout locks rows, but these controls are not a full security audit.

If a secret is accidentally committed, rotate it immediately and remove it from Git history. Changing .env does not rotate an existing database password. Do not attach private data to public issues.

Report a concern privately to the repository owner using GitHub's available private security-reporting option if enabled; otherwise contact the owner through an existing trusted private channel. Do not disclose a live credential in an issue. Dependency licenses and versions are listed in pom.xml and THIRD_PARTY_NOTICES.md.