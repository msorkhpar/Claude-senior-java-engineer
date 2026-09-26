When a statement inside a `try` block throws, control jumps straight to the matching `catch`
block; when nothing throws, the `catch` block is skipped. The catch parameter is the exception
itself, so `e.getMessage()` gives its detail.

Write `DivisionReport.report(int[][] pairs)`. Each pair is `{numerator, denominator}`. Return one
line per pair, in order:

- `"a / b = q"` when the `int` division `a / b` succeeds (it wraps silently on overflow, so
  `Integer.MIN_VALUE / -1` is `Integer.MIN_VALUE`);
- `"a / b: <message>"` when it throws, where `<message>` is the caught exception's message.

| pairs | answer |
|---|---|
| `{{10, 2}, {7, 2}}` | `["10 / 2 = 5", "7 / 2 = 3"]` |
| `{{10, 0}}` | `["10 / 0: / by zero"]` |

A pair "throws" exactly when `a / b` would throw, so the message for a zero divisor is `"/ by zero"`.
Think about where the `try` goes: one bad pair must not cost the report the pairs after it, and the
message is the exception's message, not its whole description.
