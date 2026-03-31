# 7.1.1. Java 8 (LTS)

## Concept Explanation

Java 8, released in March 2014, is one of the most transformative releases in Java's history. It introduced fundamental
paradigm shifts that moved Java from a purely object-oriented language into one that embraces functional programming
concepts. Java 8 remains the baseline for many enterprise applications and is still the most widely deployed Java version
in production environments.

**Real-world analogy**: Think of Java 8 as the moment a traditional workshop added power tools. The craftsmen (developers)
could still use their hand tools (imperative programming), but the new power tools (lambdas, streams, Optional) let them
accomplish the same tasks faster, more elegantly, and with less effort. The workshop's fundamental nature didn't change --
it just became dramatically more productive.

The five pillars of Java 8 are:
1. **Lambda Expressions and Functional Interfaces** -- enabling behavior parameterization
2. **Stream API** -- declarative data processing pipelines
3. **Default and Static Methods in Interfaces** -- evolving interfaces without breaking implementations
4. **Optional Class** -- explicit handling of absent values
5. **New Date and Time API** -- replacing the broken `java.util.Date` and `Calendar`

## Key Points to Remember

- Lambda expressions provide concise syntax for anonymous functions and only work with functional interfaces (SAM types).
- The `@FunctionalInterface` annotation documents intent and causes compile-time enforcement.
- The Stream API separates *what* from *how* -- you declare transformations, not iteration logic.
- Streams are lazy: intermediate operations are not executed until a terminal operation is invoked.
- `Optional` is designed for return types, not for fields, parameters, or collections.
- Default methods in interfaces enable API evolution without breaking existing implementations.
- The diamond problem with default methods is resolved by requiring the implementing class to override.
- `java.time` is immutable and thread-safe, unlike `java.util.Date`.
- Method references (`Class::method`) are syntactic sugar for lambdas and improve readability.
- Parallel streams use the common ForkJoinPool and can cause contention if misused.

## Relevant Java 21 Features

Java 21 builds on every Java 8 foundation:

- **Lambda expressions** are now used pervasively with virtual threads (`Thread.ofVirtual().start(() -> ...)`) and structured concurrency.
- **Streams** gained `toList()` (Java 16), `mapMulti()` (Java 16), and work with record patterns in Java 21.
- **Functional interfaces** are used with pattern matching in switch expressions for cleaner dispatching.
- **Optional** received `isEmpty()` (Java 11), `stream()` (Java 9), and `or()` (Java 9).
- **Date/Time API** remains the standard; no replacement, only incremental additions like `InstantSource` (Java 17).
- **Default methods** continue to be the foundation for interface evolution (e.g., `SequencedCollection` in Java 21 adds default methods to existing collection interfaces).

## Common Pitfalls and How to Avoid Them

1. **Mutating state in lambdas**: Lambdas can only access effectively final local variables.
   ```java
   // WRONG: modifying local variable
   int count = 0;
   list.forEach(item -> count++); // Compilation error

   // FIX: use AtomicInteger or stream reduction
   long count = list.stream().count();
   ```

2. **Overusing parallel streams**: Parallel streams are not always faster and share the common ForkJoinPool.
   ```java
   // WRONG: parallelizing trivial work
   List<String> names = List.of("Alice", "Bob");
   names.parallelStream().forEach(System.out::println); // Overhead > benefit

   // FIX: only parallelize CPU-intensive work on large datasets
   largeList.parallelStream().map(this::expensiveComputation).collect(toList());
   ```

3. **Using Optional.get() without checking**: This defeats the purpose of Optional.
   ```java
   // WRONG
   Optional<String> opt = findUser(id);
   String name = opt.get(); // NoSuchElementException risk

   // FIX
   String name = opt.orElse("Unknown");
   String name = opt.orElseThrow(() -> new UserNotFoundException(id));
   ```

