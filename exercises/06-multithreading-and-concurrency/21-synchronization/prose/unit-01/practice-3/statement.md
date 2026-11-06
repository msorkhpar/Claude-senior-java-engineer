A warehouse service moves stock between storage bins: pickers move units to the packing bins all
day, and a rebalancing job moves them back at night. Auditors count pairs of bins while both run.
Each bin's quantity is guarded by the bin's own monitor, and an operation on two bins holds both,
so that nobody ever sees units that are in neither bin. Some nights the rebalancing job stops
making progress, and the morning's thread dump shows threads that each hold one bin.

Finish `move(...)` and `count(...)` in `src/main/java/practice/Warehouse.java`. Both are written
except for the call that holds the two bins: `holdingBoth(first, second, action)` takes `first`'s
monitor, then `second`'s, and runs `action`. Moves in any direction and counts of any pair must be
able to run at the same time without ever hanging. About four lines in total.

The tests line two operations up: once one holds its first bin, it lets the other one catch up
before taking the second.
