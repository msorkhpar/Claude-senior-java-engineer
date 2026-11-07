# 3.2.1. Creating Custom Exception Classes

At 03:10 the settlement alert fires. The log has a message line and two `at` lines:
`SettlementException: batch not settled`. Which batch? What did the database say? Meanwhile the
node that retries failed batches logs `java.io.NotSerializableException` instead of the failure
itself. Someone wrote `SettlementException` in five minutes, with one constructor and one field.

A custom exception is a short class, and each of its lines decides what the person on call can see.
The examples ran on Java 21.

## The Class You Extend Decides Who Must Handle It

Extend `Exception` and the exception is checked: every method that lets it escape must declare it,
or the code doesn't compile. Extend `RuntimeException` and it is unchecked: the compiler asks
nothing.

```java
class OrderRejectedException extends Exception {
    OrderRejectedException(String message) { super(message); }
}

class Checked {
    static void reject(String orderId) {
        throw new OrderRejectedException("order " + orderId + " rejected");
    }
}
```

```text
compile/Checked.java:7: error: unreported exception OrderRejectedException; must be caught or declared to be thrown
        throw new OrderRejectedException("order " + orderId + " rejected");
        ^
1 error
```

Which to choose is a policy question (unit 3.2.4). One choice is wrong
either way: extending `Throwable` (or `Error`) directly, to mark something as "more serious". A job
runner usually has a safety net that logs a failed job and moves on to the next:

```java
class QuotaExceededException extends RuntimeException {
    QuotaExceededException(String message) { super(message); }
}

class QuotaExceeded extends Throwable {                     // extends Throwable directly
    QuotaExceeded(String message) { super(message); }
}
```

```java
interface Job { void run() throws Throwable; }

static void runAll(List<Job> jobs) throws Throwable {
    for (int i = 0; i < jobs.size(); i++) {
        try {
            jobs.get(i).run();
            System.out.println("  job " + i + " done");
        } catch (Exception e) {                          // the runner's safety net
            System.out.println("  job " + i + " failed: " + e);
        }
    }
}
```

Three jobs, the second one over its quota; `main` catches whatever leaves `runAll` and prints
"runner ended by":

```text
extends RuntimeException:
  job 0 done
  job 1 failed: QuotaExceededException: tenant t-17 over quota
  job 2 done
extends Throwable:
  job 0 done
  runner ended by QuotaExceeded: tenant t-17 over quota
```

`QuotaExceeded` is not an `Exception`, so the safety net, usually written as `catch (Exception e)`,
never sees it, and job 2 never runs. `Error` is reserved for problems a program shouldn't try to
handle, such as `OutOfMemoryError`. A direct subclass of `Throwable` is also checked, so every
interface it passes through must declare it, as `Job` does here.

## A Wrapper Keeps the Original Failure Only If It Is Given the Cause

`Throwable` stores a detail message and a **cause**, the exception that led to this one.
`Exception` and `RuntimeException` each have four public constructors for them, and a custom
exception mirrors the ones its callers need:

```java
class SettlementException extends Exception {
    SettlementException(String message) { super(message); }
    SettlementException(String message, Throwable cause) { super(message, cause); }
    SettlementException(Throwable cause) { super(cause); }
}
```

The fourth, `()`, leaves both empty. Pause before the output: the ledger write fails with a
`SQLException`, and the settlement code catches it and throws a `SettlementException`. What does
the log show with each constructor?

```java
static void settle(long batch, int variant) throws SettlementException {
    try {
        writeLedger(batch);
    } catch (SQLException e) {
        switch (variant) {
            case 0 -> throw new SettlementException("batch " + batch + " not settled");
            case 1 -> throw new SettlementException("batch " + batch + " not settled", e);
            default -> throw new SettlementException(e);
        }
    }
}
```

