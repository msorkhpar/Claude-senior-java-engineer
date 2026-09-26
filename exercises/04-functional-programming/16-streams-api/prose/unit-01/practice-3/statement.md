A stream is **single-use**: once a terminal operation has consumed it, using it again
throws `IllegalStateException`. When the same pipeline must answer two questions, the
fix is a new stream for each terminal operation, and a `Supplier<Stream<T>>` is a
tidy way to hand them out.

Write two methods in `LongWords`:

1. `longerThan(List<String> words, int min)` returns a `Supplier<Stream<String>>`
   whose every `get()` yields a stream of the words longer than `min` characters,
   in their order.
2. `report(List<String> words, int min)` returns a `LongWords.Report(long count, List<String> words)`
   holding how many words are longer than `min` and the words themselves. Build
   both parts from the supplier of method 1.

| words | min | report |
|---|---|---|
| `["hi", "coffee", "juice", "milk"]` | `3` | `Report[count=3, words=[coffee, juice, milk]]` |
| `["hi", "ok"]` | `3` | `Report[count=0, words=[]]` |

A caller may call `get()` as often as it likes. Mind the word that is exactly `min`
long.
