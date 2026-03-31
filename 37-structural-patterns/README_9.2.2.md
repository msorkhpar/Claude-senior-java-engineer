# 9.2.2. Decorator Pattern

## Concept Explanation

The Decorator Pattern is a structural design pattern that lets you dynamically attach new behaviors to objects by placing them inside special wrapper objects (decorators). Decorators provide a flexible alternative to subclassing for extending functionality at runtime.

**Real-world analogy**: Think of ordering a coffee. You start with a base espresso, then you can add decorations: whipped cream, caramel syrup, extra shot, oat milk. Each addition wraps the previous order, adding to its description and price, but the core is still an espresso. You can combine any decorations in any order, and each one knows how to delegate to the thing it wraps. The Decorator pattern works exactly this way — each decorator wraps a component and adds its own behavior before or after delegating to the wrapped object.

The pattern is classified as structural because it composes objects to achieve new functionality. Unlike inheritance, which is static and applies to an entire class, decoration is dynamic and applies to individual objects at runtime.

Key participants:
- **Component** (interface): Defines the common interface for both concrete components and decorators.
- **Concrete Component**: The original object being wrapped.
- **Base Decorator**: Abstract class that implements the component interface and holds a reference to a wrapped component.
- **Concrete Decorators**: Add specific behaviors before or after delegating to the wrapped component.

## Key Points to Remember

- Decorators implement the same interface as the object they decorate.
- Decorators hold a reference to a component object and delegate all base operations to it.
- Multiple decorators can be stacked (chained) on a single object.
- Decorators add behavior transparently — the client works with the component interface, unaware of decoration.
- The pattern follows the Open/Closed Principle — you can add new behaviors without modifying existing classes.
- The pattern follows the Single Responsibility Principle — each decorator handles one concern.
- Order of decoration matters — `Logging(Encryption(data))` is different from `Encryption(Logging(data))`.
- Java I/O streams (`InputStream`, `OutputStream`, `Reader`, `Writer`) are the classic example of the Decorator pattern.
- Decorators should not change the fundamental contract of the component interface.

## Relevant Java 21 Features

- **Sealed interfaces**: You can seal the component interface to control which decorators are permitted.
- **Records**: While decorators typically have mutable state or behavior, the base decorator reference can be stored in a record if the decorator is stateless.
- **Pattern matching for `instanceof`**: Useful for unwrapping or inspecting decorator chains at runtime.
- **Default methods**: Can reduce boilerplate in decorator base classes by providing default pass-through behavior.

Evolution across Java versions:
- **Java 1.0**: The I/O streams library (`java.io`) was designed around the Decorator pattern from the start.
- **Java 8**: Functional interfaces and lambdas enable lightweight inline decorators via method wrapping.
- **Java 17+**: Sealed classes allow compile-time control of the decorator hierarchy.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to delegate to the wrapped component**: The most common mistake is overriding a method without calling `super.send(message)` or `wrappee.send(message)`.
   ```java
   // Problem: Lost the behavior of the wrapped component
   @Override
   public String send(String message) {
       return "SMS: " + message; // forgot to call wrappee.send()
   }

   // Solution: Always delegate first
   @Override
   public String send(String message) {
       String base = wrappee.send(message); // delegate
       return base + " | SMS: " + message;  // then add behavior
   }
   ```

2. **Identity comparisons breaking**: Since the decorator wraps the original object, `==` and identity-based checks will fail. Use `equals()` that respects the wrapper chain.
   ```java
   Notifier original = new EmailNotifier("a@b.com");
   Notifier decorated = new SmsDecorator(original, "+1");
   // original == decorated is FALSE
   // original.equals(decorated) depends on equals() implementation
   ```

3. **Decorator explosion**: Creating too many fine-grained decorators makes the system hard to understand. Group related behaviors into a single decorator when they always go together.

4. **Null wrappee**: Always validate the wrapped component is not null in the constructor.
   ```java
   protected NotifierDecorator(Notifier wrappee) {
       this.wrappee = Objects.requireNonNull(wrappee, "Wrapped notifier must not be null");
   }
   ```

