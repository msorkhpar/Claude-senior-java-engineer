# 7.4.2. Syntax and Usage of Text Blocks

A contract test compares the order service's JSON with a fixture written as a text block, and it
fails. The failure message prints both values, and they match line for line. The serializer returned
34 characters and the fixture holds 35. The extra one is a newline, and it comes from where the
fixture's closing `"""` sits.

A text block is a string literal written over several lines, with stricter delimiter rules than a
quote, and each rule decides something about the value. What plain literals cost for
multi-line text is unit 7.4.1; literals, `equals` and interning in general are units 1.4.x.

## The Opening Delimiter Ends Its Line, So Even One Line of Text Takes Two

Two attempts at a short text block, and javac 21's errors:

```java
class Opening {
    String a = """Hello""";
}
```

```java
class Comment {
    String b = """   // the greeting
            Hello""";
}
```

```text
bad/Opening.java:2: error: illegal text block open delimiter sequence, missing line terminator
    String a = """Hello""";
                  ^
bad/Opening.java:2: error: illegal text block open delimiter sequence, missing line terminator
    String a = """Hello""";
                          ^
bad/Comment.java:2: error: illegal text block open delimiter sequence, missing line terminator
    String b = """   // the greeting
                     ^
bad/Comment.java:3: error: illegal text block open delimiter sequence, missing line terminator
            Hello""";
                    ^
```

After the opening `"""` only white space and a line terminator may follow, and a comment is not
allowed there either. The second error in each file points at the closing quotes: javac has read
them as another opening delimiter, so editing that end fixes nothing. For the same
reason `"""a""" + """b"""` gives four errors, one per delimiter.

So a single word takes two lines. A closing delimiter at the end of the content line gives the word
alone, and one on the line right after the opener gives the empty string:

```java
String empty = """
        """;
String oneLine = """
        Hello""";
```

```text
empty: length 0, isEmpty true
one line: [Hello] equals "Hello": true
```

For one line, use a plain literal.

## The Closing Delimiter's Line Decides Whether the Value Ends With a Newline

The fixture from the start of the unit, beside the same text closed on its last content line, and
the length and last three bytes of each:

```java
String serialized = "{\n  \"id\": 42,\n  \"status\": \"PAID\"\n}";   // what the serializer returns
String expected = """
        {
          "id": 42,
          "status": "PAID"
        }
        """;
String expectedNoNewline = """
        {
          "id": 42,
          "status": "PAID"
        }""";
```

```text
serialized:       length 34, ends with 220a7d
expected:         length 35, ends with 0a7d0a
expected, closed on the last line: length 34, ends with 220a7d
equals: false / true
lines: 4 / 4
```

Each line break inside the text block becomes `\n`, including the one before a closing delimiter on
its own line, which leaves a `\n` (`0a`) after the `}`. A closing delimiter on the last content line
ends that line before any line break, and the value has none. Counting with `lines()` hides the
difference: both hold four lines, because a final terminator ends a line without starting another.

The extra byte matters wherever text is compared or hashed rather than read: an `equals`, an HMAC
over a request body, a checksum of a generated file. A format that wants a final newline, as POSIX text files do, needs
the closing delimiter on its own line.

The closing delimiter's column matters as well: placed to the left of the content it keeps that much
indentation in every line. Unit 7.4.3 covers how the margin is computed.

<!-- practice 1 -->

## A Text Block Is a Compile-Time Constant, the Same Object as the Equal Literal

A text block has type `String`, and javac turns it into the same constant a quoted literal would
be. The comparisons below go through a helper, so that javac cannot fold the `==`:

```java
static final String PAID = """
        PAID""";
static final String HEADER = """
        id,status
        """;
static final String HEADER_V2 = """
        id,status""" + ",amount\n";
static final String GREETING = """
        Hello, %s""".formatted("Ops");
```

```java
static boolean same(Object a, Object b) { return a == b; }   // a call, so javac can't fold the ==
```

```java
String fromNetwork = new StringBuilder("PA").append("ID").toString();
System.out.println("fromNetwork, PAID:       " + same(fromNetwork, PAID));
```

```text
PAID, "PAID":           true
HEADER, "id,status\n": true
HEADER_V2, literal:      true
GREETING, literal:       false
fromNetwork, PAID:       false
fromNetwork.equals(PAID): true
```

`javap -v` shows why. `PAID`, `HEADER` and `HEADER_V2` are stored in the class file as
`ConstantValue` strings, and the JVM interns string constants, so each is the one pooled instance of
its characters. A text block joined with `+` to another constant is still a constant. `GREETING` is
computed in the static initializer by a call to `String.formatted`, which returns a new object.

A string from a request, a file or a `StringBuilder` is a separate object with the same characters,
and `==` against the constant is `false`. Tests that pass literals to an `==` check never see this.

Being a constant also lets a text block go where Java requires one: a `case` label, an annotation
element, a `static final` field that other classes inline when they compile. The `formatted`
constant can't be a label:

```java
static final String VERSION = "v2";
static final String PING = """
        PING %s""".formatted(VERSION);

static String answer(String command) {
    return switch (command) {
        case PING -> "PONG";
        default -> "?";
    };
}
```

```text
bad/Labels.java:8: error: constant string expression required
            case PING -> "PONG";
                 ^
```

Written as `"""` … `PING""" + " " + VERSION` it compiles, and `answer("PING v2")` returns `PONG`.

Indentation stripping and escape translation happen in javac, so text read at run time gets
neither. A template read from a file keeps its source indentation, and its `\t` stays two
characters. `String.stripIndent()` and `String.translateEscapes()` apply the compiler's two steps
at run time (units 7.4.3 and 7.4.4):

