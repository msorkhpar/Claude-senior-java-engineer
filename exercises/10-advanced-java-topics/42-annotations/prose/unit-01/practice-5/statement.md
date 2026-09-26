`@FunctionalInterface` asks the compiler to check that an interface has
exactly one abstract method. The interface is functional whether or not it
carries the annotation, so a tool that wants to know must apply the rule
itself.

Write `Functional.isFunctional(Class<?> type)`. It returns `true` when `type`
is **an interface** with exactly one abstract method, counting:

- the abstract methods it declares **and those it inherits from its
  superinterfaces**;
- not its default methods, and **not its static methods**;
- not an abstract redeclaration of a public `Object` method such as
  `toString()` or `equals(Object)`. **Only `Object`'s exact signature is
  excused**: an abstract `equals(String)` still counts, and so does `clone()`,
  which is `protected` in `Object`, not public.

| type | functional? |
|---|---|
| `Transformer<T, R>`: `transform(T)` + default `andThen` | `true` |
| `Validator<T>`: `validate(T)` + abstract `toString()`, `equals(Object)` | `true` |
| `Matcher`: `matches(String)` + abstract `equals(String)` | `false` |
| `Copier`: `copy()` + abstract `Object clone()` | `false` |
| `Parser`: `parse(String)` + static `of()` | `true` |
| `interface Named extends Transformer<String, String> {}` | `true` |
| `interface Wider extends Transformer<String, String> { void reset(); }` | `false` |
| `abstract class Task` with one abstract `run()` | `false` |
