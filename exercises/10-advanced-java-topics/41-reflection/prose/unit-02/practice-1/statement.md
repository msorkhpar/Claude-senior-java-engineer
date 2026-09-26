To reach a private field: find it with **`getDeclaredField`** (`getField` sees
public fields only), call **`setAccessible(true)`**, then `get` or `set`. For a
static field pass `null` as the instance. `getDeclaredField` does not look in
superclasses, so an inherited field needs a **walk up the hierarchy**.

Write `FieldAccess`:

- `read(target, name)`: the value of the field `name` of `target`, declared
  on its class or **any superclass**, at any access level. Primitives come
  back boxed.
- `write(target, name, value)`: sets that field.
- `readStatic(type, name)`: the value of a static field of `type`.
- A field that exists on no class of the hierarchy is a
  **`NoSuchFieldException`**.

| call | result |
|---|---|
| `read(point, "x")` with `x = 3` | `3` |
| `read(account, "owner")`, `owner` private | `"ann"` |
| `write(account, "balance", 2500)` | the private balance is `2500` |
| `read(savings, "owner")`, `owner` declared on the parent | `"bo"` |
| `read(point, "z")` | `NoSuchFieldException` |
