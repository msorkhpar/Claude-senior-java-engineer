The search service's latency jumps for a few seconds several times a day, and the team now runs
it with `-XX:+PrintCompilation`. Each jump comes with a burst of `made not entrant` lines, and
nobody can tell by eye which of them matter. The on-call engineer wants a small tool that, for
one method, answers two questions from the log alone.

`CompileLog` already reads the log: it skips every line that isn't a compile line and turns the
rest into `Line` records, in log order. Finish the two `TODO`s in
`src/main/java/practice/CompileLog.java`:

- `currentTier(method)`: the tier of the code that a call to the method made now would enter, or
  `0` for the interpreter;
- `deoptimizations(method)`: how many times the JIT threw away the method's code because it could
  no longer be used. The method's ordinary progress through the tiers is not an incident and
  doesn't count.

Both answers are about the code that calls enter. About fifteen lines in total.

The tests feed logs of other services' methods, with other ids and timestamps.
