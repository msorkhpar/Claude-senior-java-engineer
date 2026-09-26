The page says the compiler, the JIT and the CPU may reorder a thread's operations, as long
as the reordering does not change what the thread itself observes and does not break a
happens-before edge. It spells out what that means:

- **Within-thread semantics**: two accesses to the same variable, where at least one
  writes it, keep their order. Accesses to different variables, or two reads, may swap.
- **A volatile write**: no read or write that comes *before* it may move *after* it. A
  plain access after it may move before it.
- **A volatile read**: no read or write that comes *after* it may move *before* it. A plain
  access before it may move after it.
- **Two volatile accesses** never swap.
- **synchronized**: operations inside the block cannot move out of it, but operations
  outside may move in ("roach motel" ordering). A plain access right after `LOCK` is
  inside; one right before `LOCK` is outside, and so on for `UNLOCK`.

Write `Reordering.canSwap(Op first, Op second)`. `first` and `second` are adjacent
operations of one thread, in program order. Return whether they may be executed as
`second` then `first`. An `Op` is a `Kind` and a target: a variable for reads and writes
(plain or volatile), a monitor for `LOCK` and `UNLOCK`. Two monitor operations never swap,
and a volatile access never swaps with a monitor operation.

| first, second | answer |
|---|---|
| `WRITE a`, `WRITE b` | `true` (the page's `ReorderingDemo`) |
| `WRITE result`, `VOLATILE_WRITE ready` | `false` |
| `VOLATILE_READ ready`, `READ result` | `false` |
| `WRITE x`, `LOCK m` | `true` |
| `LOCK m`, `WRITE x` | `false` |
