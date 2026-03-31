# 9.1.2. Factory Method Pattern

## Concept Explanation

The Factory Method pattern is a creational design pattern that provides an interface for creating objects in a superclass,
but allows subclasses to alter the type of objects that will be created. Instead of calling a constructor directly, the
client code calls a **factory method** that returns an instance of a product. The exact type of that product is determined
by the subclass that overrides the factory method.

**Real-world analogy**: Consider a logistics company that originally shipped everything by truck. As the company grows,
it adds sea logistics. Rather than rewriting the entire logistics code, each logistics type (road, sea) overrides a
`createTransport()` method to return the appropriate transport object (Truck, Ship). The planning code works with the
abstract `Transport` interface and never needs to know the concrete type.

### The Problem It Solves

Without the Factory Method pattern, client code is tightly coupled to concrete classes:

```java
// Tight coupling -- adding a new type requires changing this code
if (type.equals("truck")) {
    transport = new Truck();
} else if (type.equals("ship")) {
    transport = new Ship();
}
// Adding "airplane" requires modifying this code (violates OCP)
```

With the Factory Method, subclasses decide which concrete class to instantiate, and the client code depends only on
the abstract product type.

### Structure

1. **Product** -- the interface or abstract class that defines what the factory method returns.
2. **ConcreteProduct** -- specific implementations of the Product.
3. **Creator** -- the abstract class or interface declaring the factory method.
4. **ConcreteCreator** -- subclasses that override the factory method to return a specific ConcreteProduct.

## Key Points to Remember

1. Factory Method is about **delegating instantiation to subclasses**, not just moving `new` into a static method.
2. The pattern promotes loose coupling between the creator and the products it creates.
3. It follows the **Open/Closed Principle** -- you can add new product types without modifying existing code.
4. A "Simple Factory" (static factory method) is NOT the same as the Factory Method pattern -- it is a simpler idiom,
   not a GoF pattern.
5. Factory methods can return cached instances (like `Integer.valueOf()`) rather than always creating new objects.
6. The pattern is often combined with other patterns like Template Method, Abstract Factory, or Prototype.
7. In Java, `java.util.Collection.iterator()` is a classic example of the Factory Method pattern.

## Relevant Java 21 Features

- **Sealed interfaces**: The Product interface can be sealed to restrict which ConcreteProducts are permitted, enabling
  exhaustive `switch` pattern matching over products.
- **Records**: Lightweight ConcreteProduct types can be records when they are simple data carriers.
- **Pattern matching for switch**: Clients can use `switch` with pattern matching to handle different product types
  returned by the factory method, with compile-time exhaustiveness checks on sealed types.
- **Static factory methods**: Java conventions like `List.of()`, `Map.of()`, and `Optional.of()` are static factory
  methods (a related but simpler concept).

## Common Pitfalls and How to Avoid Them

1. **Confusing Simple Factory with Factory Method**:
   ```java
   // This is a Simple Factory (static method), NOT the Factory Method pattern
   public static Shape createShape(String type) {
       return switch (type) {
           case "circle" -> new Circle();
           case "square" -> new Square();
           default -> throw new IllegalArgumentException("Unknown: " + type);
       };
   }
   ```
   **Solution**: The true Factory Method pattern uses inheritance -- a subclass overrides the factory method.

2. **Over-engineering simple cases**: If you only have one product type and no foreseeable extensions, a simple
   constructor call is fine.
   **Solution**: Apply the pattern only when you anticipate new product types or need to decouple creation from usage.

3. **Exposing concrete types in the factory method return type**:
   ```java
   // BAD: Returns concrete type, defeating the purpose
   public Truck createTransport() { return new Truck(); }
   ```
   **Solution**: Always return the abstract Product type.

4. **Not providing a default implementation**: If the base Creator is abstract, every subclass must implement the factory
   method. This can lead to unnecessary boilerplate.
   **Solution**: Provide a default implementation in the Creator that returns a common product.

