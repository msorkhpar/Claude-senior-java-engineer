The warehouse imports its inbound shipments from spreadsheets that people fill in by hand. Each
enum column (the dock, the handling flags, the stage) is read with one shared helper, whatever
enum the column holds, and the cells look like `north-gate`, ` Cold Store ` or
`HAZMAT,OVERSIZE,FRAGILE`. The dashboard's "advance" button moves a shipment's stage to the next
one with the same helper class.

`parseAll(...)` already splits a cell. Finish the three `TODO`s in
`src/main/java/practice/Enums.java`:

- `parse(...)`: the constant a cell's text means;
- `newSet(...)`: the set `parseAll` fills;
- `next(...)`: the constant after the current one.

When you're done, every column parses the way people type its values, a cell's flags come back in
the order the enum declares them, and "advance" works on every stage. About eight lines in total.

The tests use enums of their own, so the helper can't name any particular enum.