5. **Confusing with inheritance**: Subclassing creates a fixed hierarchy; decoration is dynamic. If you find yourself creating many subclass combinations (EmailSmsNotifier, EmailSlackNotifier, SmsSlackNotifier...), switch to decorators.

## Best Practices and Optimization Techniques

1. **Keep decorators focused**: Each decorator should add exactly one responsibility.
2. **Use an abstract base decorator**: It handles delegation boilerplate, so concrete decorators only override what they need.
3. **Make decorators interchangeable**: All decorators should implement the same interface so they can be freely composed.
4. **Document decoration order sensitivity**: If order matters, document it clearly.
5. **Prefer decorators over inheritance** when you need to combine behaviors in multiple ways.
6. **Consider lambdas for simple decorators**: For functional interfaces, a lambda can serve as a lightweight decorator.
7. **Make the log or state in decorators accessible** for testing and debugging (e.g., expose an unmodifiable log).
8. **Thread safety**: If decorators maintain state (like a log), consider synchronization or use concurrent data structures.

## Edge Cases and Their Handling

1. **Null messages/inputs**: Validate all method arguments, not just constructor parameters.
2. **Empty decoration chain**: A component without any decorators should work identically — the base component is always a valid standalone object.
3. **Double decoration**: Applying the same decorator twice should be supported and produce the expected cumulative effect.
4. **Removing a decorator**: The pattern does not natively support removing decorators from a chain. If you need this, consider using a different pattern (like Chain of Responsibility or a list of handlers).
5. **Serialization**: Decorator chains can be difficult to serialize. Consider flattening or recreating the chain on deserialization.

## Interview-specific Insights

Interviewers commonly ask about:
- The difference between Decorator and inheritance (subclassing)
- How Java I/O streams use the Decorator pattern
- When to use Decorator vs. Strategy vs. Chain of Responsibility
- How to stack multiple decorators and whether order matters
- The relationship between Decorator and the Open/Closed Principle

Tricky points:
- The Decorator pattern is a form of composition — it composes behavior at runtime
- Java I/O is the textbook example: `new BufferedReader(new InputStreamReader(new FileInputStream("file")))`
- Decorators can be tested independently since each one adds only one behavior
- The pattern can be implemented with lambdas for functional interfaces

## Interview Q&A Section

**Q1: What is the Decorator pattern and how does it differ from subclassing?**

```text
A1: The Decorator pattern dynamically attaches new behaviors to an object by wrapping it
inside a decorator object that implements the same interface. The decorator delegates to
the wrapped object and adds its own behavior before or after delegation.

Key differences from subclassing:

1. Dynamic vs. Static:
   - Decorator: Behavior is added at RUNTIME, per-instance
   - Subclass: Behavior is fixed at COMPILE TIME, per-class

2. Combinatorial explosion:
   - Subclassing: 3 behaviors = 7 subclass combinations (A, B, C, AB, AC, BC, ABC)
   - Decorator: 3 behaviors = 3 decorator classes, compose as needed

3. Open/Closed Principle:
   - Decorator: Add new behaviors without modifying existing classes
   - Subclass: Often requires modifying the hierarchy

4. Single Responsibility:
   - Each decorator class handles one concern
   - Subclasses often accumulate multiple responsibilities

5. Flexibility:
   - Decorators can be stacked in any order
   - Inheritance hierarchies are rigid
```

```java
// Subclassing approach — combinatorial explosion
class EmailNotifier { }
class SmsNotifier extends EmailNotifier { }
class SlackNotifier extends EmailNotifier { }
class SmsSlackNotifier extends EmailNotifier { } // need all combinations!

// Decorator approach — compose freely
Notifier notifier = new EmailNotifier("a@b.com");
notifier = new SmsDecorator(notifier, "+1234");
notifier = new SlackDecorator(notifier, "#alerts");
// Any combination, any order, at runtime
```

**Q2: How does the Java I/O library use the Decorator pattern?**

