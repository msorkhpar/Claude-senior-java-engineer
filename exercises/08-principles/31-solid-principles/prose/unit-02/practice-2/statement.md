The core banking team owns three kinds of ledger entry, `Deposit`, `Withdrawal` and `Fee`, and
adds one every year or two. Reports on them are added every month by other teams in the same
codebase. The last time a kind was added, the balance report learned about it and the monthly
statement didn't: customers got statements that skipped the new lines, and nobody noticed for
three weeks. `Reversal` is coming next.

In `src/main/java/practice/Ledger.java`, finish `balanceCents(...)` and `statementLine(...)`
(the formats are in their comments), and set up `Entry` so that adding a fourth kind can't build
until every method that looks at an entry's kind has been told about it. About twelve lines
in total.
