# 7.2.2. Declaring Generic Enums

## Concept Explanation

Java enums cannot have type parameters -- `enum Setting<T> { ... }` is a compile-time error. This is a fundamental
language constraint because the JVM creates enum constants at class loading time, and generic type information is erased
at that point. However, senior Java engineers frequently need "enum-like" constructs where each constant carries its own
type parameter (e.g., a configuration key where `MAX_RETRIES` is typed as `Integer` while `APP_NAME` is typed
as `String`).

Several well-established workaround patterns exist to simulate generic enums. These patterns trade off between type
safety, convenience, and complexity. The most important ones are:

1. **Interface-based pattern** -- define a generic interface, implement it with multiple records or classes.
2. **Sealed interface with records** (Java 17+) -- the modern, type-safe approach using sealed hierarchies.
3. **Enum with Class token** -- store a `Class<?>` in each constant for runtime type checking.
4. **Static final instances** -- use a class with `public static final` fields that mimic enum constants.
5. **Supplier-based lazy pattern** -- use `Supplier<?>` for deferred, type-varying value generation.

**Real-world analogy**: Imagine a form builder where each field has a different value type: a text field holds a String,
a number field holds an Integer, a checkbox holds a Boolean. A traditional enum can only represent "field types" but
cannot carry the value type generically. The patterns described here let you build a type-safe form field registry.

## Key Points to Remember

1. **`enum Foo<T>` is illegal** -- the Java language specification forbids generic enum types.
2. **Type erasure** is the root cause -- generics are compile-time only; enums are instantiated by the JVM at class
   load.
3. **Sealed interfaces + records** is the preferred modern pattern (Java 17+) for type-safe "generic enum" behavior.
4. **Class tokens** (`Class<T>`) provide runtime type checking but lack compile-time type parameter linkage between
   constants.
5. **Static final instances** give full generic support but lose `EnumSet`, `EnumMap`, `name()`, `ordinal()`, and switch
   exhaustiveness.
6. **Type-safe heterogeneous containers** (from Effective Java Item 33) work well with any of these patterns.
7. Pattern matching in switch (Java 21) works with sealed hierarchies, providing exhaustiveness checking comparable to
   enums.

## Relevant Java 21 Features

- **Sealed classes (JEP 409)**: The cornerstone of the modern "generic enum" pattern. A sealed interface restricts which
  classes can implement it, creating a closed set of subtypes analogous to enum constants.
- **Record patterns (JEP 440)**: Enable destructuring records in switch expressions, allowing concise handling of
  sealed-type members.
- **Pattern matching for switch (JEP 441)**: Provides exhaustive switch on sealed types, which is the key feature that
  makes sealed interfaces a true alternative to enums.
- **Type inference improvements**: Better `var` inference and diamond operator support make working with generic patterns
  less verbose.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to seal the interface**

   ```java
   // Problem: Any class can implement this -- no closed set of "constants"
   interface Setting<T> { T defaultValue(); }
   ```

   **Fix**: Use `sealed interface Setting<T> permits ...` to restrict implementations.

2. **Unchecked casts with Class tokens**

   ```java
   // Problem: Compiler warnings and potential ClassCastException
   enum ConfigKey {
       PORT(Integer.class, 8080);
       private final Object defaultValue;
       @SuppressWarnings("unchecked")
       public <T> T getDefault() { return (T) defaultValue; }
   }
   // String s = ConfigKey.PORT.getDefault(); // ClassCastException at runtime!
   ```

   **Fix**: Add a `cast()` method that uses `Class.cast()` and validate types at the call site.

3. **Losing enum-specific features**

   ```java
   // Problem: Static final instances don't support EnumSet, valueOf(), ordinal()
   class Key<T> {
       public static final Key<Integer> PORT = new Key<>("port", Integer.class, 8080);
       // No EnumSet.of(Key.PORT) -- not an enum!
   }
   ```

   **Fix**: Accept the trade-off, or use a hybrid approach with an enum + companion generic wrapper.

