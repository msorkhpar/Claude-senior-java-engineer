On `int`s, `&`, `|` and `^` work bit by bit, and `~` flips every bit. A set of
yes/no permissions fits in one `int`, one bit each: `READ` is `1`, `WRITE` is `2`
and `EXECUTE` is `4`, so `READ | WRITE` is `3`.

Write the four methods of `Permissions`. A `flag` may combine several bits.

1. `grant(mask, flag)` sets every bit of `flag`.
2. `revoke(mask, flag)` clears every bit of `flag`, and leaves a bit that was
   already clear clear.
3. `toggle(mask, flag)` flips every bit of `flag`.
4. `has(mask, flag)` returns whether **every** bit of `flag` is set in `mask`.

| call | answer |
|---|---|
| `grant(READ, WRITE)` | `3` |
| `revoke(3, WRITE)` | `1` |
| `revoke(1, WRITE)` | `1` |
| `toggle(1, WRITE)` | `3` |
| `has(READ, READ \| WRITE)` | `false` |
