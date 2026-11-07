The warehouse prints shipping labels on several packing lines. Each line has one `LabelPrinter`,
and every packer on the line renders labels with it from their own thread. Last month someone
made the printer reuse a single buffer for every label "to save allocations". Since then a few
parcels a day go out with one customer's name above another customer's street. The postcode
lookup goes to a remote service and can take seconds, so a label waiting for its city must not
hold up the other packers.

Finish the two `TODO`s in `src/main/java/practice/LabelPrinter.java`:

- `render(...)`: the three lines of one label, counted for the dashboard;
- `renderedByAll()`: the number the dashboard shows, every label that any printer has rendered
  since the service started.

About eight lines in total. The tests render labels from several threads on one printer, and on
several printers one after the other.
