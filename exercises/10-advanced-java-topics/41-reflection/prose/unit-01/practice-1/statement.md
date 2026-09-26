There are three ways to a `Class` object: `obj.getClass()`, a literal such as
`String.class`, and `Class.forName(name)`. They all reach the **same** object.
Only `forName` works from a string, so it is the one a plugin system or a
config file uses.

Write `ClassLookup`:

- `load(name, initialize)` loads the class with that name through
  `ClassLookup`'s own class loader. When `initialize` is `true` the class's
  **static initializer runs**; when it is `false` the class is **loaded but not
  initialized**. A name no class has gives **`Optional.empty()`**
  (`ClassNotFoundException` is checked: handle it).
- `nameFor(type)` returns the name that `load` accepts back. For a nested
  class that is the **binary name** `Outer$Inner`, not the source-style
  `Outer.Inner`.

| call | result |
|---|---|
| `load("java.util.ArrayList", true)` | the same object as `ArrayList.class` |
| `load("practice.NoSuchType", true)` | `Optional.empty()` |
| `load(name, false)` | the class, static block not run yet |
| `nameFor(Outer.Inner.class)` | `"practice.Outer$Inner"` |
