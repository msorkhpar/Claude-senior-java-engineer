`getDeclaredMethod(name, types...)` wants the **exact** parameter types.
Autoboxing does not apply to lookup: a method `add(int, int)` is not found
with `Integer.class, Integer.class`. With overloads the types pick the
method. `getDeclaredMethod` searches only the class itself, and
`getMethod` finds public methods only.

Write `MethodFinder`:

- `find(type, name, params...)`: the method with exactly these parameter
  types, declared on `type` or **any superclass** at **any access level**, or
  `Optional.empty()`.
- `overloads(type, name)`: `"name(Type,Type)"` (simple type names, no
  spaces) for every method `type` itself declares with that name, **private
  ones too**, sorted.

With `Base { private String secret(); }` and
`MathOps extends Base { public int add(int,int); public double add(double,double); private long add(long,long); ... }`:

| call | result |
|---|---|
| `find(MathOps.class, "add", int.class, int.class)` | the `int` version |
| `find(MathOps.class, "add", Integer.class, Integer.class)` | empty |
| `find(MathOps.class, "secret")` | `Base.secret` |
| `overloads(MathOps.class, "add")` | `[add(double,double), add(int,int), add(long,long)]` |
