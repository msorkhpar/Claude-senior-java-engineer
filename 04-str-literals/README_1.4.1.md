# 1.4.1 String Creation and Initialization in Java

The catalogue service loads a supplier's nightly feed, a million products, into memory. Each line
arrives as a `byte[]`, becomes a `String`, and is split on `;` into SKU, name, currency and
country. After the service moved from Java 17 to Java 21, the shop shows `Caf� cr�me`, and the heap
the catalogue holds grew from 192 MB to 217 MB. Neither the feed nor the code changed. To win the
memory back, someone proposes calling `intern()` on every field.

## Bytes Become Text Only Through a Charset, and new String(bytes) Picks One for You

Decoding bytes into characters needs a charset that says which bytes stand for which character.
The supplier's feed is Latin-1 (ISO-8859-1), where `é` is the single byte `0xE9`. The import, run
on Java 21 first with the Latin-1 default that Java 17 took from the servers' locale, then with
Java 21's own default:

```java
static String[] importLine(byte[] line) {
    return new String(line).split(";");
}
```

```text
$ java -Dfile.encoding=ISO-8859-1 Feed default asis
default ISO-8859-1: Café crème 250 g, ganze Bohne | catalogue: 192 MB
$ java Feed default asis
default UTF-8:    Caf� cr�me 250 g, ganze Bohne | catalogue: 217 MB
```

`new String(byte[])` decodes with the default charset. Up to Java 17 that came from the operating
system's locale; since Java 18 it is UTF-8 by default on every platform (JEP 400). In UTF-8,
`0xE9` opens a three-byte sequence and the next byte doesn't continue it, so there is no character to make.
`new String` doesn't throw: on Java 21 it puts the replacement character U+FFFD (`�`) in its place
and carries on. Nothing is logged. Every accented name reaches the database and the search index
broken, and `�` can't be turned back into `é`.

Name the charset the data is in, and decide what happens to bytes that aren't valid in it:

```java
byte[] latin1 = "Café Müller".getBytes(StandardCharsets.ISO_8859_1);  // é is 0xE9
byte[] utf8 = "Café Müller".getBytes(StandardCharsets.UTF_8);         // é is 0xC3 0xA9
Path latin1File = Files.write(Path.of("out", "latin1.txt"), latin1);

show("new String(latin1)", () -> new String(latin1));
show("new String(latin1, ISO_8859_1)", () -> new String(latin1, StandardCharsets.ISO_8859_1));
show("new String(utf8, ISO_8859_1)", () -> new String(utf8, StandardCharsets.ISO_8859_1));
show("REPORT decoder", () -> StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(latin1)).toString());
show("Files.readString", () -> Files.readString(latin1File));
```

```text
new String(latin1)              Caf� M�ller
new String(latin1, ISO_8859_1)  Café Müller
new String(utf8, ISO_8859_1)    CafÃ© MÃ¼ller
REPORT decoder                  java.nio.charset.MalformedInputException: Input length = 1
Files.readString                java.nio.charset.MalformedInputException: Input length = 3
```

Latin-1 has a character for every byte, so UTF-8 read as Latin-1 gives `Ã©` without an error, and
`new String(bytes, charset)` replaces whatever it can't decode. A `CharsetDecoder` set to `REPORT`
throws instead, and so do `Files.readString` and
`Files.readAllLines`, which read UTF-8 unless told otherwise. At a boundary you want that: a feed in
the wrong charset is rejected, not stored.

Take the feed's charset from its header, byte order mark or contract, and pass it on. Charsets that
look alike aren't: byte `0x80` is `€` in windows-1252 and a control character in ISO-8859-1.
Encoding has its own trap: `getBytes(ISO_8859_1)` turns `€` into `?`.

> **The old default can come back.** `-Dfile.encoding=COMPAT` on Java 18 or later restores the
> locale's charset; with a `C` locale the default then read `US-ASCII`.

<!-- practice 1 -->

## A String Keeps Latin-1 Text at One Byte per Character, and One Character Beyond Latin-1 Doubles It

Heap held per string, after a full GC, for 100,000 strings of 1,000 characters (each built with
`new String(char[])`; the figures vary by a few bytes from run to run):

