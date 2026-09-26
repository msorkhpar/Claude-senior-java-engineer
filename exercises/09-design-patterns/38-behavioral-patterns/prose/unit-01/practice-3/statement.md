A strategy interface with one method is a functional interface, so a lambda
or a method reference can be the strategy, and `UnaryOperator<String>` is
already the interface a text strategy needs. The page also keeps stateless
strategies as `static final` constants.

Write `TextProcessor`, a context holding a `UnaryOperator<String>`:

- The constructor and `setStrategy(s)` keep the strategy and refuse `null`
  with `IllegalArgumentException`.
- `process(text)` returns the strategy applied to `text`; **`null` text is
  refused** with `IllegalArgumentException` before the strategy runs.
- Five constants: `UPPER_CASE`, `LOWER_CASE`, `REVERSE`, `TRIM_AND_UPPER`
  and `REMOVE_WHITESPACE`. **Case changes use `Locale.ROOT`**, so they give
  the same answer on every machine. **`REMOVE_WHITESPACE` removes every
  whitespace character** (spaces, tabs, newlines).

| strategy | text | answer |
|---|---|---|
| `UPPER_CASE` | `"hello"` | `"HELLO"` |
| `REVERSE` | `"hello"` | `"olleh"` |
| `TRIM_AND_UPPER` | `"  hello  "` | `"HELLO"` |
| `REMOVE_WHITESPACE` | `"h e\tl\nlo"` | `"hello"` |
| `s -> s.replaceAll("\\s+", "-")` | `"hello world"` | `"hello-world"` |
| any | `null` | `IllegalArgumentException` |
