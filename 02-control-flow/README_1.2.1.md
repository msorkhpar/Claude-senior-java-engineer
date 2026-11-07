# 1.2.1 if/else statements in Java

One release of the order service shipped three bugs, each in an `if` that passed review.
Customers whose parcel never arrived got no refund. Customers who spent the most got the smallest
discount. And the nightly export died with a `NullPointerException` on a line where nothing
looks like it could be `null`. None of them is about syntax.

## An else Belongs to the Nearest if, Whatever the Indentation Says

The refund rule: a delivered, damaged parcel is refunded, and a parcel that never arrived gets a
tracking request. Without braces it was written like this:

```java
static String refund(boolean delivered, boolean damaged) {
    if (delivered)
        if (damaged)
            return "refund";
    else
        return "track parcel";
    return "no action";
}
```

```text
delivered, damaged:     refund
delivered, not damaged: track parcel
not delivered:          no action
```

An `if` owns exactly one statement, and without braces the inner `if (damaged) ... else ...` is
that statement. The grammar attaches an `else` to the nearest `if` that has none, so this
`else` belongs to `if (damaged)`. Indentation is for people: javac compiles this without a
warning, even with `-Xlint:all`. A parcel that never arrived falls through to `"no action"`, and a
delivered parcel in good condition triggers a tracking request. Braces on every branch make the
binding the one you wrote.

## An else-if Chain Runs the First Branch That Holds, So the Order Is the Specification

The discount tiers were 5% from 1,000 euros and 10% from 5,000:

```java
static int discountPercent(int spentEuros) {
    if (spentEuros >= 1_000) {
        return 5;
    } else if (spentEuros >= 5_000) {
        return 10;
    } else {
        return 0;
    }
}
```

```text
500 EUR -> 0%
1000 EUR -> 5%
7500 EUR -> 5%
```

`else if` is not a keyword of its own: it is an `else` whose one statement is another `if`. So
the conditions are tested from the top, and the first one that holds ends the chain. Every amount
of 5,000 or more already passes `>= 1_000`, so the 10% branch never runs. javac compiles it
without a warning: it doesn't compare conditions, so a branch that no input reaches is still
reachable code to the compiler.

When ranges overlap, put the narrow one first: `>= 5_000`, then `>= 1_000`. When one case is a
special value inside a range, the special value comes first. The boundary operator belongs to the
same specification: with `> 1_000` instead of `>= 1_000`, a customer who spent exactly 1,000 euros
gets nothing, and only a test at exactly 1,000 shows it.

<!-- practice 1 -->

## A Conditional Expression Takes Its Type From Both Branches, So a Primitive on One Side Unboxes the Other

Tenants on the free plan have a quota of 100; paid tenants have the quota from their contract, and
`null` means unlimited. Two versions of the lookup:

```java
static Integer quotaIfElse(boolean freePlan, Map<String, Integer> quotas, String tenant) {
    if (freePlan) {
        return 100;
    } else {
        return quotas.get(tenant);
    }
}

static Integer quota(boolean freePlan, Map<String, Integer> quotas, String tenant) {
    return freePlan ? 100 : quotas.get(tenant);
}
```

For a paid tenant with no quota set:

```text
if/else, no quota set: null
ternary, no quota set: java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
```

That is the nightly export. The `if`/`else` returns each branch as it is. The `?:` operator is an
expression, and an expression has one type, fixed at compile time from the types of both operands:
`100` is an `int` and `quotas.get(...)` an `Integer`, so the whole expression is an `int`. The
`Integer` from the map is unboxed, and unboxing `null` throws (unit 1.1.1). The `int` is then
boxed again for the return type, so nothing on the line looks primitive.

The same rule changes values that aren't `null`. In an export that writes a count or a ratio:

```java
int count = 3;
double ratio = 0.25;
boolean isRatio = false;
Object cell = isRatio ? ratio : count;
```

```text
cell = 3.0 (Double)
```

`int` and `double` are promoted to `double`, so the count arrives as `3.0`, and a consumer that
expects an integer rejects the file.

When the two operands differ in type, write `if`/`else`, or give both sides the type you want:
`freePlan ? Integer.valueOf(100) : quotas.get(tenant)` is an `Integer` expression and returns
`null`, and `isRatio ? (Object) ratio : count` keeps the `Integer`.

<!-- practice 2 -->

## The Compiler Judges an if by Its Constants and Its Exits, Never by What Its Conditions Mean

A parcel fee, declared without an initializer so that every path must set it, and `final` so that
none sets it twice:

