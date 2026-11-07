The build farm hands out time-limited leases on shared machines: build agents, GPUs, a plotter.
A holder renews its lease before it runs out and releases it when done, and a sweeper thread
frees the leases nobody renewed and tells their holders. Two incidents last month: a training
job lost its GPU a few milliseconds after renewing it, and a CAD team that had released the
plotter got a "your lease expired" message for it anyway.

`LeaseTable` already acquires, renews and releases. Write `sweep(...)` in
`src/main/java/practice/LeaseTable.java`: free every lease that ran out at or before `now`, tell
its holder, and return the resources it freed.

When you're done, a lease renewed while the sweep runs stays with its holder, and only leases the
sweep itself freed are reported and returned. About six lines in total.

The tests force the interleaving: they make a holder renew or release its lease just before the
sweeper changes that lease in the table, the way it happens when the holder's thread gets there
first.
