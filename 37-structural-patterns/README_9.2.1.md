# 9.2.1. Adapter Pattern

## Concept Explanation

The Adapter Pattern is a structural design pattern that allows objects with incompatible interfaces to work together. It acts as a bridge between two incompatible interfaces by wrapping an existing class with a new interface that clients expect.

**Real-world analogy**: Consider a power adapter for international travel. A US laptop charger has a two-prong plug, but European outlets use a different shape. A travel adapter does not change the electricity or the charger itself — it simply converts the plug shape so the charger can connect to the foreign outlet. Similarly, the Adapter pattern wraps an existing class so it can be used where a different interface is expected, without modifying the original class.

The Gang of Four (GoF) classifies the Adapter pattern under structural patterns because it deals with how classes and objects are composed to form larger structures. The pattern is also known as the "Wrapper" pattern because the adapter wraps the adaptee.

There are two main variants:
- **Object Adapter** (composition-based): The adapter holds a reference to the adaptee and delegates calls. This is the preferred approach in Java since it avoids tight coupling through inheritance.
- **Class Adapter** (inheritance-based): The adapter extends the adaptee and implements the target interface. Java's single inheritance limitation restricts this to adapting only one class at a time.

## Key Points to Remember

- The Adapter pattern converts the interface of a class into another interface that clients expect.
- It lets classes work together that could not otherwise because of incompatible interfaces.
- Object adapters use composition (delegation), class adapters use inheritance.
- Object adapters are more flexible because they can adapt any subclass of the adaptee.
- Class adapters can override adaptee behavior directly since they extend it.
- The adapter does not add new behavior — it only translates interfaces (unlike the Decorator pattern).
- The pattern follows the Open/Closed Principle — you can introduce new adapters without changing existing code.
- In Java, object adapters are strongly preferred due to single inheritance limitations.
- Two-way adapters can implement both the target and adaptee interfaces, enabling bidirectional adaptation.

## Relevant Java 21 Features

- **Sealed interfaces**: You can use sealed interfaces for the target interface to restrict which adapters are permitted, ensuring compile-time safety.
- **Records**: Adapter classes that only wrap an adaptee and delegate can sometimes be expressed as records when they are effectively data carriers.
- **Pattern matching for `instanceof`**: Useful when determining the type of an adaptee at runtime.
- **Default methods in interfaces**: Interfaces can provide default implementations, reducing adapter boilerplate when only a few methods need translation.
- **Functional interfaces and lambdas**: For single-method target interfaces, a lambda expression can serve as an inline adapter without creating a dedicated adapter class.

Evolution across Java versions:
- **Java 8**: Introduced default methods in interfaces and lambdas, which made lightweight adapters trivial.
- **Java 14+**: Records provide a concise way to represent data-carrying adapters.
- **Java 17+**: Sealed classes/interfaces allow controlled adapter hierarchies.

## Common Pitfalls and How to Avoid Them

1. **Adapting too many methods**: If the adaptee's interface is vastly different from the target, the adapter becomes bloated. Consider whether a Facade or a redesign would be more appropriate.

2. **Tight coupling through class adapter**: Using inheritance ties the adapter to one specific adaptee class.
   ```java
   // Problem: Class adapter can only adapt LegacyAudioPlayer
   class AudioClassAdapter extends LegacyAudioPlayer implements MediaPlayer { ... }
   
   // Solution: Object adapter can adapt any LegacyAudioPlayer subclass
   class AudioPlayerAdapter implements MediaPlayer {
       private final LegacyAudioPlayer player; // composition
   }
   ```

3. **Confusing Adapter with Decorator**: The Adapter changes an interface; the Decorator adds behavior without changing the interface. Mixing these purposes leads to unclear design.

4. **Null adaptee references**: Forgetting to validate the adaptee passed to the adapter constructor.
   ```java
   // Problem: NullPointerException later during delegation
   class Adapter implements Target {
       private final Adaptee adaptee;
       Adapter(Adaptee adaptee) { this.adaptee = adaptee; } // no validation
   }
   
   // Solution: Validate eagerly
   Adapter(Adaptee adaptee) {
       this.adaptee = Objects.requireNonNull(adaptee, "Adaptee must not be null");
   }
   ```

5. **Breaking the Liskov Substitution Principle**: The adapter should fully satisfy the target interface contract. Throwing UnsupportedOperationException for methods the adaptee cannot support violates LSP.

## Best Practices and Optimization Techniques

1. **Prefer object adapters** over class adapters in Java for flexibility and to avoid inheritance constraints.
2. **Use composition and delegation** as the primary mechanism.
3. **Validate constructor arguments** eagerly with `Objects.requireNonNull`.
4. **Keep adapters thin** — they should only translate interface calls, not contain business logic.
5. **Consider lambda-based adapters** for simple single-method target interfaces (functional interfaces).
6. **Document the mapping** between target methods and adaptee methods for maintainability.
7. **Make adapters immutable** when possible — final fields, no setters.
8. **Use the adapter to handle data format conversions** between systems (e.g., legacy array-based data to modern records).

