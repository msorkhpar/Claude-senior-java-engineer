In a pattern switch, `default` never matches `null`: without a `case null`, a `null` selector
throws `NullPointerException`, default or not. `case null, default ->` handles both in one
arm.

Write `text(Object obj)` in `Normalizer` as one switch expression:

- a `String` is trimmed: `text("  hi ")` is `"hi"`;
- an `Integer` becomes its digits: `text(42)` is `"42"`;
- anything else gives `""`, and so does `null`: `text(3.5)` and `text(null)` are both `""`.
