# 4.2.2. Supplier — Supplying Values On-Demand

## Concept Explanation

`Supplier<T>` is the functional interface that takes **no arguments** and **returns a value** of type `T`. Where a
`Consumer` is about putting something in, a `Supplier` is about pulling something out — on demand, and only when
asked.

The defining power of `Supplier` is **lazy evaluation**: the computation wrapped inside a Supplier is deferred until
the `get()` method is called. This is fundamentally different from passing the value directly, where the value is
computed eagerly at the call site regardless of whether it is ever needed.

**Real-world analogy**: Think of a Supplier like a vending machine. The machine doesn't produce your snack until you
press the button (`get()`). If you never press the button, no snack is produced and no resources are consumed. This
is in contrast to someone handing you a snack the moment you walk up to the machine (eager evaluation) — you receive
it whether you want it or not.

The interface definition:

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

`Supplier` has no `andThen()` or chaining method because it produces output with no input — composition is done by
wrapping one Supplier inside another.

**Primitive specializations** avoid boxing overhead:
- `BooleanSupplier` — `boolean getAsBoolean()`
- `IntSupplier` — `int getAsInt()`
- `LongSupplier` — `long getAsLong()`
- `DoubleSupplier` — `double getAsDouble()`

**Key use sites in the JDK:**
- `Optional.orElseGet(Supplier<T>)` — lazy default value
- `Optional.orElseThrow(Supplier<X extends Throwable>)` — lazy exception construction
- `Stream.generate(Supplier<T>)` — infinite stream from a Supplier
- `Objects.requireNonNullElseGet(T, Supplier<T>)` — null-safe lazy default (Java 9+)
- `Logger.info(Supplier<String>)` — lazy log message construction (avoid string formatting overhead)

## Key Points to Remember

1. `Supplier<T>` has one abstract method: `T get()` — it takes no arguments and returns a value.
2. Suppliers enable **lazy evaluation**: the computation does not run until `get()` is called.
3. `Optional.orElseGet(Supplier)` is lazy — the Supplier is only invoked when the Optional is empty. `orElse(T)` is eager — the default is always computed.
4. `Stream.generate(Supplier)` produces an infinite stream; always pair it with `limit()` or another short-circuiting operation.
5. A Supplier can represent a **factory**: `ArrayList::new` is a `Supplier<List<String>>` that creates a fresh list each call.
6. A Supplier can represent a **memoized/cached computation**: compute once, cache the result, return the cached value on subsequent calls.
7. Primitive specializations (`IntSupplier`, `LongSupplier`, `DoubleSupplier`, `BooleanSupplier`) avoid autoboxing.
8. Unlike `Callable<T>`, `Supplier<T>` cannot throw checked exceptions. Wrap checked exceptions in `RuntimeException` if needed.

## Relevant Java 21 Features

- **Java 8**: `Supplier<T>` and all primitive specializations introduced in `java.util.function`.
- **Java 8**: `Optional.orElseGet(Supplier)` and `Stream.generate(Supplier)` are the primary JDK use sites.
- **Java 9**: `Objects.requireNonNullElseGet(T obj, Supplier<T> supplier)` — returns `obj` if non-null, otherwise calls the Supplier.
- **Java 9**: `Optional.or(Supplier<Optional<T>>)` — returns the optional if present, otherwise calls the Supplier to produce a new Optional.
- **Java 9**: `Optional.ifPresentOrElse(Consumer, Runnable)` — for two-branch handling without a Supplier, contrast with `orElseGet`.
- **Java 11**: `Predicate.not(Predicate)` does not involve Supplier, but `Optional.orElseThrow(Supplier)` is commonly paired with it.
- **Java 16+**: Records used as DTOs can be supplied via constructor references: `() -> new PersonRecord("Alice", 30)`.
- **Java 21**: Virtual threads benefit from Supplier-based lazy initialization — the Supplier wraps expensive resource creation (DB connections, HTTP clients) and is only invoked when the virtual thread actually needs it.

## Common Pitfalls and How to Avoid Them

1. **`orElse` vs `orElseGet` — the classic mistake** — `orElse(T value)` evaluates its argument **always**, even when the Optional is present, because Java evaluates method arguments eagerly before the call.

   ```java
   // Broken: computeExpensiveDefault() runs even when optional is present
   Optional<String> opt = Optional.of("existing");
   String result = opt.orElse(computeExpensiveDefault()); // method called!
   ```

   ```java
   // Fix: use orElseGet — the Supplier is only called when opt is empty
   String result = opt.orElseGet(() -> computeExpensiveDefault());
   // computeExpensiveDefault() is NOT called because opt is present
   ```