```java
String base = "a".repeat(LENGTH - 1);
measure("1,000 x 'a'", base + "a");
measure("999 x 'a' + 'é' (U+00E9)", base + "é");
measure("999 x 'a' + '€' (U+20AC)", base + "€");
measure("999 x 'a' + U+FFFD", base + "�");
measure("empty", "");
```

```text
1,000 x 'a'                 1,045 bytes per string
999 x 'a' + 'é' (U+00E9)    1,044 bytes per string
999 x 'a' + '€' (U+20AC)    2,042 bytes per string
999 x 'a' + U+FFFD          2,045 bytes per string
empty                          23 bytes per string
```

Since Java 9 (JEP 254, compact strings) a `String` keeps its characters in a `byte[]` in one of
two encodings, chosen for the whole string when it is created. If every character is at most
U+00FF, it uses Latin-1, one byte per character. Otherwise it uses UTF-16, two bytes for every
character. `é` is inside Latin-1 and costs nothing extra. `€` is not, and neither is U+FFFD: one of
them moves all 1,000 characters to two bytes. The 40-odd bytes on top are the array's header (16)
and the `String` object (24, the empty line: an empty string has no array of its own).

Every accented product name got a U+FFFD and doubled; ASCII names didn't. That is the extra 25 MB.
Heap size isn't UTF-8 size either: `é` takes two bytes in UTF-8 and one in the heap, and `€` three
in UTF-8 and two in the heap, along with every other character of its string.

> **Compact strings can be turned off.** `-XX:-CompactStrings` stores every string as UTF-16: all four long strings above
> then measured about 2,050 bytes.

<!-- practice 2 -->

## Equal Strings Built at Run Time Are Separate Objects; intern() Shares One Only with Code That Keeps Its Result

With the charset named, the catalogue holds about 192 MB again, and half of it is two fields. `split` creates
new strings at run time (unit 1.1.2), so each row has its own `"EUR"` and its own `"DE"`: about 48
bytes each, two million of them.

The JVM keeps one table of shared strings, the string pool, and every string literal refers to a
string in it, which is why equal literals are one object. `s.intern()` looks `s` up there and
returns the pooled string with the same characters, adding `s` if there is none. It can't change
`s`, because nothing can change a `String` (next section). The feed, with four ways to load the
two codes, the last through a `HashMap<String, String> table`:

```java
case "asis" -> s -> s;
case "dropped" -> s -> { s.intern(); return s; };
case "intern" -> String::intern;
case "map" -> s -> { String c = table.putIfAbsent(s, s); return c == null ? s : c; };
```

```text
codes asis:       Café crème 250 g, ganze Bohne | catalogue: 191 MB
codes dropped:    Café crème 250 g, ganze Bohne | catalogue: 192 MB
codes intern:     Café crème 250 g, ganze Bohne | catalogue: 96 MB
codes map:        Café crème 250 g, ganze Bohne | catalogue: 95 MB
```

`s.intern();` as a statement returned the pooled `"EUR"` and threw it away; the row kept its own
copy. A `HashMap` from each string to itself does the same job and belongs to the component: it
goes away with the catalogue, and its size counts the distinct codes.

Interning every field, as proposed, does nothing for values that are unique, such as SKUs. A
million distinct ids, interned and kept, as `jcmd <pid> VM.stringtable` reports them:

```text
Number of entries       :   1000026 =  16000416 bytes, each 16
Number of literals      :   1000026 =  55921800 bytes, avg  55.000
```

Each id got a 16-byte table entry outside the heap, on top of the string itself, and shared
nothing. With the list dropped and a GC, the `Number of literals` bytes fell to 1,736: the
collector reclaims pooled strings that nothing refers to, and the table clears their entries in the
background. Intern or map the fields with few distinct values, never the ids.

<!-- practice 3 -->

## A String Copies What It Is Built From, So Nothing Changes It Afterwards

A fixed-width reader fills one `char[]` buffer per line and cuts its fields out of it:

```java
char[] buffer = "ORD-7;EUR;DE".toCharArray();
String id = new String(buffer, 0, 5);
String currency = new String(buffer, 6, 3);
Arrays.fill(buffer, 'x');                       // the reader reuses its buffer for the next line
System.out.println(id + " " + currency + " | buffer now " + new String(buffer));
```

```text
ORD-7 EUR | buffer now xxxxxxxxxxxx
```

