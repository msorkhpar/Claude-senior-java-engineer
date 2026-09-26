The course's `Person` keeps its fields private and lets callers change them only
through setters that **validate** their input, so the object can never hold a bad
value.

Write the four accessors of `Person`:

- `setName(String name)` stores the name **without surrounding spaces**. A
  `null` name, or one that is empty after trimming, is refused with
  `IllegalArgumentException("Name cannot be null or empty")`.
- `setAge(int age)` accepts `0` to `150`, both included. Anything else is refused
  with `IllegalArgumentException("Age must be between 0 and 150")`.
- `getName()` and `getAge()` return what was stored.

A refused call leaves the stored value as it was.

Examples:

```
setName("  Jane Doe  ");  getName()   -> "Jane Doe"
setAge(30);               getAge()    -> 30
setName(null)                         -> IllegalArgumentException
setAge(151)                           -> IllegalArgumentException
```
