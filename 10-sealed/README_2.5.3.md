# 2.5.3 Use Cases for Sealed Classes and Interfaces

In one week the order service sent shipping emails whose tracking link ended in `/track/null`, and
answered a declined card with HTTP 500 because a new endpoint never caught `DeclinedException`, and
both bugs compiled. A sealed type can make the compiler, or a constructor, refuse each of them, at
a price. How a switch over a sealed type is checked is unit 1.5.4's subject; here we choose where
to use one.

## A Sealed Result Makes an Expected Failure a Value the Caller Has to Match

A declined card is not a bug: the gateway declines cards every day. `charge` can throw, or it can
return one of two records:

```java
sealed interface Result<T> permits Success, Failure {}
record Success<T>(T value) implements Result<T> {}
record Failure<T>(String reason) implements Result<T> {}
```

We charge four cards in a stream, once through `chargeOrThrow`, which throws `DeclinedException`,
and once through `charge`, which returns a `Result`. The gateway declines cards ending in 0:

```java
List<Receipt> receipts = cards.stream().map(c -> chargeOrThrow(c, 2500)).toList();
cards.stream().map(c -> charge(c, 2500)).map(Charges::describe)
     .forEach(line -> System.out.println("result:   " + line));
```

```text
throwing: batch ended with card 4120 declined
result:   charged 4111
result:   not charged: card 4120 declined
result:   charged 4133
result:   not charged: card 4140 declined
```

The exception ended the batch at the second card. Card 4111 had been charged, and its receipt was
lost with the list. The `Result` version kept going and reported every card. A checked exception
would not help in a stream, since it can't leave the lambda (unit 3.2.2).

`describe` switches over the result without a `default`. One that names only `Success` fails to
build:

```text
Forgot.java:7: error: the switch expression does not cover all possible input values
        return switch (r) {
               ^
1 error
```

The switch is the only check. A caller that drops the value,
`charge("4120");`, compiles without a warning, even under `-Xlint:all`, and so does an `instanceof`
with no `else`. Each layer passes a failure on by hand, and there is no stack trace (here `charge`
takes only the card):

```java
static Result<String> checkout(String card) {
    return switch (charge(card)) {
        case Success<String> s -> new Success<>("order paid with " + s.value());
        case Failure<String> f -> new Failure<>(f.reason());
    };
}
```

Use a `Result` for outcomes the caller acts on, a decline or a validation error. A bug, a null
argument or a broken invariant, still throws: nobody can act on it but the developer, who needs the
trace (unit 3.2.4).

<!-- practice 1 -->

## Give Each State Its Own Record, and an Order Without a Tracking Number Can't Be Shipped

The order kept a status enum and one nullable field per state:

```java
enum Status { PENDING, PAID, SHIPPED }

final class Order {
    Status status = Status.PENDING;
    String paymentId;     // set when PAID
    String trackingNo;    // set when SHIPPED
}
```

The warehouse sets `SHIPPED` when the parcel leaves; the carrier sends the tracking number an hour
later. The email's switch over `status` names all three constants. It ran in that hour:

```text
Track it: https://carrier.example.com/track/null
```

Nothing ties `trackingNo` to `SHIPPED`. With a sealed interface, each state is a record that holds
exactly the data true in that state, and each transition takes the state it starts from:

```java
import java.util.Objects;

sealed interface OrderState permits Pending, Paid, Shipped {}
record Pending() implements OrderState {}
record Paid(String paymentId) implements OrderState {
    Paid { Objects.requireNonNull(paymentId, "paymentId"); }
}
record Shipped(String paymentId, String trackingNo) implements OrderState {
    Shipped {
        Objects.requireNonNull(paymentId, "paymentId");
        Objects.requireNonNull(trackingNo, "trackingNo");
    }
}
```

```java
static Shipped ship(Paid p, String trackingNo) { return new Shipped(p.paymentId(), trackingNo); }
```

Shipping a pending order no longer compiles, and shipping without a number fails where it happens,
not in a customer's inbox:

```text
ShipPending.java:10: error: incompatible types: Pending cannot be converted to Paid
        ship(order, "CX-20931");
             ^
```

```text
ship(paid, null): java.lang.NullPointerException: trackingNo
```

The record alone doesn't refuse the null: without the compact constructor,
`new Shipped("pay-7781", null)` builds and prints `trackingNo=null`. The hour between leaving the
warehouse and getting a number becomes a fourth permitted state, `Dispatched(paymentId)`, with no
tracking number for the email to print. The cost: every transition builds a new object, and
whatever stores or serializes an order must record which record it holds.

<!-- practice 2 -->

### An Enum Is Still the Right Choice When a Variant Carries No Data

When every variant is one fixed value, an enum gives more: `values()`, `valueOf` for a database
column, `EnumSet` and `EnumMap`, and one instance per value. A sealed interface has none of these. Its variants
are classes, and `getPermittedSubclasses()` lists classes, not values (unit 2.5.1).

The trouble starts when one variant needs data per order. A constant is one shared instance, so a
field on it belongs to every order at once:

