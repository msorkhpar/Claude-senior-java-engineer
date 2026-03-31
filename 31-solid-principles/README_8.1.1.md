# 8.1.1. Single Responsibility Principle (SRP)

## Concept Explanation

The Single Responsibility Principle states that **a class should have only one reason to change**. In other words, every class should encapsulate exactly one responsibility or concern, and all of its methods and fields should be narrowly aligned with that responsibility.

**Real-world analogy**: Think of a restaurant kitchen. The head chef does not also serve tables, wash dishes, and manage the finances. Each role -- cook, server, dishwasher, accountant -- has a single clearly defined responsibility. When the menu changes, only the chef's work changes. When tax laws change, only the accountant's work changes. If one person did everything, any change in any domain would affect that one overloaded person.

### 8.1.1.1. Definition and Purpose of SRP

Robert C. Martin originally stated SRP as: "A class should have one, and only one, reason to change." A "reason to change" maps to a stakeholder or business concern. If a class serves two different stakeholders (e.g., the operations team and the accounting team), it has two reasons to change and violates SRP.

The purpose of SRP is to:
- Reduce coupling between unrelated concerns
- Improve cohesion within each class
- Make code easier to test, understand, and maintain
- Limit the blast radius of changes

### 8.1.1.2. Applying SRP in Concurrent Programming

In concurrent systems, SRP becomes even more critical. When a single class handles multiple concerns -- such as producing tasks, executing them, and aggregating results -- it becomes extremely difficult to reason about thread safety. Each concern may have different synchronization requirements.

By separating concerns:
- A `TaskProducer` can be stateless and thread-safe by default
- A `TaskExecutor` encapsulates the thread pool lifecycle
- A `ResultAggregator` manages its own synchronization for collecting results

### 8.1.1.3. Benefits of SRP for Concurrency

1. **Isolated synchronization**: Each class manages only its own locks/atomics
2. **Reduced contention**: Fewer threads compete for the same lock
3. **Easier testing**: You can test thread-safe counting independently from formatting
4. **Clearer ownership**: It is obvious which class owns which shared state
5. **Composability**: Small, focused classes can be assembled into concurrent pipelines

## Key Points to Remember

- SRP is about **reasons to change**, not about "doing one thing"
- A class can have many methods but still satisfy SRP if they all serve the same concern
- Over-splitting classes is also a problem -- don't create one class per method
- In concurrency, SRP helps isolate shared mutable state into clearly owned classes
- SRP often leads naturally to the Facade pattern: a coordinator class delegates to focused classes

## Relevant Java 21 Features

- **Records**: Ideal for single-responsibility data carriers (`record User(String name, int age) {}`)
- **Sealed classes**: Allow modeling a bounded set of types where each has a single purpose
- **Virtual threads**: Make it practical to have fine-grained task producers and consumers without worrying about thread overhead
- **Pattern matching for switch**: Enables clean dispatching without bloating a single class with unrelated logic

## Common Pitfalls and How to Avoid Them

1. **God class**: A single class that handles data access, business logic, and presentation.
   ```java
   // BAD: One class doing everything
   class OrderManager {
       void saveToDatabase(Order o) { /* SQL */ }
       double calculateTax(Order o) { /* tax logic */ }
       String formatReceipt(Order o) { /* formatting */ }
   }
   ```
   **Fix**: Split into `OrderRepository`, `TaxCalculator`, and `ReceiptFormatter`.

2. **Mixing thread lifecycle with business logic**:
   ```java
   // BAD: Business logic mixed with thread management
   class DataProcessor {
       private final ExecutorService pool = Executors.newFixedThreadPool(4);
       double computeAverage(List<Integer> data) { /* ... */ }
       void shutdown() { pool.shutdown(); }
   }
   ```
   **Fix**: Inject the `ExecutorService` or separate the thread pool management into its own class.

3. **Notification side effects inside repositories**:
   ```java
   // BAD: Repository also sends emails
   class UserRepository {
       void save(User u) {
           db.insert(u);
           emailService.send("New user: " + u.name()); // Side effect!
       }
   }
   ```
   **Fix**: Move notification into a `UserService` orchestrator that calls both `UserRepository.save()` and `NotificationService.send()`.

## Best Practices and Optimization Techniques

