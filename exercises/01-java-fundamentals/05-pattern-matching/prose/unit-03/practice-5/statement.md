A type pattern on a component checks that component's run-time type. With a generic record
`Pair<A, B>(A first, B second)`, the type arguments are erased at run time, so
`obj instanceof Pair<?, ?>(String name, Integer age)` asks, for this very pair, whether its
first value is a `String` and its second an `Integer`.

`Pairs` holds `record Pair<A, B>(A first, B second)`. Write `label(Object obj)` in `Pairs`:

- a pair of a `String` and an `Integer`: `new Pair<>("Ada", 36)` is `"Ada is 36"`;
- a pair of two `Integer`s: `new Pair<>(2, 3)` is `"sum 5"`;
- any other pair, such as `new Pair<>(36, "Ada")`, `new Pair<>("a", "b")` or
  `new Pair<>(3.5, 2)`: `"pair"`;
- anything else, `null` included: `"not a pair"`.
