A carrier's nightly CSV export has a ship-date column, and the shop prints a line from it on each
customer's receipt. A few cells from last night's file:

```text
14-Nov-2031
02-MAR-2032
28-Feb-2032
```

The carrier's software writes the month in English, three letters, sometimes in capitals. The
receipt spells the date out in English, like `Shipped Wednesday 3 December 2031`.

The shop's servers run in several countries, and their container images don't agree on a default
locale. One was set up with a German default last month.

Finish the two `TODO`s in `src/main/java/practice/ShipDates.java`:

- `shipped(...)`: the date in one cell;
- `receiptDate(...)`: the date as the receipt spells it out.

Every server must read every cell the carrier writes and print the same receipt. About eight lines
in total.
