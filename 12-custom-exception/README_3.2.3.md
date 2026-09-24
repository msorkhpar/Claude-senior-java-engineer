# 3.2.3. Best Practices for Custom Exceptions

## Concept Explanation

[3.2.1](README_3.2.1.md) showed how to write a custom exception class and [3.2.2](README_3.2.2.md) how to throw one.
This lesson covers the judgement around them: when a custom exception is worth creating, what it should contain, and
how to throw and handle it so that the error stays useful from the place it happens to the place it is handled or
logged.

The module's `BankAccount` example follows these practices:

```java
public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
    if (amount <= 0) {
        throw new InvalidAmountException("Withdrawal amount must be positive");
    }
    if (amount > balance) {
        throw new InsufficientFundsException("Insufficient funds for withdrawal", amount, balance);
    }
    balance -= amount;
}
```

- The exception names say what went wrong (`InsufficientFundsException`), not where.
- `InsufficientFundsException` carries the requested amount and the balance as fields, so a caller can build a precise
  response without parsing the message.
- Both checks run **before** the balance changes, so a failed withdrawal leaves the account exactly as it was.

## Key Points to Remember

1. **Reuse standard exceptions when they fit.** `IllegalArgumentException` (bad argument), `IllegalStateException`
   (object not in the right state), `NullPointerException` (null where not allowed), `UnsupportedOperationException`,
   `NoSuchElementException` and `IndexOutOfBoundsException` are understood by every Java developer. Create a custom
   exception when the error is part of your domain and callers need to tell it apart (`InsufficientFundsException`), or
   when it must carry extra data.
2. **Name it after the problem** and end the name with `Exception`.
3. **Choose checked or unchecked deliberately** ([3.2.4](README_3.2.4.md)): checked when a caller can reasonably recover
   and should be forced to consider it, unchecked for programming errors and conditions callers cannot fix.
4. **Carry context**: typed `final` fields for data a handler may use, and a message that includes the offending
   values.
5. **Never lose the cause**: when translating one exception into another, pass the original as the cause.
6. **Fail atomically**: validate before changing state, so a thrown exception leaves the object as it was.
7. **Document it**: every exception a public method can throw, checked or unchecked, gets a Javadoc `@throws` tag.

## Relevant Java Features

- **Java 1.4**: exception chaining (`getCause()`, cause constructors), which exception translation relies on.
- **Java 7**: multi-catch (`catch (IOException | SQLException e)`) reduces duplicated handlers, and try-with-resources
  records exceptions from `close()` as suppressed instead of losing them.
- **Java 17 / 21**: a sealed exception hierarchy (Java 17) can be handled exhaustively with pattern matching for
  `switch` (Java 21); see [3.2.1](README_3.2.1.md).

## Common Pitfalls and How to Avoid Them

1. **Swallowing exceptions**: an empty `catch` block hides the failure and the program continues in a wrong state.
   Handle it, rethrow it, or at least log it with its stack trace.
2. **Logging and rethrowing**: logging an exception and then rethrowing it makes every layer log the same failure. Log
   it once, where it is finally handled.
3. **Catching too broadly**: `catch (Exception e)` also catches `RuntimeException`s such as `NullPointerException`
   that indicate bugs. Catch the specific types you can handle.
4. **Exceptions for normal control flow**: using an exception to end a loop or to signal an ordinary outcome is slower
   and harder to read than a return value or an `Optional`. Exceptions are for exceptional conditions.
5. **Sensitive data in messages**: exception messages end up in logs and sometimes in responses. Do not put passwords,
   tokens or full card numbers in them.
6. **Too many exception classes**: one class per method or per message makes the hierarchy hard to use. Keep it
   shallow, for example one base exception per module or domain and a few specific subclasses.

## Best Practices

