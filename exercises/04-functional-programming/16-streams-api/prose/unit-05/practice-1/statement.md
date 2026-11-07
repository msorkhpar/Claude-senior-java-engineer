The nightly price export has to be on the shops' servers by six in the morning. Pricing a single
SKU takes a lot of CPU, and with one thread doing it the export now runs past seven. The pricing
code is safe to call from several threads at once. The writer that appends lines to the export
file is not, and the shops' import expects the lines in catalogue order.

Finish `export(...)` in `src/main/java/practice/PriceExport.java`. When you're done, several SKUs
are priced at the same time, and the writer receives every line, one at a time, in the order of
the catalogue. About three lines in total.

The tests run the export in a pool of four threads and use a pricer that is slow for some SKUs.
