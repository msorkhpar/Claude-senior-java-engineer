The page's Q5 shows that when the target interface is a functional interface,
a lambda can be the whole adapter. Here the legacy code answers only one
question, `lessThan(a, b)`, while `Comparator<String>` needs a negative, zero or
positive `int`.

Write `Comparers.asComparator(legacy)`, returning a `Comparator<String>` that
translates each call into `lessThan` questions (a lambda is enough):

- negative when `lessThan(a, b)`;
- **positive when `b` is less** (`lessThan(b, a)`);
- **zero when neither is less by the legacy rule**, which may call two
  different strings equal.

With a legacy comparer that orders by length:

| call | answer |
|---|---|
| `compare("a", "bb")` | negative |
| `compare("abcd", "ab")` | positive |
| `compare("abc", "xyz")` | `0` |
| `Collections.min(["ccc", "a", "bb"], cmp)` | `"a"` |