4. **Mutable sealed hierarchy members**

   ```java
   // Problem: Records are immutable, but if you use classes instead of records
   // in a sealed hierarchy, you lose immutability guarantees
   sealed interface Setting<T> permits MutableSetting { ... }
   final class MutableSetting<T> implements Setting<T> {
       private T value; // Mutable -- breaks assumptions
   }
   ```

   **Fix**: Prefer records for sealed hierarchy members to guarantee immutability.

## Best Practices and Optimization Techniques

1. **Prefer sealed interfaces + records** for new code targeting Java 17+. This gives compile-time type safety,
   exhaustive switch support, immutability, and a natural `toString()`/`equals()`/`hashCode()`.
2. **Provide a static `values()` method** on the sealed interface to mimic `Enum.values()`.
3. **Use a type-safe heterogeneous container** (Map keyed by the generic setting type) for storing configuration values.
4. **Cache instances** if using records -- since records support value-based equality, create singleton instances in
   static fields if instantiation cost matters.
5. **Document the pattern** -- generic enum workarounds are less familiar to junior developers. Add Javadoc explaining
   the design decision.

## Edge Cases and Their Handling

1. **Null default values**: Some settings may legitimately default to `null`. Use `Optional<T>` as the default value type
   or document the null contract clearly.
2. **Parameterized types as type tokens**: `Class<List<String>>` cannot be expressed directly due to erasure. Use a
   custom `TypeToken` or `TypeReference` pattern (as in Jackson or Guava).
3. **Equality of sealed-interface instances**: Records provide value-based equality. If two `MaxRetries()` instances are
   created, they are `.equals()` but not `==`. This differs from enum behavior where `==` is the standard comparison.
4. **Serialization**: Records are serializable if they implement `Serializable`. Unlike enums, deserialization creates
   new instances rather than returning the singleton.

## Interview-specific Insights

This topic is a favorite in senior-level interviews because it tests:

- Deep understanding of the Java type system and type erasure
- Ability to work around language limitations with design patterns
- Knowledge of modern Java features (sealed classes, records, pattern matching)
- Judgment about trade-offs between different approaches

Expect follow-up questions about:

- "Why can't enums be generic?" (type erasure + JVM instantiation model)
- "How would you design a type-safe configuration system?" (sealed interface + type-safe heterogeneous container)
- "What are the trade-offs of each pattern?"

## Interview Q&A Section

**Q1: Why can't Java enums have type parameters?**

```text
A1: Java enums cannot have type parameters for several interconnected reasons:

1. Type Erasure: Generic type information is erased at runtime. But enum constants
   are concrete instances that exist at runtime. If you wrote `enum Setting<T>`,
   each constant would need a concrete T, but there is no mechanism in the JVM
   to maintain different type parameters for different constants of the same enum class.

2. JVM Instantiation: Enum constants are created by the JVM during class loading.
   The JVM creates exactly one instance per constant using reflection-like mechanisms.
   Generic types would require reification (runtime type information), which Java
   does not support.

3. Shared Class: All enum constants share the same Class object. There is no way to
   have Setting.MAX_RETRIES be a Setting<Integer> while Setting.APP_NAME is a
   Setting<String> because both are instances of the same Setting class.

4. Language Design: The enum specification (JLS 8.9) explicitly states that enum
   declarations cannot have type parameters. This is a deliberate simplification.

The practical impact is that any enum method returning a per-constant typed value
must use Object and unsafe casts, or you must use workaround patterns.
```

```java
// ILLEGAL: enum Setting<T> { MAX_RETRIES(3), APP_NAME("MyApp"); }

// Why? After type erasure, both constants would be Setting<Object>.
// The compiler cannot enforce that MAX_RETRIES.getValue() returns Integer
// while APP_NAME.getValue() returns String.
```

**Q2: How do you implement a type-safe "generic enum" using sealed interfaces?**

