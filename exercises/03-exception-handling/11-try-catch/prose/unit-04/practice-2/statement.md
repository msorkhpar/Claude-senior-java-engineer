A try-with-resources statement can carry `catch` and `finally` blocks, as in the page's syntax
example. The resources are closed as the `try` block exits, before any `catch` or `finally` block of
the same statement runs.

Write `Traced.run(Opener opener, Body body, List<String> log)` as one try-with-resources statement
with a `catch` and a `finally`:

- declare the resource `opener.open("r")` (the opener and the resource record `"open r"` and
  `"close r"` in the same log themselves);
- run `body`, then add `"body done"`;
- catch any `IOException` (from opening, from the body or from closing) and add `"caught " + message`;
  anything else leaves `run` unchanged, after the steps below;
- in `finally`, add `"finally"`.

| body | log |
|---|---|
| completes | `open r, body done, close r, finally` |

Work out where `"caught boom"` lands when the body throws `IOException("boom")`, and which block
handles a failure of the resource's own `close()`.
