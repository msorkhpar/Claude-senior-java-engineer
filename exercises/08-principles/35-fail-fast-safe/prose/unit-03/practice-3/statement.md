A chain of about thirty shops keeps one product in `StockLedger`: units per warehouse. Transfer
threads move units between warehouses a few times a minute; nothing is ever added or sold here, so
the chain's total never changes. The stock-take report prints one line per warehouse on a label
printer, which takes about a second per line, and then the total.

Last month's reports printed lines that didn't add up to the total the chain holds, and the
auditors flagged every one. Someone suggested locking the ledger while the report prints, but a
transfer must not wait half a minute for the printer.

Finish `src/main/java/practice/StockLedger.java`: hold the stock however you need to (the
`ConcurrentHashMap` is only where the team started), and write `move` and `report`. When you're
done, every report's lines add up to a total the chain really held, even with transfers running
during it, and a transfer never waits for the printer. About twelve lines in total.
