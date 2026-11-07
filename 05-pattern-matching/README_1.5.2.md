# 1.5.2 Pattern Matching for instanceof (Java 14+)

The export API accepts an optional `timeout` per request. Clients report that it does nothing: a
request sent with 800 ms still waits the 30-second default. The commit that rewrote the option
parsing with a pattern compiled cleanly, and the default-timeout test still passes.

`o instanceof String s` tests `o`, casts it and binds the result to a new variable `s` in one
step; it is standard since Java 16, after previews in 14 and 15. Unit 1.5.1 covered the test
itself: the run-time class, `null`, generic types. This unit is about the variable.

## A Pattern Variable Exists Only Where the Match Is Certain

Before reading the errors, guess which uses of `s` compile (the line numbers count the file's
`class Scopes {` line):

```java
static int scopes(Object o) {
    if (o instanceof String s && s.length() > 3) return s.length();
    int n = o instanceof String s ? s.length() : -1;
    if (o instanceof String s || s.isEmpty()) return 0;
    if (o instanceof String s & s.isEmpty()) return 0;
    if (!(o instanceof String s) || s.isBlank()) {
        System.out.println("no text");
    } else {
        n = s.length();
    }
    return s.length();
}

static int loop(Object o) {
    while (!(o instanceof String s)) {
        o = String.valueOf(o);
    }
    String t = "local";
    if (o instanceof String t) return t.length();
    return s.length();
}
```

```text
Scopes.java:5: error: cannot find symbol
        if (o instanceof String s || s.isEmpty()) return 0;
Scopes.java:6: error: cannot find symbol
        if (o instanceof String s & s.isEmpty()) return 0;
Scopes.java:12: error: cannot find symbol
        return s.length();
Scopes.java:20: error: variable t is already defined in method loop(Object)
        if (o instanceof String t) return t.length();
```

Within the enclosing block, the scope follows the boolean logic: `s` exists wherever the test is
known to have been true, on the right of `&&`, in the true branch of `?:`, in the `else` of a
negated test. On the right of `||` the test has failed, and `&` evaluates its right side even when
the left is false, so `s` doesn't exist there. After a statement, `s` exists only if the statement
can't finish while the test is false. The `if (!(o instanceof String s) || s.isBlank())` falls
through, so the final `return s.length()` fails. A guard clause that returns (unit 1.2.1) or a
`while` loop without a `break` keeps `s` in scope for the rest of the block.

Inside its scope `s` is an ordinary local variable. It isn't implicitly final: `s = s.strip()`
compiles, unless the pattern says `instanceof final String s`. Once reassigned, it is no longer
effectively final, and a lambda can't capture it. A local with the same name is an error, as the
last error shows. A field with the same name is not.

## Out of Its Scope, a Pattern Variable's Name Means the Field

### Spot the Problem: A Guard That Logs Instead of Returning

The export API's request handler, from the start of the unit:

```java
private final Duration timeout = Duration.ofSeconds(30);   // the service default

Duration timeoutFor(Map<String, Object> options) {
    if (!(options.get("timeout") instanceof Duration timeout) || timeout.isNegative()) {
        System.out.println("ignoring timeout option " + timeout);
    }
    return timeout;
}
```

Which `timeout` does each of the three uses read? We call it with 800 ms, then with -5 s:

```text
800 ms:  PT30S
ignoring timeout option PT30S
-5 s:    PT30S
```

Only `timeout.isNegative()` reads the option. In the `if` body the test may have failed, so the
pattern variable isn't in scope there. After the `if` it would be in scope only if the body
couldn't complete, and this body logs and falls through. In both places the name means the field.
Without the field, `cannot find symbol` (unit 1.2.1) would have caught it. With it, the class compiles
under `-Xlint:all` with no warning, every request gets 30 seconds, and the log line prints the
default as if it were the rejected value.

Give the guard its `return`, write the field as `this.timeout`, and log the input:

```java
if (!(options.get("timeout") instanceof Duration timeout) || timeout.isNegative()) {
    System.out.println("ignoring timeout option " + options.get("timeout"));
    return this.timeout;
}
return timeout;
```

```text
guarded 800 ms: PT0.8S
ignoring timeout option PT-5S
guarded -5 s:   PT30S
```

Naming the pattern variable differently from every field (`requested`) turns the original
mistake back into a compile error.

<!-- practice 1 -->

## A Condition Joined with && Sends a Matching Object That Fails It to the Next Branch

A payment router receives `record Payment(String account, long cents)` and
`record Refund(String paymentId, long cents)` events, and was flattened from nested `if`s into one
chain:

```java
static String queue(Object event) {
    if (event instanceof Payment p && p.cents() > 1_000_000) {
        return "review";
    } else if (event instanceof Refund r && r.paymentId() != null) {
        return "refunds";
    } else {
        return "payments";                       // the ordinary payment
    }
}
```

Where does a refund with no payment id go? Below, `flat` is this chain and `nested` tests each
type first, with its condition inside and an `else` that returns `"dead-letter"`; the last event is
a `String`:

```text
Payment[account=acct-17, cents=4500]     flat: payments  nested: payments
Payment[account=acct-17, cents=2000000]  flat: review    nested: review
Refund[paymentId=pay-881, cents=4500]    flat: refunds   nested: refunds
Refund[paymentId=null, cents=90000]      flat: payments  nested: dead-letter
{"type":"chargeback"}                    flat: payments  nested: dead-letter
```

`event instanceof Refund r && r.paymentId() != null` is one boolean. When it is false, the `else`
can't tell which half failed, so it receives refunds without an id as well as every other type.
The comment says "the ordinary payment"; the code puts a refund that matches no payment, and a
chargeback, on the `payments` queue. Give each type one branch that ends the
chain for that type and put its conditions inside, or end with a test for the last type and an
`else` that rejects.

A related failure is order: the first pattern that matches wins, so a supertype tested before its
subtype takes every subtype object, and javac doesn't warn in an `if` chain. A `switch` over the
same patterns refuses to compile (`this case label is dominated by a preceding case label`, unit
1.5.4).

<!-- practice 2 -->

## Since Java 21 a Pattern of the Declared Type Compiles, and It Is Still False for null

`get` on a `Map<String, String>` returns a `String` or `null`, so `instanceof String` can only fail on
`null`:

```java
static String requestId(Map<String, String> headers) {
    if (headers.get("X-Request-Id") instanceof String id && !id.isBlank()) {
        return id;
    }
    return "none";
}
```

```text
absent:  none
present: req-5c1e
null instanceof String: false
```

The pattern is a null check and a local variable in one, and `get` runs once. Java 16 to 20
rejected a pattern that the declared type already guarantees; Java 21 accepts it. javac 21 with
`--release 17`:

```text
Headers.java:6: error: unconditional patterns in instanceof are not supported in -source 17
```

"Unconditional" describes the type only. `instanceof` is false for `null` whatever the pattern,
so the variable is never `null`. A reader who takes the line for a declaration and rewrites it as
`String id = headers.get(...)` turns the absent header into a `NullPointerException` at
`id.isBlank()`.

<!-- practice 3 -->

## Summary

- **A pattern variable exists only where the match is certain.** `&&`, `?:`, the `else` of a
  negated test and a guard that exits extend it; `||`, `&` and a guard that falls through don't.
  Inside, it is an ordinary local, reassignable unless `final`.
- **Out of its scope, the name means a field of the same name.** The code compiles with no
  warning and reads the field.
- **A condition joined with `&&` sends a matching object that fails it to the next branch.** An
  `else` after it receives that type too, and an `if` chain never checks order.
- **A pattern of the declared type compiles since Java 21 and is false for `null`.** It is a null
  check.

## Reference

**Where `s` from `o instanceof String s` is in scope (javac 21):**

| Construct | `s` in scope |
|---|---|
| `o instanceof String s && cond` | in `cond` and the `if` body |
| `o instanceof String s \|\| cond`, `… & cond` | not in `cond` |
| `o instanceof String s ? a : b` | in `a` |
| `if (!(o instanceof String s)) A else B` | in `B` |
| `if (!(o instanceof String s)) { return; }` | after the `if` |
| `if (!(o instanceof String s)) { log(); }` | not after the `if` |
| `while (!(o instanceof String s)) { … }`, no `break` | after the loop |

**The variable and the test (javac 21):**

| Situation | Result |
|---|---|
| `s = s.strip()` inside the scope | compiles |
| `o instanceof final String s`, then `s = …` | `cannot assign a value to final variable s` |
| reassigned, then captured by a lambda | `local variables referenced from a lambda expression must be final or effectively final` |
| a local `s` already in scope | `variable s is already defined` |
| a field `s` | allowed; outside the pattern's scope `s` is the field |
| `o instanceof String s` vs. test then cast, for a local `o` | same bytecode: `instanceof`, `checkcast`, store (`javap -c`) |
| `next() instanceof String s` | calls `next()` once; a test then `(String) next()` calls it twice |
| `o instanceof List<String> l` for an `Object o` | `Object cannot be safely cast to List<String>` (unit 1.5.1) |
| a `Map<String, String>` value `instanceof String v` | compiles with `--release 21`, not with 17 |

## Challenges

1. Search your codebase for pattern variables that share a name with a field of their class. Which
   of them sit after a negated test whose `if` doesn't exit?
2. A chain tests five record types, two of them with conditions. Rewrite it so a new condition
   can't send objects to the wrong branch, and compare the result with a `switch` (unit 1.5.4).
3. Your build moves from `--release 17` to 21. Which null checks followed by a local variable
   would you turn into an unconditional pattern, and which would you leave as they are?

## Design Note: Why the Scope Follows the Logic

A pattern variable could have been scoped like any declaration, to the block around it. Then
`o instanceof String s && s.length() > 3` and the guard clause wouldn't work, and every use would
need a nested `if`. Java scopes it by where the test is known to have been true, which lets
patterns combine with `&&`, `!` and early returns, and later with `switch` and record patterns.

The cost is that one name can mean two variables in the same method, depending on the branch.
When the other meaning is a field, a change to control flow, such as removing a `return`, changes which variable a line reads
without any error.

## Code Examples

- Test: [PatternMatchingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/PatternMatchingTest.java)
- Source: [PatternMatching.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/PatternMatching.java)
