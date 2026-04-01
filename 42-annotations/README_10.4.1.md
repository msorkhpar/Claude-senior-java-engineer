# 10.4.1. Built-in Annotations (@Override, @Deprecated, @SuppressWarnings)

## Concept Explanation

Java provides several built-in annotations in the `java.lang` and `java.lang.annotation` packages that serve as
metadata instructions for the compiler, runtime, and documentation tools. These annotations are the foundation of Java's
annotation system and are used extensively in everyday Java development.

**Real-world analogy**: Think of built-in annotations like standardized road signs. Just as a "STOP" sign universally
tells drivers to stop regardless of the country, built-in annotations like `@Override` universally tell the Java compiler
to verify a method correctly overrides a parent method. They are pre-defined, universally understood, and enforced by the
"traffic system" (the compiler or JVM).

The most commonly used built-in annotations include:

- **`@Override`** — Indicates a method overrides a superclass method or implements an interface method. The compiler
  generates an error if the method does not actually override anything.
- **`@Deprecated`** — Marks a program element as obsolete or discouraged. Since Java 9, it supports `since` and
  `forRemoval` attributes.
- **`@SuppressWarnings`** — Instructs the compiler to suppress specific warning categories (e.g., `"unchecked"`,
  `"deprecation"`, `"rawtypes"`).
- **`@FunctionalInterface`** — Ensures an interface qualifies as a functional interface (exactly one abstract method).
- **`@SafeVarargs`** — Suppresses unchecked warnings for methods or constructors with generic varargs parameters.
- **`@Native`** — Indicates a constant field may be referenced from native code.

## Key Points to Remember

1. `@Override` has **SOURCE** retention — it is discarded by the compiler and not present in bytecode or at runtime.
2. `@Deprecated` has **RUNTIME** retention — it can be read via reflection and is preserved in bytecode.
3. `@SuppressWarnings` has **SOURCE** retention — it only affects the compiler and is not present at runtime.
4. `@FunctionalInterface` has **RUNTIME** retention — it can be queried via reflection.
5. `@SafeVarargs` has **RUNTIME** retention and can only be applied to `static`, `final`, or `private` methods and constructors.
6. `@Override` prevents subtle bugs: without it, a typo in a method name creates a new method instead of overriding.
7. `@Deprecated(forRemoval = true)` generates a stronger warning than `@Deprecated` alone.
8. `@SuppressWarnings` accepts an array of strings: `@SuppressWarnings({"unchecked", "rawtypes"})`.

## Relevant Java 21 Features

- **`@Deprecated(since, forRemoval)`** — Enhanced in Java 9 with `since` (version string) and `forRemoval` (boolean)
  attributes, allowing more precise deprecation communication.
- **`@FunctionalInterface`** — Introduced in Java 8, remains essential for lambda expressions and method references
  through Java 21.
- **`@SafeVarargs`** — Since Java 9, also applicable to private instance methods (previously only static/final).
- **Pattern matching and sealed classes** — `@Override` is used with sealed class hierarchies and pattern matching for
  `switch` to ensure correct method signatures.
- Java 21 continues to use these annotations extensively in the standard library, with many legacy APIs marked
  `@Deprecated(forRemoval = true)`.

## Common Pitfalls and How to Avoid Them

1. **Forgetting `@Override` when overriding methods**: Without it, a signature mismatch silently creates a new method
   instead of overriding.
   ```java
   // BUG: Missing @Override — "euqals" is a typo, creates a new method
   public boolean euqals(Object other) { return true; }

   // FIX: Add @Override — compiler catches the typo
   @Override
   public boolean equals(Object other) { return true; }
   ```

2. **Suppressing too broadly with `@SuppressWarnings("all")`**: This hides all warnings, potentially masking real issues.
   ```java
   // BAD: Suppresses everything
   @SuppressWarnings("all")
   public void method() { ... }

   // GOOD: Suppress only the specific warning
   @SuppressWarnings("unchecked")
   public void method() { ... }
   ```

3. **Using `@Deprecated` without providing an alternative**: Always document the replacement in the Javadoc.
   ```java
   // BAD: No guidance for users
   @Deprecated
   public void oldMethod() { ... }

   // GOOD: Clear migration path
   /**
    * @deprecated Use {@link #newMethod()} instead.
    */
   @Deprecated(since = "2.0", forRemoval = true)
   public void oldMethod() { ... }
   ```

4. **Assuming `@Override` is available at runtime**: It has SOURCE retention and is discarded after compilation. You
   cannot check for it via reflection.

5. **Misusing `@SafeVarargs` on methods that actually do unsafe operations**: The annotation is a promise that the method
   is safe. Using it incorrectly suppresses valid heap pollution warnings.

