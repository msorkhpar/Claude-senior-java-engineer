A warehouse has a loading dock with a few bays. Trucks that arrive when every bay is taken wait
for one to free up, and at night the yard manager's app waits for the dock to empty before it
locks the gate. The dock's software has had three bad weeks: once the yard counted four trucks
in three bays, a driver sat in front of a free bay for an hour, and the gate was locked with a
truck still inside.

`LoadingDock` already keeps the count of occupied bays under its lock and has two conditions,
`bayFree` and `dockEmpty`. Write its three small helpers in
`src/main/java/practice/LoadingDock.java`; each is called with the lock held:

- `waitForBay()`: returns once the arriving truck can take a bay;
- `announceLeave()`: runs after a truck has left;
- `waitForEmpty()`: returns once no truck is at the dock.

A truck never takes a bay that isn't free, a waiting truck gets in as soon as a bay frees, and
the night close goes ahead only once the dock is empty, and then without delay. About four lines
in total.

The tests run waiting trucks and the night close on threads of their own. To check that your
waits hold up, they also take the dock's lock themselves: once to let a newcomer drive into a bay
right after it was freed, and once to wake the night close while a truck is still in.