```text
A2: Java I/O streams are the classic textbook example of the Decorator pattern.

The hierarchy:
- InputStream (Component interface)
- FileInputStream, ByteArrayInputStream (Concrete Components)
- FilterInputStream (Base Decorator — holds a reference to another InputStream)
- BufferedInputStream, DataInputStream, etc. (Concrete Decorators)

Each decorator adds a specific capability:
- BufferedInputStream: adds buffering for performance
- DataInputStream: adds ability to read primitive types
- GZIPInputStream: adds decompression
- CipherInputStream: adds decryption

These can be stacked in any combination:
```

```java
// Stacking I/O decorators
InputStream raw = new FileInputStream("data.gz");         // concrete component
InputStream buffered = new BufferedInputStream(raw);       // add buffering
InputStream decompressed = new GZIPInputStream(buffered);  // add decompression
DataInputStream data = new DataInputStream(decompressed);  // add type reading

int value = data.readInt(); // reads through the entire decorator chain:
// DataInputStream -> GZIPInputStream -> BufferedInputStream -> FileInputStream

// Similarly for writers:
Writer writer = new BufferedWriter(
    new OutputStreamWriter(
        new FileOutputStream("output.txt"),
        StandardCharsets.UTF_8
    )
);
```

**Q3: When should you use Decorator vs. Strategy vs. Chain of Responsibility?**

```text
A3: These patterns are related but serve different purposes:

Decorator:
- Adds behavior to an existing object transparently
- The client uses the same interface regardless of decoration
- Decorators are stacked (nested wrappers)
- Best when: you want to combine behaviors dynamically
- Example: adding logging, caching, encryption to a data source

Strategy:
- Encapsulates interchangeable algorithms
- The client selects one strategy at a time
- Strategies are alternatives, not cumulative
- Best when: you need to switch between different algorithms
- Example: choosing a sorting algorithm or compression scheme

Chain of Responsibility:
- Passes a request along a chain of handlers
- Each handler decides whether to process or pass along
- The request may be handled by zero, one, or multiple handlers
- Best when: you don't know in advance which handler should process a request
- Example: event handling, middleware chains, approval workflows

Key difference: Decorator ALWAYS delegates to the next wrapper. Chain of Responsibility
MAY stop at any handler. Strategy selects ONE algorithm from many options.
```

```java
// Decorator: all layers always execute
Notifier n = new SlackDecorator(new SmsDecorator(new EmailNotifier("a@b.com"), "+1"), "#ch");
n.send("alert"); // Email + SMS + Slack — ALL execute

// Strategy: one algorithm selected
interface CompressionStrategy { byte[] compress(byte[] data); }
class GzipStrategy implements CompressionStrategy { /* ... */ }
class ZipStrategy implements CompressionStrategy { /* ... */ }
// Client picks ONE strategy

// Chain of Responsibility: handlers may or may not process
interface Handler { void handle(Request req); }
class AuthHandler implements Handler { /* may pass to next or reject */ }
class LoggingHandler implements Handler { /* logs and passes to next */ }
```

**Q4: Can you implement a decorator using functional interfaces and lambdas?**

```text
A4: Yes! When the component interface is a functional interface (single abstract method),
you can use lambdas and method composition to create lightweight decorators without
dedicated wrapper classes.

Java's Function interface provides andThen() and compose() methods that enable
decorator-like chaining. UnaryOperator<T> is particularly useful for same-type
transformations.

This approach is best for:
- Simple behavior additions (logging, validation, transformation)
- Stateless decorators
- When you don't need a full class hierarchy

For stateful decorators or multi-method interfaces, traditional classes are still needed.
```