```text
A2: The sealed interface pattern uses Java 17+ sealed interfaces combined with records
to create a closed set of type-safe "constants". Each record acts as an enum constant
with its own generic type parameter.

Steps:
1. Define a sealed generic interface with the common API.
2. List all permitted implementations (your "constants").
3. Implement each as a record with the appropriate type parameter.
4. Optionally provide a static values() method returning all instances.
5. Use pattern matching in switch for exhaustive handling.

Advantages over traditional enums:
- Full compile-time type safety per constant
- Exhaustive switch support (Java 21)
- Immutability (records)
- Natural toString/equals/hashCode

Disadvantages:
- No ordinal(), name(), or valueOf()
- No EnumSet/EnumMap support
- Instances are value-equal, not identity-equal
```

```java
sealed interface TypedSetting<T>
        permits TypedSetting.MaxRetries, TypedSetting.AppName, TypedSetting.Debug {
    
    String key();
    T defaultValue();
    Class<T> valueType();
    
    record MaxRetries() implements TypedSetting<Integer> {
        public String key() { return "max.retries"; }
        public Integer defaultValue() { return 3; }
        public Class<Integer> valueType() { return Integer.class; }
    }
    
    record AppName() implements TypedSetting<String> {
        public String key() { return "app.name"; }
        public String defaultValue() { return "MyApp"; }
        public Class<String> valueType() { return String.class; }
    }
    
    record Debug() implements TypedSetting<Boolean> {
        public String key() { return "debug"; }
        public Boolean defaultValue() { return false; }
        public Class<Boolean> valueType() { return Boolean.class; }
    }
    
    static List<TypedSetting<?>> values() {
        return List.of(new MaxRetries(), new AppName(), new Debug());
    }
}
```

**Q3: What is the "enum with Class token" pattern and when would you use it?**

```text
A3: The "enum with Class token" pattern stores a Class<?> object in each enum constant
to enable runtime type checking. The constant carries its expected type as metadata.

When to use:
- When you need enum features (EnumSet, valueOf, ordinal, switch)
- When runtime type checking is acceptable (no compile-time guarantee)
- When the API is internal and callers can be trusted to use correct types

Limitations:
- Requires @SuppressWarnings("unchecked") on generic methods
- Type safety is enforced at runtime, not compile time
- Cannot express parameterized types (List<String>.class doesn't exist)
- Wrong usage causes ClassCastException at runtime, not compile error

This pattern is a pragmatic middle ground: you keep enum ergonomics but add
partial type awareness through the class token.
```

```java
enum ConfigKey {
    MAX_CONNECTIONS("max.connections", Integer.class, 10),
    SERVER_HOST("server.host", String.class, "localhost"),
    ENABLE_SSL("enable.ssl", Boolean.class, false);
    
    private final String key;
    private final Class<?> type;
    private final Object defaultValue;
    
    ConfigKey(String key, Class<?> type, Object defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T cast(Object value) {
        if (value == null) return (T) defaultValue;
        if (!type.isInstance(value)) {
            throw new ClassCastException("Expected " + type.getName());
        }
        return (T) value;
    }
}
```

**Q4: How does the static-final-instances pattern work, and what are its trade-offs?**

```text
A4: The static-final-instances pattern simulates enums using a class with
public static final fields. Each field is an instance of the class with its
own generic type parameter.

How it works:
- Define a generic class (e.g., TypedKey<T>)
- Create public static final instances as "constants"
- Maintain a static list of all instances for a values() method
- Override equals/hashCode for value-based identity

Trade-offs:
+ Full generic type safety per instance
+ Can carry any type parameter, including parameterized types
+ Works with any Java version (no sealed classes required)

- No EnumSet or EnumMap support
- No switch statement support (unless using if-else or pattern matching)
- No ordinal() or name() (must implement manually)
- Instances can be created outside the class (not truly a closed set)
- Value equality, not identity equality
- No serialization singleton guarantee
```

