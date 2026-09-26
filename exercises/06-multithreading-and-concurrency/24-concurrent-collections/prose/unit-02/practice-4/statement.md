The page's best practice 2: use `ConcurrentHashMap.computeIfAbsent()` for lazy
initialization, because it is **atomic** and avoids creating the value twice.
The racy alternative looks the key up, and when it is missing creates a value
and puts it: two threads can both find the key missing, and the second `put`
throws the first thread's value away.

Write the class `Groups`, built on the `ConcurrentHashMap<String, List<String>>`
its constructor receives. Many threads call it at the same time. (Each
group's list also takes adds from many threads, so pick a thread-safe list; the
tests check the map step, which is where the page's pitfall lies.)

- `join(String group, String member)` adds `member` to `group`'s list, creating
  the group on its first join.
- `members(String group)` returns the group's members in the order they
  joined, as a list that later joins do not change; an unknown group gives an
  empty list.

| calls | answer |
|---|---|
| `join("chess", "ann")`, `join("chess", "bob")`, `join("go", "cy")`, `members("chess")` | `["ann", "bob"]` |
| `members("poker")` | `[]` |
| `m = members("chess")`, then `join("chess", "dee")` | `m` is still `["ann", "bob"]` |
| two threads `join("new", "ann")` and `join("new", "bob")` at once, then `members("new")` | both names |
