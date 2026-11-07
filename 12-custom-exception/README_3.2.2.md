# 3.2.2. Throwing Exceptions Using the `throw` Keyword

A customer sends 250.00 from account `A-1` to `B-2x`, a typo for `B-2`. The transfer API answers
`500 Internal Server Error`, and `A-1` is 250.00 lighter. The same day, a request with a negative
amount succeeds and moves money from the receiver to the sender.

Outputs are from Java 21, with classes compiled with `-g` so that exception messages name local
variables.

## Throw Where the Bad Value Arrives, Before Anything Changes

Two versions of the transfer, with amounts in cents:

```java
static void transferLate(String fromId, String toId, long cents) {
    Account from = ACCOUNTS.get(fromId);
    Account to = ACCOUNTS.get(toId);
    from.withdraw(cents);
    to.deposit(cents);
}

static void transferEarly(String fromId, String toId, long cents) {
    Account from = find(fromId);
    Account to = find(toId);
    if (cents <= 0) throw new IllegalArgumentException("amount must be positive: " + cents);
    from.withdraw(cents);
    to.deposit(cents);
}

static Account find(String id) {
    Objects.requireNonNull(id, "account id");
    Account a = ACCOUNTS.get(id);
    if (a == null) throw new IllegalArgumentException("unknown account: " + id);
    return a;
}
```

Each call starts from `A-1 = 100000` and `B-2 = 0`:

```text
late,  to=B-2x: java.lang.NullPointerException: Cannot invoke "Transfer$Account.deposit(long)" because "to" is null
    A-1 = 75000, B-2 = 0
early, to=B-2x: java.lang.IllegalArgumentException: unknown account: B-2x
    A-1 = 100000, B-2 = 0
early, to=null: java.lang.NullPointerException: account id
    A-1 = 100000, B-2 = 0
late,  amount=-500: ok
    A-1 = 100500, B-2 = -500
early, amount=-500: java.lang.IllegalArgumentException: amount must be positive: -500
    A-1 = 100000, B-2 = 0
```

The late version did fail on the unknown account, with a `NullPointerException` that names the
null variable (the default since Java 15). But it failed after the withdrawal, so the money was
already gone. The negative amount failed nowhere, because
`withdraw` and `deposit` accept a negative number; the transfer simply ran backwards.

A `throw` stops execution at that line, and control goes to the nearest enclosing matching
`catch`, in this method or a caller. Whatever ran before it stays done. So a method checks its arguments and preconditions before its
first change of state, and throws there. A `throw` placed after a change has to undo it, which a
payment, an email or a row in another system often can't.

The early version also says what was wrong and with which value, which the caller can turn into a
`400` instead of a `500`. `Objects.requireNonNull(id, "account id")` is the short form of the null
check: if `id` is null, it throws a `NullPointerException` with the message you give it.

<!-- practice 1 -->

## The Compiler Knows a `throw` Ends the Block; It Doesn't Know Your Helper Always Throws

The operand of `throw` must be a `Throwable`: `throw "negative quantity";` fails with
`incompatible types: String cannot be converted to Throwable`. In return, the compiler knows that
a `throw` never completes normally. A statement right after it in the same block is an
`unreachable statement` error, and a `switch` expression arm can be
`default -> throw new IllegalArgumentException("unknown level: " + s);` with no value.

That knowledge stops at the method boundary. A catalog moves its "unknown SKU" throw into a helper:

```java
static void fail(String sku) {
    throw new IllegalArgumentException("unknown SKU: " + sku);
}

static long price(String sku) {
    Long p = PRICES.get(sku);
    if (p != null) return p;
    fail(sku);
}
```

```text
compile-errors/HelperFail.java:14: error: missing return statement
```

The compiler checks `price` against `fail`'s signature, `void`, not against its body. The usual
patch, `return 0;` after the call, ships a price of zero the day someone changes `fail` to log
instead of throw. Make the helper return the exception, and keep the `throw` at the call site:

```java
static IllegalArgumentException unknownSku(String sku) {
    return new IllegalArgumentException("unknown SKU: " + sku);
}

static long price(String sku) {
    Long p = PRICES.get(sku);
    if (p != null) return p;
    throw unknownSku(sku);
}
```

```text
java.lang.IllegalArgumentException: unknown SKU: BOOK-17
	at Prices.unknownSku(Prices.java:7)
	at Prices.price(Prices.java:13)
	at Prices.main(Prices.java:18)
```

