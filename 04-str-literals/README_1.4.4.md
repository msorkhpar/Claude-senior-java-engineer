# 1.4.4 String Comparison in Java: equals() vs. ==

The sign-up service keeps usernames unique in a `HashSet<String>`, and since the duplicate-order
bug of unit 1.1.2 it compares strings with `equals`, never `==`. Four tickets are still open. Two
accounts are named `José`, and nobody can see a difference. `Jane` and `JANE` both registered after
someone made the check ignore case. The admin list puts `Émile` after `zoe`, and a fix to the sort
made three accounts disappear from it. And the profile editor's audit log records a change on
every save, including saves that changed nothing.

`String.equals` answers one narrow question: is the argument a `String` holding the same UTF-16
units (unit 1.4.3)? Each ticket asked it a different question.

## equals Is True Only for Another String, and Its Receiver Must Not Be null

The editor keeps the name being edited in a `StringBuilder`, and the audit compares it with the
saved value:

```java
String saved = "Jane Doe";                                   // the stored value
StringBuilder edited = new StringBuilder("Jane ").append("Doe"); // the editor's buffer
System.out.println("equals:         " + saved.equals(edited));
System.out.println("Objects.equals: " + Objects.equals(saved, edited));
System.out.println("contentEquals:  " + saved.contentEquals(edited));
```

```text
equals:         false
Objects.equals: false
contentEquals:  true
```

`equals` takes an `Object`, so the compiler accepts any argument, and javac says nothing even with
`-Xlint:all`. A `StringBuilder`, a
`Character` or an `Optional<String>` with the same text is never equal. `Objects.equals` adds
null-safety and calls the same `equals`. `contentEquals(CharSequence)` compares the characters of
any `CharSequence`. With `equals`, every save is a change, the customer gets a "profile updated"
mail each time.

The other half is the receiver. The nickname is optional and was never set (compiled with `-g`, so
the message names the variable):

```java
String nickname = null;                                      // optional, never set
System.out.println("\"Jane\".equals(nickname):     " + "Jane".equals(nickname));
System.out.println("Objects.equals(nickname, null): " + Objects.equals(nickname, null));
try {
    nickname.equals("Jane");
} catch (NullPointerException e) {
    System.out.println(e.getMessage());
}
```

```text
"Jane".equals(nickname):     false
Objects.equals(nickname, null): true
Cannot invoke "String.equals(Object)" because "nickname" is null
```

`x.equals(y)` throws when `x` is `null`; `equals(null)` is `false`. Put the literal first where
`null` is a valid value. Use `Objects.equals` where both sides may be `null` and two `null`s count
as equal. Where `null` means a bug upstream, the literal-first form hides it: reject it where it
enters, with `Objects.requireNonNull` (unit 1.1.2).

<!-- practice 1 -->

## Text That Looks the Same Can Be Different Code Points: Normalize Before Comparing

Unicode can write `é` two ways: as one code point, U+00E9, or as `e` followed by a combining acute
accent, U+0301. Both render the same, and which one arrives depends on the keyboard, the operating
system and whatever the text was copied from.

```java
String typed = "Jos\u00e9";                    // é as one code point, U+00E9
String pasted = "Jose\u0301";                  // e, then a combining acute accent U+0301
System.out.println(typed + " / " + pasted + "  equals: " + typed.equals(pasted)
        + "  length: " + typed.length() + " / " + pasted.length());
System.out.println("hashCode: " + typed.hashCode() + " / " + pasted.hashCode()
        + "  equalsIgnoreCase: " + typed.equalsIgnoreCase(pasted));
System.out.println("HashSet size: " + new HashSet<>(List.of(typed, pasted)).size());
String a = Normalizer.normalize(typed, Normalizer.Form.NFC);
String b = Normalizer.normalize(pasted, Normalizer.Form.NFC);
System.out.println("after NFC equals: " + a.equals(b) + "  length: " + b.length());
```

```text
José / José  equals: false  length: 4 / 5
hashCode: 2315003 / 71761770  equalsIgnoreCase: false
HashSet size: 2
after NFC equals: true  length: 4
```

Different units give different hash codes, and the set keeps both: two
accounts named `José`, and a login that finds one or the other depending on how the user typed it.
Case was never the difference, so `equalsIgnoreCase` doesn't help. Unicode calls the two forms
canonically equivalent, and `java.text.Normalizer` turns either into one form: NFC composes, NFD
decomposes. Normalize once where the text enters and store the result; then `equals`, `hashCode`
and `length()` agree.

Removing the accents (NFD, then deleting the combining marks) is a different operation: it makes
`René` and `Rene` one user. NFKC goes further than NFC and also folds compatibility forms, such as
the ligature `ﬁ` into `fi`; use it only where such look-alikes should collide.

<!-- practice 2 -->

## equalsIgnoreCase Has No Matching hashCode: Compare and Hash One Key

The username check moved into a value class whose `equals` ignores case:

