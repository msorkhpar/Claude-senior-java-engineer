The retry scheduler keeps failed jobs in a list that it shares with the dispatcher. Before every
dispatch round it should take out the jobs that have used up their attempts, so that the
dispatcher stops resending them. Since the last release, some payout jobs are resent long after
they gave up, and one round in a few hundred fails before sending anything.

Write `dropGivenUp(...)` in `src/main/java/practice/RetryQueue.java`. It takes every job for which
`gaveUp(job)` is true out of the list it is given, the same list the dispatcher reads, and returns
how many it took out. About three lines in total.

The tests call it with `ArrayList`s of jobs in various orders.
