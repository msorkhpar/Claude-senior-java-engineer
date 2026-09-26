The page's HashMap finds a key's bucket in two steps. It first **spreads** the
hash code by folding its upper 16 bits into the lower ones,
`h ^ (h >>> 16)`, and then keeps the low bits with a mask,
`index = hash & (capacity - 1)`, which works because the capacity is a power
of two. When the table doubles, each key either keeps its index or moves up by
exactly the old capacity. HashMap also accepts a `null` key, which it stores at
index 0.

Write `Buckets.index(Object key, int capacity)`, the bucket HashMap uses for
`key` in a table of `capacity` buckets (a power of two).

| key | capacity | answer |
|---|---|---|
| `42` | `16` | `10` |
| `58` | `16` | `10` |
| `42` | `32` | `10` |
| `58` | `32` | `26` |
| `65536` (hash `0x10000`) | `16` | `1` |
| `null` | `16` | `0` |
