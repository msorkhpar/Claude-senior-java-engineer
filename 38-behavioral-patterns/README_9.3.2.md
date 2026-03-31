# 9.3.2. Observer Pattern

## Concept Explanation

The Observer pattern is a behavioral design pattern that defines a one-to-many dependency between objects so that when one object (the **subject** or **observable**) changes state, all its dependents (the **observers**) are notified and updated automatically.

**Real-world analogy**: Think of a newspaper subscription. The newspaper publisher (subject) maintains a list of subscribers (observers). Whenever a new edition is published, all subscribers receive a copy. Subscribers can start or cancel their subscription at any time. The publisher does not need to know who the subscribers are -- it just delivers to everyone on the list.

The Observer pattern consists of these participants:
- **Subject (Observable)**: the object being observed. It maintains a list of observers and provides methods to add, remove, and notify them.
- **Observer**: the interface or abstract type that defines the `update()` method called when the subject's state changes.
- **ConcreteSubject**: a specific subject that stores state and notifies observers when it changes.
- **ConcreteObserver**: a specific observer that reacts to notifications from the subject.

### When to Use the Observer Pattern

1. When changes in one object require updating other objects, and you don't know in advance how many objects need updating.
2. When an object should be able to notify other objects without making assumptions about who those objects are (loose coupling).
3. When you need an event-driven architecture where components react to events.
4. When implementing distributed event handling systems, MVC architectures, or reactive programming models.

## Key Points to Remember

1. The Observer pattern promotes **loose coupling** -- the subject knows only that observers implement a common interface.
2. Observers can be **added and removed at runtime** without modifying the subject.
3. The subject **pushes** notifications to observers (push model) or observers **pull** the data they need (pull model).
4. **Notification order is generally undefined** -- do not depend on a specific order of observer notification.
5. Java's legacy `java.util.Observable` class was **deprecated in Java 9** due to design limitations (not an interface, requires extending a class).
6. Modern Java favors **functional approaches** using `Consumer<T>` or custom functional interfaces for observers.
7. In concurrent environments, the observer list must be **thread-safe** (use `CopyOnWriteArrayList` or synchronized access).

## Relevant Java 21 Features

- **Functional interfaces (Java 8+)**: Observers can be expressed as `Consumer<T>`, `BiConsumer<String, T>`, or custom `@FunctionalInterface` types, enabling lambda-based subscriptions.
- **CopyOnWriteArrayList (Java 5+, enhanced)**: Thread-safe collection ideal for observer lists where reads (notifications) vastly outnumber writes (subscribe/unsubscribe).
- **ConcurrentHashMap (Java 8+)**: For event bus implementations where listeners are grouped by event type.
- **Records (Java 16+)**: Event data can be modeled as immutable records for type safety and clarity.
- **Virtual threads (Java 21)**: Notifications can be dispatched asynchronously using virtual threads for high-throughput event systems.

### Evolution Across Java Versions

| Version | Impact on Observer Pattern |
|---------|----------------------------|
| Java 1.0 | `java.util.Observable` and `java.util.Observer` provided built-in support (now deprecated) |
| Java 5 | `CopyOnWriteArrayList` enabled thread-safe observer collections |
| Java 8 | Lambdas made lightweight observer subscriptions practical |
| Java 9 | `java.util.Observable` deprecated; custom implementations preferred |
| Java 16+ | Records for immutable event data |
| Java 21 | Virtual threads for asynchronous notification dispatch |

## Common Pitfalls and How to Avoid Them

1. **Memory leaks from forgotten observers**: If observers are added but never removed, the subject holds strong references that prevent garbage collection.
   ```java
   // Problem: observer is never removed
   subject.addObserver(new HeavyObserver()); // reference held forever
   
   // Fix: always provide a way to unsubscribe, and unsubscribe when done
   Observer obs = new HeavyObserver();
   subject.addObserver(obs);
   // ... later ...
   subject.removeObserver(obs);
   ```

