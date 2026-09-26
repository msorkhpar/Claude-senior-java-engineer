Each `+=` on a String inside a loop builds a whole new String. A `StringBuilder` is appended
to in place, and turned into one String at the end. The page's own loop leaves a trailing
`", "` behind; this one must not.

Write `join(List<String> items)` in `Csv`. It returns the items separated by `", "`:

- `join(List.of("a", "b", "c"))` is `"a, b, c"`;
- one item has no separator: `join(List.of("solo"))` is `"solo"`;
- an empty list gives `""`.
