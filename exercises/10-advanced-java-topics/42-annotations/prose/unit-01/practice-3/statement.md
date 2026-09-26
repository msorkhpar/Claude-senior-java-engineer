`@Override` has SOURCE retention: the compiler checks it and then throws it
away, so `method.isAnnotationPresent(Override.class)` is always `false`. The
page's answer (Q4) is to inspect the class hierarchy instead, and warns that
its own snippet looks only at the direct superclass.

Write `Overrides.isOverride(Method m)`. It returns `true` when `m` is neither
`static` nor `private` and a **supertype** of its declaring class declares a
method that is neither `static` nor `private`, with the same name **and the
same parameter types**. Supertypes are **every superclass up to `Object`**
and every interface reached from them, directly or through other interfaces;
their abstract and default methods both count. A `private` method in a
supertype is not overridden.

| method | overrides? |
|---|---|
| `Dog.speak()`, `Animal` declares `speak()` | `true` |
| `Dog.fetch()`, new in `Dog` | `false` |
| `Dog.speak(String)` | `false`: an overload |
| `Dog.equals(Dog)` (the page's `equals` trap) | `false` |
| `Dog.toString()`, `Animal` does not declare it | `true`: `Object` does |
| `greet(String)` of a class implementing `Greetable` | `true` |
| `Dog.secret()`, `Animal.secret()` is private | `false` |
