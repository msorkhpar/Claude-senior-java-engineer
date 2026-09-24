# 3.2.2. Throwing Exceptions Using the `throw` Keyword

## Concept Explanation

The `throw` keyword is used to explicitly throw an exception in Java. It can be used with both built-in Java exceptions
and custom exceptions. Its operand must be an expression whose type is `Throwable` or a subclass; usually it is a new
instance:

```java
if (userInput.isEmpty()) {
    throw new InvalidUserInputException("User input cannot be empty");
}
```

When a `throw` statement runs:

1. The current method stops at that point; no further statement of it runs (apart from `finally` blocks on the way out).
2. The JVM looks for the nearest enclosing `try` whose `catch` clause matches the exception's run-time type, first in
   the current method and then in each caller up the call stack.
3. If no handler is found, the thread terminates and its uncaught-exception handler (by default) prints the stack trace.

### `throw` and `throws`

The two keywords are easy to mix up:

- `throw` is a **statement**: it throws one exception object, now.
- `throws` is part of a **method declaration**: it lists the checked exceptions the method may let escape.

A method that throws a checked exception must either catch it or declare it with `throws`; otherwise the code does not
compile:

```java
public static void methodWithCheckedException(boolean throwException) throws CustomCheckedException {
    if (throwException) {
        throw new CustomCheckedException("This is a custom checked exception");
    }
}

// Without "throws CustomCheckedException" the compiler reports:
// error: unreported exception CustomCheckedException; must be caught or declared to be thrown
```

An unchecked exception (a `RuntimeException` subclass) can be thrown without a `throws` clause. Listing it in `throws`
is allowed and documents it, but the compiler does not enforce it.

### Rethrowing and wrapping

Inside a `catch` block, `throw` can rethrow the caught exception or throw a new one that wraps it as its cause
(exception chaining):

```java
public static void methodWithExceptionChaining() throws CustomCheckedException {
    try {
        throw new IOException("Original IO exception");
    } catch (IOException e) {
        throw new CustomCheckedException("Wrapped exception", e); // e becomes getCause()
    }
}
```

Passing `e` as the cause keeps the original message and stack trace; the stack trace printed for the new exception ends
with a `Caused by: java.io.IOException: Original IO exception` section.

## Key Points to Remember

1. `throw` needs a `Throwable`; `throw "error"` or `throw 42` does not compile.
2. Statements directly after a `throw` in the same block are unreachable, and the compiler rejects them
   ("unreachable statement").
3. A checked exception thrown with `throw` must be caught or declared with `throws`; an unchecked one need not be.
4. Throwing `null` (for example `throw someVariable;` when the variable is null) throws a `NullPointerException`
   instead.
5. The stack trace is captured when the exception object is **created**, not where it is thrown. Create the exception
   at the `throw` site so the trace points there.
6. When wrapping, pass the caught exception as the cause.

## Relevant Java Features

- **Java 7**: precise rethrow. If a `catch (Exception e)` block rethrows `e` unchanged, and `e` is effectively final,
  the compiler knows which checked exceptions the `try` block can actually throw, so the method only has to declare
  those:

  ```java
  static void precise(boolean fail) throws IOException { // not "throws Exception"
      try {
          if (fail) throw new IOException("io");
      } catch (Exception e) {
          System.out.println("logging " + e.getMessage());
          throw e; // the compiler knows e can only be an IOException (or an unchecked exception)
      }
  }
  ```

- **Java 14**: switch expressions (JEP 361) allow `throw` as the body of a case, which makes it easy to reject
  unexpected values:

  ```java
  static int parseLevel(String s) {
      return switch (s) {
          case "low" -> 1;
          case "high" -> 3;
          default -> throw new IllegalArgumentException("Unknown level: " + s);
      };
  }
  // parseLevel("high") returns 3; parseLevel("mid") throws IllegalArgumentException: Unknown level: mid
  ```

