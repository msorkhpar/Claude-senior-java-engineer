# 8.1.5. Dependency Inversion Principle (DIP)

## Concept Explanation

The Dependency Inversion Principle states that **high-level modules should not depend on low-level modules; both should depend on abstractions. Abstractions should not depend on details; details should depend on abstractions.**

**Real-world analogy**: Think of an electrical outlet and a plug. The outlet (high-level infrastructure) does not know whether a lamp, a phone charger, or a toaster is plugged in. The appliance (low-level detail) does not know whether the power comes from solar, wind, or coal. Both conform to the same standard: the plug/socket interface (abstraction). You can swap the power source or the appliance independently.

### 8.1.5.1. Definition and Purpose of DIP

Robert C. Martin defines DIP with two rules:
1. **High-level modules should not depend on low-level modules.** Both should depend on abstractions.
2. **Abstractions should not depend on details.** Details should depend on abstractions.

Without DIP, a `NotificationManager` (high-level) directly creates an `EmailSender` (low-level). Changing to SMS requires modifying the manager. With DIP, both depend on a `MessageSender` abstraction.

### 8.1.5.2. Applying DIP in Concurrent Programming

In concurrent systems, DIP allows swapping execution strategies:
- A `JobScheduler` depends on an `ExecutionStrategy` abstraction
- `FixedPoolStrategy` uses a fixed thread pool
- `VirtualThreadStrategy` uses Java 21 virtual threads
- The scheduler doesn't know or care which thread model is used

### 8.1.5.3. Decoupling Modules Using Abstractions

Abstractions (interfaces) sit between modules:
- `DataRepository` abstracts persistence (in-memory, database, file system)
- `Logger` abstracts logging (console, file, remote)
- `MessageSender` abstracts notification delivery (email, SMS, push)

### 8.1.5.4. DIP and Testability of Concurrent Code

DIP makes concurrent code testable by allowing injection of controlled implementations:
- In tests, inject `InMemoryRepository` instead of a real database
- In tests, inject `InMemoryLogger` instead of a file-based logger
- No need for Testcontainers or WireMock for unit tests of business logic

## Key Points to Remember

- DIP is the foundation for Dependency Injection (DI) frameworks like Spring
- The "inversion" is in the direction of dependency: instead of high-level depending on low-level, both depend on an abstraction owned by the high-level module
- Constructor injection is the most common way to apply DIP in Java
- DIP enables the Strategy pattern naturally
- In concurrent code, DIP allows swapping between platform threads and virtual threads
- DIP dramatically improves testability by enabling mock/stub/fake injection

## Relevant Java 21 Features

- **Virtual threads**: A new `ExecutionStrategy` implementation using `Executors.newVirtualThreadPerTaskExecutor()` can be swapped in without changing the scheduler
- **Sealed interfaces**: Can define bounded abstraction hierarchies where each implementation is a known detail
- **Records**: Ideal for implementing value-object abstractions (`InMemoryRepository` backed by a `ConcurrentHashMap`)
- **Pattern matching**: Works with DIP abstractions to handle different implementation types cleanly

## Common Pitfalls and How to Avoid Them

1. **Direct instantiation of dependencies**:
   ```java
   // BAD: high-level directly creates low-level
   class OrderService {
       private final MySqlDatabase db = new MySqlDatabase();
       private final SmtpEmailer emailer = new SmtpEmailer();
   }
   ```
   **Fix**: Inject abstractions through the constructor.

2. **Abstractions that leak implementation details**:
   ```java
   // BAD: abstraction reveals it's SQL-based
   interface Repository {
       ResultSet executeSql(String query); // Leaks SQL detail!
   }
   ```
   **Fix**: Define abstractions in terms of the domain, not the implementation.

3. **Service Locator anti-pattern**:
   ```java
   // BAD: hidden dependency via service locator
   class OrderService {
       void process() {
           var db = ServiceLocator.get(Database.class); // Hidden dependency!
       }
   }
   ```
   **Fix**: Use constructor injection so dependencies are explicit and visible.

## Best Practices and Optimization Techniques

1. Own the abstraction in the high-level module's package -- the interface belongs to the consumer, not the provider
2. Use constructor injection for all dependencies -- makes dependencies explicit and immutable
3. Validate non-null dependencies in constructors with `Objects.requireNonNull`
4. Create in-memory implementations for testing -- faster and more reliable than mocks
5. Define abstractions at the boundary between modules/layers
6. In concurrent code, abstract the execution model (thread pool, virtual threads) behind an interface

## Edge Cases and Their Handling

1. **Null dependencies**: Reject with `NullPointerException` in constructors
2. **Blank inputs**: Validate at the service level, not the abstraction level
3. **Multiple implementations**: Use factory methods or DI frameworks to select the right implementation at runtime
4. **Circular dependencies**: DIP helps break cycles -- if A depends on B and B depends on A, introduce an abstraction between them
5. **Shutdown ordering**: When composing concurrent components, the high-level orchestrator must manage shutdown order

## Interview-specific Insights

