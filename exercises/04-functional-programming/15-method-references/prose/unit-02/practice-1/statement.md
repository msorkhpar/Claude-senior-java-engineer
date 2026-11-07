The accounting team reviews large journal entries on a screen that flags every amount at or above
a limit with `[review]`. At month end the same entries go out as a CSV file, and the import on the
other side rejects any amount column that isn't a plain number. Each exported entry id also carries
the accounting period after it, so `AP-9` in March 2031 becomes `AP-9/2031-03`.

`Ledger.PlainFormat` and `Ledger.FlaggedFormat` already render amounts. Finish the three `TODO`s in
`src/main/java/practice/Ledger.java`, each of which returns a function that other code calls later:

- `withPeriod(String period)`: the function the export applies to each entry id;
- `FlaggedFormat.exportColumn()`: the function for the export's amount column;
- `FlaggedFormat.screenColumn()`: the function for the review screen. Teams extend
  `FlaggedFormat` to restyle their own screen, and the screen must show their style.

One line each, about three lines in total. A method reference fits some of them; whether it fits
each one is part of the task.
