The page detects a cycle in a **directed** graph with depth-first search that
keeps two sets: *visited* (ever seen) and the *current path* (the recursion
stack). An edge to a vertex **on the current path** closes a cycle; an edge to a
vertex that was visited earlier but is finished does not. Because a graph can be
disconnected, **the search is started from every vertex**, not just the first.

Write `Dependencies`:

- `addEdge(from, to)` records "`from` depends on `to`" (a directed edge),
  adding either vertex if new.
- `hasCycle()` tells whether following edges can ever lead back to a vertex.

| edges | `hasCycle()` |
|---|---|
| A->B, B->C, C->A | `true` |
| A->B, B->C | `false` |
| A->B, A->C, B->D, C->D | `false` |
| A->B, B->A | `true` |
| A->B, then X->Y, Y->X | `true` |