## Best Practices and Optimization Techniques

1. **Always use `@Override`** on every method that overrides a superclass method or implements an interface method.
2. **Use `@Deprecated(since = "X.Y", forRemoval = true)`** to communicate timeline and urgency for migration.
3. **Pair `@Deprecated` with `@SuppressWarnings("deprecation")`** in calling code only when you have a justified reason
   to keep using the deprecated API temporarily.
4. **Use `@FunctionalInterface`** on all interfaces intended for lambda usage — it documents intent and prevents
   accidental addition of abstract methods.
5. **Minimize `@SuppressWarnings` scope** — apply at the narrowest scope (local variable > method > class).
6. **Use `@SafeVarargs` only when you are certain** the varargs parameter is not used in an unsafe way.

## Edge Cases and Their Handling

1. **`@Override` on interface default methods**: You can use `@Override` when a class overrides a default method from an
   interface.
2. **`@Deprecated` on a constructor**: Constructors can be deprecated, guiding users toward factory methods.
3. **`@SuppressWarnings` on a local variable declaration**: You can suppress warnings at the local variable level for
   minimal scope.
4. **`@FunctionalInterface` with methods from Object**: An interface with one abstract method plus `toString()`,
   `equals()`, or `hashCode()` is still functional — Object methods don't count.
5. **`@SafeVarargs` on a non-final instance method**: Prior to Java 9, this was a compilation error; since Java 9,
   private instance methods are also allowed.

## Interview-specific Insights

Interviewers commonly test:

- Whether candidates understand the **retention policies** of each built-in annotation
- The difference between `@Override` in classes vs. interfaces
- Why `@Override` is important for correctness (the classic `equals` typo question)
- The semantics of `@Deprecated(forRemoval = true)` vs. `@Deprecated`
- When `@SuppressWarnings` is appropriate vs. when you should fix the underlying issue
- The relationship between `@FunctionalInterface` and lambda expressions

## Interview Q&A Section

**Q1: Why should you always use @Override when overriding a method?**

```text
A1: The @Override annotation serves as a compile-time safety check. Without it, if you make a
typo in the method name or get the parameter types wrong, the compiler will silently create a
new method instead of overriding the intended one. This is especially dangerous with equals()
and hashCode(), where a signature mismatch causes the method to never be called by collections
or other framework code.

With @Override, the compiler immediately flags the error:
- "Method does not override or implement a method from a supertype"

This is a zero-cost annotation (SOURCE retention) that prevents subtle, hard-to-debug issues.
```

```java
class Animal {
    public String speak() { return "..."; }
}

class Dog extends Animal {
    // Without @Override, this typo creates a new method silently
    // public String speck() { return "Woof!"; }

    // With @Override, the compiler catches the typo
    @Override
    public String speak() { return "Woof!"; }
}
```

**Q2: What is the difference between @Deprecated and @Deprecated(forRemoval = true)?**

```text
A2: Both mark an API element as deprecated, but they communicate different levels of urgency:

1. @Deprecated (or @Deprecated(forRemoval = false)):
   - Indicates the API is discouraged but may remain indefinitely
   - Generates a standard deprecation warning
   - Example: An older API that has a better alternative but won't be removed

2. @Deprecated(forRemoval = true):
   - Indicates the API WILL be removed in a future release
   - Generates a stronger "removal" warning (a different warning category)
   - The 'since' attribute documents when it was deprecated
   - Example: Security Manager APIs in recent Java versions

The 'since' attribute (a String) documents the version when deprecation occurred,
helping developers assess the urgency of migration.
```

```java
public class LegacyApi {
    // Will remain but is discouraged
    @Deprecated(since = "1.5")
    public int legacyCalculation(int a, int b) {
        return a + b;
    }

    // Will be removed — stronger warning
    @Deprecated(since = "2.0", forRemoval = true)
    public String oldMethod() {
        return "old result";
    }

    // The replacement
    public int modernCalculation(int a, int b) {
        return Math.addExact(a, b); // overflow-safe
    }
}
```

**Q3: What are the common warning categories for @SuppressWarnings?**

```text
A3: The most commonly used warning categories include:

1. "unchecked" — Suppresses unchecked cast warnings (e.g., casting raw types to generic types)
2. "deprecation" — Suppresses warnings when using deprecated APIs
3. "rawtypes" — Suppresses warnings about using raw types instead of parameterized types
4. "serial" — Suppresses warnings about missing serialVersionUID in Serializable classes
5. "unused" — Suppresses warnings about unused variables, imports, or methods
6. "all" — Suppresses ALL warnings (strongly discouraged)
7. "fallthrough" — Suppresses warnings about switch case fall-through
8. "restriction" — Suppresses warnings about using restricted/internal APIs

Best practice: Always use the narrowest category and the narrowest scope possible.
Apply @SuppressWarnings to the local variable or method, not the class.
```

