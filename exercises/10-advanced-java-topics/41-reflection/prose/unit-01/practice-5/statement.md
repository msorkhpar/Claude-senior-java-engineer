Every type has a `Class` object: primitives, arrays and `void` too. The
`is...()` methods tell them apart, but they overlap: an annotation type
**is also an interface**, and `getSuperclass()` returns `null` for primitives
**and for `Object` and interfaces**, so a missing superclass does not mean
primitive. A multi-dimensional array is an **array of arrays**:
`int[][].class.getComponentType()` is `int[].class`.

Write `TypeKinds`:

- `kind(type)` returns one of `"primitive"`, `"array"`, `"annotation"`,
  `"interface"`, `"enum"`, `"record"` or `"class"`.
- `elementType(type)` returns the **innermost** element type of an array
  type, or `null` for a type that is not an array.

| call | result |
|---|---|
| `kind(String.class)` | `"class"` |
| `kind(int[].class)` | `"array"` |
| `kind(Override.class)` | `"annotation"` |
| `kind(void.class)` | `"primitive"` |
| `kind(Object.class)` | `"class"` |
| `elementType(int[][].class)` | `int.class` |
| `elementType(String.class)` | `null` |
