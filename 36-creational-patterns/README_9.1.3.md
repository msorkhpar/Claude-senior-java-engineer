# 9.1.3. Builder Pattern

## Concept Explanation

The Builder pattern is a creational design pattern that lets you construct complex objects step by step. It separates
the construction of a complex object from its representation, allowing the same construction process to create different
representations.

**Real-world analogy**: Think of ordering a custom pizza. You do not say "give me pizza #47 from the menu." Instead,
you build your pizza step by step: choose the size, add sauce, pick cheese, add toppings one by one, and finally say
"build it." The pizza builder (the person making your order) follows your instructions in any order and produces the
final pizza only when you say you are done. Different sequences of steps produce different pizzas.

### The Problem It Solves

When a class has many optional parameters, constructors become unwieldy:

```java
// Telescoping constructor anti-pattern
public User(String name) { ... }
public User(String name, int age) { ... }
public User(String name, int age, String email) { ... }
public User(String name, int age, String email, String phone, String address, boolean active) { ... }
```

The Builder pattern eliminates this explosion of constructors and provides a fluent, readable API for constructing
objects.

### Structure

1. **Product** -- the complex object being built.
2. **Builder** -- an interface or abstract class defining the construction steps.
3. **ConcreteBuilder** -- implements the Builder interface and assembles the Product.
4. **Director** (optional) -- defines the order in which to call building steps. Not always needed in the fluent builder
   variant common in Java.

### Two Styles of Builder in Java

1. **GoF Builder** (classic): Separates Builder interface from Product. A Director orchestrates the build steps.
   Used when the same build process should create different representations.
2. **Fluent Builder** (Josh Bloch, Effective Java): The Builder is a static inner class of the Product. Returns `this`
   from each setter for method chaining. This is the most common style in Java.

## Key Points to Remember

1. The Builder pattern is ideal when a class has **4+ optional parameters** or complex construction logic.
2. The fluent builder (static inner class) is the most common variant in modern Java.
3. Builder produces **immutable objects** by design -- all fields are set during construction and made final.
4. The `build()` method is where **validation** should occur -- fail fast before creating an invalid object.
5. Builder is different from the Factory pattern: Builder constructs a complex object step by step, while Factory creates
   objects in one step.
6. Java records can reduce Builder boilerplate for simple cases, but Builder is still needed for complex validation
   and optional parameters.
7. `StringBuilder`, `Stream.Builder`, and `Locale.Builder` are JDK examples of the Builder pattern.
8. The GoF Builder with a Director is useful when the same construction steps should produce different representations
   (e.g., building an HTML document vs. a PDF document from the same data).

## Relevant Java 21 Features

- **Records**: For simple immutable data objects, records eliminate the need for a Builder. But when optional parameters,
  validation, or step-by-step construction are needed, Builder remains essential.
- **Sealed interfaces**: The Product or Builder hierarchy can be sealed for type safety.
- **Text blocks**: Useful for demonstrating Builders that construct strings (like SQL, HTML, or JSON builders).
- **Pattern matching**: Can be used on products built by different builders when the Product is a sealed type.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to validate in build()**:
   ```java
   // BAD: No validation
   public User build() {
       return new User(this);
   }
   ```
   **Solution**: Always validate required fields and invariants in `build()`.
   ```java
   public User build() {
       if (name == null || name.isBlank()) {
           throw new IllegalStateException("Name is required");
       }
       return new User(this);
   }
   ```

2. **Mutable Builder reuse**: Using the same Builder instance to build multiple objects can lead to unintended state
   sharing.
   ```java
   Builder builder = new Builder().name("Alice");
   User user1 = builder.age(25).build();
   User user2 = builder.age(30).build(); // user2 also has name "Alice" -- intended?
   ```
   **Solution**: Document whether the Builder is reusable. Consider resetting state after `build()` or making the
   Builder single-use.

3. **Builder with too many steps**: If the Builder has 20+ methods, the object is likely too complex.
   **Solution**: Break the object into smaller objects, each with its own Builder if needed.

4. **Not making the Product immutable**: The whole point of Builder is to create a fully initialized, often immutable
   object. Providing setters on the Product defeats the purpose.
   **Solution**: Make Product fields `final` and provide no setters.

