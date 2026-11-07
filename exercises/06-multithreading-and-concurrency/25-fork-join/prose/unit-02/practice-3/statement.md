The video service renders previews with a library that has a floating licence of a few seats.
Every render task takes a seat before it calls the library, and the task that finishes a render
gives the seat back. Both kinds of task run on the same render `ForkJoinPool`. On a quiet day it
works. On a busy day the pool freezes: every seat is taken, more renders are waiting for one, and
the finishing tasks that would give seats back are queued behind them, never started.

`LicenceSeats` keeps the seats in a `Semaphore`. Finish the `TODO` in
`src/main/java/practice/LicenceSeats.java`: `take()` takes one seat, waiting if none is free.

When you're done, a render task waiting for a seat no longer stops the rest of the pool's work,
and a task that takes a seat really holds one, so the count of free seats stays right. About
twelve lines in total.
