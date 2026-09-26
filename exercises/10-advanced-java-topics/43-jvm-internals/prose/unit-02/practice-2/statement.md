javac adds methods the source never shows. A **bridge method** keeps
overriding working after type erasure or a covariant return: for
`class Upper implements Transformer<String>` it adds
`Object transform(Object)`, which casts and calls `String transform(String)`.
A bridge is flagged both *bridge* and *synthetic*. A lambda body becomes a
private method such as `lambda$greeting$0`, which is **synthetic but not a
bridge**.

Write `Bridges` with two methods over a class's **declared** methods, each
method written as `ReturnType name(Param1, Param2)` with simple type names,
in any order:

- `bridgesOf(type)`: the bridge methods only.
- `sourceMethodsOf(type)`: the methods the source declares, leaving out
  **every synthetic method**.

| class | `bridgesOf` | `sourceMethodsOf` |
|---|---|---|
| `Upper implements Transformer<String>` | `Object transform(Object)` | `String transform(String)` |
| `Word implements Comparable<Word>` | `int compareTo(Object)` | `int compareTo(Word)` |
| `Circle extends Shape`, overriding `Shape copy()` as `Circle copy()` | `Shape copy()` | `Circle copy()` |
| `Greeter` with `Supplier<String> greeting()` returning a lambda | none | `Supplier greeting()` |