```java
record Username(String value) {
    @Override public boolean equals(Object o) {
        return o instanceof Username u && value.equalsIgnoreCase(u.value);
    }
    @Override public int hashCode() { return value.hashCode(); }
}
```

With `Jane` in a `HashSet`:

```java
Set<Username> taken = new HashSet<>();
taken.add(new Username("Jane"));
System.out.println("equals: " + new Username("Jane").equals(new Username("JANE"))
        + "  contains: " + taken.contains(new Username("JANE")));
```

```text
equals: true  contains: false
```

A `HashSet` picks the bucket by `hashCode` and calls `equals` only on entries with the same hash.
`"Jane"` and `"JANE"` hash differently, `equals` is never asked, and `JANE` registers.
`Object.hashCode` requires equal objects to have equal hash codes, and a hash of the raw text gives
`Jane` and `JANE` different ones. Derive one key and use it for both:

```java
record KeyedUsername(String value) {
    String key() { return value.toLowerCase(Locale.ROOT); }   // one fold, used by both
    @Override public boolean equals(Object o) {
        return o instanceof KeyedUsername u && key().equals(u.key());
    }
    @Override public int hashCode() { return key().hashCode(); }
}
```

```java
Set<KeyedUsername> keyed = new HashSet<>();
keyed.add(new KeyedUsername("Jane"));
System.out.println("keyed contains: " + keyed.contains(new KeyedUsername("JANE")));
```

```text
keyed contains: true
```

Never pair `equalsIgnoreCase` in `equals` with a lower-cased key in `hashCode`: the folds disagree.
`equalsIgnoreCase` compares code point by code point, upper-casing then lower-casing each, with no
locale:

```java
Locale saved = Locale.getDefault();
Locale.setDefault(Locale.forLanguageTag("tr-TR"));
System.out.println("tr-TR default: equalsIgnoreCase " + "TITLE".equalsIgnoreCase("title")
        + ", toLowerCase().equals " + "TITLE".toLowerCase().equals("title"));
Locale.setDefault(saved);
System.out.println("STRASSE / straße: equalsIgnoreCase " + "STRASSE".equalsIgnoreCase("stra\u00dfe"));
System.out.println("ı / i: equalsIgnoreCase " + "\u0131".equalsIgnoreCase("i")
        + ", toLowerCase(ROOT) equal " + "\u0131".toLowerCase(Locale.ROOT).equals("i".toLowerCase(Locale.ROOT)));
```

```text
tr-TR default: equalsIgnoreCase true, toLowerCase().equals false
STRASSE / straße: equalsIgnoreCase false
ı / i: equalsIgnoreCase true, toLowerCase(ROOT) equal false
```

It ignores the default locale, which `toLowerCase()` uses: under Turkish, `TITLE` lowers to
`tıtle` (unit 1.2.4). It can't match `ß` with `SS`, since one character never equals two. And it
treats the dotless `ı` as `i`, which a `toLowerCase(Locale.ROOT)` key doesn't, so an `equals` that
calls `equalsIgnoreCase` beside a hash of that key breaks the contract on exactly those names.
Where no hash is needed, a `TreeMap` with `String.CASE_INSENSITIVE_ORDER` folds the way
`equalsIgnoreCase` does, and keeps the first spelling as the key:

```java
Map<String, Integer> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
byName.put("Jane", 1);
byName.put("JANE", 2);
System.out.println(byName);
```

```text
{Jane=2}
```

<!-- practice 3 -->

## compareTo Orders UTF-16 Units; People Need a Collator, and Its 0 Merges Set Entries

The admin list, sorted three ways:

```java
List<String> names = List.of("zoe", "Adam", "\u00c9mile", "adam", "Zoe", "emile", "Bea");
System.out.println("compareTo:              " + names.stream().sorted().toList());
System.out.println("CASE_INSENSITIVE_ORDER: " + names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList());
System.out.println("Collator (en):          " + names.stream().sorted(Collator.getInstance(Locale.ENGLISH)).toList());
```

```text
compareTo:              [Adam, Bea, Zoe, adam, emile, zoe, Émile]
CASE_INSENSITIVE_ORDER: [Adam, adam, Bea, emile, zoe, Zoe, Émile]
Collator (en):          [adam, Adam, Bea, emile, Émile, zoe, Zoe]
```

`compareTo` compares the first differing `char` by its number. Uppercase ASCII letters (65–90)
come before lowercase ones (97–122), and `É` (201) after both. `CASE_INSENSITIVE_ORDER` fixes case,
not accents. A `java.text.Collator` orders by a language's rules: letters first, then accents, then
case. Ask for an explicit locale, because `Collator.getInstance()` uses the server's default, and
the order differs by language:

```java
List<String> staff = List.of("Olsson", "\u00d6berg", "Zetterlund", "Andersson");
System.out.println("sv: " + staff.stream().sorted(Collator.getInstance(Locale.forLanguageTag("sv-SE"))).toList());
System.out.println("de: " + staff.stream().sorted(Collator.getInstance(Locale.GERMANY)).toList());
```

