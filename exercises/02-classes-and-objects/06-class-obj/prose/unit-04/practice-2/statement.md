The price cache is a `TreeMap` from SKU to price in cents, and the cache dashboard plots how many
prices callers write. Other code uses the cache as a `NavigableMap`, so it has to stay a
`TreeMap`. Prices arrive one at a time through `put`, and in batches through `putAll`: the
nightly reload copies yesterday's sorted snapshot into a fresh cache, and the pricing service
sends whatever map it has built.

Finish the two `TODO`s in `src/main/java/practice/CountingStore.java`, `put` and `putAll`, so that
the cache stores prices exactly as a `TreeMap` does and `writes()` counts one write per entry
written, overwrites included, for any batch, sorted or not, into an empty cache or a full one.
About six lines in total.
