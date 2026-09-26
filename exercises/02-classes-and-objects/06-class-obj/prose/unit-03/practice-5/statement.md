Static state is shared by every object, instance state is each object's own. The
page warns against **modifying static variables from instance code**: a setting
meant for one object then leaks into all the others.

Write `PriceFormatter`:

- `static void setDefaultDecimals(int decimals)` and `static int defaultDecimals()`:
  one default for the whole class, initially `2`.
- `PriceFormatter()` makes a formatter with no setting of its own: it always uses
  the default **as it is when `format` is called**.
- `PriceFormatter(int decimals)` makes a formatter with its own setting. It
  changes nothing outside this formatter, and later default changes do not
  affect it.
- `String format(double amount)` prints the amount with the formatter's number of
  decimals, rounded, with a dot as the decimal separator
  (`String.format(Locale.ROOT, "%.2f", amount)` for 2 decimals).

**Examples**

```
new PriceFormatter().format(1.5)      -> "1.50"
new PriceFormatter(3).format(1.5)     -> "1.500"
f = new PriceFormatter(); PriceFormatter.setDefaultDecimals(0); f.format(7) -> "7"
```