2. **ConcurrentModificationException during notification**: If an observer adds or removes observers during notification, iterating the list fails.
   ```java
   // Problem: modifying observer list during iteration
   for (Observer obs : observers) {
       obs.update(event, data); // if obs calls removeObserver(), boom!
   }
   
   // Fix: iterate over a snapshot copy
   List<Observer> snapshot = new ArrayList<>(observers);
   for (Observer obs : snapshot) {
       obs.update(event, data);
   }
   
   // Or use CopyOnWriteArrayList
   private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();
   ```

3. **Unexpected notification order**: Relying on a specific notification order is fragile. Different implementations may iterate in different orders.

4. **Cascading updates**: Observer A's update triggers a state change in the subject, which notifies Observer B, which triggers another change, creating an infinite loop.
   ```java
   // Fix: use a flag to prevent re-entrant notification
   private boolean notifying = false;
   
   public void notifyObservers() {
       if (notifying) return;
       notifying = true;
       try {
           // ... notify ...
       } finally {
           notifying = false;
       }
   }
   ```

5. **Thread safety issues**: In multi-threaded environments, the observer list and notification logic must be synchronized to prevent race conditions.

## Best Practices and Optimization Techniques

1. **Use `CopyOnWriteArrayList` for thread-safe observer lists**: It is optimized for cases where reads (iterations/notifications) vastly outnumber writes (add/remove observer).

2. **Prevent duplicate registrations**: Check if an observer is already registered before adding it.
   ```java
   public void addObserver(Observer obs) {
       if (!observers.contains(obs)) {
           observers.add(obs);
       }
   }
   ```

3. **Return unmodifiable views of internal state**: Published data and observer lists exposed to clients should be wrapped with `Collections.unmodifiableList()`.

4. **Support event filtering**: Allow observers to subscribe to specific event types rather than receiving all events, reducing unnecessary processing.

5. **Consider weak references for observers**: To prevent memory leaks, use `WeakReference<Observer>` when the subject's lifetime exceeds the observers'.

6. **Use an event bus for complex systems**: Instead of each subject managing its own observer list, use a centralized event bus that decouples publishers from subscribers.

7. **Document the threading model**: Clearly state whether notifications happen on the calling thread or an event thread, so observers know whether they need synchronization.

## Edge Cases and Their Handling

1. **Null observers**: Always validate observer references. Throw `IllegalArgumentException` for null.
2. **Empty observer list**: Notifications to an empty list should be a no-op, not an error.
3. **Removing a non-existent observer**: `removeObserver()` should handle this gracefully (no exception).
4. **Publishing with null or blank data**: Validate event data before notifying observers.
5. **Observer throws exception**: If one observer throws during notification, it should not prevent other observers from being notified. Consider wrapping individual calls in try-catch.
6. **Re-entrant notifications**: An observer's update method may trigger another notification. Guard against infinite loops.

## Interview-specific Insights

Interviewers often focus on:
- The difference between Observer and Pub/Sub patterns (Observer is tightly coupled via direct references; Pub/Sub uses a message broker)
- Why `java.util.Observable` was deprecated and what to use instead
- Thread safety concerns in concurrent observer implementations
- Memory leak risks and how to mitigate them
- How the Observer pattern relates to reactive programming (RxJava, Project Reactor)
- Real-world examples: GUI event listeners, Spring ApplicationEvent, JavaFX bindings

Common tricky questions:
- "How do you prevent memory leaks with the Observer pattern?" (Weak references, explicit unsubscribe)
- "What happens if an observer throws an exception during notification?" (Other observers may not get notified unless you handle it)
- "How is Observer different from Mediator?" (Observer: one-to-many broadcast; Mediator: many-to-many coordination through a central hub)

## Interview Q&A Section

**Q1: Explain the Observer pattern and give a real-world example.**

