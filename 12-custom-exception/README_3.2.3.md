# 3.2.1. Creating Custom Exception Classes

Custom exceptions are user-defined exception classes that extend existing Java exception classes. They allow developers to create specific exception types for their application's unique error conditions.

## Key points:
- Custom exceptions typically extend `Exception` (for checked exceptions) or `RuntimeException` (for unchecked exceptions).
- They should have meaningful names that describe the error condition.
- Include constructors that allow passing error messages and cause exceptions.

Example:
```java
public class InvalidUserInputException extends Exception {
    public InvalidUserInputException(String message) {
        super(message);
    }

    public InvalidUserInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 3.2.2. Throwing Exceptions Using the `throw` Keyword

The `throw` keyword is used to explicitly throw an exception in Java. It's followed by an instance of the exception class you want to throw.

Key points:
- Use `throw` when a specific error condition is met.
- You can throw both built-in and custom exceptions.
- After throwing an exception, the current method execution stops, and control is transferred to the nearest matching catch block.

Example:
```java
if (userInput.isEmpty()) {
    throw new InvalidUserInputException("User input cannot be empty");
}
```

## 3.2.3. Best Practices for Custom Exceptions

When creating and using custom exceptions, follow these best practices:

1. Use descriptive names: Name your custom exceptions clearly to indicate the error condition they represent.
2. Extend the appropriate exception class: Choose between extending `Exception` or `RuntimeException` based on whether you want a checked or unchecked exception.
3. Include constructors: Provide constructors that accept error messages and cause exceptions.
4. Add custom information: If needed, include additional fields or methods to provide more context about the error.
5. Use custom exceptions judiciously: Create custom exceptions only for truly unique error conditions specific to your application domain.
6. Document your exceptions: Use Javadoc to describe when and why the exception might be thrown.
7. Keep the exception hierarchy shallow: Avoid creating deep hierarchies of custom exceptions.
8. Follow naming conventions: End the exception class name with "Exception".

## 3.2.4. Checked vs. Unchecked Exceptions

Java has two main categories of exceptions: checked and unchecked.

Checked Exceptions:
- Extend `Exception` (but not `RuntimeException`).
- Must be declared in the method signature using the `throws` clause or handled using try-catch.
- Represent recoverable errors that the calling code should be aware of.
- Examples: `IOException`, `SQLException`

Unchecked Exceptions:
- Extend `RuntimeException`.
- Don't need to be declared or caught explicitly.
- Represent programming errors or unrecoverable conditions.
- Examples: `NullPointerException`, `IllegalArgumentException`

When to use each:
- Use checked exceptions for recoverable errors that the caller should be aware of and might want to handle.
- Use unchecked exceptions for programming errors or unrecoverable situations.

## Interview Q&A

Q1: What's the difference between throwing a custom exception and a built-in Java exception?

A1: The main difference lies in the specificity and context provided by the custom exception. Custom exceptions allow you to create error types specific to your application's domain, making the code more readable and maintainable. They can also include additional information relevant to your application. Built-in Java exceptions are more general-purpose and may not convey the exact nature of the error in your specific use case.

Here's an example of throwing a custom exception vs. a built-in exception:

```java
// Custom exception
if (user.getAge() < 18) {
    throw new UnderageUserException("User must be 18 or older to proceed.");
}

// Built-in exception
if (user.getAge() < 18) {
    throw new IllegalArgumentException("Invalid user age.");
}
```

Q2: How do you decide whether to create a checked or unchecked custom exception?

A2: The decision to create a checked or unchecked custom exception depends on the nature of the error and how you expect it to be handled:

- Use checked exceptions when:
    1. The error is recoverable.
    2. You want to force the caller to handle or declare the exception.
    3. The exception represents a condition that the caller should be aware of.

- Use unchecked exceptions when:
    1. The error is due to a programming mistake.
    2. The error is unrecoverable.
    3. Forcing every caller to handle the exception would lead to cluttered code.

Example scenario:
```java
// Checked exception - recoverable, caller should be aware
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// Unchecked exception - programming error, unrecoverable
public class InvalidDatabaseConfigException extends RuntimeException {
    public InvalidDatabaseConfigException(String message) {
        super(message);
    }
}
```

Q3: What are some best practices for creating and using custom exceptions in Java?

A3: Here are some best practices for creating and using custom exceptions:

1. Use descriptive names that clearly indicate the error condition.
2. Extend the appropriate base class (`Exception` for checked, `RuntimeException` for unchecked).
3. Provide constructors that accept messages and cause exceptions.
4. Include additional context information if necessary.
5. Use custom exceptions judiciously - only for truly unique error conditions.
6. Document your exceptions with Javadoc.
7. Keep the exception hierarchy shallow.
8. Follow naming conventions (end with "Exception").
9. Throw exceptions at the appropriate level of abstraction.
10. Catch and wrap lower-level exceptions to maintain abstraction.

Example implementing these practices:

```java
/**
 * Thrown when a user attempts to withdraw more money than their account balance allows.
 */
public class InsufficientFundsException extends Exception {
    private final double requestedAmount;
    private final double accountBalance;

    public InsufficientFundsException(String message, double requestedAmount, double accountBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.accountBalance = accountBalance;
    }

    public InsufficientFundsException(String message, double requestedAmount, double accountBalance, Throwable cause) {
        super(message, cause);
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