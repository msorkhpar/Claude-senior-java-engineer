The help desk's old ticket table has a `status` column (`OPEN`, `ASSIGNED`, `RESOLVED`) and two
columns that may be null: `agent` and `resolution`. Years of manual fixes left rows that contradict
their status, and the dashboard shows tickets "resolved by null". The new code keeps each ticket as
one of three records, `Open`, `Assigned` and `Resolved`, each holding the data of its state.

Finish `src/main/java/practice/Tickets.java`: the two constructors marked `TODO` and `fromRow`.
When you're done, no code path, the loader or any other, can produce an `Assigned` without an
agent or a `Resolved` without an agent and a resolution (a blank string counts as missing). A row
that doesn't fit its status is refused with an `IllegalArgumentException` that lets the data team
find the row; nothing in it is silently dropped. `requireText` and `requireEmpty` are there to use.
About twenty lines in total.

The tests use their own ticket ids, agents and rows.
