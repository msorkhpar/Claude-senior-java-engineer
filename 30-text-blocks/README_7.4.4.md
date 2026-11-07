# 7.4.4. Escape Sequences and Special Characters

The nightly settlement file goes to the bank as fixed-width records of 21 characters plus CR LF. Since its template moved into a text block, the bank rejects every file with
`record 1: 15 characters, expected 21`. The source looks right: the padding is typed after the
company name, and the developer's editor saves files with CRLF line endings.

The compiler builds a text block's value in steps: it translates Unicode escapes in the whole
file, turns every line break into LF, strips indentation and trailing spaces (unit 7.4.3), and only
then interprets escapes. The examples print values through this helper, since a terminal hides
trailing spaces and carriage returns:

```java
/** Writes a space as a middle dot and a tab, CR or LF as [TAB], [CR], [LF], which a terminal hides. */
static String show(String s) {
    return s.replace(" ", "\u00b7").replace("\t", "[TAB]").replace("\r", "[CR]").replace("\n", "[LF]");
}
```

## \s Is a Space the Stripping Can't See, So the Spaces Before It Stay

The header record with its padding typed, and again with the last space written as `\s`:

```java
String typed = """
        H|20261106|ACME      
        """;                    // six spaces typed after ACME
String fenced = """
        H|20261106|ACME     \s
        """;
System.out.println(show(typed) + "  length " + typed.length());
System.out.println(show(fenced) + "  length " + fenced.length());
```

```text
H|20261106|ACME[LF]  length 16
H|20261106|ACME······[LF]  length 22
```

The compiler strips trailing spaces from every line, so the typed padding is gone. When it strips,
`\s` is still two characters, a backslash and an `s`, and neither is whitespace, so the five spaces
before it aren't trailing. Escapes are interpreted afterwards, and `\s` becomes the sixth space.

Trailing spaces are stripped because editors add and remove them silently and no reviewer sees
them. A space that matters is therefore written visibly: one `\s` at the end keeps every space on the
line. Markdown's two-space line break loses its padding otherwise.

## A Backslash at the End of a Line Removes the Line Break and Nothing Else

A `\` as the last character of a line joins that line to the next, so a long one-line value can be
written over several source lines. Before reading the output, decide what stands between `orders`
and `WHERE`, and between the `;` and `DB_CLOSE_DELAY`:

```java
String query = """
        SELECT id FROM orders\
        WHERE status = 'OPEN' \
        AND total > 100""";
String config = """
        url=jdbc:h2:mem:orders;\
            DB_CLOSE_DELAY=-1
        """;
System.out.println(show(query));
System.out.println(show(config));
```

```text
SELECT·id·FROM·ordersWHERE·status·=·'OPEN'·AND·total·>·100
url=jdbc:h2:mem:orders;····DB_CLOSE_DELAY=-1[LF]
```

The escape removes the line break and adds no space, so `ordersWHERE` reaches the database, and H2
answers `Table "ORDERSWHERE" not found` at run time: the missing-space seam of unit 7.4.1 is back. The continued line
loses only the indentation common to all lines, so the four extra columns before `DB_CLOSE_DELAY`
stay inside the URL. Indentation is removed while the two lines are still separate; the join comes
last. Put the space at the seam before the `\`, and indent continued
lines like the others.

The backslash must really be the last character. One invisible space after it, and the file doesn't
compile:

```text
SpaceAfter.java:3: error: illegal escape character
            SELECT id FROM orders \   
                                   ^
1 error
```

The compiler checks each backslash as it reads the literal, before stripping, and a backslash
followed by a space is no escape.

<!-- practice 1 -->

## Line Breaks in the Source Become \n Whatever the File Uses; Only an Escape Puts \r in the Value

A script writes this class with CRLF line endings, as the developer's editor or a git checkout
with `core.autocrlf` would. `file` confirms the endings, and the class prints each `char` of its
two blocks in hex:

```java
String plain = """
        H|20261106
        T|0001
        """;
String escaped = """
        H|20261106\r
        T|0001\r
        """;
