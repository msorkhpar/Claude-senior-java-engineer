A plain compare-and-set only asks *is the value still what I read?* If another thread
changed it from `A` to `B` and back to `A` in between, the CAS succeeds although the
value did change: the **ABA problem**. The page's fix is `AtomicStampedReference`,
which pairs the reference with an integer **stamp** (a version) and compares both.

Write `VersionedValue`, a value with a stamp that starts at 0:

- `value()` and `stamp()` read them;
- `set(newValue)` stores the value and adds one to the stamp;
- `compareAndSet(expectedValue, expectedStamp, newValue)` succeeds only if **both**
  still match (the value by reference, as a CAS does); then it stores `newValue`,
  adds one to the stamp and returns `true`. Otherwise it changes nothing and returns
  `false`.

The examples use string literals, which are the same reference each time.

| steps (starting at `"A"`) | answer | `value()`, `stamp()` |
|---|---|---|
| `compareAndSet("A", 0, "C")` | `true` | `"C"`, 1 |
| (fresh) `set("B")`, `set("A")` | | `"A"`, 2 |
| then `compareAndSet("A", 0, "C")` | `false` | `"A"`, 2 |
| then `compareAndSet("A", 2, "C")` | `true` | `"C"`, 3 |
| (fresh, holding `new String("A")`) `compareAndSet(` another `new String("A")`, `0, "C")` | `false` | unchanged |
