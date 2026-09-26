The page's best practice fills a report with a text block template, and its
edge cases add the catch: when `formatted()` or `String.format` reads a
template, a `%` that should be printed is written **`%%`**. The page's example
report is:

```text
Report: Q1 2024
Revenue: $1,000,000
Growth: 15%
```

Write `Report.summary(String quarter, long revenue, Integer growth)`. It returns
the lines above joined by `\n`, with no newline at the end:

- `Report: <quarter>`;
- `Revenue: $<revenue>`, with a comma between every three digits;
- `Growth: <growth>%`, only when `growth` is not `null`.

The output must not depend on where the program runs: `formatted()` uses the
JVM's default locale, which may group digits with `.` or a space. Format with
`String.format(Locale.ROOT, ...)` instead, for every line, the growth line
included (some locales even write digits in another script).

| quarter, revenue, growth | answer (`/` separates lines) |
|---|---|
| `Q1 2024`, `1000000`, `15` | `Report: Q1 2024` / `Revenue: $1,000,000` / `Growth: 15%` |
| `Q2 2024`, `999`, `null` | `Report: Q2 2024` / `Revenue: $999` |
| `Q3 2024`, `1234567`, `-3` | `...` / `Revenue: $1,234,567` / `Growth: -3%` |
