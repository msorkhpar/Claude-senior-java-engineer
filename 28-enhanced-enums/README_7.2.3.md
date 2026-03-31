# 7.2.3. Parameterized Enum Constants

## Concept Explanation

Parameterized enum constants are enums whose constants carry constructor parameters — fields, behaviors, or strategies that vary per constant. This is one of Java's most powerful enum features: each constant is a fully initialized object with its own state and potentially its own method implementations via constant-specific method bodies.

**Real-world analogy**: Think of a vending machine with different product slots. Each slot (enum constant) has its own product name, price, and quantity (constructor parameters). Some slots might even have custom dispensing behavior (abstract method overrides) — the beverage slot pours while the snack slot drops. The machine itself (enum type) provides the common interface, but each slot carries its own configuration.

Key patterns for parameterized enum constants:
1. **Simple fields**: Constants with constructor-supplied immutable state (e.g., `Planet` with mass and radius)
2. **Abstract method per constant**: Each constant overrides an abstract method with its own logic (e.g., `MathOperation`)
3. **Strategy via functional interface**: Constants hold a `Function`, `UnaryOperator`, or `BiFunction` as a field (e.g., `TextTransform`)
4. **Multiple parameters with categories**: Rich constants with several fields and lookup maps (e.g., `HttpStatus`)
5. **Interface-implementing enums**: Constants that implement interfaces with per-constant behavior

## Key Points to Remember

- Enum constructors are implicitly `private` — they cannot be `public` or `protected`.
- Constructor parameters create per-constant immutable state — fields should be `final`.
- Constant-specific method bodies (anonymous class bodies) allow each constant to override abstract or non-abstract methods.
- Each constant with a body creates an anonymous subclass of the enum — this has implications for `getClass()` and serialization.
- Static fields and static initializer blocks in enums run after all constants are created.
- `EnumSet` and `EnumMap` are optimized for enums and should always be preferred over `HashSet`/`HashMap` for enum keys.
- The `values()` method creates a new array each time — cache it if called frequently.
- Enum constants are created in declaration order; `ordinal()` reflects this order.

## Relevant Java 21 Features

- **Pattern matching for switch**: Parameterized enums integrate beautifully with Java 21 switch expressions for dispatching based on both enum type and parameter values.
- **Record patterns**: You can combine enums with records (e.g., `enum + record` pairs) for algebraic data type patterns.
- **Sealed interfaces**: Enums can implement sealed interfaces, and Java 21 switch expressions provide exhaustive coverage.
- **Text blocks**: Multi-line enum descriptions or templates are cleaner with text blocks.
- **Sequenced collections**: Enum utility methods returning lists benefit from `SequencedCollection` methods.

## Common Pitfalls and How to Avoid Them

1. **Mutable state in enum constants**:
   ```java
   // WRONG: mutable fields break thread safety and predictability
   public enum Counter {
       INSTANCE;
       private int count = 0; // Mutable!
       public void increment() { count++; }
   }

   // RIGHT: keep enum state immutable, use external structures for mutable state
   public enum Counter {
       INSTANCE;
       private final AtomicInteger count = new AtomicInteger(0);
       public void increment() { count.incrementAndGet(); }
   }
   ```

2. **Forgetting that constant-specific bodies create anonymous subclasses**:
   ```java
   public enum Op {
       ADD { @Override public int apply(int a, int b) { return a + b; } },
       SUB { @Override public int apply(int a, int b) { return a - b; } };
       public abstract int apply(int a, int b);
   }
   // Op.ADD.getClass() != Op.class — it's an anonymous subclass!
   // Op.ADD.getClass().isEnum() is still true
   // But Op.ADD.getDeclaringClass() == Op.class — use this for the actual enum type
   ```

3. **Using `values()` in hot loops**:
   ```java
   // WRONG: allocates a new array every call
   for (int i = 0; i < 1_000_000; i++) {
       for (Planet p : Planet.values()) { /* ... */ }
   }

   // RIGHT: cache the values array
   private static final Planet[] PLANETS = Planet.values();
   for (int i = 0; i < 1_000_000; i++) {
       for (Planet p : PLANETS) { /* ... */ }
   }
   ```

4. **Static map initialization ordering**:
   ```java
   // WRONG: static field initialized before constants exist
   public enum Color {
       RED, GREEN, BLUE;
       // private static final Map<String, Color> MAP = new HashMap<>();
       // static { for (Color c : values()) MAP.put(c.name(), c); }
       // This actually works because static init runs AFTER constants,
       // but be careful with forward references in constructors!

       // RIGHT: use a static holder pattern or lazy initialization
       private static final Map<String, Color> MAP =
           Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(Color::name, c -> c));
   }
   ```

5. **Division by zero in arithmetic enum constants**:
   ```java
   // Must handle edge cases per-constant
   public enum MathOp {
       DIVIDE {
           @Override
           public double apply(double a, double b) {
               if (b == 0) throw new ArithmeticException("Division by zero");
               return a / b;
           }
       };
       public abstract double apply(double a, double b);
   }
   ```

