Reading input until it runs out is the textbook `while` loop:

```java
String line;
while ((line = reader.readLine()) != null) {
    // process the line
}
```

`readLine()` returns `null` at the end of the input, and the input may hold no line at all.

Write `count(BufferedReader reader)` in `Words`. It returns how many words the whole input
holds, where words are separated by any run of spaces or tabs:

- `"one two\nthree"` holds `3` words;
- a blank line holds none: `"a\n\n   \nb"` holds `2`;
- extra spaces separate nothing more: `"  a   b  "` holds `2`;
- an empty input holds `0`.
