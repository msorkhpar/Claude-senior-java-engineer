# 1.5.4 Switch Patterns in Java (Java 17+)

The refunds service routes payment events with a switch. A rule added in March sends every refund
above 10,000.00 to manual review. In June an auditor finds a B2B refund of 25,000.00 that was paid
without one, and the code compiled without a warning. The same week, a replay from a legacy queue
that stores empty slots as `null` stopped the consumer with a `NullPointerException`, although its
switch has a `default`. Switch patterns were a preview in Java 17 to 20 and are standard since Java 21;
every output below is from Java 21.

## A case With a Type Pattern Tests, Casts and Binds, and the First Case That Matches Wins

A switch over a reference type can use the type patterns of unit 1.5.2 as labels. `case Refund r`
matches when the selector is a `Refund`, and binds it, already cast, to `r`. A `when` clause, the
**guard**, adds a condition the case must also meet. The events and the router:

```java
public class Router {
    sealed interface Event permits Charge, Refund {}
    record Charge(String account, long cents) implements Event {}
    record Refund(String account, long cents) implements Event {}

    static String route(Event event) {
        return switch (event) {
            case Charge c -> "charges";
            case Refund r when r.account().startsWith("B2B-") -> "b2b-refunds";
            case Refund r when r.cents() > 1_000_000 -> "manual-review";
            case Refund r -> "refunds";
        };
    }
}
```

Pause here: where does `Refund("B2B-7", 2_500_000)` go?

```text
Charge[account=C-11, cents=4200] -> charges
Refund[account=C-11, cents=3000] -> refunds
Refund[account=C-12, cents=2500000] -> manual-review
Refund[account=B2B-7, cents=900] -> b2b-refunds
Refund[account=B2B-7, cents=2500000] -> b2b-refunds
```

The switch tries the cases from the top and runs the first whose pattern matches and whose guard
is true. The B2B case matched first, so the review case never saw the refund. Nothing ranks cases
by how specific they are, the way overload resolution ranks methods: order is the only priority.
The fix is to put the case that must win, here the review, first.

A binding is in scope only in its own guard and arm. A colon-style case can't fall into a case
that declares a binding (`illegal fall-through to a pattern`), since the binding would be unset
there.

## The Compiler Rejects a Case That an Earlier Case Always Wins, but It Does Not Read Guards

The compiler does reject a case that can never run because an earlier, unguarded case matches
everything it would:

```java
return switch (o) {
    case CharSequence cs -> "text";
    case String s -> "string";
    default -> "other";
};
```

```text
Dominated.java:5: error: this case label is dominated by a preceding case label
            case String s -> "string";
                 ^
```

Every `String` is a `CharSequence`, so `case String s` could never run. An unguarded case
dominates every later case of its own type or a subtype, guarded or not: `case Integer i` followed
by `case Integer i when i > 100` gets the same error. So does a `default` before a pattern case,
or `case 0` after an unguarded `case Integer i`.

A guarded case dominates nothing, because the compiler doesn't evaluate guards, apart from a
constant `true` or `false`. These two compile
without a warning, and the second never returns `"big"`:

```java
case Integer i when i > 0 -> "positive";
case Integer i when i > 100 -> "big";
```

The router's two guards test different fields, so no compiler could tell that they overlap. Within
one type, write the guarded cases in priority order and the unguarded case last.

## A Guard Runs Only After Its Pattern Matches, and Again for Every Case That Tries It

A guard runs only after its pattern matches, so it can use the binding, and again each time the
switch reaches its case, so a call in two guards runs twice for one event. Here
`isFlagged(account)` stands for a call to the risk service: it flags accounts ending in `-X`, and
counts its calls:

```java
case Refund r when r.cents() > 1_000_000 && isFlagged(r.account()) -> "hold";
case Refund r when isFlagged(r.account()) -> "review";
case Refund r -> "refund";
case Charge c -> "charge";
```

```text
Refund[account=C-12, cents=2500000] -> refund, isFlagged calls: 2
Refund[account=C-13, cents=500] -> refund, isFlagged calls: 1
Refund[account=C-14-X, cents=500] -> review, isFlagged calls: 1
Charge[account=C-15, cents=700] -> charge, isFlagged calls: 0
```

