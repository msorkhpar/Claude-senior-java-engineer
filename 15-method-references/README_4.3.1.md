# 4.3.1. Introduction to Method References

## Concept Explanation

Method references are a shorthand notation introduced in Java 8 for lambda expressions that do nothing but call an
existing method. They are a feature of functional programming in Java that lets you refer to methods or constructors
without actually invoking them — you capture a reference to an existing method and pass it around as a function object.

**Real-world analogy**: Think of a method reference like a bookmark on a page in a cookbook. Instead of copying the
entire recipe (writing out the lambda body), you just hand someone the bookmark and tell them, "follow the instructions
on that page." The person (the JVM) knows exactly what to do when the time comes. The bookmark doesn't execute the
recipe — it just references it.

Before method references, developers using lambdas would often write trivial wrappers like:

```java
list.forEach(s -> System.out.println(s));      // lambda that just calls one method
list.forEach(s -> s.toUpperCase());             // lambda that delegates to another
list.stream().map(s -> new StringBuilder(s));  // lambda that just calls a constructor
```

Method references simplify these to:

```java
list.forEach(System.out::println);
list.forEach(String::toUpperCase);
list.stream().map(StringBuilder::new);
```

The compiler translates a method reference into the same bytecode as the equivalent lambda. The key difference is
readability: method references are self-documenting because the method name communicates intent directly, whereas a
lambda body requires reading to understand.

### Relationship Between Lambdas and Method References

A method reference is syntactic sugar — every method reference can be rewritten as a lambda expression:

| Method Reference           | Equivalent Lambda                          |
|----------------------------|--------------------------------------------|
| `String::toUpperCase`      | `s -> s.toUpperCase()`                     |
| `System.out::println`      | `s -> System.out.println(s)`               |
| `Integer::parseInt`        | `s -> Integer.parseInt(s)`                 |
| `ArrayList::new`           | `() -> new ArrayList<>()`                  |
| `String::compareTo`        | `(s1, s2) -> s1.compareTo(s2)`             |

The reverse is NOT always true: not every lambda can be rewritten as a method reference. Lambdas that perform
calculations, combine calls, or include conditional logic cannot be expressed as method references.

### Syntax

The double-colon (`::`) operator separates the class or object from the method name:

```
ClassName::methodName      // Static or unbound instance method reference
objectReference::methodName  // Bound instance method reference
ClassName::new             // Constructor reference
```

No parentheses are used after the method name — that would invoke the method. The `::` syntax captures a reference,
not a result.

## Key Points to Remember

1. Method references use the `::` (double-colon) operator to separate the class/object from the method name.
2. They are syntactic sugar for lambdas that simply invoke a single method — no transformation, no logic.
3. There are four types: static method, bound instance method, unbound instance method, and constructor.
4. Method references are resolved at compile time; the compiler verifies that the referenced method signature matches
   the functional interface's abstract method signature.
5. Method references improve readability when the intent is clear from the method name alone.
6. They do NOT introduce new behavior — they reuse existing methods.
7. A method reference that calls a method that may throw a checked exception cannot be used with a functional interface
   that doesn't declare that exception.
8. Method references compose seamlessly with the Stream API and other functional interfaces.
9. The `::` syntax was deliberately chosen to avoid confusion with field access (`.`) and invocation (`()`).
10. Overloaded methods can cause ambiguity — the compiler picks based on the functional interface's target type.

## Relevant Java 21 Features

- **Java 8 (introduced)**: The `::` operator and all four method reference types were introduced in Java 8 alongside
  lambda expressions and the `java.util.function` package.
- **Java 8+**: Method references work with all standard functional interfaces in `java.util.function`:
  `Function`, `Consumer`, `Supplier`, `Predicate`, `BiFunction`, `Comparator`, etc.
- **Java 11**: `var` in lambda parameters (not directly for method references, but for contexts where method references
  are used).
- **Java 14+**: Pattern matching and records integrate cleanly with method references — record accessor methods
  (`Person::name()`) can be used as method references.
- **Java 16+**: `Stream.toList()` and other convenience terminal operations pair naturally with method references.
- **Java 21**: Virtual threads and structured concurrency commonly use `Thread.ofVirtual()::start` and similar
  method references for cleaner concurrent code.
- **Java 21**: Pattern matching in switch combined with method references enables concise dispatch patterns.

