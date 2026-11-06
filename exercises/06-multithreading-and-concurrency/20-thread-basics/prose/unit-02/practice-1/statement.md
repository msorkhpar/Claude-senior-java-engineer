The price-comparison page asks three to five parts suppliers for a quote and shows them side by
side. Each supplier's API takes between half a second and two seconds to answer. Two complaints
came in: the page takes as long as all the suppliers added together, and now and then it shows a
supplier with no price at all, although the supplier's logs show it answered.

Finish `askAll(...)` in `src/main/java/practice/QuoteRound.java`. `askerFor(...)` already builds
the thread that asks one supplier and records its price.

When you're done, a round takes about as long as its slowest supplier, and the page never renders
before every price is recorded. About six lines.

The tests use suppliers that answer on cue, so they can hold one back and check what `askAll`
does in the meantime.
