A filter whose lambda reads and writes shared state is *stateful*: its answer for one element depends on what the
lambda saw before. In a parallel stream "before" means "on some thread, earlier in time", not "earlier in the
list". `distinct()` is the stream's own stateful step: on an ordered stream it keeps the first occurrence of each
element in encounter order, whatever the threads do.

Write `KeepFirst.firstOccurrences(Stream<Integer> ids)`. It returns each id once, at the place where it first
occurs, in encounter order. The stream may be sequential or parallel.

| ids | answer |
|---|---|
| `[1, 2, 2, 3, 1]` | `[1, 2, 3]` |
| `[4, 4, 4]` | `[4]` |

The tests use ids whose first appearance is not their numeric order, and a large parallel stream in which every id
appears twice.
