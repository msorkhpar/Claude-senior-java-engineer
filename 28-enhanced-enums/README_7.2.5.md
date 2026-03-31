# 7.2.5. Use Cases and Examples

## Concept Explanation

Enhanced enums unlock powerful design patterns that go far beyond simple named constants. This section explores real-world use cases where enums serve as state machines, strategy selectors, validation frameworks, singleton services, permission systems, and result containers. These patterns are common in production Java codebases and frequently appear in senior-level interviews.

**Real-world analogy**: Think of enums as the Swiss Army knife of Java design. A regular knife (plain enum) just has a blade (a name). But a Swiss Army knife (enhanced enum) has a blade, screwdriver, bottle opener, scissors, and tweezers — each tool (pattern) serves a different purpose, and they're all compactly bundled in one reliable, pocket-sized package. You always have the right tool, and you never lose any of them because they're all attached.

Key use cases covered:
1. **State Machine** — enum constants as states with valid transitions
2. **Strategy Pattern** — enum constants as interchangeable algorithms
3. **Validation Framework** — enum constants as composable validation rules
4. **Singleton Service Registry** — enum constants as thread-safe singletons with behavior
5. **Permission/Flag System** — EnumSet-backed efficient permission combinations
6. **Result Type** — enum status combined with generic record for monadic error handling

## Key Points to Remember

- **State machines**: Each state knows its valid transitions; `transitionTo()` enforces invariants at runtime.
- **Strategy pattern with enums**: Fixed set of strategies with singleton semantics — no allocation overhead.
- **Validation rules**: Composable with `validateAll()` that returns all violations, not just the first.
- **EnumSet** is backed by a bit vector — O(1) for `contains()`, `add()`, `remove()` on up to 64 constants.
- **EnumMap** is backed by an array indexed by ordinal — faster and more memory-efficient than HashMap.
- **Enum singletons** are inherently thread-safe, serialization-safe, and reflection-proof.
- **Result type pattern**: Combining enum status with generic records creates a type-safe alternative to exceptions for expected failures.
- **Enum + sealed interface**: Modern Java (17+) allows enums to work alongside sealed hierarchies for exhaustive pattern matching.

## Relevant Java 21 Features

- **Pattern matching for switch**: State machines and strategy enums are naturally dispatched with switch expressions.
- **Record patterns**: The `Result<T>` pattern (enum status + record) benefits from record pattern deconstruction in switches.
- **Sealed interfaces**: Enums implementing sealed interfaces gain exhaustive switch support.
- **Virtual threads**: Enum-based service registries can dispatch work onto virtual threads.
- **Sequenced collections**: EnumSet iteration order is guaranteed (declaration order), making it a natural `SequencedSet`.

## Common Pitfalls and How to Avoid Them

1. **State machine transitions that don't validate**:
   ```java
   // WRONG: allowing any transition
   public OrderState moveTo(OrderState next) { return next; }

   // RIGHT: validate transitions
   public OrderState transitionTo(OrderState target) {
       if (!canTransitionTo(target)) {
           throw new IllegalStateException(
               "Cannot transition from " + this + " to " + target);
       }
       return target;
   }
   ```

2. **Mutable state in singleton enum services**:
   ```java
   // WRONG: non-thread-safe mutable cache
   public enum Cache {
       LRU;
       private Map<String, Object> data = new HashMap<>(); // NOT thread-safe!
   }

   // RIGHT: use thread-safe data structures
   public enum Cache {
       LRU;
       private final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();
   }
   ```

3. **Negative amounts in discount strategies**:
   ```java
   // WRONG: not validating input
   public double applyDiscount(double amount) {
       return calculator.apply(amount);
   }

   // RIGHT: validate before applying
   public double applyDiscount(double amount) {
       if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
       return Math.max(0, Math.round(calculator.apply(amount) * 100.0) / 100.0);
   }
   ```

4. **Using EnumSet.of() for empty sets**:
   ```java
   // WRONG: EnumSet.of() requires at least one element
   // Set<Permission> empty = EnumSet.of(); // COMPILE ERROR

   // RIGHT: use EnumSet.noneOf()
   Set<Permission> empty = EnumSet.noneOf(Permission.class);
   ```

