The Holder idiom (Bill Pugh) **keeps the instance in a private static nested
class**. The JVM initializes that nested class **only when `getInstance()` first
reads it**, and class initialization is thread-safe, so the instance is lazy
without any lock. Using the outer class for anything else creates nothing.

Write `ServiceRegistry` that way:

- `getInstance()` returns the one registry.
- `created()` (given) counts how many times the constructor has run.
- `describe()` (given) is another static method; calling it must **not**
  create the instance.
- The private constructor **refuses to run a second time** with an
  `IllegalStateException`, because reflection can call a private constructor.

| calls, in order | `created()` after it |
|---|---|
| `describe()` | `0` |
| `getInstance()` | `1` |
| `getInstance()` again | `1`, same object |
| the constructor through reflection | `IllegalStateException` |
