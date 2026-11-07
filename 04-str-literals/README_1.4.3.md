# 1.4.3 String methods (e.g., length(), charAt(), substring())

A partner sends order lines such as `ORD-7|Jane Doe|42.50|`: order, name, amount and an optional
note. The import splits each line at the delimiter in the partner's settings, trims the name,
cuts it to a 20-character column and fills a confirmation template. It had only served partners
that used commas, with ASCII names, a note on every line and no currency signs. With this
partner the fields came out one character each, some names kept a leading space, some ended in `?`, and one confirmation
threw `IndexOutOfBoundsException: No group 4`. Each method followed a rule its name doesn't state.

## length(), charAt() and substring() Count UTF-16 Units, So an Emoji Counts Twice

A `String` is a sequence of UTF-16 `char` values, and every index counts them. A character outside
the Basic Multilingual Plane, such as most emoji, takes two: a surrogate pair.

```java
String name = "Hi \uD83D\uDC4B";                  // "Hi " and a waving hand
System.out.println(name.length() + " " + name.codePointCount(0, name.length()));
String cut = name.substring(0, 4);                 // the column holds 4
System.out.println(new String(cut.getBytes(UTF_8), UTF_8));
```

```text
5 4
Hi ?
```

Counting units is what makes `charAt` and `length()` constant-time. The cut kept the first half
of the pair, a lone surrogate, which UTF-8 cannot encode, so `getBytes` wrote `?`, and that is
what reached the database. Before cutting at `n`, cut one earlier if
`Character.isHighSurrogate(s.charAt(n - 1))`, or count with `offsetByCodePoints`.

A thumbs-up with a skin tone is two code points and four units. Where the limit is what a user sees, `java.text.BreakIterator.getCharacterInstance()`
finds the boundaries.

> **`substring` copies.** In Java 21 a 5-character key cut from a 1,000,000-character line has its
> own array: reflection shows backing arrays of 1,000,000 and 5 bytes (Latin-1 text takes one
> byte per character), so the key doesn't keep the line alive.
> Answers that say it shares the parent's array describe Java 6 and early Java 7.

<!-- practice 1 -->

## split() Takes a Regex and Drops Trailing Empty Strings

```java
String line = "ORD-7|Jane Doe|42.50|";            // order, name, amount, empty note
System.out.println(line.split("|").length);
System.out.println(Arrays.toString(line.split("\\|")));
System.out.println(Arrays.toString(line.split("\\|", -1)));
```

```text
21
[ORD-7, Jane Doe, 42.50]
[ORD-7, Jane Doe, 42.50, ]
```

`|` means "empty or empty", which matches between every two characters, so the line came apart
into 21 one-character pieces. `"+"` throws `PatternSyntaxException`. `Pattern.quote(delimiter)`
turns any delimiter into a literal.

The escaped version returned three fields, not four. `split(regex)` is `split(regex, 0)`, and
limit 0 removes trailing empty strings. Lines with an empty note come back one field short, and
`fields[3]` throws `ArrayIndexOutOfBoundsException`. A negative limit keeps them. `"."` matches
every character, so every piece is empty and `"1.2.3".split(".")` has no elements at all. Check
the field count right after splitting.

<!-- practice 2 -->

## trim() Stops at U+0020, strip() at Unicode Whitespace, and Neither Removes a No-Break Space

Names pasted from a web page or a word processor carry characters that look like spaces:

```java
for (String s : List.of("\u2003Jane", "\u0007Jane", "\u00A0Jane")) {
    System.out.printf("U+%04X  trim: %-5s strip: %-5s isBlank: %s%n", (int) s.charAt(0),
            s.trim().equals("Jane"), s.strip().equals("Jane"), s.substring(0, 1).isBlank());
}
```

```text
U+2003  trim: false strip: true  isBlank: true
U+0007  trim: true  strip: false isBlank: false
U+00A0  trim: false strip: false isBlank: false
```

