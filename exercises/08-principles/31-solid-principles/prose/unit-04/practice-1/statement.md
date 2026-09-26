The page's fat interface forces every store to offer every operation, so a read-only
store ends up with write methods that can only throw:

```java
class ReadOnlyStore implements DataStore {
    public void write(String k, String v) {
        throw new UnsupportedOperationException(); // ISP violation signal!
    }
}
```

**Clients should not be forced to depend on methods they do not use.** In `Stores`, the
fine-grained interfaces are given: `Readable` (`read`, `exists`), `Writable` (`write`,
`delete`) and `Listable` (`listKeys`, `size`, `isEmpty`). Write the two stores:

- `ReadWriteStore` implements all three, on a `ConcurrentHashMap`;
- `ReadOnlyStore(Map)` implements **only the roles it can honour**, `Readable` and
  `Listable`, so it has no write method at all. The starter's `ReadOnlyStore` still
  implements `Writable` and refuses its methods: take that role away.

`read` of a missing key is `null`, and `listKeys()` lists the keys in sorted order in both
stores, whatever order the underlying map keeps them in. As the page's edge cases say, a
read-only store wraps **its own immutable copy** of the map it was given, whatever kind of
map that is, so later changes to that map do not reach it (the tests see that; whether your copy is
itself immutable, rather than a private map nobody writes, they cannot see), and a `null` map gives an empty store
whose `listKeys()` is an empty list, not `null`.
