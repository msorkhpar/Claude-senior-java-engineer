`counter++` is not atomic: it is a read, an add and a write, and two threads can both
read the same old value, so one update is lost. The same is true of a count kept in a
map: `map.put(w, map.getOrDefault(w, 0) + 1)` is a read-modify-write even on a
`ConcurrentHashMap`, whose single calls are each thread-safe. The page's fix is to make
the whole compound step one atomic operation.

Write `WordCounter`. Its constructor takes the `ConcurrentHashMap<String, Integer>` to
keep counts in. `add(word)` adds one to the word's count, as **one atomic step** on
that map; `count(word)` returns the count, `0` for a word never added.

| calls | answer |
|---|---|
| `add("to")`, `add("be")`, `add("to")` | |
| `count("to")`, `count("be")`, `count("or")` | `2`, `1`, `0` |
| `add("word")` 300 times | `count("word")` is `300` |
| two threads call `add("x")` at the same moment | `count("x")` is `2` |

The tests hand you a map that can hold a read open, to make two adds meet in the
middle.