2. **Forgetting `limit()` on `Stream.generate()`** — `Stream.generate(Supplier)` produces an infinite stream. Without a terminal short-circuit operation, it runs forever.

   ```java
   // Broken: runs forever, eventually OOM
   List<Integer> randoms = Stream.generate(() -> (int)(Math.random() * 100))
       .collect(Collectors.toList()); // infinite!
   ```

   ```java
   // Fix: always limit infinite streams
   List<Integer> randoms = Stream.generate(() -> (int)(Math.random() * 100))
       .limit(10)
       .collect(Collectors.toList());
   ```

3. **Memoization without thread safety** — A naive memoizing Supplier using a local flag is not thread-safe. Multiple threads may each see the "not yet computed" state and compute the value concurrently.

   ```java
   // Broken: not thread-safe under concurrent access
   Object[] cached = {null};
   boolean[] computed = {false};
   Supplier<String> memo = () -> {
       if (!computed[0]) { // race condition here
           cached[0] = expensiveComputation();
           computed[0] = true;
       }
       return (String) cached[0];
   };
   ```

   ```java
   // Fix: use AtomicReference for thread-safe lazy initialization
   AtomicReference<String> ref = new AtomicReference<>();
   Supplier<String> threadSafeMemo = () ->
       ref.updateAndGet(v -> v != null ? v : expensiveComputation());
   ```

4. **Using `Supplier` where `Callable` is needed** — `Supplier.get()` cannot throw checked exceptions. If the computation throws a checked exception, use `Callable<T>` instead.

   ```java
   // Broken: IOException is checked, Supplier cannot throw it
   Supplier<String> reader = () -> Files.readString(Path.of("file.txt")); // compile error
   ```

   ```java
   // Fix 1: use Callable (for ExecutorService, etc.)
   Callable<String> reader = () -> Files.readString(Path.of("file.txt"));

   // Fix 2: wrap in unchecked exception
   Supplier<String> reader = () -> {
       try { return Files.readString(Path.of("file.txt")); }
       catch (IOException e) { throw new UncheckedIOException(e); }
   };
   ```

5. **Capturing mutable state that changes between `get()` calls** — If a Supplier captures a mutable variable that is modified externally, it may return different values across calls unexpectedly.

   ```java
   // Tricky: 'prefix' is effectively final, but what if it pointed to a mutable object?
   StringBuilder prefix = new StringBuilder("Hello");
   Supplier<String> greeter = () -> prefix + " World";
   prefix.append("!!!"); // greeter will now return "Hello!!! World"
   ```

## Best Practices and Optimization Techniques

1. **Prefer `orElseGet(Supplier)` over `orElse(value)` when the default involves any computation** — even simple string formatting or object construction.

2. **Use constructor references as Suppliers for factory patterns.** `ArrayList::new` is cleaner and more expressive than `() -> new ArrayList<>()`.

   ```java
   Supplier<List<String>> listFactory = ArrayList::new;
   Supplier<Map<String, Integer>> mapFactory = HashMap::new;
   ```

3. **Implement the initialization-on-demand pattern with Supplier.** Wrap expensive one-time initialization in a Supplier that is stored as a field and called once.

   ```java
   private Supplier<HeavyResource> resourceSupplier = () -> new HeavyResource(); // not yet created
   // Later, when actually needed:
   HeavyResource resource = resourceSupplier.get();
   ```

4. **Use Supplier for dependency injection in tests.** Instead of creating the real object, inject a Supplier that returns a stub or mock — easy to swap without frameworks.

5. **Use primitive specializations for numeric suppliers in tight loops.** `IntSupplier.getAsInt()` avoids boxing each return value.

6. **Use `Optional.or(Supplier<Optional<T>>)` (Java 9+) for chaining Optional fallbacks** without nesting.

   ```java
   Optional<String> result = primary.or(() -> secondary).or(() -> tertiary);
   ```

## Edge Cases and Their Handling

1. **Supplier returning `null`** — `Supplier<T>` can return `null`. If the consumer of the Supplier does not expect null, a `NullPointerException` may follow. Use `Optional.ofNullable(supplier.get())` to handle this safely.

   ```java
   Supplier<String> mayBeNull = () -> null;
   String value = Optional.ofNullable(mayBeNull.get()).orElse("default");
   ```