5. **Parallel class hierarchies explosion**: Each new product requires a new creator subclass.
   **Solution**: Consider parameterized factory methods, lambda-based factories, or the Abstract Factory pattern.

## Best Practices and Optimization Techniques

1. **Use sealed interfaces** for the Product hierarchy to enable exhaustive pattern matching.
2. **Combine with functional interfaces**: In modern Java, factory methods can be `Supplier<Product>` lambdas, avoiding
   the need for separate Creator subclasses.
3. **Cache results** when the factory creates immutable or expensive objects (flyweight pattern).
4. **Use generic factory methods** to provide type safety:
   ```java
   public <T extends Product> T create(Class<T> type) { ... }
   ```
5. **Prefer composition over inheritance**: Instead of requiring a Creator subclass, accept a factory function as a
   parameter.
6. **Name factory methods clearly**: `of()`, `from()`, `create()`, `newInstance()`, `valueOf()`.
7. **Document the contract**: Specify what the factory method is expected to return and under what conditions.

## Edge Cases and Their Handling

1. **Null product type**: The factory method should throw `IllegalArgumentException` or `NullPointerException` for null
   input, not silently return null.
2. **Unknown product type**: Throw a descriptive exception rather than returning a default product that might mask bugs.
3. **Concurrent factory calls**: If the factory maintains state (e.g., a counter), ensure thread safety.
4. **Factory returning null**: This is almost always a design error. Use Optional or throw an exception instead.
5. **Circular dependencies**: A factory that needs its own product as a dependency should use lazy initialization.

## Interview-specific Insights

Interviewers often focus on:

- The difference between Simple Factory, Factory Method, and Abstract Factory
- How Factory Method follows the Open/Closed Principle
- Real-world examples in the JDK (`Collection.iterator()`, `URLStreamHandlerFactory`, `LoggerFactory`)
- When to use Factory Method vs. direct instantiation
- How to combine Factory Method with modern Java features (sealed types, records, lambdas)

Common tricky questions:

- "What is the difference between Factory Method and Abstract Factory?"
- "Can you give a real example of Factory Method in the JDK?"
- "How does Factory Method relate to the Dependency Inversion Principle?"
- "When would you NOT use Factory Method?"

## Interview Q&A Section

**Q1: What is the Factory Method pattern and how does it differ from a Simple Factory?**

```text
A1: The Factory Method pattern defines an interface for creating objects but lets subclasses decide which class to
instantiate. It uses inheritance: a base Creator class declares the factory method, and ConcreteCreator subclasses
override it.

Simple Factory is just a static method that creates objects based on a parameter. It does NOT use inheritance or
polymorphism for the creation logic.

Key differences:
1. Factory Method uses inheritance (subclass overrides) -- Simple Factory uses a single static method with conditionals.
2. Factory Method follows OCP (add new Creator subclass) -- Simple Factory requires modifying the static method.
3. Factory Method is a GoF design pattern -- Simple Factory is a programming idiom.
4. Factory Method supports polymorphic creation -- Simple Factory centralizes creation logic.

When to use which:
- Simple Factory: Few product types, unlikely to change, straightforward creation logic.
- Factory Method: Product types will grow, creation logic varies by context, you need polymorphic behavior.
```

```java
// Simple Factory (NOT the GoF Factory Method pattern)
public class SimpleShapeFactory {
    public static Shape create(String type) {
        return switch (type) {
            case "circle" -> new Circle(1.0);
            case "square" -> new Square(1.0);
            default -> throw new IllegalArgumentException("Unknown shape: " + type);
        };
    }
}

// Factory Method pattern (GoF)
public abstract class ShapeFactory {
    public abstract Shape createShape();

    public double calculateArea() {
        Shape shape = createShape(); // Delegates to subclass
        return shape.area();
    }
}

public class CircleFactory extends ShapeFactory {
    @Override
    public Shape createShape() {
        return new Circle(1.0);
    }
}
```

