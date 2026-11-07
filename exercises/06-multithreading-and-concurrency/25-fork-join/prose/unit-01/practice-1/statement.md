The card team scores every payment of the day for fraud risk and adds up the points. Scoring one
amount is pure arithmetic, but a busy day has forty million of them, and on one thread the nightly
job no longer finishes before the morning settlement run. The job has been given a
`ForkJoinPool`, and `RiskPoints` already wraps the day in one task and hands it to that pool.

Finish `compute()` of the `Total` task in `src/main/java/practice/RiskPoints.java`. When you're
done:

- the total is right for any day, including an empty one;
- the pool's workers score the day together, rather than one after another;
- the operations dashboard, which shows `tasksCreated()`, sees a few tasks per `threshold`
  amounts, not one per payment. A range of exactly `threshold` amounts is one task.

About ten lines in total. The tests run `RiskPoints` on real pools of two and three workers.
