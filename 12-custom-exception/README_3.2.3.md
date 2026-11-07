# 3.2.3. Best Practices for Custom Exceptions

One payment fails at 14:02, and four things go wrong around it. The mobile app shows "Something
went wrong" instead of "Top up your account". The API answers 500, which tells the client the
server is broken. On-call gets three SEVERE alerts for one failure. And one log line holds the
customer's email and full card number.

None of these is about writing the exception class (unit 3.2.1) or throwing it (unit 3.2.2). They
are about how the module's exceptions are shaped and what the rest of the system does with them.
The examples run on Java 21.

## Give a Module One Base Exception, So Each Caller Chooses How Finely It Catches

Take the module's `BankAccount`, with `InvalidAmountException` and `InsufficientFundsException`
made unchecked: two unrelated subclasses of `RuntimeException`. An ATM was written against it:

```java
static String dispense(BankAccount account, double amount) {
    try {
        account.withdraw(amount);
        return "dispensed " + amount;
    } catch (InsufficientFundsException e) {
        return "offer a smaller amount";
    } catch (InvalidAmountException e) {
        return "rejected: " + e.getMessage();
    }
}
```

The next release adds `AccountFrozenException`, and a customer whose account is frozen asks for 200.
Pause here: what does the ATM do?

```text
Exception in thread "main" AccountFrozenException: account is frozen
	at BankAccount.withdraw(Atm.java:18)
	at Atm.dispense(Atm.java:28)
	at Atm.main(Atm.java:40)
```

The code compiled, and the new exception went past both catch blocks. In a web service, that is a
500 for an answer the module gave on purpose. Had the exceptions been checked and listed in
`throws`, the ATM would have stopped compiling instead (unit 3.2.4 shows that error).

The fix is one base class for the module, `AccountException`, which every specific exception, a
leaf, extends. A method that throws checked leaves declares `throws AccountException` and documents
each leaf with a `@throws` tag. The ATM catches the one leaf it treats differently, and the base for
the rest:

```java
} catch (InsufficientFundsException e) {
    return "offer a smaller amount";
} catch (AccountException e) {
    return "rejected: " + e.getMessage();
}
```

The ATM needs no change for the next release. Run against three accounts:

```text
dispensed 200.0
offer a smaller amount
rejected: account is frozen
```

A nightly batch can catch only `AccountException`, while the ATM catches one leaf and the base:
each caller picks its granularity.

The base also gives callers something narrower to catch than `Exception`. A caller that catches
`Exception` or `RuntimeException` to be safe also catches the `NullPointerException` from a bug,
and answers it with "rejected".

<!-- practice 1 -->

## Throw IllegalArgumentException, IllegalStateException or UnsupportedOperationException When They Already Mean the Error

A cinema booking service is tempted to add `SeatNotFoundException`. Its web layer already has a
handler, written long before the booking code:

```java
static int status(RuntimeException e) {
    if (e instanceof IllegalArgumentException) return 400;     // the caller sent something wrong
    if (e instanceof IllegalStateException) return 409;        // fine request, wrong moment
    if (e instanceof UnsupportedOperationException) return 405;
    return 500;                                                // a bug: page someone
}
```

Six failures go through it:

```text
seat 0, own RuntimeException       500  SeatNotFoundException
seat 0, own IAE subclass           400  SeatOutOfRangeException
seat "12a", Integer.parseInt       400  NumberFormatException
booking closed, ISE                409  IllegalStateException
booking closed, IAE                400  IllegalArgumentException
add to read-only seat list         405  UnsupportedOperationException
```

The new class turned a bad seat number into a 500 that pages on-call. The three standard
exceptions have a meaning every Java developer shares, so handlers like this one already map them:

- `IllegalArgumentException`: the argument is wrong whatever the object's state. Seat 0 never
  exists. The JDK's `NumberFormatException` is a subclass.
- `IllegalStateException`: the argument is fine, but not now. Seat 4 exists, the booking is
  closed, and the same request may succeed later.
- `UnsupportedOperationException`: this object never does that, like `add` on an unmodifiable
  list.

A closed booking reported as `IllegalArgumentException` answers 400, which tells the client to change a request that was right.

When you need your own type, to carry a field or to let one caller tell it apart, extend the
standard exception that has its meaning: `SeatOutOfRangeException extends IllegalArgumentException`
still gets its 400. Start a new branch, under the module's base, only for an error that belongs to
your domain and that no standard exception describes, such as insufficient funds.

<!-- practice 2 -->