- **Java 14**: helpful `NullPointerException` messages (JEP 358) name the variable or expression that was null, which
  makes an explicit `throw` for a null check less necessary than it used to be; `Objects.requireNonNull(value, "value")`
  still documents intent and fails fast.

## Common Pitfalls and How to Avoid Them

1. **Losing the cause**: `throw new CustomCheckedException("Failed: " + e.getMessage());` keeps only the text. Pass
   `e` as the cause.
2. **Throwing from a lambda**: the standard functional interfaces (`Function`, `Consumer`, ...) declare no checked
   exceptions, so a lambda body that throws a checked exception does not compile ("unreported exception IOException;
   must be caught or declared to be thrown"). Catch it inside the lambda and wrap it in an unchecked exception, or use
   a functional interface of your own that declares it.
3. **Throwing a generic `Exception` or `RuntimeException`**: callers cannot catch it selectively. Throw the most
   specific type that describes the problem.
4. **Creating an exception far from the `throw`**: a pre-built exception stored in a field has the stack trace of the
   place where it was created, which misleads anyone reading the logs.

## Best Practices

1. Validate arguments at the start of a method and throw immediately (fail fast), before any state has changed.
2. Write messages that state what was wrong and with which value: `"Unknown level: mid"`, not `"Error"`.
3. When translating an exception to a higher-level one, always chain the cause.
4. Declare unchecked exceptions a public method throws in its Javadoc `@throws` tags, even though `throws` does not
   require them.

## Edge Cases and Their Handling

1. **`throw` inside `finally`**: an exception thrown from `finally` replaces the one that was propagating, which is lost.
   See [3.1.3](../11-try-catch/README_3.1.3.md); prefer try-with-resources, which records the second exception as
   suppressed instead.
2. **Rethrowing a caught exception unchanged**: `throw e;` keeps the original stack trace; it does not reset it to the
   rethrow point.

## Interview-specific Insights

- Know the difference between `throw` and `throws` and be able to say which one is a statement.
- Be able to explain what happens to control flow after `throw`, including `finally`.
- Know precise rethrow (Java 7) and `throw` in switch expressions (Java 14).
- Be ready to explain why wrapping should keep the cause.

## Interview Q&A

Q1: What is the difference between `throw` and `throws`?

A1: `throw` is a statement that throws one exception object at that point in the code, for example
`throw new IllegalStateException("closed")`. `throws` is a clause in a method or constructor declaration that lists the
checked exceptions the method may propagate to its caller, for example `void read() throws IOException`. A method can
use `throw` without `throws` (for unchecked exceptions, or checked ones it catches itself), and can declare `throws`
without containing a `throw` (when it calls another method that throws).

Q2: What happens when an exception is thrown and never caught?

A2: The exception propagates up the call stack, running any `finally` blocks on the way. If no method on the stack
catches it, the thread terminates. The thread's uncaught-exception handler is called; the default one prints
`Exception in thread "main" ...` and the stack trace to standard error. When the `main` thread dies this way and no
other non-daemon thread is running, the JVM exits with a non-zero status.

Q3: Can you throw a checked exception from a method without declaring it?

A3: Not with a plain `throw`: the compiler reports "unreported exception X; must be caught or declared to be thrown".
The method must either catch it or declare it with `throws`. The usual alternative is to wrap it in an unchecked
exception, passing the original as the cause.

Q4: How can you preserve the original exception information when throwing a new custom exception?

A4: Use exception chaining: pass the caught exception to the new exception's `(message, cause)` constructor.

```java
try {
    // Some code that may throw an IOException
} catch (IOException e) {
    throw new CustomCheckedException("A custom error occurred", e);
}
```

The original exception is then available from `getCause()`, and the printed stack trace shows it in a `Caused by:`
section.

## Code Examples

- Test: [ExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemoTest.java)
- Source: [ExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/ExceptionDemo.java)
