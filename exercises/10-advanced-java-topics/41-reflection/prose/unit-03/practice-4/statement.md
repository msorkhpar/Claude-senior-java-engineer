`java.lang.reflect.Array` builds and fills arrays whose element type is only
known at run time: `Array.newInstance(type, length)` (or several dimensions),
`Array.get`, `Array.set` (which unboxes into a primitive array and **refuses
`null` there** with `IllegalArgumentException`) and `Array.getLength`. The
result is an `Object`; its **component type** is
`array.getClass().getComponentType()`, even for an empty array.

Write `ArrayMaker`:

- `filled(type, length, value)`: a new array of `type` with every element
  set to `value`.
- `resize(array, newLength)`: a new array of the **same component type**
  (primitive arrays stay primitive), holding the old elements that fit; the
  rest keep their default value.
- `grid(type, rows, cols)`: a two-dimensional array whose **every row
  exists** with `cols` elements.

| call | result |
|---|---|
| `filled(String.class, 3, "x")` | `String[] {"x", "x", "x"}` |
| `filled(int.class, 2, 7)` | `int[] {7, 7}` |
| `filled(int.class, 2, null)` | `IllegalArgumentException` |
| `resize(new int[] {1, 2}, 4)` | `int[] {1, 2, 0, 0}` |
| `resize(new String[] {"a", "b", "c"}, 2)` | `String[] {"a", "b"}` |
| `grid(int.class, 2, 3)` | `int[2][3]` |
