The payment team runs chaos tests against its gateway: several workers send calls at the same
time, each on its own thread, and `ChaosRun` decides which of each worker's calls fail. Each run
is named by an algorithm and a seed, which the report prints, for example
`L128X256MixRandom / 4417`. Last week a run found a double charge, and nobody could make it happen
again from the name in the report.

`ChaosRun.failingCalls(...)` already makes one draw per call. Finish the `TODO` in
`src/main/java/practice/ChaosRun.java`: `generatorFor(worker)` returns the generator that worker
draws from in this run.

When you're done, every worker draws with the algorithm the run is named by, and a new `ChaosRun`
with the same algorithm and seed fails exactly the same calls for every worker, in whatever order
the workers start. Within one run the workers fail different
calls from each other, and draw on their own threads without getting in each other's way. About
three lines.