4. **Confusing `this` in lambdas vs anonymous classes**: In lambdas, `this` refers to the enclosing class.
   ```java
   // In lambda: this == enclosing instance
   Runnable r = () -> System.out.println(this.getClass()); // Enclosing class

   // In anonymous class: this == anonymous class instance
   Runnable r = new Runnable() {
       public void run() { System.out.println(this.getClass()); } // Anon class
   };
   ```

5. **Ignoring stream short-circuiting**: Operations like `findFirst()`, `anyMatch()` are short-circuiting.
   ```java
   // INEFFICIENT: processes entire stream
   boolean found = list.stream().filter(predicate).collect(toList()).size() > 0;

   // FIX: short-circuits on first match
   boolean found = list.stream().anyMatch(predicate);
   ```

## Best Practices and Optimization Techniques

1. **Prefer method references** over lambdas for improved readability: `String::toUpperCase` vs `s -> s.toUpperCase()`.
2. **Cache reusable lambdas** as static final fields to avoid repeated object creation.
3. **Use primitive stream specializations** (`IntStream`, `LongStream`, `DoubleStream`) to avoid autoboxing overhead.
4. **Chain Optional operations** instead of nested if-null checks: `opt.map(f).filter(p).orElse(default)`.
5. **Prefer `Collectors.toUnmodifiableList()`** (Java 10+) for immutable results.
6. **Use `DateTimeFormatter` as static constants** -- they are thread-safe and expensive to create.
7. **Always use `ZonedDateTime`** when dealing with time zones, never `LocalDateTime` + manual offset.
8. **Favor `Stream.of()` and `IntStream.range()`** over `Arrays.stream()` for clarity.

## Edge Cases and Their Handling

1. **Null elements in streams**: `filter(Objects::nonNull)` before processing.
2. **Empty Optional chaining**: `opt.flatMap(f).map(g).orElse(default)` safely handles absence at every step.
3. **Empty stream reductions**: `reduce()` without identity returns `Optional`, `reduce(identity, op)` returns identity for empty streams.
4. **Parallel stream ordering**: `forEachOrdered()` preserves encounter order; `forEach()` does not.
5. **LocalDate edge cases**: `LocalDate.of(2024, 2, 29)` is valid (leap year); `LocalDate.of(2023, 2, 29)` throws `DateTimeException`.
6. **Duration vs Period**: `Duration` measures time-based (hours, minutes, seconds), `Period` measures date-based (years, months, days).
7. **Time zone DST transitions**: `ZonedDateTime` correctly handles daylight saving gaps and overlaps.

## Interview-specific Insights

Interviewers frequently test:
- The difference between `map()` and `flatMap()` in both Stream and Optional
- When to use parallel streams (and when not to)
- How default methods resolve the diamond problem
- Why `Optional` should not be used for fields or method parameters
- The internal mechanics of lambda desugaring (`invokedynamic`)
- Thread-safety properties of `java.time` vs `java.util.Date`

Tricky topics:
- Explaining why `Comparator.comparing()` works with lambdas (target typing + type inference)
- The difference between `Collectors.toList()` and `Stream.toList()` (mutability guarantees)
- How `peek()` behaves with short-circuiting terminal operations

## Interview Q&A Section

**Q1: What are the main features introduced in Java 8 and why are they significant?**

```text
A1: Java 8 introduced five major features:

1. Lambda Expressions: Enable treating behavior as data, reducing boilerplate for anonymous
   inner classes and enabling functional programming patterns.

2. Stream API: Provides a declarative way to process collections using a pipeline of
   operations (filter, map, reduce). Supports lazy evaluation and parallel processing.

3. Default Methods: Allow adding new methods to interfaces without breaking existing
   implementations. This was essential for evolving the Collections API (e.g., adding
   forEach(), stream() to Iterable/Collection).

4. Optional: A container type that explicitly represents the presence or absence of a
   value, reducing NullPointerException occurrences.

5. Date and Time API (java.time): Replaces the broken java.util.Date and Calendar with
   immutable, thread-safe types like LocalDate, LocalTime, ZonedDateTime.

Significance: Java 8 transformed Java from a purely OOP language into one supporting
functional programming, dramatically improving code expressiveness and enabling modern
programming patterns.
```

