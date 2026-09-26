The page's leak pattern 3: a stack backed by an array that pops with
`return elements[--size]` still holds a reference to the popped element in the
array. The object is unreachable for the program but **reachable for the garbage
collector**, so it is never freed. The fix: **pop clears the slot it frees**.

Write `ArrayStack<E>`, backed by an `Object[]` field (the tests look inside it):

- `new ArrayStack<>(capacity)`; `push(e)`; **the array grows when it is full**.
- `pop()` removes and returns the top element; `peek()` returns it without
  removing it; **an empty stack refuses pop and peek** with
  `java.util.EmptyStackException`.
- `size()`.

| calls | result |
|---|---|
| push a, b, c; pop; pop | `c`, `b`; `size()` is 1; `peek()` is `a` |
| after those pops | array slots 1 and 2 are `null` |
| capacity 2, push 100 values | all 100 pop back, last first |
| `pop()` on an empty stack | `EmptyStackException` |
