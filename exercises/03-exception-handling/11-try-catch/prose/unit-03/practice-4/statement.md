Returning from a `finally` block overrides whatever the `try` or `catch` block was doing, a thrown
exception included, so a `finally` block holds cleanup and never a `return`.

Write the class `Meter`, which runs tasks and counts the calls in flight:

- `int call(IntSupplier task)` counts one more call in flight, returns `task.getAsInt()`, and counts
  the call as finished however the task ends; an exception from the task reaches the caller
  unchanged;
- `int inFlight()` returns how many calls of **this** meter are running right now (calls may nest,
  and each meter counts its own).

| step | `inFlight()` |
|---|---|
| `new Meter()` | `0` |
| inside the task of `meter.call(...)` | `1` |
| after `call` returns | `0` |

For example, `meter.call(() -> meter.inFlight() * 10)` returns `10`. Check what happens to both the
count and the exception when a task throws, whatever it throws, an `Error` included.
