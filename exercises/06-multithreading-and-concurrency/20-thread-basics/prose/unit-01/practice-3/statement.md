The billing service has a dozen thread pools, all created with `Executors`' default factory. Two
things went wrong last month. During an incident, on-call took a thread dump and found 37 threads
called `pool-<N>-thread-<M>`, and had to guess which pool belonged to which component. And after a
routine restart, the audit log was missing its last entries: the audit pool is created lazily, on
the first audit event, and that event is raised by the housekeeping thread, which is a daemon.

The team now creates every pool with a `ServiceThreads` factory, which knows the component's name
and whether the component is background work that may be dropped at exit (a CPU sampler) or
foreground work that must finish (audit, ledger). Finish the `TODO` in
`src/main/java/practice/ServiceThreads.java`:

- `newThread(...)`: the thread a pool asks for.

When you're done, a thread dump shows `audit-1`, `audit-2`, `ledger-1`, ... (each component
counting its own threads), and whether the JVM waits for a pool's threads at exit depends only on
what the component declared, whichever thread happened to create the pool. About four lines in
total.

The tests build real pools with the factory and also call it from a daemon thread.
