A plugin system reads a class name from configuration and creates it:
`Class.forName(name)`, then `getDeclaredConstructor()`, `setAccessible(true)`
and `newInstance()`. Configuration can be wrong, so **check the class before
creating anything**: it must be a `Plugin`, and an **abstract class or an
interface** cannot be instantiated at all.

Write `PluginLoader.load(className)` for the nested interface
`PluginLoader.Plugin` (given):

- It returns a **new** instance of the named class, made with its no-arg
  constructor, **at any access level**.
- It throws **`IllegalArgumentException`** for a name no class has, for a
  class that is **not a `Plugin`**, and for an **abstract** plugin class, in
  each case **without running any constructor**.
- Any other reflective failure becomes an `IllegalStateException`.

| class name | result |
|---|---|
| `DefaultPlugin` (implements `Plugin`) | a plugin; `execute()` is `"Default executed"` |
| `HiddenPlugin` (private constructor) | a plugin |
| `NotAPlugin` | `IllegalArgumentException`, no instance made |
| `AbstractPlugin` (abstract, implements `Plugin`) | `IllegalArgumentException` |
| `practice.NoSuchPlugin` | `IllegalArgumentException` |
