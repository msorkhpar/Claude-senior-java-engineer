# 8.1.3. Liskov Substitution Principle (LSP)

## Concept Explanation

The Liskov Substitution Principle states that **objects of a superclass should be replaceable with objects of a subclass without altering the correctness of the program**. If `S` is a subtype of `T`, then objects of type `T` can be replaced with objects of type `S` without breaking any desirable property of the program.

**Real-world analogy**: Think of a rental car company. You reserve a "sedan." When you arrive, they give you a Toyota Camry, a Honda Accord, or a Hyundai Sonata. Each is a different subtype of "sedan," but you can drive any of them the same way -- same steering, pedals, and gear shift. If they gave you a vehicle where the brake pedal accelerated, that would violate LSP, even though it's technically still a "vehicle."

### 8.1.3.1. Definition and Purpose of LSP

Barbara Liskov formalized this in 1987. The formal definition includes:
- **Preconditions cannot be strengthened**: A subtype cannot require more than the base type
- **Postconditions cannot be weakened**: A subtype must deliver at least what the base type promises
- **Invariants must be preserved**: Properties guaranteed by the base type must hold in the subtype
- **History constraint**: Subtypes cannot allow state changes that the base type would not allow

### 8.1.3.2. Applying LSP in Concurrent Programming

In concurrent systems, LSP applies to thread-safe data structures:
- If `TaskQueue` promises that `offer()` never blocks and `poll()` returns `null` when empty, all subtypes must honor these contracts
- A `BoundedTaskQueue` may return `false` from `offer()` when full (which is allowed -- the contract says `offer` returns a boolean)
- An `UnboundedTaskQueue` always returns `true` from `offer()` -- also valid

### 8.1.3.3. Subtype Behavioral Consistency

The key test: write code that uses the base type, then substitute any subtype. If the code breaks or behaves unexpectedly, LSP is violated.

### 8.1.3.4. LSP and Thread Safety in Inheritance Hierarchies

When a base class promises thread safety, all subtypes must maintain that guarantee. A common violation: a subclass that overrides a synchronized method without proper synchronization.

## Key Points to Remember

- LSP is about **behavioral subtyping**, not just structural (implements the interface)
- The classic violation is `Square extends Rectangle` -- setting width independently of height breaks the rectangle contract
- LSP violations often manifest as `instanceof` checks or type-casting in client code
- In Java, `sealed` classes and `record` types help prevent LSP violations by making types immutable and exhaustive
- LSP is tested by substituting subtypes in existing code and verifying nothing breaks
- Composition (has-a) is often safer than inheritance (is-a) for LSP compliance

## Relevant Java 21 Features

- **Sealed interfaces**: Define bounded hierarchies where each permitted type has a clear, distinct contract
- **Records**: Immutable by default, preventing the mutable-state LSP violations common with inheritance
- **Pattern matching for switch**: Enables handling each subtype explicitly, making behavioral differences visible
- **Virtual threads**: LSP applies to thread-safe interfaces -- virtual thread implementations must honor the same contracts as platform thread implementations

## Common Pitfalls and How to Avoid Them

1. **Square extends Rectangle**:
   ```java
   class Rectangle {
       void setWidth(int w) { this.width = w; }
       void setHeight(int h) { this.height = h; }
   }
   class Square extends Rectangle {
       void setWidth(int w) { this.width = w; this.height = w; } // Violates postcondition!
   }
   ```
   **Fix**: Use a sealed interface with distinct `Rectangle` and `Square` record types.

2. **Subclass throwing UnsupportedOperationException**:
   ```java
   class ReadOnlyList extends ArrayList {
       @Override public boolean add(Object o) {
           throw new UnsupportedOperationException(); // Violates LSP
       }
   }
   ```
   **Fix**: Don't extend `ArrayList`; implement a read-only interface instead.

3. **Strengthening preconditions**:
   ```java
   class Base { void process(int n) { /* works for any int */ } }
   class Sub extends Base {
       void process(int n) {
           if (n < 0) throw new IllegalArgumentException(); // Strengthened precondition!
       }
   }
   ```
   **Fix**: Subtypes should accept at least the same range of inputs as the base type.

## Best Practices and Optimization Techniques

1. Prefer composition over inheritance -- it avoids most LSP issues
2. Use `sealed` interfaces with `record` implementations for type hierarchies
3. Write contract tests: test suites that run against the interface and can be parameterized with any implementation
4. Avoid mutable state in type hierarchies -- immutable objects trivially satisfy LSP
5. Document contracts explicitly: preconditions, postconditions, and invariants
6. Use the "can I substitute this?" mental test before creating inheritance relationships

## Edge Cases and Their Handling

