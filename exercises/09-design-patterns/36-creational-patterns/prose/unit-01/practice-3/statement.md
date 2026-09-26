The page lists the ways a class-based Singleton breaks: **reflection** calls
the private constructor, **deserialization** makes a new object, and **cloning**
copies it when a superclass exposes `clone()`. Its `SecureSingleton` guards all
three, and notes a trap: static initializers run **in textual order**, so a
guard flag must be declared (and initialized) **before** the instance.

`Base` (given) implements `Cloneable` and has a public `clone()`. Write
`Settings extends Base implements Serializable` so that:

- `getInstance()` returns the one `Settings`; `name()` returns `"settings"`.
- Serializing it and reading it back gives **the same instance**.
- A second construction through reflection throws `IllegalStateException`.
- `clone()` throws `CloneNotSupportedException`.

| action | result |
|---|---|
| `getInstance()` twice | the same object |
| write then read with object streams | the same object |
| private constructor via reflection | `IllegalStateException` (inside `InvocationTargetException`) |
| `clone()` | `CloneNotSupportedException` |
