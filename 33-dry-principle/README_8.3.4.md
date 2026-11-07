# 8.3.4. Balancing DRY with Other Principles (e.g., SRP)

On Monday the catalog import rejects every product whose name holds an inch mark, such as
`Monitor 27"`. Friday's deploy changed only the bank-statement import: the bank had started
putting payee names in quotes, and the bank team's tests passed. Nobody on that team opened the
catalog code. Both imports call one method, `Fields.split`, extracted a year earlier because the
two importers had the same loop.

Removing duplication (DRY) and keeping each actor's rules apart (SRP, unit 8.1.1) pull against each
other when two pieces of code look alike.

## Merging Look-Alike Code Ties Two Formats Together, and a Change for One Breaks the Other

The bank's statement file has a date, a payee and an amount on each line; the supplier's product
feed has a name and a price. Both importers split a line on commas and stripped the spaces, so a
developer moved that loop into one class:

```java
import java.util.ArrayList;
import java.util.List;

/** Extracted from StatementImport and CatalogImport, which had the same loop. */
final class Fields {
    private Fields() {}

    static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        for (String f : line.split(",", -1)) out.add(f.strip());
        return out;
    }
}
```

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One line of the bank's statement file: date, payee, amount. */
public class StatementImport {
    public record Entry(LocalDate date, String payee, BigDecimal amount) {}

    public static Entry parse(String line) {
        List<String> f = Fields.split(line);
        return new Entry(LocalDate.parse(f.get(0)), f.get(1), new BigDecimal(f.get(2)));
    }
}
```

`CatalogImport.parse` builds an `Item(name, price)` the same way. Each team has a check; the bank's
already uses the new quoted format:

```java
import java.util.function.Supplier;

public class Checks {
    public static void main(String[] args) {
        check("bank: payee with a comma", "2026-03-02 | ACME, Inc. | -120.00", () -> {
            var e = StatementImport.parse("2026-03-02,\"ACME, Inc.\",-120.00");
            return e.date() + " | " + e.payee() + " | " + e.amount();
        });
        check("catalog: 27\" monitor", "Monitor 27\" | 199.00", () -> {
            var i = CatalogImport.parse("Monitor 27\",199.00");
            return i.name() + " | " + i.price();
        });
    }

    static void check(String name, String expected, Supplier<String> run) {
        String actual;
        try { actual = run.get(); } catch (RuntimeException e) { actual = e.toString(); }
        System.out.println(expected.equals(actual)
                ? "ok      " + name
                : "FAILED  " + name + ": expected " + expected + ", got " + actual);
    }
}
```

Split on every comma, `ACME, Inc.` becomes two fields, and the bank check fails with a
`NumberFormatException`. The bank developer fixes `Fields.split`:

```java
import java.util.ArrayList;
import java.util.List;

/** Extracted from StatementImport and CatalogImport, which had the same loop. */
final class Fields {
    private Fields() {}

    static List<String> split(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (char c : line.toCharArray()) {
            if (c == '"') quoted = !quoted;                   // new: the bank quotes payees
            else if (c == ',' && !quoted) { out.add(field.toString().strip()); field.setLength(0); }
            else field.append(c);
        }
        out.add(field.toString().strip());
        return out;
    }
}
```

Which check fails now?

```text
ok      bank: payee with a comma
FAILED  catalog: 27" monitor: expected Monitor 27" | 199.00, got java.lang.IndexOutOfBoundsException: Index 1 out of bounds for length 1
```

The supplier's feed quotes nothing, so the `"` in `27"` is an inch mark. The new loop read it as an
opening quote, took the comma after it as part of the name, and returned one field.

The two loops were the same text for two file formats, one owned by the bank and one by the
supplier. Unit 8.3.1's test (must one copy change when the other does?) answered no before the
extraction. In unit 8.1.1's terms, `Fields` answers to two actors.

## A Flag for Each Caller Keeps the Method Shared, and Every Change Must Be Checked Against Both

The fix that keeps `Fields` is a parameter: `split(line, quotes)`, `true` from the bank, `false`
from the catalog. Both checks pass. Next the supplier moves its feed to `;` with decimal commas,
`Monitor 27";199,00;SPRING`, where the last field is a promo code and is often empty; `split` gets
a `sep` parameter. Meanwhile the bank's importer has started rejecting any line without exactly
three fields (`if (f.size() != 3) throw ...`). Then the bank's file starts ending every line with a
`,`, as in `2026-03-02,"ACME, Inc.",-120.00,`, and every bank line is rejected. An empty last field
looked like noise in any file, so that change went in without a flag:

```java
import java.util.ArrayList;
import java.util.List;

/** Extracted from StatementImport and CatalogImport, which had the same loop. */
final class Fields {
    private Fields() {}

    /** sep: the catalog's feed moved to ';'.  quotes: only the bank quotes its fields. */
    static List<String> split(String line, char sep, boolean quotes) {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (char c : line.toCharArray()) {
            if (quotes && c == '"') quoted = !quoted;
            else if (c == sep && !quoted) { out.add(field.toString().strip()); field.setLength(0); }
            else field.append(c);
        }
        out.add(field.toString().strip());
        // new: the bank's file now ends every line with a separator, and an empty last field is never data
        if (out.get(out.size() - 1).isEmpty()) out.remove(out.size() - 1);
        return out;
    }
}
```

The checks now read the new formats, and the catalog has a third: `Desk lamp;49,90;`, a product
with no promo code.

```text
ok      bank: payee with a comma
ok      catalog: 27" monitor
FAILED  catalog: no promo code: expected Desk lamp | 49.90 | , got java.lang.IndexOutOfBoundsException: Index 2 out of bounds for length 2
```

For the catalog an empty last field is data: no promo. The repair that keeps the method is a fourth
parameter, `boolean dropTrailingEmpty`, guarding that line. All three checks pass, and the call
sites read:

```java
List<String> f = Fields.split(line, ',', true, true);       // StatementImport
List<String> f = Fields.split(line, ';', false, false);     // CatalogImport
```

`quotes` and `dropTrailingEmpty` are `true` only for the bank, and the value `;` comes only from the
catalog. A parameter value that only one caller passes is that caller's rule written inside shared
code.

A flag separates the callers only where the body reads it: the drop line sat outside every flag,
so it ran for both, and a line added inside a branch runs for every caller that takes that branch.
Every later change must now choose between "for everyone" and "another flag" by reading both
callers' formats. A wrong choice breaks the other import, and its tests catch it only if they hold
the case: without the desk lamp, the catalog checks pass and every product without a promo code is
rejected in production.

## Inlining the Method Back Gives Each Format Its Own Code, and All Checks Pass

Inlining reverses the extraction. Copy the method's body into each caller, replace each parameter
with the value that caller passes, and delete what can no longer run. For the bank, `sep` becomes
`','` and both flags become `true`, so the body stays as it is:

```java
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** One line of the bank's statement file: date, payee, amount. */
public class StatementImport {
    public record Entry(LocalDate date, String payee, BigDecimal amount) {}

    public static Entry parse(String line) {
        List<String> f = fields(line);
        if (f.size() != 3) throw new IllegalArgumentException("expected 3 fields, got " + f.size());
        return new Entry(LocalDate.parse(f.get(0)), f.get(1), new BigDecimal(f.get(2)));
    }

    /** The bank's format: ',' between fields, payees in quotes, a ',' after the last field. */
    private static List<String> fields(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (char c : line.toCharArray()) {
            if (c == '"') quoted = !quoted;
            else if (c == ',' && !quoted) { out.add(field.toString().strip()); field.setLength(0); }
            else field.append(c);
        }
        out.add(field.toString().strip());
        if (out.get(out.size() - 1).isEmpty()) out.remove(out.size() - 1);
        return out;
    }
}
```

For the catalog both flags are `false`, so the quote branch and the line that drops an empty last
field never run. What remains is a split on `;`, which `String.split(";", -1)` does, with each
field then stripped:

