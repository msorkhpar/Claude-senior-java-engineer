A consumer that calls a method on its input throws `NullPointerException` the moment a list hands it a `null`.
A small wrapper keeps that concern apart from the business logic.

Write two methods in `NullSkipping`:

1. `nullSafe(Consumer<T> downstream)` returns a consumer that passes non-null inputs to `downstream` and silently
   ignores `null`.
2. `forEachCounting(List<T> items, Consumer<T> action)` runs `action` on every non-null item, in order, and returns
   how many items it ran on. With `forEach`, note that a lambda cannot change a local `int`, so the count lives in something
   whose reference stays fixed.

| call | action sees | returns |
|---|---|---|
| `forEachCounting(["Alice", "Bob"], …)` | `Alice`, `Bob` | `2` |
| `forEachCounting([], …)` | nothing | `0` |

Lists may contain `null`, and the count must match what the action actually saw.