## Common Pitfalls and How to Avoid Them

1. **Confusing bound and unbound instance method references**: The same method (`String::toUpperCase`) can mean
   different things depending on context.

   ```java
   // Problem: unclear whether this is bound (specific instance) or unbound (on each element)
   String s = "hello";
   Supplier<String> bound = s::toUpperCase;       // bound: always operates on 's'
   Function<String, String> unbound = String::toUpperCase; // unbound: operates on each passed string
   ```

   ```java
   // Fix: choose the right type based on whether you have a specific instance
   // Bound: use when you have a specific object
   String prefix = "Hello ";
   Supplier<String> supplier = prefix::toUpperCase;  // always returns "HELLO "
   // Unbound: use when each element should be transformed
   List<String> result = List.of("a","b").stream().map(String::toUpperCase).toList();
   ```

2. **Method references to overloaded methods causing ambiguity**:

   ```java
   // Problem: println is overloaded — which overload does the compiler pick?
   Consumer<Object> printer = System.out::println; // OK — unambiguous here
   // But when the target type is ambiguous, compiler may reject it
   ```

   ```java
   // Fix: cast to the specific functional interface or use explicit lambda with a cast
   Consumer<String> stringPrinter = System.out::println; // explicitly Consumer<String>
   Consumer<Integer> intPrinter = System.out::println;   // explicitly Consumer<Integer>
   ```

3. **Using method references with checked exceptions**: Standard functional interfaces don't declare checked
   exceptions, so a method reference to a method that throws one won't compile.

   ```java
   // Problem: Files::delete throws IOException, which Consumer<Path> doesn't declare
   List<Path> paths = List.of(Path.of("/tmp/a"), Path.of("/tmp/b"));
   // paths.forEach(Files::delete); // Compile error!
   ```

   ```java
   // Fix: wrap in a lambda with try-catch, or create a custom functional interface
   paths.forEach(p -> {
       try { Files.delete(p); }
       catch (IOException e) { throw new UncheckedIOException(e); }
   });
   ```

4. **Null receiver for bound method references**: If the instance in a bound method reference is null, an NPE
   occurs at the time of method reference creation (for bound references where the receiver is evaluated eagerly).

   ```java
   // Problem: if formatter is null, NPE at method reference creation time
   DateTimeFormatter formatter = null;
   Function<LocalDate, String> fn = formatter::format; // NPE here!
   ```

   ```java
   // Fix: null-check before creating the method reference
   Objects.requireNonNull(formatter, "formatter must not be null");
   Function<LocalDate, String> fn = formatter::format;
   ```

5. **Assuming method references always have better performance**: Method references and their equivalent lambdas
   compile to essentially the same bytecode. Choosing between them should be based on readability, not performance.

## Best Practices and Optimization Techniques

1. **Prefer method references over trivial lambdas**: If a lambda does nothing but call a single method, replace it
   with a method reference. `list.forEach(System.out::println)` vs `list.forEach(s -> System.out.println(s))`.

2. **Name your helper methods meaningfully**: The readability benefit of method references is only realized when the
   method name is descriptive. `users.stream().filter(User::isActive)` is clear; `users.stream().filter(User::x)`
   is not.

3. **Use constructor references for factory-method patterns**: `Stream.of("a","b","c").map(StringBuilder::new)` is
   idiomatic and more readable than `Stream.of("a","b","c").map(s -> new StringBuilder(s))`.

4. **Avoid method references when parameters need transformation**: If you need to adapt the parameter before
   calling the method (e.g., convert a type, validate, add default values), a lambda is more appropriate.

5. **Combine with `Comparator.comparing()`**: This is one of the most common and readable uses of method references:
   `Comparator.comparing(Person::getName)` vs `Comparator.comparing(p -> p.getName())`.

6. **Consider method references in tests**: Method references like `assertThat(result).allSatisfy(String::isEmpty)`
   make test assertions highly readable.

## Edge Cases and Their Handling

1. **Method reference to an inherited method**: If a subclass inherits a method from a superclass, you can use
   either `SubClass::method` or `SuperClass::method` — they resolve to the same method unless overridden.

   ```java
   class Animal { String speak() { return "..."; } }
   class Dog extends Animal { }
   Function<Dog, String> f = Dog::speak; // Valid, resolves to Animal::speak
   ```