1. **Null inputs**: Both bounded and unbounded queues should reject null items consistently
2. **Empty collections**: `totalArea` of an empty shape list should return 0, `largestByArea` should throw
3. **Zero-value shapes**: A shape with zero dimensions is valid and has zero area -- all subtypes must handle this
4. **Boundary capacity**: A bounded queue at capacity returns `false` from `offer()`; an unbounded queue always returns `true` -- both honor the `boolean offer(T)` contract

## Interview-specific Insights

Interviewers often focus on:
- The Rectangle/Square problem -- be ready to explain why it violates LSP
- Asking you to identify LSP violations in code
- Understanding the formal properties (preconditions, postconditions, invariants)
- Relating LSP to real-world APIs (Collections framework, I/O streams)

Common tricky questions:
- "Does `ArrayList` implement `List` in an LSP-compliant way?" (Yes, because the `List` interface doesn't promise constant-time access -- that's an implementation detail)
- "Is `Collections.unmodifiableList()` an LSP violation?" (Controversial -- it implements `List` but throws on mutation. The Java community generally considers it acceptable because `List.add()` is documented as optional)
- "How does LSP relate to the Liskov substitution in the context of covariant return types?"

## Interview Q&A Section

**Q1: What is the Liskov Substitution Principle and why does Square-extends-Rectangle violate it?**

```text
A1: The Liskov Substitution Principle states that objects of a base type should be 
replaceable with objects of any subtype without breaking program correctness.

The Square-extends-Rectangle violation:
- Rectangle's contract: setWidth changes only width, setHeight changes only height
- Square's override: setWidth also changes height (and vice versa)
- Client code that sets width=5 and height=3 expects area=15
- With a Square substituted, area becomes 9 (both dimensions set to 3)

This breaks the postcondition of setWidth: "after setWidth(w), getWidth() == w 
AND getHeight() is unchanged."

The fix is to use a common Shape interface with separate Rectangle and Square 
implementations, rather than having Square extend Rectangle.
```

```java
// LSP violation
class Rectangle {
    protected int width, height;
    void setWidth(int w) { this.width = w; }
    void setHeight(int h) { this.height = h; }
    int area() { return width * height; }
}

class Square extends Rectangle {
    @Override void setWidth(int w) { this.width = w; this.height = w; }
    @Override void setHeight(int h) { this.width = h; this.height = h; }
}

// Client code breaks with Square
void resize(Rectangle r) {
    r.setWidth(5);
    r.setHeight(3);
    assert r.area() == 15; // FAILS with Square! area = 9
}

// LSP-compliant solution
sealed interface Shape permits LspRectangle, LspSquare {
    double area();
}
record LspRectangle(double width, double height) implements Shape {
    public double area() { return width * height; }
}
record LspSquare(double side) implements Shape {
    public double area() { return side * side; }
}
```

**Q2: How does LSP apply to concurrent data structures?**

```text
A2: LSP is critical for concurrent data structures because client code must be 
able to swap implementations without changing synchronization assumptions.

For example, a TaskQueue interface with these contracts:
- offer(item): adds item, returns true if successful, false if full
- poll(): returns and removes head, or null if empty
- size(): returns current count

Both BoundedTaskQueue and UnboundedTaskQueue must honor these contracts:
- Bounded: offer returns false when at capacity (valid per contract)
- Unbounded: offer always returns true (also valid -- exceeds the minimum guarantee)
- Both: poll returns null when empty
- Both: size returns accurate count

A consumer that processes items from any TaskQueue will work correctly with either 
implementation, which is the essence of LSP.
```

```java
interface TaskQueue<T> {
    boolean offer(T item);  // Returns false if can't accept
    T poll();               // Returns null if empty
    int size();
}

class BoundedTaskQueue<T> implements TaskQueue<T> {
    private final ArrayBlockingQueue<T> queue;
    BoundedTaskQueue(int cap) { queue = new ArrayBlockingQueue<>(cap); }
    public boolean offer(T item) { return queue.offer(item); } // false when full
    public T poll() { return queue.poll(); }
    public int size() { return queue.size(); }
}

class UnboundedTaskQueue<T> implements TaskQueue<T> {
    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    public boolean offer(T item) { queue.offer(item); return true; } // always true
    public T poll() { return queue.poll(); }
    public int size() { return queue.size(); }
}

// Client works with any TaskQueue -- LSP in action
class TaskConsumer<T> {
    void drainAll(TaskQueue<T> queue) {
        T item;
        while ((item = queue.poll()) != null) { process(item); }
    }
}
```

**Q3: What are the formal properties that LSP requires?**

```text
A3: LSP requires four formal properties:

1. Precondition rule: A subtype cannot STRENGTHEN preconditions
   - If base accepts any integer, subtype cannot reject negatives
   - Subtype can WEAKEN preconditions (accept more inputs)

2. Postcondition rule: A subtype cannot WEAKEN postconditions
   - If base guarantees sorted output, subtype must also guarantee it
   - Subtype can STRENGTHEN postconditions (guarantee more)

3. Invariant rule: All invariants of the base type must hold in the subtype
   - If base guarantees size >= 0, subtype must also maintain this

4. History constraint: Subtypes cannot introduce state changes that the base 
   type would not allow
   - If base is immutable, subtype cannot add mutating methods

These rules ensure that any code written against the base type's contract 
continues to work correctly when a subtype is substituted.
```

