# 3.2.1. Creating Custom Exception Classes

## Concept Explanation

Custom exceptions in Java allow developers to create specific error types tailored to their application's needs. These
exceptions can carry additional information and provide more meaningful error messages than a general-purpose exception
such as `RuntimeException`.

A custom exception is an ordinary class in the `Throwable` hierarchy. To create one:

1. Create a new class that extends either `Exception` (for a checked exception) or `RuntimeException` (for an unchecked
   exception). Which one to choose is the subject of [3.2.4](README_3.2.4.md).
2. Implement constructors that pass the message and, where there is one, the cause up to the superclass.
3. Optionally, add fields and getters that carry extra information about the error.

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

### The standard constructors

`Throwable` stores two things that every exception should be able to carry: a detail message and a cause (the
exception that led to this one). `Exception` and `RuntimeException` each offer four public constructors for them, and a
custom exception usually mirrors the ones it needs:

```java
public class ReportGenerationException extends Exception {
    public ReportGenerationException() {
        super();
    }

    public ReportGenerationException(String message) {
        super(message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportGenerationException(Throwable cause) {
        super(cause); // the message becomes cause.toString()
    }
}
```

The `(message, cause)` constructor is the important one: without it, code that catches a low-level exception and
throws yours cannot keep the original exception as the cause, and its stack trace is lost.

### Adding context

A custom exception can carry data that the handler needs, not just a string. Make such fields `final` and set them in
the constructor, so the exception is immutable once thrown:

```java
public class FileProcessingException extends Exception {
    private final String fileName;

    public FileProcessingException(String message, String fileName, Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
```

A handler can then react to `e.getFileName()` instead of parsing the message text.

## Key Points to Remember

1. A custom exception extends `Exception` (checked) or `RuntimeException` (unchecked). Do not extend `Throwable` or
   `Error` directly: `catch (Exception e)` does not catch them, and `Error` is meant for JVM-level problems.
2. Pass the message and the cause to the superclass constructor; do not store them in your own fields.
3. Extra fields should be `final` and exposed through getters.
4. `Throwable` implements `Serializable`, so every exception class is serializable. Declare a
   `private static final long serialVersionUID` if the exception may be serialized (for example by RMI or a
   distributed cache); `javac -Xlint:serial` warns when it is missing. Fields that are not serializable make
   serialization fail at run time.
