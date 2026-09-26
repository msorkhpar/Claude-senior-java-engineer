`length()` counts UTF-16 `char` units, not the characters a reader sees. A character outside
the Basic Multilingual Plane, such as the emoji `"\uD83D\uDE00"`, takes two units (a
surrogate pair): its `length()` is `2`, while `codePointCount(0, 2)` is `1`.

Write two methods in `CodePoints`:

1. `characters(String s)` returns how many characters `s` holds, an emoji counting once:
   `characters("abc")` is `3`, and `characters("a\uD83D\uDE00")` is `2`.
2. `reverse(String s)` returns the characters in reverse order, keeping each emoji whole:
   `reverse("abc")` is `"cba"`, and `reverse("a\uD83D\uDE00")` is `"\uD83D\uDE00a"`.
