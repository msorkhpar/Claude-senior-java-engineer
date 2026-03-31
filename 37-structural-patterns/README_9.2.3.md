# 9.2.3. Proxy Pattern

## Concept Explanation

The Proxy Pattern is a structural design pattern that provides a substitute or placeholder for another object. A proxy controls access to the original object, allowing you to perform operations before or after the request reaches the real object.

**Real-world analogy**: Consider a credit card as a proxy for a bank account. When you use a credit card at a store, you do not hand over your actual bank account. Instead, the card acts as a proxy that authorizes the transaction, checks your credit limit, and logs the purchase. The store interacts with the card (proxy) the same way it would with cash (real subject), but the card adds access control, logging, and deferred payment. The Proxy pattern works the same way — the proxy stands in for the real object and adds control logic.

The Proxy pattern is classified as structural because it controls how objects are accessed rather than how they are created or how they behave. The proxy and the real subject implement the same interface, so the client cannot distinguish between them.

There are several types of proxies:
- **Virtual Proxy**: Delays the creation of an expensive object until it is actually needed (lazy initialization).
- **Protection Proxy**: Controls access based on permissions or roles.
- **Remote Proxy**: Represents an object in a different address space (e.g., RMI stubs).
- **Smart Reference / Caching Proxy**: Adds additional behavior like caching, reference counting, or logging.

## Key Points to Remember

- The proxy and the real subject implement the same interface — the client cannot tell them apart.
- Virtual proxies delay expensive object creation until first use (lazy loading).
- Protection proxies enforce access control rules before delegating to the real subject.
- Caching proxies store results of expensive operations and return cached values on repeated calls.
- Logging proxies record all interactions for auditing or debugging.
- Java has built-in support for dynamic proxies via `java.lang.reflect.Proxy`.
- Dynamic proxies can only proxy interfaces, not concrete classes (for classes, use CGLIB or ByteBuddy).
- The Proxy pattern differs from Decorator: Proxy controls access, Decorator adds behavior.
- Proxies often create/manage the real subject's lifecycle; Decorators receive the component from outside.

## Relevant Java 21 Features

- **Sealed interfaces**: Seal the subject interface to control which proxies are permitted at compile time.
- **Records**: The `User` record with role-based access control demonstrates modern Java data carriers for proxy configuration.
- **Pattern matching for `switch`**: Useful in protection proxies for matching user roles.
- **Virtual threads**: Proxies can use virtual threads for async delegation without blocking platform threads.
- **`java.lang.reflect.Proxy`**: Java's built-in dynamic proxy mechanism (since Java 1.3) creates proxy instances at runtime for interfaces.

Evolution across Java versions:
- **Java 1.3**: Introduced `java.lang.reflect.Proxy` for dynamic proxies.
- **Java 5**: Generics improved type safety in proxy factories.
- **Java 8**: Lambda expressions enabled lightweight proxy-like behavior for functional interfaces.
- **Java 21**: Pattern matching and sealed classes enhance type-safe proxy dispatch.

## Common Pitfalls and How to Avoid Them

1. **Proxy becoming a god object**: A proxy that adds too many responsibilities (caching + logging + access control + lazy loading) violates SRP. Split into separate proxies or use a decorator chain.

2. **Forgetting to delegate**: A proxy that overrides a method without delegating to the real subject silently breaks functionality.
   ```java
   // Problem: Lost the real behavior
   @Override
   public String display() {
       return "Proxy display"; // forgot to delegate
   }

   // Solution: Always delegate
   @Override
   public String display() {
       checkAccess(); // proxy logic
       return realService.display(); // delegate to real subject
   }
   ```

3. **Null real subject in virtual proxy**: If the virtual proxy creates the real subject lazily, ensure thread safety for the lazy initialization.
   ```java
   // Problem: Race condition in multi-threaded environment
   private RealService getService() {
       if (service == null) {
           service = new RealService(); // not thread-safe
       }
       return service;
   }

   // Solution: Use synchronized or double-checked locking
   private volatile RealService service;
   private RealService getService() {
       if (service == null) {
           synchronized (this) {
               if (service == null) {
                   service = new RealService();
               }
           }
       }
       return service;
   }
   ```

4. **Dynamic proxy limitations**: `java.lang.reflect.Proxy` only works with interfaces, not concrete classes. Use CGLIB or ByteBuddy for class-based proxying.

5. **Performance overhead**: Each proxy call adds indirection. For hot code paths, measure whether the proxy overhead is acceptable.