```java
import java.util.function.Function;
import java.util.function.UnaryOperator;

// Traditional class-based decorator
class UpperCaseDecorator implements Formatter {
    private final Formatter wrapped;
    UpperCaseDecorator(Formatter w) { this.wrapped = w; }
    public String format(String s) { return wrapped.format(s).toUpperCase(); }
}

// Lambda-based decorator using Function composition
Function<String, String> base = s -> s.trim();
Function<String, String> withUpper = base.andThen(String::toUpperCase);
Function<String, String> withPrefix = withUpper.andThen(s -> "[LOG] " + s);

String result = withPrefix.apply("  hello  "); // "[LOG] HELLO"

// UnaryOperator chaining
UnaryOperator<String> trim = String::trim;
UnaryOperator<String> upper = String::toUpperCase;
UnaryOperator<String> pipeline = s -> upper.apply(trim.apply(s));
```

**Q5: Does the order of decorators matter? Give an example.**

```text
A5: Yes, the order of decorators can significantly affect the outcome. Each decorator
adds its behavior relative to the previous decorator in the chain.

Consider a data source with encryption and compression decorators:
- Compress then Encrypt: Data is compressed first (smaller), then encrypted.
  The encrypted data cannot be compressed further. This is efficient.
- Encrypt then Compress: Data is encrypted first (looks random), then
  compressed. Random data compresses poorly. This is wasteful.

The order also matters for logging and caching:
- Log then Cache: Every request is logged, but only cache misses hit the
  real service.
- Cache then Log: Only cache misses are logged, missing visibility into
  cache hits.

Rule of thumb: think about the data flow direction. The outermost decorator
executes first on the way in and last on the way out.
```

```java
// Order matters: Compression + Encryption
DataSource source = new InMemoryDataSource();

// Option 1: Compress then Encrypt (preferred)
DataSource compressThenEncrypt = new EncryptionDecorator(
    new CompressionDecorator(source), key
);
// Write: data -> compress -> encrypt -> store
// Read: store -> decrypt -> decompress -> data

// Option 2: Encrypt then Compress (wasteful)
DataSource encryptThenCompress = new CompressionDecorator(
    new EncryptionDecorator(source, key)
);
// Write: data -> encrypt -> compress -> store
// Encrypted data is random and compresses poorly!
```

**Q6: What are the disadvantages of the Decorator pattern?**

```text
A6: Despite its flexibility, the Decorator pattern has several disadvantages:

1. Complexity: Deep decorator chains can be hard to debug and understand.
   new A(new B(new C(new D(base)))) — which layer caused the bug?

2. Identity issues: The decorated object is not the same instance as the
   original. Code that relies on object identity (==) will break.

3. Difficult to remove decorators: Once wrapped, there is no standard way
   to unwrap or remove a specific decorator from the chain.

4. Configuration complexity: Creating the right decorator chain requires
   careful setup, often handled by a factory or builder.

5. Many small classes: Each behavior requires its own decorator class,
   which can lead to many small classes in the codebase.

6. Method proliferation: If the component interface has many methods, each
   decorator must implement all of them (even if most just delegate).

7. Ordering sensitivity: Wrong order can produce incorrect results or
   poor performance.

Mitigation strategies:
- Use a builder or factory to construct decorator chains
- Use an abstract base decorator to handle delegation boilerplate
- Document the expected decoration order
- Keep the component interface small (Interface Segregation Principle)
```

```java
// Problem: Many methods in the interface, decorator must handle all
interface DataService {
    String read(String id);
    void write(String id, String data);
    void delete(String id);
    List<String> list();
    boolean exists(String id);
    // ... 10 more methods
}

// Solution: Abstract base decorator handles delegation
abstract class DataServiceDecorator implements DataService {
    protected final DataService wrapped;

    DataServiceDecorator(DataService wrapped) {
        this.wrapped = wrapped;
    }

    @Override public String read(String id) { return wrapped.read(id); }
    @Override public void write(String id, String data) { wrapped.write(id, data); }
    @Override public void delete(String id) { wrapped.delete(id); }
    @Override public List<String> list() { return wrapped.list(); }
    @Override public boolean exists(String id) { return wrapped.exists(id); }
    // delegates all methods — concrete decorators only override what they need
}
```

## Code Examples

- Implementation: [DecoratorPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/DecoratorPattern.java)
- Tests: [DecoratorPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/DecoratorPatternTest.java)
