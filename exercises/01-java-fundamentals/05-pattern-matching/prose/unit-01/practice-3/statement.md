A class can implement several interfaces, so one object can be an instance of several
interface types at once. Each one is tested on its own.

Write `of(Object obj)` in `Abilities`. It returns, in this order, the names of the
interfaces among `Runnable`, `Comparable` and `CharSequence` that `obj` is an instance of:

- `of(new Thread())` is `["Runnable"]`, and `of(42)` is `["Comparable"]`;
- a `String` is both comparable and a character sequence: `of("hi")` is
  `["Comparable", "CharSequence"]`;
- `of(new Object())` and `of(null)` are `[]`.
