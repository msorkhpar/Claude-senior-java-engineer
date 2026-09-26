`instanceof` needs its type at compile time. When the type comes from
configuration you only have a `Class` object, and `Class.isInstance(obj)` is
the runtime equivalent: it is **polymorphic** (subclasses and implemented
interfaces count) and **`null` is an instance of nothing**. A manual
`obj.getClass() == type` misses subtypes.

`getModifiers()` returns an `int` bitmask; `java.lang.reflect.Modifier`
decodes it. Mind that an **interface carries the abstract bit** too.

Write `TypeGuard`:

- `accepts(type, value)`: whether `value` is an instance of `type`.
- `isAbstractClass(type)`: whether `type` is an abstract **class** (not an
  interface).
- `modifiers(type)`: the modifiers as Java source spells them, in
  `Modifier.toString` order.

| call | result |
|---|---|
| `accepts(CharSequence.class, new StringBuilder("x"))` | `true` |
| `accepts(Integer.class, "x")` | `false` |
| `accepts(String.class, null)` | `false` |
| `isAbstractClass(AbstractList.class)` | `true` |
| `isAbstractClass(Runnable.class)` | `false` |
| `modifiers(String.class)` | `"public final"` |
