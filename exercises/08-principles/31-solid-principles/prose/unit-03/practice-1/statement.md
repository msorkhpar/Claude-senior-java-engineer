The catalog importer reads each supplier's feed with one of several parsers and passes the parsed
SKU list to `SkuCleaner.clean`. The cleaner passed its tests, and in production it fails for some
suppliers and not others; for one supplier the importer's own list came back rewritten.

Write `clean` in `src/main/java/practice/SkuCleaner.java`. It returns the SKUs trimmed,
upper-cased, without blank entries or duplicates, in sorted order. Whatever kind of `List` a parser
produced, the call must work, and the importer's list must be as it was afterwards. About five
lines in total.