2. **Supplier that throws `RuntimeException`** — The exception propagates out of `get()` to the caller. This is the only way for a `Supplier` to signal failure.

3. **Supplier used with `Stream.generate` producing the same object reference each time** — If the Supplier returns the same mutable object (not a copy), the stream will contain references to the same mutated object.

   ```java
   // Dangerous: all elements point to the same list
   List<String> shared = new ArrayList<>();
   List<List<String>> bad = Stream.generate(() -> shared).limit(3).collect(toList());
   // bad.get(0) == bad.get(1) == bad.get(2) (same reference)

   // Safe: create a new instance per call
   List<List<String>> good = Stream.generate(ArrayList::new).limit(3).collect(toList());
   ```

4. **BooleanSupplier** — Returns `boolean`, not `Boolean`. No boxing. Use `getAsBoolean()` instead of `get()`.

5. **Calling `get()` multiple times** — Unless you explicitly memoize, each call to `get()` re-executes the lambda. For expensive one-shot computations, store the result after the first call.

## Interview-specific Insights

Interviewers focus on:

- The `orElse` vs `orElseGet` distinction — this is one of the most commonly asked Java interview questions.
- Understanding that `Stream.generate` is infinite and requires `limit()`.
- Distinguishing Supplier (no-arg, returns value) from all other functional interfaces.
- Lazy evaluation concept and where it matters for performance.
- Memoization pattern using Supplier.
- `Callable<T>` vs `Supplier<T>` — both take no args and return a value, but Callable is for concurrent tasks and allows checked exceptions.

**Common tricky questions:**
- "What is the difference between `Supplier<T>` and `Callable<T>`?" (Both return a value with no args; Callable is in `java.util.concurrent`, supports checked exceptions, used with ExecutorService. Supplier is in `java.util.function`, no checked exceptions, used for lazy evaluation.)
- "In `optional.orElse(computeDefault())`, when is `computeDefault()` executed?" (Always — before `orElse` is even called, because Java uses strict evaluation.)
- "Can a Supplier produce different values on each call?" (Yes — a Supplier wrapping `Math.random()` or `new ArrayList<>()` returns a new value each time.)

## Interview Q&A Section

**Q1: What is the key difference between `Optional.orElse(T)` and `Optional.orElseGet(Supplier<T>)`?**

```text
A1: orElse(T value) is eager: the argument expression is evaluated before orElse is called,
regardless of whether the Optional is present or empty. This is standard Java argument
evaluation — the value is computed and then passed.

orElseGet(Supplier<T> supplier) is lazy: the Supplier's get() is only called when the
Optional is empty. If the Optional is present, get() is never invoked.

This matters when the "default" is expensive to compute (database query, HTTP call, complex
object construction). Using orElse with an expensive call wastes resources when the Optional
is already present.

Rule of thumb:
- orElse(constant) — fine for constants, literals, already-computed values
- orElseGet(() -> compute()) — preferred for any non-trivial default
```

```java
Optional<String> opt = Optional.of("found");

// orElse: computeDefault() ALWAYS runs
String a = opt.orElse(computeDefault());   // computeDefault() executes, result discarded

// orElseGet: computeDefault() only runs if opt is empty
String b = opt.orElseGet(() -> computeDefault()); // computeDefault() NOT called

// Demonstration with side effect to prove the point
int[] callCount = {0};
Supplier<String> countingSupplier = () -> {
    callCount[0]++;
    return "default";
};

Optional.of("present").orElseGet(countingSupplier);
System.out.println(callCount[0]); // 0 — Supplier was NOT called

Optional.<String>empty().orElseGet(countingSupplier);
System.out.println(callCount[0]); // 1 — Supplier WAS called
```

---

**Q2: How is `Supplier<T>` used with `Stream.generate()` and what must you remember?**

```text
A2: Stream.generate(Supplier<T>) creates an infinite, unordered, sequential Stream<T>.
Each call to the stream pipeline requests the next element by calling Supplier.get().

Because the stream is infinite, you MUST use a short-circuiting or limiting operation:
- limit(n)         — take exactly n elements
- findFirst()      — take one element
- anyMatch(pred)   — stop as soon as a match is found
- takeWhile(pred)  — take while condition holds (Java 9+)

If no limiting operation is used and collect() or forEach() is called, the stream will
attempt to process infinite elements — resulting in an infinite loop or OutOfMemoryError.
```

