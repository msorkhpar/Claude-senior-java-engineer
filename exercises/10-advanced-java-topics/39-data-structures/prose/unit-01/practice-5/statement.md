The page stores a graph as an **adjacency list**: each vertex keeps the set of
its neighbours. Breadth-first search (BFS) from a vertex visits it, then all its
neighbours, then theirs, using a queue and a *visited* set, and in an
unweighted graph it finds a **shortest path**.

Write `Graph<T>`:

- `new Graph<>(directed)`; `addEdge(from, to)` adds both vertices if new and
  the edge; in an undirected graph the edge goes both ways, in a directed one
  **only from `from` to `to`**. Neighbours are kept in the order their edges
  were added.
- `bfs(start)` lists the vertices reached from `start` in BFS order, **each
  once**; an unknown `start` gives an empty list.
- `shortestPath(from, to)` lists the vertices of a shortest path, both ends
  included; **no path (or an unknown vertex) gives an empty list**, and
  **a vertex's path to itself is just `[that vertex]`**.

Edges added (undirected): New York-Boston, New York-Philadelphia,
Boston-Portland, Philadelphia-Washington.

| call | result |
|---|---|
| `bfs("New York")` | `[New York, Boston, Philadelphia, Portland, Washington]` |
| `shortestPath("Portland", "Washington")` | `[Portland, Boston, New York, Philadelphia, Washington]` |
| directed A->B: `shortestPath("B", "A")` | `[]` |
| `shortestPath("Boston", "Boston")` | `[Boston]` |
