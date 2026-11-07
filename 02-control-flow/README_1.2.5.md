# 1.2.5 Switch Expressions in Java (Java 14+)

The `Plan` enum in the shared accounts library gains a constant, `EDUCATION`. By the next morning,
three services that switch on `Plan` have reacted in three ways. Invoicing bills schools for 1,000
enterprise seats and logs nothing. The seat-limit service, which nobody rebuilt, answers 500 with
`java.lang.MatchException` for school accounts. The reporting service doesn't build. Only the last
two did their job. Every output below is from Java 21.

## A Switch Expression Is a Value, So Every Arm Must Produce One

A switch statement does things; a switch expression evaluates to one value, like `a + b`. The
statement form can't promise that value even when it looks complete. This statement assigns `n` in
an arm for every one of the four constants of `Plan`:

```java
int n;
switch (plan) {
    case FREE -> n = 1;
    case TEAM -> n = 10;
    case BUSINESS -> n = 50;
    case ENTERPRISE -> n = 1000;
}
return n;
```

```text
StatementAssign.java:11: error: variable n might not have been initialized
        return n;
               ^
```

The compiler doesn't treat a statement over an enum as complete, because a statement may skip a
constant and run nothing (unit 1.2.4). The usual fix is a `default` arm, and a `default` is how
invoicing came to bill schools as enterprises. The expression form needs neither:

```java
return switch (plan) {
    case FREE -> 1;
    case TEAM -> 10;
    case BUSINESS -> 50;
    case ENTERPRISE -> 1000;
};
```

Each arm is an expression, a block that yields a value, or a `throw`. In a statement, a value arm
such as `case FREE -> 1;` is refused with `not a statement`.

An expression also has a type, and here it differs from the conditional operator it often replaces:

```java
Object ternary = k == 0 ? 1 : 2.0;
Object sw = switch (k) { case 0 -> 1; default -> 2.0; };
var v = switch (k) { case 0 -> 1; default -> 2.0; };
```

```text
ternary: 1.0 (Double)
switch:  1 (Integer)
var:     1.0 (Double)
```

Assigned to `Object`, the switch takes the target type, and each arm is boxed on its own. A `?:`
whose operands are both numeric ignores its target, and `var` gives the switch none, so in both
the `int` is promoted to `double`. Code that later checks `instanceof Integer` depends on which one
you wrote.

## An Arrow Label Runs One Arm and Never Falls Through; a Colon Label Still Does, Even in an Expression

The arrow (`case A, B ->`) and the expression form arrived together in Java 14, but they are
independent. An arrow works in a statement too, with no `break`:

```java
switch (plan) {
    case FREE -> features.add("5 projects");
    case TEAM, BUSINESS -> features.add("unlimited projects");
    case ENTERPRISE -> { features.add("unlimited projects"); features.add("SSO"); }
}
```

```text
FREE -> [5 projects]
TEAM -> [unlimited projects]
BUSINESS -> [unlimited projects]
ENTERPRISE -> [unlimited projects, SSO]
```

A switch expression can still use colon labels, ending each case with `yield`. Pause here: what
does this return for `TEAM`?

```java
return switch (plan) {
    case FREE:
        yield 1;
    case TEAM:
        log.add("team discount applied");
    case BUSINESS:
        yield 50;
    default:
        yield 1000;
};
```

```text
colon TEAM seats: 50 [team discount applied]
arrow TEAM seats: 10 [team discount applied]
```

`TEAM` has no `yield`, so it falls into `BUSINESS` and returns 50. The arrow version, with the
`TEAM` arm shown in the next section, gives 10. Mixing arrows and colons in one switch is a compile
error: `different case kinds used in the switch`.

## A Block Arm Hands Out Its Value With yield; return and break Can't Leave a Switch Expression

An arm that needs statements becomes a block, and `yield` gives the block's value:

```java
case TEAM -> {
    log.add("team discount applied");
    yield 10;
}
```

`yield` leaves the switch, not the method. Any other exit except `throw` is refused:

| In a block arm | javac 21 says |
|---|---|
| `return 0;` | `attempt to return out of a switch expression` |
| `break;` | `attempt to break out of a switch expression` |
| `continue;` | `attempt to continue out of a switch expression` |
| a path that ends without `yield` | `switch rule completes without providing a value` |
| `yield 1;` in a switch statement | `yield outside of switch expression` |

An expression must finish with a value, and a `return` from the middle of one would leave the
assignment or call around it with nothing. When one case has to leave the method, check before the
switch or write a statement.

