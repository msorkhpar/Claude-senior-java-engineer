The team settles performance arguments with `MicroBench`, a small helper of its own, because JMH
isn't on the build's class path. Last week it reported that the new coupon check costs 0.02 ns
per call, and that the old one, which nobody had touched, had become three times slower after
someone raised the number of warm-up rounds. Nobody believes either number.

Write `measure(...)` in `src/main/java/practice/MicroBench.java`. It gets the work, the inputs
to call it with, and the number of warm-up rounds; it runs the work over every input in each
warm-up round and then in one timed round. The report it returns gives:

- `nanosPerCall`: the time per call of the timed round;
- `checksum`: the sum of every value the work returned.

When you're done, the numbers describe the work as it would run in production, whatever the JIT
is allowed to do with the loop. About seven lines.

The tests pass a clock of their own, so no test depends on how fast the machine is.
