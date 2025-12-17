# 3.1.2. Using Multiple Catch Blocks for Different Exception Types

## Concept Explanation

In Java, multiple catch blocks allow developers to handle different types of exceptions separately within a single
try-catch structure. This feature enables more granular and specific exception handling, improving code readability and
maintainability.

## Key Points

1. Multiple catch blocks are defined after a single try block.
2. Catch blocks are evaluated in order, from top to bottom.
3. More specific exceptions should be caught before more general ones.
4. Java 7 introduced multi-catch syntax, allowing multiple exception types in a single catch block.

## Java 21 Features

Java 21 doesn't introduce new features specific to multiple catch blocks, but it supports all previous enhancements,
including multi-catch syntax introduced in Java 7.

## Common Pitfalls and How to Avoid Them

1. **Catching Exception class first**: This will catch all exceptions, preventing more specific handlers from executing.
    - Solution: Order catch blocks from most specific to most general.

2. **Duplicating code in catch blocks**: This can lead to maintenance issues.
    - Solution: Use multi-catch syntax or extract common handling logic to a separate method.

3. **Catching and ignoring exceptions**: This can hide important errors.
    - Solution: Always log or handle exceptions appropriately.

## Best Practices and Optimization Techniques

1. Use specific exception types when possible for more precise error handling.
2. Utilize multi-catch syntax to reduce code duplication.
3. Log exceptions with meaningful context information.
4. Consider wrapping and re-throwing exceptions to maintain abstraction levels.

## Edge Cases and Their Handling

1. **Nested exceptions**: Use getCause() method to access the underlying cause.
2. **Exceptions in catch or finally blocks**: These can overshadow the original exception.
    - Solution: Use try-catch within catch blocks if necessary.

## Interview-specific Insights

Interviewers often look for:

- Understanding of exception hierarchy
- Proper ordering of catch blocks
- Knowledge of multi-catch syntax
- Ability to handle exceptions gracefully without code duplication

## Implementation

See `ExceptionHandler.java` for sample implementation and `ExceptionHandlerTest.java` for unit tests.

## Interview Q&A Section

Q1: Why is the order of catch blocks important?

```text
A1: The order of catch blocks is crucial because they are evaluated from top to bottom. 
If a more general exception (like Exception) is caught before a more specific one 
(like IllegalArgumentException), the more specific catch block will never be reached. 
This is because any exception that would be caught by the specific block would already 
have been caught by the general one. To ensure proper exception handling, always order 
catch blocks from most specific to most general.
```

Q2: What is the multi-catch syntax in Java, and when would you use it?

```text
A2: Multi-catch syntax, introduced in Java 7, allows catching multiple exception types 
in a single catch block. It uses the pipe symbol (|) to separate exception types. 
You would use this when you want to handle multiple exception types in the same way, 
reducing code duplication. For example:

try {
    // some code that may throw exceptions
} catch (IOException | SQLException e) {
    // handle both IOException and SQLException here
}

This is particularly useful when the handling logic for different exceptions is the same, 
improving code readability and maintainability.
```

Q3: How would you implement a method that demonstrates the use of multiple catch blocks, including multi-catch syntax?

```java
public static void demonstrateMultipleCatch(String input) {
    try {
        int value = Integer.parseInt(input);
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        double result = 100.0 / value;
        System.out.println("Result: " + result);
    } catch (NumberFormatException | IllegalArgumentException e) {
        System.err.println("Invalid input: " + e.getMessage());
    } catch (ArithmeticException e) {
        System.err.println("Cannot divide by zero");
    } catch (Exception e) {
        System.err.println("An unexpected error occurred: " + e.getMessage());
    }
}
```

Q4: How does exception handling in Java differ from error handling in other languages you've worked with?

```text
A4: Java's exception handling differs from other languages in several ways:

1. Checked vs Unchecked Exceptions: Java has both checked exceptions (must be declared or caught) 
   and unchecked exceptions. Many languages only have unchecked exceptions.

2. Try-with-resources: Java provides a special syntax for automatically closing resources, 
   which isn't common in all languages.

3. Multi-catch: Java allows catching multiple exception types in a single catch block, 
   which isn't available in all languages.

4. Finally block: While some languages have similar constructs, Java's finally block ensures 
   certain code always runs, whether an exception occurs or not.

5. Exception hierarchy: Java has a well-defined hierarchy of exception classes, making it 
   easier to catch groups of related exceptions.

Compared to languages like Python or JavaScript, Java's approach is more structured and 
verbose, which can lead to more robust error handling but requires more explicit code.
```

## Code Examples

-
Test: [ExceptionHandlerTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandlerTest.java)
- Source: [ExceptionHandler.java](src/main/java/com/github/msorkhpar/claudejavatutor/trycatch/ExceptionHandler.java)