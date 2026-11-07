The fulfilment service builds the morning pick lists with one task per warehouse, on a
`ForkJoinPool` that the service creates at start-up and passes in. The morning run is called from
a scheduler thread, not from the pool. With six warehouses it takes six times as long as one,
although the pool has room for all six. Since two warehouses started sharing a cross-dock (each
task waits until its partner's task has started), the run doesn't finish at all. And when one
warehouse's data is broken, the alert should name that warehouse's own exception type, not a
generic wrapper.

Finish the `TODO` in `src/main/java/practice/PickLists.java`: `buildAll(...)` builds every
warehouse's list and returns them in the order of the tasks.

When you're done, all the warehouse tasks run at the same time on the service's pool, partners
finish, and a warehouse task that throws makes `buildAll` throw that exception's type. About four
lines in total.
