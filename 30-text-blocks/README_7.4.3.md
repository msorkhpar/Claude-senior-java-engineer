# 7.4.3. Formatting and Indentation

The nightly settlement export starts every file with three header records, copied from the bank's
specification into a text block. Each record is 16 characters, padded with spaces. The bank
rejects the first file: record 2 is 8 characters long. The padding is in the source file, and
nothing failed when the program ran.

The compiler removes the indentation common to a text block's lines and the whitespace at the end
of each line, and the source shows neither.

## A Printer That Shows Whitespace

A terminal hides trailing spaces and draws a tab like spaces, so every example prints its value's
length and then the value through this helper:

```java
static String show(String s) {
    return s.replace(" ", "·").replace("\t", "→").replace("\n", "⏎\n");
}
```

A space prints as `·`, a tab as `→`, and each `\n` as `⏎` followed by a line break. A second
program, `SourceView`, prints lines of the examples' source file through the same helper, so the
source's own spaces and tabs show too. The outputs are from Java 21.

## The Compiler Removes the Smallest Indentation Among the Non-Blank Lines and the Closing Line

This block sits indented in a method. Its blank line has no spaces at all, because the editor
removed them on save:

```java
String config = """
        {
          "port": 8080,

          "host": "127.0.0.1"
        }
        """;
print("config", config);
```

```text
-- config, length 43
{⏎
··"port":·8080,⏎
⏎
··"host":·"127.0.0.1"⏎
}⏎
```

The compiler counts the leading whitespace of each non-blank line and of the line that holds the
closing `"""`, and removes the smallest count from every line. Here that is 8: the
**incidental** whitespace, which comes from where the block sits in the code. The two extra
spaces of the nested lines are **essential** and stay. Re-indenting the whole block doesn't
change its value.

Pause on the blank line: it has 0 leading spaces, so why isn't the minimum 0?

Blank lines, empty or whitespace only, take no part in the count, and they come out empty. If they
counted, an editor that strips whitespace from blank lines and one that keeps it would compile the
same block to two different strings. Two lines of 2 and 20 spaces between `first` and `last`, both
indented by 16 with the closing delimiter under them:

```text
-- lines of 2 and 20 spaces, length 13
first⏎
⏎
⏎
last⏎
```

## Moving the Closing Delimiter Left Indents Every Line; Moving It Right Changes Nothing

A deploy bot posts this note to a chat that renders Markdown. Someone moved the closing delimiter
back to the method's indentation, 4 spaces left of the content:

```java
String left = """
        **Deploy finished**
        - service: billing
        - version: 4.2.0
    """;
```

The same note with the delimiter 8 spaces to the right of the content:

```java
String right = """
        **Deploy finished**
        - service: billing
        - version: 4.2.0
                """;
```

```text
-- closing delimiter 4 to the left, length 68
····**Deploy·finished**⏎
····-·service:·billing⏎
····-·version:·4.2.0⏎
-- closing delimiter 8 to the right, length 56
**Deploy·finished**⏎
-·service:·billing⏎
-·version:·4.2.0⏎
```

On the left, the delimiter has the smallest count, so every line keeps the 4 spaces between the
delimiter and the content. On the right, its count is larger than the content's and the minimum
stays where the content puts it. Moving the delimiter left is how a literal keeps indentation
common to every line; otherwise that indentation is removed.

In CommonMark a line indented by four spaces is code, so the chat shows the left note as one
preformatted block, asterisks and dashes included. The CommonMark renderer markdown-it gives:

```text
== note-left.md
<pre><code>**Deploy finished**
- service: billing
- version: 4.2.0
</code></pre>
== note-right.md
<p><strong>Deploy finished</strong></p>
<ul>
<li>service: billing</li>
<li>version: 4.2.0</li>
</ul>
```

## Trailing Whitespace Is Removed from Every Line

`SourceView` prints the bank header's source lines:

```text
····static·final·String·HEADER·=·"""
············HDR·20261106·EUR
············ACC·0042········
············CNT·3···········
············""";
```

Its value:

```text
-- HEADER, length 32
HDR·20261106·EUR⏎
ACC·0042⏎
CNT·3⏎
record length 16
record length 8
record length 5
```

After the incidental whitespace, the compiler removes all whitespace at the end of every line.
The reason is the one behind blank lines: trailing whitespace can't be seen, and many editors
delete it on save.

To keep it, end the line with `\s` (unit 7.4.4). Escapes are translated after the stripping, so
the `\s` becomes a space and protects the spaces before it:

```text
····static·final·String·HEADER_FIXED·=·"""
············HDR·20261106·EUR
············ACC·0042·······\s
············CNT·3··········\s
············""";
```

```text
-- HEADER_FIXED, length 51
HDR·20261106·EUR⏎
ACC·0042········⏎
CNT·3···········⏎
record length 16
record length 16
record length 16
```

javac stays silent about `HEADER` unless `-Xlint:text-blocks` (or `-Xlint:all`) is on:

```text
Blocks.java:16: warning: [text-blocks] trailing white space will be removed
    static final String HEADER = """
                                 ^
```

## A Tab and a Space Each Count as One Character

The second line of this query was indented with four tabs and four spaces, in an editor that shows
a tab as four columns. There it sits four columns right of `SELECT`, as intended. `SourceView`
shows what the file holds:

```text
········String·query·=·"""
················SELECT·id,
→→→→····name
················FROM·users
················""";
```

```text
-- query, length 43
········SELECT·id,⏎
name⏎
········FROM·users⏎
```

The compiler can't know how wide your editor draws a tab, so it counts each whitespace character
as one; the `stripIndent()` javadoc says the same of all whitespace. The tabbed line has 8 leading
characters and the others 16, so 8 go: the space-indented lines keep 8 spaces and the tabbed line
keeps none. In SQL that only looks wrong; in YAML or Python the line would move to another
level. The same lint flags it:

