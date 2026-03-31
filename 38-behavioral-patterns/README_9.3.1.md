# 9.3.1. Strategy Pattern

## Concept Explanation

The Strategy pattern is a behavioral design pattern that defines a family of algorithms, encapsulates each one in a separate class, and makes them interchangeable. The pattern lets the algorithm vary independently from the clients that use it.

**Real-world analogy**: Think of a GPS navigation app. When you ask for directions, you can choose different routing strategies: fastest route, shortest distance, avoid tolls, or scenic route. Each strategy calculates a route differently, but the app (the context) uses whichever one you select without knowing the internal details of the routing algorithm. You can switch strategies at any time without changing the app itself.

The Strategy pattern consists of three participants:
- **Strategy** (interface): declares the interface common to all supported algorithms.
- **ConcreteStrategy** (implementing class): implements the algorithm using the Strategy interface.
- **Context**: maintains a reference to a Strategy object and delegates algorithm execution to it.

### When to Use the Strategy Pattern

1. When you have multiple algorithms for a specific task and want to switch between them at runtime.
2. When you want to isolate the implementation details of an algorithm from the code that uses it.
3. When a class has a massive conditional statement that switches between variants of the same algorithm.
4. When you want to avoid exposing complex, algorithm-specific data structures to client code.

## Key Points to Remember

1. Strategy pattern follows the **Open/Closed Principle** -- you can add new strategies without modifying the context.
2. It eliminates conditional statements for algorithm selection (no more `if/else` or `switch` chains).
3. Strategies are **interchangeable at runtime** -- the context can change its behavior dynamically.
4. Each strategy encapsulates a **single algorithm** (Single Responsibility Principle).
5. The pattern introduces a **level of indirection** -- clients must be aware of different strategies.
6. In Java, **functional interfaces and lambdas** (Java 8+) provide a lightweight alternative to full strategy classes for simple algorithms.
7. **Sealed interfaces** (Java 17+) can restrict the set of permitted strategies for exhaustive pattern matching.

## Relevant Java 21 Features

- **Lambda expressions (Java 8+)**: For simple single-method strategies, lambdas eliminate the need for separate strategy classes. `UnaryOperator<String>`, `Comparator<T>`, `Predicate<T>`, and `Function<T,R>` are all functional interfaces that naturally model strategies.
- **Sealed interfaces (Java 17+)**: When the set of strategies is fixed and known, sealed interfaces allow exhaustive `switch` expressions with pattern matching, ensuring all strategies are handled at compile time.
- **Pattern matching for switch (Java 21)**: Combined with sealed strategy interfaces, pattern matching enables clean dispatching logic without `instanceof` chains.
- **Records (Java 16+)**: Strategy implementations that carry configuration data (e.g., a credit card number for a payment strategy) can be concisely expressed as records.
- **Method references (Java 8+)**: `String::toUpperCase` or `Integer::compare` can serve as strategy instances directly.

### Evolution Across Java Versions

| Version | Impact on Strategy Pattern |
|---------|----------------------------|
| Pre-Java 8 | Required separate classes or anonymous inner classes for each strategy |
| Java 8 | Lambdas and functional interfaces made simple strategies one-liners |
| Java 14+ | Records provide concise strategy implementations with data |
| Java 17+ | Sealed interfaces restrict permitted strategies for type safety |
| Java 21 | Pattern matching for switch enables exhaustive strategy dispatching |

## Common Pitfalls and How to Avoid Them

1. **Null strategy reference**: Forgetting to validate that the strategy is not null can lead to `NullPointerException` at runtime.
   ```java
   // Problem
   public class Sorter {
       private SortStrategy strategy; // could be null!
       public void sort(List<Integer> data) {
           strategy.sort(data); // NullPointerException if strategy is null
       }
   }
   
   // Fix: validate in constructor and setter
   public class Sorter {
       private SortStrategy strategy;
       public Sorter(SortStrategy strategy) {
           if (strategy == null) throw new IllegalArgumentException("Strategy cannot be null");
           this.strategy = strategy;
       }
   }
   ```

2. **Over-engineering with full strategy classes when lambdas suffice**: Creating a class hierarchy for trivial one-liner algorithms adds unnecessary complexity.
   ```java
   // Overkill for simple logic
   public class UpperCaseStrategy implements TextStrategy {
       public String process(String s) { return s.toUpperCase(); }
   }
   
   // Better: use a lambda
   UnaryOperator<String> upperCase = String::toUpperCase;
   ```

