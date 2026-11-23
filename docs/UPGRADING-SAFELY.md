# Upgrade the working Windows installation without resetting data

This improvement changes Java code and documentation only. The MySQL schema, `.env`, `config.properties`, Compose ports and named volumes do not need changing.

## Your working folder

Use the original folder whose database now works, reported as:

`C:\Users\limko\Downloads\marketflow-complete-project\event-driven-project`

If the folder you actually repaired is different, use that known working folder instead. **Do not create a third installation or run setup in another folder.** A new Compose project name can create another independent database volume.

## Safe manual update

1. Confirm the current app loads saved products and sales. Make a private MySQL backup using the README's backup guidance before any broader database work. CSV is not a full backup.
2. Close MarketFlow. Keep Docker/MySQL running; do not delete/recreate containers or volumes.
3. Download the new complete-project ZIP or the current repository files and extract them into a separate **temporary** folder. Do not run its setup script.
4. In the working folder, keep a copy of the existing `dist\marketflow.jar` outside the project as a rollback copy.
5. Copy the **new** `dist\marketflow.jar` into the working folder's `dist` directory. If the working folder contains `target\marketflow.jar`, the launch script prefers it: replace that JAR with the same new executable too, or rebuild the updated source. Do not delete unrelated files.
6. Copy the updated `src` directory, `docs` study materials, README and build/configuration **examples** as needed for code explanation. Preserve any student-edited reports separately before replacing canonical generated reports.
7. **Do not overwrite** `.env`, `config.properties`, your receipts/backups or database files. Do not run `setup.ps1`, change ports, or execute `docker compose down -v` for this update.
8. Run `scripts\run.bat` from the known working folder. Confirm original products/sales still load and that Sales history shows page status plus Older sales / Newer sales buttons.

With 50 or fewer sales, Older sales is correctly disabled. Do not generate fake transactions in your business database merely to enable it. The isolated GUI test verifies navigation with 55 synthetic sale headers.

## Verify the downloaded executable

In PowerShell, from the folder containing the new executable:

```powershell
Get-FileHash .\dist\marketflow.jar -Algorithm SHA256
Get-Content .\dist\SHA256SUMS.txt
```

The hash values should match. This does not reveal credentials. The `--check-db` diagnostic may also be used from the working project root:

```powershell
java -jar .\dist\marketflow.jar --check-db
```

For source edits, run `mvn clean package` before launch. The batch launcher does not automatically rebuild an existing JAR.

## If the new app fails

Close it and restore the old JAR copy to the same launch location. Since this update has no schema migration, rollback does not require reversing database changes. Share the error message, not password files. Keep both existing database volumes until their contents and purpose are verified; this update does not authorize deleting either one.