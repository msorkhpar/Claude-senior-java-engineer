The warehouse service exports stock levels as CSV every night, and a test compares the export
with an expected file kept as a text block. The test passes on the Linux build agents and fails on
the Windows one, with the message `line 1 differs` although the two texts look the same. A
colleague's quick fix made the check ignore all white space, and a week later a row with a
trailing space, which the partner's strict CSV parser rejects, went through unnoticed.

The partner's parser accepts lines ending with `\n` or `\r\n`, and a last row with or without a
line break after it. Every other character counts.

Finish the two `TODO`s in `src/main/java/practice/ExportCheck.java`:

- `linesOf(text)`: the lines of a text;
- `firstDifference(expected, actual)`: the number of the first line that differs, counting from 1,
  or 0 when the texts match. A line that one text has and the other doesn't is a difference.

About seven lines in total.
