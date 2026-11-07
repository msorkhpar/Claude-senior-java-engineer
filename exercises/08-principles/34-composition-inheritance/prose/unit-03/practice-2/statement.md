The shop's stock service wants a dashboard: how many stock calls succeed and how many fail. The
warehouse store `MapStock` is one implementation of `Stock`; the checkout uses another, and the
tests use fakes. `MeteredStock` wraps any of them and is itself a `Stock`, so the code that calls
the stock doesn't change. The dashboard reads `succeeded()` and `failed()` while checkout threads
are calling.

The counters are in place. Finish `MeteredStock.level` and `MeteredStock.set` in `src/main/java/practice/Stocks.java`.

When you're done, a call through `MeteredStock` behaves exactly as a call to the stock it wraps:
same answer, same exception. Every call is counted once, as a success if it returned and as a
failure if it threw. Callers already catch `IllegalArgumentException` for a negative count, and
some catch the exceptions their own store throws. About ten lines in total.
