Text is a stream source too: `String.chars()` gives an `IntStream` of the string's
`char` values, and `Pattern.splitAsStream(text)` streams the pieces between the
matches of a pattern.

Write two methods in `TextParts`:

1. `countVowels(String text)` returns how many characters of `text` are vowels
   (`a`, `e`, `i`, `o`, `u`, in either case, whatever the default locale); `chars()` streams the characters.
2. `parts(String text, String delimiter)` splits `text` at every occurrence of
   `delimiter` and returns the pieces trimmed, in order, leaving out empty ones. The
   delimiter is **plain text**, matched as a whole (it may be several characters
   long), not a regular expression.

| call | answer |
|---|---|
| `countVowels("hello world")` | `3` |
| `countVowels("")` | `0` |
| `parts("a, b ,c", ",")` | `["a", "b", "c"]` |
| `parts("1 - 2", "-")` | `["1", "2"]` |

`String.split` takes a regular expression, and some delimiters mean something there.
`Pattern.quote` turns text into a pattern that matches it literally.
