# 8.4.1. Definition and Purpose of Composition over Inheritance

## Concept Explanation

The Composition over Inheritance principle (also known as "favor composition over inheritance") is a fundamental object-oriented design guideline stating that classes should achieve polymorphic behavior and code reuse by containing instances of other classes (composition) rather than inheriting from a base or parent class.

**Real-world analogy**: Consider building a car. The inheritance approach would be like saying "a sports car IS a car IS a vehicle IS a machine" and trying to encode every variation through a class hierarchy. The composition approach says "a car HAS an engine, HAS a transmission, HAS brakes" -- each component can be independently designed, tested, and swapped. You can put a turbocharged engine into a sedan or a hybrid drivetrain into an SUV without redesigning the entire vehicle hierarchy.

In Java, inheritance establishes an "is-a" relationship (`SportsCar extends Car`), while composition establishes a "has-a" relationship (`Car` has an `Engine` field). The principle does not say "never use inheritance" -- it says that when choosing between the two, composition should be the default choice unless inheritance is clearly the better fit (e.g., when there is a genuine is-a relationship and the superclass is designed for extension).

Joshua Bloch, in *Effective Java*, dedicates Item 18 to this principle: "Favor composition over inheritance." The key insight is that inheritance violates encapsulation because a subclass depends on the implementation details of its superclass. When those details change, the subclass can break -- this is the **fragile base class problem**.

## Key Points to Remember

- Inheritance creates tight coupling between parent and child classes; composition creates loose coupling through interfaces.
- The fragile base class problem occurs when changes to a superclass inadvertently break subclasses that depend on its internal behavior.
- Composition delegates work to contained objects, while inheritance overrides behavior in subclasses.
- Use inheritance only when there is a genuine "is-a" relationship AND the superclass is designed and documented for extension.
- The **forwarding/wrapper pattern** (Effective Java Item 18) uses composition + delegation to wrap an existing class and add behavior safely.
- Composition supports runtime flexibility: you can change the composed object's behavior at runtime by swapping the delegate.
- Java's single-inheritance constraint makes composition even more valuable, since a class can compose multiple behaviors but can only extend one class.

## Relevant Java 21 Features

- **Records** (JEP 395): Records are implicitly final and cannot be extended. They naturally encourage composition since you cannot build inheritance hierarchies with them.
- **Sealed classes** (JEP 409): Sealed classes restrict which classes can extend them, providing controlled inheritance. They pair well with composition by defining a closed set of types while delegating behavior through composed objects.
- **Pattern matching for switch** (JEP 441): When using sealed types with composition, pattern matching allows clean decomposition of behavior without requiring inheritance-based polymorphism.
- **Interfaces with default methods**: Since Java 8, interfaces can have default methods, enabling a form of multiple inheritance of behavior. This reduces the need for abstract base classes and supports composition through interface delegation.
- **Functional interfaces and lambdas**: Composing behavior through `Function`, `Predicate`, `Consumer`, etc. is a lightweight form of composition that avoids class hierarchies entirely.

## Common Pitfalls and How to Avoid Them

1. **The fragile base class problem with inheritance**

   When a subclass overrides a method that the superclass calls internally, the subclass's behavior becomes dependent on implementation details:

   ```java
   // PROBLEM: InstrumentedHashSet extends HashSet
   public class InstrumentedHashSet<E> extends HashSet<E> {
       private int addCount = 0;

       @Override
       public boolean add(E e) {
           addCount++;
           return super.add(e);
       }

       @Override
       public boolean addAll(Collection<? extends E> c) {
           addCount += c.size();
           return super.addAll(c); // BUG: HashSet.addAll calls add() internally!
       }
       // addCount will be double the actual adds when using addAll
   }
   ```

   **Fix**: Use forwarding wrapper (composition):

   ```java
   public class InstrumentedSet<E> extends ForwardingSet<E> {
       private int addCount = 0;

       public InstrumentedSet(Set<E> delegate) { super(delegate); }

       @Override
       public boolean add(E e) {
           addCount++;
           return super.add(e);
       }

       @Override
       public boolean addAll(Collection<? extends E> c) {
           addCount += c.size();
           return super.addAll(c); // ForwardingSet.addAll delegates directly, no double-counting
       }
   }
   ```

2. **Over-engineering with unnecessary composition layers**

   Adding too many wrapper/decorator layers when simple inheritance is appropriate creates complexity without benefit.

   **Fix**: Use composition when you need flexibility, runtime behavior changes, or wrapping existing classes. Use inheritance for genuine "is-a" relationships with well-designed base classes.

3. **Forgetting to delegate all methods in a wrapper**

   When creating a forwarding class, missing a method means the default Object behavior is used, leading to subtle bugs.

   **Fix**: Create a complete forwarding class that delegates all interface methods, as shown in the `ForwardingSet` pattern.

