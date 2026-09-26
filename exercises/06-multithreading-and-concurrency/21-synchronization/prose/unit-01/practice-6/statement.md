Two of the page's pitfalls meet here. A lock held too long costs every other
thread that wants it, and **calling alien code**, such as a listener, while
holding a lock is risky: the listener may block, or take another lock and
deadlock. The page's fix is to update the state under the lock, **release it**,
and only then call the listener.

Write `Thermostat`:

- `void addListener(IntConsumer listener)`.
- `void set(int temperature)` stores the temperature under the thermostat's
  lock, then calls **every** listener with it, **after** the lock is released.
- `int get()` reads the temperature under the same lock.
- A listener may call `get()`, and may add another listener while it runs. A
  listener added during a `set` is called from the next `set` on.

| calls | the listener hears |
|---|---|
| `set(20)`, `set(22)` | `20`, then `22` |
| a listener that calls `get()` during `set(25)` | `get()` returns `25` |
| a listener that asks another thread to `get()` and waits for it | that thread gets its answer while the listener is still running |
