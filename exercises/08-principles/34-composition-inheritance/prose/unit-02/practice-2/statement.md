A hierarchy such as `BaseTask -> LoggingTask -> SynchronizedLoggingTask` needs a new class
for every combination, and each level may add its own synchronization. The page's
alternative is **decorators**: every concern implements the same `TaskRunner` interface,
holds the runner it wraps, and adds one thing.

```java
TaskRunner runner = new SynchronizedTaskRunner(
    new LoggingTaskRunner(
        new UpperCaseRunner()
    )
);
```

`UpperCaseRunner` is given. Write the two decorators and the factory in `TaskRunners`:

- `LoggingTaskRunner(delegate, log)` adds `"START: " + input` to `log`, runs the delegate, then
  adds `"SUCCESS: " + result` and returns the result. If the delegate throws, it adds
  `"ERROR: " + message` and **rethrows that same exception**: logging must not swallow a
  failure;
- `SynchronizedTaskRunner(delegate)` holds a lock around the delegate call, so **one call at a
  time** runs inside the runner it wraps; a second caller waits until the first returns;
- `compose(base, log, logging, threadSafe)` applies exactly the decorators asked for: a
  `LoggingTaskRunner` alone, a `SynchronizedTaskRunner` alone (which writes nothing to
  `log`), both, or `base` itself when neither is asked for. The page warns that **decorator order matters**:
  `compose` puts the lock outermost, so logging happens inside the lock and the log lists
  each call's lines in the order the calls ran. `compose(new UpperCaseRunner(), log, true, true)`
  turns `"hello"` into `"HELLO"` and logs `START: hello`, `SUCCESS: HELLO`.
