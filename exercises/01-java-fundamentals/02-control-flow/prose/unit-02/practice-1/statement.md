Every morning the warehouse service brings each shelf up to a minimum stock level and reports
what the night shift counted. Since the last release, the report says shelves were restocked, but
the pickers' screens, which read the same `levels` array afterwards, still show the old levels. And
on nights when the shift doesn't reach every shelf, the report fails instead of giving a total.

Finish the two `TODO`s in `src/main/java/practice/Warehouse.java`:

- `raiseToMinimum(...)`: raise every level below the minimum to the minimum, and return how many
  you raised;
- `countedTotal(...)`: add up the counts; a shelf nobody counted holds `null` and adds nothing.

When you're done, the levels you raised are in the array the caller passed in, and an uncounted
shelf leaves the total of the others intact. About eight lines in total.