```java
// Demonstrating the four properties
interface SortedCollection<T extends Comparable<T>> {
    void add(T item);           // Precondition: item != null
    List<T> getAll();           // Postcondition: returned list is sorted
    int size();                 // Invariant: size() >= 0
}

// Correct subtype: honors all properties
class SortedArrayList<T extends Comparable<T>> implements SortedCollection<T> {
    private final List<T> items = new ArrayList<>();

    @Override
    public void add(T item) {
        Objects.requireNonNull(item);    // Same precondition (not strengthened)
        items.add(item);
        items.sort(Comparator.naturalOrder());
    }

    @Override
    public List<T> getAll() {
        return Collections.unmodifiableList(items); // Postcondition: sorted
    }

    @Override
    public int size() {
        return items.size(); // Invariant: always >= 0
    }
}
```

**Q4: How do you detect LSP violations in existing code?**

```text
A4: LSP violations manifest through these code smells:

1. instanceof checks in client code:
   if (shape instanceof Square) { /* special handling */ }
   This means the subtype cannot be treated uniformly.

2. Empty or throwing method overrides:
   @Override public void add(E e) { throw new UnsupportedOperationException(); }
   The subtype refuses to fulfill the base type's contract.

3. Defensive null checks after calling subtype methods:
   Result r = processor.process(input);
   if (r == null) { /* shouldn't happen per contract, but... */ }

4. Subclass methods that silently change behavior:
   Base.setX() only sets X, but Sub.setX() also sets Y.

5. Tests that need different assertions for different subtypes:
   If your test says "if BoundedQueue, expect X; if UnboundedQueue, expect Y" 
   for the SAME operation, there's likely an LSP issue.

Prevention:
- Write "contract tests" that run against the interface
- Use abstract test classes parameterized with different implementations
- Prefer composition over inheritance
- Use sealed interfaces with records for type safety
```

```java
// LSP violation detection: instanceof smell
void processShape(Shape shape) {
    if (shape instanceof Square s) {
        // Special handling for Square -- LSP violation indicator!
        System.out.println("Square side: " + s.side());
    } else if (shape instanceof Rectangle r) {
        System.out.println("Rectangle: " + r.width() + "x" + r.height());
    }
}

// LSP-compliant: uniform handling through the interface
void processShape(Shape shape) {
    System.out.println(shape.description()); // Each shape describes itself
    System.out.println("Area: " + shape.area());
    System.out.println("Perimeter: " + shape.perimeter());
}
```

**Q5: How does LSP relate to the `Collections.unmodifiableList()` in Java?**

```text
A5: This is a well-known LSP gray area in Java:

Collections.unmodifiableList() returns a List that throws 
UnsupportedOperationException for add(), remove(), set(), etc.

Arguments that it VIOLATES LSP:
- The List interface defines add(), remove(), etc.
- Client code using List expects these operations to work
- The unmodifiable wrapper breaks this expectation

Arguments that it does NOT violate LSP:
- The List interface documents add() as an "optional operation"
- The Javadoc explicitly states that implementations may throw 
  UnsupportedOperationException
- The contract includes the possibility of failure

Java's approach is pragmatic:
- The "optional operations" pattern is documented in the Collection interface
- Real-world code handles this via convention (defensive programming)
- Newer Java avoids this with List.of() and List.copyOf(), which return 
  truly unmodifiable implementations that are clearly documented as such

Best practice: Use List.of() / List.copyOf() for unmodifiable lists, and 
reserve List for mutable contexts. This avoids the LSP ambiguity entirely.
```

```java
// The LSP gray area
List<String> mutable = new ArrayList<>(List.of("a", "b", "c"));
List<String> unmodifiable = Collections.unmodifiableList(mutable);

// This works
String first = unmodifiable.get(0); // OK

// This throws -- potential LSP violation
try {
    unmodifiable.add("d"); // UnsupportedOperationException
} catch (UnsupportedOperationException e) {
    // Expected if you know it's unmodifiable, but surprising if you only see List<String>
}

// Better approach in modern Java -- clear intent, no ambiguity
List<String> immutable = List.of("a", "b", "c");
// immutable.add("d"); // Also throws, but List.of() is documented as unmodifiable

// Best practice: use specific return types when possible
public List<String> getItems() {
    return List.copyOf(internalList); // Caller knows this is an unmodifiable snapshot
}
```

## Code Examples

- Source: [LiskovSubstitution.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/LiskovSubstitution.java)
- Test: [LiskovSubstitutionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/LiskovSubstitutionTest.java)
