One try-with-resources statement can manage several resources. They are declared in the order they
depend on each other and closed in the reverse order, and each one that was opened is closed.

Write `Transfer.copy(Opener opener)`. It opens a channel named `"in"`, then a channel named `"out"`,
writes to `out` what `in.read()` returns, and closes both.

With channels that record what happens to them, a successful copy of `"data"` gives:

```
open in, open out, read in, write out data, close out, close in
```

Every exception reaches the caller. When the copy fails and a close fails too, the copy's exception is
the one thrown, with the close failure added to it as suppressed, and every opened channel is still
closed. Consider what must happen to `"in"` when `"out"` cannot be opened at all, whatever the
opener throws.