5. **Forgetting that EnumSet.allOf() is mutable**:
   ```java
   // WRONG: exposing mutable set
   public static final Set<Permission> ALL = EnumSet.allOf(Permission.class);
   // Callers can modify ALL!

   // RIGHT: wrap in unmodifiable set
   public static final Set<Permission> ALL =
       Collections.unmodifiableSet(EnumSet.allOf(Permission.class));
   ```

## Best Practices and Optimization Techniques

- **Use `EnumSet` for any `Set<MyEnum>`** — it's faster and uses less memory than `HashSet`.
- **Use `EnumMap` for any `Map<MyEnum, V>`** — array-backed, O(1) access.
- **Define permission constants as `public static final`** sets for reuse: `READ_ONLY`, `READ_WRITE`, `FULL_ACCESS`.
- **Make state transitions explicit** — a state machine enum should document valid transitions in each constant.
- **Use functional interfaces in constructors** for strategies — `Function<Double, Double>` is cleaner than abstract methods for simple transformations.
- **Return `Optional` from validation** — `Optional.empty()` means valid, `Optional.of(message)` means violation.
- **Prefer the Result pattern over exceptions** for expected business failures (validation errors, not-found, etc.).
- **Combine enums with records** for algebraic data types: `enum Status { SUCCESS, FAILURE }` + `record Result<T>(Status status, T value)`.

## Edge Cases and Their Handling

- **Terminal state transitions**: `transitionTo()` on a terminal state should always throw — terminal states have no valid transitions.
- **Empty validation rule set**: `validateAll(value)` with no rules should return an empty list (all valid).
- **Null input to validation**: The `NOT_NULL` rule should catch null; other rules should handle null gracefully.
- **Discount on zero amount**: Should return 0.0, not negative.
- **Cache eviction at max size**: LRU and FIFO caches should correctly evict the appropriate entry when full.
- **EnumSet with all constants removed**: Results in an empty set, not null — iteration works but finds nothing.
- **Result.map() on failure**: Should propagate the failure without invoking the mapper function.

## Interview-specific Insights

- **"Implement a state machine using enums."** — One of the most common senior-level design questions. Show the transition validation pattern and terminal state concept.
- **"When would you use enum singletons vs. static classes?"** — Enums are serialization-safe, reflection-proof, and can implement interfaces. Static classes can't.
- **"How do EnumSet and EnumMap work internally?"** — EnumSet uses a bit vector (RegularEnumSet for ≤64 constants, JumboEnumSet for more). EnumMap uses an array indexed by ordinal.
- **"Design a permission system."** — Show the EnumSet-based approach with combine/intersect operations.
- **"Compare the Result pattern with exceptions."** — Result is for expected failures (validation, business logic); exceptions are for unexpected failures (I/O errors, bugs).

## Interview Q&A Section

### Q1: How do you implement a state machine using enums?

```text
A state machine enum encodes:
1. States as enum constants
2. Valid transitions per state (returned by an abstract method or stored in a field)
3. Transition enforcement via a transitionTo() method that checks validity
4. Terminal states identified by having no valid transitions

Advantages over other state machine implementations:
- Compile-time safety: all states are known
- Self-documenting: each state declares its transitions
- Thread-safe: enum constants are immutable singletons
- No external state needed: the current state IS an enum constant
- Easy to test: just assert transition validity

The pattern scales well — add a new state by adding a constant and defining its
transitions. The compiler won't catch missing transitions (unlike sealed switches),
but code review and tests will.
```

```java
public enum OrderState {
    CREATED {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.of(PENDING_PAYMENT, CANCELLED);
        }
    },
    PENDING_PAYMENT {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.of(PAID, CANCELLED);
        }
    },
    PAID {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.of(PROCESSING, REFUNDED);
        }
    },
    SHIPPED {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.of(DELIVERED, RETURNED);
        }
    },
    DELIVERED {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.of(RETURNED);
        }
    },
    CANCELLED {
        @Override public Set<OrderState> validTransitions() {
            return EnumSet.noneOf(OrderState.class); // Terminal state
        }
    };
    // ... other states

    public abstract Set<OrderState> validTransitions();

    public boolean canTransitionTo(OrderState target) {
        return validTransitions().contains(target);
    }

    public OrderState transitionTo(OrderState target) {
        if (!canTransitionTo(target))
            throw new IllegalStateException(this + " → " + target + " is invalid");
        return target;
    }

    public boolean isTerminal() { return validTransitions().isEmpty(); }
}

// Usage
OrderState state = OrderState.CREATED;
state = state.transitionTo(OrderState.PENDING_PAYMENT); // OK
// state.transitionTo(OrderState.DELIVERED); // throws IllegalStateException
```

