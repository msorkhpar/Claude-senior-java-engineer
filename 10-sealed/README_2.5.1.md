# 2.5.1 Defining Sealed Classes and Interfaces

Sealed classes and interfaces, introduced in Java 15 and finalized in Java 17, provide a way to restrict which other
classes or interfaces may extend or implement them. This feature enhances control over class hierarchies and enables
more precise modeling of domain-specific relationships.

## Key Points

1. Sealed classes and interfaces are defined using the `sealed` modifier.
2. They must specify their permitted subclasses or subinterfaces using the `permits` clause.
3. Permitted subclasses must have one of three modifiers: `final`, `sealed`, or `non-sealed`.
4. Sealed classes and interfaces must be in the same module or package as their permitted subclasses.

## Syntax

```java
public sealed class Shape permits Circle, Square, Triangle { }

public sealed interface Vehicle permits Car, Motorcycle, Truck { }
```

## Benefits

1. **Controlled Inheritance**: Explicitly define which classes can extend or implement a sealed type.
2. **Pattern Matching**: Enables exhaustive pattern matching in switch expressions.
3. **Domain Modeling**: Better represents closed sets of subtypes in domain models.
4. **API Design**: Allows library designers to restrict how their APIs can be extended.

## Common Pitfalls

1. Forgetting to specify all permitted subclasses in the `permits` clause.
2. Not applying the required modifier (`final`, `sealed`, or `non-sealed`) to permitted subclasses.
3. Placing permitted subclasses in a different package or module than the sealed class.

## Best Practices

1. Use sealed classes when you want to represent a fixed set of subtypes.
2. Consider using sealed interfaces for API design to control how clients can implement your interfaces.
3. Combine sealed classes with pattern matching in switch expressions for type-safe, exhaustive handling of subtypes.

## Interview Insights

Interviewers often ask about:

- The purpose and benefits of sealed classes
- How sealed classes differ from final classes or enums
- The relationship between sealed classes and pattern matching
- Use cases for sealed classes in API design and domain modeling

Q1: What is the primary purpose of sealed classes in Java?

```text
A1: The primary purpose of sealed classes in Java is to restrict which other classes can extend them. This allows developers to have more control over class hierarchies and enables more precise modeling of domain-specific relationships. Sealed classes provide a middle ground between regular inheritance (which is completely open) and final classes (which prevent any inheritance).
```

Q2: How do you define a sealed class and its permitted subclasses?

```java
public sealed class Animal permits Dog, Cat, Bird {
    // Class body
}

final class Dog extends Animal {
    // Class body
}

final class Cat extends Animal {
    // Class body
}

non-sealed class Bird extends Animal {
    // Class body
}
```

Q3: What are the rules for subclasses of a sealed class?

```text
A3: Subclasses of a sealed class must adhere to the following rules:
1. They must be declared in the same module as the sealed class (or in the same package if the sealed class is in the unnamed module).
2. They must have one of three permitted modifiers:
   - final: The subclass cannot be further extended.
   - sealed: The subclass is itself a sealed class and must specify its own permitted subclasses.
   - non-sealed: The subclass can be freely extended by any other class.
3. They must extend the sealed class directly.
```

Q4: How do sealed classes differ from enums?

```text
A4: While both sealed classes and enums represent a fixed set of possibilities, they differ in several ways:
1. Instances: Enum constants are singleton instances, while sealed classes can have multiple instances of each subclass.
2. Inheritance: Enum constants cannot extend other classes (they implicitly extend java.lang.Enum), while sealed class subclasses can extend the sealed class and potentially other interfaces.
3. Functionality: Sealed classes can have different implementations and state for each subclass, while enum constants are more limited in this regard.
4. Use cases: Enums are typically used for simple sets of constants, while sealed classes are better suited for more complex hierarchies with varying behavior between subclasses.
```

Q5: Can you demonstrate how sealed classes improve pattern matching in switch expressions?

```java
public String describeShape(Shape shape) {
    return switch (shape) {
        case Circle c -> "A circle with radius " + c.getRadius();
        case Square s -> "A square with side length " + s.getSide();
        case Triangle t -> "A triangle with base " + t.getBase() + " and height " + t.getHeight();
    };
}
```

Q6: What are some practical use cases for sealed classes in real-world applications?

```text
A6: Sealed classes are useful in various scenarios:
1. Domain modeling: Representing a fixed set of subtypes in a domain model, such as different types of financial transactions or document types.
2. State machines: Modeling states in a state machine where transitions between states are well-defined and limited.
3. Abstract syntax trees: Representing nodes in a compiler or interpreter's abstract syntax tree.
4. API design: Allowing library designers to restrict how their APIs can be extended, ensuring better control over the behavior of the system.
5. Pattern matching: Enabling exhaustive pattern matching in switch expressions, which can lead to more robust and maintainable code.
6. Algebraic data types: Implementing sum types or tagged unions in a type-safe manner.
```

## Code Examples

-
Test: [SealedClassesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/sealedclasses/SealedClassesTest.java)
- Source: [Shape.java](src/main/java/com/github/msorkhpar/claudejavatutor/sealedclasses/Shape.java)