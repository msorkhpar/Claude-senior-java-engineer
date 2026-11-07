# 7.2.3. Parameterized Enum Constants

Finance finds two weeks of Apple Pay payments booked with no fee. The payment methods are an enum,
and the fees live in a `switch` in the fee calculator:

```java
enum PaymentMethod { CARD, SEPA, PAYPAL, APPLE_PAY }

class FeeCalculator {
    static long fee(PaymentMethod method, long amountCents) {
        return switch (method) {
            case CARD -> 30 + amountCents * 290 / 10_000;
            case SEPA -> 35;
            case PAYPAL -> 49 + amountCents * 349 / 10_000;
            default -> 0;
        };
    }
}
```

```text
CARD fee on 120.00: 378
SEPA fee on 120.00: 35
PAYPAL fee on 120.00: 467
APPLE_PAY fee on 120.00: 0
```

Whoever added `APPLE_PAY` updated the checkout and the API mapping, and the compiler had nothing to
say about the `switch`: `default` covers every constant, including the ones nobody has written a
case for yet.

"Parameterized" here means constructor arguments and per-constant code, not type parameters. An
enum can't be generic (`enum Key<T>` fails with `error: enums cannot be generic`); unit 7.2.2 covers
that and the alternatives.

## A Constant's Data Goes Through the Constructor, So a New Constant Can't Leave It Out

Every constant is an object, built once by the enum's constructor when the enum class initialises.
The arguments in parentheses after the constant's name are passed to it:

```java
enum PaymentMethod {
    CARD("CRD", 30, 290),
    SEPA("SEP", 35, 0),
    PAYPAL("PPL", 49, 349),
    APPLE_PAY;

    private final String code;
    private final long fixedCents;
    private final int basisPoints;

    PaymentMethod(String code, long fixedCents, int basisPoints) {
        this.code = code;
        this.fixedCents = fixedCents;
        this.basisPoints = basisPoints;
    }

    long fee(long amountCents) {
        return fixedCents + amountCents * basisPoints / 10_000;
    }
}
```

```text
PaymentMethod.java:5: error: constructor PaymentMethod in enum PaymentMethod cannot be applied to given types;
    APPLE_PAY;
    ^
  required: String,long,int
  found:    no arguments
```

The zero fee is now a build failure: a constant can't exist without its fee. A `switch` without
`default` would catch it too (`the switch expression does not cover all possible input values`), but
only in that one `switch`. The next one written with `default`, or an `if` chain, is unchecked again.

The constructor is private whether you write `private` or not: `public` is refused, and so is
`new PaymentMethod(...)` anywhere, so the constants listed are the only instances there will ever be.

Keep the fields `final`. Each constant is one object shared by every caller in the JVM, so a field
written during one request is what the next request reads; unit 2.5.3 shows a tracking number
overwritten that way. Per-request data belongs in an object made for the request and passed to the
constant's method.

<!-- practice 1 -->

## An Abstract Method Makes Every Constant Write Its Own Rule

Then the invoice method arrives: 1.5 % of the amount, capped at 5.00. That is no longer
"fixed plus percent", and another field won't express it. The enum declares the method `abstract`,
and each constant supplies its own body:

```java
enum PaymentMethod {
    CARD("CRD", 30, 290) {
        long fee(long amountCents) { return fixedCents + percentOf(amountCents); }
    },
    SEPA("SEP", 35, 0) {
        long fee(long amountCents) { return fixedCents; }
    },
    INVOICE("INV", 0, 150) {
        long fee(long amountCents) { return Math.min(percentOf(amountCents), 500); }   // capped at 5.00
    };

    final String code;
    final long fixedCents;
    final int basisPoints;

    PaymentMethod(String code, long fixedCents, int basisPoints) {
        this.code = code;
        this.fixedCents = fixedCents;
        this.basisPoints = basisPoints;
    }

    abstract long fee(long amountCents);

    long percentOf(long amountCents) {
        return amountCents * basisPoints / 10_000;
    }
}
```

```text
CARD     fee on  120.00: 378
SEPA     fee on  120.00: 35
INVOICE  fee on  120.00: 180
CARD     fee on  900.00: 2640
SEPA     fee on  900.00: 35
INVOICE  fee on  900.00: 500
```

A constant added without a body doesn't compile: `PaymentMethod is abstract; cannot be
instantiated`. A method with a default body that some constants override gives that up: the new
constant inherits the default without a word: the `APPLE_PAY` bug again, inside the enum.

The fields are package-private on purpose. Make them `private`, and the bodies stop compiling. Here
are the SEPA body above and a CARD body rewritten to use `this.fixedCents`:

```text
Priv.java:3: error: non-static variable fixedCents cannot be referenced from a static context
        long fee(long amountCents) { return fixedCents; }
                                            ^