```text
Blocks.java:73: warning: [text-blocks] inconsistent white space indentation
        String query = """
                       ^
```

Indent text blocks with one kind of whitespace, and let the lint check it.

<!-- practice 1 -->

## At Run Time, Indentation Is Your Job: indent() and stripIndent()

All of the above happens in javac, to the literal. A value inserted by `formatted` (unit 7.4.2),
Java's only interpolation since String templates were withdrawn after previews in Java 21 and 22,
arrives later, and nothing re-indents it. A code generator inserts a method body:

```java
String body = "int total = 0;\nfor (int x : xs) total += x;\nreturn total;";
String method = """
        int sum(int[] xs) {
            %s
        }
        """.formatted(body);
```

```text
-- formatted(body), length 84
int·sum(int[]·xs)·{⏎
····int·total·=·0;⏎
for·(int·x·:·xs)·total·+=·x;⏎
return·total;⏎
}⏎
```

Only the first inserted line got the 4 spaces in front of `%s`. `indent(n)` adds `n` spaces to
every line, turns every line terminator into `\n` and ends the result with one. So `indent(4)`
supplies both the indentation and the final line break: the placeholder moves to the block's
margin and the closing brace follows it:

```java
String fixed = """
        int sum(int[] xs) {
        %s}
        """.formatted(body.indent(4));
```

```text
-- formatted(body.indent(4)), length 92
int·sum(int[]·xs)·{⏎
····int·total·=·0;⏎
····for·(int·x·:·xs)·total·+=·x;⏎
····return·total;⏎
}⏎
```

`indent(n)` puts its spaces on blank lines too, and a negative `n` removes up to `-n` leading
whitespace characters from each line (both in the Reference).

`stripIndent()` applies the compiler's rule to any string, closing line included: its javadoc
counts the last line "even if blank". Text read from a file usually ends with a newline, so its
last line is empty, counts 0, and nothing is removed, as with a closing delimiter at column 0:

```java
String sql = Files.readString(Path.of("find-user.sql"));   // every line indented, ends with a newline
print("sql.stripIndent()", sql.stripIndent());
print("sql.stripTrailing().stripIndent()", sql.stripTrailing().stripIndent());
```

```text
-- sql.stripIndent(), length 55
····SELECT·id,·name⏎
······FROM·users⏎
·····WHERE·id·=·?⏎
-- sql.stripTrailing().stripIndent(), length 42
SELECT·id,·name⏎
··FROM·users⏎
·WHERE·id·=·?
```

Strip the final line break first with `stripTrailing()`, and add it back if the caller needs it.
Neither method translates escapes; that is `translateEscapes()`, unit 7.4.4.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **The compiler removes the smallest indentation among the non-blank lines and the closing
  line.** Blank lines don't count and come out empty, so the value doesn't depend on the editor.
- **Moving the closing delimiter left indents every line; moving it right changes nothing.** A
  delimiter moved left for tidiness adds its distance to every line.
- **Trailing whitespace is removed from every line.** Padding needs `\s`, and
  `-Xlint:text-blocks` warns when it is missing.
- **A tab and a space each count as one character.** A line indented with tabs among lines
  indented with spaces ends up misaligned.
- **At run time, indentation is your job.** `formatted` doesn't re-indent inserted lines; use
  `indent(n)`. `stripIndent()` removes no indentation from text that ends with a newline.

## Reference

**How javac computes a text block's value (Java 21):**

| Source | Value |
|---|---|
| non-blank lines and the closing delimiter's line | the smallest leading-whitespace count is removed from every line |
| blank line, empty or whitespace only | not counted; comes out empty |
| closing delimiter on its own line, left of the content | every line keeps the difference |
| closing delimiter on its own line, right of the content | no effect on the indentation |
| whitespace at the end of a line | removed; `\s` keeps it (unit 7.4.4) |
| tab | one character, never expanded |

**`-Xlint:text-blocks`** is off by default and part of `-Xlint:all`. Its trailing-whitespace
warning also fires for a closing delimiter right of the content.

**The run-time methods:**

| Call | Result |
|---|---|
| `"a\n\nb".indent(2)` | `"  a\n  \n  b\n"` |
| `"a\r\nb".indent(0)` | `"a\nb\n"` |
| `"      a\n  b\n\t\tc".indent(-4)` | `"  a\nb\nc\n"` |
| `"\tx\n    y".stripIndent()` | `"x\n   y"` |
| `"    a\n    b\n  ".stripIndent()` | `"  a\n  b\n"` |

## Challenges

1. Should your build fail on `-Xlint:text-blocks` warnings? What would it catch in your code
   base, and which correct blocks would it reject?
2. A YAML comparison test fails with a diff that looks empty. Compare the `show` helper,
   comparing lists of lines, and normalising both sides: what does each one hide?
3. A template engine removes the indentation of templates read from files. For a template with
   Windows line endings, and one that mixes tabs and spaces: fix it silently, or refuse it?

## Design Note: The Closing Delimiter Is a Ruler

Java could have kept every character between the delimiters, or made you mark the margin on every
line. It infers the margin instead: the least-indented line, or the closing delimiter, sets it.
A block then moves with its code, and the source carries no markers.

The price is that the value depends on whitespace nobody sees: a delimiter moved for tidiness, a
tab from another editor, padding the compiler drops, and javac says nothing by default. Where
whitespace is part of the format (fixed-width files, YAML, Markdown), turn on the lint and test the
value, not the source.

## Code Examples

- Test: [TextBlockIndentationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockIndentationTest.java)
- Source: [TextBlockIndentation.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockIndentation.java)