Interviewers often focus on:
- Explaining why DIP is not the same as Dependency Injection (DI)
- Refactoring tightly coupled code to use DIP
- Discussing how DIP improves testability
- Understanding the relationship between DIP and frameworks like Spring

Common tricky questions:
- "What is the difference between DIP, DI, and IoC?"
- "Who owns the abstraction -- the producer or the consumer?"
- "Can you apply DIP without a DI framework?"

## Interview Q&A Section

**Q1: What is the Dependency Inversion Principle and how does it differ from Dependency Injection?**

```text
A1: DIP and DI are related but distinct concepts:

Dependency Inversion Principle (DIP):
- A design PRINCIPLE stating that high-level and low-level modules should both 
  depend on abstractions
- It's about the DIRECTION of dependencies in your architecture
- It says nothing about how dependencies are provided

Dependency Injection (DI):
- A TECHNIQUE for providing dependencies to a class from the outside
- Constructor injection, setter injection, method injection
- Can be done manually or via frameworks (Spring, Guice, Dagger)

Inversion of Control (IoC):
- A broader PATTERN where the framework calls your code (not the other way around)
- DI is one form of IoC
- Event-driven programming is another form of IoC

You can apply DIP without DI (e.g., using factory methods). You can use DI 
without DIP (injecting concrete classes). They work best together.
```

```java
// DIP without DI framework (manual injection)
interface MessageSender {
    void send(String recipient, String message);
}

class EmailSender implements MessageSender {
    public void send(String recipient, String message) {
        // Send email
    }
}

class SmsSender implements MessageSender {
    public void send(String recipient, String message) {
        // Send SMS
    }
}

// High-level module depends on abstraction (DIP)
class NotificationManager {
    private final MessageSender sender;

    // Dependency injected via constructor (DI technique)
    NotificationManager(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender);
    }

    void notifyUser(String user, String msg) {
        sender.send(user, msg);
    }
}

// Manual wiring -- no framework needed
var manager = new NotificationManager(new EmailSender());
// Or swap to SMS without changing NotificationManager
var smsManager = new NotificationManager(new SmsSender());
```

**Q2: How does DIP improve testability, especially for concurrent code?**

```text
A2: DIP improves testability by allowing injection of controlled implementations:

Without DIP:
- Business logic directly uses a thread pool, database, and email server
- Tests require real infrastructure or complex mocking
- Concurrent tests are flaky because they depend on thread timing

With DIP:
- Inject InMemoryRepository instead of a real database
- Inject InMemoryLogger instead of a file logger
- Inject a single-threaded executor for deterministic testing
- Each test controls its own dependencies completely

Concrete benefits for concurrent code:
1. Test business logic without threads (inject synchronous strategy)
2. Test concurrency separately with simple tasks
3. No Testcontainers needed for unit tests
4. No flaky tests due to network/timing issues
5. Fast test execution -- in-memory is orders of magnitude faster
```

```java
// Abstraction
interface DataRepository {
    void save(String key, String value);
    Optional<String> findByKey(String key);
}

// In-memory implementation for testing
class InMemoryRepository implements DataRepository {
    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    public void save(String key, String value) { store.put(key, value); }
    public Optional<String> findByKey(String key) { return Optional.ofNullable(store.get(key)); }
}

// Business service depends on abstraction
class BusinessService {
    private final DataRepository repository;
    private final Logger logger;

    BusinessService(DataRepository repository, Logger logger) {
        this.repository = Objects.requireNonNull(repository);
        this.logger = Objects.requireNonNull(logger);
    }

    void processData(String key, String value) {
        repository.save(key, value);
        logger.log("Processed: " + key);
    }
}

// Test: clean, fast, deterministic
@Test
void testProcessData() {
    var repo = new InMemoryRepository();
    var logger = new InMemoryLogger();
    var service = new BusinessService(repo, logger);

    service.processData("k1", "v1");

    assertThat(repo.findByKey("k1")).contains("v1");
    assertThat(logger.getLogEntries()).contains("Processed: k1");
}
```

**Q3: How does DIP apply to switching between platform threads and virtual threads?**

```text
A3: DIP makes thread model switching transparent to business logic:

1. Define an ExecutionStrategy abstraction
2. Implement FixedPoolStrategy (platform threads) and VirtualThreadStrategy
3. The JobScheduler depends only on ExecutionStrategy
4. At startup, choose the strategy based on configuration or environment

Benefits:
- Business logic doesn't import java.util.concurrent directly
- Switching from platform to virtual threads is a one-line configuration change
- Testing can use a single-threaded strategy for determinism
- Performance tuning doesn't require changing business code

This is exactly how modern frameworks handle thread models:
- Spring WebFlux abstracts the event loop
- Quarkus allows switching between platform and virtual threads via config
- Vert.x uses an event loop abstraction
```

