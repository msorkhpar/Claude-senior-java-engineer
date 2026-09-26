The page's key points say `invokeAll()` takes **any number** of subtasks,
`invokeAll(task1, task2, task3, ...)` or a collection, and **blocks until all of
them complete**. Once it returns, the side effects of every subtask are done
and can be read, which is how a `RecursiveAction` builds on its subtasks' work
without returning anything.

A `Node` has a `size`, a list of `children` and a `total` that starts at 0.
Write `TotalsAction(node)`, a `RecursiveAction` that sets the `total` of
**every** node of the tree to its own `size` plus the totals of all its
children. Run the children's actions with `invokeAll`, then compute the node's
own total. The tests run the actions in a pool of **one** worker: a child's
action has run only if the parent waited for it before reading its total. The tests see the totals, not how
the children were run, so `invokeAll` is the page's way rather than a graded step. Running the action again
on the same tree sets the same totals.

| tree (name:size) | totals afterwards |
|---|---|
| `root:1` with children `a:2`, `b:3` | root 6, a 2, b 3 |
| `root:1` with children `a:2`, `b:3`, `c:4` | root 10 |
| `root:1` > `x:2` > `y:3` > `z:4` (a chain) | root 10, x 9, y 7, z 4 |
| `solo:5`, no children | solo 5 |
