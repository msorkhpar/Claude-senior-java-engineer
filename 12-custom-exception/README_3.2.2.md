# 3.2.1. Creating Custom Exception Classes

Custom exceptions in Java are user-defined exception classes that extend existing Java exception classes. They allow
developers to create specific exception types for their application's unique error conditions.

## Key points:

- Custom exceptions typically extend `Exception` (for checked exceptions) or `RuntimeException` (for unchecked
  exceptions).
- They should have a meaningful name that describes the exceptional condition.
- Include constructors that allow passing a custom message and/or cause.

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

The `throw` keyword is used to explicitly throw an exception in Java. It can be used with both built-in Java exceptions
and custom exceptions.

Key points:

- Use `throw` followed by a new instance of the exception class.
- The `throw` statement is typically used within a method or constructor.
- After throwing an exception, the current method execution stops, and the exception is propagated up the call stack.

Example:

```java
if (userInput.isEmpty()) {
    throw new InvalidUserInputException("User input cannot be empty");
}
```

## 3.2.3. Best Practices for Custom Exceptions

When creating and using custom exceptions, follow these best practices:

1. Use descriptive names: Choose exception names that clearly describe the error condition.
2. Extend appropriate superclass: Extend `Exception` for checked exceptions or `RuntimeException` for unchecked
   exceptions.
3. Include constructors: Provide constructors that accept a message and/or cause.
4. Add custom information: If needed, include additional fields or methods specific to the exception.
5. Document exceptions: Use Javadoc to document the conditions under which the exception is thrown.
6. Throw exceptions at the appropriate level: Throw exceptions as close to the source of the error as possible.
7. Catch and wrap exceptions: When catching and re-throwing, consider wrapping the original exception to preserve the
   stack trace.

## 3.2.4. Checked vs. Unchecked Exceptions

Java has two main categories of exceptions: checked and unchecked.

Checked Exceptions:

- Extend `Exception` (but not `RuntimeException`).
- Must be declared in the method signature using the `throws` clause or handled using try-catch.
- Represent recoverable errors that the calling code should handle.
- Examples: `IOException`, `SQLException`.

Unchecked Exceptions:

- Extend `RuntimeException`.
- Do not need to be declared or caught explicitly.
- Represent programming errors or unrecoverable conditions.
- Examples: `NullPointerException`, `IllegalArgumentException`.

When to use each:

- Use checked exceptions for recoverable errors that the caller should be aware of and handle.
- Use unchecked exceptions for programming errors or unrecoverable situations.

## Interview Q&A

Q1: What is the purpose of creating custom exceptions in Java?
A1: Custom exceptions in Java serve several purposes:

1. They provide more specific error handling for application-specific scenarios.
2. They improve code readability by clearly indicating the type of error that occurred.
3. They allow for adding custom information or behavior relevant to the specific error condition.
4. They help in creating a hierarchical and well-organized exception handling structure in large applications.

Q2: How do you create a custom checked exception in Java?
A2: To create a custom checked exception in Java:

```java
public class CustomCheckedException extends Exception {
    public CustomCheckedException(String message) {
        super(message);
    }

    public CustomCheckedException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Q3: What is the difference between throwing a checked and an unchecked exception?
A3: The main differences are:

1. Declaration: Checked exceptions must be declared in the method signature using the `throws` clause, while unchecked
   exceptions don't require this.
2. Handling: Checked exceptions must be either caught or propagated, while unchecked exceptions don't force the caller
   to handle them.
3. Inheritance: Checked exceptions extend `Exception` (but not `RuntimeException`), while unchecked exceptions extend
   `RuntimeException`.
4. Use case: Checked exceptions are for recoverable errors, while unchecked exceptions are for programming errors or
   unrecoverable situations.

Q4: Can you provide an example of when to use a custom unchecked exception?
A4: Here's an example of using a custom unchecked exception:

```java
public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}

public class ConfigurationManager {
    public void loadConfiguration(String filePath) {
        if (!Files.exists(Paths.get(filePath))) {
            throw new InvalidConfigurationException("Configuration file not found: " + filePath);
        }
        // Load configuration...
    }
}
```

In this example, `InvalidConfigurationException` is an unchecked exception used to indicate a programming error (missing
configuration file) that should be fixed during development rather than handled at runtime.

Q5: What are some best practices for using custom exceptions in Java?
A5: Some best practices for using custom exceptions in Java include:

1. Use meaningful and descriptive names for exception classes.
2. Provide constructors that accept a message and/or cause.
3. Document the conditions under which the exception is thrown using Javadoc.
4. Throw exceptions at the appropriate level of abstraction.
5. Include relevant information in the exception message.
6. Consider creating a custom exception hierarchy for complex applications.
7. Use checked exceptions for recoverable errors and unchecked exceptions for programming errors.
8. Avoid catching and ignoring exceptions without proper handling or logging.

Q6: How can you preserve the original exception information when throwing a new custom exception?
A6: To preserve the original exception information when throwing a new custom exception, you can use exception chaining.
Here's an example:

```java
try {
    // Some code that may throw an exception
} catch (SomeException e) {
    throw new CustomException("A custom error occurred", e);
}
```

In this example, the original exception `e` is passed as the cause to the new `CustomException`. This preserves the
stack trace and allows access to the original exception information.

## Code Examples

- Test: [ExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemoTest.java)
- Source: [ExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemo.java)