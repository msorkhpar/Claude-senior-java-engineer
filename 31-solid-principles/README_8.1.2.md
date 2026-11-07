# 8.1.2. Open/Closed Principle (OCP)

The payments service adds wallet payments. The ticket asks for a 1.5 % fee, so the developer adds
a `Wallet` record and one branch to `feeCents()`, and the fee tests pass. Two weeks later finance
reports wallet money booked a day before the provider pays it out, receipts that say only
"payment", and a customer whose wallet refund failed with an exception. Three other methods had
their own copy of the same type check, and nothing told anyone they existed.

A module is open for extension and closed for modification when a new case is added as new code
and the code that already works isn't edited. All output below is from Java 21.

## A Type Check Copied Into Several Methods Still Compiles When One Copy Is Missed

The service decides each payment's fee, settlement delay, receipt text and refund route in four
methods, each with its own `instanceof` chain:

```java
import java.util.List;

public class Chains {
    interface Payment {}
    record Card(String last4) implements Payment {}
    record BankTransfer(String iban) implements Payment {}
    record Wallet(String provider) implements Payment {}          // new this sprint

    static long feeCents(Payment p, long amountCents) {
        if (p instanceof Card) return 30 + amountCents * 29 / 1000;
        if (p instanceof BankTransfer) return 25;
        if (p instanceof Wallet) return amountCents * 15 / 1000;   // the ticket's one change
        throw new IllegalArgumentException("unknown payment: " + p);
    }

    static int settlementDays(Payment p) {
        if (p instanceof BankTransfer) return 3;
        return 1;                                                  // cards settle next day
    }

    static String receiptLine(Payment p) {
        if (p instanceof Card c) return "card ending " + c.last4();
        if (p instanceof BankTransfer b) return "transfer from " + b.iban();
        return "payment";
    }

    static String refund(Payment p, long amountCents) {
        if (p instanceof Card c) return "reverse " + amountCents + " to card " + c.last4();
        if (p instanceof BankTransfer b) return "send " + amountCents + " to " + b.iban();
        throw new IllegalArgumentException("cannot refund " + p);
    }

    public static void main(String[] args) {
        for (Payment p : List.of(new Card("4242"), new BankTransfer("DE89-0000"), new Wallet("PayCo"))) {
            System.out.printf("%-28s fee %3d, settles in %d day(s), receipt: %s%n",
                    p, feeCents(p, 10_000), settlementDays(p), receiptLine(p));
        }
        System.out.println(refund(new Wallet("PayCo"), 10_000));
    }
}
```

Before reading the output: which of the four methods are wrong for a wallet, and which of them
says so?

```text
Card[last4=4242]             fee 320, settles in 1 day(s), receipt: card ending 4242
BankTransfer[iban=DE89-0000] fee  25, settles in 3 day(s), receipt: transfer from DE89-0000
Wallet[provider=PayCo]       fee 150, settles in 1 day(s), receipt: payment
Exception in thread "main" java.lang.IllegalArgumentException: cannot refund Wallet[provider=PayCo]
	at Chains.refund(Chains.java:30)
	at Chains.main(Chains.java:38)
```

Three are wrong, and all compile. Each chain ends in a fallback or a `throw`, and both hide the
missed case. A fallback answer (`return 1`, `return "payment"`) is wrong without a sound: the
wallet provider pays out after two days, so for a day the books show money that isn't there. A
`throw` waits until the path runs, which for refunds is weeks later, in front of a customer.

The knowledge "what a wallet is" is spread over every method that asks. The only list of those
methods is a text search for `instanceof`, which also finds every unrelated type check, and
`settlementDays()` never mentions `Card` at all.

## Put the Behaviour on the Type, and a New Type Is One New Class That javac Checks

Each type answers the four questions itself:

```java
public interface PaymentMethod {
    long feeCents(long amountCents);
    int settlementDays();
    String receiptLine();
    String refund(long amountCents);
}
```

```java
public record Card(String last4) implements PaymentMethod {
    public long feeCents(long amountCents) { return 30 + amountCents * 29 / 1000; }
    public int settlementDays() { return 1; }
    public String receiptLine() { return "card ending " + last4; }
    public String refund(long amountCents) { return "reverse " + amountCents + " to card " + last4; }
}
```

`BankTransfer` has the same shape, and the code that uses them asks only the interface:

```java
public final class Checkout {
    public static String summary(PaymentMethod p, long amountCents) {
        return String.format("fee %3d, settles in %d day(s), receipt: %s",
                p.feeCents(amountCents), p.settlementDays(), p.receiptLine());
    }
}
```

