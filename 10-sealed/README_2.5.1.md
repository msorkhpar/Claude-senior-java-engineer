# 2.5.1 Defining Sealed Classes and Interfaces

The payments library exports an interface `Payment` and two records, `Card` and `Transfer`. The
ledger books a payment through an `instanceof` chain whose last branch throws `unknown payment`. A
partner team implemented `Payment` for its gift cards, and at month end 312 gift-card payments sat
in the dead-letter queue. Sealed types (Java 17) let a type's owner name its direct subtypes. Outputs are
from Java 21.

## A Sealed Type Names Its Direct Subtypes, and javac Refuses Any Other

```java
public sealed interface Payment permits Card, Transfer {}
record Card(String last4, long cents) implements Payment {}
record Transfer(String iban, long cents) implements Payment {}
```

The partner's record, in its own file, no longer compiles:

```java
public record GiftCard(String code, long cents) implements Payment {}
```

```text
GiftCard.java:1: error: class is not allowed to extend sealed class: Payment (as it is not listed in its 'permits' clause)
public record GiftCard(String code, long cents) implements Payment {}
       ^
```

`sealed` works on a class or an interface, and `permits` lists every direct subtype. A record is
implicitly `final`, so it qualifies as it is; a class needs `final`, `sealed` or `non-sealed`, and
every subtype must sit in the sealed type's module or package (unit 2.5.2).

An anonymous class is refused too, and so is a lambda: a sealed interface isn't a functional
interface.

### The JVM Checks the List When It Loads a Subtype

javac stores the list in the class file. We compiled `GiftCard` against the unsealed `Payment`,
then ran it with the sealed library:

```text
Exception in thread "main" java.lang.IncompatibleClassChangeError: class GiftCard cannot implement sealed interface Payment
```

Sealing a published type is a breaking change: the partner's build fails, and an upgrade without
a rebuild fails when the class loads.

## Without permits, the Subtypes in the Same File Are the List

```java
public sealed interface Payment {
    record Card(String last4, long cents) implements Payment {}
    record Transfer(String iban, long cents) implements Payment {}
    record TestPayment(long cents) implements Payment {}   // added for a demo, months later
}
```

`Payment.class.getPermittedSubclasses()` returns:

```text
[class Payment$Card, class Payment$Transfer, class Payment$TestPayment]
```

javac takes every direct subtype declared in the same source file; a `Voucher` in
another file of the same package gets the `not listed in its 'permits' clause` error.

Without `permits`, whatever someone adds to the file joins the closed set, and no review sees a
list change. The demo's `TestPayment` is now a payment that every exhaustive switch (unit 1.5.4)
must handle. Write `permits` on a type other code depends on; leave it out for a small hierarchy
nested in one file.

## At Run Time, getPermittedSubclasses() Lists the Direct Subtypes Only

```java
sealed interface Payment permits Card, BankTransfer {}
record Card(String last4, long cents) implements Payment {}
sealed interface BankTransfer extends Payment permits Sepa, Wire {}
record Sepa(String iban, long cents) implements BankTransfer {}
record Wire(String swift, long cents) implements BankTransfer {}
```

Printing `isSealed()` and the simple names of what `getPermittedSubclasses()` returns:

```text
Payment: isSealed() = true, getPermittedSubclasses() = [Card, BankTransfer]
BankTransfer: isSealed() = true, getPermittedSubclasses() = [Sepa, Wire]
Card: isSealed() = false, getPermittedSubclasses() = null
```

`Sepa` and `Wire` don't appear under `Payment`. Code that needs every concrete type, a decoder or
a test that each type has a handler, walks down through each sealed subtype. For a type that
isn't sealed the method returns `null`, not an empty array, and the javadoc leaves the order of
the array unspecified.

<!-- practice 1 -->

<!-- practice 2 -->

## Summary

- **`sealed ... permits` names the direct subtypes, and javac refuses any other**, anonymous
  classes and lambdas included. Records qualify as they are.
- **The JVM checks the list again when it loads a subtype**, so an implementation compiled before
  the sealing fails with `IncompatibleClassChangeError`.
- **Without `permits`, the list is every direct subtype in the same file**, never the package.
- **`getPermittedSubclasses()` returns the direct subtypes only**, and `null` for a type that
  isn't sealed.

## Reference

**More javac 21 errors:**

| Code | Error |
|---|---|
| `new Payment() {}` | `anonymous classes must not extend sealed classes` |
| a lambda for a sealed interface `Rule` with one abstract method | `incompatible types: Rule is not a functional interface` |
| `permits Wallet`, where `Wallet` doesn't implement it | `invalid permits clause` (`subclass Wallet must extend sealed class`) |
| a permitted class without `final`, `sealed` or `non-sealed` | `sealed, non-sealed or final modifiers expected` |
| no `permits` and no subtype in the file | `sealed class must have subclasses` |

## Challenges

1. Four services you don't own implement your library's `Event`. What would you release, and in
   what order, to seal it?
2. Forty tests stub `Payment` with anonymous classes. After sealing, what replaces them?
3. Write a test that fails when a permitted subtype of `Payment` has no handler in the ledger.
   Which part could the compiler check instead?

## Design Note: Why the List Lives in the Type

Before Java 17 a library closed a hierarchy by hiding it: a package-private constructor stops
other packages from extending an abstract class, but an interface has no constructor, and nobody
could read off which subtypes existed. Sealing makes the list part of the type, read by javac for
exhaustive switches, by the JVM to refuse a stranger, and by tools through reflection. The cost is
that the list becomes public API: adding a subtype breaks the build of every exhaustive switch without a `default`, and removing
one breaks the clients that name it.

## Code Examples

- Test: [SealedClassesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/sealedclasses/SealedClassesTest.java)
- Source: [Shape.java](src/main/java/com/github/msorkhpar/claudejavatutor/sealedclasses/Shape.java)
