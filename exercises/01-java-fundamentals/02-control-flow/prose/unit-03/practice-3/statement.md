Walking a linked list is a classic `while` loop: follow `next` until it is `null`. The
number of links is not known in advance, and a list may have none at all, so the
condition has to be checked before the first pass.

`Chain` gives you a list link, `Chain.Node(int value, Node next)`. An empty list is a
`null` head.

Write `indexOf(Node head, int target)` in `Chain`. It returns the position (from `0`) of
the first link whose value is `target`, or `-1` when no link holds it.

Examples, for the list `1 -> 3 -> 5 -> 3`: `indexOf(head, 1)` is `0`, `indexOf(head, 5)`
is `2`, `indexOf(head, 3)` is `1`, and `indexOf(head, 4)` is `-1`. For the empty list,
`indexOf(null, 1)` is `-1`.