```java
// Generate 10 random numbers between 0 and 99
List<Integer> randoms = Stream.generate(() -> (int)(Math.random() * 100))
    .limit(10)
    .collect(Collectors.toList());

// Generate UUIDs
List<String> uuids = Stream.generate(() -> UUID.randomUUID().toString())
    .limit(5)
    .collect(Collectors.toList());

// Generate incrementing sequence using a stateful Supplier
int[] counter = {0};
Supplier<Integer> incrementing = () -> counter[0]++;
List<Integer> sequence = Stream.generate(incrementing).limit(5).collect(Collectors.toList());
// [0, 1, 2, 3, 4]

// Java 9+: takeWhile for condition-based stopping
Stream.generate(() -> (int)(Math.random() * 10))
    .takeWhile(n -> n != 0)     // stop at first 0
    .forEach(System.out::println);
```

---

**Q3: Explain the memoization (caching) pattern using `Supplier<T>`.**

```text
A3: Memoization is an optimization where the result of an expensive computation is cached
after the first call, and all subsequent calls return the cached result without recomputing.

A Supplier is a natural fit: it represents "produce this value when asked". By wrapping it
in a memoizing layer, we change its semantics to "produce this value once, then cache it".

There are two common implementations:
1. Single-threaded: use a boolean flag and a result holder.
2. Thread-safe: use AtomicReference with compareAndSet or updateAndGet.
```

```java
// Thread-safe memoizing Supplier
public static <T> Supplier<T> memoize(Supplier<T> supplier) {
    AtomicReference<T> cached = new AtomicReference<>();
    return () -> {
        T value = cached.get();
        if (value == null) {
            // updateAndGet is atomic: only one thread wins the computation
            cached.compareAndSet(null, supplier.get());
            value = cached.get();
        }
        return value;
    };
}

// Usage
Supplier<String> expensive = memoize(() -> {
    System.out.println("Computing...");
    return "result-" + System.currentTimeMillis();
});

System.out.println(expensive.get()); // "Computing..." then "result-..."
System.out.println(expensive.get()); // returns cached value — no "Computing..."
System.out.println(expensive.get()); // still cached

// Instance-level memoization pattern (simple, single-threaded)
public class Config {
    private String cachedValue;

    private final Supplier<String> valueSupplier = () -> {
        if (cachedValue == null) cachedValue = loadFromDisk();
        return cachedValue;
    };

    public String getValue() { return valueSupplier.get(); }
    private String loadFromDisk() { return "value-from-disk"; }
}
```

---

**Q4: How does `Supplier<T>` differ from `Callable<T>`?**

```text
A4: Both Supplier<T> and Callable<T> are zero-argument functional interfaces that return a
value of type T. The key differences are:

1. Package: Supplier is in java.util.function (functional programming). Callable is in
   java.util.concurrent (concurrency).

2. Checked exceptions: Supplier.get() cannot throw checked exceptions. Callable.call()
   declares "throws Exception" — any checked exception is allowed.

3. Use sites: Supplier is used with Optional, Stream.generate, lazy initialization.
   Callable is used with ExecutorService.submit(), FutureTask, and other concurrency APIs.

4. Nullability: Both can return null, but Supplier is often expected to return non-null
   in Optional.orElseGet.

Use Supplier for lazy evaluation and functional composition.
Use Callable when submitting tasks to an executor or when checked exceptions must be propagated.
```

```java
// Supplier: no checked exceptions
Supplier<String> supplier = () -> "hello";
String s = supplier.get();

// Callable: allows checked exceptions, used with ExecutorService
Callable<String> callable = () -> Files.readString(Path.of("file.txt")); // checked IOException ok
ExecutorService exec = Executors.newSingleThreadExecutor();
Future<String> future = exec.submit(callable);
String content = future.get(); // may throw ExecutionException wrapping IOException

// Converting Supplier to Callable (trivial)
Callable<String> asCallable = supplier::get;

// Converting Callable to Supplier (requires exception wrapping)
Supplier<String> asSupplier = () -> {
    try { return callable.call(); }
    catch (Exception e) { throw new RuntimeException(e); }
};
```

---

**Q5: Demonstrate the factory pattern and dependency injection using `Supplier<T>`.**

```text
A5: A Supplier is an excellent fit for the factory pattern: you pass a Supplier<T> instead of
a concrete T, giving the receiver control over when and how many instances are created. This
decouples creation from use, makes testing easier (inject a Supplier returning a mock), and
enables lazy creation.

In dependency injection contexts, a Supplier replaces the container's role for lightweight
scenarios — the caller provides "how to create the dependency" and the callee calls get()
only when it needs it.
```

