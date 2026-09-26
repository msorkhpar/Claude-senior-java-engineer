In a parallel stream several threads run the lambdas at once, so a lambda that adds to one shared list races with
the others. `collect` avoids that: each chunk fills its own container and the containers are merged afterwards,
left before right, so encounter order survives as well.

Write two static methods in `SafeCollect`. Each receives a stream that may be sequential or parallel:

1. `List<Long> squares(Stream<Integer> numbers)`: each number squared, as a `long`, in encounter order.
2. `Map<Character, List<String>> byFirstLetter(Stream<String> words)`: the words grouped by their first letter,
   each group in encounter order.

| call | answer |
|---|---|
| `squares(Stream.of(3, 1, 2))` | `[9, 1, 4]` |
| `squares(Stream.of(100000))` | `[10000000000]` |
| `byFirstLetter(Stream.of("apple", "banana", "avocado"))` | `{a=[apple, avocado], b=[banana]}` |

A square is exact for every `int`, `Integer.MAX_VALUE` included, and a first letter is taken as written: `'A'`
and `'a'` are two groups. The tests also hand both methods large parallel streams and compare the whole result, order included.
