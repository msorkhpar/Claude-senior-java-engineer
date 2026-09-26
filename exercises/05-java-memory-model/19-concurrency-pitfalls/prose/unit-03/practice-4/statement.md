The page's `ThisEscape` registers a lambda with an event source **inside its
constructor**, before `value = 42` runs. The source may call the listener at
once, and it sees `value == 0`: `this` escaped before the object was built,
which also voids the guarantee `final` fields give. The page's handling: a
static factory that builds the object first, then registers it.

Write `TemperatureAlarm`:

- `static TemperatureAlarm create(int threshold, EventSource source)` returns
  an alarm registered with `source`. `EventSource` (given) has one method,
  `void register(IntConsumer listener)`. A source may deliver readings from
  any thread, and may deliver one **during** `register` (for example, it
  replays the last reading it saw).
- The alarm counts every reading **strictly above** its threshold;
  `alarms()` returns that count and `threshold()` the threshold.
- Keep the alarm immutable apart from its count: every field is `final`.

| threshold | readings delivered | `alarms()` |
|---|---|---|
| 30 | 25, 31, 40 | 2 |
| 30 | 30 | 0 |
| 30 | 25 replayed during `register`, then 35 | 1 |