3. **Tight coupling between context and concrete strategies**: The context should depend on the strategy interface, never on concrete implementations.
   ```java
   // Problem: context depends on concrete class
   private BubbleSortStrategy strategy;
   
   // Fix: depend on the interface
   private SortStrategy strategy;
   ```

4. **Exposing strategy internals to clients**: Strategies should have a clean interface. Do not leak implementation details (internal data structures, intermediate state) through the strategy interface.

5. **Not considering thread safety when strategies are shared**: If a strategy holds mutable state and is shared across threads, race conditions can occur. Prefer stateless strategies or use synchronization.

## Best Practices and Optimization Techniques

1. **Prefer functional interfaces for simple strategies**: If the strategy has a single method with a common signature, use `Predicate<T>`, `Function<T,R>`, `Comparator<T>`, or `UnaryOperator<T>` instead of creating a custom interface.

2. **Cache stateless strategies as constants**: Stateless strategy instances can be reused as `static final` fields to avoid repeated object allocation.
   ```java
   public static final UnaryOperator<String> UPPER_CASE = String::toUpperCase;
   ```

3. **Use sealed interfaces when the strategy set is fixed**: This enables compile-time exhaustiveness checks with pattern matching.

4. **Make strategies immutable**: Strategies that carry configuration should be records or final classes with no mutable state.

5. **Compose strategies with `andThen()` / `compose()`**: Functional interfaces like `Function` support chaining, which lets you build complex strategies from simple ones.

6. **Document the contract**: Clearly document what the strategy interface expects (preconditions, postconditions, null handling) so that all implementations are consistent.

## Edge Cases and Their Handling

1. **Null input to strategy**: Always validate inputs. Strategies should throw `IllegalArgumentException` for null data.
2. **Empty collections**: Sorting strategies should handle empty lists gracefully (return empty list, not throw).
3. **Single-element collections**: Ensure sorting works correctly for lists of size 1.
4. **Strategy switching during processing**: If the context is used concurrently, switching strategies mid-operation can produce inconsistent results. Use synchronization or make the context immutable.
5. **Strategy with side effects**: If a strategy modifies external state, document this clearly. Prefer pure functions.

## Interview-specific Insights

Interviewers often focus on:
- The difference between Strategy and other behavioral patterns (State, Template Method, Command)
- When to use lambdas vs. full strategy classes
- How the Strategy pattern relates to SOLID principles (OCP, SRP, DIP)
- Real-world use cases (sorting algorithms, payment processing, validation rules, compression algorithms)
- Thread safety considerations when strategies are shared
- How Java 21 features (sealed interfaces, pattern matching) enhance the pattern

Common tricky questions:
- "How is Strategy different from State pattern?" (Strategy: client chooses the algorithm; State: context changes behavior based on internal state)
- "Can you implement Strategy without creating separate classes?" (Yes, using lambdas or method references)
- "How does the Strategy pattern violate or support the Open/Closed Principle?" (It supports OCP -- new strategies can be added without modifying existing code)

## Interview Q&A Section

**Q1: What is the Strategy pattern and when should you use it?**

```text
A1: The Strategy pattern defines a family of algorithms, encapsulates each one, and makes
them interchangeable. The pattern lets the algorithm vary independently from clients that
use it.

You should use it when:
1. You have multiple algorithms for a task and need to switch between them at runtime
2. You have a conditional block (if/else or switch) that selects algorithm variants
3. You want to isolate algorithm implementation details from client code
4. You need to follow the Open/Closed Principle for algorithm families

The pattern has three participants:
- Strategy interface: common contract for all algorithms
- ConcreteStrategy: each algorithm implementation
- Context: holds a strategy reference and delegates work to it

Benefits:
- Eliminates conditional statements for algorithm selection
- Each algorithm is in its own class (Single Responsibility Principle)
- New algorithms can be added without modifying existing code (Open/Closed Principle)
- Algorithms can be swapped at runtime
```

```java
// Strategy interface
interface CompressionStrategy {
    byte[] compress(byte[] data);
    byte[] decompress(byte[] data);
}

// Context
class FileCompressor {
    private CompressionStrategy strategy;

    public FileCompressor(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public byte[] compress(byte[] data) {
        return strategy.compress(data);
    }
}
```

**Q2: How do lambda expressions simplify the Strategy pattern in modern Java?**

