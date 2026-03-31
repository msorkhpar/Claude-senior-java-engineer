# 8.4.3. Benefits of Composition over Inheritance for Concurrency

## Concept Explanation

While the previous section focused on how to apply composition in concurrent code, this section examines the specific benefits that composition delivers in concurrent systems. The two primary benefits are: (1) making concurrent behavior easier to reason about, and (2) improving modularity and testability.

**Real-world analogy**: Consider two approaches to building a home security system. The inheritance approach is like a monolithic alarm system where the motion detector, camera, door lock, and siren are all wired together in a single unit -- if one component fails or needs upgrading, you must understand the entire system and risk breaking other components. The composition approach is like a modular smart home system where each device (camera, sensor, lock, siren) is independent, communicates through a standard protocol, and can be tested, replaced, or upgraded individually. When multiple events happen simultaneously (motion detected AND door opened), the modular system handles them cleanly because each component manages its own state.

### 8.4.3.1 Easier to Reason About Concurrent Behavior

When components are composed rather than inherited, each component has a clear, bounded scope of responsibility. A `ThreadSafeCache` wrapper knows exactly one thing: synchronize access to the underlying cache. The underlying `SimpleCache` knows exactly one thing: store key-value pairs. Neither component needs to understand the other's internals.

This separation makes it straightforward to answer critical concurrent programming questions:
- "Where is synchronization applied?" -- In the ThreadSafeCache wrapper, nowhere else.
- "Can this operation deadlock?" -- Each component has at most one lock; composition does not create nested locking.
- "Is this data structure thread-safe?" -- Check if it is wrapped in a thread-safe decorator.

### 8.4.3.2 Improved Modularity and Testability

Composition inherently supports dependency injection, which is the foundation of testable concurrent code. Each composed component can be:
- **Tested in isolation**: Test the `SimpleCache` without threading concerns, test the `ThreadSafeCache` wrapper with concurrent access.
- **Mocked easily**: Replace a real `NotificationSender` with an `InMemoryNotificationSender` for testing.
- **Measured independently**: Profile the `TemplateFormatter` separately from the `NotificationService` to identify performance bottlenecks.

## Key Points to Remember

- Composition isolates thread-safety concerns into dedicated wrapper components, making them auditable and replaceable.
- Each composed component can be unit-tested without needing the full composition stack.
- Mocking composed dependencies is straightforward, enabling focused concurrent testing.
- Composition supports the "single responsibility for synchronization" principle: only one layer is responsible for thread safety.
- Processing pipelines built through composition can be parallelized naturally (each item runs through the composed pipeline independently).
- Composition eliminates "inherited lock" problems where a child class unknowingly shares a parent's monitor.
- The combination of composition + interfaces + dependency injection creates a "testing triangle" that makes concurrent code verifiable.

## Relevant Java 21 Features

- **Virtual threads (JEP 444)**: Composed pipelines execute naturally on virtual threads. Each pipeline stage delegates to the next without blocking platform threads, achieving high concurrency with simple, sequential code.
- **Records for immutable compositions**: Records as value objects in composed systems are inherently thread-safe, eliminating the need for defensive copying.
- **Pattern matching for switch**: When composed objects use sealed interfaces, pattern matching enables clean decomposition without downcasting, keeping concurrent code readable.
- **Scoped values (JEP 446, preview)**: Scoped values work well with composed components, allowing context to flow through a composition chain without mutable shared state.

## Common Pitfalls and How to Avoid Them

1. **Testing only the composition, not the components**

   If you only test `NotificationService` end-to-end, you miss edge cases in `TemplateFormatter` and `InMemoryNotificationSender`.

   **Fix**: Test each composed component independently, then test the composition as an integration test.

2. **Over-mocking losing realistic behavior**

   Replacing every dependency with a mock can hide concurrency bugs because mocks do not simulate timing.

   **Fix**: Use real implementations for concurrent tests (e.g., `InMemoryNotificationSender`) and mocks only for isolation tests.

3. **Assuming composition automatically provides thread safety**

   Composing non-thread-safe components does not make the composition thread-safe.

   **Fix**: Explicitly add a thread-safe wrapper (decorator) when concurrent access is required. Document which components are thread-safe and which are not.

4. **Ignoring decorator ordering effects on concurrency**

   Applying `LoggingDecorator(SynchronizedDecorator(base))` means logging happens outside the lock, while `SynchronizedDecorator(LoggingDecorator(base))` means logging happens inside the lock.

   **Fix**: Document the intended ordering and create factory methods that enforce it.

5. **Shared mutable state in composed components**

   If two composed components share a mutable object (e.g., a shared counter), concurrency bugs can still occur despite clean composition.

   **Fix**: Use `AtomicInteger`, `ConcurrentHashMap`, or other thread-safe types for shared state. Prefer immutable data transfer between components.

