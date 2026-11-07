The monthly billing run sets up one `Invoice.Builder` per plan, with the plan's fixed lines, and
builds each customer's invoice from a copy of it, adding that customer's own lines. Support has
two complaints: one customer's overage line showed up on the next customer's invoice, and an
invoice already sent as a PDF read differently when it was opened again in the portal.

Finish the two `TODO`s in `src/main/java/practice/Invoice.java`:

- `keep(...)`: the lines an invoice holds on to when it is built;
- `Builder.copy()`: a builder with the same customer and lines, filled in further on its own.

An issued invoice must read the same whatever is done afterwards to its builder, to another copy,
or to the list its `lines()` returns. About five lines in total.
