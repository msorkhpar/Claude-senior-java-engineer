# 8.1.2. Open/Closed Principle (OCP)

## Concept Explanation

The Open/Closed Principle states that **software entities (classes, modules, functions) should be open for extension but closed for modification**. You should be able to add new behavior to a system without changing existing, tested, working code.

**Real-world analogy**: Think of a power strip. It is "closed" in the sense that its internal wiring is sealed and you never modify it. But it is "open" for extension -- you can plug in any appliance (lamp, computer, toaster) without rewiring the strip. The strip works with any device that conforms to the plug interface.

### 8.1.2.1. Definition and Purpose of OCP

Bertrand Meyer coined OCP in 1988. The principle recognizes that requirements change constantly, and modifying existing code introduces risk of breaking what already works. Instead, design systems so new requirements are met by adding new code (new classes, new implementations) rather than editing old code.

The purpose:
- Protect working, tested code from modification
- Enable new features through extension rather than editing
- Reduce regression risk when requirements evolve
- Support plug-in architectures and frameworks

### 8.1.2.2. Applying OCP in Concurrent Programming

In concurrent systems, OCP manifests through strategy patterns and pipeline designs:
- A `ConcurrentPipeline` accepts any `TaskProcessor` implementation
- New processing strategies can be added without touching the pipeline code
- Thread-safety logic in the pipeline remains stable as new processors are created

### 8.1.2.3. Extending Behavior Without Modifying Existing Code

The key mechanisms for OCP in Java:
1. **Polymorphism**: Define interfaces/abstract classes; add new implementations
2. **Decorator pattern**: Wrap existing objects to add behavior
3. **Strategy pattern**: Inject different algorithms at runtime
4. **Sealed classes with permits**: Controlled extension in Java 17+

### 8.1.2.4. OCP and Thread Safety

When OCP is applied correctly, thread-safety code is written once in the base framework and never modified:
- The pipeline/executor handles synchronization
- New task processors only implement business logic
- Decorators can add thread-safe caching or logging without touching the decorated class

## Key Points to Remember

- OCP is enabled by **abstraction**: program to interfaces, not implementations
- Adding a new feature should mean adding a new class, not editing an existing one
- OCP does not mean you never modify code -- it means core abstractions are stable
- In Java, interfaces, abstract classes, and sealed classes are the primary OCP tools
- The Decorator, Strategy, and Template Method patterns are classic OCP enablers
- OCP reduces the need for extensive regression testing when adding features

## Relevant Java 21 Features

- **Sealed classes and interfaces**: Provide controlled extension -- you define which classes can implement an interface, enabling exhaustive pattern matching while still being open for new permitted types
- **Records**: Ideal for creating new data-carrying implementations of interfaces
- **Pattern matching for switch**: Works well with sealed hierarchies to handle all subtypes without modifying existing switch logic (when new types are added, the compiler warns about missing cases)
- **Virtual threads**: Enable creating lightweight concurrent pipelines that are open for extension

## Common Pitfalls and How to Avoid Them

1. **Switch/if-else on type**: Adding a new type requires modifying every switch statement.
   ```java
   // BAD: closed for extension
   double calculateArea(String type, double... dims) {
       return switch (type) {
           case "circle" -> Math.PI * dims[0] * dims[0];
           case "rect" -> dims[0] * dims[1];
           default -> throw new IllegalArgumentException("Unknown: " + type);
       };
   }
   ```
   **Fix**: Use polymorphism -- each shape computes its own area.

2. **Modifying existing classes to add features**:
   ```java
   // BAD: editing existing validator to add new rule
   class Validator {
       boolean isValid(String s) {
           return s != null && !s.isEmpty() && s.length() >= 5; // keep adding conditions
       }
   }
   ```
   **Fix**: Use the Decorator pattern -- chain validators.

3. **Premature abstraction**: Creating interfaces before you have two implementations leads to over-engineering.
   **Fix**: Follow the "Rule of Three" -- abstract after the second or third concrete need.

## Best Practices and Optimization Techniques

1. Use interfaces to define extension points: `Shape`, `TaskProcessor`, `Validator`
2. Use the Decorator pattern to extend behavior without subclassing
3. Use factory methods or dependency injection to create implementations
4. Sealed interfaces provide a middle ground: open for extension within a defined set
5. Write unit tests against the interface, not the implementation -- tests remain valid as new implementations are added
6. In concurrent code, keep synchronization in the framework (pipeline/executor) and let extensions be pure logic

## Edge Cases and Their Handling

