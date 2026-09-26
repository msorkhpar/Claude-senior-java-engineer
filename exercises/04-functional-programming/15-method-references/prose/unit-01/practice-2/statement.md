The same method, `String::toUpperCase`, can be a **bound** reference (the receiver is
one object, fixed when the reference is made) or an **unbound** one (the receiver is
whatever string the function is handed).

A `Holder` keeps one changeable string (`get()` and `set(String)`). Write three methods
in `Shout`:

1. `Supplier<String> of(Holder holder)` returns a supplier that upper-cases the value
   the holder has **when `of` is called**. A bound reference behaves this way.
2. `Function<String, String> each()` upper-cases whatever string it is given, as an
   unbound reference does.
3. `List<String> all(List<String> words)` upper-cases every word, keeping every word (repeats
   included) in its original position.

| call | result |
|---|---|
| `of(new Holder("hello")).get()` | `"HELLO"` |
| `each().apply("goodbye")` | `"GOODBYE"` |
| `all(List.of("a", "b"))` | `["A", "B"]` |

If the holder's value is `null`, `of` throws a `NullPointerException` with the message
`value must not be null` straight away. Think about when a bound reference reads its
receiver, and what that means when the holder changes afterwards.