```java
enum Status {
    PENDING, PAID, SHIPPED;
    String trackingNo;          // "just one more field"
}
```

```text
first order's tracking number: CX-20932
```

The second order overwrote the first one's number. A sealed interface can mix both designs, since
an enum may be one of its permitted subtypes (unit 2.5.2).

## A Sealed Exception Hierarchy Lets One catch Answer Every Failure, and a New One Breaks the Build

The payment endpoint must turn every gateway failure into an HTTP answer. An exception must extend
`Throwable`, so the base is a sealed class, not an interface of records. It is abstract, so the
switch needs no case for the base itself (unit 2.5.2):

```java
abstract sealed class PaymentException extends Exception
        permits Declined, InsufficientFunds, GatewayTimeout {
    PaymentException(String message) { super(message); }
}
final class Declined extends PaymentException {
    Declined(String code) { super("declined, code " + code); }
}
final class InsufficientFunds extends PaymentException {
    InsufficientFunds() { super("insufficient funds"); }
}
final class GatewayTimeout extends PaymentException {
    GatewayTimeout() { super("no answer from the gateway"); }
}
```

```java
static String pay(String card) {
    try {
        charge(card);
        return "200 paid";
    } catch (PaymentException e) {
        return switch (e) {
            case Declined d -> "402 " + d.getMessage();
            case InsufficientFunds f -> "402 " + f.getMessage();
            case GatewayTimeout t -> "503 retry later";
        };
    }
}
```

The usual alternative is a catch clause per subtype:

```java
static String payWithClauses(String card) throws PaymentException {
    try {
        charge(card);
        return "200 paid";
    } catch (Declined | InsufficientFunds e) {
        return "402 " + e.getMessage();
    } catch (GatewayTimeout e) {
        return "503 retry later";
    }
}
```

Both versions answer cards 4111, 4105, 4151 and 4199 the same way. javac doesn't count the clauses
as covering the sealed base: without `throws PaymentException`, the clause version fails with
`unreported exception PaymentException; must be caught or declared to be thrown`. Now the gateway
adds `FraudHold` to the `permits` list, and javac reports only the switch:

```text
Gateway.java:35: error: the switch expression does not cover all possible input values
            return switch (e) {
                   ^
1 error
```

With a case added there, card 4166 shows what the clause version does:

```text
4166: 403 held for fraud review | escaped: FraudHold: held for fraud review
```

The fraud hold left the endpoint as an unhandled exception, a 500. Seal a
family of exceptions when one handler must answer all of them; leave it open when callers only
catch the base.

## Public Sealed Interfaces Over Package-Private Records Let Callers Match but Not Implement

A shipping library returns tracking states. Callers must switch over them, and must not build a
`Delivered` the carrier never reported. Public records fail the second rule: anyone can call their
constructor. Package-private records fail the first: a caller can't name them in a case:

```text
app/Peek.java:7: error: InTransitRecord is not public in shipping; cannot be accessed from outside package
        return t instanceof shipping.InTransitRecord r ? r.hub() : "-";
                                    ^
```

The library publishes one sealed interface per variant, each permitting a hidden record:

```java
package shipping;

public sealed interface Tracking permits Tracking.InTransit, Tracking.Delivered {
    String parcelId();

    sealed interface InTransit extends Tracking permits InTransitRecord {
        String hub();
    }

    sealed interface Delivered extends Tracking permits DeliveredRecord {
        String signedBy();
    }

    static Tracking of(String parcelId) {               // stands in for the carrier's API
        return parcelId.startsWith("D")
                ? new DeliveredRecord(parcelId, "Jane Doe")
                : new InTransitRecord(parcelId, "Leipzig");
    }
}

record InTransitRecord(String parcelId, String hub) implements Tracking.InTransit {}

record DeliveredRecord(String parcelId, String signedBy) implements Tracking.Delivered {}
```

A caller in another package switches over `Tracking.InTransit` and `Tracking.Delivered` with no
`default`:

```text
P-31 is at Leipzig
D-07 was signed for by Jane Doe
```

If it implements `Delivered` itself, javac refuses:

```text
app/Fake.java:6: error: class is not allowed to extend sealed class: Delivered (as it is not listed in its 'permits' clause)
```

The cost is two types per variant, and callers lose record patterns, since an interface has no
components. Their tests can't build a `Delivered` either, so the library ships a factory for them.
A public final class with a package-private constructor gives the same guarantees with one type:
`new` from another package fails with `is not public in Delivered`.

<!-- practice 4 -->

## An Expression Tree Is Records and a Recursive switch: Keep the Walk Exhaustive, Only the Rules Partial

A pricing rule, a search query or a formula is a tree. Each node is a record, and each operation
is a function with one switch, no visitor needed: this program's `eval` and `show` are one switch
each. Nested record patterns (unit 1.5.3) let a rewrite rule match a shape two levels deep in one
case:

```java
sealed interface Expr permits Num, Var, Add, Mul {}
record Num(int value) implements Expr {}
record Var(String name) implements Expr {}
record Add(Expr left, Expr right) implements Expr {}
record Mul(Expr left, Expr right) implements Expr {}
```