1. Ask the "who cares?" question: If two different stakeholders would request changes to the same class, it probably violates SRP
2. Use constructor injection to compose focused classes into higher-level services
3. Prefer immutable value objects (records) for data transfer -- they have exactly one responsibility: holding data
4. In concurrent code, make each thread-safe class own a single piece of shared state (one `AtomicLong`, one `ConcurrentHashMap`, etc.)
5. Use the Facade pattern to provide a simple API that delegates to focused classes

## Edge Cases and Their Handling

1. **Empty inputs**: Repository methods should handle empty collections and null IDs gracefully
2. **Null dependencies**: Constructor injection should validate non-null with `Objects.requireNonNull`
3. **Thread safety at boundaries**: When composing SRP classes, ensure the orchestrator also handles thread safety at the composition level
4. **Exception propagation**: Each single-responsibility class should define its own exception contract -- don't let persistence exceptions leak into notification code

## Interview-specific Insights

Interviewers often focus on:
- Asking you to refactor a God class into SRP-compliant classes
- Discussing how SRP applies in real-world systems (microservices, concurrent code)
- Testing whether you understand that SRP is about "reasons to change," not just "doing one thing"
- Asking about trade-offs: when is splitting too aggressive?

Common tricky questions:
- "How do you decide where to draw the boundary for a single responsibility?"
- "Can SRP be taken too far? What problems does over-splitting cause?"
- "How does SRP relate to cohesion and coupling?"

## Interview Q&A Section

**Q1: What is the Single Responsibility Principle and why is it important?**

```text
A1: The Single Responsibility Principle states that a class should have only one reason 
to change. "Reason to change" maps to a stakeholder or business concern.

Importance:
1. Reduces coupling: Changes in one concern don't ripple to unrelated code
2. Improves testability: Focused classes are easier to unit test
3. Enhances readability: Developers can quickly understand what a class does
4. Simplifies maintenance: Bug fixes and features are localized
5. Enables parallel development: Teams can work on different concerns independently

For example, a UserRepository that also sends emails violates SRP because changes to 
email templates would require modifying a class that should only care about persistence.
```

```java
// SRP violation
class UserService {
    void createUser(String name) {
        // persistence
        database.save(name);
        // notification -- different concern!
        emailClient.send("Welcome " + name);
        // logging -- yet another concern!
        auditLog.record("Created user: " + name);
    }
}

// SRP applied
class UserRepository { void save(String name) { database.save(name); } }
class WelcomeNotifier { void notify(String name) { emailClient.send("Welcome " + name); } }
class AuditLogger { void record(String action) { auditLog.record(action); } }
class UserService {
    UserService(UserRepository repo, WelcomeNotifier notifier, AuditLogger logger) { /*...*/ }
    void createUser(String name) {
        repo.save(name);
        notifier.notify(name);
        logger.record("Created user: " + name);
    }
}
```

**Q2: How does SRP apply to concurrent programming?**

```text
A2: In concurrent programming, SRP is critical because:

1. Isolated synchronization: Each class manages its own locks/atomics, making 
   thread-safety reasoning local rather than global
2. Reduced contention: When unrelated concerns share the same lock, threads 
   contend unnecessarily. SRP eliminates this.
3. Easier deadlock prevention: With isolated locks, lock ordering is simpler
4. Testability: You can stress-test each concurrent component independently

For example, separating a thread-safe counter from its display formatter means the 
counter can use AtomicLong internally without the formatter needing synchronization 
at all.
```

```java
// Thread-safe counter -- single responsibility: counting
class ThreadSafeCounter {
    private final AtomicLong count = new AtomicLong(0);
    long increment() { return count.incrementAndGet(); }
    long getCount() { return count.get(); }
}

// Formatter -- single responsibility: display formatting (no synchronization needed)
class CounterFormatter {
    String format(long count) {
        return count == 0 ? "Zero" : "Count: " + count;
    }
}

// Composition: each class handles its own concern
var counter = new ThreadSafeCounter();
var formatter = new CounterFormatter();
System.out.println(formatter.format(counter.increment())); // "Count: 1"
```

**Q3: Can SRP be taken too far? What are the trade-offs?**

