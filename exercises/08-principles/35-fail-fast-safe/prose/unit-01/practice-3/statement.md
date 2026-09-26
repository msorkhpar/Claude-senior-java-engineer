`Collections.synchronizedList` makes each call on the list synchronized, but its iterator is
still fail-fast: a traversal is many calls, and another thread can write between them. The page's
fix is to hold the wrapper's own lock for the whole traversal:

```java
synchronized (syncList) {
    Iterator<String> it = syncList.iterator();
    while (it.hasNext()) {
        if (it.next().equals("target")) {
            it.remove();
        }
    }
}
```

Write `removeIf(List<String> syncList, Predicate<String> doomed)` in `SyncPurge`. `syncList` was
made by `Collections.synchronizedList`, and other threads use it. Remove every element for which
`doomed` is true, in place.

- with `["a", "bb", "ccc"]` and `s -> s.length() > 1`, the list becomes `["a"]`;
- **the lock is held for the whole traversal**: every time `doomed` is tested, the current
  thread holds the lock of `syncList` itself, the wrapper, not some other object;
- do it the page's way, with one `synchronized (syncList)` block of your own around the whole
  job: every call your method makes on the list, from the first read to the last removal,
  runs inside that block. The wrapper's own per-call locking, `syncList.removeIf` included,
  does not count, and neither does a second pass or a rebuild after the block has ended.