### Q2: Explain EnumSet and EnumMap internals and when to use them.

```text
EnumSet internals:
- RegularEnumSet (≤64 constants): uses a single long as a bit vector.
  Each bit corresponds to one constant (by ordinal). Operations like
  contains(), add(), remove() are single bitwise operations — O(1).
- JumboEnumSet (>64 constants): uses a long[] array. Same principle,
  more storage.
- Both are much faster and smaller than HashSet<MyEnum>.
- Iteration is in declaration order (by ordinal).

EnumMap internals:
- Uses a plain Object[] array indexed by ordinal.
- No hashing, no collision resolution — O(1) guaranteed.
- Less memory than HashMap (no Entry objects, no table resizing).
- Iteration is in declaration order.

When to use:
- ALWAYS use EnumSet instead of HashSet for Set<MyEnum>
- ALWAYS use EnumMap instead of HashMap for Map<MyEnum, V>
- The performance difference is significant: EnumSet operations are
  1-2 orders of magnitude faster than HashSet equivalents.
```

```java
// EnumSet — extremely efficient
Set<Permission> perms = EnumSet.of(Permission.READ, Permission.WRITE);
perms.contains(Permission.READ);     // Single bit check: O(1)
perms.add(Permission.EXECUTE);       // Single bit set: O(1)
perms.retainAll(EnumSet.of(READ));   // Bitwise AND: O(1)

// Common EnumSet patterns
Set<Permission> all = EnumSet.allOf(Permission.class);
Set<Permission> none = EnumSet.noneOf(Permission.class);
Set<Permission> complement = EnumSet.complementOf(EnumSet.of(Permission.ADMIN));

// EnumMap — fastest map for enum keys
EnumMap<Permission, String> descriptions = new EnumMap<>(Permission.class);
descriptions.put(Permission.READ, "Can read resources");
descriptions.put(Permission.WRITE, "Can modify resources");
String desc = descriptions.get(Permission.READ); // Array index lookup: O(1)

// Real-world: permission-based access control
Map<Permission, Set<String>> accessLog = new EnumMap<>(Permission.class);
for (Permission p : Permission.values()) {
    accessLog.put(p, new HashSet<>());
}
accessLog.get(Permission.READ).add("user:alice");
```

### Q3: How do you implement a composable validation framework with enums?

```text
A validation enum uses parameterized constants with Predicate fields.
Each constant represents one validation rule. Rules are composable:

1. Single validation: rule.validate(value) returns Optional<String> — empty means valid
2. Composed validation: validateAll(value, rules...) returns List<String> of all violations
3. Rules are reusable and combinable for different contexts

This pattern is common in form validation, API input validation, and domain
object invariant checking. It's simpler than a full validation framework
(like Bean Validation) for focused use cases.

Key design: return ALL violations, not just the first. This gives the user
complete feedback in one pass.
```

```java
public enum ValidationRule {
    NOT_NULL("Must not be null", Objects::nonNull),
    NOT_BLANK("Must not be blank",
        v -> v instanceof String s && !s.isBlank()),
    POSITIVE_NUMBER("Must be positive",
        v -> v instanceof Number n && n.doubleValue() > 0),
    VALID_EMAIL("Must be a valid email",
        v -> v instanceof String s && s.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"));

    private final String message;
    private final Predicate<Object> validator;

    ValidationRule(String message, Predicate<Object> validator) {
        this.message = message;
        this.validator = validator;
    }

    public Optional<String> validate(Object value) {
        return validator.test(value) ? Optional.empty() : Optional.of(message);
    }

    // Compose rules: returns ALL violations
    public static List<String> validateAll(Object value, ValidationRule... rules) {
        return Arrays.stream(rules)
            .map(r -> r.validate(value))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
}

// Usage
List<String> errors = ValidationRule.validateAll(null,
    ValidationRule.NOT_NULL, ValidationRule.NOT_BLANK);
// ["Must not be null", "Must not be blank"]

List<String> emailErrors = ValidationRule.validateAll("bad-email",
    ValidationRule.NOT_NULL, ValidationRule.NOT_BLANK, ValidationRule.VALID_EMAIL);
// ["Must be a valid email"]
```

