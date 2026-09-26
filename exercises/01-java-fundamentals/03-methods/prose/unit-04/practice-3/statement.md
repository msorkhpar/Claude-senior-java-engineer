`List<Integer>` has two `remove` overloads: `remove(int index)` removes the element at a
position, and `remove(Object o)` removes the first element equal to `o`. Passed an `int`,
the call picks `remove(int)`, the exact match, before boxing is ever tried.

Write `removeValue(List<Integer> values, int value)` in `Removal`. It removes the first
element equal to `value` and returns whether one was removed:

- with `values = [10, 20, 30]`, `removeValue(values, 20)` returns `true` and leaves
  `[10, 30]`;
- a value not in the list removes nothing: `removeValue(values, 99)` returns `false`;
- only the first equal element goes: `[5, 7, 5]` becomes `[7, 5]`;
- large values are compared by value too: removing `2000` from `[1000, 2000, 1000]` leaves
  `[1000, 1000]`.
