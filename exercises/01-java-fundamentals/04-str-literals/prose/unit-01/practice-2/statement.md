The storefront keeps product descriptions in an in-memory cache with a budget of bytes for their
text, so it can't take more than its share of the heap. Its weigher counts each string's UTF-8
size. The caches of the Nordic and the Japanese shops hold fewer products than their budgets
allow, while the Dutch shop's, whose descriptions carry prices in euros, takes more than its share
of the heap.

Eviction already works. Finish the `TODO` in `src/main/java/practice/ProductCache.java`:

- `textBytes(s)`: how many bytes the characters of `s` take in the heap of a JVM with default
  settings, leaving out the object's and the array's headers.

About four lines.