## Best Practices and Optimization Techniques

1. **Layer thread safety as a single wrapper**: Apply synchronization at exactly one level in the composition chain. Avoid redundant synchronization in multiple layers.
2. **Use `CopyOnWriteArrayList` for thread-safe listener/observer lists**: When composing observable behavior, CopyOnWriteArrayList provides safe iteration under concurrent modification.
3. **Design composed interfaces for testability**: Keep interfaces small and focused so that mock implementations are trivial.
4. **Use in-memory test doubles over mocks for concurrency tests**: Real objects with simple implementations (like `InMemoryNotificationSender`) expose timing issues that mocks hide.
5. **Compose processing pipelines for parallelism**: A `ProcessingPipeline` of independent stages can process multiple items concurrently because each item flows through its own copy of the pipeline execution.
6. **Use `AtomicInteger` / `AtomicReference` for counters and metrics in composed services**: Avoid synchronized blocks for simple counters; atomic variables are lock-free and performant.
7. **Test concurrent behavior with Awaitility**: Use the Awaitility library to assert conditions that depend on asynchronous composition.

## Edge Cases and Their Handling

1. **Empty pipeline execution**: A pipeline with no stages should return the input unchanged, not null or an error.
2. **Null results from pipeline stages**: If a stage returns null, subsequent stages would throw NPE. Define a short-circuit convention (return null early or throw).
3. **Concurrent modification of pipeline stages**: If stages are added while the pipeline is executing, results are unpredictable. Make the stage list immutable after construction or use `CopyOnWriteArrayList`.
4. **Notification to blank/null recipients**: Validation should happen at the composed boundary (sender) and results should propagate through the composition chain.
5. **Timeout in concurrent pipeline execution**: When processing items concurrently, individual items may hang. Use `Future.get(timeout)` to bound execution time.

## Interview-specific Insights

Interviewers asking about composition benefits in concurrency want to see:
- Understanding that composition isolates concerns, making each component's thread-safety properties auditable.
- Ability to design testable concurrent systems using dependency injection and composition.
- Knowledge that mocking composed dependencies enables focused testing of concurrent logic.
- Awareness that processing pipelines composed from stages can be parallelized naturally.
- Understanding the difference between "thread-safe by composition" (wrapping with a sync decorator) vs "thread-safe by inheritance" (extending with synchronized overrides).

Whiteboard tip: When asked to design a concurrent system, start by identifying the components, define interfaces for each, then compose them. Show how the thread-safe wrapper is applied at a single layer.

## Interview Q&A Section

**Q1: How does composition make concurrent behavior easier to reason about?**

```text
A1: Composition makes concurrent behavior easier to reason about through
several mechanisms:

1. Explicit boundaries: Each composed component has a clear scope. A
   ThreadSafeCache wrapper's ONLY job is synchronization. The SimpleCache's
   ONLY job is storage. You can audit thread safety by looking at one class.

2. Single lock scope: With composition, each wrapper has at most one lock.
   There are no hidden inherited locks that might cause deadlocks. The lock
   graph is flat, not hierarchical.

3. Visible synchronization: You can see exactly which operations are
   synchronized by examining the wrapper. With inheritance, you must read
   the entire hierarchy to find all synchronized methods.

4. Predictable behavior: A composed decorator always delegates to its
   delegate. There are no super.method() calls that might invoke overridden
   methods in a subclass (which is the fragile base class problem).

5. Isolated state: Each component manages its own state. Shared state is
   explicit (passed through interfaces), not implicit (inherited fields).

6. Composable safety: You can add thread safety to ANY Cache implementation
   by wrapping it, without modifying the implementation or knowing its
   internals.
```

```java
// Reasoning about thread safety is EASY with composition:

// Step 1: SimpleCache is NOT thread-safe (no synchronization)
Cache<String, Integer> simple = new SimpleCache<>();

// Step 2: ThreadSafeCache IS thread-safe (wraps with synchronized)
Cache<String, Integer> safe = new ThreadSafeCache<>(simple);

// Step 3: BoundedCache adds eviction (thread safety depends on what wraps it)
Cache<String, Integer> bounded = new BoundedCache<>(new SimpleCache<>(), 100);

// Step 4: Compose both bounded AND thread-safe
Cache<String, Integer> safeBounded = new ThreadSafeCache<>(
    new BoundedCache<>(new SimpleCache<>(), 100)
);
// Thread safety is clear: ThreadSafeCache provides it, others don't.
```

**Q2: How does composition improve testability of concurrent code?**