## Edge Cases and Their Handling

1. **Null inputs**: Always validate constructor parameters and method arguments with `Objects.requireNonNull`.
2. **Empty collections**: When adapting data collections, handle empty lists gracefully without throwing exceptions.
3. **Partial adaptation**: When the adaptee only supports a subset of the target interface, consider using default methods or abstract base classes to provide sensible defaults.
4. **Thread safety**: If the adaptee is not thread-safe, the adapter should document this or add synchronization.
5. **Extra data fields**: When adapting data formats, handle cases where the source has more or fewer fields than expected.

## Interview-specific Insights

Interviewers commonly ask about:
- The difference between class and object adapters and when to use each
- How the Adapter pattern relates to the Decorator and Facade patterns
- Real-world examples of the Adapter pattern in the Java SDK (e.g., `Arrays.asList()`, `InputStreamReader`)
- When NOT to use the Adapter pattern
- How to handle situations where the adaptee's interface is vastly different from the target

Tricky points:
- In Java, class adapters are limited by single inheritance — you can only extend one adaptee class
- `java.util.Arrays.asList()` is often cited as an adapter (it adapts an array to the `List` interface)
- `InputStreamReader` adapts `InputStream` (byte stream) to `Reader` (character stream)

## Interview Q&A Section

**Q1: What is the Adapter pattern and when would you use it?**

```text
A1: The Adapter pattern is a structural design pattern that allows two incompatible interfaces
to work together. It wraps an existing class with a new interface that the client expects.

You would use it when:
1. You want to use an existing class but its interface doesn't match what you need.
2. You're integrating a third-party library that has a different interface than your codebase.
3. You need to create a reusable class that cooperates with unrelated or unforeseen classes.
4. You're migrating from a legacy system and need to bridge old and new interfaces.
5. You want to decouple your code from a specific implementation.

The key insight is that the adapter does NOT change the behavior of the adaptee — it only
translates the interface. If you need to add behavior, use the Decorator pattern instead.
```

```java
// Example: Adapting a legacy payment processor to a modern interface
interface ModernPaymentGateway {
    PaymentResult processPayment(String cardNumber, double amount);
}

class LegacyPaymentProcessor {
    public int charge(String card, int amountInCents) {
        // legacy implementation
        return 0; // status code
    }
}

class PaymentAdapter implements ModernPaymentGateway {
    private final LegacyPaymentProcessor legacy;

    PaymentAdapter(LegacyPaymentProcessor legacy) {
        this.legacy = Objects.requireNonNull(legacy);
    }

    @Override
    public PaymentResult processPayment(String cardNumber, double amount) {
        int cents = (int) (amount * 100);
        int statusCode = legacy.charge(cardNumber, cents);
        return new PaymentResult(statusCode == 0, statusCode);
    }
}
```

**Q2: What is the difference between a class adapter and an object adapter?**

```text
A2: The two variants differ in their mechanism:

Class Adapter (inheritance-based):
- Extends the adaptee class AND implements the target interface
- Can override adaptee methods directly
- Limited by Java's single inheritance — can only adapt one class
- Creates a tighter coupling between adapter and adaptee
- Cannot adapt subclasses of the adaptee

Object Adapter (composition-based):
- Implements the target interface and holds a reference to the adaptee
- Delegates calls to the adaptee through the reference
- Can adapt any subclass of the adaptee (polymorphism)
- More flexible and loosely coupled
- Can work with multiple adaptees if needed

In Java, object adapters are strongly preferred because of single inheritance limitations.
Class adapters are more common in languages with multiple inheritance like C++.
```

```java
// Class Adapter — extends LegacyPlayer and implements MediaPlayer
class AudioClassAdapter extends LegacyAudioPlayer implements MediaPlayer {
    @Override
    public String play(String filename) {
        return playMp3(filename); // direct call via inheritance
    }
}

// Object Adapter — wraps LegacyPlayer and implements MediaPlayer
class AudioObjectAdapter implements MediaPlayer {
    private final LegacyAudioPlayer legacy;

    AudioObjectAdapter(LegacyAudioPlayer legacy) {
        this.legacy = legacy;
    }

    @Override
    public String play(String filename) {
        return legacy.playMp3(filename); // delegation
    }
}
```

**Q3: How does the Adapter pattern differ from the Facade and Decorator patterns?**

```text
A3: These three structural patterns are often confused but serve different purposes:

Adapter:
- Converts one interface to another
- Makes two incompatible interfaces work together
- Does NOT add new behavior
- Works with a single class (the adaptee)

Decorator:
- Adds new responsibilities/behavior to an object
- Keeps the SAME interface as the wrapped object
- Can be stacked (multiple decorators on one object)
- Follows the same interface hierarchy

Facade:
- Provides a simplified interface to a complex subsystem
- Hides the complexity of multiple classes behind a unified API
- Does not wrap a single object; it orchestrates many objects
- Creates a higher-level interface

The key distinction: Adapter changes interfaces, Decorator adds behavior,
and Facade simplifies interfaces.
```