```java
public class WarningExamples {
    // Suppressing a single warning type
    @SuppressWarnings("unchecked")
    public List<String> unsafeCast(Object obj) {
        return (List<String>) obj;
    }

    // Suppressing multiple warning types
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List rawToTyped(List rawList) {
        return new ArrayList<String>(rawList);
    }

    // Narrowest scope: suppress at local variable level
    public void example() {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) getObject();
    }
}
```

**Q4: Can @Override be detected at runtime via reflection?**

```text
A4: No. @Override has SOURCE retention policy, meaning it is discarded by the compiler and does
not appear in the compiled bytecode (.class file) or at runtime. This is by design — @Override
is purely a compile-time check with no runtime meaning.

The three retention policies are:
- SOURCE: Discarded by the compiler (e.g., @Override, @SuppressWarnings)
- CLASS: Retained in .class file but not loaded by the JVM (default if unspecified)
- RUNTIME: Retained and accessible via reflection (e.g., @Deprecated, @FunctionalInterface)

If you need to detect at runtime whether a method is an override, you must use reflection to
inspect the class hierarchy — check if a superclass or interface declares the same method
signature.
```

```java
import java.lang.reflect.Method;

public class OverrideCheck {
    public static void main(String[] args) throws Exception {
        Method speakMethod = Dog.class.getDeclaredMethod("speak");

        // This is ALWAYS false — @Override has SOURCE retention
        boolean hasOverride = speakMethod.isAnnotationPresent(Override.class);
        System.out.println("Has @Override: " + hasOverride); // false

        // To check if it's actually an override, inspect the superclass
        boolean isOverride = Dog.class.getSuperclass()
                .getDeclaredMethod("speak") != null;
        System.out.println("Is actually an override: " + isOverride); // true
    }
}
```

**Q5: What is @FunctionalInterface and when should you use it?**

```text
A5: @FunctionalInterface is a marker annotation that verifies an interface is a valid
functional interface — one with exactly one abstract method (SAM: Single Abstract Method).

When to use it:
1. On ANY interface intended to be used with lambda expressions
2. On interfaces that serve as types for method references
3. When you want to prevent accidental addition of abstract methods

What qualifies as a functional interface:
- Exactly one abstract method
- May have any number of default methods
- May have any number of static methods
- Methods inherited from Object (toString, equals, hashCode) don't count

The annotation is optional — an interface with one abstract method IS a functional interface
regardless. But the annotation:
- Documents intent clearly
- Causes a compilation error if the contract is broken
- Has RUNTIME retention, so it can be checked via reflection
```

```java
@FunctionalInterface
interface Transformer<T, R> {
    R transform(T input);  // The single abstract method

    // Default methods are allowed
    default <V> Transformer<T, V> andThen(Transformer<R, V> after) {
        return input -> after.transform(this.transform(input));
    }

    // Static methods are allowed
    static <T> Transformer<T, T> identity() {
        return input -> input;
    }
}

// Usage with lambda
Transformer<String, Integer> length = String::length;
Transformer<String, String> pipeline = length.andThen(i -> "Length: " + i);
System.out.println(pipeline.transform("hello")); // "Length: 5"
```

**Q6: What is @SafeVarargs and when is it appropriate to use?**

```text
A6: @SafeVarargs suppresses unchecked warnings related to varargs parameters with generic
types. When you declare a method like foo(List<String>... lists), the compiler warns about
potential "heap pollution" because Java's type erasure means the varargs array is actually
Object[] at runtime.

When to use @SafeVarargs:
- When the method only reads from the varargs array (safe)
- When the method creates a collection from the varargs elements (safe)

When NOT to use it:
- When the method stores into the varargs array with different types (unsafe)
- When the method exposes the varargs array to external code

Rules for application:
- Can be applied to: static methods, final methods, private methods (Java 9+), constructors
- Cannot be applied to: non-final, non-static, non-private instance methods
  (because they could be overridden unsafely)
```

```java
public class SafeVarargsExample {
    // SAFE: Only reads from the array
    @SafeVarargs
    public static <T> List<T> safeListOf(T... elements) {
        List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    // UNSAFE: Writes to the array — DO NOT use @SafeVarargs
    public static <T> void unsafeMethod(T... elements) {
        Object[] array = elements;
        array[0] = "String"; // Heap pollution if T is not String!
    }
}
```

## Code Examples

- Test: [BuiltInAnnotationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/BuiltInAnnotationsTest.java)
- Source: [BuiltInAnnotations.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/BuiltInAnnotations.java)