```java
// Factory pattern: method accepts a Supplier to create instances on demand
public <T> List<T> createN(int n, Supplier<T> factory) {
    List<T> result = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        result.add(factory.get());  // new instance created each time
    }
    return result;
}

// Usage with different factories
List<ArrayList<String>> lists = createN(3, ArrayList::new);
List<StringBuilder> builders = createN(5, StringBuilder::new);
List<String> uuids = createN(10, () -> UUID.randomUUID().toString());

// Dependency injection via Supplier (no framework needed)
public class UserService {
    private final Supplier<DatabaseConnection> connectionSupplier;

    public UserService(Supplier<DatabaseConnection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    public User findUser(String id) {
        DatabaseConnection conn = connectionSupplier.get(); // lazy, only when needed
        return conn.query("SELECT * FROM users WHERE id = ?", id);
    }
}

// In tests, inject a Supplier returning a mock
DatabaseConnection mockConn = mock(DatabaseConnection.class);
UserService service = new UserService(() -> mockConn);

// In production, inject a Supplier returning a real pooled connection
UserService prodService = new UserService(() -> connectionPool.borrow());
```

---

**Q6: How do primitive Supplier specializations work and when should you use them?**

```text
A6: Generic Supplier<T> requires T to be a reference type. When T is Integer, Long, or Double,
each call to get() returns a boxed object, requiring heap allocation. Primitive specializations
avoid this:

- IntSupplier.getAsInt()       — returns int (no boxing)
- LongSupplier.getAsLong()     — returns long (no boxing)
- DoubleSupplier.getAsDouble() — returns double (no boxing)
- BooleanSupplier.getAsBoolean() — returns boolean (no boxing)

Use primitive specializations when:
- Supplying values in tight loops or high-frequency code paths
- Working with IntStream, LongStream, DoubleStream
- The allocation overhead of boxing is measurable in your profiling
```

```java
// Generic: each call boxes the int to Integer
Supplier<Integer> genericRandom = () -> (int)(Math.random() * 100);
Integer boxed = genericRandom.get(); // Integer object allocated on heap

// Primitive: no boxing
IntSupplier primitiveRandom = () -> (int)(Math.random() * 100);
int primitive = primitiveRandom.getAsInt(); // no allocation

// With IntStream — requires IntSupplier
IntStream.generate(primitiveRandom).limit(10).sum();

// BooleanSupplier — common for feature flags and lazy checks
BooleanSupplier featureEnabled = () -> System.getProperty("feature.x") != null;
if (featureEnabled.getAsBoolean()) {
    enableFeature();
}

// LongSupplier — for timestamps and large counters
LongSupplier timestampMs = System::currentTimeMillis;
long start = timestampMs.getAsLong();
```

---

**Q7: Show how `Optional.or(Supplier<Optional<T>>)` (Java 9+) enables clean fallback chains.**

```text
A7: Optional.or(Supplier<Optional<T>>) introduced in Java 9 allows you to chain multiple
Optional sources without nesting. If the first Optional is empty, the Supplier is called
to produce another Optional, which may itself be empty (triggering further fallbacks).

This is cleaner than nested orElseGet calls and reads like a natural "try this, then try that"
chain. Unlike orElseGet which produces a plain value, or() produces an Optional, allowing
the final stage to still apply orElse/orElseThrow.
```

```java
// Without or() — nested and hard to read
Optional<String> result = primaryCache.get(key)
    .map(Optional::of)
    .orElseGet(() -> secondaryCache.get(key)
        .map(Optional::of)
        .orElseGet(() -> database.find(key)));

// With or() — clean and readable chain (Java 9+)
Optional<String> result = primaryCache.get(key)
    .or(() -> secondaryCache.get(key))
    .or(() -> database.find(key))
    .or(() -> Optional.of("default"));

// Each Supplier is only called if the previous Optional was empty
// If primaryCache returns a value, the remaining Suppliers are never invoked

// Practical example: multi-tier configuration lookup
String config = Optional.ofNullable(System.getProperty("app.timeout"))
    .or(() -> Optional.ofNullable(System.getenv("APP_TIMEOUT")))
    .or(() -> readFromConfigFile("app.timeout"))
    .orElse("30"); // final default if all sources are empty
```

## Code Examples

- Source: [SupplierDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/SupplierDemo.java)
- Test: [SupplierDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/SupplierDemoTest.java)