```java
// Adapter: changes the interface
class XmlToJsonAdapter implements JsonDataSource {
    private final XmlDataSource xmlSource;
    // converts XML interface calls to JSON interface calls
}

// Decorator: same interface, adds behavior
class CachingDataSource implements DataSource {
    private final DataSource wrapped;
    // adds caching behavior, same DataSource interface
}

// Facade: simplifies multiple subsystems
class OrderFacade {
    private final InventoryService inventory;
    private final PaymentService payment;
    private final ShippingService shipping;
    // simplifies order placement across all three services
}
```

**Q4: Can you give real-world examples of the Adapter pattern in the Java SDK?**

```text
A4: The Java SDK uses the Adapter pattern extensively:

1. Arrays.asList(T... a) — adapts an array to the List interface. The returned
   list is backed by the array, providing a List view of array data.

2. InputStreamReader — adapts InputStream (byte-oriented) to Reader (character-oriented).
   It bridges the gap between byte streams and character streams.

3. OutputStreamWriter — adapts OutputStream to Writer, the reverse of InputStreamReader.

4. Collections.enumeration(Collection) — adapts a Collection to the legacy
   Enumeration interface for backward compatibility.

5. Collections.list(Enumeration) — adapts a legacy Enumeration to a List.

6. java.util.concurrent.FutureTask — adapts Callable to both Runnable and Future.

7. javax.xml.bind.annotation.adapters.XmlAdapter — used in JAXB for custom
   marshalling/unmarshalling between XML and Java types.

Each of these converts one interface to another without changing the underlying behavior.
```

```java
// Arrays.asList — adapts array to List
String[] array = {"a", "b", "c"};
List<String> list = Arrays.asList(array); // List view of the array

// InputStreamReader — adapts InputStream to Reader
InputStream byteStream = new FileInputStream("file.txt");
Reader charReader = new InputStreamReader(byteStream, StandardCharsets.UTF_8);

// Collections.enumeration — adapts Collection to Enumeration
List<String> items = List.of("x", "y");
Enumeration<String> enumeration = Collections.enumeration(items);
```

**Q5: How can you use Java lambdas as lightweight adapters?**

```text
A5: When the target interface is a functional interface (single abstract method), you can
use a lambda expression as an inline adapter without creating a dedicated adapter class.
This is a modern Java approach that reduces boilerplate significantly.

This works because:
1. A lambda expression implements a functional interface
2. The lambda body can delegate to the adaptee's method
3. It can also transform parameters and return values

This approach is best for simple adapters with straightforward translations. For complex
adapters that need to maintain state or handle multiple methods, a dedicated adapter class
is still preferred.
```

```java
// Traditional adapter class
class StringComparatorAdapter implements Comparator<String> {
    private final LegacyStringComparer comparer;

    StringComparatorAdapter(LegacyStringComparer comparer) {
        this.comparer = comparer;
    }

    @Override
    public int compare(String s1, String s2) {
        return comparer.compareStrings(s1, s2);
    }
}

// Lambda-based adapter — same result, less code
LegacyStringComparer comparer = new LegacyStringComparer();
Comparator<String> adapted = (s1, s2) -> comparer.compareStrings(s1, s2);
// Or with method reference:
Comparator<String> adapted2 = comparer::compareStrings;
```

**Q6: What are the trade-offs of using the Adapter pattern?**

```text
A6: Trade-offs to consider:

Advantages:
- Promotes code reuse by allowing existing classes to work with new systems
- Follows the Open/Closed Principle — extend without modifying
- Follows the Single Responsibility Principle — separation of interface translation
- Reduces coupling between client code and third-party or legacy code
- Enables gradual migration from legacy to modern interfaces

Disadvantages:
- Adds an extra layer of indirection, increasing complexity
- Can become a maintenance burden if the adaptee's interface changes frequently
- Performance overhead of delegation (usually negligible)
- If many adapters are needed, it may indicate a deeper design problem
- Class adapters are limited by single inheritance in Java

When NOT to use:
- When you control both interfaces and can simply change one to match the other
- When the interfaces are too different — a more significant redesign may be needed
- When the overhead of the adapter layer is not justified by the decoupling benefit
```

```java
// Example of when NOT to use an adapter:
// If you control both interfaces, just align them
interface DataFetcher {
    List<String> fetch(String query); // Change this to match what clients need
}

// Example of when an adapter IS appropriate:
// Third-party library you cannot modify
class ThirdPartySearchEngine {
    public SearchResults search(SearchQuery query) { ... }
}

// Your system expects a different interface
interface MySearchService {
    List<Result> find(String keyword);
}

// Adapter bridges the gap
class SearchAdapter implements MySearchService {
    private final ThirdPartySearchEngine engine;
    // ...
}
```

## Code Examples

- Implementation: [AdapterPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/AdapterPattern.java)
- Tests: [AdapterPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/AdapterPatternTest.java)