Priv.java:6: error: fixedCents has private access in PaymentMethod
        long fee(long amountCents) { return this.fixedCents + amountCents * 290 / 10_000; }
                                                ^
```

Why a static-context error for an instance field? Each body is an anonymous subclass of the enum
(unit 7.2.1), and a subclass doesn't inherit private members. So `fixedCents` resolves to the
enum's own field, and the body is declared where the constants are, in the enum's static
initialisation, with no enum instance to read the field from. Use package access or a non-private
accessor.

## A Lambda Argument Fits a Formula; a Body Fits a Rule That Needs the Constant

When every body is a one-line formula, the rule can be an ordinary constructor argument:

```java
import java.util.function.LongUnaryOperator;

enum PaymentMethod {
    CARD("CRD", percentPlusFixed(290, 30)),
    PAYPAL("PPL", percentPlusFixed(349, 49)),
    SEPA("SEP", amount -> 35),
    INVOICE("INV", amount -> Math.min(amount * 150 / 10_000, 500));

    private final String code;
    private final LongUnaryOperator feeRule;

    PaymentMethod(String code, LongUnaryOperator feeRule) {
        this.code = code;
        this.feeRule = feeRule;
    }

    long fee(long amountCents) {
        return feeRule.applyAsLong(amountCents);
    }

    static LongUnaryOperator percentPlusFixed(int basisPoints, long fixedCents) {
        return amount -> fixedCents + amount * basisPoints / 10_000;
    }
}
```

```text
CARD     fee on 900.00: 2640, class PaymentMethod
PAYPAL   fee on 900.00: 3190, class PaymentMethod
SEPA     fee on 900.00: 35, class PaymentMethod
INVOICE  fee on 900.00: 500, class PaymentMethod
```

The fees match, and `getClass()` is `PaymentMethod` for every constant, so there are no
subclasses. Because the rule is a value, a factory such as `percentPlusFixed` builds it, and CARD
and PAYPAL share its shape.

The lambda can't see the constant it configures. The arguments are evaluated before the constant
exists, in a static context:

```text
Bad.java:4: error: non-static variable code cannot be referenced from a static context
    CARD("CRD", amount -> amount * 290 / 10_000 + code.length());
                                                  ^
```

A body runs on the constant, so it can use `this`, its fields and the enum's other methods. Pick
the lambda when the rule is a formula over the method's arguments, and a body when the rule needs
the constant's own state or overrides more than one method.

## Callers Take an Interface the Enum Implements, So a Rule From Elsewhere Fits Too

Sales signs a merchant at 0.20 plus 1.8 % on cards. A constant per contract would mean a release per
contract, since an enum's constants are fixed when it compiles. An enum can implement an interface,
and code that only needs the fee takes the interface:

```java
interface FeeRule {
    long fee(long amountCents);
}

/** A merchant's negotiated card rate, read from its contract at run time. */
record NegotiatedRate(String merchant, long fixedCents, int basisPoints) implements FeeRule {
    @Override
    public long fee(long amountCents) {
        return fixedCents + amountCents * basisPoints / 10_000;
    }
}
```

The first section's `PaymentMethod` now reads `enum PaymentMethod implements FeeRule`, and its
`fee` becomes `public`, since an interface method is public and an override can't narrow it. The
payout takes the interface:

```java
static long payout(long amountCents, FeeRule rule) {
    return amountCents - rule.fee(amountCents);
}

System.out.println("CARD:        " + payout(90_000, PaymentMethod.CARD));
System.out.println("Acme's rate: " + payout(90_000, new NegotiatedRate("Acme", 20, 180)));
System.out.println("test rule:   " + payout(90_000, amount -> 0));
```

```text
CARD:        87360
Acme's rate: 88360
test rule:   90000
```

The enum still holds the standard rules, with `values()` and `EnumMap`.

The price is paid where code holds a `FeeRule`: anyone can implement it, so a `switch` over it
can't list every case and needs a catch-all (`default`, or `case FeeRule f`). A `switch` on
`PaymentMethod` itself stays exhaustive. When you want both, unit 2.5.3 shows a sealed interface
whose permitted subtypes can include an enum.

<!-- practice 2 -->

## Look a Constant Up by Its Code Through a Map Built After the Constants, and Let It Refuse Duplicates

Payment requests arrive with `"method": "PPL"`. `valueOf` matches constant names, not your codes:
`PaymentMethod.valueOf("PPL")` throws `IllegalArgumentException: No enum constant
PaymentMethod.PPL`. Build an index from the code field, once:

```java
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