## Best Practices and Optimization Techniques

- **Make constructor parameters final**: Ensures immutability and thread safety.
- **Use `Optional` for nullable constructor parameters**: Better than null checks scattered throughout.
- **Prefer functional interface fields over abstract methods** when behavior is a simple transformation — it's more concise.
- **Use abstract methods when constants need complex, multi-line implementations** — constant-specific bodies are clearer.
- **Create static lookup maps in a static initializer** for fast `fromCode()`/`fromName()` methods.
- **Implement `toString()`** to return a meaningful representation instead of the constant name.
- **Use EnumSet for flag combinations** — it's backed by a bit vector and extremely efficient.
- **Document each constant's behavior** when it overrides abstract methods — the reader shouldn't need to read the body to understand the contract.

## Edge Cases and Their Handling

- **Null constructor argument**: Enum constructors can accept null, but this defeats immutability — validate in the constructor.
- **Empty enum**: Legal but unusual — an enum with no constants (`enum Empty {}`) compiles fine.
- **Single-constant enum**: Common pattern for singletons — `enum Singleton { INSTANCE; }`.
- **Enum with no methods or fields**: Acts as a simple set of named constants — the simplest enum usage.
- **Serialization**: Enum constants are serialized by name only. Fields, even transient ones, are reconstructed via the `values()` mechanism. Custom `readObject`/`writeObject` are ignored.
- **Enum constants with identical parameters**: Legal — two constants can have the same field values but are still distinct by identity.

## Interview-specific Insights

- **"When would you use abstract methods vs. functional interface fields in enums?"** — Abstract methods for complex, multi-statement logic; functional fields for simple transformations that can be expressed as lambdas.
- **"How is the Planet enum a better alternative to constants?"** — Type safety, namespacing, ability to add behavior, serialization support, and `values()`/`valueOf()` for free.
- **"What's the Strategy pattern relationship?"** — Parameterized enums are a natural implementation of the Strategy pattern when the set of strategies is fixed and known at compile time.
- **"Explain enum serialization behavior."** — Only the name is serialized; deserialization uses `valueOf()`. Custom serialization methods are ignored.

## Interview Q&A Section

### Q1: How do you implement the Strategy pattern using parameterized enum constants?

```text
The Strategy pattern encapsulates a family of algorithms behind a common interface.
Parameterized enums implement this naturally:

1. Each enum constant represents a strategy.
2. The strategy behavior is either an abstract method overridden per constant
   or a functional interface field supplied via the constructor.
3. Client code selects a strategy by choosing an enum constant.
4. New strategies require adding a constant — compile-time safety ensures all
   switch/if-else chains are updated.

Advantages over interface-based Strategy:
- Fixed set of strategies — no accidental runtime additions
- Built-in valueOf() for configuration-driven selection
- Singleton semantics — no multiple instances
- EnumSet/EnumMap for efficient strategy collections
- Natural serialization
```

```java
public enum TextTransform {
    UPPER_CASE("Upper Case", String::toUpperCase),
    LOWER_CASE("Lower Case", String::toLowerCase),
    TRIM("Trim", String::trim),
    REVERSE("Reverse", s -> new StringBuilder(s).reverse().toString()),
    CAPITALIZE("Capitalize", s -> s.isEmpty() ? s :
        Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase());

    private final String displayName;
    private final UnaryOperator<String> transformer;

    TextTransform(String displayName, UnaryOperator<String> transformer) {
        this.displayName = displayName;
        this.transformer = transformer;
    }

    public String apply(String input) {
        return transformer.apply(Objects.requireNonNull(input));
    }

    // Chain strategies
    public static String applyAll(String input, TextTransform... transforms) {
        String result = input;
        for (TextTransform t : transforms) result = t.apply(result);
        return result;
    }
}

// Usage
String result = TextTransform.applyAll("  hello WORLD  ",
    TextTransform.TRIM, TextTransform.CAPITALIZE);
// "Hello world"
```

### Q2: Explain the Planet enum pattern and its advantages over plain constants.

```text
The Planet enum demonstrates parameterized constants with computed behavior.
Each constant carries mass and radius, and derives surfaceGravity() and
surfaceWeight() from these parameters.

Advantages over static final constants:
1. Type safety — you can't pass a random double where a Planet is expected
2. Behavior encapsulation — calculations live with the data
3. Iteration — Planet.values() gives all planets without maintaining a separate list
4. Lookup — Planet.valueOf("EARTH") for configuration-driven access
5. Switch support — exhaustive switch with compiler checking
6. Immutability — enum fields are naturally final
7. Serialization — built-in, reliable name-based serialization
8. Namespace — no pollution of the enclosing class's namespace
```

```java
public enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6);
    // ... other planets

    private final double mass;    // kg
    private final double radius;  // meters
    static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    public double surfaceWeight(double otherMass) {
        return otherMass * surfaceGravity();
    }

    // Compare with plain constants — unsafe, no behavior
    // static final double EARTH_MASS = 5.976e+24;
    // static final double EARTH_RADIUS = 6.37814e6;
    // You'd need separate utility methods, no type safety, easy to mix up
}

// Usage
double earthWeight = 75.0;
for (Planet p : Planet.values()) {
    System.out.printf("Weight on %s: %.2f N%n", p, p.surfaceWeight(earthWeight));
}
```