2. **Method reference to a default interface method**: Default methods in interfaces can also be used as method
   references through the implementing type.

   ```java
   // Iterable.forEach is a default method, but we reference it via the concrete type
   Consumer<List<String>> printer = list -> list.forEach(System.out::println);
   ```

3. **Generic methods as method references**: When the method is generic and the target type provides enough
   information for type inference, method references work cleanly.

   ```java
   // Collections.emptyList() is generic — target type infers the type parameter
   Supplier<List<String>> emptyListSupplier = Collections::emptyList; // List<String> inferred
   ```

4. **Varargs methods as method references**: Methods that accept varargs work as method references, but the compiler
   generates an array-creation call under the hood.

   ```java
   // String.format is varargs; it works as a BiFunction<String,Object[],String> but is rarely used that way
   // More commonly used with printf(String, Object...)
   ```

5. **Method references and `null` in the stream**: Method references don't automatically handle null elements in
   a stream. Always filter nulls first or use `Objects::nonNull`.

   ```java
   List<String> withNulls = Arrays.asList("a", null, "b");
   // withNulls.stream().map(String::toUpperCase).toList(); // NPE on null!
   withNulls.stream().filter(Objects::nonNull).map(String::toUpperCase).toList(); // Safe
   ```

## Interview-specific Insights

Interviewers focus on:

- Ability to identify when a lambda can be replaced by a method reference (signal of Java fluency).
- Understanding the four distinct types of method references and how to choose between them.
- The difference between bound vs. unbound instance method references — this trips up many candidates.
- How method references interact with overloaded methods and how the compiler resolves ambiguity.
- Recognizing that method references are compiled identically to their lambda equivalents.
- Real-world use cases, especially in Stream pipelines, `Comparator.comparing`, and `Optional.map`.

**Common tricky interview questions:**

- "What is the difference between `String::toUpperCase` as a `Function<String, String>` and as a `Supplier<String>`
  when `str` is a bound instance?"
- "Can a method reference throw a checked exception? If so, when is that allowed?"
- "How does the compiler resolve the overload in `System.out::println` when the target type is `Consumer<Integer>`?"
- "Write the equivalent lambda for `ClassName::instanceMethod` when used as a `BiFunction`."

## Interview Q&A Section

**Q1: What is a method reference and how does it differ from a lambda expression?**

```text
A1: A method reference is a shorthand syntax that allows you to refer to an existing method
(or constructor) without invoking it. It uses the double-colon (::) operator.

A lambda expression is a more general construct that can contain arbitrary logic. A method
reference is a special case of a lambda that delegates to a single, already-defined method.

Key differences:
1. Syntax: method references use ClassName::methodName; lambdas use (params) -> body.
2. Reuse: method references reuse existing code; lambdas define new inline behavior.
3. Readability: method references communicate intent through the method name; lambdas require
   reading the body.
4. Bytecode: both are compiled using invokedynamic — the resulting bytecode is equivalent.
5. Flexibility: lambdas can contain multiple statements, conditionals, and complex logic;
   method references are limited to single-method delegation.

When to prefer method references over lambdas:
- When the lambda body consists of a single method call that maps naturally to the target interface
- When the method name clearly communicates the intent
- When the method is already defined and tested elsewhere
```

```java
// Lambda
Function<String, Integer> lambdaLength = s -> s.length();

// Method reference (equivalent — preferred)
Function<String, Integer> refLength = String::length;

// More complex lambda — cannot be expressed as a method reference
Function<String, Integer> complexFn = s -> s.trim().length() * 2; // must stay as lambda
```

---

**Q2: What is the `::` operator and what does it produce?**

```text
A2: The :: (double-colon) operator is the method reference operator introduced in Java 8.
It separates the object/class (left side) from the method name (right side).

What it produces: The :: operator does NOT call the method. It creates a functional interface
instance whose abstract method is bound to (or delegates to) the referenced method. The actual
method invocation happens later, when the functional interface's abstract method is called.

The compiler uses the target type (the functional interface the method reference is assigned to
or passed to) to determine how to bind the method reference. The method signature must be
compatible with the functional interface's abstract method.

Important: no parentheses follow the method name after :: — that would invoke the method.
```

