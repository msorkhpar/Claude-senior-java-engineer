A retail chain issues invoice numbers from one sequence for the whole company, and every branch's
tills call `issue(...)` on their own branch's `Branch` object. The finance team has two
complaints. The audit found pairs of invoices with the same number, issued seconds apart by
different branches, and journal lines with their numbers out of order. And on the morning a new
branch opened, every till in the chain froze for as long as the new branch took to load its
settings from head office.

Write `issue(...)` in `src/main/java/practice/Branch.java`. Each call takes the next number of the
company's sequence, writes it to the journal with the branch's name and the customer, and returns
it. With any number of tills in any number of branches calling it at once, no number is issued
twice, the journal gets its lines one at a time and in number order, and no till waits for a
branch that is being opened. About five lines in total, and you may add a field.

The tests use journals and settings loaders that hold a call open, so they can see who waits for
whom.
