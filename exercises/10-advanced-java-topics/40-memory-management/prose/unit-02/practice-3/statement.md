The page's pitfall: `finalize()` is deprecated and may never run, so cleanup
belongs in `close()` (called by try-with-resources), with a
`java.lang.ref.Cleaner` as the safety net for a caller that forgets. The
`Cleaner.Cleanable` that `register` returns runs its action **at most once**,
whether `clean()` is called explicitly or the object becomes unreachable.

Write `NativeBuffer implements AutoCloseable`:

- `new NativeBuffer(cleaner, size, release)` allocates a `byte[size]` and
  registers `release` (a `Runnable` standing for freeing native memory) with
  the given `Cleaner`.
- `write(index, value)` stores a byte; `isOpen()`.
- `close()` runs the release through the `Cleanable`: **the release action runs
  at most once**, however often `close()` is called.
- **A closed buffer refuses to be used**: `write` throws `IllegalStateException`.

| calls | release runs | `isOpen()` |
|---|---|---|
| `try (var b = new NativeBuffer(...)) { b.write(0, 7); }` | 1 | `false` afterwards |
| `close()`, `close()` | 1 | `false` |
| `close()`, then `write(0, 1)` | 1 | `IllegalStateException` |
