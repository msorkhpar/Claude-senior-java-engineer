# 7.4.1. Limitations of Traditional String Literals

At 02:00 the nightly cleanup job logged out every user of the shop. It was meant to delete only
expired sessions, and its `DELETE` had been copied from the DBA's script, comment included, one
`"..." +` per line. Nothing failed: the statement ran and reported a row count.

A traditional string literal holds one line of text, and quotes, backslashes and line breaks
inside it are written as escapes. Multi-line SQL, JSON and HTML pay for that, in four ways, one
per heading below.

## Every Quote and Backslash Is Written Twice, and a Wrong Backslash Can Still Compile

Inside a literal, `"` is written `\"` and `\` is written `\\`. JSON that itself contains an
escaped quote gets both layers:

```java
String json = "{\"sku\": \"A-17\", \"note\": \"say \\\"hi\\\"\"}";
System.out.println(json);
```

```text
{"sku": "A-17", "note": "say \"hi\""}
```

That is hard to review, but a dropped `\"` usually fails to compile. A dropped backslash fails only
when the letter after it is not a known escape:

```text
bad/Paths.java:2: error: illegal escape character
    String users = "C:\Users\deploy";
                       ^
bad/Paths.java:2: error: illegal escape character
    String users = "C:\Users\deploy";
                             ^
bad/Paths.java:3: error: illegal escape character
    String digits = "\d+";
                      ^
```

Pause before the next run: a Windows path and a regex for the whole word `cat`, each written with
single backslashes. Do they compile?

```java
String path = "C:\temp\new-orders";
System.out.printf("path: length %d, char 2 is %d, char 6 is %d%n",
        path.length(), (int) path.charAt(2), (int) path.charAt(6));

String text = "the cat sat on a catalog";
long escaped = Pattern.compile("\\bcat\\b").matcher(text).results().count();
long single = Pattern.compile("\bcat\b").matcher(text).results().count();
System.out.printf("written \"\\\\bcat\\\\b\": %d match%n", escaped);
System.out.printf("written \"\\bcat\\b\":   %d matches, first char %d%n", single, (int) "\bcat\b".charAt(0));
```

```text
path: length 16, char 2 is 9, char 6 is 10
written "\\bcat\\b": 1 match
written "\bcat\b":   0 matches, first char 8
```

Both compile. `\t` and `\n` are valid escapes, so the path holds a tab (9) and a newline (10), and
any code that opens it looks for a folder that doesn't exist. `\b` is a valid escape too: backspace, U+0008.
The regex engine never sees a word boundary, so the pattern matches nothing, and an alert built on
it never fires.

`javac` processes the escapes first, and only what is left reaches the regex engine, the JSON
parser or the file system. Each backslash the inner
language needs is `\\` in the source, and a single one is either an error or a different
character, depending on the letter after it. The Reference lists the letters that compile.

<!-- practice 1 -->

## A Value Split Across Lines Has a Seam at Every `+`, and a Missing `\n` or Space There Changes It

A literal can't contain a line break (`javac` says `unclosed string literal`), so a statement
copied from a script becomes one literal per line, joined with `+`. These two came from the
cleanup job and from a report:

```java
String cleanup =
        "DELETE FROM sessions " +
        "-- keep sessions that are still valid " +
        "WHERE expires_at < TIMESTAMP '2026-11-01 00:00:00'";
String report =
        "SELECT id, total" +
        "FROM orders WHERE status = 'OPEN'";
```

Printed, then run against an in-memory H2 database holding three sessions, two of them expired:

```text
DELETE FROM sessions -- keep sessions that are still valid WHERE expires_at < TIMESTAMP '2026-11-01 00:00:00'
SELECT id, totalFROM orders WHERE status = 'OPEN'
cleanup deleted 3 of 3 sessions, 2 had expired
report: Column "ID" not found; SQL statement:
SELECT id, totalFROM orders WHERE status = 'OPEN' [42122-224]
```