```text
A1: The Observer pattern establishes a one-to-many relationship between a subject and
its observers. When the subject's state changes, all registered observers are notified
automatically.

Participants:
- Subject: maintains a list of observers, provides add/remove/notify methods
- Observer: defines an update method that is called when the subject changes
- ConcreteSubject: stores relevant state and notifies observers on change
- ConcreteObserver: implements the update method to react to changes

Real-world examples:
1. GUI event listeners: A button (subject) notifies click listeners (observers)
2. Stock price alerts: A stock ticker (subject) notifies trading bots (observers)
3. Social media: When a user (subject) posts, all followers (observers) see it
4. MVC architecture: The model (subject) notifies views (observers) of data changes

The key benefit is loose coupling: the subject does not know the concrete types of
its observers. It only knows they implement the Observer interface. This makes it
easy to add new observers without modifying the subject.
```

```java
// Subject interface
interface Subject<T> {
    void addObserver(Observer<T> observer);
    void removeObserver(Observer<T> observer);
    void notifyObservers(String event, T data);
}

// Observer interface
interface Observer<T> {
    void update(String event, T data);
}

// Concrete subject
class NewsAgency implements Subject<String> {
    private final List<Observer<String>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<String> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<String> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String event, String data) {
        for (Observer<String> obs : new ArrayList<>(observers)) {
            obs.update(event, data);
        }
    }

    public void publishArticle(String article) {
        notifyObservers("NEW_ARTICLE", article);
    }
}
```

**Q2: Why was `java.util.Observable` deprecated in Java 9, and what should you use instead?**

```text
A2: java.util.Observable was deprecated for several design flaws:

1. It's a CLASS, not an interface: Since Java only supports single inheritance,
   any class extending Observable cannot extend another class. This severely
   limits its usefulness.

2. Not type-safe: The update method passes Object, requiring casting:
   void update(Observable o, Object arg) -- no generics support.

3. Thread-safety issues: While some methods were synchronized, the notification
   mechanism had race conditions that were difficult to fix.

4. Weak API design: No way to distinguish event types, no filtering,
   limited composability.

5. Serialization problems: Observable implements Serializable but its
   internal observer list cannot be serialized properly.

What to use instead:
1. Custom Observer interface with generics (recommended):
   - Define your own Subject<T> and Observer<T> interfaces
   - Use CopyOnWriteArrayList for thread safety

2. java.beans.PropertyChangeSupport:
   - Built into the JDK, supports property change events
   - Used by JavaBeans convention

3. Functional approach:
   - Use Consumer<T> or custom @FunctionalInterface as observers
   - Enables lambda subscriptions

4. Reactive libraries:
   - RxJava, Project Reactor for complex event streams
   - Spring ApplicationEvent for Spring-based apps

5. Java Flow API (Java 9+):
   - java.util.concurrent.Flow provides Publisher/Subscriber interfaces
   - Standard reactive streams support in the JDK
```

```java
// Modern approach: custom generic Observer
interface Observer<T> {
    void update(String event, T data);
}

interface Subject<T> {
    void addObserver(Observer<T> observer);
    void removeObserver(Observer<T> observer);
    void notifyObservers(String event, T data);
}

// Functional approach: using Consumer
class EventBus {
    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    public void publish(String eventType, Object data) {
        var list = listeners.get(eventType);
        if (list != null) {
            list.forEach(l -> l.accept(data));
        }
    }
}

// Usage with lambdas
EventBus bus = new EventBus();
bus.subscribe("user.created", data -> System.out.println("New user: " + data));
bus.publish("user.created", "Alice");
```

**Q3: How do you make the Observer pattern thread-safe?**

```text
A3: Thread safety in the Observer pattern involves protecting three operations:
adding observers, removing observers, and notifying observers.

Approach 1: CopyOnWriteArrayList (recommended for most cases)
- Creates a copy of the internal array on every write (add/remove)
- Iterations (notifications) use a snapshot, so they never see modifications
- Best when notifications greatly outnumber add/remove operations
- No explicit synchronization needed

Approach 2: Synchronized blocks
- Use synchronized(observers) around all list operations
- Simple but can cause contention under heavy load
- Risk of deadlock if observers call back into the subject

Approach 3: ReadWriteLock
- Allow concurrent reads (notifications) but exclusive writes (add/remove)
- More complex but better throughput than full synchronization

Approach 4: ConcurrentHashMap for event bus
- Use computeIfAbsent() for atomic listener registration
- Combined with CopyOnWriteArrayList for per-event-type lists

Key considerations:
- Avoid holding locks during notification (observers might call back)
- Consider asynchronous notification using a thread pool or virtual threads
- Document whether observers are called on the publisher's thread or a separate one
```