```text
SettlementException: batch 42 not settled
	at Cause.settle(Cause.java:20)
	at Cause.main(Cause.java:30)

SettlementException: batch 42 not settled
	at Cause.settle(Cause.java:21)
	at Cause.main(Cause.java:30)
Caused by: java.sql.SQLException: deadlock detected
	at Cause.writeLedger(Cause.java:12)
	at Cause.settle(Cause.java:17)
	... 1 more

SettlementException: java.sql.SQLException: deadlock detected
	at Cause.settle(Cause.java:22)
	at Cause.main(Cause.java:30)
Caused by: java.sql.SQLException: deadlock detected
	at Cause.writeLedger(Cause.java:12)
	at Cause.settle(Cause.java:17)
	... 1 more
```

The first is the 03:10 log line. Without a cause, the deadlock, its SQL state and the frames that
led to it are gone, and nothing in the program can get them back. A class with only a message
constructor forces every caller into that shape, so `(message, cause)` is the constructor never to
leave out. The `(cause)` constructor keeps the chain but takes `cause.toString()` as its message,
and the batch number is lost. Unit 3.2.2 covers `throw` and rethrowing from a `catch`.

A class without one can still attach a cause once, with `initCause(cause)`, but every caller has
to remember to (see the Reference).

## Context Goes in Final Fields and in the Message

A handler that needs the order id should read it from a field, not parse it out of the message.
Two versions of a payment exception, both with an `orderId` field and a getter; the second also
puts its context in the message:

```java
class PaymentDeclinedException extends Exception {
    private final String orderId;

    PaymentDeclinedException(String orderId, Throwable cause) {
        super("payment declined", cause);
        this.orderId = orderId;
    }

    String getOrderId() { return orderId; }
}

class PaymentDeclinedWithIdException extends Exception {
    private final String orderId;
    private final String reason;

    PaymentDeclinedWithIdException(String orderId, String reason, Throwable cause) {
        super("payment declined for order " + orderId + ": " + reason, cause);
        this.orderId = orderId;
        this.reason = reason;
    }

    String getOrderId() { return orderId; }
    String getReason() { return reason; }
}
```

Logged as `"checkout failed: " + e`:

```text
checkout failed: PaymentDeclinedException: payment declined
checkout failed: PaymentDeclinedWithIdException: payment declined for order ord-5531: card expired
```

`toString()`, and with it the first line of a stack trace, prints the class name and the
message. Your fields never appear unless you put them there. So the context goes in twice:
in typed fields for code, and in the message for people. Build the message in the constructor and
pass it to `super` rather than keeping a message field of your own, so that `getMessage()`,
`toString()` and the stack trace agree. Keep the fields `final` with no setters: one exception
object is caught, logged and rethrown by several layers, and none of them should be able to change
what the next one reads.

<!-- practice 1 -->

## Every Exception Is Serializable, So Its Fields Must Be Too

`Throwable` implements `Serializable`, so every exception class is serializable whether you meant
it or not. Most exceptions are never serialized. The ones that are cross a JVM boundary through
Java serialization: RMI, and some caches and job frameworks that ship a task's failure back to the
caller. Here one exception holds the domain object, and a second holds only its id:

```java
class Order {                                   // a domain object, not Serializable
    final String id;
    Order(String id) { this.id = id; }
}

class OrderFailedException extends Exception {
    private static final long serialVersionUID = 1L;
    private final Order order;

    OrderFailedException(Order order, String message) {
        super(message);
        this.order = order;
    }
}

class OrderIdFailedException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String orderId;               // the id, not the object

    OrderIdFailedException(String orderId, String message) {
        super(message);
        this.orderId = orderId;
    }

    String getOrderId() { return orderId; }
}
```

Each is written to an `ObjectOutputStream` and read back:

```text
Exception is Serializable: true
Order field:   java.io.NotSerializableException: Order
orderId field: stock check failed, order ord-5531
```

