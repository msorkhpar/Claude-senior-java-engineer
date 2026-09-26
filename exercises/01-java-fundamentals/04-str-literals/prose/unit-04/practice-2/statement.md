`a.equals(b)` throws `NullPointerException` when `a` is `null`. `Objects.equals(a, b)` does
not: it is `true` when both are `null`, `false` when only one is, and otherwise calls
`a.equals(b)`.

Write `sameText(String a, String b)` in `Same`:

- `sameText("hello", "hello")` is `true`, and `sameText("hello", "world")` is `false`;
- `sameText(null, null)` is `true`;
- `sameText(null, "hello")` and `sameText("hello", null)` are `false`;
- `sameText("hello", new String("hello"))` and `sameText("", new String(""))` are `true`.
