A freight marketplace shows shippers a board of quotes from every carrier it works with. The
product team promised the board within two seconds, whatever the carriers do: a carrier that has
not answered by then is shown as too late, and one whose API fails is shown without a quote. The
first version waited for every carrier, so one carrier with a stuck API kept the board from ever
appearing.

Write the `TODO` in `src/main/java/practice/QuoteBoard.java`:

- `collect(...)`: ask every carrier on the pool and build the board's lines.

When you're done, the board comes by its deadline with one line per carrier, in the carriers'
order, and a carrier that missed the deadline is shown as `too late` and stops using a pool thread.
About twelve lines in total.

The tests ask carriers on a real `ExecutorService`; some of them never answer until the test
ends.