5. **Exposing the Product constructor**: If the Product has a public constructor, clients can bypass the Builder.
   **Solution**: Make the Product constructor `private` and only accessible from the Builder (which is a static inner
   class).

## Best Practices and Optimization Techniques

1. **Make the Builder a static inner class** of the Product for encapsulation.
2. **Return `this`** from every setter method to enable fluent chaining.
3. **Use meaningful method names** -- `withAge()`, `addTopping()`, `setName()` or just `age()`, `topping()`, `name()`.
4. **Validate in build()** -- check required fields, invariants, and consistency.
5. **Make the Product immutable** -- `final` fields, no setters, defensive copies for mutable fields.
6. **Consider a required-parameters constructor** on the Builder for mandatory fields.
7. **Provide sensible defaults** for optional parameters.
8. **Use generics** for type-safe builders in inheritance hierarchies (the "Curiously Recurring Template Pattern").
9. **Consider Lombok's @Builder** for reducing boilerplate in production code (but understand the manual approach for
   interviews).

## Edge Cases and Their Handling

1. **Null values**: Decide whether null is allowed for optional fields. Use `Objects.requireNonNull()` for required
   fields.
2. **Empty strings**: Treat `""` and `"  "` as missing for String fields. Use `isBlank()` checks.
3. **Negative numbers**: Validate numeric constraints (e.g., age >= 0, price > 0) in `build()`.
4. **Collection fields**: Use defensive copies to prevent external modification after building.
   ```java
   this.tags = List.copyOf(builder.tags); // Immutable copy
   ```
5. **Builder reuse**: Clarify whether calling `build()` resets the builder or leaves it in a reusable state.
6. **Thread safety**: Builders are typically not thread-safe. Each thread should have its own Builder instance.

## Interview-specific Insights

Interviewers often focus on:

- Why Builder over telescoping constructors or JavaBeans (setters)
- How Builder enforces immutability
- Where validation should occur (in build(), not in setters)
- The difference between GoF Builder and the fluent Builder (Effective Java)
- How Builder relates to the Dependency Inversion Principle
- Real-world examples: StringBuilder, Protobuf builders, OkHttp Request.Builder

Common tricky questions:

- "What is the difference between Builder and Factory Method?"
- "How do you handle required vs. optional parameters in a Builder?"
- "Can a Builder be thread-safe? Should it be?"
- "How do you implement a Builder in an inheritance hierarchy?"

## Interview Q&A Section

**Q1: Why use the Builder pattern instead of telescoping constructors or JavaBeans setters?**

```text
A1: There are three common approaches to constructing objects with many parameters. Each has trade-offs:

1. Telescoping Constructors:
   - Every combination of optional parameters gets its own constructor.
   - Problem: Unreadable for 4+ parameters. "What does the 5th argument mean?" Hard to maintain.

2. JavaBeans (setters):
   - Create the object with a no-arg constructor, then call setters.
   - Problem: The object is in an inconsistent state between construction and the last setter call. Cannot be immutable.

3. Builder:
   - Fluent API for step-by-step construction.
   - Advantages: Readable (named methods), immutable products, validation in build(), works with any number of
     optional parameters.
   - Trade-off: More boilerplate code (mitigated by IDE generation or Lombok).

The Builder pattern wins when:
- The class has 4+ parameters (especially optional ones).
- Immutability is desired.
- Construction requires validation or complex logic.
- Readability of construction code matters.
```

```java
// Telescoping constructor (hard to read)
User user1 = new User("Alice", 25, "alice@example.com", null, null, true);

// JavaBeans (mutable, inconsistent state possible)
User user2 = new User();
user2.setName("Alice");
user2.setAge(25);
// Object exists but is incomplete here!
user2.setEmail("alice@example.com");

// Builder (readable, validated, immutable)
User user3 = User.builder()
    .name("Alice")
    .age(25)
    .email("alice@example.com")
    .active(true)
    .build();
```

**Q2: How do you handle required vs. optional parameters in a Builder?**

```text
A2: There are several strategies:

1. Constructor parameters on the Builder: Put required parameters in the Builder's constructor. Optional parameters
   are set via fluent methods.

2. Validation in build(): Accept all parameters via fluent methods but validate required ones when build() is called.
   Throw IllegalStateException if required fields are missing.

3. Step Builder (type-safe): Use a chain of interfaces where each required parameter returns the next interface.
   The build() method is only available after all required parameters are set. This provides compile-time safety
   but is verbose.

4. Annotations: Use @NonNull or @Required annotations on fields and validate at build time.

Recommendation: For most cases, use required parameters in the Builder constructor and validate in build(). Use the
Step Builder only when compile-time safety is critical.
```