```text
sv: [Andersson, Olsson, Zetterlund, Öberg]
de: [Andersson, Öberg, Olsson, Zetterlund]
```

Swedish sorts `Ö` as a letter after `Z`; German sorts it with `O`.

The fix that lost accounts wanted `adam` and `Adam` side by side. It set the collator's strength to
`PRIMARY`, which compares letters only, and kept the list in a `TreeSet`:

```java
Collator loose = Collator.getInstance(Locale.ENGLISH);
loose.setStrength(Collator.PRIMARY);              // ignore case and accents
Set<String> admin = new TreeSet<>(loose);
admin.addAll(names);
System.out.println(admin);
```

```text
[Adam, Bea, Émile, zoe]
```

A `TreeSet` never calls `equals`. When its comparator returns 0, the new name counts as already
there and is not added, so the admin list shows four of seven accounts. Sort with a collator into
a `List`. Use a sorted set or map only with a comparator whose 0 means what you want a duplicate
to mean. `compareTo`'s 0 matches `equals`; a collator's 0 merges different strings, even at its
default strength.

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **`equals` is true only for another `String`, and its receiver must not be `null`.** Compare a
  `StringBuilder` with `contentEquals`; use `Objects.equals` where both sides may be `null`.
- **Text that looks the same can be different code points.** Normalize to NFC where text enters,
  or equal-looking names get different hash codes and both register.
- **`equalsIgnoreCase` has no matching `hashCode`.** Derive one canonical key and use it for both
  `equals` and `hashCode`; a different fold in each breaks hash sets on the letters where they
  disagree.
- **`compareTo` orders UTF-16 units.** Sort for people with a `Collator` for an explicit locale, and
  keep collated names in a `List`: a sorted set treats its comparator's 0 as a duplicate.

## Reference

| Call (Java 21) | Compares | Notes |
|---|---|---|
| `a.equals(b)` | `b` is a `String` with the same UTF-16 units | `false` for a `StringBuilder` or `null`; NPE when `a` is `null` |
| `Objects.equals(a, b)` | `true` for two `null`s, else `a.equals(b)` when `a` isn't `null` | no help with types |
| `a.contentEquals(cs)` | the `char`s of any `CharSequence` | NPE when `cs` is `null` |
| `a.equalsIgnoreCase(b)` | same length, each code point equal after `toUpperCase` then `toLowerCase` | no locale; `ß` ≠ `SS`; `ı` = `i` |
| `a.compareTo(b)` | first differing `char`, else length | returns the difference: `"apple".compareTo("app")` is 2, `"Zoe".compareTo("adam")` is -7; 0 exactly when `equals` is true |
| `compareToIgnoreCase`, `CASE_INSENSITIVE_ORDER` | as `compareTo`, after the fold `equalsIgnoreCase` uses | `"ı"` and `"i"` compare as 0 |
| `Collator`, strength `TERTIARY` (default) | letters, accents, case | `"Jos\u00e9"` and `"Jose\u0301"` compare as 0 |
| `Collator`, strength `SECONDARY` | letters, accents | `adam` = `Adam`; `emile` < `Émile` |
| `Collator`, strength `PRIMARY` | letters | `adam` = `ÁDAM` |
| `Normalizer`, NFC / NFD | canonical forms, composed / decomposed | `é` (U+00E9) and `e` + U+0301 become one form |
| `Normalizer`, NFKC | NFC plus compatibility forms | `ﬁle` → `file`, fullwidth `ＡＢＣ` → `ABC`; NFC leaves `ﬁ` alone |

`compareTo` compares surrogate units, not code points: `"\uFF61".compareTo("\uD83D\uDE00")` is
positive although U+FF61 is below U+1F600.

## Challenges

1. Your usernames are stored un-normalized in a database with a unique index. Plan the migration
   to NFC: what do you do with the pairs that collide, and how does the database's own collation
   change the answer?
2. Sorting 100,000 names with a `Collator` compares each pair from scratch. Measure it against
   sorting precomputed `CollationKey`s, and decide where the keys belong.
3. Find a class in a code base you know whose `equals` ignores case or trims. Does its `hashCode`
   apply the same fold, and does any `TreeMap` use a comparator that disagrees with it?

## Design Note: Why String.equals Is Exact

`String.equals` compares units and nothing else. It needs no locale and gives the same answer on
every server, which a hash key or a protocol value needs. Everything a
person means by "the same text" (case, accents, the order of a list) depends on the language and
on the application, so Java leaves it to explicit, separate tools: `Normalizer`, `equalsIgnoreCase`
with its documented limits, and `Collator`.

The price is that the plain call is the wrong one for user-facing text, and the compiler says
nothing.

## Code Examples

- Test: [StringComparisonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/literals/StringComparisonTest.java)
- Source: [StringComparison.java](src/main/java/com/github/msorkhpar/claudejavatutor/literals/StringComparison.java)