5. An exception class cannot be generic: `class GenericException<T> extends Exception` is a compile error ("a generic
   class may not extend java.lang.Throwable"), because a `catch` clause cannot check a type argument at run time.
6. A record cannot be an exception: records implicitly extend `java.lang.Record` and cannot extend another class.

## Relevant Java Features

- **Java 1.4**: exception chaining — the `(message, cause)` and `(cause)` constructors and `getCause()`.
- **Java 7**: `Throwable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)`, a
  protected constructor also available in `Exception` and `RuntimeException`. A subclass can pass
  `writableStackTrace = false` to skip capturing the stack trace.
- **Java 17**: sealed classes (JEP 409) can close an exception hierarchy, so the compiler knows every subtype.
- **Java 21**: pattern matching for `switch` (JEP 441) can then handle a sealed exception hierarchy exhaustively,
  without a `default` branch.

```java
public sealed abstract class PaymentException extends Exception
        permits CardDeclinedException, PaymentTimeoutException {
    protected PaymentException(String message) {
        super(message);
    }
}

public final class CardDeclinedException extends PaymentException {
    public CardDeclinedException(String message) {
        super(message);
    }
}

public final class PaymentTimeoutException extends PaymentException {
    public PaymentTimeoutException(String message) {
        super(message);
    }
}

// Exhaustive: no default branch needed, and adding a new permitted subclass is a compile error here
static String describe(PaymentException e) {
    return switch (e) {
        case CardDeclinedException d -> "declined: " + d.getMessage();
        case PaymentTimeoutException t -> "retry later";
    };
}
// describe(new CardDeclinedException("insufficient limit")) returns "declined: insufficient limit"
```

## Common Pitfalls and How to Avoid Them

1. **No cause constructor**: the exception cannot wrap the original failure, so the root cause disappears from logs.
   Always provide `(String message, Throwable cause)` when the exception may be thrown from a `catch` block.
2. **Duplicating the message in a field**: storing `message` in your own field and overriding `getMessage()` is
   unnecessary; `super(message)` already does it, and `toString()` and stack traces use it.
3. **Mutable context fields**: an exception can be caught, logged and rethrown by several layers. Mutable fields let one
   layer change what another reads. Use `final` fields.
4. **Extending the wrong class**: extending `Exception` when every caller would just wrap it in a `RuntimeException`
   adds boilerplate without value; extending `RuntimeException` for a condition callers must handle hides it from the
   compiler. See [3.2.4](README_3.2.4.md).

## Best Practices

1. End the class name with `Exception` and name the problem, not the place: `InsufficientFundsException`, not
   `BankAccountException2`.
2. Provide the constructors the exception needs, and always the `(message, cause)` one.
3. Carry context as typed, `final` fields with getters.
4. Consider one base exception per module or domain (for example `PaymentException`) with specific subclasses, so
   callers can catch either the family or one case.

## Edge Cases and Their Handling

1. **An exception without a stack trace**: for an exception that is thrown very often and whose stack trace is never
   read (a validation failure mapped straight to a response, for example), the protected four-argument constructor can
   switch the stack trace off. Only do this when profiling shows exception creation is a real cost.

   ```java
   public class ValidationException extends RuntimeException {
       public ValidationException(String message) {
           super(message, null, false, false); // no suppression, no stack trace
       }
   }
   // new ValidationException("bad").getStackTrace().length == 0
   ```

2. **Setting the cause later**: `initCause(Throwable)` sets the cause after construction, for exception classes that
   have no cause constructor. It can be called only once, and not at all if a constructor already set the cause; a
   second call throws `IllegalStateException`.

## Interview-specific Insights

- Be ready to write a custom exception with the standard constructors from memory.
- Know why the `(message, cause)` constructor matters (exception chaining keeps the root cause).
- Know the language restrictions: no generic exception classes, and records cannot be exceptions.
- Be able to explain how a sealed exception hierarchy works with an exhaustive `switch` in Java 21.

## Interview Q&A

Q1: How would you create a custom checked exception in Java?

A1: Extend `Exception` (not `RuntimeException`) and provide the constructors callers need, passing the message and cause
to the superclass:

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

A custom unchecked exception looks the same but extends `RuntimeException`.

Q2: Why can't an exception class be generic?

A2: The compiler rejects `class MyException<T> extends Exception` with "a generic class may not extend
java.lang.Throwable". A `catch` clause selects a handler by the exception's run-time class, and because of type erasure
`MyException<String>` and `MyException<Integer>` would be the same class at run time, so `catch (MyException<String> e)`
could never be checked. Carry the varying data in a field of type `Object` or of a common supertype instead.

Q3: How do you add extra information to a custom exception?

A3: Add `final` fields, set them in the constructor alongside `super(message)` or `super(message, cause)`, and expose
them with getters. For example, `FileProcessingException` in this module's code carries the `fileName` that failed, so
a handler can use it directly instead of parsing the message.

Q4: Should a custom exception declare a `serialVersionUID`?

A4: Every exception is `Serializable`, because `Throwable` implements it. If instances may be serialized (remote calls,
some caches and message brokers), declare `private static final long serialVersionUID = 1L;` so that recompiling the
class does not change its computed serial version, and keep extra fields serializable. `javac -Xlint:serial` reports
exception classes that lack it.

## Code Examples

- Test: [CustomExceptionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemoTest.java)
- Source: [CustomExceptionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/exceptions/CustomExceptionDemo.java)