## Best Practices and Optimization Techniques

1. **Use virtual proxies for expensive objects**: Defer creation until first actual use.
2. **Keep proxy logic minimal**: The proxy should focus on its single concern (access control, caching, etc.) and delegate everything else.
3. **Use `ConcurrentHashMap.computeIfAbsent`** for thread-safe caching proxies.
4. **Validate all inputs** in both the proxy and the real subject (defense in depth).
5. **Make access control rules configurable** rather than hard-coded in the proxy.
6. **Use dynamic proxies** when you need to proxy many interfaces with the same cross-cutting concern (logging, metrics).
7. **Consider using Spring AOP or AspectJ** for proxy-based concerns in enterprise applications — they handle proxy creation and management automatically.
8. **Return unmodifiable collections** from logging proxies to prevent tampering with audit trails.

## Edge Cases and Their Handling

1. **Null inputs**: Both proxies and real subjects should validate arguments. The proxy should validate before delegating to catch errors early.
2. **Non-existent resources**: Protection proxies should let `NoSuchElementException` propagate from the real subject rather than catching and hiding it.
3. **Concurrent access to virtual proxy**: Lazy initialization must be thread-safe if the proxy is shared across threads.
4. **Cache invalidation**: Caching proxies must handle cache staleness. Provide a `clearCache()` method or use time-based expiration.
5. **Proxy chains**: Stacking multiple proxies (logging on top of caching on top of protection) is valid but increases call depth.
6. **Dynamic proxy with null arguments**: `InvocationHandler.invoke` may receive a null `args` array for no-argument methods.

## Interview-specific Insights

Interviewers commonly ask about:
- The difference between Proxy and Decorator patterns
- Types of proxies and when to use each type
- How `java.lang.reflect.Proxy` works and its limitations
- How Spring Framework uses proxies for AOP, transactions, and security
- Virtual proxy vs. lazy initialization in general
- Thread safety of lazy-loading proxies

Tricky points:
- Spring creates JDK dynamic proxies for interface-based beans and CGLIB proxies for class-based beans
- The Proxy pattern controls access; the Decorator pattern adds behavior
- A proxy typically manages the lifecycle of the real subject; a decorator does not
- Dynamic proxies can only proxy interfaces in standard Java — CGLIB generates subclasses for class proxying
- `InvocationHandler.invoke` must handle `Object` methods (toString, equals, hashCode) properly

## Interview Q&A Section

**Q1: What is the Proxy pattern and what types of proxies exist?**

```text
A1: The Proxy pattern provides a surrogate or placeholder for another object to control
access to it. The proxy and the real subject implement the same interface, making them
interchangeable from the client's perspective.

Main types of proxies:

1. Virtual Proxy (Lazy Proxy):
   - Delays creation of an expensive object until first use
   - Example: Loading a large image only when display() is called
   - Useful when the object may never be needed

2. Protection Proxy (Authorization Proxy):
   - Controls access based on permissions, roles, or credentials
   - Example: Document service that checks user roles before allowing write/delete
   - Enforces security policies

3. Remote Proxy:
   - Represents an object in a different address space (different JVM, server)
   - Example: Java RMI stubs, gRPC generated classes
   - Hides network communication details

4. Smart Reference (Caching/Logging Proxy):
   - Adds supplementary operations when accessing the real object
   - Examples: caching results, counting references, logging access
   - Improves performance or observability

5. Firewall Proxy:
   - Controls network access to a set of resources
   - Protects subjects from bad clients
```

```java
// Virtual Proxy — lazy loading
ImageService proxy = new VirtualImageProxy("heavy-image.png");
// Real image NOT loaded yet
proxy.getFilename(); // Still not loaded — cheap operation
proxy.display();     // NOW the real image is loaded

// Protection Proxy — access control
DocumentService proxy = new ProtectionDocumentProxy(realService,
    new User("guest", Role.GUEST));
proxy.read("doc1");   // OK
proxy.write("doc1", "new"); // throws SecurityException

// Caching Proxy — smart reference
DataLookupService proxy = new CachingDataLookupProxy(realService);
proxy.lookup("key1"); // hits real service, caches result
proxy.lookup("key1"); // returns cached result
```

**Q2: How does the Proxy pattern differ from the Decorator pattern?**

