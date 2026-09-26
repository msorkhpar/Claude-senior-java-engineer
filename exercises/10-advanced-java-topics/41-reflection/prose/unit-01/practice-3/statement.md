Java has no single call for a full hierarchy. `getSuperclass()` gives one
parent, and returns **`null` for `Object`, interfaces, primitives and
`void`**. `getInterfaces()` gives only the interfaces a type **declares
directly**: interfaces reached through a superclass or through a
super-interface are not in it.

Write `Hierarchy`:

- `superclasses(type)`: `type`, then its parent, then that parent's parent,
  until `getSuperclass()` returns `null`.
- `allInterfaces(type)`: every interface `type` has, those declared on **any
  superclass** and every **super-interface** of those, each once. The order is
  not graded.

With `Shape implements Drawable`, `Scalable extends Resizable` and
`Circle extends Shape implements Scalable`:

| call | result |
|---|---|
| `superclasses(Circle.class)` | `[Circle, Shape, Object]` |
| `allInterfaces(Circle.class)` | `{Scalable, Resizable, Drawable}` |
| `superclasses(Scalable.class)` | `[Scalable]` |
| `superclasses(int.class)` | `[int]` |
