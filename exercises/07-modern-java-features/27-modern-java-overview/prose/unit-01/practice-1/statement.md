The shop's `PriceBook` keeps prices in cents by SKU, and Finance audits it: every change is
written to a trail as `sku: old -> new`. The class was written years ago and has always passed its
audit. This quarter the auditors found price changes that are not in the trail. All of them come
from two newer jobs: the daily adjustment job, which calls `merge(sku, delta, Long::sum)`, and the
yearly increase, which calls `replaceAll(...)`.

Finish the two `TODO` methods in `src/main/java/practice/PriceBook.java` so that every price
change, whichever method makes it, reaches the trail. Callers of `merge` must still get exactly
the behaviour `Map.merge` documents, including when the function returns `null`. About eight
lines in total.
