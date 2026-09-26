A `MethodHandle` is a typed, directly executable reference to a method or a
field. Access is checked **once, when the handle is looked up**, not on each
call, and `invokeExact` calls it with **its exact type**, no `Object[]` and no
boxing, which is what lets the JIT inline it.

A `MethodHandles.Lookup` finds handles: `findVirtual` for an instance method
(the handle's first parameter is the receiver), **`findStatic`** for a static
one (no receiver), `findGetter` for a field. `MethodHandles.lookup()` sees
what its own class may see; for **private members of another class** use
**`MethodHandles.privateLookupIn(owner, MethodHandles.lookup())`**.

Write `Handles`:

- `virtual(owner, name, type)`: a handle for an instance method, of any
  access level, whose type is exactly the method's type with the receiver
  first.
- `staticMethod(owner, name, type)`: a handle for a static method.
- `getter(owner, field, fieldType)`: a handle reading an instance field, of
  any access level.

| handle | call | result |
|---|---|---|
| `virtual(Calculator.class, "multiply", methodType(int.class, int.class, int.class))` | `(int) h.invokeExact(calc, 6, 7)` | `42` |
| `staticMethod(Calculator.class, "add", methodType(int.class, int.class, int.class))` | `(int) h.invokeExact(3, 4)` | `7` |
| `getter(Calculator.class, "memory", int.class)` | `h.invoke(calc)` | `5` |
| `getter(Calculator.class, "secret", String.class)`, `secret` private | `h.invoke(calc)` | `"s3"` |
