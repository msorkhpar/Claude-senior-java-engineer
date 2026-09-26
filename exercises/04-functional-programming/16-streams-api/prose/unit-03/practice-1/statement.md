`map` turns each element into exactly one result; `flatMap` turns each element into a
stream of results and flattens all those streams into one. Splitting sentences
with `map` would give a `Stream<String[]>`; `flatMap` gives the words themselves.

Write two methods in `Words`:

1. `words(List<String> sentences)` returns every word of every sentence, in order.
   Words are separated by one or more whitespace characters. A `null` sentence has no
   words.
2. `present(List<Optional<String>> values)` returns the values of the optionals that
   are present, in order.

| call | answer |
|---|---|
| `words(["Hello World", "Java Streams"])` | `["Hello", "World", "Java", "Streams"]` |
| `words(["  Hello   World  "])` | `["Hello", "World"]` |
| `present([Optional[a], Optional.empty, Optional[c]])` | `["a", "c"]` |

Every element may turn into zero, one or many results; make sure "zero" really
adds nothing.