```java
// Strategy 1: Required parameters in Builder constructor
public class HttpRequest {
    private final String url;     // Required
    private final String method;  // Required
    private final Map<String, String> headers; // Optional
    private final String body;    // Optional

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Map.copyOf(builder.headers);
        this.body = builder.body;
    }

    public static class Builder {
        private final String url;    // Required -- set in constructor
        private final String method; // Required -- set in constructor
        private Map<String, String> headers = new HashMap<>();
        private String body;

        public Builder(String url, String method) {
            this.url = Objects.requireNonNull(url, "URL is required");
            this.method = Objects.requireNonNull(method, "Method is required");
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }
}

// Usage:
HttpRequest request = new HttpRequest.Builder("https://api.example.com", "POST")
    .header("Content-Type", "application/json")
    .body("{\"key\": \"value\"}")
    .build();
```

**Q3: What is the difference between the GoF Builder and the Effective Java (fluent) Builder?**

```text
A3: The GoF (Gang of Four) Builder and the Effective Java Builder serve different purposes:

GoF Builder:
- Separates the Builder interface from the Director.
- The Director defines the ORDER of construction steps.
- Multiple ConcreteBuilders can produce different representations from the same construction process.
- Example: A DocumentBuilder with buildTitle(), buildBody(), buildFooter(). An HtmlDocumentBuilder and a
  PdfDocumentBuilder each produce different output from the same steps.

Effective Java (Fluent) Builder:
- The Builder is a static inner class of the Product.
- No Director -- the client controls the order of steps.
- Focuses on solving the telescoping constructor problem.
- Returns 'this' for method chaining (fluent API).
- Typically builds only one type of Product.

In practice:
- The fluent Builder is far more common in Java codebases.
- The GoF Builder is used when you need the same construction process to produce different representations.
- Modern Java developers usually mean the fluent Builder when they say "Builder pattern."
```

```java
// GoF Builder with Director
public interface MealBuilder {
    void buildDrink();
    void buildMainCourse();
    void buildDessert();
    Meal getMeal();
}

public class Director {
    public Meal constructKidsMeal(MealBuilder builder) {
        builder.buildDrink();
        builder.buildMainCourse();
        builder.buildDessert();
        return builder.getMeal();
    }
}

public class HealthyMealBuilder implements MealBuilder {
    private final Meal meal = new Meal();
    @Override public void buildDrink() { meal.setDrink("Water"); }
    @Override public void buildMainCourse() { meal.setMainCourse("Grilled chicken"); }
    @Override public void buildDessert() { meal.setDessert("Fruit salad"); }
    @Override public Meal getMeal() { return meal; }
}

// Fluent Builder (Effective Java)
Meal meal = Meal.builder()
    .drink("Water")
    .mainCourse("Grilled chicken")
    .dessert("Fruit salad")
    .build();
```

**Q4: How do you implement a Builder for a class hierarchy (inheritance)?**

```text
A4: Implementing Builder in an inheritance hierarchy is tricky because the fluent builder's chaining methods return
'this', but subclass builders need to return their own type, not the parent builder type.

The solution is the "Curiously Recurring Generic Pattern" (also called the "self-type" idiom):

1. The base Builder is generic: Builder<T extends Builder<T>>
2. Each setter returns T (the actual builder subtype) instead of Builder.
3. A protected abstract method self() returns 'this' cast to T.
4. Concrete builders extend the base and specify themselves as T.

This allows subclass builders to add new methods while inheriting parent builder methods, and the fluent chain
always returns the correct type.
```