```text
A2: Lambda expressions eliminate the need for separate strategy classes when the strategy
interface has a single method. Instead of creating a class for each algorithm, you can
pass a lambda expression or method reference directly.

This is possible because:
1. Strategy interfaces with a single abstract method are functional interfaces
2. Lambda expressions can be used wherever a functional interface is expected
3. Java's standard functional interfaces (Predicate, Function, Comparator, etc.) already
   serve as strategy interfaces for common operations

Advantages of lambda-based strategies:
- Less boilerplate code (no separate class files)
- Strategies can be defined inline where they're used
- Method references provide even more concise syntax
- Strategies can be composed using andThen(), compose(), etc.

When to still use full classes:
- When the strategy has multiple methods
- When the strategy needs to maintain internal state
- When the strategy implementation is complex (more than ~5 lines)
- When you need to test the strategy in isolation
```

```java
// Traditional approach: separate class for each strategy
class UpperCaseStrategy implements TextStrategy {
    @Override
    public String process(String text) {
        return text.toUpperCase();
    }
}

// Lambda approach: strategies as one-liners
UnaryOperator<String> upperCase = String::toUpperCase;
UnaryOperator<String> reverse = s -> new StringBuilder(s).reverse().toString();
UnaryOperator<String> trim = String::trim;

// Usage
TextProcessor processor = new TextProcessor(upperCase);
processor.process("hello"); // "HELLO"

// Switch strategy with a lambda
processor.setStrategy(s -> s.replaceAll("\\s+", "-"));
processor.process("hello world"); // "hello-world"
```

**Q3: How can sealed interfaces and pattern matching improve the Strategy pattern?**

```text
A3: Sealed interfaces (Java 17+) restrict which classes can implement the strategy
interface. Combined with pattern matching for switch (Java 21), this provides:

1. Compile-time exhaustiveness: The compiler ensures all strategy types are handled
   in switch expressions. If a new strategy is added but not handled, it's a compile error.

2. Type-safe dispatching: Pattern matching lets you safely access strategy-specific
   data without casting, using record patterns or type patterns.

3. Closed set of strategies: When the set of algorithms is fixed and known at design
   time, sealed interfaces document this constraint explicitly.

This approach is ideal for:
- Payment processing (credit card, PayPal, crypto -- known set)
- Shipping calculators (ground, express, overnight -- known set)
- Tax strategies per jurisdiction (known fixed set)

It is NOT ideal when:
- New strategies need to be added frequently by external developers
- The strategy set is open-ended
```

```java
// Sealed strategy interface
sealed interface PaymentStrategy
    permits CreditCardPayment, PayPalPayment, CryptoPayment {
    PaymentResult pay(double amount);
}

// Record-based concrete strategies
record CreditCardPayment(String cardNumber) implements PaymentStrategy {
    public PaymentResult pay(double amount) {
        return new PaymentResult(true, "CC-" + UUID.randomUUID());
    }
}

record PayPalPayment(String email) implements PaymentStrategy {
    public PaymentResult pay(double amount) {
        return new PaymentResult(true, "PP-" + UUID.randomUUID());
    }
}

record CryptoPayment(String wallet) implements PaymentStrategy {
    public PaymentResult pay(double amount) {
        return new PaymentResult(true, "CRYPTO-" + UUID.randomUUID());
    }
}

// Exhaustive pattern matching -- compiler ensures all cases are covered
String describe(PaymentStrategy strategy) {
    return switch (strategy) {
        case CreditCardPayment cc -> "Card: " + cc.cardNumber();
        case PayPalPayment pp -> "PayPal: " + pp.email();
        case CryptoPayment cp -> "Crypto: " + cp.wallet();
        // No default needed -- compiler knows all cases are covered
    };
}
```

**Q4: What is the difference between the Strategy and State patterns?**

```text
A4: While both patterns use composition to delegate behavior to an encapsulated object
and share a similar class structure, they differ in intent and usage:

Strategy Pattern:
- The CLIENT selects which algorithm to use
- Strategies are typically stateless and interchangeable
- The context does not change its strategy on its own
- Used when you have multiple algorithms for a task
- Example: choosing a sorting algorithm, compression method, or payment method

State Pattern:
- The CONTEXT changes its behavior based on internal state transitions
- The state object may trigger transitions to other states
- The context's behavior changes automatically as state changes
- Used when an object's behavior depends on its state
- Example: a vending machine (idle, has money, dispensing), TCP connection states

Key differences:
1. WHO decides to change: Client (Strategy) vs. Context/State (State)
2. Awareness of other variants: Strategies don't know about each other;
   States often know which state to transition to
3. Lifetime: Strategy is typically set once or changed explicitly;
   State transitions happen as part of normal operation
4. Number of methods: Strategy often has one method; State typically has
   multiple methods representing all possible actions in that state
```

