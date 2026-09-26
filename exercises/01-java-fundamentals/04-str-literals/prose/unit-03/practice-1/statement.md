`charAt` and `substring` throw `StringIndexOutOfBoundsException` for an index past the end,
so check the length first. And a check such as "does it start with `abc`?" needs no
substring at all: `s.substring(0, 3).equals("abc")` throws on a shorter `s`, while
`s.startsWith("abc")` just answers `false`.

Write two methods in `Chars`:

1. `lastChar(String s)` returns the last character: `lastChar("Hello")` is `'o'`. An empty
   String has none: throw `IllegalArgumentException`.
2. `hasPrefix(String s, String prefix)` says whether `s` starts with `prefix`:
   `hasPrefix("abcdef", "abc")` is `true`, `hasPrefix("abxdef", "abc")` is `false`, and
   `hasPrefix("ab", "abc")` is `false`.
