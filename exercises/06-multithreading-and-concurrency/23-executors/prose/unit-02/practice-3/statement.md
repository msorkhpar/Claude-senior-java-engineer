The on-call engineers open the operations dashboard first thing in an incident. It has a dozen
panels, and each panel runs its own query on a pool. The complaint: the dashboard stays blank for
forty seconds whenever the weekly-revenue panel's query is slow, even though the error rate and
the queue depth, the two panels anyone looks at during an incident, were ready almost at once.

The screen can show a panel from any thread. Write the `TODO` in
`src/main/java/practice/Dashboard.java`:

- `load(...)`: run the panels' queries and put each panel on the screen.

When you're done, each panel appears as soon as its own query has answered, a query that fails
shows its panel as `unavailable` without keeping the others off the screen, and `load` returns
once every panel is shown. About twelve lines in total.

The tests run the queries on a real `ExecutorService` and hold some of them with latches.