```java
// Abstraction for execution strategy
interface ExecutionStrategy {
    <T> Future<T> execute(Callable<T> task);
    void shutdown();
}

// Platform thread implementation
class FixedPoolStrategy implements ExecutionStrategy {
    private final ExecutorService executor;
    FixedPoolStrategy(int poolSize) { executor = Executors.newFixedThreadPool(poolSize); }
    public <T> Future<T> execute(Callable<T> task) { return executor.submit(task); }
    public void shutdown() { executor.shutdown(); }
}

// Virtual thread implementation -- same interface
class VirtualThreadStrategy implements ExecutionStrategy {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public <T> Future<T> execute(Callable<T> task) { return executor.submit(task); }
    public void shutdown() { executor.shutdown(); }
}

// High-level module -- doesn't know about thread models
class JobScheduler {
    private final ExecutionStrategy strategy;
    JobScheduler(ExecutionStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy);
    }
    <T> Future<T> scheduleJob(Callable<T> job) {
        return strategy.execute(job);
    }
}

// Switch thread model without touching JobScheduler
var scheduler = new JobScheduler(new VirtualThreadStrategy()); // or FixedPoolStrategy
```

**Q4: Who should own the abstraction -- the producer or the consumer?**

```text
A4: The CONSUMER (high-level module) should own the abstraction. This is the 
"inversion" in Dependency Inversion:

Traditional dependency direction:
  NotificationManager -> EmailSender
  (high-level depends on low-level)

Inverted dependency direction:
  NotificationManager -> MessageSender (interface, owned by high-level)
  EmailSender -> MessageSender (low-level depends on high-level's abstraction)

Why the consumer owns it:
1. The abstraction is shaped by the consumer's NEEDS, not the producer's capabilities
2. The consumer defines what it requires; implementations adapt to meet it
3. The consumer can change the abstraction if its needs change
4. Multiple producers can implement the same consumer-defined abstraction

Package structure reflecting ownership:
  com.myapp.notification/
    MessageSender.java          (abstraction -- owned by high-level)
    NotificationManager.java    (high-level consumer)
  com.myapp.notification.email/
    EmailSender.java            (low-level producer, depends on abstraction above)
  com.myapp.notification.sms/
    SmsSender.java              (another low-level producer)
```

```java
// Package: com.myapp.scheduling (high-level owns the abstraction)
package com.myapp.scheduling;

public interface ExecutionStrategy {
    <T> Future<T> execute(Callable<T> task);
    void shutdown();
}

public class JobScheduler {
    private final ExecutionStrategy strategy;
    public JobScheduler(ExecutionStrategy strategy) { /* ... */ }
}

// Package: com.myapp.scheduling.threadpool (low-level implements it)
package com.myapp.scheduling.threadpool;

import com.myapp.scheduling.ExecutionStrategy;

public class FixedPoolStrategy implements ExecutionStrategy {
    // Low-level depends on high-level's abstraction
}

// Package: com.myapp.scheduling.virtual (another low-level implementation)
package com.myapp.scheduling.virtual;

import com.myapp.scheduling.ExecutionStrategy;

public class VirtualThreadStrategy implements ExecutionStrategy {
    // Another low-level depending on the same high-level abstraction
}
```

**Q5: What are common anti-patterns that violate DIP?**

```text
A5: Common DIP anti-patterns:

1. new keyword in business logic:
   Creating concrete dependencies inline (new MySqlDatabase()) instead of 
   accepting an abstraction via constructor.

2. Static utility classes:
   UserService.getInstance() or DatabaseUtils.query() -- hidden dependencies 
   that cannot be swapped or mocked.

3. Service Locator pattern:
   var db = ServiceLocator.get(Database.class) -- dependencies are resolved 
   at runtime from a global registry, making them invisible in the API.

4. Framework coupling:
   Business logic importing Spring annotations or Hibernate classes directly. 
   The domain layer should be framework-agnostic.

5. Concrete return types from factory methods:
   returning ArrayList<String> instead of List<String> -- ties callers to 
   the implementation.

6. God configuration class:
   One class that wires everything together with hardcoded new statements. 
   Use a DI container or at least factory methods.

Prevention: code review checklist item -- "Does this class create its own 
dependencies, or are they injected?"
```

```java
// Anti-pattern 1: new in business logic
class OrderService {
    // BAD: hardcoded dependency
    private final MySqlDatabase db = new MySqlDatabase("jdbc:mysql://...");

    void processOrder(Order o) { db.insert(o); }
}

// Fix: inject abstraction
class OrderService {
    private final OrderRepository repository;

    OrderService(OrderRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    void processOrder(Order o) { repository.save(o); }
}

// Anti-pattern 2: static utility
class OrderService {
    void processOrder(Order o) {
        DatabaseUtils.save(o);     // Hidden, untestable dependency
        EmailUtils.sendReceipt(o); // Another hidden dependency
    }
}

// Fix: inject via abstractions
class OrderService {
    private final OrderRepository repo;
    private final ReceiptSender sender;

    OrderService(OrderRepository repo, ReceiptSender sender) {
        this.repo = Objects.requireNonNull(repo);
        this.sender = Objects.requireNonNull(sender);
    }

    void processOrder(Order o) {
        repo.save(o);
        sender.sendReceipt(o);
    }
}
```

## Code Examples

- Source: [DependencyInversion.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/DependencyInversion.java)
- Test: [DependencyInversionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/DependencyInversionTest.java)
