An **unbound** reference takes its receiver from the interface's first argument. That
is why `String::compareTo`, an instance method with one parameter, is a
`Comparator<String>`: `compare(a, b)` runs `a.compareTo(b)`. In the same way
`List::isEmpty` is a `Predicate<List<String>>`.

Write four methods in `WordOrder`, each returning a new list:

1. `natural(List<String> words)` sorts with `String::compareTo`.
2. `ignoringCase(List<String> words)` sorts with `String::compareToIgnoreCase`.
3. `shortestFirst(List<String> words)` sorts by length, and words of equal length
   alphabetically. `Comparator.comparing` and `thenComparing` take method references.
4. `nonEmpty(List<List<String>> lists)` drops the empty lists, keeping the order.

| call | result |
|---|---|
| `natural(["banana", "apple", "cherry"])` | `["apple", "banana", "cherry"]` |
| `ignoringCase(["Banana", "apple", "CHERRY"])` | `["apple", "Banana", "CHERRY"]` |
| `shortestFirst(["bb", "ab", "c"])` | `["c", "ab", "bb"]` |
| `nonEmpty([["a"], [], ["b"]])` | `[["a"], ["b"]]` |

Watch the words that differ only in case, and the words that tie on length.