A large refund that isn't flagged costs two calls: a second bill or quota unit on a paid service,
and on a call that consumes something (`iterator.next()`, `queue.poll()`) a second guard that
reads a different element. An exception thrown in a guard propagates as it is:
`Integer.parseInt` in a guard ends the switch with `NumberFormatException`, and no later case is
tried. A local variable that a guard reads must be effectively final.

Keep guards to cheap checks on the binding. When a decision needs a call, make it once in the arm:

```java
case Refund r -> {
    boolean flagged = isFlagged(r.account());
    yield flagged && r.cents() > 1_000_000 ? "hold" : flagged ? "review" : "refund";
}
```

```text
Refund[account=C-12, cents=2500000] -> refund, isFlagged calls: 1
Refund[account=C-14-X, cents=500] -> review, isFlagged calls: 1
```

<!-- practice 1 -->

## Over a Sealed Type, Naming Every Permitted Subtype Is Exhaustive, and Only Unguarded Cases Count

A switch with patterns must cover every value of its selector. The router has no `default` and
compiles, because `Event` is sealed: `Charge` and `Refund` are its only subtypes, and each has a
case. Over a selector of type `Object`, cases for `String` and `Integer` alone are refused; the
switch needs a `default` or a `case Object o`.

A guarded case covers nothing, unless its guard is the constant `true`. Over a sealed `Shape` of `Circle` and `Square`:

```java
return switch (s) {
    case Circle c -> "circle";
    case Square q when q.side() > 0 -> "square";
};
```

```text
GuardOnly.java:6: error: the switch expression does not cover all possible input values
        return switch (s) {
               ^
```

The compiler doesn't try to prove that the guards together accept every square.

As with enums in unit 1.2.5, when the events library adds `Chargeback` to the `permits` list, every switch over `Event` without a `default` stops building at the switch,
and someone decides what a chargeback means for the ledger, the refunds and the reports. A switch
with `default -> "refunds"` builds, and books every chargeback as a refund.

### A Switch Statement With Patterns Must Be Exhaustive Too

Unit 1.2.4 showed a classic statement over an enum that skips a constant and runs nothing. As soon
as a statement uses a pattern, or `case null`, it gets the expression's rule. Over the same `Shape`:

```java
switch (s) {
    case Circle c -> System.out.println("circle");
}
```

```text
StatementPattern.java:6: error: the switch statement does not cover all possible input values
        switch (s) {
        ^
```

A statement that does nothing for some types says so with `default -> {}`.

### A Subtype Added After Compilation Reaches the Switch as MatchException

We compile a `Ledger` class
against the two-type `Event`, then run it with a library jar that adds `Chargeback`:

```text
withDefault(Chargeback[account=C-21, cents=12000]) = refunds
noDefault(Chargeback[account=C-21, cents=12000]) threw java.lang.MatchException
```

As with enums, javac adds a hidden `default` that throws `MatchException`, in the statement form
too. Recompiled against the new library, `noDefault` is the
compile error above. A `default` that throws also fails at run time, but it builds against the new
library, so the compiler no longer points at the switch.

<!-- practice 2 -->

## null Matches Only case null; Neither default Nor case Object Does

The replay's consumer, reduced:

```java
static String replay(Object o) {
    return switch (o) {
        case String s -> "string";
        default -> "other";
    };
}
```

Called with `null`, it throws, and so does the same switch with `case Object x` in place of
`default`:

```text
replay(null) -> threw java.lang.NullPointerException
replayObject(null) -> threw java.lang.NullPointerException
```

A pattern switch keeps the rule of unit 1.2.4: a `null` selector throws before any case is tried,
unless the switch has `case null`. `case Object x` matches every object, and `null` isn't one. So the
binding of a case's type pattern is never null, and a switch written before Java 21 still throws on `null` as it did.

`case null` sends `null` to its own arm, and `case null, default` sends it with everything else:

```java
static String nullDefault(Object o) {
    return switch (o) {
        case String s -> "string " + s;
        case null, default -> "not a string: " + o;
    };
}
```

