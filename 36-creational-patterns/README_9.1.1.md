# 9.1.1. Singleton Pattern

## Concept Explanation

The Singleton pattern is one of the simplest yet most debated creational design patterns. Its purpose is to ensure that
a class has **exactly one instance** throughout the entire application lifecycle and provides a **global point of access**
to that instance.

**Real-world analogy**: Think of a country's government. There is only one president (or prime minister) at any given
time. No matter who asks "who is the president?", the answer is always the same single person. The office of the
president is the Singleton -- there is exactly one holder of the position, and everyone references the same one.

The Singleton pattern addresses two problems simultaneously:

1. **Controlled access to a single instance** -- useful for shared resources like database connection pools, logging
   services, configuration managers, or hardware interface controllers.
2. **Global access** -- the instance is accessible from anywhere in the code without passing it around as a parameter.

### How It Works

The core mechanics are:
- **Private constructor** -- prevents external code from instantiating the class directly.
- **Static field** -- holds the single instance reference.
- **Static factory method** -- provides the global access point, creating the instance lazily or eagerly.

### Evolution of Singleton in Java

| Era | Approach | Thread-Safe? | Notes |
|---|---|---|---|
| Pre-Java 5 | Eager initialization | Yes | Simple but wastes memory if never used |
| Pre-Java 5 | Synchronized method | Yes | Correct but slow due to lock contention |
| Java 5+ | Double-checked locking with volatile | Yes | Efficient and lazy |
| Java 5+ | Enum-based singleton | Yes | Serialization-safe, reflection-safe |
| Java 5+ | Holder idiom (Bill Pugh) | Yes | Lazy, no synchronization overhead |

## Key Points to Remember

1. Singleton guarantees only **one instance** per classloader, not per JVM (multiple classloaders can each load their own
   copy).
2. The private constructor prevents instantiation from outside the class.
3. Thread safety is critical -- naive lazy initialization leads to race conditions in multithreaded environments.
4. The `volatile` keyword is essential in double-checked locking to prevent instruction reordering.
5. Enum-based Singleton is recommended by Joshua Bloch (Effective Java) as the most robust approach.
6. Singletons can complicate unit testing because they introduce hidden global state.
7. In modern Java, dependency injection frameworks (Spring, Guice) often manage singleton scope more elegantly.
8. Serialization and reflection can break non-enum Singletons unless specifically guarded against.

## Relevant Java 21 Features

- **Sealed classes**: You can declare a Singleton as a sealed class to prevent subclassing, adding an extra layer of
  protection beyond the private constructor.
- **Records**: While records cannot be used directly for Singleton (they require a public canonical constructor), they
  can model the data a Singleton manages.
- **Virtual threads**: Singletons managing thread pools should consider virtual threads for scalability. A Singleton
  executor service could return a virtual-thread-based executor.
- **Pattern matching for switch**: Can be used when Singletons implement sealed interfaces, enabling exhaustive pattern
  matching over the singleton instances.

## Common Pitfalls and How to Avoid Them

1. **Broken Singleton via reflection**:
   ```java
   // An attacker can create a second instance
   Constructor<MySingleton> constructor = MySingleton.class.getDeclaredConstructor();
   constructor.setAccessible(true);
   MySingleton second = constructor.newInstance(); // Different instance!
   ```
   **Solution**: Throw an exception in the constructor if an instance already exists, or use an enum-based Singleton.

2. **Broken Singleton via serialization**:
   ```java
   // Deserializing creates a new instance
   ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("singleton.ser"));
   oos.writeObject(MySingleton.getInstance());
   ObjectInputStream ois = new ObjectInputStream(new FileInputStream("singleton.ser"));
   MySingleton deserialized = (MySingleton) ois.readObject(); // Different instance!
   ```
   **Solution**: Implement `readResolve()` to return the existing instance, or use an enum-based Singleton.

3. **Double-checked locking without volatile**:
   ```java
   // BROKEN -- missing volatile
   private static MySingleton instance;
   public static MySingleton getInstance() {
       if (instance == null) {
           synchronized (MySingleton.class) {
               if (instance == null) {
                   instance = new MySingleton(); // Instruction reordering can expose partially constructed object
               }
           }
       }
       return instance;
   }
   ```
   **Solution**: Always declare the instance field as `volatile`.

4. **Classloader issues**: In environments with multiple classloaders (e.g., application servers), each classloader can
   load its own copy of the Singleton class.
   **Solution**: Use a common parent classloader or a dependency injection framework to manage the scope.

5. **Singleton as a hidden dependency**: Code that uses `Singleton.getInstance()` everywhere creates tight coupling.
   **Solution**: Pass the Singleton as a constructor parameter (dependency injection) to improve testability.

## Best Practices and Optimization Techniques

1. **Prefer enum Singleton** for simplicity, thread safety, serialization safety, and reflection safety.
2. **Use the Holder idiom** (Bill Pugh) when you need a class-based Singleton with lazy initialization.
3. **Consider dependency injection** instead of the Singleton pattern -- frameworks like Spring manage singleton scope
   and make testing easier.
