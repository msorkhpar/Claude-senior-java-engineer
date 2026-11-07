The shelf-label printer in warehouse 3 has started printing labels whose fields drift to the
left, and some labels come out blank. Its manual is strict about the job it receives: a setup
command on one line, then the label's body, where every line is a field exactly 24 characters wide,
padded with spaces on the right, and every line ends with a line break.

For the label on aisle B, shelf 07, the setup command is

```text
^XA^PW406^LL203^LH0,0^MD10^PR4^FO20,20^A0N,28,28^FDAISLE B^FS^XZ
```

and the body's three fields are `AISLE B / SHELF 07`, `SKU 44-1093` and `QTY 12`.

The team's style rule allows 60 columns per source line, so the setup command can't sit on one
line. Write both `TODO` methods of `src/main/java/practice/ShelfLabel.java` as text blocks: when
you're done, `job()` returns the exact characters the manual asks for. About eight lines in total.
