`distinct()` removes duplicates using `equals()` and `hashCode()`, and on an ordered
stream it keeps the **first** occurrence of each value, in encounter order. A class
that does not override those two methods compares by identity, so `distinct()` keeps
every instance of it.

The file you are given declares a small `Point` class next to `Dedupe`. Write two
methods in `Dedupe`, and change `Point` however you need (keep its constructor
`Point(int x, int y)`):

1. `distinctPoints(List<Point> points)` returns the points without duplicates; two
   points are the same when their coordinates are.
2. `firstSeen(List<String> names)` returns the names without duplicates, in the order
   each name first appears.

| call | answer |
|---|---|
| `distinctPoints([Point(1,2), Point(3,4), Point(1,2)])` | `[Point(1,2), Point(3,4)]` |
| `firstSeen(["a", "b", "a", "c", "b"])` | `["a", "b", "c"]` |
| `firstSeen(["pear", "apple", "pear", "fig"])` | `["pear", "apple", "fig"]` |

A duplicate is a matter of value, not of identity; and the order asked for is the
order of first appearance, not any sorted order.