We compile `PaymentMethod`, `Card`, `BankTransfer` and `Checkout` first. Then we compile a
`Wallet` that forgets the settlement delay, against the compiled classes only:

```text
Wallet.java:1: error: Wallet is not abstract and does not override abstract method settlementDays() in PaymentMethod
public record Wallet(String provider) implements PaymentMethod {
       ^
1 error
```

The missed copy is now a compile error in the new class. javac names one missing method at a
time, so a class missing three needs three rounds, but it doesn't build until all are written.
With `settlementDays()` returning 2 and the receipt `"wallet " + provider`, `Wallet` compiles. A
small `Demo`, compiled with it, calls `Checkout.summary` and `refund` through the old
`Checkout.class`:

```text
== Checkout.class before Wallet:
c6c5cb95973471a3
fee 150, settles in 2 day(s), receipt: wallet PayCo
refund 10000 via PayCo
== Checkout.class after:
c6c5cb95973471a3
```

The caller's class file is byte for byte the one that was tested. The
Strategy and Decorator patterns (unit 9.x) are this same move: a caller written against an
interface takes implementations it has never seen.

<!-- practice 1 -->

## An Interface Is Closed to a New Operation: Adding One Edits Every Implementation

Disputes now need each payment type's chargeback window. That is a new question, not a new type.
Adding `int chargebackDays();` to `PaymentMethod` and recompiling our sources (error lines only):

```text
BankTransfer.java:1: error: BankTransfer is not abstract and does not override abstract method chargebackDays() in PaymentMethod
Card.java:1: error: Card is not abstract and does not override abstract method chargebackDays() in PaymentMethod
Wallet.java:1: error: Wallet is not abstract and does not override abstract method chargebackDays() in PaymentMethod
3 errors
```

A partner team also ships a `GiftCard implements PaymentMethod`, compiled in its own build
against the old interface. We update our three classes (a wallet has no chargebacks: 0) and run
the disputes report with the partner's class file as shipped:

```text
Card[last4=4242] chargeback window: 120 days
BankTransfer[iban=DE89-0000] chargeback window: 0 days
GiftCard[code=GC-77] -> java.lang.AbstractMethodError: Receiver class GiftCard does not define or inherit an implementation of the resolved method 'abstract int chargebackDays()' of interface PaymentMethod.
```

The usual way out is `default int chargebackDays() { return 120; }`. It compiles with nothing else
edited, and answers for everyone:

```text
Card[last4=4242] chargeback window: 120 days
BankTransfer[iban=DE89-0000] chargeback window: 120 days
GiftCard[code=GC-77] chargeback window: 120 days
```

A bank transfer has no chargebacks, so this is the silent fallback of `settlementDays()` in the
first section, moved into the interface. Unit 8.1.4 covers what default methods cost an interface's design.

An interface is open only to new types. A new operation, or a new parameter on an existing one,
edits every implementation, and the ones you don't own fail when the new method is called. In
the chain layout of the first section, the chargeback window would have been one new method.

## When the Types Are Fixed and the Operations Grow, Seal the Hierarchy and Let javac List the Switches

Card, transfer and wallet are a set the payments team owns and changes a few times a year, while
finance, disputes and reporting keep adding questions about them. For that shape the first
section's layout, one method per question, is right, written as switches over a sealed type
(unit 2.5.3):

```java
public class Payments {
    sealed interface Payment permits Card, BankTransfer {}
    record Card(String last4) implements Payment {}
    record BankTransfer(String iban) implements Payment {}

    static long feeCents(Payment p, long amountCents) {
        return switch (p) {
            case Card c -> 30 + amountCents * 29 / 1000;
            case BankTransfer b -> 25;
        };
    }

    static int settlementDays(Payment p) {
        return switch (p) {
            case Card c -> 1;
            case BankTransfer b -> 3;
        };
    }

    static String receiptLine(Payment p) {
        return switch (p) {
            case Card c -> "card ending " + c.last4();
            case BankTransfer b -> "transfer from " + b.iban();
        };
    }

    static String refund(Payment p, long amountCents) {
        return switch (p) {
            case Card c -> "reverse " + amountCents + " to card " + c.last4();
            case BankTransfer b -> "send " + amountCents + " to " + b.iban();
        };
    }
}
```

The wallet ticket again: `Wallet` goes into `permits`, and a case goes into `feeCents()` only.

```text
Payments.java:16: error: the switch expression does not cover all possible input values
        return switch (p) {
               ^
Payments.java:23: error: the switch expression does not cover all possible input values
        return switch (p) {
               ^
Payments.java:30: error: the switch expression does not cover all possible input values
        return switch (p) {
               ^
3 errors
```