```text
A2: Composition improves testability in several concrete ways:

1. Dependency injection: Composed objects receive their dependencies through
   constructors. In tests, you can inject test doubles (mocks, stubs, fakes)
   that simulate specific concurrent behaviors.

2. Isolated unit testing: Test the SimpleCache without threading. Test the
   ThreadSafeCache with concurrent threads. Each test focuses on one concern.

3. In-memory test doubles: Create lightweight implementations (like
   InMemoryNotificationSender) that record interactions. These are more
   realistic than mocks for concurrent testing because they actually execute.

4. Focused concurrency tests: To test that ThreadSafeCache handles concurrent
   puts correctly, you only need to create threads that call put() on the
   wrapper. You do not need to set up an entire system.

5. Mock verification: Using Mockito, you can verify that a composed component
   was called the expected number of times, even in concurrent scenarios.

6. Pipeline testing: Test each pipeline stage independently with various
   inputs, then test the composed pipeline end-to-end.

7. Strategy testing: Test each retry strategy (fixed, exponential) in
   isolation, then test the ResilientExecutor with each strategy.
```

```java
// Testing composed components independently

// Test 1: TemplateFormatter in isolation (no concurrency needed)
@Test
void testFormatter() {
    var formatter = new TemplateFormatter();
    String result = formatter.format("Hello ${name}!", Map.of("name", "Alice"));
    assertThat(result).isEqualTo("Hello Alice!");
}

// Test 2: InMemoryNotificationSender in isolation
@Test
void testSender() {
    var sender = new InMemoryNotificationSender();
    assertThat(sender.send("user", "msg")).isTrue();
    assertThat(sender.getSentMessages()).containsExactly("user: msg");
}

// Test 3: NotificationService with real components (integration)
@Test
void testNotificationService() {
    var sender = new InMemoryNotificationSender();
    var formatter = new TemplateFormatter();
    var service = new NotificationService(sender, formatter);

    service.notify("admin@test.com", "Alert: ${event}", Map.of("event", "login"));
    assertThat(sender.getSentMessages())
        .containsExactly("admin@test.com: Alert: login");
}

// Test 4: Concurrent access to NotificationService
@Test
void testConcurrentNotifications() throws Exception {
    var sender = new InMemoryNotificationSender();
    var service = new NotificationService(sender, new TemplateFormatter());

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < 100; i++) {
            final int id = i;
            executor.submit(() ->
                service.notify("user" + id, "Message ${id}", Map.of("id", String.valueOf(id)))
            );
        }
    }
    // InMemoryNotificationSender uses CopyOnWriteArrayList, so this is safe
    assertThat(sender.getSentMessages()).hasSize(100);
}
```

**Q3: How do processing pipelines benefit from composition in concurrent systems?**

```text
A3: Processing pipelines built through composition offer several concurrency
benefits:

1. Natural parallelism: Each item flows through the pipeline independently.
   Multiple items can be processed concurrently on different threads (or
   virtual threads) because they do not share pipeline state.

2. Stage independence: Each stage is a Function<T, T> that transforms input
   to output. Stages are stateless and do not interfere with each other.

3. Easy scaling: To process N items concurrently, submit each to a virtual
   thread. The pipeline itself does not need any modification.

4. Testable stages: Each stage can be tested independently. A trim stage,
   an uppercase stage, and a suffix stage are all simple functions that can
   be verified in isolation.

5. Dynamic composition: Stages can be added based on runtime configuration.
   Different users or contexts can have different pipelines without changing
   any class hierarchy.

6. Null safety: The pipeline can define a convention for null results
   (short-circuit and return null) that is consistent across all stages.

7. Error isolation: If one item fails in its pipeline execution, other
   items on other threads continue processing unaffected.
```

```java
// Composing a pipeline and executing concurrently
var pipeline = new ProcessingPipeline<String>()
    .addStage(String::trim)
    .addStage(String::toUpperCase)
    .addStage(s -> s + " [PROCESSED]");

// ConcurrentPipelineExecutor processes each item on a virtual thread
var executor = new ConcurrentPipelineExecutor<>(pipeline);
List<String> results = executor.processAll(
    List.of("  hello  ", "  world  ", "  java  ")
);
// Results: ["HELLO [PROCESSED]", "WORLD [PROCESSED]", "JAVA [PROCESSED]"]

// Each item runs independently:
// Virtual Thread 1: "  hello  " -> trim -> "hello" -> upper -> "HELLO" -> suffix
// Virtual Thread 2: "  world  " -> trim -> "world" -> upper -> "WORLD" -> suffix
// Virtual Thread 3: "  java  "  -> trim -> "java"  -> upper -> "JAVA"  -> suffix
```