4. **Breaking the Liskov Substitution Principle with inheritance**

   Subclasses that change the semantics of inherited methods violate LSP. Composition avoids this by not establishing an is-a relationship.

   **Fix**: When behavior needs to differ significantly, compose rather than inherit.

## Best Practices and Optimization Techniques

1. **Design interfaces first, then compose implementations**: Define behavior contracts through interfaces and compose implementations using delegation.
2. **Use the forwarding/wrapper pattern for extending third-party classes**: You cannot control how third-party classes evolve, so wrapping them with composition is safer than extending them.
3. **Prefer constructor injection for composed dependencies**: This makes dependencies explicit and supports immutability.
4. **Keep composed objects immutable when possible**: Immutable composed objects are inherently thread-safe and easier to reason about.
5. **Use Java records for simple value compositions**: Records provide a concise way to compose data without inheritance.
6. **Leverage functional interfaces for lightweight composition**: Instead of creating new classes, compose behavior using lambdas and method references.

## Edge Cases and Their Handling

1. **Null delegates**: Always validate that composed objects are not null in constructors using `Objects.requireNonNull()`.
2. **Circular composition**: Avoid A composing B composing A. Use interfaces to break circular dependencies.
3. **Serialization with composition**: Composed objects must be serializable if the containing class is serializable. Consider using `transient` for non-serializable components.
4. **Equality and hashCode**: When using composition, define `equals()` and `hashCode()` based on the composed state, not on identity.
5. **Deep vs. shallow copying**: Composed objects may need deep copying to avoid shared mutable state.

## Interview-specific Insights

Interviewers frequently ask about this principle because it tests understanding of OOP design trade-offs. Key areas:

- The classic `InstrumentedHashSet` example from Effective Java (Item 18) -- be ready to explain the double-counting bug and the forwarding wrapper solution.
- When inheritance IS appropriate (genuine is-a, designed-for-extension superclass like `AbstractList`).
- How composition supports the Open/Closed Principle (open for extension via new composed behaviors, closed for modification).
- The relationship between composition and the Strategy, Decorator, and Adapter patterns.
- How Java's single inheritance limitation makes composition essential for combining multiple behaviors.

## Interview Q&A Section

**Q1: What is the Composition over Inheritance principle and why does it matter?**

```text
A1: The Composition over Inheritance principle states that classes should achieve
code reuse and polymorphic behavior by containing instances of other classes
(composition/delegation) rather than inheriting from them.

It matters because:
1. Inheritance creates tight coupling -- subclasses depend on superclass
   implementation details (fragile base class problem).
2. Composition creates loose coupling -- the composed object interacts only
   through a well-defined interface.
3. Composition allows runtime flexibility -- you can swap behaviors at runtime.
4. Inheritance is constrained to single inheritance in Java; composition has
   no such limitation.
5. Composition makes code easier to test -- you can mock composed dependencies.

This does NOT mean "never use inheritance." Use inheritance when there is a
genuine is-a relationship and the superclass is designed for extension.
```

```java
// Inheritance approach (fragile)
class CountingList<E> extends ArrayList<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }
    // What about addAll? It may call add() internally -- fragile!
}

// Composition approach (robust)
class CountingList<E> implements List<E> {
    private final List<E> delegate = new ArrayList<>();
    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return delegate.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return delegate.addAll(c); // no double-counting risk
    }
    // ... delegate all other List methods
}
```

**Q2: What is the fragile base class problem? Give a concrete Java example.**

```text
A2: The fragile base class problem occurs when a subclass depends on the
internal implementation details of its superclass. If the superclass changes
how it implements a method, the subclass can break silently.

The classic example is extending HashSet to count element insertions.
HashSet.addAll() internally calls add() for each element. If a subclass
overrides both add() and addAll() to count insertions, addAll() will
double-count because the superclass's addAll() calls the overridden add().

This is fragile because:
- The subclass depends on the IMPLEMENTATION of HashSet.addAll(), not just
  its CONTRACT.
- If a future JDK version changes how addAll() works internally, the
  subclass breaks.
- The developer cannot know this without reading HashSet's source code.
```

```java
// Demonstrates the fragile base class problem
public class CountingInheritanceStack<E> extends InheritanceStack<E> {
    private int pushCount = 0;

    @Override
    public void push(E item) {
        pushCount++;
        super.push(item);
    }

    @Override
    public void pushAll(Collection<E> items) {
        pushCount += items.size();
        super.pushAll(items); // BUG: super.pushAll calls push() for each item!
    }
    // After pushAll(List.of("a","b","c")):
    //   pushCount = 6 (3 from pushAll + 3 from push called by super.pushAll)
    //   Expected: 3
}
```

**Q3: Explain the forwarding wrapper pattern and when to use it.**

