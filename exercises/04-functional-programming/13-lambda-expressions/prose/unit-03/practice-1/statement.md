`java.util.function` has one interface for each shape of job. A `Consumer<T>` takes a value
and returns nothing, a `Supplier<T>` takes nothing and returns a value, a `Function<T, R>`
turns one value into another, a `Predicate<T>` answers yes or no, and a `BinaryOperator<T>`
combines two values of one type. Writing `Function<String, Void>` and returning `null` is the
sign that a `Consumer` was wanted.

Write the static factories of `Handlers`, each returning the interface named in its signature:

| method | what the returned object does |
|---|---|
| `Consumer<String> appendTo(List<String> sink)` | adds every accepted string to `sink`, repeats included |
| `Supplier<List<String>> freshList()` | gives an empty, modifiable list |
| `Function<String, Integer> length()` | `"hello"` → `5` |
| `Predicate<String> startsWithA()` | `"Alice"` → `true`; `"Bob"`, `"alice"` and `""` → `false` |
| `BinaryOperator<Integer> larger()` | `(3, 7)` → `7`, for any two `int` values |
| `Consumer<String> both(Consumer<String> first, Consumer<String> second)` | runs `first`, then `second` |

A factory's supplier is called many times over its life, and "first, then second" is a
promise about order.