4. **Keep Singletons stateless** or minimize mutable state to reduce thread-safety complexity.
5. **Document thread-safety guarantees** for any Singleton that holds mutable state.
6. **Avoid eager initialization** if the Singleton is expensive to create and might not be used.
7. **Use interface-based design** -- have the Singleton implement an interface so it can be mocked in tests.

## Edge Cases and Their Handling

1. **Null instance before first access**: Lazy Singletons must handle the initial null state safely. Use
   double-checked locking with volatile, the Holder idiom, or enum.
2. **Concurrent first access**: Multiple threads calling `getInstance()` simultaneously for the first time must be
   handled with proper synchronization.
3. **Garbage collection**: A Singleton referenced only by a WeakReference can be collected. Use strong references.
4. **Multiple classloaders**: Each classloader creates its own static fields. In web application servers, this can lead
   to multiple "singletons."
5. **Singleton destruction**: Java does not guarantee finalization order. Avoid depending on Singleton cleanup in
   `finalize()`.

## Interview-specific Insights

Interviewers often focus on:

- Different implementation approaches and their trade-offs
- Thread-safety guarantees of each approach
- Why double-checked locking requires `volatile`
- How enum-based Singleton prevents reflection and serialization attacks
- When NOT to use Singleton (testability concerns, hidden dependencies)
- Difference between Singleton pattern and singleton scope in Spring

Common tricky questions:

- "Can you break a Singleton? How would you prevent it?"
- "Why is the Holder idiom thread-safe without explicit synchronization?"
- "What happens if you serialize and deserialize a Singleton?"
- "How many Singleton instances can exist in a JVM?"

## Interview Q&A Section

**Q1: What is the Singleton pattern and when should you use it?**

```text
A1: The Singleton pattern ensures a class has exactly one instance and provides a global access point to it.

Use it when:
1. Exactly one instance of a class is needed to coordinate actions across the system (e.g., a configuration manager,
   logging service, or connection pool).
2. The single instance should be extensible by subclassing, and clients should be able to use an extended instance
   without modifying their code.
3. Controlled access to a shared resource is required (e.g., a file system or printer spooler).

Avoid it when:
1. The Singleton holds mutable state that makes testing difficult.
2. You can use dependency injection to manage the lifecycle instead.
3. Multiple instances might be needed in the future (e.g., multi-tenant systems).
```

```java
// Basic eager Singleton
public final class ConfigManager {
    private static final ConfigManager INSTANCE = new ConfigManager();
    private final Map<String, String> properties = new HashMap<>();

    private ConfigManager() {
        // Load configuration
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }
}
```

**Q2: Explain the double-checked locking pattern. Why is `volatile` required?**

```text
A2: Double-checked locking is a technique that reduces the overhead of acquiring a lock by first testing the
locking criterion without holding the lock. Only when the criterion is met does it proceed to acquire the lock and
check again.

The volatile keyword is required because of the Java Memory Model (JMM):

1. Without volatile, the JVM is allowed to reorder instructions. When "instance = new Singleton()" executes, the JVM
   may: (a) allocate memory, (b) assign the reference to instance, (c) call the constructor. If steps (b) and (c)
   are reordered, another thread can see a non-null but partially constructed instance.

2. Volatile prevents this reordering by establishing a happens-before relationship. Once a volatile write completes,
   all preceding writes are visible to other threads that subsequently read the volatile variable.

3. Without volatile, the outer null check might see a stale cached value, or worse, a partially constructed object.
```

```java
public final class LazyRegistry {
    private static volatile LazyRegistry instance; // volatile is critical

    private LazyRegistry() { }

    public static LazyRegistry getInstance() {
        if (instance == null) {                    // First check (no lock)
            synchronized (LazyRegistry.class) {
                if (instance == null) {            // Second check (with lock)
                    instance = new LazyRegistry();
                }
            }
        }
        return instance;
    }
}
```

**Q3: Why is the enum-based Singleton considered the best approach in Java?**

```text
A3: Joshua Bloch recommends enum-based Singleton in "Effective Java" because it provides three guarantees automatically:

1. Thread safety: The JVM guarantees that enum values are instantiated exactly once, in a thread-safe manner, during
   class loading. No synchronization code is needed.

2. Serialization safety: Java's serialization mechanism handles enums specially. It serializes only the enum constant's
   name and deserializes by calling Enum.valueOf(), always returning the same instance. No readResolve() needed.

3. Reflection safety: The JVM prevents reflective instantiation of enum types. Calling
   Constructor.newInstance() on an enum throws IllegalArgumentException.

Limitations:
- Cannot extend another class (enums implicitly extend java.lang.Enum).
- Eager initialization only (the instance is created when the enum class is loaded).
- Looks unusual to developers unfamiliar with the idiom.
```