```text
A3: Yes, SRP can be over-applied, leading to:

1. Class explosion: Too many tiny classes make the codebase hard to navigate
2. Indirection overload: Simple operations require tracing through many layers
3. Premature abstraction: Splitting before understanding the domain leads to 
   wrong boundaries that are expensive to fix later
4. Performance overhead: Excessive object creation and method calls in hot paths

The right balance:
- Split when you can identify distinct stakeholders or change drivers
- Don't split purely for the sake of having small classes
- Consider cohesion: methods that operate on the same data usually belong together
- Refactor toward SRP when you see actual pain (frequent merge conflicts, 
  test fragility, difficulty understanding a class)
```

```java
// Over-application of SRP (too granular)
class NameValidator { boolean isValid(String name) { return name != null; } }
class NameSanitizer { String sanitize(String name) { return name.trim(); } }
class NameFormatter { String format(String name) { return name.toUpperCase(); } }
class NameProcessor {
    NameProcessor(NameValidator v, NameSanitizer s, NameFormatter f) { /*...*/ }
}

// Better: these are all "name processing" -- one cohesive responsibility
class NameProcessor {
    String process(String name) {
        if (name == null) throw new IllegalArgumentException("Name required");
        return name.trim().toUpperCase();
    }
}
```

**Q4: How do you identify SRP violations in existing code?**

```text
A4: Common indicators of SRP violations:

1. Class name includes "And" or "Manager" or "Handler" (e.g., UserAndOrderManager)
2. The class has methods that don't interact with each other's fields
3. Different groups of methods use different subsets of instance variables
4. The class imports from many unrelated packages (UI, database, networking)
5. Changes to one feature require modifying this class alongside unrelated changes
6. The class is frequently involved in merge conflicts
7. Unit tests require complex setup because the class depends on many concerns

Refactoring approach:
- Identify groups of methods that operate on the same fields
- Extract each group into its own class
- Create a coordinator/facade that composes the extracted classes
```

```java
// Identifying the violation: methods use different fields
class ReportService {
    private final Database db;         // Used by data methods
    private final EmailClient email;   // Used by notification methods
    private final PdfRenderer pdf;     // Used by rendering methods

    List<Order> fetchOrders() { return db.query("SELECT ..."); }
    void notifyManager(String msg) { email.send("manager@co.com", msg); }
    byte[] renderPdf(List<Order> orders) { return pdf.render(orders); }
}

// After SRP refactoring
class OrderRepository { List<Order> fetchOrders() { /* db logic */ } }
class ManagerNotifier { void notify(String msg) { /* email logic */ } }
class ReportRenderer { byte[] render(List<Order> orders) { /* pdf logic */ } }
```

**Q5: How does SRP relate to microservices architecture?**

```text
A5: SRP at the class level is a microcosm of SRP at the service level:

1. Each microservice owns one bounded context (e.g., user management, billing, 
   notifications)
2. A change in billing logic should not require redeploying the user service
3. Each service has its own database, deployment pipeline, and team ownership

The parallel:
- Class-level SRP: separate UserRepository from NotificationService
- Service-level SRP: separate user-service from notification-service

Benefits in microservices:
- Independent deployment: each service changes for only one reason
- Team autonomy: teams own their service end-to-end
- Fault isolation: a bug in notifications doesn't crash user management
- Technology diversity: each service can use the best tool for its concern

Warning: Just as classes can be over-split, microservices can be too fine-grained, 
leading to distributed monolith problems (excessive inter-service calls, data 
consistency challenges).
```

```java
// Monolith violating SRP at service level
class ECommerceApp {
    void processOrder(Order o) {
        inventoryDb.reserve(o.items());      // Inventory concern
        paymentGateway.charge(o.total());    // Payment concern
        emailService.sendConfirmation(o);    // Notification concern
        analyticsClient.trackPurchase(o);    // Analytics concern
    }
}

// Microservice-style SRP (each would be a separate service)
interface InventoryService { void reserve(List<Item> items); }
interface PaymentService { void charge(BigDecimal amount); }
interface NotificationService { void sendConfirmation(Order o); }
interface AnalyticsService { void trackPurchase(Order o); }

// Orchestrator composes them
class OrderOrchestrator {
    OrderOrchestrator(InventoryService inv, PaymentService pay,
                      NotificationService notif, AnalyticsService analytics) { /*...*/ }
}
```

## Code Examples

- Source: [SingleResponsibility.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/SingleResponsibility.java)
- Test: [SingleResponsibilityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/SingleResponsibilityTest.java)