### Q4: When should you use enum singletons vs. other singleton patterns?

```text
Enum singletons are the recommended singleton pattern in Java (per Effective Java, Item 3).

Advantages of enum singletons:
1. Thread-safe: JVM guarantees single instantiation during class loading
2. Serialization-safe: deserialization always returns the same instance
3. Reflection-proof: Enum.class constructor check prevents reflective instantiation
4. Simple: no double-checked locking, no static holder, no volatile fields
5. Lazy: initialized when the enum class is first accessed

When to use enum singletons:
- Service registries, caches, configuration holders
- When you need multiple singleton services (multiple constants)
- When serialization correctness matters

When NOT to use:
- When you need lazy initialization of heavy resources (use static holder instead)
- When the singleton needs to extend another class (enums extend Enum)
- When you need the singleton to be replaceable in tests (prefer dependency injection)
- When the number of instances might change in the future
```

```java
// Enum singleton — recommended
public enum AppConfig {
    INSTANCE;

    private final Properties props = new Properties();

    AppConfig() {
        // Load configuration on first access
        try (var is = getClass().getResourceAsStream("/app.properties")) {
            if (is != null) props.load(is);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}

// Multiple singleton services
public enum CacheType {
    LRU {
        private final Map<String, Object> cache =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, Object> e) {
                    return size() > 100;
                }
            });
        @Override public void put(String key, Object val) { cache.put(key, val); }
        @Override public Optional<Object> get(String key) { return Optional.ofNullable(cache.get(key)); }
    },
    FIFO { /* ... similar but FIFO eviction ... */ };

    public abstract void put(String key, Object value);
    public abstract Optional<Object> get(String key);
}
```

### Q5: How do you combine enums with records for a Result/Either type?

```text
The Result type pattern combines:
1. An enum for status (SUCCESS, FAILURE, PENDING) — the "what happened"
2. A generic record for the payload — the "what's inside"

This creates a type-safe alternative to exceptions for expected failures:
- map() transforms the value if successful, propagates failure otherwise
- flatMap() chains operations that might fail
- getValue() returns Optional — forces callers to handle absence

Advantages over exceptions:
- Explicit in the type signature — callers see that failure is possible
- No stack trace overhead — much faster than throwing exceptions
- Composable — map/flatMap chains vs. nested try-catch
- Forces handling — Optional return makes ignoring errors harder

Use for: validation results, API responses, business rule outcomes
Don't use for: truly exceptional conditions (I/O errors, OOM, bugs)
```

```java
public enum ResultStatus {
    SUCCESS, FAILURE, PENDING;
}

public record Result<T>(ResultStatus status, T value, String message) {
    public static <T> Result<T> success(T value) {
        return new Result<>(ResultStatus.SUCCESS, value, "OK");
    }
    public static <T> Result<T> failure(String msg) {
        return new Result<>(ResultStatus.FAILURE, null, msg);
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (status == ResultStatus.SUCCESS && value != null)
            return Result.success(mapper.apply(value));
        return new Result<>(status, null, message);
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (status == ResultStatus.SUCCESS && value != null)
            return mapper.apply(value);
        return new Result<>(status, null, message);
    }

    public Optional<T> getValue() { return Optional.ofNullable(value); }
}

// Usage — composable chain
Result<Integer> result = Result.success("42")
    .map(Integer::parseInt)           // Result<Integer> SUCCESS: 42
    .map(n -> n * 2);                 // Result<Integer> SUCCESS: 84

Result<Integer> failed = Result.<String>failure("not found")
    .map(Integer::parseInt);          // Result<Integer> FAILURE: "not found"
    // map was never called — failure propagated
```

## Code Examples

- Implementation: [EnumUseCases.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/EnumUseCases.java)
- Tests: [EnumUseCasesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/EnumUseCasesTest.java)