```java
public final class TypedKey<T> {
    private static final List<TypedKey<?>> ALL = new ArrayList<>();
    
    public static final TypedKey<Integer> PORT =
        new TypedKey<>("port", Integer.class, 8080);
    public static final TypedKey<String> HOST =
        new TypedKey<>("host", String.class, "0.0.0.0");
    public static final TypedKey<List<String>> ORIGINS =
        new TypedKey<>("origins", null, List.of("*"));
    
    private final String name;
    private final Class<T> type;
    private final T defaultValue;
    
    private TypedKey(String name, Class<T> type, T defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        ALL.add(this);
    }
    
    public static List<TypedKey<?>> values() {
        return Collections.unmodifiableList(ALL);
    }
}
```

**Q5: What is a type-safe heterogeneous container and how does it relate to generic enums?**

```text
A5: A type-safe heterogeneous container (from Effective Java, Item 33) is a Map-like
data structure where each key carries its own type parameter, and the container
ensures that the value stored for each key matches the key's type.

How it relates to generic enums:
- Generic enum patterns define "keys" (settings, config keys) with per-key types.
- The heterogeneous container is the natural "store" for values associated with
  these keys.
- Together they form a complete type-safe configuration system.

Implementation:
1. The "key" type has a generic parameter T and a Class<T> token.
2. The container stores values as Object internally.
3. On get(), the container uses the key's Class<T> to cast the value safely.
4. On put(), the container can optionally validate the value type.

This pattern eliminates unchecked casts at the call site -- the container handles
all casting internally using the type token.
```

```java
class TypeSafeConfig {
    private final Map<String, Object> store = new HashMap<>();
    
    public <T> void put(TypedSetting<T> setting, T value) {
        Objects.requireNonNull(value);
        store.put(setting.key(), value);
    }
    
    public <T> T get(TypedSetting<T> setting) {
        Object raw = store.get(setting.key());
        return setting.cast(raw); // Type-safe cast using the setting's type token
    }
}

// Usage -- fully type-safe, no casts at the call site:
var config = new TypeSafeConfig();
config.put(new TypedSetting.MaxRetries(), 5);       // Only Integer accepted
config.put(new TypedSetting.AppName(), "MyApp");     // Only String accepted
Integer retries = config.get(new TypedSetting.MaxRetries()); // Returns Integer
```

**Q6: How do you choose between the different generic enum patterns?**

```text
A6: Decision criteria:

1. Sealed interface + records (Java 17+):
   - Best when: You need compile-time type safety AND exhaustive switch.
   - Avoid when: You need EnumSet/EnumMap performance or ordinal support.

2. Enum with Class token:
   - Best when: You need enum features (switch, EnumSet, valueOf) and
     runtime type checking is acceptable.
   - Avoid when: You need compile-time guarantee that callers use correct types.

3. Static final instances:
   - Best when: You need full generics including parameterized types
     (e.g., TypedKey<List<String>>).
   - Avoid when: You need a truly closed set of constants.

4. Interface-based (unsealed):
   - Best when: Extensibility is desired (anyone can add new settings).
   - Avoid when: You need a closed, known set of constants.

5. Supplier-based:
   - Best when: Values are lazily generated and vary per invocation.
   - Avoid when: You need stable, cacheable default values.

In practice, most senior engineers choose sealed interface + records for new
Java 17+ projects, and enum + Class token for libraries that must support
older Java versions.
```

```java
// Decision tree in code:
// Q: Do you need per-constant type parameters?
//    No  -> Use a regular enum.
//    Yes -> Q: Java 17+ available?
//            Yes -> Use sealed interface + records.
//            No  -> Q: Need enum features (EnumSet, switch)?
//                    Yes -> Enum + Class token.
//                    No  -> Static final instances.
```

## Code Examples

- Test: [GenericEnumPatternsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumPatternsTest.java)
- Source: [GenericEnumPatterns.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumPatterns.java)