**Q2: How does the Factory Method pattern support the Open/Closed Principle?**

```text
A2: The Open/Closed Principle states that software entities should be open for extension but closed for modification.

The Factory Method pattern supports this by:

1. The Creator class is CLOSED for modification -- its template logic (e.g., a method that calls createProduct(),
   processes it, and returns a result) never changes.

2. The system is OPEN for extension -- adding a new product type requires only creating a new ConcreteProduct class
   and a new ConcreteCreator that overrides the factory method. No existing code changes.

Contrast with non-OCP code:
- Without Factory Method, adding a new product type means modifying a conditional (if/else or switch) in the creation
  code. This is a violation of OCP because existing code must be changed.

Example: A notification system originally supports Email. When SMS is needed:
- Without Factory Method: Modify the send() method to add an "sms" branch.
- With Factory Method: Create SmsNotification (product) and SmsNotificationFactory (creator). Existing code is untouched.
```

```java
// Adding a new product type without modifying existing code

// Existing code (never modified):
public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
    String send(String message);
}

public abstract class NotificationFactory {
    public abstract Notification createNotification();

    public String notify(String message) {
        Notification notification = createNotification();
        return notification.send(message);
    }
}

// Extension (new code only):
public record PushNotification() implements Notification {
    @Override
    public String send(String message) {
        return "Push: " + message;
    }
}

public class PushNotificationFactory extends NotificationFactory {
    @Override
    public Notification createNotification() {
        return new PushNotification();
    }
}
```

**Q3: Can you give real-world examples of Factory Method in the JDK?**

```text
A3: The JDK contains many examples of the Factory Method pattern:

1. Collection.iterator(): Each Collection implementation (ArrayList, HashSet, TreeMap.keySet()) returns its own
   Iterator implementation. The client code works with Iterator interface only.

2. URLStreamHandlerFactory.createURLStreamHandler(): Returns protocol-specific handlers (HTTP, FTP, etc.).

3. javax.xml.parsers.DocumentBuilderFactory.newInstance(): Returns a platform-specific DocumentBuilderFactory.

4. java.nio.charset.Charset.newDecoder(): Each Charset subclass returns its own CharsetDecoder.

5. java.util.ResourceBundle.getBundle(): Returns a locale-specific ResourceBundle subclass.

6. java.text.NumberFormat.getInstance(): Returns a locale-specific NumberFormat implementation.

Static factory methods (a simpler related concept):
- List.of(), Set.of(), Map.of()
- Optional.of(), Optional.empty()
- Integer.valueOf()
- EnumSet.of()

These are technically Simple Factory (static methods), not the full Factory Method pattern, but they demonstrate
the principle of abstracting object creation.
```

```java
// JDK Factory Method in action
import java.util.*;

public class JdkFactoryMethodExamples {
    public static void main(String[] args) {
        // Collection.iterator() -- each collection returns its own Iterator type
        List<String> arrayList = new ArrayList<>(List.of("a", "b", "c"));
        Iterator<String> arrayIter = arrayList.iterator(); // Returns ArrayList$Itr

        Set<String> hashSet = new HashSet<>(Set.of("x", "y", "z"));
        Iterator<String> hashIter = hashSet.iterator();    // Returns HashMap$KeyIterator

        // Client code works with Iterator interface -- doesn't know the concrete type
        while (arrayIter.hasNext()) {
            System.out.println(arrayIter.next());
        }
    }
}
```

**Q4: How can you implement Factory Method using modern Java features like sealed interfaces and records?**

```text
A4: Modern Java features make the Factory Method pattern more concise and type-safe:

1. Sealed interfaces restrict which product types can exist, enabling compile-time exhaustiveness checks.
2. Records provide concise, immutable product implementations.
3. Pattern matching for switch allows elegant product handling without instanceof chains.
4. Functional interfaces (Supplier<Product>) can replace Creator subclasses in simple cases.

The combination of sealed interfaces with records creates a "closed" product hierarchy where the compiler
enforces that all cases are handled.
```

