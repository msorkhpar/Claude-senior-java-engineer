A **bound** reference (`rule::accepts`) fixes its receiver, so every call runs on that
one object and can use its fields. A static method has no object and cannot read them.

Complete `LengthRule`:

- `LengthRule(int min)` makes a rule with its own minimum length.
- `boolean accepts(String word)` returns whether `word` is at least `min` characters long,
  counting every character, spaces included (so a rule of `0` accepts `""`). `null` is never
  accepted, through any of the methods below either.
- `Predicate<String> asPredicate()` returns this rule's `accepts` as a predicate.
- `List<String> keep(List<String> words)` returns the accepted words, in order.

| rule | call | result |
|---|---|---|
| `new LengthRule(5)` | `accepts("hello")` | `true` |
| `new LengthRule(5)` | `asPredicate().test("hi")` | `false` |
| `new LengthRule(3)` | `keep(["a", "abc", "abcd"])` | `["abc", "abcd"]` |

A program may hold many rules at once, each with a different minimum.
