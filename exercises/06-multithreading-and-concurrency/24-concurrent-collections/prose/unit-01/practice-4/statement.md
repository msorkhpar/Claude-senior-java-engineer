The page's typical `CopyOnWriteArrayList` use is an **event listener
registry**: listeners register rarely, events fire often, and a listener may
change the registry while an event is being delivered. Its `addIfAbsent`
refuses a listener that is already there.

Write the class `Listeners`, a registry whose listeners may change it while an
event is being delivered. (The page's choice, `CopyOnWriteArrayList`, also makes
it safe to share between threads; the tests check the single-threaded rules
below.)

- `register(Consumer<String> listener)` adds the listener and returns `true`,
  or returns `false` if that listener is already registered.
- `unregister(Consumer<String> listener)` removes it, returning whether it was
  there.
- `fire(String event)` passes `event` to each listener registered **when the
  call began**, in registration order, and returns how many it called.
  A listener may call `register` or `unregister` from inside `fire`: a listener
  added that way hears only later events, and a listener removed that way does
  not stop the others from hearing this one.

| calls | answer |
|---|---|
| `register(a)`, `register(b)`, `fire("x")` | `2`; `a` and `b` each got `"x"` |
| `register(a)`, `register(a)` | `true`, then `false` |
| `a` registers `b` when called; `register(a)`, `fire("1")`, `fire("2")` | `1`, then `2`; `b` got only `"2"` |
| `a` unregisters itself when called; `register(a)`, `register(b)`, `fire("1")`, `fire("2")` | `2`, then `1` |
