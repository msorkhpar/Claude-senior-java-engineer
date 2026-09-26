The page's `LegacyApi` keeps two old methods working while it steers callers
to their replacements. `@Deprecated` says how urgent the move is: `since`
names the version, and `forRemoval = true` warns that the method **will be
removed**, while plain `@Deprecated` only discourages it. `@Deprecated` has
RUNTIME retention, so the tests read it back through reflection.

Write `LegacyApi`:

- `oldMethod()` returns `"old result"`. It is deprecated since `"2.0"` and
  **for removal**.
- `newMethod()` returns `"new result"`. It is its replacement.
- `legacyCalculation(a, b)` returns `a + b` and wraps on overflow, as it always
  has. It is deprecated since `"1.5"` but **not for removal**.
- `modernCalculation(a, b)` returns `a + b` but **refuses overflow** with
  `ArithmeticException`.

| call | result |
|---|---|
| `oldMethod()` | `"old result"` |
| `legacyCalculation(Integer.MAX_VALUE, 1)` | `-2147483648` |
| `modernCalculation(2, 3)` | `5` |
| `modernCalculation(Integer.MAX_VALUE, 1)` | `ArithmeticException` |
| `@Deprecated` on `oldMethod` | `since = "2.0"`, `forRemoval = true` |
| `@Deprecated` on `legacyCalculation` | `since = "1.5"`, `forRemoval = false` |