```java
static Expr simplify(Expr e) {
    Expr walked = switch (e) {                       // the walk: one case per node, no default
        case Num n -> n;
        case Var v -> v;
        case Add(Expr l, Expr r) -> new Add(simplify(l), simplify(r));
        case Mul(Expr l, Expr r) -> new Mul(simplify(l), simplify(r));
    };
    return switch (walked) {                         // the rules: partial by nature
        case Add(Num(int a), Num(int b)) -> new Num(a + b);
        case Mul(Num(int a), Num(int b)) -> new Num(a * b);
        case Add(Expr l, Num(int z)) when z == 0 -> l;
        case Mul(Num(int z), Expr r) when z == 0 -> new Num(0);
        case Mul(Num(int one), Expr r) when one == 1 -> r;
        default -> walked;
    };
}
```

```text
expression:  (1 * (x + 2 * 3) + 0 * x)
at x = 4:    10
simplified:  (x + 6)
```

Rules never cover every tree, so the rule switch needs its `default`. Written as one switch, the
rules and the recursion share it:

```java
static Expr simplifyOneSwitch(Expr e) {
    return switch (e) {
        case Add(Num(int a), Num(int b)) -> new Num(a + b);
        case Mul(Num(int a), Num(int b)) -> new Num(a * b);
        case Add(Expr l, Expr r) -> new Add(simplifyOneSwitch(l), simplifyOneSwitch(r));
        case Mul(Expr l, Expr r) -> new Mul(simplifyOneSwitch(l), simplifyOneSwitch(r));
        default -> e;
    };
}
```

We added `record Neg(Expr operand)` to `permits`. javac reported `eval`, `show` and the walk, and
not `simplifyOneSwitch`. With cases added to the three it named, simplifying `Neg(2 * 3)`:

```text
simplify:          -(6)
simplifyOneSwitch: -(2 * 3)
```

The one-switch version never looks inside the new node. The price of the split is that every
operation lists every node, which suits a tree whose operations grow faster than its node types.

<!-- practice 3 -->

## Summary

- **A sealed `Result` makes an expected failure a value.** A batch keeps going and a switch must
  name `Failure`, but a dropped result compiles and each layer passes failures on by hand.
- **A record per state holds only that state's data.** A transition that takes `Paid` refuses a
  `Pending` at compile time; the compact constructor, not the record, refuses a null.
- **An enum stays right for fixed variants without data**; a field on a constant is shared by
  everyone.
- **A sealed exception base lets one catch switch over every failure**, and a new subtype breaks
  that switch. javac doesn't count one clause per subtype as covering the base, so the method
  declares the base and a new subtype escapes.
- **Public sealed interfaces over package-private records** let callers match but not implement or
  construct; their tests need a factory.
- **An expression tree is records plus a recursive switch.** Keep the walk free of `default`, so a
  new node breaks the build.

## Reference

**Which design refuses which mistake (javac 21):**

| Design | Refused at compile time | Still compiles |
|---|---|---|
| sealed `Result`, switch without `default` | a switch that omits `Failure` | dropping the result; `instanceof` with no `else` |
| record per state, transitions typed by state | `ship(pending, ...)` | `new Shipped(id, null)` unless the compact constructor checks |
| sealed exception, switch inside one `catch` | a new subtype the switch doesn't name | catch clauses that miss it, when the method declares the base |
| public sealed interfaces, package-private records | implementing a variant; naming or building a record | — |
| tree walk without `default` | a new node the walk doesn't handle | a new node under a `default` |

**Enum or sealed interface:**

| Need | Enum | Sealed interface |
|---|---|---|
| different data per instance | no: one shared instance per constant | yes: one record per variant |
| `values()`, `valueOf`, `EnumSet`, `EnumMap` | yes | no |
| exhaustive switch without `default` | yes | yes |

## Challenges

1. Your service has 40 methods that throw `OrderNotFoundException`. Which would you move to a
   `Result`, and what does each caller have to change?
2. The order states gain `Cancelled`, reachable from `Pending` and `Paid` but not from `Shipped`.
   Write the transition methods so a wrong one doesn't compile, and count what else changes.
3. The shipping library must add `Lost`, and some callers can't recompile this quarter. How would
   you ship it, and what do those callers' switches do meanwhile?

## Design Note: A Public Sealed Type Makes Every New Variant a Breaking Change

Inside one codebase, the compile error a new variant causes is the point: it lists every switch to
update. Across a library boundary the same error lands in code the library team doesn't own, and
callers who don't recompile get a `MatchException` when the new variant reaches their switch (unit
1.5.4). A library that seals a public type
promises its variants as part of its API, so it adds one only in a release that may break callers.
When the set must grow freely, a `non-sealed` branch (unit 2.5.2) or a plain interface lets it
grow without breaking callers.

## Code Examples

- Test: [VehicleTypeTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/sealedclasses/VehicleTypeTest.java)
- Source: [VehicleType.java](src/main/java/com/github/msorkhpar/claudejavatutor/sealedclasses/VehicleType.java)