1. **Null inputs to calculators**: Validate collections before processing (`if (shapes == null) throw ...`)
2. **Empty collections**: `totalArea` of zero shapes should return `0.0`, not throw
3. **Zero-dimension shapes**: A circle with radius 0 is valid and has area 0
4. **Negative dimensions**: Validate in constructors; reject invalid shapes at creation time
5. **New shape types**: If using sealed classes, adding a new permitted type will cause compiler warnings in pattern matches -- this is a feature, not a bug

## Interview-specific Insights

Interviewers often focus on:
- Asking you to refactor a type-switching method into a polymorphic design
- Understanding the relationship between OCP and the Strategy/Decorator patterns
- Discussing real-world OCP applications (plugin systems, middleware chains, event handlers)
- Trade-offs: OCP vs. simplicity for small codebases

Common tricky questions:
- "How do sealed classes relate to OCP? Don't they restrict extension?"
- "Is OCP always desirable? When might you choose to modify existing code?"
- "How does OCP apply to database schemas and APIs?"

## Interview Q&A Section

**Q1: What is the Open/Closed Principle and how do you implement it in Java?**

```text
A1: The Open/Closed Principle states that software entities should be open for 
extension but closed for modification. In Java, this is primarily achieved through:

1. Interfaces and abstract classes: Define contracts that can have new implementations
2. Polymorphism: Code against interfaces so new types work automatically
3. Decorator pattern: Wrap existing objects to add behavior
4. Strategy pattern: Inject different algorithms via interfaces

The key insight is that when a new requirement arrives, you should be able to meet 
it by ADDING new code (a new class implementing an existing interface) rather than 
EDITING existing code.

This reduces regression risk, preserves existing tests, and allows parallel 
development -- one developer adds a new shape while another adds a new validator, 
with no merge conflicts.
```

```java
// OCP with interfaces
interface Shape {
    double area();
}

record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
}

record Rectangle(double w, double h) implements Shape {
    public double area() { return w * h; }
}

// Calculator is CLOSED for modification -- works with any Shape
class AreaCalculator {
    double totalArea(List<Shape> shapes) {
        return shapes.stream().mapToDouble(Shape::area).sum();
    }
}

// OPEN for extension: add Triangle without touching Calculator or existing shapes
record Triangle(double base, double height) implements Shape {
    public double area() { return 0.5 * base * height; }
}
```

**Q2: How does the Decorator pattern support OCP?**

```text
A2: The Decorator pattern supports OCP by allowing you to add behavior to objects 
without modifying their source code. Each decorator implements the same interface as 
the object it wraps, adding its own behavior before or after delegating to the 
wrapped object.

Benefits for OCP:
1. Each decorator is a new class (open for extension)
2. The original class is never modified (closed for modification)
3. Decorators can be composed in any combination
4. Each decorator is independently testable
5. New validation rules, logging, caching, etc., are added as new decorators

This is how java.io works: BufferedInputStream wraps FileInputStream wraps 
InputStream -- each adds behavior without modifying the others.
```

```java
interface Validator<T> {
    boolean isValid(T value);
}

// Base validator -- never modified
class NonNullValidator implements Validator<String> {
    public boolean isValid(String value) { return value != null; }
}

// Decorator 1 -- extends validation without modifying NonNullValidator
class NonEmptyValidator implements Validator<String> {
    private final Validator<String> delegate;
    NonEmptyValidator(Validator<String> delegate) { this.delegate = delegate; }
    public boolean isValid(String value) {
        return delegate.isValid(value) && !value.isEmpty();
    }
}

// Decorator 2 -- further extends without modifying anything
class MinLengthValidator implements Validator<String> {
    private final Validator<String> delegate;
    private final int min;
    MinLengthValidator(Validator<String> delegate, int min) {
        this.delegate = delegate;
        this.min = min;
    }
    public boolean isValid(String value) {
        return delegate.isValid(value) && value.length() >= min;
    }
}

// Compose: NonNull + NonEmpty + MinLength(5) -- no existing code modified
Validator<String> validator = new MinLengthValidator(
    new NonEmptyValidator(new NonNullValidator()), 5);
```

**Q3: How does OCP apply in concurrent programming?**

```text
A3: In concurrent programming, OCP is crucial because concurrent code is 
notoriously hard to modify safely. Once a thread-safe pipeline or executor is 
working correctly, you should avoid modifying its synchronization logic.

OCP in concurrency:
1. Define a TaskProcessor interface for business logic
2. The pipeline/executor handles thread management and synchronization (closed)
3. New processing strategies implement TaskProcessor (open for extension)
4. The pipeline doesn't know or care what the processor does

This separation means:
- Thread-safety bugs in the pipeline are fixed once
- New processors are pure business logic -- no synchronization needed
- Testing is simpler: test processors without threads, test pipeline with 
  simple mock processors
```

