A class loader follows the **parent delegation model**: asked for a class, it
first asks its parent, and only when the parent cannot load it does it define
the class itself. A class's runtime identity is its name **plus** the loader
that defined it. A `null` parent means the bootstrap loader.

Write `BytesLoader extends ClassLoader`. `new BytesLoader(parent, classes)`
gets a parent (possibly `null`) and a map from binary class name to the bytes
of a `.class` file.

- `loadClass(name)` asks the parent chain first, and defines the class from
  the map only when no parent can load it.
- One loader defines a name **once**: asking again returns the same `Class`.
- Two loaders that each define the same bytes hold two different classes.
- A name that no parent and no map entry knows fails with
  `ClassNotFoundException`.

| loaders | call | result |
|---|---|---|
| `a = new BytesLoader(null, {Plugin})` | `a.loadClass("...Plugin")` | a new class, defined by `a` |
| `a` | `a.loadClass("java.lang.String")` | `String.class` (bootstrap) |
| `child = new BytesLoader(a, {Plugin})` | `child.loadClass("...Plugin")` | the class defined by `a` |
| `a` and `b`, both `(null, {Plugin})` | load `Plugin` from each | two classes, same name |
| `a` | `a.loadClass("practice.Missing")` | `ClassNotFoundException` |
