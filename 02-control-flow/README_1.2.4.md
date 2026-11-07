# 1.2.4 Switch Statements in Java

One release of the order service changed the `switch` that sends customer emails, and three
tickets came in the same week. Customers whose order was cancelled were told their parcel was on
its way. Orders in the new `ON_HOLD` status got no email at all. And the nightly resend job died
with a `NullPointerException` on a legacy row with no status, although the switch has a `default`.

All three come from how the classic `switch` statement (`case X:` with a colon) runs. The arrow
form and switch expressions are unit 1.2.5; patterns in a switch are units 1.5.x.

## A Case Label Is Where Execution Enters, and It Runs On Until a break

The emails switch, after the change:

```java
switch (status) {
    case PLACED:
        sent.add("order received");
        break;
    case CANCELLED:
        sent.add("order cancelled");
    case SHIPPED:
        sent.add("parcel on its way");
        break;
    default:
        break;
}
```

```text
PLACED -> [order received]
CANCELLED -> [order cancelled, parcel on its way]
SHIPPED -> [parcel on its way]
```

A case label is an entry point, not the start of a block that ends at the next label. The switch
evaluates the selector once, jumps to the matching label, and runs everything below it, across
later labels, until a `break`, `return`, `throw` or the end of the switch. `CANCELLED` has no `break`, so the
cancelled customer gets both emails. Plain `javac` says nothing; `javac -Xlint:fallthrough` does:

```text
Examples.java:18: warning: [fallthrough] possible fall-through into case
            case SHIPPED:
            ^
```

The same rule makes grouping work. Labels stacked with nothing between them, or listed with
commas (`case 'y', 'w':`, allowed in the colon form since Java 14), share the statements below
them, and the lint doesn't flag them.

`default` is a label like the others: its position doesn't change which values reach it, but in
the middle it falls through like any case. This router puts it before `503`:

```java
switch (code) {
    case 200:
        steps.add("ok");
        break;
    default:
        steps.add("log unknown");
    case 503:
        steps.add("retry");
        break;
    case 404:
        steps.add("give up");
}
```

```text
200 -> [ok]
503 -> [retry]
404 -> [give up]
418 -> [log unknown, retry]
```

A `418` gets retried.

The switch body is also one block for declarations: a variable declared under one label is in
scope under the later ones, so declaring `msg` under two labels doesn't compile (`variable msg is
already defined`).

<!-- practice 1 -->

## The Selector Is an int-Compatible Type, a String or an Enum, and Every Label Is a Distinct Constant

With constant labels, a switch takes `char`, `byte`, `short`, `int`, their wrappers, `String` or
an enum. An
order id in a `long` doesn't compile:

```text
LongSel.java:1: error: selector type long is not allowed
```

and neither does a `boolean`, `float` or `double`. The rule matches the bytecode: `javac`
compiles a switch to a `tableswitch` (a jump table, for dense values) or a `lookupswitch` (sorted
keys, for sparse ones), both keyed by an `int`, so a `String` or an enum is mapped to an `int`
first. The order of the cases in the source doesn't affect speed.

Each label must be a constant expression the compiler can put in that table:

```java
    static final int LIMIT = 3;
    static final Integer BOXED = 4;
    static int limit() { return 5; }
    static String f(int n) {
        final int local = 6;
        final int computed = limit();
        switch (n) {
            case LIMIT: return "constant field";
            case local: return "final local, constant initializer";
            case BOXED: return "Integer field";
            case computed: return "final local, method call";
            default: return "other";
        }
    }
```

```text
NonConst.java:11: error: constant expression required
            case BOXED: return "Integer field";
NonConst.java:12: error: constant expression required
            case computed: return "final local, method call";
