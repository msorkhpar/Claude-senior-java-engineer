The page's first best practice uses `\s` for alignment:

```java
String table = """
        Name  \s
        Alice \s
        Bob   \s""";
```

Trailing spaces in a text block are stripped, so a table whose cells are padded
to equal width loses the padding at the end of each line, unless the line ends
with `\s`: the spaces before it are then no longer trailing.

Write `AlignedTable.lines(List<List<String>> rows)`. Every row has the same
number of cells. Return the content lines you would write inside the text block
(no delimiters, no indentation), joined by `\n`:

- each cell is padded with spaces on the right to its column's width, the
  length of the widest cell of that column in any row;
- cells are separated by one space;
- if a line then ends with a space, that last space is written `\s`.

| rows | answer (`·` is a space, `⏎` is `\n`) |
|---|---|
| `[Name, Age]`, `[Bob, 7]`, `[Ann, 30]` | `Name·Age⏎Bob··7·\s⏎Ann··30\s` |
| `[k, v]`, `[key, value]` | `k···v···\s⏎key·value` |
| `[Name]`, `[Alice]`, `[Bob]` | `Name\s⏎Alice⏎Bob·\s` |
| `[a, ]`, `[bb, cc]` | `a···\s⏎bb·cc` |