```java
// :: creates a Function<String, Integer> — method is NOT called yet
Function<String, Integer> fn = String::length;

// The method is called here, when apply() is invoked
int len = fn.apply("hello"); // 5

// Compare with direct invocation (no method reference):
int directLen = "hello".length(); // called immediately, result is 5

// :: on a constructor — creates a Supplier<ArrayList>
Supplier<ArrayList<String>> listFactory = ArrayList::new;
ArrayList<String> list = listFactory.get(); // constructor called here
```

---

**Q3: Can a method reference throw a checked exception? Give an example.**

```text
A3: A method reference can refer to a method that throws a checked exception. However, whether
it compiles depends on the target functional interface.

Rule: A method reference is only compatible with a functional interface if the functional
interface's abstract method declares (throws) all checked exceptions that the referenced method
may throw, OR if the referenced method throws no checked exceptions.

Standard functional interfaces (Consumer, Function, Predicate, etc.) do NOT declare checked
exceptions. Therefore, a method reference to a method that throws a checked exception is
incompatible with them.

To use such a method reference, you need a custom functional interface that declares the
checked exception, or you must wrap the call in a lambda with a try-catch.
```

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Custom functional interface declaring IOException
@FunctionalInterface
interface CheckedConsumer<T> {
    void accept(T t) throws IOException;
}

public class CheckedExceptionDemo {

    // Method that throws a checked exception
    static void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    public static void main(String[] args) {
        // This compiles because CheckedConsumer declares IOException
        CheckedConsumer<Path> ref = CheckedExceptionDemo::deleteFile; // OK

        // This does NOT compile: Consumer<Path> doesn't declare IOException
        // Consumer<Path> consumer = CheckedExceptionDemo::deleteFile; // compile error!

        // Workaround with a lambda wrapper:
        List<Path> paths = List.of(Path.of("/tmp/test.txt"));
        paths.forEach(p -> {
            try { deleteFile(p); }
            catch (IOException e) { throw new RuntimeException(e); }
        });
    }
}
```

---

**Q4: How does the compiler resolve an overloaded method reference?**

```text
A4: When a method is overloaded and used as a method reference, the compiler resolves the
overload based on the target type (the functional interface the method reference is assigned
to or passed to). The functional interface's abstract method signature serves as the type
context.

The compiler applies the same overload resolution rules as for normal method calls, using
the parameter types of the functional interface as the argument types.

If the target type is ambiguous (e.g., the method reference is passed to a method that
accepts multiple overloaded functional interfaces), the compiler reports an ambiguity error
and you must cast to the specific functional interface to disambiguate.
```

```java
import java.util.function.Consumer;

// System.out.println is overloaded for String, int, long, Object, etc.
// The compiler picks the right overload based on target type:

Consumer<String>  sc = System.out::println;  // resolves to println(String)
Consumer<Integer> ic = System.out::println;  // resolves to println(int) via unboxing
Consumer<Object>  oc = System.out::println;  // resolves to println(Object)

// Ambiguity example:
// If a method accepts both Consumer<String> and Consumer<Object>,
// the compiler cannot pick the right overload:
// ambiguousMethod(System.out::println); // compile error: ambiguous

// Fix: cast explicitly
// ambiguousMethod((Consumer<String>) System.out::println);
```

---

**Q5: What is the difference between a bound and an unbound instance method reference?**

```text
A5: Both types use the :: syntax with an instance method name, but they differ in how the
receiver (the object on which the method is called) is determined:

BOUND instance method reference (objectInstance::methodName):
- The receiver object is FIXED at the time the method reference is created.
- The method is always called on that specific object.
- The functional interface's abstract method takes the SAME parameters as the referenced method.
- Used as: Supplier, Consumer, Function (with just the method's own arguments)

UNBOUND instance method reference (ClassName::instanceMethodName):
- The receiver object is the FIRST argument supplied when the method is invoked.
- The method is called on whatever object is passed at invocation time.
- The functional interface's abstract method takes the receiver AS THE FIRST PARAMETER,
  followed by the method's own parameters.
- Used as: Function, BiFunction, Predicate, Comparator, etc.

This distinction is critical for interviews and is a common source of confusion.
```

```java
// BOUND: specific instance is captured
String greeting = "Hello, World!";
Supplier<String> bound = greeting::toUpperCase;
// Every call to bound.get() operates on the same 'greeting' object
System.out.println(bound.get()); // "HELLO, WORLD!" — always

// UNBOUND: receiver is supplied at call time
Function<String, String> unbound = String::toUpperCase;
// The String passed to unbound.apply() is the receiver
System.out.println(unbound.apply("hello"));   // "HELLO"
System.out.println(unbound.apply("goodbye")); // "GOODBYE"

// BOUND with a specific comparator object
java.util.Comparator<String> cmp = String.CASE_INSENSITIVE_ORDER;
java.util.function.ToIntBiFunction<String,String> boundCompare = cmp::compare;
System.out.println(boundCompare.applyAsInt("abc", "ABC")); // 0

// UNBOUND: String::compareTo — first String arg is receiver, second is argument
java.util.Comparator<String> unboundCompare = String::compareTo;
System.out.println(unboundCompare.compare("apple", "banana")); // negative
```

---

**Q6: How do method references improve code in a Streams pipeline? Give a real example.**

```text
A6: Method references improve stream pipelines by:
1. Removing boilerplate — eliminating trivial lambda wrappers
2. Improving readability — method names communicate intent better than lambda bodies
3. Encouraging code reuse — existing utility methods become first-class stream operations
4. Making chains more scannable — the pipeline reads as a sequence of named operations

A well-written stream pipeline with method references reads almost like a sentence describing
what the code does, without describing HOW (which is handled by the referenced methods).
```

```java
import java.util.*;
import java.util.stream.*;
import java.util.function.*;

record Employee(String name, String department, double salary, boolean active) {}

public class StreamMethodRefDemo {

    public static List<String> getTopEarnerNames(List<Employee> employees) {
        // With lambdas (harder to scan):
        return employees.stream()
                .filter(e -> e.active())
                .sorted((e1, e2) -> Double.compare(e2.salary(), e1.salary()))
                .limit(5)
                .map(e -> e.name())
                .collect(Collectors.toList());
    }

    public static List<String> getTopEarnerNamesRefactored(List<Employee> employees) {
        // With method references (reads like a description):
        return employees.stream()
                .filter(Employee::active)                          // filter active
                .sorted(Comparator.comparingDouble(Employee::salary).reversed()) // sort by salary desc
                .limit(5)
                .map(Employee::name)                               // extract name
                .collect(Collectors.toList());
    }

    public static Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        return employees.stream()
                .filter(Employee::active)
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.averagingDouble(Employee::salary)
                ));
    }

    public static void main(String[] args) {
        // Without method references — possible, but noisier
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        names.stream()
                .map(s -> s.toLowerCase())  // lambda
                .forEach(s -> System.out.println(s)); // lambda

        // With method references — idiomatic Java 8+
        names.stream()
                .map(String::toLowerCase)    // method reference
                .forEach(System.out::println); // method reference
    }
}
```

---

**Q7: How do you handle null safety when using method references in streams?**

```text
A7: Method references do not automatically guard against null values — if a null element
flows into a method reference that calls an instance method on it, you get a NullPointerException.

Common strategies for null safety:
1. Pre-filter with Objects::nonNull before applying instance method references
2. Use Optional::ofNullable in map before operating on potentially null results
3. Prefer static utility methods (like Objects::toString) that handle nulls explicitly
4. Filter nulls out early in the pipeline so the rest of the pipeline is null-safe

The method reference Objects::nonNull is itself extremely useful as a filter predicate.
```

```java
import java.util.*;
import java.util.stream.*;

public class NullSafeMethodRefDemo {

    record User(String name, String email) {}

    public static List<String> getEmailsNullSafe(List<User> users) {
        return users.stream()
                .filter(Objects::nonNull)          // filter null users
                .map(User::email)                  // extract email (may be null)
                .filter(Objects::nonNull)           // filter null emails
                .map(String::toLowerCase)          // safe: all non-null here
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<User> users = Arrays.asList(
                new User("Alice", "alice@example.com"),
                null,                               // null user
                new User("Bob", null),              // null email
                new User("Charlie", "CHARLIE@example.com")
        );

        List<String> emails = getEmailsNullSafe(users);
        // ["alice@example.com", "charlie@example.com"]
        System.out.println(emails);
    }
}
```

## Code Examples

- Source: [MethodReferenceIntro.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceIntro.java)
- Test: [MethodReferenceIntroTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceIntroTest.java)
