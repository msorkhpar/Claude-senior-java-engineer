When reads vastly outnumber writes, the page's `RWLockCache` guards a plain
`HashMap` with a `ReentrantReadWriteLock`: any number of readers hold the read
lock **together**, and a writer takes the write lock **alone**. (The page also
notes that the default, non-fair lock can starve writers under heavy reads;
`new ReentrantReadWriteLock(true)` serves them in order.)

Write `PriceCache`, a map from product to price:

- `put(product, price)` stores a price under the **write** lock.
- `get(product)` returns the price or `null`, under the **read** lock.
- `view(product, reader)` calls `reader.apply(price)` **while holding the
  read lock** and returns its result (the price may be `null`).
- `snapshot()` returns a copy of every price, taken under the read lock.

| calls | result |
|---|---|
| `put("tea", 3)`, `get("tea")` | `3` |
| `view("tea", p -> p * 2)` | `6` |
| `get("coffee")` | `null` |
| a reader is inside `view`; another thread calls `get` | `get` returns at once |
| a reader is inside `view`; another thread calls `put` | `put` waits for the reader |