## A Message Names the Bad Value and the Range It Expected, and Nothing Secret

A charge of 25 cents is rejected because the minimum is 50. `r` is a `ChargeRequest` record, and
three versions of the check differ only in the message:

```java
throw new IllegalArgumentException("Invalid amount");
throw new IllegalArgumentException("Invalid amount in " + r);
throw new IllegalArgumentException("amountCents " + r.amountCents()
        + " outside [" + MIN + ", " + MAX + "] in charge " + r.id());
```

The three messages:

```text
Invalid amount
Invalid amount in ChargeRequest[id=ch_7Q2, email=jane@example.com, cardNumber=4111111111111111, amountCents=25]
amountCents 25 outside [50, 1000000] in charge ch_7Q2
```

From the first, the engineer reading the log can't tell a client bug from a limit set too high.
The stack trace shows where it failed, not the value. The second tried to add context and
added the whole request. A record's `toString()` prints every component, so the email and card
number are now everywhere the message goes: the log, the log aggregator, the ticket someone pastes
it into, and any error page that echoes it. Removing them means finding every copy.

The third gives the value, the range with its bounds, and an identifier that leads to the rest.
`Objects.checkIndex(12, 10)` gives the value and the bound too:

```text
Index 12 out of bounds for length 10
```

Write the range the way the code checks it, with each bound marked inclusive or exclusive: the
reader of the log can't see the code. An identifier such as `ch_7Q2`, or a card's last four digits,
points at the data without copying it. A password, a token, a full card number or personal data
such as an email never goes in a message.

<!-- practice 3 -->

## Log an Exception Where It Is Handled, Never Where It Is Rethrown

An order lookup has three layers: repository, service and controller. The lower two catch the
failure, log it with `java.util.logging` and rethrow it, the service after wrapping it in
`OrderLookupException` with the cause. The controller logs it and answers 503. The repository's
catch:

```java
} catch (SQLException e) {
    LOG.log(Level.SEVERE, "query failed for order " + id, e);
    throw e;
}
```

One `SQLException` produces this log:

```text
SEVERE: query failed for order 8812
java.sql.SQLException: connection reset
	at LogTwice.loadRow(LogTwice.java:15)
	at LogTwice.findOrder(LogTwice.java:24)
	at LogTwice.getOrder(LogTwice.java:34)
	at LogTwice.main(LogTwice.java:67)
SEVERE: lookup failed
LogTwice$OrderLookupException: could not load order 8812
	at LogTwice.findOrder(LogTwice.java:26)
	at LogTwice.getOrder(LogTwice.java:34)
	at LogTwice.main(LogTwice.java:67)
Caused by: java.sql.SQLException: connection reset
	at LogTwice.loadRow(LogTwice.java:15)
	at LogTwice.findOrder(LogTwice.java:24)
	... 2 more
SEVERE: GET /orders/8812 failed
LogTwice$OrderLookupException: could not load order 8812
	at LogTwice.findOrder(LogTwice.java:26)
	at LogTwice.getOrder(LogTwice.java:34)
	at LogTwice.main(LogTwice.java:67)
Caused by: java.sql.SQLException: connection reset
	at LogTwice.loadRow(LogTwice.java:15)
	at LogTwice.findOrder(LogTwice.java:24)
	... 2 more
```

Three SEVERE records and 24 lines for one failure, with `connection reset` three times. An alert
on SEVERE fires three times, and whoever reads the log looks for three incidents. The last record
holds it all: the wrapper keeps the original as its cause, and `Caused by` prints it.

When only the controller logs, the log is that last record alone, 9 lines. A layer with something to add puts it in the exception it throws, as the service's "could
not load order 8812" does. The layer that decides what happens next (answers 503, retries, skips
the record) logs, once, and passes the exception to `log(level, message, e)` so the stack trace is
printed.

<!-- practice 4 -->

## Clients Branch on an Error Code, Never on the Message

An API client needs to tell "insufficient funds" from "card expired". A leaf class pays off when
a caller in the code catches it differently; for a client that only branches on the outcome, the
payments module gives its base exception a code:

```java
enum ErrorCode { CARD_DECLINED, INSUFFICIENT_FUNDS, CARD_EXPIRED }

class PaymentException extends RuntimeException {
    private final ErrorCode code;
    PaymentException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }
    ErrorCode code() { return code; }
}
```

The web layer maps the code, and only the code, to the response:

```java
static ApiError toResponse(PaymentException e) {
    int status = switch (e.code()) {
        case CARD_DECLINED, CARD_EXPIRED -> 402;
        case INSUFFICIENT_FUNDS -> 422;
    };
    return new ApiError(status, e.code().name());
}
```

A later release rewords the message from `balance 1200 < amount 4999 on account acc-31` to
`amount 4999 exceeds available balance 1200 (account acc-31)`. The response, and a client that
matched on the message text, before and after:

```text
ApiError[status=422, code=INSUFFICIENT_FUNDS]
  client matching the message: shows 'top up your account'
ApiError[status=422, code=INSUFFICIENT_FUNDS]
  client matching the message: shows 'something went wrong'
```

The message is written for the engineer reading the log, and it changes whenever someone improves
it. The code is part of the API: the documentation lists it, and an existing code keeps its meaning,
so renaming or removing one needs a new API version. New codes do appear, so a client needs a branch
for a code it doesn't know yet. The response carries the code and not the message, which also keeps
the values from the previous section away from clients.

The `switch` has no `default`. Add `ACCOUNT_FROZEN` to the enum and the mapping stops compiling:

```text
v2/ErrorCodes.java:19: error: the switch expression does not cover all possible input values
        int status = switch (e.code()) {
                     ^
1 error
```

So a person decides the frozen account's status, instead of a fallback to 500. A mapping built
from an `instanceof` chain, or a `switch` with a `default`, gives no such error: with
`default -> 500` added, the same change compiles, and a frozen account gets the 500.

<!-- practice 5 -->

## Summary

- **One base exception per module** lets each caller catch a leaf it treats specially and the
  base for the rest, so a new leaf needs no change in callers. A new unchecked leaf that callers
  catch only by name escapes them at run time.
- **Reuse `IllegalArgumentException`, `IllegalStateException` and
  `UnsupportedOperationException`** when they mean the error, or extend them. Existing handlers
  already map them; a home-grown `RuntimeException` falls through to their fallback.
- **A message gives the bad value, the expected range and an identifier**, never a whole request
  object, a password, a token, a card number or an email.
- **Log where the exception is handled.** Logging and rethrowing prints the same stack trace once
  per layer.
- **Clients branch on an error code.** An enum field mapped in a `switch` with no `default`
  survives rewording, and turns a new code into a compile error.

## Reference

**The JDK's standard exceptions (from their javadoc):**

| Exception | Thrown when |
|---|---|
| `IllegalArgumentException` | a method has been passed an illegal or inappropriate argument |
| `IllegalStateException` | a method has been invoked at an illegal or inappropriate time |
| `UnsupportedOperationException` | the requested operation is not supported |
| `NullPointerException` | `null` is used where an object is required (`Objects.requireNonNull`) |
| `IndexOutOfBoundsException` | an index is out of range (`Objects.checkIndex`) |
| `NoSuchElementException` | the element being requested does not exist (`Iterator.next`) |

**What a change in the module does to an existing caller (runs on this page, Java 21; unit 3.2.4 covers a checked exception added to `throws`):**

| Change | Effect |
|---|---|
| unchecked leaf added; the caller catches leaves by name | the new leaf escapes the handler |
| leaf added under a base the caller catches | handled by the base's `catch` |
| enum constant added; mapped by a `switch` expression with no `default` | the mapping doesn't compile |
| message reworded | the code is unchanged; text matching breaks |

## Challenges

1. Your module's base exception is checked, and a team asks for it to be unchecked so they can call
   the module from a stream. What do callers lose and gain, and how would you migrate them? (Unit
   3.2.4 helps.)
2. A third-party client library throws only `RuntimeException`, with different messages. Design
   the adapter that gives your callers a base exception and error codes.
3. Find a log in your system where one failure produces more than one stack trace. Which layer
   keeps the log call, and what does each other layer add to the exception instead?

## Design Note: Declare the Base, Document the Leaves

A `throws` clause is part of a method's contract, and callers compile against whatever it names.
Naming each leaf gives the most precise contract, and turns every new failure into a change every
caller must make. Naming the base gives up that precision for room to grow: the leaves move to
`@throws` tags and to error codes, where adding one breaks no build but the one
`switch` meant to catch it. The cost
is that the compiler no longer tells a caller about a new leaf, and the base's handler takes it
without anyone deciding. The `switch` over the codes puts one compile-time check back, in the one
place that maps failures to responses.

## Code Examples

- Test: [BankAccountTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/BankAccountTest.java)
- Source: [BankAccount.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/BankAccount.java)
