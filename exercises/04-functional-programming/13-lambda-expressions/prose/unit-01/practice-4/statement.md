A lambda does not open a scope of its own: inside its body, `this` is the object
of the enclosing class. An anonymous inner class is a new object, so `this`
inside it is that anonymous object. A lambda may also update the enclosing
object's **fields**, because they belong to the object, not to the method.

`Tally` holds an `int count` field (read with `count()`). Write three methods:

1. `incrementer()` returns a `Runnable` lambda that adds one to `count` each
   time it runs.
2. `selfFromLambda()` returns a `Supplier<Object>` written **as a lambda**, such
   as `() -> this`, whose `get()` gives back the object `this` means there.
3. `selfFromAnonymousClass()` returns a `Supplier<Object>` written **as an
   anonymous inner class** whose `get()` returns `this`.

| code | result |
|---|---|
| `t.incrementer()` run 3 times, then `t.count()` | `3` |
| `t.selfFromLambda().get()` | the object `t` |
| `s = t.selfFromAnonymousClass(); s.get()` | the object `s` |

The tests check which object each `this` turns out to be.