enum PaymentMethod {
    CARD("CRD"), SEPA("SEP"), PAYPAL("PPL");

    private static final Map<String, PaymentMethod> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(m -> m.code, Function.identity()));

    private final String code;

    PaymentMethod(String code) { this.code = code; }

    static Optional<PaymentMethod> fromCode(String code) {
        return Optional.ofNullable(BY_CODE.get(code));
    }
}
```

```text
fromCode("PPL"): Optional[PAYPAL]
fromCode("APL"): Optional.empty
```

Static fields are initialised in source order, and the constants come first. So `BY_CODE` is
built after every constant exists, and `values()` lists them all, in declaration order.

Registering each constant from its constructor is too early: `BY_CODE` is still `null` while the
constructors run. javac refuses a direct reference, and a call through a static method compiles
and fails at class initialisation (unit 7.2.1 shows both). A map in a nested holder class can be
filled from the constructor, because the holder initialises on first use. It then needs its own
duplicate check.

`Optional` leaves an unknown code to the caller. Mapping it to a catch-all constant books a payment
nobody recognised as an ordinary one.

The collector also checks the codes. Add a `CARD_PRESENT("CRD")` after the other constants and
forget to change the code:

```text
java.lang.ExceptionInInitializerError
  caused by java.lang.IllegalStateException: Duplicate key CRD (attempted merging values CARD and CARD_PRESENT)
```

The service fails on its first use of the enum, in a test or at startup; every later use gets
`NoClassDefFoundError: Could not initialize class`, which is often the only line in the logs. A
`HashMap` filled with
`put` would keep the last constant for `"CRD"` and send every card payment to `CARD_PRESENT`.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **A constant's data goes through the constructor**, so a new constant without it doesn't
  compile, where a `switch` with `default` gives it a silent zero. Keep the fields final: a constant
  is shared by every caller.
- **An abstract method makes every constant write its own rule**: a constant without a body
  doesn't compile, where a default method lets it inherit silently. A body can't see the enum's
  private fields; give them package access or use an accessor.
- **A lambda argument fits a formula**, can come from a factory and adds no subclass, but it can't
  see the constant, which a body can.
- **Callers take an interface the enum implements**, so negotiated rules and test stubs fit, at the
  cost of the exhaustive `switch`.
- **Look up by code through a map built after the constants.** `valueOf` knows only names, and
  the map is still `null` while constructors run. A collector refuses duplicate codes at startup,
  where `put` keeps the last one.

## Reference

**What javac refuses in an enum (Java 21):**

| Code | Error |
|---|---|
| `enum Key<T> { ... }` | `enums cannot be generic` |
| a constant without the constructor's arguments | `constructor X in enum X cannot be applied to given types` |
| a `public` constructor | `modifier public not allowed here` |
| `new X(...)` | `enum classes may not be instantiated` |
| a constant without a body when a method is abstract | `X is abstract; cannot be instantiated` |
| a body reading a `private` field by name | `non-static variable f cannot be referenced from a static context` |
| a lambda argument reading the constant's field | the same |
| a constructor reading the enum's own non-constant static field | `illegal reference to static field from initializer` |

**Finding a constant:**

| By | Use | Unknown input |
|---|---|---|
| name | `X.valueOf(name)` | `IllegalArgumentException` |
| a field | a static map built from `values()` | what the lookup returns, e.g. `Optional.empty()` |
| position | `values()[i]` (a new array on every call) | `ArrayIndexOutOfBoundsException` |

## Challenges

1. Your enum has 40 constants and three of them need a different fee shape. An abstract method with
   40 bodies, a default with 3 overrides, or a lambda argument? What does each cost when the 41st
   constant is added?
2. The fee rules move to a database so finance can change them without a release. What stays in the
   enum, and what does `PaymentMethod` become?
3. Two services share the `PaymentMethod` enum through a library, and one deploys a new constant
   first. What does the other do when it reads `"APL"`, and where would you decide that?

## Design Note: Data on the Constant

An enum with fields turns a list of names into a table the compiler checks: every row has every
column, and there are no rows except the ones written. That is what fixed the zero fee. The table
is closed, though. A value that appears at run time (a contract, a tenant, a rule from a database)
can't be a constant, and adding one is a release of every service that shares the enum. Put on the
constant what changes only when the code changes, and let an interface carry the rest.

## Code Examples

- Implementation: [ParameterizedEnumConstants.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/ParameterizedEnumConstants.java)
- Tests: [ParameterizedEnumConstantsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/ParameterizedEnumConstantsTest.java)
