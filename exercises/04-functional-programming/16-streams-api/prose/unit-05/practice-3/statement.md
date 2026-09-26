A parallel stream processes its chunks on several threads at once, so any step that hands elements to the outside
world, or picks one of them, must say whether encounter order matters. `forEach` and `findAny` do not keep it;
`forEachOrdered` and `findFirst` do, at the cost of some coordination between the threads.

Write two static methods in `OrderedOutput`. Each receives a stream that may be sequential or parallel:

1. `void emitInOrder(Stream<String> lines, Consumer<String> sink)`: hands every line to `sink`, in encounter order.
2. `Optional<Integer> firstAbove(Stream<Integer> numbers, int threshold)`: the first number, in encounter order,
   that is greater than `threshold`.

| call | answer |
|---|---|
| `emitInOrder(Stream.of("a", "b", "c"), sink)` | `sink` receives `"a"`, `"b"`, `"c"` |
| `firstAbove(Stream.of(5, 3, 8, 1, 9, 2, 7, 4, 6), 5)` | `Optional[8]` |
| `firstAbove(Stream.of(1, 2), 5)` | `Optional.empty` |

The tests also hand both methods large parallel streams, where many numbers are above the threshold.
