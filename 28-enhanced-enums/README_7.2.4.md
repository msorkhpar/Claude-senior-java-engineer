# 7.2.4. Generic Methods in Enums

## Concept Explanation

While Java enum types themselves cannot be generic (you cannot write `enum MyEnum<T>`), their individual methods can declare type parameters. This allows enum constants to act as type-safe operations, converters, factories, or aggregators that work with any type — combining the fixed, singleton nature of enums with the flexibility of generics.

**Real-world analogy**: Consider a set of kitchen appliances (enum constants) — a blender, a toaster, a coffee maker. Each appliance is a fixed, named item (you can't create new appliance types at runtime). But each appliance can process different ingredients (generic type parameter): the blender can blend fruits, vegetables, or ice; the toaster can toast bread, bagels, or waffles. The appliance is fixed; what it processes is generic.

Key patterns for generic methods in enums:
1. **Generic converter methods**: `<T> T convert(Object input, Class<T> targetType)`
2. **Generic collection operations**: `<T> List<T> execute(List<T> input, Predicate<T> predicate)`
3. **Bounded type parameters**: `<T extends Number> double aggregate(List<T> values)`
4. **Generic factory methods**: `<T> Collection<T> create()` for producing typed collections
5. **Generic comparison strategies**: `<T> int compare(T a, T b)` for pluggable comparators

## Key Points to Remember

- Enum types cannot have type parameters (`enum Foo<T>` is illegal), but enum methods can (`<T> T convert(...)`).
- Type parameters on enum methods are resolved at the call site, not at enum declaration time.
- Abstract generic methods in enums must be overridden by each constant — each constant can have a different implementation.
- Generic methods in enums enable the Template Method, Strategy, and Factory patterns with type safety.
- Use `Class<T>` parameters for type tokens when the generic type must be known at runtime (e.g., for casting).
- Bounded type parameters (`<T extends Number>`) constrain what types can be used with the method.
- The `@SafeVarargs` annotation is needed on `final` enum methods that use varargs with generics.
- Enum generic methods can return `Optional<T>` for safe error handling.

## Relevant Java 21 Features

- **Pattern matching for switch**: Generic enum methods combine well with switch expressions to dispatch different behaviors based on runtime types.
- **Sealed interfaces with records**: Enums with generic factory methods can produce instances of sealed record hierarchies.
- **Type inference improvements**: Java 21's improved type inference makes calling generic enum methods more concise.
- **Virtual threads**: Generic executor-style enums can dispatch work onto virtual threads.

## Common Pitfalls and How to Avoid Them

1. **Type erasure hiding bugs at runtime**:
   ```java
   // WRONG: unchecked cast at runtime due to erasure
   public enum Converter {
       INSTANCE;
       @SuppressWarnings("unchecked")
       public <T> T convert(Object obj) {
           return (T) obj; // Compiles but fails at runtime if types don't match
       }
   }
   // Converter.INSTANCE.<Integer>convert("hello"); // ClassCastException later!

   // RIGHT: use Class<T> type token for runtime safety
   public <T> T convert(Object obj, Class<T> type) {
       return type.cast(obj); // Throws ClassCastException immediately
   }
   ```

2. **Ignoring generic type constraints in abstract methods**:
   ```java
   // WRONG: abstract method without proper bounds
   public abstract <T> T aggregate(List<T> values);
   // Callers can pass List<String> to a numeric aggregation!

   // RIGHT: bound the type parameter
   public abstract <T extends Number> double aggregate(List<T> values);
   ```

3. **Heap pollution with varargs**:
   ```java
   // WARNING: potential heap pollution
   public <T> Collection<T> of(T... elements) { /* ... */ }
   // Needs @SafeVarargs (only valid on final or static methods)

   // In enums, methods are effectively final, so use:
   @SafeVarargs
   public final <T> Collection<T> of(T... elements) { /* ... */ }
   ```

4. **Returning raw types from generic methods**:
   ```java
   // WRONG: raw type return
   public <T> List execute(List<T> input) { return new ArrayList(); }

   // RIGHT: properly typed return
   public <T> List<T> execute(List<T> input) { return new ArrayList<>(input); }
   ```

5. **Losing type information in enum method chains**:
   ```java
   // Problem: type inference may not propagate through method chains
   List<String> result = CollectionOp.FILTER
       .execute(List.of("a", "bb", "ccc"), s -> s.length() > 1);
   // This works because Java infers T=String from the input list
   ```

## Best Practices and Optimization Techniques

- **Always use `Class<T>` type tokens** when the generic type must be known at runtime for casting or reflection.
- **Prefer bounded type parameters** (`<T extends Number>`, `<T extends Comparable<T>>`) to catch type errors at compile time.
- **Use `Optional<T>` for safe conversion methods** — `safeConvert()` returning `Optional<T>` is better than throwing exceptions.
- **Document the generic method contract clearly** — what types are accepted, what happens on type mismatch.
- **Mark varargs methods as `@SafeVarargs`** if they don't store the varargs array or do unsafe operations on it.
- **Consider Comparator<T> integration** — generic comparison methods should return `Comparator<T>` for composability.
- **Test with multiple types** — ensure generic methods work with Integer, String, custom objects, and edge cases.

## Edge Cases and Their Handling

- **Null inputs to generic methods**: Always decide and document null handling — use `Objects.requireNonNull()` or return `Optional.empty()`.
- **Empty collections**: Aggregation methods like `min()` and `max()` should throw `NoSuchElementException` on empty input, while `sum()` and `average()` should return 0.
- **Incompatible types at runtime**: Use `Class.cast()` for safe casting — it throws `ClassCastException` with a clear message.
- **Generic methods with wildcards**: `List<?>` as a parameter loses type information — prefer `<T> void process(List<T> items)`.
- **Recursive type bounds**: `<T extends Comparable<T>>` ensures natural ordering is available.

## Interview-specific Insights

- **"Why can't enums be generic but their methods can?"** — Enums are implicitly `final` classes that extend `Enum<E>` which is already generic. Adding another type parameter would create `Enum<E, T>` which breaks the core enum machinery. But methods can have independent type parameters because they're resolved at each call site.
- **"How do generic enum methods compare to generic interface implementations?"** — Enums provide singleton constants + generic methods; interfaces provide polymorphic implementations. Enums are better when the set of operations is fixed.
- **"What's the difference between `<T>` and `<T extends Comparable<T>>` on an enum method?"** — Unbounded allows any type but limits what you can do (no ordering, no arithmetic). Bounded constrains the type but enables more operations.

## Interview Q&A Section

### Q1: Why can't Java enums have type parameters, and how do generic methods work around this?

```text
Java enums cannot be generic because they implicitly extend java.lang.Enum<E>,
where E is the enum type itself. Adding another type parameter would require
Enum<E, T>, which would break:
1. The values() method — it returns E[], but with generics, what's T?
2. The valueOf() method — needs to know the exact type at compile time
3. Serialization — enum constants are serialized by name, not by parameterized type
4. Singleton guarantee — MyEnum<String>.VALUE and MyEnum<Integer>.VALUE would be
   the same constant but with different types, which is contradictory

Generic methods work around this by declaring type parameters at the method level.
The type is resolved at each call site, so the enum constant remains a singleton
while its methods can operate on different types.

This gives us the best of both worlds: fixed constants with type-safe operations.
```

```java
// Enum cannot be generic
// enum Converter<T> { } // COMPILE ERROR

// But methods can be generic
public enum DataConverter {
    STRING_TO_NUMBER {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            String str = String.valueOf(input);
            if (targetType == Integer.class) return targetType.cast(Integer.parseInt(str));
            if (targetType == Double.class) return targetType.cast(Double.parseDouble(str));
            throw new UnsupportedOperationException("Unsupported: " + targetType);
        }
    },
    IDENTITY {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            return targetType.cast(input);
        }
    };

    public abstract <T> T convert(Object input, Class<T> targetType);
}

// Usage — type resolved at call site
Integer num = DataConverter.STRING_TO_NUMBER.convert("42", Integer.class);
Double dbl = DataConverter.STRING_TO_NUMBER.convert("3.14", Double.class);
String str = DataConverter.IDENTITY.convert("hello", String.class);
```

### Q2: How do you implement a type-safe collection factory using enum generic methods?

```text
A collection factory enum uses generic methods to create typed collections without
requiring the caller to cast. Each constant produces a different collection type
(ArrayList, LinkedList, HashSet, TreeSet), but the generic method signature ensures
the returned collection has the correct element type.

Key design decisions:
- create() returns an empty collection of the specified type
- createFrom(Collection<T> source) copies from a source collection
- of(T... elements) creates a pre-populated collection (needs @SafeVarargs)
- The return type is Collection<T>, not the specific implementation, for flexibility
```

```java
public enum CollectionFactory {
    ARRAY_LIST {
        @Override public <T> Collection<T> create() { return new ArrayList<>(); }
        @Override public <T> Collection<T> createFrom(Collection<T> src) {
            return new ArrayList<>(src);
        }
    },
    HASH_SET {
        @Override public <T> Collection<T> create() { return new HashSet<>(); }
        @Override public <T> Collection<T> createFrom(Collection<T> src) {
            return new HashSet<>(src);
        }
    },
    TREE_SET {
        @Override public <T> Collection<T> create() { return new TreeSet<>(); }
        @Override public <T> Collection<T> createFrom(Collection<T> src) {
            TreeSet<T> set = new TreeSet<>();
            set.addAll(src);
            return set;
        }
    };

    public abstract <T> Collection<T> create();
    public abstract <T> Collection<T> createFrom(Collection<T> source);

    @SafeVarargs
    public final <T> Collection<T> of(T... elements) {
        Collection<T> c = create();
        Collections.addAll(c, elements);
        return c;
    }
}

// Type-safe usage
Collection<String> strings = CollectionFactory.ARRAY_LIST.of("a", "b", "c");
Collection<Integer> numbers = CollectionFactory.HASH_SET.of(1, 2, 3, 2, 1);
// numbers.size() == 3 (duplicates removed by HashSet)
```

### Q3: How do bounded type parameters work with enum generic methods?

```text
Bounded type parameters (<T extends SomeType>) constrain which types can be passed
to a generic enum method. This is crucial for operations that require specific
capabilities (Comparable for sorting, Number for arithmetic).

Common bounds:
- <T extends Number> — numeric operations (sum, average, min, max)
- <T extends Comparable<T>> — ordering operations (sort, min, max)
- <T extends Serializable> — serialization operations
- <T extends CharSequence> — string-like operations
- Multiple bounds: <T extends Number & Comparable<T>> — both numeric and orderable

Without bounds, you can only call Object methods on T. With bounds, you unlock
the methods of the bound type — this is checked at compile time.
```

```java
public enum Aggregator {
    SUM {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue).sum();
        }
    },
    AVERAGE {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.isEmpty() ? 0.0 :
                values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        }
    },
    MAX {
        @Override
        public <T extends Number> double aggregate(List<T> values) {
            return values.stream().mapToDouble(Number::doubleValue)
                .max().orElseThrow(NoSuchElementException::new);
        }
    };

    // Bounded type parameter — only Number subclasses allowed
    public abstract <T extends Number> double aggregate(List<T> values);
}

// Works with any Number subclass
double intSum = Aggregator.SUM.aggregate(List.of(1, 2, 3));         // 6.0
double dblAvg = Aggregator.AVERAGE.aggregate(List.of(1.5, 2.5));   // 2.0
double longMax = Aggregator.MAX.aggregate(List.of(100L, 200L));     // 200.0

// Compile error — String is not a Number:
// Aggregator.SUM.aggregate(List.of("a", "b")); // ERROR
```

### Q4: How do generic comparison strategies work in enums?

```text
A comparison strategy enum uses generic methods to compare any two objects of
the same type. Each constant implements a different comparison algorithm:
natural ordering, reverse ordering, hash code comparison, or string-based comparison.

The key insight is that compare(T a, T b) returns an int (like Comparator),
and toComparator() converts the strategy into a standard Comparator<T>.

This pattern is powerful because:
- The set of strategies is fixed and known at compile time
- Each strategy is a singleton (no allocation)
- Strategies compose with Java's Comparator API (thenComparing, reversed)
- Type safety is preserved throughout
```

```java
public enum CompareStrategy {
    NATURAL {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            if (!(a instanceof Comparable<?>))
                throw new UnsupportedOperationException("Not comparable");
            return ((Comparable<T>) a).compareTo(b);
        }
    },
    REVERSE {
        @Override
        @SuppressWarnings("unchecked")
        public <T> int compare(T a, T b) {
            return ((Comparable<T>) b).compareTo(a);
        }
    },
    BY_STRING {
        @Override
        public <T> int compare(T a, T b) {
            return String.valueOf(a).compareTo(String.valueOf(b));
        }
    };

    public abstract <T> int compare(T a, T b);

    // Convert to standard Comparator
    public <T> Comparator<T> toComparator() {
        return this::compare;
    }

    // Find min/max using this strategy
    public <T> T min(List<T> items) {
        return items.stream().min(this::compare).orElseThrow();
    }
}

// Usage
List<String> names = List.of("Charlie", "Alice", "Bob");
List<String> sorted = names.stream()
    .sorted(CompareStrategy.NATURAL.toComparator())
    .toList();
// [Alice, Bob, Charlie]

String first = CompareStrategy.NATURAL.min(names);   // "Alice"
String last = CompareStrategy.REVERSE.min(names);     // "Charlie"
```

### Q5: What are the limitations and workarounds for generic methods in enums?

```text
Limitations:
1. Cannot declare type parameters on the enum itself — only on methods
2. Type information is erased at runtime — need Class<T> tokens for runtime safety
3. Cannot create generic arrays directly — use collections instead
4. Wildcard capture can be tricky with abstract enum methods
5. @SafeVarargs only works on final or static methods (enum methods are implicitly
   final in some contexts, but constant-specific methods are not)
6. No generic constant fields — fields must use raw or specific types

Workarounds:
1. Class<T> type tokens for runtime type safety
2. Optional<T> for safe conversion results
3. Generic interfaces — enum can implement a generic interface
4. Builder pattern alongside enum — enum selects the strategy, builder handles types
5. Use sealed interfaces + records if you need both type-safe constants AND per-instance
   generic types (the "Typeclass" pattern)
```

```java
// Limitation: no generic enum fields
// enum Box<T> { INSTANCE; T value; } // ILLEGAL

// Workaround 1: Type token parameter
public enum TypeSafe {
    INSTANCE;
    public <T> T safeCast(Object obj, Class<T> type) {
        return type.isInstance(obj) ? type.cast(obj) : null;
    }
}

// Workaround 2: Generic interface implementation
interface Transformer<T, R> { R transform(T input); }

// Each constant can implement for specific types
public enum StringTransformer {
    TO_INT {
        public Integer apply(String s) { return Integer.parseInt(s); }
    },
    TO_UPPER {
        public String apply(String s) { return s.toUpperCase(); }
    };
    // Note: can't make the return type generic at enum level

    // But CAN have a generic utility method
    public <T> Optional<T> safeApply(String s, Class<T> type) {
        try {
            Object result = switch (this) {
                case TO_INT -> Integer.parseInt(s);
                case TO_UPPER -> s.toUpperCase();
            };
            return Optional.of(type.cast(result));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

// Workaround 3: Sealed interface + records for full generics
sealed interface TypedConstant<T> {
    T defaultValue();
    record StringConst(String defaultValue) implements TypedConstant<String> {}
    record IntConst(Integer defaultValue) implements TypedConstant<Integer> {}
}
```

## Code Examples

- Implementation: [GenericEnumMethods.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumMethods.java)
- Tests: [GenericEnumMethodsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumMethodsTest.java)