```java
// Strategy: client chooses the algorithm
Sorter sorter = new Sorter(new BubbleSortStrategy()); // client picks
sorter.setStrategy(new QuickSortStrategy());           // client changes

// State: context transitions automatically
class TrafficLight {
    private TrafficLightState state = new RedState();

    public void next() {
        state = state.next(); // state decides the transition
    }

    public String color() {
        return state.color();
    }
}
```

**Q5: How do you compose strategies for complex behavior?**

```text
A5: Strategy composition allows building complex algorithms from simpler ones.
Java's functional interfaces support this natively through methods like
andThen(), compose(), and(), or(), and negate().

Approaches to composition:
1. Function chaining: Use Function.andThen() or Function.compose() to chain
   transformations in sequence.
2. Predicate combination: Use Predicate.and(), Predicate.or(), Predicate.negate()
   to combine filtering conditions.
3. Pipeline pattern: Create a pipeline that applies a sequence of strategy stages
   to data, where each stage is a strategy.
4. Composite pattern: Create a MacroStrategy that holds a list of strategies and
   executes them in order (combines Strategy with Composite).

Benefits:
- Build complex behavior from simple, tested building blocks
- Each building block can be reused across different compositions
- Easy to add, remove, or reorder stages
- Follows the Single Responsibility Principle
```

```java
// Function chaining
Function<String, String> normalize = String::trim;
Function<String, String> lower = String::toLowerCase;
Function<String, String> pipeline = normalize.andThen(lower);
pipeline.apply("  HELLO  "); // "hello"

// Predicate composition
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> longerThan3 = s -> s.length() > 3;
Predicate<String> combined = notEmpty.and(longerThan3);

// Data pipeline with strategy stages
DataPipeline<Integer> pipeline = new DataPipeline<Integer>()
    .addStage(list -> list.stream().filter(n -> n > 0).toList())
    .addStage(list -> list.stream().distinct().toList())
    .addStage(list -> list.stream().sorted().toList());

List<Integer> result = pipeline.execute(Arrays.asList(3, -1, 2, 3, 0, 5));
// result: [2, 3, 5]
```

**Q6: What are the trade-offs of the Strategy pattern?**

```text
A6: Like all design patterns, the Strategy pattern involves trade-offs:

Advantages:
1. Open/Closed Principle: new algorithms without modifying existing code
2. Single Responsibility: each algorithm in its own class
3. Runtime flexibility: swap algorithms dynamically
4. Testability: strategies can be tested independently
5. Eliminates conditionals: no if/else chains for algorithm selection

Disadvantages:
1. Increased number of objects: each algorithm is a separate class
   (mitigated by lambdas in modern Java)
2. Client awareness: clients must know which strategies exist and
   understand the differences to choose appropriately
3. Communication overhead: the context must pass data to the strategy,
   which may require exposing internal state
4. Overkill for simple cases: if you only have 2-3 simple algorithms
   that rarely change, the pattern adds unnecessary complexity

When NOT to use Strategy:
- When algorithms rarely change and there are only a few of them
- When the algorithm selection is known at compile time and never changes
- When the overhead of the indirection is not justified
- When a simple if/else or switch is clearer
```

```java
// Overkill: only two simple cases, unlikely to change
// Using Strategy pattern here adds unnecessary complexity
interface DiscountStrategy {
    double apply(double price);
}
class NoDiscount implements DiscountStrategy { ... }
class TenPercentOff implements DiscountStrategy { ... }

// Simpler and clearer:
double applyDiscount(double price, boolean hasDiscount) {
    return hasDiscount ? price * 0.9 : price;
}

// Justified: multiple algorithms, likely to grow
// Payment processing with credit card, PayPal, crypto, bank transfer, etc.
sealed interface PaymentStrategy permits CreditCard, PayPal, Crypto, BankTransfer {
    PaymentResult pay(double amount);
}
```

## Code Examples

- Test: [StrategyPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/StrategyPatternTest.java)
- Source: [StrategyPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/StrategyPattern.java)