System.out.println("plain:   " + hex(plain));
System.out.println("escaped: " + hex(escaped));
```

```text
Crlf.java: Java source, ASCII text, with CRLF line terminators
plain:   48 7c 32 30 32 36 31 31 30 36 0a 54 7c 30 30 30 31 0a
escaped: 48 7c 32 30 32 36 31 31 30 36 0d 0a 54 7c 30 30 30 31 0d 0a
```

Every CR LF of the file became a lone `0a`. The compiler turns each line terminator inside a text
block, whether CR LF, CR or LF, into LF before anything else, so the value doesn't depend on who
saved the file. A format that requires CR LF, such as this bank file, HTTP headers or SMTP, gets it
only from the `\r` escape: it is interpreted after the normalisation, and the real line break after
it supplies the `0a`.

## Quotes Need No Escape Until Three Stand in a Row or One Meets the Closing Delimiter

A text block ends at the first `"""`, so a quote needs an escape only where it would help form one.
This line about Python contains three:

```java
class Triple {
    String doc = """
            Python docstrings start with """ and end with it.
            """;
}
```

```text
Triple.java:3: error: ';' expected
            Python docstrings start with """ and end with it.
                                            ^
Triple.java:3: error: ';' expected
            Python docstrings start with """ and end with it.
                                                    ^
Triple.java:3: error: ';' expected
            Python docstrings start with """ and end with it.
                                                            ^
Triple.java:4: error: illegal text block open delimiter sequence, missing line terminator
            """;
               ^
4 errors
```

The block closed mid-line, and javac reports the rest as broken Java without mentioning quotes. Escaping any one of the three is enough. The second trap is a quote at the end
of a value whose closing delimiter is on the same line:

```java
class EndQuote {
    String reply = """
            she said "yes"""";
}
```

```text
EndQuote.java:3: error: unclosed string literal
            she said "yes"""";
                            ^
1 error
```

Three of the four quotes close the block, and the fourth opens a string literal that never ends.
With one quote escaped in each:

```java
String doc = """
        Python docstrings start with \""" and end with it.
        """;
String reply = """
        she said "yes\"""";
System.out.print(doc);
System.out.println(reply);
```

```text
Python docstrings start with """ and end with it.
she said "yes"
```

<!-- practice 2 -->

## Unicode Escapes Are Translated Before the Compiler Sees the Text Block

`\u0022` is a double quote, so it looks like a way around the previous section:

```java
class UnicodeQuotes {
    String doc = """
            Python docstrings start with \u0022\u0022\u0022 and end with it.
            """;
}
```

```text
UnicodeQuotes.java:3: error: ';' expected
            Python docstrings start with \u0022\u0022\u0022 and end with it.
                                                           ^
UnicodeQuotes.java:3: error: ';' expected
            Python docstrings start with \u0022\u0022\u0022 and end with it.
                                                                   ^
UnicodeQuotes.java:3: error: ';' expected
            Python docstrings start with \u0022\u0022\u0022 and end with it.
                                                                           ^
UnicodeQuotes.java:4: error: illegal text block open delimiter sequence, missing line terminator
            """;
               ^