The value holds exactly what is between the quotes. The line breaks you see in the editor are
outside them, so they are not in the string. In the script, the comment ended at the end of its
line; in the string there is no end of line, and `--` comments out the `WHERE`. A `DELETE` without
a condition is valid SQL, so the database deleted every row and reported success. The second seam
lost a space, and fails only when it runs, with an error that names a column.

The `+` itself costs nothing at run time: both expressions are constant, and `javac` folds each
into one string (`javap` shows a single `ldc` per statement; unit 1.4.2). The cost is that every
seam needs its `\n` or its space by hand, and a reviewer reading the Java sees the layout of the
source, not of the value. If you build multi-line text from literals, end every piece with `\n`, so
comments stay terminated and the value prints the way the source looks.

## `\n` Is Always a Line Feed; `%n`, `println` and `lineSeparator()` Follow the Platform

Which newline goes at the end of each piece? The same program, run as it is and with
`line.separator` set to `\r\n`, the value Windows uses (`hex` prints the UTF-8 bytes; `buf` holds
what a `PrintStream`'s `println("ok")` wrote):

```java
System.out.println("\"ok\\n\"                   " + hex("ok\n"));
System.out.println("String.format(\"ok%n\")    " + hex(String.format("ok%n")));
System.out.println("\"ok\" + lineSeparator()   " + hex("ok" + System.lineSeparator()));
System.out.println("println(\"ok\")            " + hex(buf.toString(StandardCharsets.UTF_8)));

String expected = "shipped: 3\n";
String actual = String.format("shipped: %d%n", 3);
System.out.println("expected.equals(actual): " + expected.equals(actual));
```

```text
$ java LineEnds
"ok\n"                   6f 6b 0a
String.format("ok%n")    6f 6b 0a
"ok" + lineSeparator()   6f 6b 0a
println("ok")            6f 6b 0a
expected.equals(actual): true
$ java -Dline.separator=$'\r\n' LineEnds
"ok\n"                   6f 6b 0a
String.format("ok%n")    6f 6b 0d 0a
"ok" + lineSeparator()   6f 6b 0d 0a
println("ok")            6f 6b 0d 0a
expected.equals(actual): false
```

`\n` is decided by `javac`: it is U+000A in the class file, whatever machine runs it. `%n`,
`println` and `System.lineSeparator()` use the `line.separator` property as the JVM started with
it, which the javadoc gives as `"\n"` on Unix and `"\r\n"` on Windows.

A test that compares a report built with `%n` against an expected `"...\n"` passes on the Linux CI and fails on a colleague's
Windows laptop. In the other direction, a format that requires CR LF, such as HTTP/1.1 headers,
SMTP or CSV per RFC 4180, gets bare LF from `%n` on a Linux server. Choose by where the text goes:
a format with fixed line endings gets the escape it requires (`\r\n` or `\n`), and only text for
the local console or a local file gets `%n`.

<!-- practice 2 -->

## A Text Block Removes the Escaped Quotes and the Seams, Not the Backslashes or the Values

A text block (Java 15, unit 7.1.3) is one literal between `"""` delimiters that may span lines.
Here is the JSON, once concatenated and twice as a block, one of them with a placeholder in the
style of other languages:

```java
String sku = "A-17";

String concatenated = "{\n" +
        "  \"sku\": \"" + sku + "\",\n" +
        "  \"qty\": 2\n" +
        "}\n";
String block = """
        {
          "sku": "${sku}",
          "qty": 2
        }
        """;
String filled = """
        {
          "sku": "%s",
          "qty": 2
        }
        """.formatted(sku);

System.out.print(block);
System.out.println("block.equals(concatenated):  " + block.equals(concatenated));
System.out.println("filled.equals(concatenated): " + filled.equals(concatenated));
```

```text
{
  "sku": "${sku}",
  "qty": 2
}
block.equals(concatenated):  false
filled.equals(concatenated): true
```

The quotes need no escape and the line breaks are real ones, so there are no seams to get wrong:
the cleanup `DELETE` written as a block, one source line per SQL line, deleted 2 of the 3
sessions. `filled` is an ordinary `String`, equal to the concatenated one.

A text block has no interpolation: `${sku}` is six characters of text, and values
still go in through `formatted`, `+`, or, for SQL, bind parameters (unit 7.4.5). String templates
(JEPs 430 and 459) were previews in Java 21 and 22 and were then withdrawn: `javac` 21 rejects
`STR."..."` without `--enable-preview`, and `javac` 25 rejects its `\{` as an illegal escape. And a
block still processes escapes, so the regex problem of the first section is unchanged:

```text
bad/Regex.java:3: error: illegal escape character
            \d+""";
             ^
```

A block's line breaks are `\n` on every platform, so the previous section still applies when one is
compared with output written with `%n`. How the delimiters and the trailing newline work is unit
7.4.2, indentation is 7.4.3, and the block-only escapes are 7.4.4.

<!-- practice 3 -->

## Summary

- **Every quote and backslash is written twice.** Unknown escapes fail to compile, but `\t`, `\n`,
  `\b`, `\r`, `\f`, `\s` and octal ones such as `\1` compile and silently change a path or a regex.
- **Text split across lines has a seam at every `+`.** A missing `\n` lets a SQL `--` comment
  swallow the rest of the statement, which then reports success; a missing space joins two
  words, and the statement fails only when it runs. The `+` of literals is folded at compile time and costs nothing.
- **`\n` is fixed; `%n`, `println` and `lineSeparator()` follow the platform.** Write the escape a
  format requires, and keep `%n` for local output.
- **A text block removes escaped quotes and seams.** It still processes backslash escapes and has
  no interpolation; string templates were withdrawn.

## Reference

**Escapes a traditional literal accepts (Java 21):**

| Escape | Character |
|---|---|
| `\b` | U+0008 backspace |
| `\t` | U+0009 tab |
| `\n` | U+000A line feed |
| `\f` | U+000C form feed |
| `\r` | U+000D carriage return |
| `\s` | U+0020 space (since Java 15) |
| `\"`, `\'`, `\\` | the quote, apostrophe or backslash |
| `\0` to `\377` | the octal character code: `\0` is U+0000, `\1` is U+0001 (not a regex backreference), `\101` is `A` |
| `\uXXXX` | a Unicode escape, translated before the source is read as tokens (unit 7.4.4) |
| any other character after `\` | `illegal escape character` |

**Line endings:**

| Written as | Bytes |
|---|---|
| `\n` in a literal or a text block | `0a` on every platform |
| `\r\n` in a literal | `0d 0a` on every platform |
| `%n`, `println`, `System.lineSeparator()`, `BufferedWriter.newLine()` | the `line.separator` property at JVM start: `0a` on Unix, `0d 0a` on Windows |

## Challenges

1. Search a codebase you know for `" +` followed on the next line by SQL. Which seams have neither
   `\n` nor a space, and which statements contain `--`?
2. A service writes CSV that a partner parses with a strict RFC 4180 reader, and the tests run on
   Linux only. How would you make the tests notice a `%n` slipping into the writer?
3. Your team keeps large queries in `.sql` resource files instead of literals. Which of this
   page's costs does that remove, and what does it add?

## Design Note: Why a Text Block Still Processes Escapes

A text block could have been raw, with every backslash kept as written. Java chose to process the
same escapes as a traditional literal, so moving a literal into a block never changes what an
escape means, and a block can still say `\t` or `\r\n` where an invisible character is needed.
The cost is the one the first section measured: a regex or a Windows path in a block doubles its
backslashes exactly as before, and a single `\b` is still a backspace.

## Code Examples

- Test: [TraditionalStringLimitationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TraditionalStringLimitationsTest.java)
- Source: [TraditionalStringLimitations.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TraditionalStringLimitations.java)