The first frame is the factory, because the trace is recorded when the exception is constructed,
not when it is thrown (unit 3.1.1 shows what that does to a shared, pre-built instance). The
frame below it is the line that threw.

The operand is evaluated at run time, so it can be `null`. A class keeps a failure it found in a
field and, later in `main`, throws it:

```java
static RuntimeException pending;               // set only when a check failed

throw pending;
```

```text
java.lang.NullPointerException: Cannot throw exception because "ThrowNull.pending" is null
```

When no check failed, the caller gets an exception about the `throw` itself, not about any failure.

## A Checked Exception Can't Leave a `Function`: Wrap It in the Lambda, Unwrap It at Your Boundary

A `throws` clause lists the checked exceptions a method may let escape, and the compiler checks
every call against it. `Function.apply` declares none, so this does not compile, whatever the
enclosing method declares:

```java
return files.stream().map(f -> Files.readString(f)).toList();
```

```text
compile-errors/LambdaChecked.java:7: error: unreported exception IOException; must be caught or declared to be thrown
```

The lambda is the body of `apply`, not of the method that contains it. The usual way out is to
catch the `IOException` in the lambda and throw an `UncheckedIOException` around it. Then a caller
of the method that writes `catch (IOException e)` no longer compiles,

```text
compile-errors/CatchNeverThrown.java:21: error: exception IOException is never thrown in body of corresponding try statement
```

so it catches `RuntimeException` broadly, or nothing. Keep the wrapper inside the method that made
it, and give the caller back the checked type:

```java
static List<String> loadAll(List<Path> files) throws IOException {
    try {
        return files.stream().map(f -> {
            try {
                return Files.readString(f);
            } catch (IOException e) {
                throw new UncheckedIOException(e);       // the only way out of Function.apply
            }
        }).toList();
    } catch (UncheckedIOException e) {
        throw e.getCause();                             // back to the checked type at our boundary
    }
}
```

```text
caught IOException: java.nio.file.NoSuchFileException: reports/q3.txt
lazy, escaped as: java.io.UncheckedIOException: java.nio.file.NoSuchFileException: reports/q4.txt
```

The caller of `loadAll` catches the original `NoSuchFileException` by type. The second line comes from
the same method returning the `Stream` without `toList()`: the lambda ran only in the caller's
`forEach`, outside the `try`, and the wrapper escaped. For your own APIs, a functional interface that
declares the exception, such as `R apply(T t) throws IOException`, needs no wrapper.

<!-- practice 2 -->

## A Sneaky Throw Compiles, and Leaves Callers Unable to Name What It Throws

Some libraries skip the wrapper with a generic method:

```java
@SuppressWarnings("unchecked")
static <E extends Throwable> RuntimeException sneaky(Throwable t) throws E {
    throw (E) t;                                         // the cast is erased: no check happens
}
```

Called as `throw sneaky(e);` inside the lambda, it compiles with no `throws` anywhere. When a
thrown type variable has nothing else to go on, Java infers `RuntimeException` for it. So the call
declares nothing checked, and the erased cast lets the `IOException` itself out of the lambda. The
caller tries `catch (RuntimeException e)`, then `catch (Exception e)`; then the same loader is
called through a `java.lang.reflect.Proxy` of `Supplier`, the way an interface-based proxy calls it:

```text
catch (Exception):        java.nio.file.NoSuchFileException: reports/q3.txt
through a proxy:          java.lang.reflect.UndeclaredThrowableException, cause: java.nio.file.NoSuchFileException: reports/q4.txt
```

The `IOException` arrives where no signature says it can, and `catch (RuntimeException e)` let it
pass. A caller that writes `catch (IOException e)` gets the same `never thrown` compile error as
above, whatever its own method declares, so it must catch `Exception` and test the type. A dynamic proxy wraps a checked exception
its interface doesn't declare in an `UndeclaredThrowableException`, so behind a proxy the real
failure is a cause one level down.

## Translate Exceptions at a Layer Boundary, and Keep the Cause

The account service reads from a JDBC store whose methods throw `SQLException`. Its callers, the
web layer here, should not need to know about JDBC. The service translates at its boundary: a
missing row becomes `UnknownAccountException`, and a failing store becomes
`AccountsUnavailableException`, with the `SQLException` as its cause.

```java
static Account find(AccountStore store, String id) {
    Account a;
    try {
        a = store.load(id);
    } catch (SQLException e) {
        throw new AccountsUnavailableException("cannot load account " + id, e);
    }
    if (a == null) throw new UnknownAccountException(id);
    return a;
}
```