<!-- practice 1 -->

## A Switch Expression Must Cover Every Input, and an Enum Covered Without default Is Rechecked at Every Compile

A switch expression has to produce a value for every possible input. Over an `int`, or a `String` with constant labels,
that means a `default`. Over an enum, naming every constant is enough, and the compiler checks it:

```java
return switch (plan) {
    case FREE -> 1;
    case TEAM -> 10;
    case BUSINESS -> 50;
};
```

```text
EnumMissing.java:4: error: the switch expression does not cover all possible input values
        return switch (plan) {
               ^
```

The check runs at every compile, so once `EDUCATION` is added, the reporting service's switch, which
named all four constants, stops building at this line. That is the reporting service's failure, and the cheapest
one: a developer decides what a school gets before any customer sees it.

Invoicing wrote `default -> 1000;` in place of `case ENTERPRISE`. Recompiled against the new enum,
it builds, and:

```text
withDefault(EDUCATION) = 1000
```

A `default` over an enum you expect to grow gives every new constant the default arm's answer
without anyone deciding it. Keep it for selectors whose values you can't list, such as a `String`.

## A Constant Added After Compilation Reaches the Switch as MatchException

The seat-limit service was compiled against the four-constant `Plan`, and the new library jar was
dropped in without a rebuild. Its switch had no `default` and covered every constant when it was
compiled. A class `Seats` holds both versions, `withDefault` and `noDefault`. We compile it against
the old enum, then recompile only the enum with `EDUCATION`
added, and run:

```text
withDefault(EDUCATION) = 1000
noDefault(EDUCATION) threw java.lang.MatchException
```

For a switch expression over an enum with no `default`, javac adds one of its own that throws, so
the switch fails at the line that needs a decision instead of picking an arm. A rebuild turns the
500 into the reporting service's compile error, which is where the fix belongs.

> **Before release 21 the exception differs.** Compiled with `--release 20` or lower (we ran 14, 17
> and 20), the same switch throws `IncompatibleClassChangeError`.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A switch expression is a value.** Every arm yields or throws, the result is definitely
  assigned without a `default`, and assigned to a reference type it boxes each arm on its own
  rather than promoting like `?:`.
- **Arrows never fall through; colons do, even in an expression.** A colon case without `yield`
  runs into the next case.
- **A block arm leaves only by `yield` or `throw`.** `return`, `break` and `continue` are compile
  errors.
- **An expression must cover every input.** Over an enum, name every constant and leave out
  `default`, so a new constant breaks the build at the switch.
- **An enum constant added after compilation throws `MatchException`** in a switch expression with
  no `default` compiled for release 21, from the `default` javac generated.

## Reference

**Arm forms (Java 21):**

| Form | In a statement | In an expression |
|---|---|---|
| `case A, B -> e;` | `e` must be a statement (call, assignment) | the value of `e` |
| `case A -> { ... }` | runs the block | the value of its `yield` |
| `case A -> throw ...;` | throws | throws |
| `case A: ...` | falls through without `break` | falls through without `yield` |

**When a switch expression is exhaustive:**

| Selector | Exhaustive when |
|---|---|
| `int`, `char`, `short`, `byte`, their wrappers, `String`, with constant labels | it has a `default` |
| an enum | every constant is named, or it has a `default` |
| a sealed type, or any selector with type patterns | see unit 1.5.4 |

A `null` selector throws `NullPointerException` unless the switch has `case null` (Java 21); unit
1.2.4 covers it.

## Challenges

1. The `Plan` enum lives in a library that ten services use. Which of them should switch on it with
   no `default`, and what must the library's release notes promise for that to be safe?
2. A switch expression over a third-party enum throws `MatchException` after a dependency
   upgrade. Would you add a `default`, catch the exception, or fail the build on the upgrade? What
   does each cost?
3. Find a switch statement in your code that assigns one variable in every case. Rewrite it as an
   expression: does the `default` you remove hide anything?

## Design Note: Why the Compiler Writes a default for You

javac checks coverage at compile time and still adds a throwing `default`, because an enum is
compiled separately from the code that switches on it, and nothing stops a new jar from adding a
constant.

The cost lands on library authors. The JLS counts adding a constant to an enum as binary
compatible, yet since Java 14 it can make clients throw. An enum that others switch on is part of a
contract, and a new constant is a change their code must react to.

## Code Examples

- Test: [SwitchExpressionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/SwitchExpressionTest.java)
- Source: [SwitchExpression.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/SwitchExpression.java)