4 errors
```

These are the four errors of the unescaped `"""`. The compiler translates every `\uXXXX` in the file
into its character first, before it divides the source into tokens, so when it looks for the end
of the block, the line holds three ordinary quotes. Every
other escape on this page belongs to the literal and is interpreted last; a unicode escape belongs
to the source text.

A `\u000A` is therefore a real line break. The text after it starts a new line at column 0, which
brings the block's minimum indentation down to zero (this block sits inside a method):

```java
        String greeting = """
                Hello\u000AWorld
                """;
        System.out.println(show(greeting));
```

```text
················Hello[LF]World[LF]
```

Write `\n`, which is interpreted last, and keep
`\u` escapes for characters that play no part in the syntax, such as `\u2022` for a bullet.

<!-- practice 4 -->

## translateEscapes() Interprets Escapes in a String That Arrives at Run Time

Support staff typed `\n` and `\t` into a notification template stored in a database column. The
application reads two characters for each, and `String.translateEscapes()` (Java 15) interprets
them as the compiler would have:

```java
String stored = "Dear %s,\\nyour order shipped.\\tRef:\\s%s";   // as read from a database column
System.out.println(show(stored));
System.out.println(show(stored.translateEscapes()));
```

```text
Dear·%s,\nyour·order·shipped.\tRef:\s%s
Dear·%s,[LF]your·order·shipped.[TAB]Ref:·%s
```

It accepts every escape of a literal, `\s` and `\<line-terminator>` included, and throws
`IllegalArgumentException` on anything else. That includes `\u`, since unicode escapes are not
escapes of a literal: `"Caf\\u00e9 %s".translateEscapes()` fails with
`Invalid escape sequence: \u \\u0075`.

To give a file holding a text block's body the value the compiler would, apply the steps in the
compiler's order, `stripIndent()` (unit 7.4.3) and then `translateEscapes()`. A receipt body saved
to a file, both ways round:

```java
String body = Files.readString(file);   // a text block's body, saved to a file
System.out.println(show(body));
System.out.println(show(body.stripIndent().translateEscapes()));
System.out.println(show(body.translateEscapes().stripIndent()));
```

```text
····TOTAL····\s\r[LF]····PAID\r[LF]····
TOTAL·····[CR][LF]PAID[CR][LF]
TOTAL[LF]PAID[LF]
```

Translated first, `\s` became a space that `stripIndent()` then stripped as trailing, and `\r`
became a CR that formed a CR LF with the real line break, which `stripIndent()` normalised to LF.

<!-- practice 3 -->

## Summary

- **`\s` keeps a trailing space**, because it is not whitespace when trailing spaces are stripped.
  Typed padding is lost.
- **A line-ending `\` removes the line break and nothing else.** It adds no space, a deeper next
  line keeps its extra spaces, and a space after the `\` doesn't compile.
- **Source line breaks are LF in the value, whatever the file uses.** CR LF comes only from `\r`.
- **A quote needs an escape only where three would stand in a row or one meets the closing
  delimiter.** The errors don't mention quotes.
- **Unicode escapes are source text, translated first.** `\u0022` counts as a quote, `\u000A` as a
  line break.
- **`translateEscapes()` interprets escapes in run-time strings.** It rejects `\u`, and on a text
  block's body it comes after `stripIndent()`.

## Reference

**How the compiler builds a text block's value:**

| Step | Works on | Consequence |
|---|---|---|
| unicode escapes translated | the whole source file | `\u0022\u0022\u0022` closes the block |
| line terminators normalised | the block's content | CR LF, CR and LF all become LF |
| incidental indentation and trailing whitespace stripped | each line | typed padding is lost; `\s` is kept |
| escapes interpreted | the content | `\s`, `\r`, `\t` become characters; `\<line-terminator>` is removed |

**Escapes and quotes in a text block (Java 21):**

| In the source | In the value |
|---|---|
| `C:\\` at the end of a line | `C:\` and LF: an escaped backslash, not a continuation |
| last content line ending in `\`, closing delimiter on its own line | no trailing LF |
| `"""` in the content | write `\"""` or `""\"` |
| `""` right before a closing `"""` | closes early; escape the last quote |
| `\"` | `"`; a quote escaped for JSON is written `\\"` (unit 7.4.5) |
| `\u005Ct` | a tab: `\u005C` is a backslash, which then starts `\t` |
| `\\u0041` | the six characters `\u0041`: an escaped backslash starts no unicode escape |

**`String.translateEscapes()` (Java 15):** translates `\b \t \n \f \r \s \" \' \\`, octal `\0` to
`\377`, and discards `\<line-terminator>`; throws `IllegalArgumentException` on a malformed escape;
does not translate unicode escapes.

## Challenges

1. Find text blocks in a codebase you know with spaces at the end of a line and no `\s`. Which did
   the author mean, and which check would catch the next one?
2. A test needs an HTTP request with CR LF after every line. Compare `\r` on every line with
   `.replace("\n", "\r\n")` on a plain block: which is easier to review, and which survives someone
   adding a line?
3. Your team moves its SQL from text blocks into `.sql` resource files. Which of this page's rules
   still apply, and which disappear?

## Design Note: Why Unicode Escapes Come Before Everything Else

Java translates `\uXXXX` before it reads tokens, so that a program kept in ASCII can still contain
any character, in a name as well as in a string. The translation can't know where it is, so it
applies everywhere: a `\u000A` in a `//` comment ends the comment, and whatever follows on that
line compiles as code. Text blocks inherited the rule unchanged. `translateEscapes()` leaves
unicode escapes out, since, in its documentation's words, they "are not part of the string literal
specification", and a string read at run time never went through the compiler's first step.

## Code Examples

- Test: [TextBlockEscapeSequencesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockEscapeSequencesTest.java)
- Source: [TextBlockEscapeSequences.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockEscapeSequences.java)