```java
// Modern Factory Method with sealed interfaces and records
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
    String describe();
}

public record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
    public String describe() { return "Circle with radius " + radius; }
}

public record Rectangle(double width, double height) implements Shape {
    public double area() { return width * height; }
    public String describe() { return "Rectangle " + width + "x" + height; }
}

public record Triangle(double base, double height) implements Shape {
    public double area() { return 0.5 * base * height; }
    public String describe() { return "Triangle base=" + base + " height=" + height; }
}

// Pattern matching over sealed products
public String formatShape(Shape shape) {
    return switch (shape) {
        case Circle c    -> "Round shape: " + c.describe();
        case Rectangle r -> "Rectangular: " + r.describe();
        case Triangle t  -> "Triangular: " + t.describe();
    };  // Exhaustive -- compiler enforces all cases
}
```

**Q5: What is the difference between Factory Method and Abstract Factory?**

```text
A5: Both patterns abstract object creation, but they differ in scope and mechanism:

Factory Method:
- Creates ONE product.
- Uses inheritance: a subclass overrides a single factory method.
- The Creator class has other logic that depends on the product.
- Example: A Document class with a createPage() method -- subclasses return different Page types.

Abstract Factory:
- Creates a FAMILY of related products.
- Uses composition: the factory object is passed to client code.
- Each factory provides methods for creating each product in the family.
- Example: A UIFactory with createButton(), createTextField(), createMenu() -- WindowsUIFactory and MacUIFactory
  each return platform-specific widgets.

When to use which:
- Factory Method: Single product, creation logic varies by subclass.
- Abstract Factory: Multiple related products that must be used together (e.g., ensuring all UI widgets match the
  same platform theme).

Abstract Factory often uses Factory Methods internally -- each createXxx() method in the Abstract Factory is a
Factory Method.
```

```java
// Factory Method -- single product
public abstract class PageCreator {
    public abstract Page createPage();
}

// Abstract Factory -- family of related products
public interface UIComponentFactory {
    Button createButton();
    TextField createTextField();
    Menu createMenu();
}

public class WindowsUIFactory implements UIComponentFactory {
    @Override public Button createButton() { return new WindowsButton(); }
    @Override public TextField createTextField() { return new WindowsTextField(); }
    @Override public Menu createMenu() { return new WindowsMenu(); }
}
```

**Q6: When should you NOT use the Factory Method pattern?**

```text
A6: The Factory Method pattern adds complexity. Avoid it when:

1. Only one product type exists and there is no foreseeable need for variation. A direct constructor call is simpler.

2. The product creation logic is trivial (no conditional logic, no configuration, no polymorphism needed).

3. The pattern leads to a parallel class hierarchy explosion -- every new product requires a new creator subclass.
   In this case, consider lambda-based factories or a registry pattern.

4. You are already using a dependency injection framework (Spring, Guice) that handles object creation and wiring.

5. Performance-critical code where the indirection of a virtual method call matters (rare in practice).

6. The code is a simple script or utility where design pattern overhead is not justified.

The key question to ask: "Will I ever need to substitute different product implementations?" If the answer is
"probably not," skip the pattern.
```

```java
// Over-engineering: Factory Method for a single, simple type
// BAD -- unnecessary complexity
public abstract class StringProcessorFactory {
    public abstract StringProcessor create();
}
public class DefaultStringProcessorFactory extends StringProcessorFactory {
    @Override
    public StringProcessor create() { return new DefaultStringProcessor(); }
}

// GOOD -- just use the constructor
StringProcessor processor = new DefaultStringProcessor();

// Or use a Supplier if you need deferred creation
Supplier<StringProcessor> factory = DefaultStringProcessor::new;
```

## Code Examples

- Source: [FactoryMethodPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/FactoryMethodPattern.java)
- Test: [FactoryMethodPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/FactoryMethodPatternTest.java)