```java
public enum DatabaseConnectionPool {
    INSTANCE;

    private final List<String> connections = new ArrayList<>();

    public void addConnection(String conn) {
        connections.add(conn);
    }

    public List<String> getConnections() {
        return Collections.unmodifiableList(connections);
    }
}

// Usage:
// DatabaseConnectionPool.INSTANCE.addConnection("jdbc:mysql://localhost/db");
```

**Q4: What is the Bill Pugh Singleton (Holder idiom) and why is it preferred over other approaches?**

```text
A4: The Bill Pugh Singleton, also known as the Initialization-on-Demand Holder idiom, uses a static inner class to
hold the Singleton instance. It leverages the JVM's class loading mechanism for thread safety.

How it works:
1. The inner holder class is NOT loaded when the outer class is loaded.
2. The holder class is loaded only when getInstance() is called for the first time.
3. Class loading in the JVM is guaranteed to be thread-safe (the JLS specifies this).
4. Therefore, the instance is created lazily and safely without any explicit synchronization.

Advantages over other approaches:
- Lazy initialization (unlike eager initialization).
- No synchronization overhead (unlike synchronized method).
- No volatile keyword needed (unlike double-checked locking).
- Simpler to implement correctly than double-checked locking.

The only limitation compared to enum Singleton is that it does not automatically handle serialization or reflection
attacks.
```

```java
public final class ServiceRegistry {
    private ServiceRegistry() {
        // Prevent reflection attacks
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("Singleton already initialized");
        }
    }

    private static class Holder {
        private static final ServiceRegistry INSTANCE = new ServiceRegistry();
    }

    public static ServiceRegistry getInstance() {
        return Holder.INSTANCE;
    }
}
```

**Q5: How can Singleton be broken and how do you prevent it?**

```text
A5: A non-enum Singleton can be broken in three ways:

1. Reflection: Using setAccessible(true) on the private constructor allows creating new instances.
   Prevention: Check in the constructor if an instance already exists and throw an exception.

2. Serialization: Deserializing a serialized Singleton creates a new object.
   Prevention: Implement readResolve() to return the existing instance.

3. Cloning: If the Singleton extends a class that implements Cloneable, clone() creates a new instance.
   Prevention: Override clone() to throw CloneNotSupportedException or return the existing instance.

4. Multiple classloaders: Different classloaders load separate copies of the class, each with its own static field.
   Prevention: Use a shared classloader or dependency injection framework.

The enum-based Singleton prevents attacks 1, 2, and 3 automatically. Only the classloader issue remains, and it is
rare in practice.
```

```java
public final class SecureSingleton implements Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final SecureSingleton INSTANCE = new SecureSingleton();
    private static boolean instantiated = false;

    private SecureSingleton() {
        // Prevent reflection attack
        if (instantiated) {
            throw new IllegalStateException("Use getInstance()");
        }
        instantiated = true;
    }

    public static SecureSingleton getInstance() {
        return INSTANCE;
    }

    // Prevent serialization attack
    @java.io.Serial
    private Object readResolve() {
        return INSTANCE;
    }

    // Prevent cloning attack
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Singleton cannot be cloned");
    }
}
```

**Q6: What is the difference between the Singleton pattern and singleton scope in Spring?**

```text
A6: These are fundamentally different concepts:

Singleton Pattern (GoF):
- One instance per classloader.
- The class itself controls its own instantiation.
- Uses static methods / fields.
- Difficult to test (hidden global state).
- The class is responsible for thread safety.

Spring Singleton Scope (default):
- One instance per Spring IoC container (ApplicationContext).
- The framework controls instantiation and lifecycle.
- Uses dependency injection (no static getInstance()).
- Easy to test (inject mocks via constructor injection).
- Spring manages thread safety of the bean lifecycle (though not of the bean's internal state).

Key differences:
1. Spring singleton scope does NOT use the Singleton pattern internally -- it simply caches a single instance in
   the container's bean registry.
2. Multiple ApplicationContexts can each have their own "singleton" bean instance.
3. Spring singletons are more testable because dependencies are injected rather than looked up globally.
4. You can change a Spring bean's scope from singleton to prototype without changing the bean class.
```

```java
// Spring-style singleton (preferred in modern Java)
// @Service  // This is a Spring singleton by default
public class UserService {
    private final UserRepository repository;

    // Constructor injection -- no static getInstance()
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public String findUser(String id) {
        return repository.findById(id);
    }
}

// Compared to GoF Singleton (less testable)
public final class UserServiceSingleton {
    private static final UserServiceSingleton INSTANCE = new UserServiceSingleton();

    private UserServiceSingleton() { }

    public static UserServiceSingleton getInstance() {
        return INSTANCE;
    }

    public String findUser(String id) {
        // Hard-coded dependency -- difficult to mock in tests
        return "user-" + id;
    }
}
```

## Code Examples

- Source: [SingletonPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/SingletonPattern.java)
- Test: [SingletonPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/SingletonPatternTest.java)
