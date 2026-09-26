Building a log message (formatting, joining, `toString()` on big objects) costs time even when the message is then
thrown away. A logger that takes a `Supplier<String>` builds the text only when it will be used. A feature switch
is a natural `BooleanSupplier`: `boolean getAsBoolean()`, no boxing.

Write the class `LazyLog`:

- `LazyLog(BooleanSupplier enabled)` remembers the switch.
- `debug(Supplier<String> message)` records `message.get()` if the switch is on at that moment.
- `lines()` returns the recorded messages, oldest first.

| switch | calls | `lines()` |
|---|---|---|
| on | `debug(() -> "a")`, `debug(() -> "b")` | `["a", "b"]` |
| off | `debug(() -> "a")` | `[]` |

The switch may change while the program runs.