```java
String inSource = """
        id\tstatus
          42\tPAID
        """;
String fromFile = "        id\\tstatus\n          42\\tPAID\n        ";
System.out.println("as read:   " + fromFile.equals(inSource));
System.out.println("processed: " + fromFile.stripIndent().translateEscapes().equals(inSource));
```

```text
as read:   false
processed: true
```

<!-- practice 3 -->

## formatted() Is String.format With the Default Locale, and Every % Is a Specifier

Text blocks have no interpolation. String templates (`STR."..."`) were a preview in Java 21 and
were later dropped (javac 25 rejects their syntax), so a text block is filled with format
specifiers and `formatted(...)`. Its javadoc defines it as `String.format(this, args)`, and that
includes the locale. On a server whose default locale is German:

```java
static final String LINE = """
        invoice %s: EUR %.2f, due %s
        """;
```

```java
Locale.setDefault(Locale.GERMANY);                     // as on a server set up with de_DE
System.out.print(LINE.formatted("INV-7", 1234.5, "2026-12-01"));
System.out.print(String.format(Locale.ROOT, LINE, "INV-7", 1234.5, "2026-12-01"));
```

```text
invoice INV-7: EUR 1234,50, due 2026-12-01
invoice INV-7: EUR 1234.50, due 2026-12-01
```

`formatted` has no overload that takes a `Locale`. When the text goes to a machine (a file another
system parses, a protocol message, a log someone greps), use `String.format(Locale.ROOT, template,
…)`. Keep `formatted` for text meant for people, where the reader's locale is what you want.

The template is a format string, so every `%` in it starts a specifier. Text blocks hold CSS, SQL
and prose, which use `%` freely (`attempt` prints the exception a call throws):

```java
String css = """
        .bar { width: %d%; }
        """;
attempt("width: ", () -> css.formatted(80));
String note = "Paid 50% upfront";
attempt("note:  ", () -> ("""
        Dear %s,
        """ + note).formatted("Jane Doe"));
```

```text
width: java.util.UnknownFormatConversionException: Conversion = ';'
note:  java.util.UnknownFormatConversionException: Conversion = 'u'
```

Write a literal percent sign as `%%`, and never put data into the template. The second example
appended a note to the template and failed on `% u`. Passed to `%s`, the note is copied unchanged:

```text
Dear Jane Doe,
Paid 50% upfront
```

SQL built this way is also open to injection; unit 7.4.5 uses bind parameters instead.

<!-- practice 2 -->

## Summary

- **The opening `"""` must end its line.** A comment after it doesn't compile, a one-line text block
  takes two source lines, and a closing delimiter on the next line gives `""`.
- **The closing delimiter's line decides the final newline.** On its own line the value ends with
  `\n`; on the last content line it doesn't. `lines()` can't tell them apart; `equals` and a hash can.
- **A text block is a compile-time constant `String`.** It is `==` to the equal literal and can be a
  case label, but a string built at run time is a different object, and `formatted(...)` makes a
  run-time value.
- **`formatted` is `String.format` with the default locale.** Use `String.format(Locale.ROOT, …)`
  for machine-read text, write `%%` for a percent sign, and pass data only as arguments.

## Reference

**Delimiter forms (javac 21):**

| Source | Value |
|---|---|
| `"""` + line break + `"""` | `""` |
| `"""` + line break + `Hello"""` | `"Hello"` |
| `"""` + line break + `Hello` + line break + `"""` | `"Hello\n"` |
| a whitespace-only content line | `"\n"` for that line, wherever the closing delimiter sits |
| `"""Hello"""`, or a comment after the opening `"""` | compile error: missing line terminator |

**Constant or not:**

| Expression | Compile-time constant? |
|---|---|
| a text block alone | yes: interned, usable as a `case` label or annotation value |
| text block `+` a literal or constant variable | yes |
| text block `+` a parameter or method result | no |
| a text block with any method call, such as `.formatted(...)` | no: computed at run time |

**Run-time counterparts (`java.lang.String`, since 15):**

| Method | Does |
|---|---|
| `formatted(Object...)` | `String.format(this, args)`, with the default `FORMAT` locale |
| `stripIndent()` | removes incidental indentation and trailing white space, as javac does (unit 7.4.3) |
| `translateEscapes()` | interprets escape sequences such as `\t`, `\n`, `\s` (unit 7.4.4) |

## Challenges

1. Your team's fixtures mix both closing styles. Would you normalise the fixtures, the comparison,
   or the code under test, and what does each choice hide?
2. A library exposes `public static final String SCHEMA` as a text block. You change it and ship
   only the library jar. Which callers see the change? Try it with two classes compiled separately.
3. Find the `formatted` calls in a codebase you know. Which produce text that a machine parses, and
   which would change if the server's locale changed?

## Design Note: Why the Opening Delimiter Must End Its Line

A language could allow text right after the opening `"""`, and some do. Requiring a line break
means every content line starts at a column javac can measure. The incidental indentation is the
smallest of those columns. Text on the opener's line would start at a column set by the code before it, such as
`String json = `, which has nothing to do with the content.

The cost is that a text block is never a compact inline literal: it spans at least two lines, even
when it holds one word.

## Code Examples

- Test: [TextBlockSyntaxTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockSyntaxTest.java)
- Source: [TextBlockSyntax.java](src/main/java/com/github/msorkhpar/claudejavatutor/textblocks/TextBlockSyntax.java)