```java
import java.math.BigDecimal;

/** One line of the supplier's product feed: name; price with a decimal comma; promo code, often empty. */
public class CatalogImport {
    public record Item(String name, BigDecimal price, String promo) {}

    public static Item parse(String line) {
        String[] f = line.split(";", -1);
        return new Item(f[0].strip(), new BigDecimal(f[1].strip().replace(',', '.')), f[2].strip());
    }
}
```

`Fields` is deleted, and the same checks print:

```text
ok      bank: payee with a comma
ok      catalog: 27" monitor
ok      catalog: no promo code
```

The `-1` matters: with no limit, `split` drops trailing empty strings, and the desk lamp line
throws again.

<!-- practice 1 -->

## The Rule of Three Is a Heuristic for When You Can't Yet Tell

The usual guard against early extraction is the rule of three, which Martin Fowler's *Refactoring*
attributes to Don Roberts: write it once, tolerate a second copy, refactor at the third. Two copies
that agree show little about which of their parts will vary; a third shows more before you choose
parameters.

The count decides nothing when you already know the answer. A second copy of one rule, such as
unit 8.3.1's username check, gets one home at once, because the copies must change together and the
next fix would reach only one of them. Three importers that each split a line are three formats,
and the third copy is no reason to merge them.

<!-- practice 2 -->

## Summary

- **Merging look-alike code ties two formats together.** The bank's quote handling, made in the
  shared `Fields.split`, broke every catalog line with an inch mark.
- **A flag for each caller keeps the method shared and makes every change answer to both.** A
  parameter only one caller sets is that caller's rule; a change that skips the flag breaks the
  other import, as the trailing-field change did.
- **Inlining back gives each format its own code.** Copy the body into each caller, substitute the
  caller's arguments, delete the dead branches; the bank keeps its loop, the catalog becomes one
  `split`, and all checks pass.
- **The rule of three is a heuristic.** One rule gets one home at the second copy; look-alike
  copies of different rules stay apart at any count.

## Reference

What the checks printed for each shape of the code (the first two rows use the comma feed, the rest
the `;` feed):

| Shape | bank: payee with a comma | catalog: `27"` monitor | catalog: no promo code |
|---|---|---|---|
| shared `split(line)`, comma split | FAILED, `NumberFormatException` | ok | — |
| shared `split(line)`, quote handling added for the bank | ok | FAILED, one field | — |
| shared `split(line, sep, quotes)`, empty last field dropped for all | ok | ok | FAILED, two fields |
| shared `split(line, sep, quotes, dropTrailingEmpty)` | ok | ok | ok |
| inlined into each importer | ok | ok | ok |

| Expression (Java 21) | Result |
|---|---|
| `"Desk lamp;49,90;".split(";")` | `[Desk lamp, 49,90]`: trailing empty strings removed |
| `"Desk lamp;49,90;".split(";", -1)` | `[Desk lamp, 49,90, ]`: three elements, the last one `""` |

## Challenges

1. The bank's next format writes a quote inside a payee as `""`. Change `StatementImport.fields` for
   it. Which check could that change break now, and which could it have broken while `Fields` was
   shared?
2. A payroll import arrives: `,`-separated, quoted names, no trailing separator. Copy the bank's
   `fields`, or extract a CSV reader both use? Name each parameter that reader would take, and who
   would ask for changes to it.
3. Find a method with a boolean parameter in a codebase you know, and list its callers with the
   value each passes. Which flags does only one caller set, and what would inlining the method into
   that caller remove?

## Design Note: Why a Wrong Abstraction Outlives Its Use

Sandi Metz described the pattern in "The Wrong Abstraction" (2016): each developer who meets the
shared method takes its existence as proof that it is right, and adding one more parameter looks
cheaper than undoing a colleague's work. Her advice is the one this page ran: inline the code back
into every caller, delete what each caller doesn't need, then look again for what is common.

Inlining has a cost of its own. If the bank and the payroll file both follow one published CSV
specification, their quoting is one rule, and two copies of it can drift as unit 8.3.1's username
checks did. Inlining swaps one risk for another, a shared rule fixed in only one copy, and the
trade is right only when the copies answer to different owners.

## Code Examples

- Test: [DryBalancingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBalancingTest.java)
- Source: [DryBalancing.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBalancing.java)
