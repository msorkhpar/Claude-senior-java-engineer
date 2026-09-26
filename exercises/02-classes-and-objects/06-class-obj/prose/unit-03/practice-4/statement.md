The page's advice for static members: use **static methods** for operations that
need no object state, and **static final** fields for constants, named in upper
case with underscores (`MAX_VALUE`). `final` is what stops the shared value from
being changed.

Write the utility class `Temperatures`:

- a constant `ABSOLUTE_ZERO_CELSIUS`, equal to `-273.15`, that nobody can change;
- `static double toFahrenheit(double celsius)`: `celsius * 9 / 5 + 32`;
- `static double toKelvin(double celsius)`: `celsius - ABSOLUTE_ZERO_CELSIUS`;
- both methods refuse a temperature **below** absolute zero with
  `IllegalArgumentException`. Absolute zero itself is fine.

**Examples**

```
Temperatures.toFahrenheit(100)     -> 212.0
Temperatures.toKelvin(0)           -> 273.15
Temperatures.toKelvin(-273.15)     -> 0.0
Temperatures.toFahrenheit(-300)    -> IllegalArgumentException
```
