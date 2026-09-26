The page's `LinkedList` implements both `List` and `Deque`: adding or removing
at **either end** is O(1), and the page recommends `ArrayDeque` for the same
queue and stack work. A `Deque` has two families of end methods: one throws
when the deque is empty, the other returns `null`.

Write the class `Tasks`, a line of task names:

- `add(String task)` puts a task at the **back** of the line.
- `urgent(String task)` puts a task at the **front**, so it is served before
  everything already waiting. The newest urgent task goes first of all.
- `next()` removes and returns the task at the front, or `null` when the line
  is empty.
- `last()` returns the task at the back without removing it, or `null` when
  the line is empty.

| calls | answer |
|---|---|
| `add("a")`, `add("b")`, `add("c")`, then `next()` three times | `"a"`, `"b"`, `"c"` |
| `add("a")`, `urgent("u")`, then `next()` twice | `"u"`, `"a"` |
| `urgent("x")`, `urgent("y")`, then `next()` twice | `"y"`, `"x"` |
| `next()` on a new line | `null` |