A shorter version puts both lines in one `try` and catches `Exception`:

```java
static Account findBroad(AccountStore store, String id) {
    try {
        Account a = store.load(id);
        if (a == null) throw new UnknownAccountException(id);
        return a;
    } catch (Exception e) {
        throw new AccountsUnavailableException("cannot load account " + id, e);
    }
}
```

The web layer maps `UnknownAccountException` to `404` and `AccountsUnavailableException` to `503`:

```text
narrow  A-1, store up:   200 100000
narrow  Z-9, store up:   404 unknown account: Z-9
narrow  A-1, store down: 503 retry later
broad   A-1, store up:   200 100000
broad   Z-9, store up:   503 retry later
broad   A-1, store down: 503 retry later
```

The broad catch translated the service's own `UnknownAccountException` too. A client asking for
an account that doesn't exist is told to retry, and it does, against a store that is fine.
Catch only the lower layer's own failure type, `SQLException` here, in a `try` that holds only
the call to it. An unchecked exception from the store is usually a bug, and should reach the caller as one.

The cause keeps what the web layer must not see but on-call needs. The log entry for the `503`:

```text
Translate$AccountsUnavailableException: cannot load account A-1
	at Translate.find(Translate.java:27)
	at Translate.main(Translate.java:69)
Caused by: java.sql.SQLTransientConnectionException: connection refused
	at Translate.lambda$main$0(Translate.java:59)
	at Translate.find(Translate.java:25)
	... 1 more
```

Without the cause, the log says only that the store was unavailable. Letting `SQLException` through instead ties every caller to JDBC: the
day accounts move behind an HTTP client, each `catch (SQLException e)` stops compiling.

<!-- practice 3 -->

## Summary

- **Throw where the bad value arrives, before anything changes.** A throw after a change leaves
  the change behind, and a value nothing checks may not fail at all.
- **The compiler knows a `throw` ends the block, not that a helper always throws.** Have the
  helper return the exception and `throw` it at the call site. Throwing a `null` reference throws
  a `NullPointerException`.
- **A checked exception can't leave a `Function`.** Wrap it in the lambda, and unwrap it before
  it leaves your method so callers can still catch the checked type.
- **A sneaky throw compiles and hides the checked type.** Callers can't catch it by name, and a
  proxy turns it into `UndeclaredThrowableException`.
- **Translate at a layer boundary and keep the cause.** Translate only the lower layer's
  exceptions, or your own become theirs.

## Reference

**`throw` and `throws` (Java 21):**

| Code | Result |
|---|---|
| `throw "text";` | compile error: `String cannot be converted to Throwable` |
| a statement after `throw` in the same block | compile error: `unreachable statement` |
| `throw x;` with `x == null` | `NullPointerException` |
| `default -> throw ...` in a `switch` expression | allowed; that arm needs no value |
| a checked exception thrown, not caught, not in `throws` | compile error: `unreported exception X; must be caught or declared to be thrown` |
| `catch (X e)` for a checked `X` the `try` body can't throw | compile error: `exception X is never thrown in body of corresponding try statement` |
| an unchecked exception listed in `throws` | allowed; callers need not catch it |
| an exception uncaught in `main` | the default handler prints `Exception in thread "main"` and the trace to standard error; exit status 1 |
| a checked exception its interface doesn't declare, through a `java.lang.reflect.Proxy` | `UndeclaredThrowableException` with the exception as its cause |

## Challenges

1. Your service validates requests in a filter, then again in each method. Which checks belong
   where, and what happens when the two disagree?
2. A stream pipeline runs in parallel and three elements fail with `IOException`. Which one does
   the caller see, and how would you report all three?
3. Find a sneaky throw in a library you use. Which of your `catch` blocks would have to change if
   it became a declared checked exception?

## Design Note: Why the Checked Exception Stops at the Lambda

Checked exceptions were designed for direct calls: the compiler reads each signature and makes
the caller deal with what it lists. A lambda turns a call into a value that runs later, maybe on
another thread, through an interface written long before your exception. `java.util.function`
chose interfaces that declare nothing, so any lambda fits any stream. A checked exception then
has to be wrapped, or hidden from the compiler, to get through. Either way the caller loses the
one thing the checked exception gave it: a type it can catch by name. Unwrapping at your own
boundary gives it back.

## Code Examples

- Test: [ExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemoTest.java)
- Source: [ExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemo.java)