1. Throw early (validate inputs at the top of the method), catch late (where the caller can actually do something).
2. Translate exceptions at layer boundaries: a service should not leak a `SQLException` from its data layer. Wrap it in
   an exception of the service's own level of abstraction, keeping it as the cause.

   ```java
   public class OrderLookupException extends Exception {
       public OrderLookupException(String message, Throwable cause) {
           super(message, cause);
       }
   }

   public Order findOrder(long id) throws OrderLookupException {
       try {
           return orderDao.load(id); // may throw SQLException
       } catch (SQLException e) {
           throw new OrderLookupException("Could not load order " + id, e);
       }
   }
   ```

3. Make exception objects immutable: `final` fields, no setters.
4. Write messages for the person reading the log: what failed, with which values.
5. Keep the exception hierarchy shallow and meaningful to callers.

## Edge Cases and Their Handling

1. **Exceptions from constructors**: if a constructor throws, no object reference is returned to the caller. Validate
   arguments in the constructor so an invalid object can never exist.
2. **Failure atomicity is not automatic**: if a method changes several fields and throws in the middle, the object is
   left half-updated. Validate first, or compute the new state in local variables and assign it only at the end.

## Interview-specific Insights

- Be ready to say when you would create a custom exception and when a standard one is enough.
- Know exception translation and why the cause must be kept.
- Know failure atomicity and how to achieve it.
- Be able to name common anti-patterns: swallowing, log-and-rethrow, catching `Exception`, exceptions for flow control.

## Interview Q&A

Q1: What's the difference between throwing a custom exception and a built-in Java exception?

A1: The main difference lies in the specificity and context provided by the custom exception. Custom exceptions allow
you to create error types specific to your application's domain, so callers can catch exactly that case, and they can
carry additional fields. Built-in exceptions are general-purpose; they are the right choice when the error is a generic
one (a bad argument, a wrong state) that callers do not need to distinguish.

```java
// Custom exception: a domain rule that callers may want to handle specifically
if (user.getAge() < 18) {
    throw new UnderageUserException("User must be 18 or older to proceed.");
}

// Built-in exception: a plain invalid argument
if (age < 0) {
    throw new IllegalArgumentException("Age cannot be negative: " + age);
}
```

Q2: What is exception translation, and why should the cause be kept?

A2: Exception translation is catching a lower-level exception and throwing one that fits the current layer's
abstraction, such as turning a `SQLException` into an `OrderLookupException` in a service. Callers then depend only on
the service's API, not on its storage technology. Passing the original exception as the cause keeps its message and
stack trace, so the real reason is still visible in the logs (`Caused by: java.sql.SQLException: ...`).

Q3: What is failure atomicity, and how does `BankAccount.withdraw` achieve it?

A3: A method is failure-atomic if, when it throws, the object is left in the state it had before the call.
`BankAccount.withdraw` checks the amount and the balance first and subtracts only after both checks have passed, so a
withdrawal that throws `InsufficientFundsException` leaves the balance unchanged. This module's `BankAccountTest`
checks exactly that.

Q4: What are some best practices for creating and using custom exceptions in Java?

A4: Here are some best practices for creating and using custom exceptions:

1. Reuse standard exceptions when they describe the error; create custom ones for domain-specific errors.
2. Use descriptive names that end with "Exception".
3. Extend the appropriate base class (`Exception` for checked, `RuntimeException` for unchecked).
4. Provide constructors that accept a message and a cause.
5. Include additional context as `final` fields, and useful values in the message (never secrets).
6. Document your exceptions with Javadoc `@throws`.
7. Keep the exception hierarchy shallow.
8. Throw exceptions at the appropriate level of abstraction, translating lower-level ones and keeping the cause.
9. Validate before changing state, so a thrown exception leaves the object unchanged.
10. Do not swallow exceptions, and log each one once.

This module's `InsufficientFundsException` shows several of these:

```java
class InsufficientFundsException extends Exception {
    private final double requestedAmount;
    private final double accountBalance;

    public InsufficientFundsException(String message, double requestedAmount, double accountBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.accountBalance = accountBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getAccountBalance() {
        return accountBalance;
    }
}
```

## Code Examples

- Test: [BankAccountTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/BankAccountTest.java)
- Source: [BankAccount.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/BankAccount.java)
