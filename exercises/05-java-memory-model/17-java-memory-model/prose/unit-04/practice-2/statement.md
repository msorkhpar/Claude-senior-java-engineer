This practice continues the previous one: the starter is its solution, with a second machine
added. The baseline model can't explain what the team saw on an x86 server running in the
interpreter: two threads that each wrote a flag and then read the other's both read 0, while a
reader that saw a `ready` flag always saw the data written before it.

`outcomesWithStoreBuffers` models that server. Each thread's writes go into a buffer of its own
and reach shared memory later, one at a time; at every step the checker tries both a thread's
next operation and moving one buffered write to memory. A new operation, `fence()`, waits until
the thread's own buffer is empty. Finish the three `TODO` methods of `Buffered` in
`src/main/java/practice/LitmusModel.java`:

- `load(t, variable)`: the value thread `t` reads now;
- `canPerformNext(t)`: whether thread `t`'s next operation may run now;
- `drainOne(t)`: a copy in which one of `t`'s buffered writes has reached memory.

When you're done, the checker allows both of the server's outcomes above and no outcome the
server could not produce. About ten lines in total.
