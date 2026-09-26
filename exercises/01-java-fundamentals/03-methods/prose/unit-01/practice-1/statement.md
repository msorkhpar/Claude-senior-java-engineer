A static field belongs to the class, so every object shares it; an instance field belongs to
one object. A static method is called on the class (`Counter.total()`) and cannot touch
instance fields; an instance method is called on an object (`c.click()`) and can use both.

Complete `Counter`. Each counter counts its own clicks, and the class keeps a running total
of the clicks of every counter:

- `click()` adds one click to this counter, and to the total;
- `count()` returns this counter's clicks;
- `Counter.total()` returns the clicks of all counters together;
- `Counter.resetTotal()` sets the total back to `0` (tests call it first).

Example: after `a.click(); a.click(); b.click();`, `a.count()` is `2`, `b.count()` is `1`
and `Counter.total()` is `3`.