The constructor copied the characters, so refilling the buffer doesn't reach the fields. Every
constructor that takes an array copies it, and `toCharArray()` hands out a copy too. No code
outside `java.lang` holds a reference to a string's array, so a `String` can't change once it
exists. The third argument is a count, not an end index: `new String(chars, 6, 9)` on the same
12 characters throws `StringIndexOutOfBoundsException: Range [6, 6 + 9) out of bounds for length 12`.

The copy costs memory: a `String` built from a million Latin-1 characters allocated 1,000,040 bytes. Because the text
can't change, a `String` computes its hash code once and keeps it: on 100 million characters the
first `hashCode()` took 73,322 µs and the second 0 µs.

A password in a `String` can't be erased: it stays in the heap until the memory is reused, and a
heap dump shows it. That is why `Console.readPassword()` returns a `char[]`, which
its javadoc tells the caller to zero after use.

<!-- practice 4 -->

## Summary

- **Bytes become text only through a charset.** `new String(bytes)` uses the default, UTF-8 since
  Java 18, and silently replaces what it can't decode; name the charset, and use a `REPORT` decoder
  where bad input must be rejected.
- **A string is Latin-1 at one byte per character or UTF-16 at two.** One character above U+00FF,
  a U+FFFD included, doubles the whole string.
- **Strings built at run time are separate objects.** `intern()` or a map shares one copy only
  where the code stores what it returns, and pays only for fields with few distinct values.
- **A `String` copies what it is built from and never changes.** Its fields are safe from the
  buffer it came from, its hash is computed once, and a secret in it can't be wiped.

## Reference

**Building a `String` (Java 21):**

| Call | What you get |
|---|---|
| `"EUR"` | the pooled object, the same for every equal literal |
| `new String(s)` | a new object sharing `s`'s array: 24 bytes allocated for a million-character `s` |
| `new String(char[])`, `new String(char[], offset, count)`, `String.valueOf(char[])` | a copy of the characters |
| `new String(byte[])` | decoded with the default charset; the javadoc leaves invalid bytes unspecified |
| `new String(byte[], Charset)` | invalid bytes replaced with U+FFFD |
| `s.intern()` | the pooled string with `s`'s characters; `s` itself is added if there is none |
| `s.toCharArray()` | a copy; changing it leaves `s` as it was |

**Decoding and encoding:**

| Call | Bytes or characters the charset can't handle |
|---|---|
| `new String(bytes, cs)`, `new InputStreamReader(in, cs)` | replaced with U+FFFD |
| `cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT)` | `MalformedInputException` |
| `Files.readString`, `Files.readAllLines`, `Files.newBufferedReader` | `MalformedInputException`; UTF-8 unless a charset is passed |
| `s.getBytes(cs)` | characters it can't encode replaced with `?` |
| `s.getBytes(ISO_8859_1)` of `€`, byte `0x80` decoded | `?` (63); `0x80` is `€` in windows-1252, U+0080 in ISO-8859-1 |

## Challenges

1. Your API decodes request bodies with the charset in the `Content-Type` header. What do you do
   when the header names a charset the JVM doesn't support, or none at all?
2. A heap dump shows a third of the heap in strings' `byte[]` arrays. How do you tell whether
   duplicates, UTF-16 strings or plain volume are the cause, and what would each fix cost?
3. Measure `intern()` against a `HashMap` canonicalizer for 10 million lookups, once with 50
   distinct values and once with 10 million. Then try `-XX:+UseStringDeduplication` on the
   feed: what does it save, and does `==` between two equal codes change?

## Design Note: Why a String Can Never Change

Java shares one string between unrelated code. The pool hands one `"EUR"` to every literal in every class, a
`HashMap` files a `String` key under a hash it computed once, and a security check validates a file
name before the code opens it. Each is safe only because nobody can change the string afterwards:
the class is `final` and nothing hands its array out.

The cost falls on code that builds text: every change is a new string, which units 1.4.2 and
1.4.5 deal with, and a secret can't be erased. Compact strings rely on the same choice: a string's
encoding is fixed when it is created, because nothing can later add a character that doesn't fit.

## Code Examples

- Test: [StringCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringCreationTest.java)
- Source: [StringCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringCreation.java)
