The page recommends combining sealed classes with records for immutable data
structures. `IntList` is a list of `int`s built that way:

```text
sealed interface Node permits Empty, Cons
record Empty()                      // the empty list
record Cons(int head, Node tail)    // a value in front of a list
```

A record's fields are final, so no list can ever change: every operation
returns a **new** list, and may safely reuse parts of an old one.

Write, each with a `switch` over `Node` where it walks the list:

1. `static Node of(int... values)` — the list of `values` in order;
2. `static int sum(Node list)` — the sum of its values;
3. `static Node append(Node list, int value)` — a new list with `value` added at the end;
4. `static Node prepend(Node list, int value)` — a new list with `value` in
   front, whose tail **is** `list` itself (nothing is copied).

Records compare by their fields, so two lists with the same values are `equals`.

**Examples**

- `of(1, 2, 3)` -> `Cons[head=1, tail=Cons[head=2, tail=Cons[head=3, tail=Empty[]]]]`
- `sum(of(1, 2, 3))` -> `6`
- `append(of(1, 2), 3)` equals `of(1, 2, 3)`, and `of(1, 2)` is unchanged
- `of()` -> `Empty[]`
