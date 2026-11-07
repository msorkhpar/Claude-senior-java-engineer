Pickers move stock between the bins of a warehouse all day, and every hour a job compares the
warehouse's total with the books. Each bin is thread-safe on its own. The books are right, yet the
hourly count sometimes reports a few units missing, and sometimes a few too many, always while
stock is being moved.

Write `move(...)` and `total()` in `src/main/java/practice/Warehouse.java`, so that a move either
happens whole or not at all, and a total taken at any moment, while moves run, equals the stock
in the warehouse. About eight lines in total.

The tests use bins of their own, which can stop a call partway through, and run a move and a
total against each other at that point.