```

`final` alone is not enough: a constant is a `final` primitive or `String` variable initialised
with a constant expression. The error for `BOXED` is not a type mismatch with the `int` selector:
a field of type `Integer` is never a constant. A label must also fit the selector (`case 200` on a `byte` is a `possible lossy
conversion`), and no two labels may be equal: `case RETRY:`, where `RETRY` is `503`, beside
`case 503:` is a `duplicate case label`. An `if` chain that tests `503` twice compiles without a
warning, and its second branch never runs.

## A String Switch Matches With equals, So It Is Exact and Case-Sensitive

A decoder picks its codec from the `Content-Encoding` header:

```java
switch (encoding) {
    case "gzip":
        return "GZIP";
    case "identity":
        return "IDENTITY";
    default:
        return "unsupported: " + encoding;
}
```

```text
fromHeader == "gzip": false
gzip (new String) -> GZIP
GZIP -> unsupported: GZIP
Gzip -> unsupported: Gzip
```

A value parsed from a request (`fromHeader`, built with `new String("gzip")`) is a new `String`,
not the literal (`==` is `false`), and it still matches: `javac` compiles the switch to a `lookupswitch` on `encoding.hashCode()`, then
`encoding.equals("gzip")` (`javap -c` shows both calls). `equals` compares characters exactly, so
a client that sends `GZIP` is refused. HTTP defines this header as case-insensitive, so the value
is normalised before the switch:

```java
Locale.setDefault(Locale.forLanguageTag("tr-TR"));
codec("IDENTITY".toLowerCase());
codec("IDENTITY".toLowerCase(Locale.ROOT));
```

```text
IDENTITY.toLowerCase()            -> unsupported: ıdentıty
IDENTITY.toLowerCase(Locale.ROOT) -> IDENTITY
```

`toLowerCase()` uses the default locale, and in Turkish an `I` lowers to a dotless `ı`. A server
whose default locale is Turkish refuses every `IDENTITY`. Use `toLowerCase(Locale.ROOT)` for
protocol values.

## A null Selector Throws Before Any Label Is Compared, default Included

The resend job's switch has a `default`. Here a `String`, an enum and an `Integer` switch, each
with a `default`, are called with `null` (compiled with `-g`, so the messages name variables):

```text
String:  Cannot invoke "String.hashCode()" because "<local1>" is null
enum:    Cannot invoke "Examples$Status.ordinal()" because "s" is null
Integer: Cannot invoke "java.lang.Integer.intValue()" because "n" is null
```

Before any label is compared, the switch turns the selector into
an `int`: `hashCode()` for a `String`, `ordinal()` for an enum, unboxing for a wrapper. On `null`
that call throws, and `default` is never considered. The resend job stops at the first legacy
row, and every customer after it gets nothing.

Before Java 21 the fix is a null check in front of the switch. Java 21 adds a `case null` label:

```java
switch (encoding) {
    case null:
        return "IDENTITY (no header)";
    case "gzip":
        return "GZIP";
    default:
        return "unsupported: " + encoding;
}
```

`case null, default:` sends both to one body. A `case null` also changes the rules for the whole
statement: the compiler now requires it to cover every value. The same switch without its
`default` fails with `the switch statement does not cover all possible input values`.

<!-- practice 2 -->

## A Switch Statement Over an Enum Needn't Name Every Constant, So a New Constant Runs Nothing

The fraud team added `ON_HOLD` to the status enum. A version of the emails switch that names
`PLACED`, `PAID`, `SHIPPED` and `CANCELLED`, with no `default`, compiles, with `-Xlint:all` too.
For `ON_HOLD` it returns:

```text
ON_HOLD (no default)    -> []
```

A classic switch statement over an enum, without `case null`, is not checked for coverage: a value
no label names runs nothing. The same happens when only the enum is recompiled, with `ON_HOLD`
inserted in the middle so the later ordinals move: the old switch class still sends each constant
to its case, because it builds its ordinal-to-case table from the constants' names when it first
runs, and `ON_HOLD` matches none.

A `default` that throws makes the first `ON_HOLD` order fail:

```java
    default:
        throw new IllegalStateException("no email rule for " + status);
```

```text
ON_HOLD (default throws) -> java.lang.IllegalStateException: no email rule for ON_HOLD
```

A local assigned under every label and read after the switch doesn't compile without a `default`,
even when every constant is listed (`variable subject might not have been initialized`). A
compile-time error for the missing `ON_HOLD` comes from a switch expression (unit 1.2.5), or from a
statement with `case null`, which must then name every constant.

<!-- practice 3 -->

## Summary

- **A case label is an entry point.** Execution runs on through later labels until `break`, so a
  missing `break` runs the next case, and a `default` in the middle falls through too.
- **Selectors are `char`, `byte`, `short`, `int`, their wrappers, `String` or an enum; labels are
  distinct compile-time constants.** `long`, a boxed field and a duplicate are compile errors.
- **A `String` switch uses `hashCode` and `equals`.** It matches values built at run time, exactly
  and case-sensitively; normalise with `Locale.ROOT`.
- **A `null` selector throws `NullPointerException` before `default` is considered.** Check first,
  or use Java 21's `case null`, which makes the compiler demand full coverage.
- **A switch statement over an enum, without `case null`, is not checked for coverage.** A new
  constant runs nothing unless a `default` throws.

## Reference

| Situation (Java 21, colon-form statement) | Result |
|---|---|
| selector `long`, `float`, `double`, `boolean` | compile error: `selector type long is not allowed` |
| selector `Long` with `case 1L:` | compile error: constant label not compatible with `Long` |
| label from a `static final Integer`, or a `final` local set by a method call | compile error: `constant expression required` |
| label outside the selector's range (`case 200` on `byte`) | compile error: lossy conversion |
| two labels with the same value | compile error: `duplicate case label` |
| qualified enum label `case Status.PLACED:` | compiles |
| same variable declared under two labels | compile error: `already defined` |
| variable declared under one label, read under a later one | compile error: `might not have been initialized` |
| no label matches, no `default` | nothing runs |
| `null` selector without `case null` | `NullPointerException`, `default` or not |
| `case null` without `default`, not every value covered | compile error: `does not cover all possible input values` |
| dense / sparse `int` labels | `tableswitch` / `lookupswitch` with sorted keys |
| `String` selector | `lookupswitch` on `hashCode()`, then `equals` |
| fall-through into a non-empty case | warned only under `-Xlint` or `-Xlint:fallthrough` |

## Challenges

1. Find a switch in your codebase that falls through on purpose. Would a new team member notice?
   Rewrite it so the intent can't be missed, and compare with the arrow form of unit 1.2.5.
2. Your enum has 40 constants and 12 switch statements over it in five services. Which get a
   throwing `default`, and which a harmless one? What does each choice cost the day a constant is
   added?
3. A switch over 2,000 `String` keys, or a `HashMap` lookup: measure both with JMH, then decide on
   something other than speed.

## Design Note: Why the Switch Statement Falls Through

Java's `switch` follows C's, where a case label is a jump target inside one block of code and
falling through is what happens after the jump. The cost is that the common case, one value and
one action, needs a `break` that nothing forces you to write, and falling through on purpose looks
like a forgotten `break` unless a comment says otherwise. Unit 1.2.5's arrow form has no
fall-through, so it needs no `break`.

## Code Examples

- Test: [SwitchStatementTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/SwitchStatementTest.java)
- Source: [SwitchStatement.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/SwitchStatement.java)