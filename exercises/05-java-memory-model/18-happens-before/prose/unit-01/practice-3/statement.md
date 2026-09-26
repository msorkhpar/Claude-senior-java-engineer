Two of the page's six rules carry values between threads without any lock or
`volatile`: **thread start** (everything before `t.start()` is visible inside `t`) and
**thread join** (everything `t` did is visible once `t.join()` returns). The page's
`ThreadStartHappensBefore` and `CorrectOrdering` examples lean on exactly these.

Write `ParallelSum.sum(List<LongSupplier> parts)`. It starts **one new thread per
part**; each thread calls its part and stores the result in a plain `long[]` slot of its
own (no `volatile`, no lock, no atomic). The method then returns the total of all the
slots. The start and join rules are what make the slots safe to read, so read them only
when they are guaranteed visible.

| parts | answer |
|---|---|
| `() -> 1, () -> 2, () -> 3` | `6` |
| `() -> 40, () -> 2` | `42` |
| none | `0` |

Rules:

- every part runs on a thread you started for it, never on the calling thread;
- a part may take a long time: the total must still include it.