```text
A2: While both Proxy and Decorator wrap another object and implement the same interface,
they have fundamentally different intents:

Proxy:
- Intent: CONTROL ACCESS to the real subject
- The proxy often manages the lifecycle of the real subject
- The client may not know the proxy exists
- Types: virtual, protection, caching, remote
- The proxy decides WHETHER to delegate

Decorator:
- Intent: ADD BEHAVIOR to the wrapped component
- The decorator receives the component from outside (dependency injection)
- The client knows it is composing behaviors
- The decorator always delegates to the wrapped component
- Multiple decorators are commonly stacked

Structural differences:
- Proxy may create the real subject internally (virtual proxy)
- Decorator always receives the component through its constructor
- Proxy focuses on a single subject; Decorator is reusable across components
- Proxy can refuse to delegate (protection); Decorator always delegates

In practice, the line can blur — a caching proxy looks similar to a caching decorator.
The key question is: does the wrapper control access (proxy) or add behavior (decorator)?
```

```java
// Proxy: controls access, manages lifecycle
class VirtualImageProxy implements ImageService {
    private RealImageService real; // creates internally
    public String display() {
        if (real == null) real = new RealImageService(filename); // controls creation
        return real.display();
    }
}

// Decorator: adds behavior, receives component
class LoggingNotifier implements Notifier {
    private final Notifier wrapped; // received from outside
    LoggingNotifier(Notifier wrapped) { this.wrapped = wrapped; }
    public String send(String msg) {
        log("Sending: " + msg);
        return wrapped.send(msg); // always delegates
    }
}
```

**Q3: How does `java.lang.reflect.Proxy` work and what are its limitations?**

```text
A3: Java's dynamic proxy mechanism creates proxy instances at runtime that implement
one or more interfaces. The proxy routes all method calls through an InvocationHandler.

How it works:
1. You define an InvocationHandler that intercepts all method calls
2. Proxy.newProxyInstance() creates a class at runtime that implements the target interfaces
3. When any method on the proxy is called, it invokes handler.invoke(proxy, method, args)
4. The handler can perform logic before/after delegating to the real object

Limitations:
1. INTERFACES ONLY: Can only proxy interfaces, not concrete classes
   - For classes, use CGLIB (generates subclasses) or ByteBuddy
2. Performance: Reflection-based invocation is slower than direct calls
   - JIT compiler can partially optimize this
3. Object methods: toString(), equals(), hashCode() are also routed through the handler
   - You must handle them explicitly
4. No constructor parameters: The proxy is created without parameters
5. Return type boxing: Primitive return types are auto-boxed/unboxed
6. Checked exceptions: The handler can only throw exceptions declared by the interface method
```

```java
// Creating a dynamic proxy
DataLookupService real = new RealDataLookupService();
LoggingInvocationHandler handler = new LoggingInvocationHandler(real);

DataLookupService proxy = (DataLookupService) Proxy.newProxyInstance(
    DataLookupService.class.getClassLoader(),
    new Class<?>[]{DataLookupService.class},
    handler
);

proxy.lookup("key1"); // routed through handler.invoke()

// The handler:
class LoggingInvocationHandler implements InvocationHandler {
    private final Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Calling: " + method.getName());
        Object result = method.invoke(target, args); // delegate via reflection
        System.out.println("Returned: " + result);
        return result;
    }
}
```

**Q4: How does Spring Framework use the Proxy pattern?**

```text
A4: Spring Framework uses proxies extensively for cross-cutting concerns:

1. AOP (Aspect-Oriented Programming):
   - Spring AOP creates proxies around beans to apply aspects (logging, security, etc.)
   - For interface-based beans: uses JDK dynamic proxies
   - For class-based beans: uses CGLIB to create a subclass proxy
   - @Aspect + @Around annotations define the proxy behavior

2. @Transactional:
   - Spring wraps the bean in a proxy that manages database transactions
   - The proxy begins a transaction before the method, commits after, rolls back on exception
   - This is why @Transactional does not work on private methods or self-invocation

3. @Cacheable:
   - Spring creates a caching proxy that checks the cache before invoking the method
   - If cached, returns the cached result; otherwise, invokes and caches the result

4. @Secured / @PreAuthorize:
   - Protection proxies that check user permissions before allowing method execution

5. @Lazy:
   - Virtual proxy that defers bean creation until first access

Common gotcha: Spring proxies do not intercept self-invocation (calling this.method()).
The call bypasses the proxy because it goes directly to the target object.
```

