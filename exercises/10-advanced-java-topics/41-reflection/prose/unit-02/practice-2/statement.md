`Method.invoke(target, args...)` calls a method; pass `null` as the target for
a static method. A private method needs **`setAccessible(true)`** first. When
the invoked method throws, `invoke` does not let the exception through: it
wraps it in `InvocationTargetException`, and callers catching
`RuntimeException` miss it. **Unwrap it** with `getCause()`.

Write `Invoker`:

- `call(target, name, types, args...)`: finds the method `name` with exactly
  those parameter types on `target`'s class (any access level) and invokes it.
- `callStatic(type, name, types, args...)`: the same for a static method.
- When the invoked method throws an **unchecked exception** (or an `Error`),
  the caller gets **that exception itself**. When it throws a **checked
  exception**, the caller gets an `IllegalStateException` whose **cause is the
  method's exception**.
- Lookup and access problems stay `ReflectiveOperationException`s.

| call | result |
|---|---|
| `call(calc, "add", {int, int}, 3, 4)` | `7` |
| `callStatic(Calculator.class, "twice", {int}, 21)` | `42` |
| `call(calc, "reset", {})` (a `void` method) | `null` |
| `call(calc, "secretMultiply", {int, int}, 6, 7)` (private) | `42` |
| `call(calc, "secretDivide", {int, int}, 6, 0)` | `ArithmeticException` |
| `call(calc, "check", {})` (throws `AssertionError`) | that `AssertionError` |
| `call(calc, "load", {String}, "a.txt")` (throws `IOException`) | `IllegalStateException`, cause the `IOException` |
