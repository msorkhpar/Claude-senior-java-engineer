A lambda that reads a variable from the method that created it **closes over**
that variable: it carries the value with it after the method has returned.
That makes small factories possible, each returning a function tuned by its
arguments.

Write three factories in `Closures`:

1. `multiplier(int factor)` returns a `Function<Integer, Integer>` that
   multiplies its input by `factor`.
2. `between(int min, int max)` returns a `Predicate<Integer>` that tests whether
   a value lies in the range from `min` to `max`, both included. When `min` is
   greater than `max` the range is empty.
3. `prefixFilter(String prefix, int minLength)` returns a
   `Function<List<String>, List<String>>` that keeps, in order and duplicates
   included, every word that starts with `prefix` (case-sensitive, as
   `String.startsWith` decides) and is at least `minLength` long.

| call | answer |
|---|---|
| `multiplier(3).apply(5)` | `15` |
| `between(10, 20).test(15)` | `true` |
| `between(10, 20).test(25)` | `false` |
| `prefixFilter("hel", 5).apply(["hello", "help", "hi", "helicopter"])` | `["hello", "helicopter"]` |

Check the ends of the range, and make sure several functions made by the same
factory do not interfere with one another.
