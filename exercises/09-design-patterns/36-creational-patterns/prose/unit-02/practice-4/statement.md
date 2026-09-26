A factory method **does not have to create a new object**: `Integer.valueOf()`
returns cached instances, and the page recommends caching when the products
are immutable. Callers then get the same object for the same value.

Write the immutable `Currency` with a private constructor and:

- `Currency.of(String code)` returns **one cached instance per code**. A code
  must be exactly three capital letters `A`-`Z`; anything else throws
  `IllegalArgumentException`.
- `code()` returns the code.

| calls | result |
|---|---|
| `of("EUR")`, `of("EUR")` | the same instance |
| `of("EUR")`, `of("USD")` | two different instances |
| `of("EURO")`, `of("eu")`, `of("E1R")`, `of("ÄÖÜ")`, `of(null)` | `IllegalArgumentException` |
