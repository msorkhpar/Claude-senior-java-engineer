# 3.2.1. Creating Custom Exception Classes

Custom exceptions in Java allow developers to create specific error types tailored to their application's needs. These
exceptions can carry additional information and provide more meaningful error messages.

To create a custom exception:

1. Create a new class that extends either `Exception` (for checked exceptions) or `RuntimeException` (for unchecked
   exceptions).
2. Implement constructors to allow various ways of creating the exception.
3. Optionally, add custom methods or fields to provide additional information about the error.

Example:

```java
public class InvalidUserException extends RuntimeException {
    private final String userId;

    public InvalidUserException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
```

## 3.2.2. Throwing Exceptions Using the `throw` Keyword

To throw a custom exception, use the `throw` keyword followed by an instance of your exception class.

Example:

```java
if(user ==null){
        throw new

InvalidUserException("User not found",userId);
}
```

## 3.2.3. Best Practices for Custom Exceptions

1. **Naming Convention**: Name your exception class with a suffix "Exception" (e.g., `InvalidUserException`).
2. **Inheritance**: Extend `Exception` for checked exceptions or `RuntimeException` for unchecked exceptions.
3. **Constructors**: Provide multiple constructors for flexibility (e.g., with message, with cause, with both).
4. **Information**: Include relevant information in the exception to aid in debugging.
5. **Documentation**: Clearly document when and why the exception is thrown.
6. **Granularity**: Create specific exceptions for distinct error cases rather than using a generic exception.

## 3.2.4. Checked vs. Unchecked Exceptions

### Checked Exceptions

- Extend from `Exception` (but not `RuntimeException`).
- Must be declared in the method signature or caught within the method.
- Used for recoverable errors that the calling code should be aware of.
- Example: `IOException`, `SQLException`

### Unchecked Exceptions

- Extend from `RuntimeException`.
- Do not need to be declared or caught explicitly.
- Used for programming errors or unrecoverable states.
- Example: `NullPointerException`, `IllegalArgumentException`

Choose between checked and unchecked based on whether the calling code can reasonably be expected to recover from the
exception.

## Key Points to Remember

- Custom exceptions enhance code readability and error handling.
- Use descriptive names and include relevant error information.
- Choose between checked and unchecked exceptions based on the nature of the error.
- Follow Java naming conventions and best practices when creating custom exceptions.

## Common Pitfalls

- Overusing exceptions for flow control.
- Creating too many or too few custom exceptions.
- Not providing enough information in custom exceptions.
- Using checked exceptions for unrecoverable errors.

## Interview Insights

- Be prepared to explain when and why you would create a custom exception.
- Understand the difference between checked and unchecked exceptions and when to use each.
- Be able to discuss best practices in exception handling and creation.

Q1: What is the difference between checked and unchecked exceptions in Java?

```text
A1: The main differences between checked and unchecked exceptions in Java are:

1. Inheritance: Checked exceptions inherit from Exception (but not RuntimeException), while unchecked exceptions inherit from RuntimeException.

2. Compile-time checking: Checked exceptions must be either caught or declared in the method signature using the 'throws' keyword. Unchecked exceptions don't require this.

3. Usage: Checked exceptions are typically used for recoverable errors that the calling code should be aware of (e.g., IOException). Unchecked exceptions are used for programming errors or unrecoverable states (e.g., NullPointerException).

4. Handling: Checked exceptions force the developer to handle the exception or propagate it, which can lead to more robust error handling. Unchecked exceptions can be handled optionally.

5. Performance: Unchecked exceptions have slightly better performance because the compiler doesn't need to check them.

Choose between checked and unchecked exceptions based on whether the calling code can reasonably be expected to recover from the exception.
```

Q2: How would you create a custom checked exception in Java?

```java
public class CustomCheckedException extends Exception {
    public CustomCheckedException() {
        super();
    }

    public CustomCheckedException(String message) {
        super(message);
    }

    public CustomCheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomCheckedException(Throwable cause) {
        super(cause);
    }
}
```

Q3: What are some best practices for creating and using custom exceptions?

```text
A3: Some best practices for creating and using custom exceptions include:

1. Use descriptive names: Name your exception class clearly, ending with "Exception" (e.g., InvalidUserException).

2. Extend the appropriate base class: Extend Exception for checked exceptions or RuntimeException for unchecked exceptions.

3. Provide multiple constructors: Include constructors with no arguments, with a message, with a cause, and with both message and cause.

4. Include relevant information: Add fields and methods to provide additional context about the error.

5. Use specific exceptions: Create distinct exception classes for different error scenarios rather than using a generic exception.

6. Document thoroughly: Clearly document when and why the exception is thrown in the class and method Javadocs.

7. Follow the principle of least astonishment: Throw exceptions that make sense in the context of the method and class.

8. Don't use exceptions for flow control: Exceptions should be for exceptional circumstances, not regular program flow.

9. Preserve the original exception: When catching and re-throwing, consider using exception chaining to maintain the stack trace.

10. Keep exceptions at the appropriate level of abstraction: Higher-level components should throw higher-level exceptions.

By following these practices, you can create more maintainable and understandable code with effective error handling.
```

## Code Examples

-
Test: [CustomExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemoTest.java)
-
Source: [CustomExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemo.java)