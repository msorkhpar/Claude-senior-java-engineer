A `try`/`catch` block in the middle of a pipeline buries the pipeline. Wrapping it once in a
small helper keeps each pipeline readable: `lift` turns a function that may throw into one that
returns an `Optional`, empty when there is no result.

The file declares `CheckedFunction<T, R>`, whose method is `R apply(T t) throws Exception`.
Write two static methods of `Lifting`:

1. `<T, R> Function<T, Optional<R>> lift(CheckedFunction<T, R> function)`: the returned function
   gives `Optional.of(result)`, or an empty `Optional` when `function` throws or has no result.
2. `List<Integer> parseAll(List<String> texts)`: the texts that parse as integers, parsed, in
   order, the rest skipped; `lift(Integer::parseInt)` is the natural way to build it.

| call | answer |
|---|---|
| `parseAll(List.of("1", "abc", "3", "xyz", "5"))` | `[1, 3, 5]` |
| `parseAll(List.of("abc", "xyz"))` | `[]` |
| `lift(Integer::parseInt).apply("42")` | `Optional[42]` |

"Throws" means any exception the interface allows, and "no result" has more than one form.
