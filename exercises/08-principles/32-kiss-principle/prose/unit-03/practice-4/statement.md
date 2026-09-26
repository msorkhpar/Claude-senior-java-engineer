The page prefers a `Semaphore` for resource limiting: it is simpler than custom counting logic
with locks and conditions, and it is safe for many threads. Build the pool on one, as the
page advises; the tests check the pool's behaviour below, not the class behind it. It also warns about one edge case: a `Semaphore` accepts more
releases than acquires, and each extra release quietly adds a permit, so the API should
guard against it.

Write `ResourcePool`:

- `ResourcePool(int capacity)` creates a pool of `capacity` resources; a capacity of `0` or
  less throws `IllegalArgumentException`;
- `tryAcquire()` takes a free resource and returns `true`, or returns `false` **when every
  resource is taken**. `Semaphore.tryAcquire()` with no timeout answers without waiting,
  which is what the page's manager does (a caller whose interrupt flag is set still gets a free
  resource);
- `release()` gives one back. **A release with no matching acquire**, when every resource is
  already free, throws `IllegalStateException` and leaves the pool as it was;
- `available()` is the number of free resources.

Example: a pool of 2 grants two `tryAcquire()` calls and refuses the third; after one
`release()`, `tryAcquire()` succeeds again.
