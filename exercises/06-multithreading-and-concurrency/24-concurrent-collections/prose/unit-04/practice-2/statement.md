The page's HashMap resizing rules: the **threshold** is
`(int) (capacity * loadFactor)`, and the table **doubles** as soon as the
number of entries goes **above** the threshold. With the defaults (16, 0.75)
that is 7 resizes on the way to 1000 entries. The page's pitfall: asking for
`new HashMap<>(100)` gives capacity 128 and threshold 96, so the map still
resizes on the 97th put. To hold `n` entries with no resize, the page requests
`(int) (n / 0.75f) + 1`.

Write two methods of `Resizes`:

- `capacities(int capacity, float loadFactor, int entries)`: the table
  capacities a map whose table starts at `capacity` (a power of two) goes
  through while `entries` entries are put one by one, starting with
  `capacity`.
- `initialCapacityFor(int entries)`: the page's initial capacity for `entries`
  entries at the default load factor 0.75.

| call | answer |
|---|---|
| `capacities(16, 0.75f, 13)` | `[16, 32]` |
| `capacities(16, 0.75f, 1000)` | `[16, 32, 64, 128, 256, 512, 1024, 2048]` |
| `capacities(128, 0.75f, 97)` | `[128, 256]` |
| `capacities(16, 0.75f, 12)` | `[16]` |
| `initialCapacityFor(100)` | `134` |