`trim()` removes every `char` up to U+0020: the space, tabs, line breaks and control characters
such as the bell. `strip()` (Java 11) and `isBlank()` use `Character.isWhitespace`. It accepts the
em space but excludes the no-break space U+00A0, which is meant to hold words together; HTML's
`&nbsp;` produces it. A customer saved as `"\u00A0Jane"` doesn't match `"Jane"`, and `isBlank()`
returns `false` for a field holding only U+00A0, so a required-field check accepts it. A regex
with Unicode classes treats it as whitespace: `s.replaceAll("(?U)^\\s+|\\s+$", "")` returns
`Jane`. It leaves control characters such as the bell, which `trim()` removed.

<!-- practice 3 -->

## replace() Replaces Every Literal Match; replaceAll() Interprets Both of Its Arguments

Next to `replaceAll`, `replace` looks as if it changes only the first match. It changes every
one, literally: `"a-b-c".replace("-", "+")` is `a+b+c`. `replaceAll` takes a pattern:

```java
String template = "Total: {amount}";
System.out.println(template.replace("{amount}", "$42.50"));
System.out.println(template.replaceAll("\\{amount\\}", "C:\\temp"));
System.out.println(template.replaceAll("\\{amount\\}", Matcher.quoteReplacement("$42.50")));
try {
    System.out.println(template.replaceAll("\\{amount\\}", "$42.50"));
} catch (RuntimeException e) {
    System.out.println(e);
}
```

```text
Total: $42.50
Total: C:temp
Total: $42.50
java.lang.IndexOutOfBoundsException: No group 4
```

In a replacement, `$4` means "group 4 of the match" and `\` escapes the next character. A price
with a dollar sign threw; a path lost its backslash without any error. In the pattern,
`"1.5.2".replaceAll(".", "-")` is `-----`. For literal text use `replace`; when you need a
pattern, pass the replacement through `Matcher.quoteReplacement`.

<!-- practice 4 -->

## Summary

- **Indices count UTF-16 units.** An emoji is two, and a cut between them stores `?`.
- **`split` takes a regex and drops trailing empty strings.** Quote the delimiter and pass `-1`.
- **`trim` and `strip` miss the no-break space.** Use a Unicode-aware regex for pasted text.
- **`replace` is literal and replaces every match.** `replaceAll` reads `$` and `\` in the replacement.

## Reference

| Call | Result (Java 21) |
|---|---|
| `"abc".charAt(3)` | `StringIndexOutOfBoundsException: Index 3 out of bounds for length 3` |
| `"abc".substring(2, 5)` | `StringIndexOutOfBoundsException: Range [2, 5) out of bounds for length 3` |
| `"abc".substring(3)`, `substring(2, 2)` | `""`; the end index is exclusive |
| `s.substring(0)` | `s` itself, no copy |
| `"".split(",")` | `[""]`, one element |
| `",a,,b,,".split(",")` | `["", "a", "", "b"]`: leading and inner empties kept, trailing dropped |
| `"".isEmpty()`, `" ".isEmpty()`, `" ".isBlank()` | `true`, `false`, `true` |
| any method on `null` | `NullPointerException` |
| `"straße".toUpperCase(Locale.ROOT)` | `STRASSE`: length 6 becomes 7; case and locale: unit 1.2.4 |

## Challenges

1. Your column limit is in bytes of UTF-8, not characters. Write the cut, and test it with
   `é`, `€` and an emoji.
2. A CSV field may be quoted and contain the delimiter. Where does `split` stop being the right
   tool, and what do you use instead?
3. Search your codebase for `replaceAll(` and `split(` calls whose argument is a variable. Which
   of them could receive a `$`, `|` or `.`?

## Design Note: Why a String Counts char Values

Java chose a 16-bit `char` when Unicode fit in 65,536 code points. Unicode later outgrew that,
and Java 5 added code points through surrogate pairs instead of changing `char`, because every
`charAt` and every index in existing code would have changed meaning. So `length()` stays fast,
while counting code points means a scan for any string that holds a non-Latin-1 character, and
`BreakIterator` always scans. Since Java 9, compact strings store Latin-1 text in one byte per
character, but the API still counts UTF-16 units.

## Code Examples

- Test: [StringMethodsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringMethodsTest.java)
- Source: [StringMethods.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringMethods.java)
