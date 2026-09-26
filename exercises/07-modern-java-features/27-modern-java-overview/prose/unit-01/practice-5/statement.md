Java 8 lets an interface carry default methods (with a body that
implementations inherit) and static methods (called on the interface).
An interface with one abstract method stays a functional interface, so a lambda
can implement it and still inherit its defaults. When a class inherits the same
default method from two unrelated interfaces, it must override it and may call
each one as `X.super.method()`.

Complete the nested types of `Greeters`:

- `Greeter.greetWithTitle(String title, String name)` is a default method: it
  greets `title + " " + name` using **the implementation's own `greet`**.
- `Greeter.standard()` is a static method returning a greeter whose
  `greet(name)` is `"Hello, " + name + "!"`.
- `Bilingual` inherits `hello()` from both `English` (`"Hello"`) and `French`
  (`"Bonjour"`). Its `hello()` returns both greetings, English first: `"Hello / Bonjour"`.

| call | answer |
|---|---|
| `Greeter.standard().greet("Ada")` | `"Hello, Ada!"` |
| `Greeter.standard().greetWithTitle("Dr.", "Ada")` | `"Hello, Dr. Ada!"` |
| `((Greeter) n -> "Hi " + n).greetWithTitle("Dr.", "Ada")` | `"Hi Dr. Ada"` |
| `new Bilingual().hello()` | `"Hello / Bonjour"` |