These are the three methods that went wrong in the first section, found by the compiler instead
of by finance. The chargeback window is now one new method with a switch, and no record changes.
With both done:

```text
Card[last4=4242] fee 320, 1 day(s), card ending 4242, chargeback 120 days
BankTransfer[iban=DE89-0000] fee 25, 3 day(s), transfer from DE89-0000, chargeback 0 days
Wallet[provider=PayCo] fee 150, 2 day(s), wallet PayCo, chargeback 0 days
```

A `default` branch would have switched the check off for that switch; unit 1.5.4 shows that, and
what a caller compiled against the old `permits` list gets at run time.

So the hierarchy is closed to new types and open to new operations, where the interface is the
reverse. Choose by who adds what. Types added by other teams or plug-ins need the interface:
they can't edit your switches. A set of types you own, with questions that keep coming, needs the
sealed type.

<!-- practice 2 -->

## A Closed Pipeline Still Runs Every Extension on Its Own Threads

The module's `ConcurrentPipeline` is closed code with an extension point: it takes any
`TaskProcessor<T, R>`, whose one method is `R process(T input)`, runs each item on a virtual
thread, and guards its result list with a lock. A
team plugs in a processor that counts the orders it sees:

```java
static final class CountingProcessor implements TaskProcessor<Integer, Integer> {
    long seen;
    public Integer process(Integer order) { seen++; return order; }
}

static final class AdderProcessor implements TaskProcessor<Integer, Integer> {
    final LongAdder seen = new LongAdder();
    public Integer process(Integer order) { seen.increment(); return order; }
}
```

Each runs through `new ConcurrentPipeline<>(processor).processAll(orders)` with 100,000 orders, three
times:

```text
orders 100000: plain field saw 89322, LongAdder saw 100000
orders 100000: plain field saw 93320, LongAdder saw 100000
orders 100000: plain field saw 88538, LongAdder saw 100000
```

The pipeline calls `process()` outside its lock, from many threads at once, so `seen++` loses
updates (unit 6.2.1). The pipeline itself is correct, but nothing in `R process(T input)` tells an
implementation that it is called concurrently. Closed code still shares its threads with every
extension, so the extension point's contract has to state them.

<!-- practice 3 -->

## Summary

- **A type check copied into several methods compiles when one copy is missed.** A fallback answer
  is silently wrong; a `throw` fails when the path first runs.
- **Behaviour on the type makes a new type one new class.** javac refuses it until every operation
  is written, and the callers' class files don't change.
- **An interface is closed to a new operation.** Every implementation is edited; one you don't own
  fails with `AbstractMethodError`, and a `default` method brings back the silent fallback.
- **A sealed type with exhaustive switches is the mirror image.** A new type is a compile error at
  every switch, and a new operation is one method. Pick the axis by who adds what.
- **An extension runs on the framework's threads.** The extension point's contract says whether
  calls are concurrent, and the implementation holds its state accordingly.

## Reference

What each layout makes javac do, from the runs above:

| Change | `instanceof` chain in each method | Interface, behaviour on the type | Sealed type, switch in each method |
|---|---|---|---|
| Add a type | edit every chain; javac silent; a missed copy falls back or throws | one new class; javac refuses it while a method is missing | `permits` and every switch; javac lists each switch without `default` |
| Add an operation | one new method | edit every implementation; javac lists them; an implementation compiled against the old interface throws `AbstractMethodError` when the method is called | one new method; javac checks it covers every type |
| Types written outside your code | accepted; they fall back or throw | yes | no: only the listed `permits` |

## Challenges

1. Your `PaymentMethod` interface is implemented by four partner jars. Disputes need
   `chargebackDays()`. Compare a `default` method, a separate `Disputable` interface checked with
   `instanceof`, and a lookup table in the disputes module. Which one fails loudest when a partner
   adds a new type?
2. A rules engine has 40 rule types and 3 operations; another codebase has 3 message types and 40
   operations. Pick a layout for each and count the files a typical change touches.
3. Rewrite `CountingProcessor` so that it is correct without `LongAdder`. What does each choice
   cost the pipeline's throughput?
4. Find a type switch in code you work on. What would have told you about the next case?

## Design Note: Closed to What

Every design in this unit is closed to one axis of change and open to the other. The chain is cheap to add operations to and unsafe to add types to; the
interface reverses that; the sealed type keeps the chain's layout and adds the compiler's check.
Making code "open for extension" before a second case exists picks an axis without evidence. If
the first change comes on the other axis, as the chargeback window did, it edits every
implementation of the abstraction instead of adding one method.

## Code Examples

- Source: [OpenClosed.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/OpenClosed.java)
- Test: [OpenClosedTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/OpenClosedTest.java)
