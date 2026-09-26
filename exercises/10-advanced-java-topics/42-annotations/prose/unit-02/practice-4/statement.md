The page's Q5 builds a validation framework in three parts: constraint
annotations, a class whose fields carry them, and a processor that reads them
through reflection. `@NotEmpty(message)` and `@Range(min, max, message)` are
given, with RUNTIME retention and default messages.

Write `Validator.validate(Object obj)`. It checks every field `obj`'s class
declares (private ones too) and returns one `Violation(field, message)` per
broken rule, sorted by field name:

- `@NotEmpty`: the value is `null` or a String that is **blank** (only
  whitespace counts as empty too).
- `@Range`: the value is a number outside `min..max`, **both ends
  inclusive**.

**Every violation is reported**, not only the first. Each uses its
annotation's `message()`.

| object | violations |
|---|---|
| `User("Ada", 36)` | `[]` |
| `User(null, 36)` | `[name: Name is required]` |
| `User("   ", 36)` | `[name: Name is required]` |
| `User("Ada", 0)`, `User("Ada", 150)` | `[]` |
| `User("Ada", 151)` | `[age: Invalid age]` |
| `User("", 200)` | `[age: Invalid age, name: Name is required]` |
