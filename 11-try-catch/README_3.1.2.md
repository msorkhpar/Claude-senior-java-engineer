# 3.1.2. Using Multiple Catch Blocks for Different Exception Types

The checkout service charges cards through a payment gateway. When the gateway times out, the
charge may or may not have gone through, so the timeout's catch block asks the gateway's status
endpoint. During a gateway incident the status call failed too. Customers got an HTTP 500,
although `pay()` has a `catch (GatewayException e)` right below.

## The First Catch Clause That Matches Wins, So a Subclass Goes Above Its Superclass

Java tests the catch clauses from top to bottom and runs the first one whose type the exception is
an instance of. A rates loader uses yesterday's file when today's is missing, retries a slow read,
and fails on any other I/O error:

```java
static String load(String what) {
    try {
        read(what);
        return "ok";
    } catch (FileNotFoundException e) {
        return "missing, use yesterday's file: " + e.getMessage();
    } catch (SocketTimeoutException e) {
        return "slow, retry: " + e.getMessage();
    } catch (IOException e) {
        return "failed: " + e.getMessage();
    }
}
```

```text
missing -> missing, use yesterday's file: rates.csv
slow -> slow, retry: Read timed out
reset -> failed: Connection reset
```

Position decides, not specificity as with overloads. Both specific types are `IOException`s, so
with the `IOException` clause on top nothing could reach them, and javac rejects the order:

```text
Reversed.java:15: error: exception FileNotFoundException has already been caught
        } catch (FileNotFoundException e) {
          ^
```

Only catch clauses get this check. The same order as an `instanceof` chain inside one
`catch (Exception e)` compiles, and the yesterday's-file branch never runs:

```java
} catch (Exception e) {
    if (e instanceof IOException) {
        return "failed: " + e.getMessage();
    } else if (e instanceof FileNotFoundException) {   // compiles, never runs
        return "missing, use yesterday's file: " + e.getMessage();
    }
    throw new IllegalStateException(e);
}
```

```text
failed: rates.csv
```

## An Exception Thrown in a Catch Block Leaves the Whole try Statement

The catch clauses of a `try` guard its try block, nothing else. The checkout:

```java
public class Siblings2 {
    static class GatewayException extends RuntimeException {
        GatewayException(String m) { super(m); }
    }
    static class Timeout extends GatewayException {
        Timeout(String m) { super(m); }
    }

    static String charge() { throw new Timeout("no answer in 5 s"); }
    static String status() { throw new GatewayException("503 from /status"); }

    static String pay() {
        try {
            return charge();
        } catch (Timeout e) {
            return status();                        // the charge may have gone through: ask
        } catch (GatewayException e) {
            return "retry later: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println(pay());
    }
}
```

```text
Exception in thread "main" Siblings2$GatewayException: 503 from /status
	at Siblings2.status(Siblings2.java:10)
	at Siblings2.pay(Siblings2.java:16)
	at Siblings2.main(Siblings2.java:23)
```

The 503 is a `GatewayException`, but the statement chose its handler when `charge()` threw, and it
chooses once. Had `status()` thrown a checked `IOException`, javac would have refused it as
unreported, even with a `catch (IOException e)` below. An unchecked one compiles and propagates, and
the customer gets a 500 for a charge that may have succeeded. A call in a handler that can fail needs its own `try`, which
decides what that failure means: here, "payment pending".

<!-- practice 1 -->

## A Multi-Catch Shares One Handler; Its Parameter Is Final and Typed as What the Types Share

`catch (A | B e)` runs one block for either type. Its alternatives must be unrelated, since a
subclass alternative adds nothing:

```text
Related.java:10: error: Alternatives in a multi-catch statement cannot be related by subclassing
        } catch (FileNotFoundException | IOException e) {
                                         ^
  Alternative FileNotFoundException is a subclass of alternative IOException
```

The parameter is implicitly final (`multi-catch parameter e may not be assigned`). Its type is not "`A` or `B`" with the methods of both, but the
closest supertype they share, their least upper bound. For `IOException | SQLException` that is
`Exception`, and `e.getSQLState()` doesn't compile:

```text
Lub.java:11: error: cannot find symbol
            System.out.println("failed: " + e.getSQLState());
                                             ^
  symbol:   method getSQLState()
  location: variable e of type Exception
```

`catch (Exception e)` is not a shorter multi-catch: it also takes unchecked exceptions, so a bug's
`NullPointerException` gets the I/O error's handling (unit 3.1.1).

## Catching Exception to Rethrow It Keeps the Precise throws Clause

A cleanup block can catch `Exception` and still declare only the checked exceptions its try
throws:

```java
static void transfer() throws IOException, SQLException {
    try {
        debit();                         // throws SQLException
        journal();                       // throws IOException
    } catch (Exception e) {              // every exception, unchecked ones included
        rollback();
        throw e;                         // still only IOException or SQLException, or unchecked
    }
}
```

```text
rolled back
caller sees java.sql.SQLException: deadlock detected, SQLState 40P01
```

`throw e` on an effectively final catch parameter throws only the checked exceptions the try block
can throw and that no earlier clause catches (JLS 11.2.2). Catching `Exception` is right here because nothing is
swallowed: a bug's `IllegalStateException` is rolled back and rethrown too. Assign to `e` and the
precision is gone:

```java
} catch (Exception e) {
    if (e instanceof SQLException s && "40P01".equals(s.getSQLState())) {
        e = new SQLException("transfer deadlocked, safe to retry", "40P01", e);
    }
    throw e;
}
```

```text
Reassigned.java:17: error: unreported exception Exception; must be caught or declared to be thrown
            throw e;
            ^
```

Developers who expect that error everywhere declare `throws Exception`, which every caller must
then handle. Others wrap the failure in a `RuntimeException`, and the caller's
`catch (SQLException e)` that retries deadlocks never runs.

<!-- practice 2 -->

## A Catch for a Checked Exception the Try Can't Throw Is an Error, Except for Exception and Throwable

javac knows which checked exceptions a try block can throw, from its `throw` statements and the
`throws` clauses of what it calls. A refactor made `readRates()` read a cache and dropped its `throws IOException`:

```java
static List<String> load() {
    try {
        return readRates();
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
}
```

```text
Never.java:13: error: exception IOException is never thrown in body of corresponding try statement
        } catch (IOException e) {
          ^
```

Each multi-catch alternative is checked the same way. `Exception` and `Throwable` always pass,
because they include unchecked exceptions, which any code can throw. After the same refactor a
`catch (Exception e)` compiles silently, and its handler now takes only unchecked exceptions.

<!-- practice 3 -->

## Summary

- **The first matching clause wins.** javac checks that order for clauses, not for `instanceof`
  chains.
- **A catch block's own exception skips the clauses below it.** A handler's risky call needs its
  own `try`.
- **A multi-catch takes unrelated types; its parameter is final and typed as their least upper
  bound.**
- **Rethrowing an unchanged `catch (Exception e)` keeps the precise `throws` clause.**
- **Catching a checked exception the try can't throw is an error**; `Exception` hides a dead
  handler.

## Reference

**Which catch types compile (javac 21):**

| The clause catches | Compiles when |
|---|---|
| a checked exception | the try block can throw it, a subclass of it, or a superclass of it |
| an unchecked exception, `Exception`, `Throwable` | always, unless after a clause for its supertype |
| a type after a clause for its supertype | never |
| related types in one multi-catch | never |

## Challenges

1. Find a `catch (Exception e)` in your code that handles rather than rethrows. Would a multi-catch
   of the checked types change behaviour?
2. A timeout handler calls a second service. For each exception that call can throw, what should
   the method return?
3. When is wrapping better than rethrowing `e` unchanged? What does the caller gain and lose?

## Design Note: The Compiler Reads the Catch List as a Claim

javac checks each catch clause as a claim that this try can throw that exception, and multi-catch
and an unchanged `throw e` keep the claim narrow without duplicated blocks or `throws Exception`.
Unchecked exceptions, `catch (Exception e)` and `instanceof` chains claim nothing javac can test,
so a refactor that changes what a try throws goes unnoticed there.

## Code Examples

- Test: [ExceptionHandlerTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandlerTest.java)
- Source: [ExceptionHandler.java](src/main/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandler.java)
