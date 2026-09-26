Adding an abstract method to an interface breaks every class that already
implements it. A **default method** (Java 8+) adds behaviour without breaking
them: existing implementers inherit it, and it can call the interface's
abstract methods, so each implementer's own behaviour shows through.

`Greetings.Greeter` has one abstract method, `String greet(String name)`.
`English` and `French` implemented it before anything else existed, and they
must keep compiling unchanged. Write:

- `English.greet` returns `"Hello, <name>!"`; `French.greet` returns
  `"Bonjour, <name> !"`.
- A new **default** method `List<String> greetAll(List<String> names)` in
  `Greeter` that greets every name, in order, with **this** greeter's `greet`.
- `Shy`, a greeter whose `greet` is `"hi <name>"` and which **overrides**
  `greetAll` to greet only the first name (an empty list for no names).

## Examples

```
new English().greetAll(["Ann", "Bo"]) -> ["Hello, Ann!", "Hello, Bo!"]
new French().greetAll(["Ann"])        -> ["Bonjour, Ann !"]
new Shy().greetAll(["Ann", "Bo"])     -> ["hi Ann"]
new English().greetAll([])            -> []
new Shy().greetAll([])                -> []
```