```text
A3: The forwarding wrapper pattern (from Effective Java Item 18) is a
composition-based alternative to inheritance. It consists of two parts:

1. A forwarding class that implements the same interface as the class to
   wrap, delegating all method calls to a contained instance.
2. A wrapper class that extends the forwarding class and overrides only
   the methods it needs to add behavior to.

When to use it:
- When you need to add behavior to an existing class but cannot (or should
  not) modify it.
- When extending a concrete class that was not designed for inheritance.
- When wrapping third-party library classes.
- When you need to decorate or instrument behavior without risking the
  fragile base class problem.

The pattern works with any class that implements an interface, because the
forwarding class delegates through the interface contract, not through
implementation details.
```

```java
// Step 1: Forwarding class delegates all calls
public class ForwardingSet<E> implements Set<E> {
    private final Set<E> delegate;

    public ForwardingSet(Set<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override public boolean add(E e) { return delegate.add(e); }
    @Override public boolean addAll(Collection<? extends E> c) { return delegate.addAll(c); }
    @Override public int size() { return delegate.size(); }
    // ... all other Set methods delegated
}

// Step 2: Wrapper adds behavior
public class InstrumentedSet<E> extends ForwardingSet<E> {
    private int addCount = 0;

    public InstrumentedSet(Set<E> delegate) { super(delegate); }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);  // delegates to ForwardingSet, which delegates to actual Set
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);  // no double-counting!
    }
}
```

**Q4: When is inheritance the better choice over composition?**

```text
A4: Inheritance is appropriate when ALL of these conditions are met:

1. There is a genuine "is-a" relationship (a Dog IS an Animal, an ArrayList
   IS a List).
2. The superclass is designed and documented for extension (e.g., AbstractList,
   AbstractMap in the JDK).
3. The subclass does not need to work with multiple unrelated types (since
   Java has single inheritance).
4. The subclass genuinely extends the superclass's behavior rather than
   restricting or altering it.

Examples of appropriate inheritance:
- Extending AbstractList to create a custom List implementation.
- Creating a SpecificException that extends a base ApplicationException.
- Sealed class hierarchies (Java 17+) where the set of subtypes is fixed
  and known.

Red flags that suggest composition instead:
- You want to "reuse code" but there is no is-a relationship.
- You are extending a concrete class not designed for inheritance.
- You override methods primarily to restrict or nullify behavior.
- You need to combine behaviors from multiple sources.
```

```java
// Good use of inheritance: AbstractList is designed for extension
public class ImmutableArrayList<E> extends AbstractList<E> {
    private final E[] elements;

    @SuppressWarnings("unchecked")
    public ImmutableArrayList(Collection<E> source) {
        this.elements = (E[]) source.toArray();
    }

    @Override
    public E get(int index) {
        return elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }
    // AbstractList provides all other methods via get() and size()
}

// Good use of sealed classes with inheritance
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}
public record Circle(double radius) implements Shape {
    public double area() { return Math.PI * radius * radius; }
}
```

**Q5: How does composition relate to common design patterns?**

```text
A5: Many classic design patterns are implementations of composition:

1. Strategy Pattern: Compose an algorithm object instead of inheriting
   different algorithm variants. Example: a Sorter class that composes a
   Comparator rather than creating SortByName, SortByAge subclasses.

2. Decorator Pattern: Wrap an object to add behavior. Each decorator
   composes the original object and delegates to it. Example: BufferedReader
   wrapping FileReader.

3. Adapter Pattern: Compose an existing class and present a different
   interface. Adapts one interface to another via delegation.

4. Observer Pattern: A subject composes a list of observers rather than
   inheriting notification behavior.

5. Builder Pattern: Composes parts of a complex object step by step.

6. Dependency Injection: The entire DI paradigm is built on composition --
   dependencies are composed (injected) rather than created via inheritance.

In modern Java, functional interfaces (Function, Predicate, Consumer) enable
lightweight composition without creating explicit classes, replacing many
uses of the Strategy and Command patterns.
```

```java
// Strategy via composition (modern Java style)
public class TextProcessor {
    private final List<Function<String, String>> transformations = new ArrayList<>();

    public TextProcessor addTransformation(Function<String, String> t) {
        transformations.add(t);
        return this;
    }

    public String process(String input) {
        String result = input;
        for (var t : transformations) {
            result = t.apply(result);
        }
        return result;
    }
}

// Usage: compose behaviors at runtime
var processor = new TextProcessor()
    .addTransformation(String::trim)
    .addTransformation(String::toUpperCase)
    .addTransformation(s -> s.replace(" ", "_"));

processor.process("  hello world  "); // "HELLO_WORLD"
```

## Code Examples

- Source: [CompositionBasics.java](src/main/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBasics.java)
- Test: [CompositionBasicsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/compositioninheritance/CompositionBasicsTest.java)
