`null instanceof String s` is `false`, so a pattern is null-safe and never throws. When
`null` needs an answer of its own, test for it separately, before the pattern.

Write `describe(Object obj)` in `Describer`:

- a String: `"text of "` and its length, so `describe("hello")` is `"text of 5"`;
- `null`: `"nothing"`;
- anything else: `"not text"`, so `describe(42)` is `"not text"`.
