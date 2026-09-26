In a **binary search tree** every node's left subtree holds smaller values and
its right subtree larger ones. The page walks one tree in four orders:
in-order (left, root, right) gives the values sorted, pre-order puts the root
first, post-order puts it last, and **level order** goes breadth-first, one
level at a time, with a queue.

Write `SearchTree<T extends Comparable<T>>`:

- `insert(value)` places a value; **a value already in the tree is ignored**;
  `null` is refused with `NullPointerException`.
- `inOrder()`, `preOrder()`, `postOrder()`, `levelOrder()` return new lists.
- `height()` is the number of edges on the longest root-to-leaf path: one
  node has height 0 and **an empty tree has height -1**.

| inserted | in-order | pre-order | post-order | level order | height |
|---|---|---|---|---|---|
| 5, 3, 7, 1, 4 | `[1, 3, 4, 5, 7]` | `[5, 3, 1, 4, 7]` | `[1, 4, 3, 7, 5]` | `[5, 3, 7, 1, 4]` | 2 |
| 5, 3, 5, 3, 7 | `[3, 5, 7]` | | | | 1 |
| nothing | `[]` | `[]` | `[]` | `[]` | -1 |
