The scheduler keeps the tasks due on each tick in `DueTasks`, a small array-backed list. Its
plugins walk the list, and some of them change it while they walk: they drop a task they have
handled, or add a follow-up. Last week a plugin dropped a task mid-walk, and the walk silently
skipped the task after it. The team wants a walk that might skip or repeat a task to throw
instead, and a plugin that changes the list through its own walk to keep going without either.

`add`, `removeAt`, `set` and the counter `modCount` are written. Finish the two `TODO` methods of
the nested `Walk` class in `src/main/java/practice/DueTasks.java`:

- `next()`: the next task id, or `ConcurrentModificationException` when the list changed behind
  the walk's back;
- `remove()`: removes the id `next()` just returned, and leaves the walk able to carry on.

About ten lines in total.