```java
// Demonstrating all five features in one example
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class Java8AllFeatures {
    // Default method in interface
    interface Describable {
        String describe();
        default String describeUpperCase() {
            return describe().toUpperCase();
        }
    }

    record Person(String name, LocalDate birthDate) implements Describable {
        public String describe() { return name + " born " + birthDate; }
    }

    public static void main(String[] args) {
        List<Person> people = List.of(
            new Person("Alice", LocalDate.of(1990, 5, 15)),
            new Person("Bob", LocalDate.of(2005, 3, 20))
        );

        // Lambda + Stream + Optional
        Optional<String> firstAdult = people.stream()
            .filter(p -> p.birthDate().isBefore(LocalDate.now().minusYears(18)))
            .map(Person::describeUpperCase)  // method reference + default method
            .findFirst();

        System.out.println(firstAdult.orElse("No adults found"));
    }
}
```

**Q2: Explain the difference between `map()` and `flatMap()` in the Stream API.**

```text
A2: Both are intermediate operations that transform stream elements, but they differ in
how they handle the transformation result:

map(Function<T, R>):
- Applies the function to each element, producing one output per input.
- The result is a Stream<R>.
- Use when each element maps to exactly one result.

flatMap(Function<T, Stream<R>>):
- Applies the function to each element, where each element produces a Stream.
- All resulting streams are then "flattened" into a single Stream<R>.
- Use when each element maps to zero or more results (one-to-many transformation).

Common use case: flattening nested collections.
- map() on List<List<String>> produces Stream<List<String>>
- flatMap() on List<List<String>> produces Stream<String>

The same distinction applies to Optional:
- Optional.map() wraps the result in Optional -> Optional<Optional<T>> possible
- Optional.flatMap() expects the function to return Optional -> avoids nesting
```

```java
// map vs flatMap demonstration
List<List<String>> nested = List.of(
    List.of("a", "b"),
    List.of("c", "d"),
    List.of("e")
);

// map: Stream<List<String>> - NOT what we want
List<List<String>> mapped = nested.stream()
    .map(list -> list)
    .collect(Collectors.toList());
// Result: [[a, b], [c, d], [e]]

// flatMap: Stream<String> - flattened
List<String> flatMapped = nested.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());
// Result: [a, b, c, d, e]

// Optional flatMap example
Optional<String> userId = Optional.of("123");
// map would give Optional<Optional<User>>
// flatMap gives Optional<User>
Optional<User> user = userId.flatMap(id -> findUserById(id));
```

**Q3: When should you use parallel streams, and what are the dangers?**

```text
A3: Parallel streams split the data source and process chunks on multiple threads
using the common ForkJoinPool.

Use parallel streams when:
1. The dataset is large (typically > 10,000 elements).
2. The per-element processing is CPU-intensive.
3. The data source splits well (ArrayList, arrays split well; LinkedList does not).
4. The operations are stateless, non-interfering, and associative.
5. There is no shared mutable state.

Dangers and pitfalls:
1. Shared ForkJoinPool: All parallel streams in the JVM share the same pool
   (Runtime.getRuntime().availableProcessors() - 1 threads). A slow parallel stream
   can starve others.

2. Overhead: Thread coordination has overhead. For small datasets or trivial operations,
   sequential is faster.

3. Order sensitivity: Operations like forEach() do not preserve order in parallel.
   Use forEachOrdered() if order matters.

4. Mutable state: Accumulating into a shared collection (e.g., ArrayList) from parallel
   stream causes race conditions. Use collect() with thread-safe collectors instead.

5. Non-splittable sources: LinkedList, Stream.iterate(), and BufferedReader.lines()
   split poorly, making parallelism ineffective.

Rule of thumb: Measure before parallelizing. Use JMH benchmarks for critical paths.
```

