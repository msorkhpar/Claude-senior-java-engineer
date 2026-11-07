Every night the warehouse sends its stock to a partner as one CSV file. The partner's importer
reads the first line as the header and every following line as a record, and it rejects a file
that has an empty line anywhere, the end included. The catalogue was 2,000 items when the export
was written; next quarter it will be 300,000, and the export has to finish in the same window.

`StockExport` already formats one record with `line(item)`. Write `csv(items)` in
`src/main/java/practice/StockExport.java`: the header, then one line per item in the order given,
with `"\n"` between lines. A catalogue with no items still gets its header.

About five lines in total.
