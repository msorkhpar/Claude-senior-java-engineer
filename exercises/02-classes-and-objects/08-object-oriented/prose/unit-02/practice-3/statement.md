Only **non-static, non-private** methods are virtual. A static method with the
same signature in a subclass **hides** the superclass's one: a call is bound at
compile time to the declared type. A private method is never dispatched: a
subclass method with the same name is a different method.

In `Virtuals`, write:

- `Base.kind()` returns `"base"`, and `Derived` overrides it to return
  `"derived"`.
- `Base.label()` is **static** and returns `"Base"`; `Derived.label()` is
  **static** and returns `"Derived"` (it hides `Base.label()`).
- `Virtuals.kindOf(Base b)` returns the kind of the object `b` refers to,
  whatever the declared type.
- `Base.introduce()` returns `"I am " + secret()`, where `Base.secret()` is
  **private** and returns `"base"`. `Derived` has its **own private**
  `secret()` returning `"derived"`. Keep both helpers private.

## Examples

```
new Base().kind()          -> "base"
Base.label()               -> "Base"
Derived.label()            -> "Derived"
Base b = new Derived();
kindOf(b)                  -> "derived"
b.introduce()              -> "I am base"
```