```java
public class Fee {
    static int fee(int weightKg) {
        final int fee;
        if (weightKg <= 5) {
            fee = 499;
        } else if (weightKg > 5) {
            fee = 999;
        }
        return fee;
    }
}
```

```text
compile/definite/Fee.java:9: error: variable fee might not have been initialized
        return fee;
               ^
1 error
```

Every `int` passes one of the two conditions, but the definite-assignment rules evaluate only
constant expressions, not what `weightKg <= 5` means, so to javac both conditions can be false.
Make the last branch a plain `else` and it compiles. Dropping `final` changes nothing: the error
comes from reading a local that might be unassigned, and a local of any type, `String`
included, has no default value.

The tempting fix is `int fee = 0;`. It compiles and switches the check off. Add a third weight
class, forget one assignment, and the method returns 0: a free shipment, and no tool complains.
A local declared without a value and assigned in every branch keeps a missing assignment a
compile error.

Constants are what the compiler does evaluate. `if (false) { ... }` compiles, while
`while (false) { ... }` is `error: unreachable statement`. The language exempts `if` on purpose,
so that a constant flag can switch code off:

```java
static final boolean TRACE = false;

static void handle(String request) {
    if (TRACE) {
        System.out.println("handling " + request);
    }
}
```

`javap -c` shows the body of `handle` as a single `return`: the trace code isn't in the class file.

The compiler also tracks whether a branch can complete. A pattern variable introduced by a negated
`instanceof` is in scope after the `if` when the `if`'s body can't finish normally:

```java
static int length(Object payload) {
    if (!(payload instanceof String text)) {
        return -1;
    }
    return text.length();
}
```

Replace the `return -1` with a log line and `return text.length()` becomes
`error: cannot find symbol`: a non-`String` can now reach that line.
That makes the guard clause, which rejects the bad case and returns, the natural shape for checks
at the top of a method: the main path stays unindented and gets the typed variable.

<!-- practice 3 -->

## Summary

- **An `else` binds to the nearest `if` without one.** Indentation doesn't count; braces do.
- **An `else if` chain stops at the first condition that holds.** Order overlapping conditions
  from narrow to wide and test each boundary value; javac never reports a branch no input reaches.
- **`?:` has one type, taken from both operands.** An `int` on one side unboxes an `Integer` on
  the other and throws on `null`; an `int` beside a `double` becomes a `double`.
- **The compiler evaluates constants and tracks exits, nothing else.** A chain needs a final
  `else` to assign a local declared without a value, `if (CONSTANT)` removes code, and a guard that returns keeps
  its pattern variable in scope.

## Reference

**Conditions that compile and surprise (Java 21):**

| Condition | Result |
|---|---|
| `if (retries = 0)` with an `int` | `error: incompatible types: int cannot be converted to boolean` |
| `if (dryRun = true)` with a `boolean` | compiles; assigns `true`, and the branch always runs |
| `if (approved)` with a `Boolean` that is `null` | `NullPointerException` from unboxing |
| `if (false) { ... }` | compiles; the body is not generated |
| `while (false)`, or `while (FLAG)` with a constant `false` | `error: unreachable statement` |

**Type of `c ? a : b` (JLS 15.25), as run:**

| Operands | Type | Effect |
|---|---|---|
| `int`, `Integer` | `int` | the `Integer` is unboxed: `null` throws |
| `Integer`, `Integer` | `Integer` | no unboxing; `null` passes through |
| `int`, `double` | `double` | `3` becomes `3.0` |
| `long`, `double` | `double` | a `long` above 2^53 loses its last digits |
| one operand cast to `Object` | `Object` | each operand keeps its boxed type |

## Challenges

1. Find an `else if` chain of five or more branches in your code base. Which conditions overlap,
   and which test would show that their order is wrong?
2. Your team's style allows a brace-less `if` for one-line returns. Write a rule that keeps that
   and still rules out the refund bug, or argue for braces everywhere.
3. A method returns `Integer` from a `?:` whose operands come from two other methods. One of them
   changes its return type from `Integer` to `int`. What breaks, where, and when do you find out?

## Design Note: Why a Condition Must Be a boolean

In C an `if` takes any integer, and `if (x = 0)` compiles, assigns, and never runs its branch.
Java requires a `boolean`, so that mistake is a type error, and writing `0 == x` to guard against
it buys nothing for numbers. The rule costs a comparison (`if (count != 0)`, not `if (count)`),
and it still lets one case through: when the variable is itself a `boolean`, `if (dryRun = true)`
is legal. Test a boolean by using it, `if (dryRun)`, and there is no `=` to mistype.

## Code Examples

- Test: [IfElseTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/IfElseTest.java)
- Source: [IfElse.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/IfElse.java)