```java
// Strategy interface -- open for extension
interface TaskProcessor<T, R> {
    R process(T input);
}

// Thread-safe pipeline -- closed for modification
class ConcurrentPipeline<T, R> {
    private final TaskProcessor<T, R> processor;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<R> results = new ArrayList<>();

    ConcurrentPipeline(TaskProcessor<T, R> processor) {
        this.processor = processor;
    }

    R processItem(T input) {
        R result = processor.process(input);
        lock.lock();
        try { results.add(result); }
        finally { lock.unlock(); }
        return result;
    }
}

// Extension 1: square numbers
class SquareProcessor implements TaskProcessor<Integer, Long> {
    public Long process(Integer input) { return (long) input * input; }
}

// Extension 2: cube numbers -- no pipeline modification needed
class CubeProcessor implements TaskProcessor<Integer, Long> {
    public Long process(Integer input) { return (long) input * input * input; }
}
```

**Q4: How do sealed classes relate to OCP?**

```text
A4: Sealed classes seem to contradict OCP because they restrict which classes 
can extend them. However, they actually support a controlled form of OCP:

1. Within the sealed hierarchy, the set of subtypes is fixed and exhaustive
2. This enables the compiler to verify exhaustive pattern matching
3. New behavior can be added to existing types without modifying them (via 
   external visitors or pattern matching)
4. The sealed set can be expanded by adding new permitted types (though this 
   requires modifying the sealed declaration)

Sealed classes trade unlimited extension for safety guarantees:
- The compiler ensures all cases are handled
- Refactoring is safer because missing cases are caught at compile time
- Domain modeling is more precise (a Shape is ONLY Circle, Rectangle, or Triangle)

When to use sealed vs. open interfaces:
- Sealed: when the set of types is inherently bounded (HTTP methods, AST nodes)
- Open: when new types are expected (plugins, user-defined strategies)
```

```java
// Sealed hierarchy -- controlled extension
sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}

record Circle(double r) implements Shape { public double area() { return Math.PI * r * r; } }
record Rectangle(double w, double h) implements Shape { public double area() { return w * h; } }
record Triangle(double b, double h) implements Shape { public double area() { return 0.5 * b * h; } }

// Pattern matching with exhaustive check -- compiler ensures all cases handled
String describe(Shape s) {
    return switch (s) {
        case Circle c -> "Circle with radius " + c.r();
        case Rectangle r -> "Rectangle " + r.w() + "x" + r.h();
        case Triangle t -> "Triangle base=" + t.b();
        // No default needed -- compiler knows all cases are covered
    };
}
```

**Q5: When should you NOT apply OCP?**

```text
A5: OCP should not be applied in every situation:

1. Simple, stable code: If a class is small, well-understood, and rarely changes, 
   adding abstraction layers for OCP is over-engineering
2. Early development: During prototyping, modifying code is faster and cheaper 
   than building extension points that may not be needed
3. Performance-critical paths: Polymorphic dispatch (virtual method calls) has 
   small overhead that matters in tight loops
4. One-off scripts: Throwaway code doesn't benefit from extension points
5. When abstractions are unclear: Creating the wrong abstraction is worse than 
   no abstraction -- wait until patterns emerge

Rule of thumb: Apply OCP when you've seen the same class modified for the same 
kind of reason more than twice. The third time, refactor to be open for extension.

Remember: OCP is about minimizing risk of breaking working code. If the code 
isn't working yet or is changing rapidly, direct modification may be appropriate.
```

```java
// Over-engineering with OCP for simple code
// BAD: unnecessary abstraction for a one-liner
interface Greeter { String greet(String name); }
class EnglishGreeter implements Greeter {
    public String greet(String name) { return "Hello, " + name; }
}
class GreeterFactory {
    Greeter create(String lang) { return new EnglishGreeter(); }
}

// BETTER: just write the simple code
String greet(String name) { return "Hello, " + name; }

// Refactor to OCP later IF you actually need multiple languages:
// Map<String, Function<String, String>> greeters = Map.of(
//     "en", name -> "Hello, " + name,
//     "es", name -> "Hola, " + name
// );
```

## Code Examples

- Source: [OpenClosed.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/OpenClosed.java)
- Test: [OpenClosedTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/OpenClosedTest.java)
