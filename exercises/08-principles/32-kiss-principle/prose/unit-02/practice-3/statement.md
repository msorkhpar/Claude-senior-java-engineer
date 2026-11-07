The shop's checkout asks `PostcodeLookup` for the delivery region of every order. The table of
regions comes from the address service, which takes a few seconds to answer and now and then
times out, so the lookup fetches it on first use instead of at startup. Right after each deploy,
dozens of checkout threads ask at once.

Write `table()` in `src/main/java/practice/PostcodeLookup.java`, with any fields it needs. When
you're done, nothing is fetched until the first call, and the address service is asked once: every
caller, including one that arrives while the fetch is still running, gets that same table. If a
fetch fails, its caller gets the exception and the next call fetches again. About five lines in
total.

The tests hold the first fetch open while a second checkout thread calls `table()`.