### Q3: How do constant-specific method bodies work?

```text
Each enum constant can have its own anonymous class body that overrides methods
declared in the enum type. This is called a "constant-specific method body."

How it works:
1. The enum declares an abstract method (or a regular method to override).
2. Each constant provides its own implementation in its body { }.
3. At compile time, each constant with a body becomes an anonymous subclass.
4. The JVM dispatches calls to the correct implementation via virtual method dispatch.

Key implications:
- getClass() returns the anonymous subclass, not the enum class itself
- getDeclaringClass() always returns the actual enum class
- Each anonymous subclass is its own .class file (Enum$1.class, Enum$2.class, etc.)
- Constants WITHOUT bodies share the enum's class directly
```

```java
public enum MathOperation {
    ADD("+") {
        @Override public double apply(double a, double b) { return a + b; }
    },
    SUBTRACT("-") {
        @Override public double apply(double a, double b) { return a - b; }
    },
    MULTIPLY("*") {
        @Override public double apply(double a, double b) { return a * b; }
    },
    DIVIDE("/") {
        @Override public double apply(double a, double b) {
            if (b == 0) throw new ArithmeticException("Division by zero");
            return a / b;
        }
    };

    private final String symbol;

    MathOperation(String symbol) { this.symbol = symbol; }

    public abstract double apply(double a, double b);

    public String format(double a, double b) {
        return "%.2f %s %.2f = %.2f".formatted(a, symbol, b, apply(a, b));
    }
}

// Class hierarchy insight
System.out.println(MathOperation.ADD.getClass().getName());
// "MathOperation$1" — anonymous subclass

System.out.println(MathOperation.ADD.getDeclaringClass().getName());
// "MathOperation" — the actual enum type
```

### Q4: How do you create fast lookup methods for parameterized enums?

```text
When you need to find an enum constant by a parameter value (e.g., code, symbol, name),
create a static Map for O(1) lookup instead of iterating values() each time.

Best practices:
1. Use an unmodifiable map for thread safety
2. Initialize in a static block (runs after all constants are created)
3. Return Optional to handle missing values gracefully
4. Consider using a switch expression for small enums (JIT-optimized)
5. Cache values() if you must iterate frequently
```

```java
public enum HttpStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error");

    private final int code;
    private final String reason;

    // Static lookup map — initialized ONCE
    private static final Map<Integer, HttpStatus> CODE_MAP =
        Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(HttpStatus::code, s -> s));

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int code() { return code; }
    public String reason() { return reason; }

    // O(1) lookup by code
    public static Optional<HttpStatus> fromCode(int code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    // With default fallback
    public static HttpStatus fromCodeOrDefault(int code, HttpStatus defaultStatus) {
        return CODE_MAP.getOrDefault(code, defaultStatus);
    }
}

// Usage
HttpStatus status = HttpStatus.fromCode(404).orElseThrow();
assert status == HttpStatus.NOT_FOUND;
```

### Q5: How does enum serialization handle parameterized constants?

```text
Enum serialization in Java is special and different from regular object serialization:

1. Only the enum constant's name() is serialized — not its fields or state.
2. Deserialization calls Enum.valueOf(Class, String) to resolve the constant.
3. Custom writeObject(), readObject(), writeReplace(), and readResolve() are IGNORED.
4. This ensures identity preservation: deserialized constant == original constant.
5. If the enum definition changes (fields added/removed), deserialization still works
   as long as the constant name still exists.
6. If a constant is removed, deserialization throws InvalidObjectException.

This makes enums inherently serialization-safe, but it also means:
- You cannot serialize mutable state carried by enum constants
- transient fields have no effect (nothing is serialized anyway)
- serialVersionUID is ignored for enums
```

```java
public enum Currency {
    USD("US Dollar", "$", 2),
    EUR("Euro", "€", 2),
    JPY("Japanese Yen", "¥", 0);

    private final String name;
    private final String symbol;
    private final int decimalPlaces;

    Currency(String name, String symbol, int decimalPlaces) {
        this.name = name;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    // These fields are NOT serialized — only "USD", "EUR", "JPY" are written
}

// Serialization round-trip
ByteArrayOutputStream baos = new ByteArrayOutputStream();
try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
    oos.writeObject(Currency.USD);
}
try (ObjectInputStream ois = new ObjectInputStream(
        new ByteArrayInputStream(baos.toByteArray()))) {
    Currency deserialized = (Currency) ois.readObject();
    assert deserialized == Currency.USD; // Same identity — not just equals!
}
```

## Code Examples

- Implementation: [ParameterizedEnumConstants.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/ParameterizedEnumConstants.java)
- Tests: [ParameterizedEnumConstantsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/ParameterizedEnumConstantsTest.java)