The first failure never arrives: the code shipping it gets a `NotSerializableException` about
`Order` instead, which is what the retry node logged at 03:10. Marking the field `transient` silences the error, but a transient field isn't
written and reading doesn't run the constructor, so it arrives as `null`. Keep ids, codes and amounts in exceptions, not connections, sessions or entities. The
cause chain is serialized too, so a cause with such a field fails the same way.

The `serialVersionUID` matters on a rolling deploy, when the two sides run different releases.
Without it, the version is computed from the class's shape, so adding a field changes it. Here
release 1 of `StockException` writes one and release 2, with one more field, reads it, first
without a declared `serialVersionUID` and then with one:

```text
no serialVersionUID, release 1 writes, release 2 reads:
java.io.InvalidClassException: StockException; local class incompatible: stream classdesc serialVersionUID = 2416996496418282543, local class serialVersionUID = 999212421213874131
serialVersionUID = 1L, release 1 writes, release 2 reads:
read: StockException: out of stock: SKU-0091
```

With `private static final long serialVersionUID = 1L;` in both releases, the new field reads as its default and the failure arrives. `Throwable`'s own `serialVersionUID` covers
only `Throwable`; each subclass needs its own. `javac -Xlint:serial` warns about a missing one, and
about a field whose type isn't serializable.

<!-- practice 2 -->

## An Exception Class Can't Be Generic

A `RejectedException<T>` that carries the rejected value as a `T` looks natural. The compiler
refuses it:

```java
class RejectedException<T> extends RuntimeException {
    final T payload;
    RejectedException(T payload) { this.payload = payload; }
}
```

```text
compile/Generic.java:1: error: a generic class may not extend java.lang.Throwable
class RejectedException<T> extends RuntimeException {
                                   ^
1 error
```

A `catch` clause picks its handler by the exception's class at run time. After erasure,
`RejectedException<String>` and `RejectedException<Integer>` are the same class, so
`catch (RejectedException<String> e)` could never be checked. The rule also covers an inner
(non-static) class declared inside a generic class, which is implicitly generic too; a `static`
nested class compiles. Carry the value in a field of type `Object` or of a common supertype. A
record can't be an exception either, because it already extends `java.lang.Record`.

<!-- practice 4 -->

## Filling In the Stack Trace Is Most of the Cost; Skip It Only Where Nobody Reads the Trace

The stack trace is captured when the exception object is created: the constructor calls
`fillInStackTrace()`, which walks the thread's stack. Some code throws exceptions as ordinary
control flow, such as a rules engine where every rule that doesn't apply throws a miss and the
engine tries the next. There the walk is paid on every miss, and nobody reads the trace.

`Throwable` has a protected four-argument constructor, `(message, cause, enableSuppression,
writableStackTrace)`, which `Exception` and `RuntimeException` pass on. With
`writableStackTrace = false` no trace is captured. The older way overrides `fillInStackTrace()`:

```java
class NoMatchException extends RuntimeException {           // the usual kind
    NoMatchException(String message) { super(message); }
}

class CheapNoMatchException extends RuntimeException {      // no stack trace, no suppression
    CheapNoMatchException(String message) { super(message, null, false, false); }
}

class FillNoMatchException extends RuntimeException {       // the older way: skip the fill
    FillNoMatchException(String message) { super(message); }
    @Override public synchronized Throwable fillInStackTrace() { return this; }
}
```

Each was created, thrown and caught 200,000 times at three stack depths, reading only the message,
on one laptop. The times are per exception:

```text
 10 frames deep: with trace    493 ns   writableStackTrace=false   7 ns   fillInStackTrace overridden  14 ns
100 frames deep: with trace  2,124 ns   writableStackTrace=false   7 ns   fillInStackTrace overridden  14 ns
500 frames deep: with trace  8,687 ns   writableStackTrace=false   6 ns   fillInStackTrace overridden  14 ns
```

