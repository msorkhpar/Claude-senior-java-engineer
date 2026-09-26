The page's best practice for a very long single-line string is the line
continuation escape: a `\` at the end of a text block line suppresses the line
break, so the source reads well and the value stays one line. Its example:

```java
String singleLine = """
        This is one \
        continuous \
        line.""";
// "This is one continuous line."
```

Write `LongLine.source(String sentence, int width)`. `sentence` is words
separated by single spaces (no quote, no backslash, no leading or trailing
space). Return the text block source for it:

- the opening `"""`, then `\n`;
- the sentence split into pieces, filled greedily: each piece takes as many
  whole words as fit in `width` characters, a word longer than `width` takes a
  piece of its own and is **never cut**;
- the space where the sentence breaks stays at the end of the piece before
  it, and **counts** towards its width (a piece of exactly `width` characters fits);
- each piece indented by eight spaces; every piece but the last ends with `\`
  and `\n`, the last ends with the closing `"""`.

| sentence | width | answer (`·` is a space, `⏎` is `\n`) |
|---|---|---|
| `This is one continuous line.` | `15` | `"""⏎········This·is·one·\⏎········continuous·\⏎········line."""` |
| `This is one continuous line.` | `12` | the same |
| `This is one continuous line.` | `11` | `"""⏎········This·is·\⏎········one·\⏎········continuous·\⏎········line."""` |
| `a supercalifragilistic word` | `8` | `"""⏎········a·\⏎········supercalifragilistic·\⏎········word"""` |
| `short` | `20` | `"""⏎········short"""` |
