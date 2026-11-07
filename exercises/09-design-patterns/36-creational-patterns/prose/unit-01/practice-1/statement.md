The product is sold for 25 concurrent seats, and `SeatLicense` counts them: one instance per
process, reached through `getInstance()`. A seat audit found customers using more seats than they
paid for, and two `SeatLicense` objects in one process on two occasions. The first appeared at
start-up, when the plugin framework instantiated every class of the package through its private
constructor before anything had called `getInstance()`. The second appeared after the session
store wrote a user's session out and read it back.

Finish the two `TODO`s in `src/main/java/practice/SeatLicense.java`:

- `checkConstruction()`, which the constructor runs every time, whoever calls it;
- `readResolve()`, for a license that is read back from a session.

When you're done, neither the framework nor the session store can produce a second license, and a
session read back counts against the live license, with the seats that are claimed now. About four lines in total.

The tests load the class afresh for each test, so each one starts in a process where nothing has
used the license yet.
