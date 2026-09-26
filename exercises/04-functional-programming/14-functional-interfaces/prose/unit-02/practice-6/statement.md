`Supplier.get()` cannot throw a checked exception, while `Callable.call()` declares `throws Exception`. Both take no
arguments and return a value, so the bridge from one to the other is a small wrapper.

Write `Tasks.unchecked(Callable<T> task)`. It returns a `Supplier<T>` whose `get()` calls the task and returns its
result. Failures leave `get()` like this:

| task throws | `get()` throws |
|---|---|
| `IOException` | `UncheckedIOException`, cause = that exception |
| another checked exception | `RuntimeException`, cause = that exception |
| a `RuntimeException` | the same exception |

| task | `unchecked(task).get()` |
|---|---|
| `() -> "hello"` | `"hello"` |

A `Supplier` is lazy: think about when the task should run, and how often.