**Q4: What is the "single responsibility for synchronization" principle?**

```text
A4: The "single responsibility for synchronization" principle states that
in a composed system, exactly ONE layer should be responsible for thread
safety. This is a concurrent-specific application of the Single Responsibility
Principle.

Why this matters:
- If multiple layers add synchronization, you get redundant locking (reduced
  performance) or nested locking (deadlock risk).
- If NO layer adds synchronization, you get data races.
- With exactly one layer, the system is both safe and efficient.

How composition enables this:
- The base component (SimpleCache) is NOT thread-safe. It is simple and fast.
- A dedicated wrapper (ThreadSafeCache) adds thread safety.
- Higher-level components (BoundedCache) do NOT add synchronization -- they
  rely on being wrapped by ThreadSafeCache if needed.
- The composition order makes the synchronization layer explicit:
    ThreadSafeCache(BoundedCache(SimpleCache))

Contrast with inheritance:
- A parent class might add synchronized methods.
- A child class might add its own synchronized methods.
- The interaction between these synchronization layers is implicit and
  potentially dangerous.
```

```java
// Single responsibility for synchronization

// Layer 1: Storage (no sync) - simple, testable, fast
class SimpleCache<K, V> implements Cache<K, V> {
    private final Map<K, V> store = new HashMap<>();
    // All operations are unsynchronized
    public V get(K key) { return store.get(key); }
    public void put(K key, V value) { store.put(key, value); }
}

// Layer 2: Thread safety (ONLY sync layer)
class ThreadSafeCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final Object lock = new Object();

    public V get(K key) {
        synchronized (lock) { return delegate.get(key); }
    }
    public void put(K key, V value) {
        synchronized (lock) { delegate.put(key, value); }
    }
}

// Layer 3: Eviction (no sync) - relies on ThreadSafeCache wrapping it
class BoundedCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;
    private final int maxSize;
    // Eviction logic, no synchronization
}

// Correct composition: sync wraps everything
Cache<K, V> cache = new ThreadSafeCache<>(
    new BoundedCache<>(new SimpleCache<>(), 1000)
);
```

**Q5: How would you design a notification system using composition for concurrency?**

```text
A5: A well-designed notification system using composition separates concerns
into independently testable components:

1. MessageFormatter (interface): Transforms templates into messages.
   - TemplateFormatter: Replaces ${key} placeholders.
   - HtmlFormatter, MarkdownFormatter: Alternative formatters.

2. NotificationSender (interface): Sends formatted messages.
   - EmailSender, SmsSender: Production implementations.
   - InMemoryNotificationSender: Test double that records messages.

3. NotificationService: Composes a formatter and sender. Orchestrates the
   formatting and sending workflow. Tracks success/failure counts using
   AtomicInteger for thread safety.

Benefits for concurrency:
- The service uses AtomicInteger for counters (lock-free, thread-safe).
- InMemoryNotificationSender uses CopyOnWriteArrayList (thread-safe reads).
- Each dependency can be tested independently.
- The sender can be swapped (email in prod, in-memory in tests) via DI.
- Multiple notifications can be sent concurrently because the service's
  notify() method is stateless (only atomic counters are mutated).

This design would never work well with inheritance because you would need
classes like EmailTemplateNotifier, SmsTemplateNotifier, EmailHtmlNotifier,
SmsHtmlNotifier -- an explosion of subclasses.
```

```java
// Composition-based notification system
interface NotificationSender {
    boolean send(String recipient, String message);
}

interface MessageFormatter {
    String format(String template, Map<String, String> variables);
}

class NotificationService {
    private final NotificationSender sender;
    private final MessageFormatter formatter;
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    NotificationService(NotificationSender sender, MessageFormatter formatter) {
        this.sender = Objects.requireNonNull(sender);
        this.formatter = Objects.requireNonNull(formatter);
    }

    boolean notify(String recipient, String template, Map<String, String> vars) {
        String message = formatter.format(template, vars);
        boolean success = sender.send(recipient, message);
        if (success) successCount.incrementAndGet();
        else failureCount.incrementAndGet();
        return success;
    }
}

// Production: email sender + template formatter
var prodService = new NotificationService(new EmailSender(), new TemplateFormatter());

// Testing: in-memory sender + same formatter
var testSender = new InMemoryNotificationSender();
var testService = new NotificationService(testSender, new TemplateFormatter());
testService.notify("user@test.com", "Hello ${name}!", Map.of("name", "Alice"));
assertThat(testSender.getSentMessages()).containsExactly("user@test.com: Hello Alice!");
```

## Code Examples

- Source: [CompositionBenefits.java](src/main/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBenefits.java)
- Test: [CompositionBenefitsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBenefitsTest.java)