```java
// Thread-safe subject using CopyOnWriteArrayList
class ThreadSafeSubject<T> implements Subject<T> {
    private final CopyOnWriteArrayList<Observer<T>> observers =
        new CopyOnWriteArrayList<>();

    @Override
    public void addObserver(Observer<T> observer) {
        observers.addIfAbsent(observer); // prevents duplicates atomically
    }

    @Override
    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String event, T data) {
        // Safe: iteration uses a snapshot of the array
        for (Observer<T> observer : observers) {
            observer.update(event, data);
        }
    }
}

// Asynchronous notification using virtual threads (Java 21)
class AsyncSubject<T> implements Subject<T> {
    private final CopyOnWriteArrayList<Observer<T>> observers =
        new CopyOnWriteArrayList<>();

    @Override
    public void notifyObservers(String event, T data) {
        for (Observer<T> observer : observers) {
            Thread.startVirtualThread(() -> observer.update(event, data));
        }
    }
    // ... addObserver, removeObserver ...
}
```

**Q4: How do you prevent memory leaks with the Observer pattern?**

```text
A4: Memory leaks occur when observers are registered but never unregistered.
The subject holds a strong reference to each observer, preventing garbage collection
even when the observer is no longer needed.

Prevention strategies:

1. Explicit unsubscription:
   - Always pair addObserver() with removeObserver()
   - Use try-finally or try-with-resources to ensure cleanup
   - Return a "subscription" handle that can be cancelled

2. Weak references:
   - Store observers as WeakReference<Observer>
   - The GC can collect observers when no other strong reference exists
   - Must clean up stale references during notification

3. Lifecycle-aware observers:
   - In frameworks (Android, Spring), tie observer lifetime to component lifecycle
   - Automatically unsubscribe when the component is destroyed

4. Scoped subscriptions:
   - Use a subscription scope that auto-unsubscribes all observers when closed
   - Similar to coroutine scopes in Kotlin

5. Event bus with weak listeners:
   - Some event bus implementations use weak references by default
   - Guava's EventBus can be configured for weak listeners
```

```java
// Strategy 1: Return a subscription handle for easy cleanup
interface Subscription {
    void cancel();
}

class SafeSubject<T> {
    private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();

    public Subscription subscribe(Observer<T> observer) {
        observers.add(observer);
        return () -> observers.remove(observer); // cancellation lambda
    }

    public void notifyAll(String event, T data) {
        observers.forEach(obs -> obs.update(event, data));
    }
}

// Usage
SafeSubject<String> subject = new SafeSubject<>();
Subscription sub = subject.subscribe((event, data) ->
    System.out.println("Got: " + data));

// When done:
sub.cancel(); // observer is removed, no memory leak

// Strategy 2: Weak reference observer list
class WeakSubject<T> {
    private final List<WeakReference<Observer<T>>> observers = new ArrayList<>();

    public void addObserver(Observer<T> observer) {
        observers.add(new WeakReference<>(observer));
    }

    public void notifyObservers(String event, T data) {
        observers.removeIf(ref -> ref.get() == null); // clean stale refs
        for (WeakReference<Observer<T>> ref : new ArrayList<>(observers)) {
            Observer<T> obs = ref.get();
            if (obs != null) obs.update(event, data);
        }
    }
}
```

**Q5: What is the difference between Observer, Pub/Sub, and Mediator patterns?**

