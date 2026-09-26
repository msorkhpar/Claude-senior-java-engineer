**Overloading** is compile-time polymorphism: several methods share a name and
differ in their parameter lists. The **compiler** picks one from the
arguments' **static** (declared) types, choosing the most specific overload
that applies. The return type alone never tells two overloads apart.

Write the lesson's `Calculator`, plus three `kind` overloads:

- `int add(int a, int b)`, `double add(double a, double b)` and
  `int add(int a, int b, int c)` return the sum of their arguments.
- `String kind(String s)` returns `"String"`, `String kind(Integer i)` returns
  `"Integer"`, and `String kind(Object o)` returns `"Object"`.

Each overload answers only for its own parameter type: `kind(Object)` does not
look at what the object really is. Which overload runs is the compiler's
decision, made from the type the caller declared.

## Examples

```
add(5, 10)            -> 15
add(5.5, 10.5)        -> 16.0
add(5, 10, 15)        -> 30
kind("hi")            -> "String"
kind(List.of())       -> "Object"
Object o = "hi"; kind(o) -> "Object"
kind(7)               -> "Integer"
```
