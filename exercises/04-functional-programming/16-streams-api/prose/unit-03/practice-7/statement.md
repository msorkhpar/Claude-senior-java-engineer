`mapMulti` (Java 16) is a one-to-many mapping like `flatMap`, but instead of
returning a stream per element it hands you a downstream consumer: call
`downstream.accept(x)` once for each result you want to emit, as many times as you
like, including not at all. Give it a type witness, `.<Integer>mapMulti(...)`, so
the result type is known.

Write two methods in `Expand`; `mapMulti` fits both:

1. `valueAndSquare(List<Integer> numbers)` emits each number followed by its square.
2. `upperStrings(List<Object> mixed)` emits each element that is a `String` (even an empty
   one), upper-cased with `Locale.ROOT`, and nothing for any other element, other
   `CharSequence` types such as `StringBuilder` included.

| call | answer |
|---|---|
| `valueAndSquare([2, 3, 4])` | `[2, 4, 3, 9, 4, 16]` |
| `upperStrings(["hello", "world"])` | `["HELLO", "WORLD"]` |
| `upperStrings([1, "hello", 2.0, "world", 3])` | `["HELLO", "WORLD"]` |

A mixed list can hold anything, `null` included.