```java
// Base class with generic Builder
public abstract class Pizza {
    private final String size;
    private final boolean cheese;

    protected Pizza(Builder<?> builder) {
        this.size = builder.size;
        this.cheese = builder.cheese;
    }

    public abstract static class Builder<T extends Builder<T>> {
        private final String size;
        private boolean cheese = false;

        public Builder(String size) {
            this.size = Objects.requireNonNull(size);
        }

        public T cheese(boolean cheese) {
            this.cheese = cheese;
            return self();
        }

        protected abstract T self();
        public abstract Pizza build();
    }
}

// Subclass with its own Builder
public class NyStylePizza extends Pizza {
    private final boolean thinCrust;

    private NyStylePizza(Builder builder) {
        super(builder);
        this.thinCrust = builder.thinCrust;
    }

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean thinCrust = true;

        public Builder(String size) { super(size); }

        public Builder thinCrust(boolean thinCrust) {
            this.thinCrust = thinCrust;
            return this;
        }

        @Override protected Builder self() { return this; }
        @Override public NyStylePizza build() { return new NyStylePizza(this); }
    }
}

// Usage -- fluent chain returns the correct type throughout:
NyStylePizza pizza = new NyStylePizza.Builder("Large")
    .cheese(true)       // Returns NyStylePizza.Builder (not Pizza.Builder)
    .thinCrust(false)   // This works because cheese() returned the right type
    .build();
```

**Q5: What is the difference between Builder and Factory Method patterns?**

```text
A5: Builder and Factory Method solve different problems:

Builder:
- Constructs a complex object STEP BY STEP.
- The client controls the construction process (which steps, in what order).
- Used when the object has many optional parameters or complex initialization.
- The product is usually immutable once built.
- Example: Building a complex HTTP request with optional headers, body, timeouts.

Factory Method:
- Creates an object in ONE STEP.
- The subclass decides which type to instantiate.
- Used when the type of object to create is determined at runtime by subclass.
- The focus is on WHICH type to create, not HOW to construct it.
- Example: A document application where each document type creates its own pages.

Can they work together?
Yes. A Builder might use a Factory Method internally to create components. For example, a ComputerBuilder might use
a ComponentFactory to create CPU, RAM, and Storage objects during the build process.
```

```java
// Builder: step-by-step construction, many parameters
Computer computer = Computer.builder()
    .cpu("Intel i9")
    .ram(32)
    .storage("1TB SSD")
    .gpu("RTX 4090")
    .build();

// Factory Method: one-step creation, type varies by subclass
public abstract class DocumentFactory {
    public abstract Document createDocument();
}
Document doc = new PdfDocumentFactory().createDocument();
```

**Q6: How does the Builder pattern help achieve immutability?**

```text
A6: The Builder pattern is one of the best ways to create immutable objects with many fields:

1. The Product class has only final fields -- no setters.
2. The private constructor takes a Builder and extracts all values.
3. Mutable collections are defensively copied (List.copyOf(), Map.copyOf()).
4. The Builder accumulates state through its fluent methods.
5. Validation in build() ensures the object is always in a valid state.

Once build() returns, the Product is fully initialized and immutable. No code can modify it.

Without Builder, creating an immutable object with 10 fields would require a constructor with 10 parameters --
unreadable and error-prone. With Builder, each parameter is named and optional parameters have defaults.

The key insight: mutability exists only during construction (in the Builder), and the final product is completely
immutable.
```

```java
public final class ImmutableConfig {
    private final String host;
    private final int port;
    private final boolean ssl;
    private final List<String> allowedOrigins;
    private final Map<String, String> properties;

    private ImmutableConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.ssl = builder.ssl;
        this.allowedOrigins = List.copyOf(builder.allowedOrigins);  // Defensive copy
        this.properties = Map.copyOf(builder.properties);            // Defensive copy
    }

    // Only getters, no setters
    public String host() { return host; }
    public int port() { return port; }
    public boolean ssl() { return ssl; }
    public List<String> allowedOrigins() { return allowedOrigins; }
    public Map<String, String> properties() { return properties; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host = "localhost";
        private int port = 8080;
        private boolean ssl = false;
        private List<String> allowedOrigins = new ArrayList<>();
        private Map<String, String> properties = new HashMap<>();

        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder ssl(boolean ssl) { this.ssl = ssl; return this; }
        public Builder addOrigin(String origin) { this.allowedOrigins.add(origin); return this; }
        public Builder property(String key, String value) { this.properties.put(key, value); return this; }

        public ImmutableConfig build() {
            if (port < 0 || port > 65535) throw new IllegalStateException("Invalid port: " + port);
            return new ImmutableConfig(this);
        }
    }
}
```

## Code Examples

- Source: [BuilderPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/BuilderPattern.java)
- Test: [BuilderPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/BuilderPatternTest.java)
