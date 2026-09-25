`i++` and `++i` both add one to `i`. They differ in the **value of the
expression**: `i++` evaluates to the value *before* the increment, and `++i` to
the value *after* it.

A `TicketDispenser` hands out numbered tickets, starting at `1`. Write its three
methods, each a single `return` statement:

1. `take()` hands out the next number and moves on: the first call returns `1`,
   the second `2`.
2. `skip()` throws the next ticket away and returns the number `take()` will
   hand out now: on a new dispenser, `skip()` returns `2`, and `take()` then returns `2`.
3. `peek()` returns the number `take()` would hand out, and changes nothing.