```text
nullDefault(null) -> not a string: null
nullDefault(42) -> not a string: 42
```

`null` can't share a label with a pattern: `case null, String s` is refused with `invalid case
label combination`. Merge `null` into `default` only when "missing" and "a type I don't handle"
mean the same thing to the caller. Otherwise the merged arm turns a type the code was never
written for into an ordinary missing value. `case null` plays no part in exhaustiveness: the
sealed router is exhaustive with or without it.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **The first case that matches wins.** A type pattern tests, casts and binds; cases are tried in
  order, and a broader guard placed first takes values a later case was written for.
- **The compiler rejects a case that an earlier unguarded case always wins**, but it doesn't
  evaluate guards other than constants, so overlapping guards compile silently.
- **A guard runs after its pattern matches, once for every case that tries it.** A call in two
  guards runs twice; make it once in the arm.
- **Over a sealed type, an unguarded case for every permitted subtype is exhaustive.** Without a
  `default`, a new subtype stops the build, and throws `MatchException` in code that wasn't
  rebuilt. A statement with patterns must be exhaustive too.
- **`null` matches only `case null`.** Neither `default` nor `case Object` does, so without
  `case null` a `null` selector throws `NullPointerException`.

## Reference

**javac 21 errors in a pattern switch:**

| Code | Error |
|---|---|
| `case CharSequence cs` before `case String s` | `this case label is dominated by a preceding case label` |
| `case Integer i` before `case Integer i when ...` | `this case label is dominated by a preceding case label` |
| `default` before a pattern case | `this case label is dominated by a preceding case label` |
| `case Integer i` before `case 0` | `this case label is dominated by a preceding case label` |
| `case String s when true` before `case String s` | `this case label is dominated by a preceding case label` |
| `case String s when false` | `this case label has a guard that is a constant expression with value 'false'` |
| a guarded case before a guarded case it overlaps | none: compiles |
| a guard reading a local that is reassigned | `local variables referenced from a guard must be final or effectively final` |
| a colon case falling into `case Integer i:` | `illegal fall-through to a pattern` |
| `case null, String s` | `invalid case label combination` |
| a type left without an unguarded case, and no `default` | `the switch expression does not cover all possible input values` (`statement` for a statement) |

**What matches a `null` selector:**

| Label | Matches `null` |
|---|---|
| `case null`, `case null, default` | yes |
| `default` | no: `NullPointerException` |
| `case Object o`, or any type pattern | no: `NullPointerException` |

**What makes a pattern switch exhaustive (Java 21):**

| Selector | Exhaustive when |
|---|---|
| a sealed type | every permitted subtype has an unguarded case (a sealed subtype through its own subtypes), or an unguarded case of a supertype covers it, or there is a `default` |
| a record type | also an exhaustive record pattern (unit 1.5.3) |
| any other reference type | an unguarded case of the selector's type or a supertype, or a `default` |

## Challenges

1. A switch over a sealed type has twelve cases and a `default` that logs and returns a safe value.
   What would you check before removing the `default`, and what must the services that use the
   library do on its next release?
2. Two guards on the same type call a service whose answer can change between the two calls. What
   can the switch return that neither answer alone would give, and how do you rule it out?
3. The `Event` hierarchy is used by twenty services you don't own. Would you seal it? What does
   each choice cost when you add `Chargeback`?

## Design Note: A Switch Over a Sealed Type, or a Method on Each Type

Before patterns, the object-oriented answer to "do X for each kind of event" was an abstract
method on `Event`, overridden in each subtype. Adding a subtype was safe: the compiler demanded the
method. Adding an operation meant editing every subtype, which you can't do when the types live in
a library you don't own.

A switch over a sealed type reverses that trade. Operations live with the code that needs them, so
the ledger, the refunds service and the reports each write their own switch, and adding a subtype
touches every one of them. The exhaustiveness check is what makes that safe: each switch without a
`default` shows up as a compile error. Sealed types and switches suit a set of types that is
stable while the operations on it keep growing.

## Code Examples

- Test: [SwitchPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/SwitchPatternTest.java)
- Source: [SwitchPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/SwitchPattern.java)