```java
// Spring's @Transactional creates a proxy:
@Service
public class OrderService {
    @Transactional
    public void placeOrder(Order order) {
        // Spring proxy: BEGIN TRANSACTION
        orderRepository.save(order);
        paymentService.charge(order);
        // Spring proxy: COMMIT (or ROLLBACK on exception)
    }

    // GOTCHA: self-invocation bypasses the proxy!
    public void processOrders(List<Order> orders) {
        for (Order order : orders) {
            this.placeOrder(order); // NOT proxied! Calls target directly.
        }
    }
}

// Equivalent proxy structure:
class OrderServiceProxy extends OrderService {
    private final OrderService target;
    private final TransactionManager txManager;

    @Override
    public void placeOrder(Order order) {
        txManager.begin();
        try {
            target.placeOrder(order);
            txManager.commit();
        } catch (Exception e) {
            txManager.rollback();
            throw e;
        }
    }
}
```

**Q5: How do you ensure thread safety in a virtual proxy?**

```text
A5: Virtual proxies that lazily initialize the real subject face a classic
thread-safety challenge when accessed from multiple threads simultaneously.

Approaches (from simplest to most efficient):

1. Synchronized method: Simple but may cause contention
2. Double-checked locking with volatile: Best balance of safety and performance
3. Initialization-on-demand holder idiom: If the proxy is a singleton
4. AtomicReference with compareAndSet: Lock-free but may create multiple instances

The double-checked locking pattern is most commonly used:
- First check without locking (fast path for already-initialized case)
- Synchronize only if not yet initialized
- Second check inside the synchronized block (prevents double creation)
- The volatile keyword ensures visibility across threads

For simple cases, making the method synchronized is often good enough.
Premature optimization of lazy initialization is a common anti-pattern.
```

```java
// Approach 1: Synchronized (simple, may contend)
public class SyncVirtualProxy implements ImageService {
    private RealImageService real;
    public synchronized String display() {
        if (real == null) real = new RealImageService(filename);
        return real.display();
    }
}

// Approach 2: Double-checked locking (preferred)
public class DCLVirtualProxy implements ImageService {
    private volatile RealImageService real;
    public String display() {
        if (real == null) {                        // first check (no lock)
            synchronized (this) {
                if (real == null) {                 // second check (with lock)
                    real = new RealImageService(filename);
                }
            }
        }
        return real.display();
    }
}

// Approach 3: Using AtomicReference (lock-free)
public class AtomicVirtualProxy implements ImageService {
    private final AtomicReference<RealImageService> ref = new AtomicReference<>();
    public String display() {
        ref.compareAndSet(null, new RealImageService(filename));
        return ref.get().display();
        // Note: may create extra instances that get discarded
    }
}
```

**Q6: When should you use a proxy vs. a simple null check or lazy initialization?**

```text
A6: Use a proxy when you need to encapsulate the control logic and keep it separate
from the client code. Use a simple null check when the laziness is internal to a class.

Use a Proxy when:
1. The client should not know about the lazy loading / access control
2. You want to swap implementations (real vs. proxy) without changing client code
3. Multiple clients need the same control behavior
4. You are applying cross-cutting concerns (logging, caching, security)
5. You want to test the control logic independently

Use a simple null check / lazy field when:
1. The laziness is purely an internal optimization
2. Only one class uses the lazy field
3. There is no access control or cross-cutting concern
4. Adding a proxy class would be over-engineering

Rule of thumb: if the control logic is a single line (null check), inline it.
If it involves complex logic (permissions, caching, logging), extract to a proxy.
```

```java
// Simple case: just use a lazy field (no proxy needed)
class Report {
    private List<String> data;
    public List<String> getData() {
        if (data == null) {
            data = loadExpensiveData(); // internal concern
        }
        return data;
    }
}

// Complex case: use a proxy (multiple concerns, client transparency)
interface ReportService {
    Report getReport(String id);
}

class CachingReportProxy implements ReportService {
    private final ReportService real;
    private final Map<String, Report> cache = new ConcurrentHashMap<>();

    public Report getReport(String id) {
        return cache.computeIfAbsent(id, real::getReport);
    }
}

class AuthReportProxy implements ReportService {
    private final ReportService real;
    private final User user;

    public Report getReport(String id) {
        if (!user.canAccess(id)) throw new SecurityException();
        return real.getReport(id);
    }
}
```

## Code Examples

- Implementation: [ProxyPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/ProxyPattern.java)
- Tests: [ProxyPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/ProxyPatternTest.java)