```java
// WRONG: shared mutable state
List<Integer> results = new ArrayList<>();
IntStream.range(0, 10000).parallel().forEach(results::add); // Race condition!

// CORRECT: use collect
List<Integer> results = IntStream.range(0, 10000)
    .parallel()
    .boxed()
    .collect(Collectors.toList());

// Custom ForkJoinPool to avoid starving the common pool
ForkJoinPool customPool = new ForkJoinPool(4);
List<String> result = customPool.submit(() ->
    largeList.parallelStream()
        .map(this::expensiveOperation)
        .collect(Collectors.toList())
).get();
```

**Q4: How do default methods in interfaces solve the diamond problem?**

```text
A4: The diamond problem occurs when a class implements two interfaces that both provide
a default method with the same signature. Java resolves this with three rules:

1. Class wins: If the class provides an implementation, it takes priority over any
   default method.

2. Sub-interface wins: If one interface extends another and both define the same
   default method, the more specific (sub-interface) wins.

3. Explicit resolution required: If neither rule applies (two unrelated interfaces
   with same default method), the class MUST override the method and explicitly choose
   which to call using InterfaceA.super.method().

This design was necessary because Java 8 needed to add methods like forEach() and
stream() to existing collection interfaces without breaking millions of existing
implementations.
```

```java
interface A {
    default String greet() { return "Hello from A"; }
}

interface B {
    default String greet() { return "Hello from B"; }
}

// Compilation error without explicit override!
// class C implements A, B {} // ERROR: class C inherits unrelated defaults

// Must resolve explicitly
class C implements A, B {
    @Override
    public String greet() {
        // Choose one, combine, or provide entirely new implementation
        return A.super.greet() + " and " + B.super.greet();
    }
}

// Sub-interface rule
interface D extends A {
    @Override
    default String greet() { return "Hello from D"; }
}

class E implements A, D {
    // No override needed - D.greet() wins (more specific)
    // E.greet() returns "Hello from D"
}
```

**Q5: Why was Optional introduced, and what are the rules for using it properly?**

```text
A5: Optional was introduced to address the pervasive problem of NullPointerException
in Java code. It makes the possibility of absence explicit in the type system, forcing
callers to handle the absent case.

Rules for proper Optional usage:

DO:
1. Use as method return type when absence is a valid outcome.
2. Chain operations: map(), flatMap(), filter(), orElse(), orElseThrow().
3. Use orElseGet() with expensive default computations (lazy evaluation).
4. Use stream() to integrate with Stream pipelines (Java 9+).
5. Use isEmpty()/isPresent() for conditional logic.

DO NOT:
1. Use Optional as a field type (adds overhead, complicates serialization).
2. Use Optional as a method parameter (callers already handle null; use overloading).
3. Use Optional for collections (return empty collection instead).
4. Use Optional.get() without checking (defeats the purpose).
5. Use Optional.of(null) (throws NPE; use Optional.ofNullable()).
6. Create Optional just to chain .isPresent()/.get() (use if-null check instead).

The key insight: Optional is a RETURN TYPE signal that says "this method may not
produce a result." It is NOT a general-purpose null replacement.
```

```java
// Good: return type signaling possible absence
public Optional<User> findById(long id) {
    User user = database.lookup(id);
    return Optional.ofNullable(user);
}

// Good: chaining operations
String displayName = findById(id)
    .filter(User::isActive)
    .map(User::getDisplayName)
    .orElse("Anonymous");

// Good: flatMap for nested Optional
Optional<String> city = findById(id)
    .flatMap(User::getAddress)        // returns Optional<Address>
    .flatMap(Address::getCity);       // returns Optional<String>

// BAD: Optional as parameter
public void process(Optional<String> name) { } // Don't do this
// Better: overload
public void process(String name) { }
public void process() { } // no-name version

// BAD: Optional for collections
public Optional<List<Item>> getItems() { } // Don't do this
// Better: return empty collection
public List<Item> getItems() { return Collections.emptyList(); }
```

## Code Examples

- Test: [Java8FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java8FeaturesTest.java)
- Source: [Java8Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java8Features.java)