```text
A5: These three patterns all deal with communication between objects but differ
in coupling, topology, and intent:

Observer Pattern:
- Direct one-to-many relationship
- Subject KNOWS its observers (holds references to them)
- Synchronous notification (typically)
- Tight coupling: observers register directly with the subject
- Example: GUI button click listeners

Publish/Subscribe (Pub/Sub):
- Indirect many-to-many relationship
- Publishers and subscribers do NOT know each other
- Communication through a message broker or event channel
- Loose coupling: publisher sends to a topic, broker delivers
- Often asynchronous
- Example: Message queues (Kafka, RabbitMQ), event buses

Mediator Pattern:
- Centralized many-to-many coordination
- Components communicate THROUGH a mediator, not directly
- The mediator encapsulates interaction logic between components
- Reduces direct dependencies between components
- Example: Air traffic control (planes don't talk to each other;
  they talk to the tower)

Key differences:
- Observer: 1 subject -> N observers (direct reference)
- Pub/Sub: M publishers -> Broker -> N subscribers (no direct reference)
- Mediator: N components <-> 1 mediator (bidirectional, centralized logic)
```

```java
// Observer: subject directly notifies observers
class Button {
    private final List<ClickListener> listeners = new ArrayList<>();
    public void addClickListener(ClickListener l) { listeners.add(l); }
    public void click() { listeners.forEach(ClickListener::onClick); }
}

// Pub/Sub: publisher and subscriber don't know each other
class EventBus {
    private final Map<String, List<Consumer<Object>>> topics = new ConcurrentHashMap<>();

    public void subscribe(String topic, Consumer<Object> subscriber) {
        topics.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    public void publish(String topic, Object message) {
        var subs = topics.get(topic);
        if (subs != null) subs.forEach(s -> s.accept(message));
    }
}

// Mediator: components talk through a central coordinator
interface ChatMediator {
    void sendMessage(String message, User sender);
}

class ChatRoom implements ChatMediator {
    private final List<User> users = new ArrayList<>();

    public void sendMessage(String message, User sender) {
        users.stream()
             .filter(u -> u != sender)
             .forEach(u -> u.receive(message));
    }
}
```

**Q6: How do you implement an event bus with support for multiple event types?**

```text
A6: An event bus is a centralized event dispatching system that decouples event
publishers from subscribers. It extends the Observer pattern by:

1. Supporting multiple event types (topics/channels)
2. Allowing subscribers to register for specific events only
3. Decoupling publishers from subscribers entirely

Implementation approach:
- Use a Map<String, List<Consumer>> to maintain per-event-type listener lists
- Use ConcurrentHashMap for thread-safe registration
- Use CopyOnWriteArrayList for thread-safe iteration during notification
- Provide subscribe(eventType, listener) and publish(eventType, data) methods

Advanced features:
- Event filtering: subscribers can specify predicates for fine-grained control
- Priority ordering: listeners can have priorities for notification order
- Async delivery: use a thread pool or virtual threads for non-blocking dispatch
- Dead letter handling: track events with no subscribers
- Event history: buffer recent events for late subscribers
```

```java
class EventBus {
    private final Map<String, List<Consumer<Object>>> listeners =
        new ConcurrentHashMap<>();

    public void subscribe(String eventType, Consumer<Object> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(listener);
    }

    public void unsubscribe(String eventType, Consumer<Object> listener) {
        var list = listeners.get(eventType);
        if (list != null) list.remove(listener);
    }

    public void publish(String eventType, Object data) {
        var list = listeners.get(eventType);
        if (list != null) {
            list.forEach(l -> l.accept(data));
        }
    }

    public Set<String> eventTypes() {
        return Collections.unmodifiableSet(listeners.keySet());
    }
}

// Usage
EventBus bus = new EventBus();
bus.subscribe("user.created", data -> log("New user: " + data));
bus.subscribe("user.created", data -> sendWelcomeEmail((String) data));
bus.subscribe("order.placed", data -> processOrder(data));

bus.publish("user.created", "Alice");  // both user.created listeners fire
bus.publish("order.placed", order);    // only order.placed listener fires
```

## Code Examples

- Test: [ObserverPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/ObserverPatternTest.java)
- Source: [ObserverPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/ObserverPattern.java)
