A record's `equals` and `hashCode` are generated from its components and its
state cannot change, which is what makes a record a safe **key** in a
`HashSet` or a `HashMap`.

A walk starts at the cell `(row 0, col 0)` of an unbounded grid. Each letter of
`path` moves one cell: `U` is row − 1, `D` is row + 1, `L` is col − 1 and `R` is
col + 1. The start cell counts as visited.

`Walk` declares `public record Cell(int row, int col) {}`. Write

```java
static Walk.Cell firstRevisit(String path)
```

which returns the first cell the walk enters that it had already visited, or
`null` when no cell is visited twice.

## Examples

```
firstRevisit("RRL")     -> Cell[row=0, col=1]
firstRevisit("UD")      -> Cell[row=0, col=0]     (back at the start)
firstRevisit("RRLLRR")  -> Cell[row=0, col=1]     (the first of several revisits)
firstRevisit("URDR")    -> null
firstRevisit("")        -> null
```

`path` holds only the letters `U`, `D`, `L` and `R`.