With a trace, each frame adds roughly 17 ns here, and a request handled inside a framework is
often hundreds of frames deep. Read the numbers as orders of
magnitude, not a benchmark: this is a loop timed with `System.nanoTime`, not JMH, and the cheap
versions likely owe part of their speed to the JIT compiling the throw and the catch into one
method.

What you give up shows in the log. The whole output of `printStackTrace()` for the cheap one:

```text
printStackTrace of the cheap one:
CheapNoMatchException: no rule matched
```

So keep a cheap exception inside the code that throws and catches it.
When the miss has to leave as a real failure, throw an ordinary exception with the miss as its
cause. And switch the trace off only after a profiler shows `fillInStackTrace` in a hot path.

<!-- practice 3 -->

## Summary

- **The superclass decides checked or unchecked.** Extending `Throwable` or `Error` directly
  escapes every `catch (Exception e)` safety net.
- **`(message, cause)` is the constructor never to leave out.** A wrapper without the cause loses
  the original exception and its frames for good.
- **Context goes in `final` fields and in the message.** Logs print the message, not the fields.
- **Every exception is serializable.** Keep its fields serializable, and declare a
  `serialVersionUID` so that the other release can still read it.
- **An exception class can't be generic**, nor can an inner class of a generic class or a record
  be an exception.
- **A stack trace costs time that grows with stack depth.** Switch it off only for exceptions that
  never leave the code that catches them, and only after profiling.

## Reference

**`Exception`'s constructors** (the same for `RuntimeException`, Java 21):

| Constructor | `getMessage()` | `getCause()` | `initCause(c)` afterwards |
|---|---|---|---|
| `()` | `null` | `null` | allowed, once |
| `(String message)` | `message` | `null` | allowed, once |
| `(String message, Throwable cause)` | `message` | `cause` | `IllegalStateException`, even when `cause` was `null` |
| `(Throwable cause)` | `cause.toString()`, or `null` when `cause` is `null` | `cause` | `IllegalStateException` |
| `protected (message, cause, enableSuppression, writableStackTrace)` | `message` | `cause` | `IllegalStateException` |

**A trace switched off** (Java 21):

| Call | `writableStackTrace = false` | `fillInStackTrace()` overridden |
|---|---|---|
| `getStackTrace()` | empty | empty |
| `setStackTrace(t)` | ignored | sets it |
| `addSuppressed(s)` | ignored when `enableSuppression` is `false` | records it |

**What the compiler refuses:**

| Declaration | Result |
|---|---|
| `class E<T> extends Exception` | error: a generic class may not extend `java.lang.Throwable` |
| an inner class extending `Exception` inside `class Batch<T>` | the same error; a `static` nested class compiles |
| `record E(String id) extends RuntimeException` | error: `'{' expected` (a record has no `extends` clause) |
| `catch (X e)` where `X` is a type parameter | error: unexpected type; `throws X` is allowed |

## Challenges

1. Your service's exceptions reach clients as JSON error bodies and reach the log. Which context
   belongs in the message, which only in fields, and which (a card number, an email address) in
   neither?
2. A library throws an exception that holds a JDBC `Connection` in a field, and your job framework
   serializes failures. Where do you translate it, and what do you keep?
3. Measure the stack depth at the point where one of your services throws its most common
   exception. Is the trace worth what it costs there?

## Design Note: Why the Trace Is Taken in the Constructor

The JVM records the trace when the exception is created, not when it is thrown, so the trace
points at the `new`, wherever the `throw` is (unit 3.2.2). So every exception
pays for its trace, whether anyone reads it or not, and a class that is created often pays most.
Java 7 added the four-argument constructor so that such a class can opt out without overriding
`fillInStackTrace()`. It is `protected`: the decision belongs to whoever writes the exception
class, who knows whether the exception will ever reach a log, and not to the code that throws it.
For almost every exception the default is the right choice, because a failure is rare and its
trace is what tells you where it happened.

## Code Examples

- Test: [CustomExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemoTest.java)
- Source: [CustomExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemo.java)
