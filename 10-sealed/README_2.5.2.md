# 2.5.2. Permitted and Non-Permitted Subclasses in Sealed Classes and Interfaces

## Overview
Sealed classes and interfaces in Java allow you to restrict which classes can inherit from them. This feature provides more control over the class hierarchy and enables better code organization and maintainability. In this section, we'll explore permitted and non-permitted subclasses in the context of sealed classes and interfaces.

## Key Concepts

### Permitted Subclasses
- Explicitly listed classes that are allowed to extend a sealed class or implement a sealed interface.
- Defined using the `permits` keyword in the sealed class or interface declaration.
- Must be in the same module as the sealed class or interface, or in the same package if the sealed type is in an unnamed module.

### Non-Permitted Subclasses
- Classes that are not allowed to extend a sealed class or implement a sealed interface.
- Any attempt to create a non-permitted subclass will result in a compilation error.

## Declaring Permitted Subclasses

There are three ways to declare permitted subclasses:

1. **Explicit Declaration**: Using the `permits` clause in the sealed class or interface declaration.
   ```java
   public sealed class Shape permits Circle, Square, Triangle { }
   ```

2. **Implicit Declaration**: When the permitted subclasses are defined in the same file as the sealed class or interface.
   ```java
   public sealed class Shape { }
   final class Circle extends Shape { }
   final class Square extends Shape { }
   non-sealed class Triangle extends Shape { }
   ```

3. **Mixed Declaration**: Combining explicit and implicit declarations.
   ```java
   public sealed class Shape permits Circle {
       final class Square extends Shape { }
   }
   final class Circle extends Shape { }
   ```

## Rules for Permitted Subclasses

1. Permitted subclasses must extend the sealed class or implement the sealed interface.
2. Permitted subclasses must be declared as either `final`, `sealed`, or `non-sealed`.
3. Permitted subclasses must be accessible to the sealed class or interface.

## Benefits of Using Permitted Subclasses

1. **Type Safety**: Ensures that only specific classes can inherit from a sealed type.
2. **Code Maintainability**: Provides a clear and controlled hierarchy of classes.
3. **Pattern Matching**: Enables exhaustive pattern matching in switch expressions.
4. **API Design**: Allows for better API design by controlling extensibility.

## Common Pitfalls

1. Forgetting to declare permitted subclasses as `final`, `sealed`, or `non-sealed`.
2. Attempting to create a subclass that is not permitted.
3. Declaring permitted subclasses in a different package or module (when not allowed).

## Best Practices

1. Use sealed classes and interfaces when you want to restrict the inheritance hierarchy.
2. Choose the appropriate modifier (`final`, `sealed`, or `non-sealed`) for permitted subclasses based on your design needs.
3. Keep the list of permitted subclasses minimal and focused on the domain model.
4. Use sealed classes in combination with pattern matching for more robust switch expressions.

## Interview Insights

- Be prepared to explain the difference between `sealed`, `final`, and `non-sealed` classes.
- Understand the implications of sealed classes on class design and inheritance.
- Be able to discuss scenarios where sealed classes provide benefits over traditional class hierarchies.


Q: What are the three ways to declare permitted subclasses in a sealed class or interface?

A: The three ways to declare permitted subclasses are:

1. Explicit Declaration: Using the `permits` clause in the sealed class or interface declaration.
   Example:
```java
   public sealed class Shape permits Circle, Square, Triangle { }
```

2. Implicit Declaration: When the permitted subclasses are defined in the same file as the sealed class or interface.
   Example:
   ```java
   public sealed class Shape { }
   final class Circle extends Shape { }
   final class Square extends Shape { }
   non-sealed class Triangle extends Shape { }
   ```

3. Mixed Declaration: Combining explicit and implicit declarations.
   Example:
   ```java
   public sealed class Shape permits Circle {
       final class Square extends Shape { }
   }
   final class Circle extends Shape { }
   ```

Q: What are the rules that permitted subclasses must follow?

A: Permitted subclasses must follow these rules:

1. They must extend the sealed class or implement the sealed interface.
2. They must be declared as either `final`, `sealed`, or `non-sealed`.
3. They must be accessible to the sealed class or interface (usually in the same package or module).

Q: How does using sealed classes and interfaces improve pattern matching in switch expressions?

A: Sealed classes and interfaces improve pattern matching in switch expressions by enabling exhaustive pattern matching. Since the compiler knows all possible subclasses of a sealed type, it can ensure that all cases are covered in a switch expression. This leads to more robust and maintainable code.

Example:
```java
public double calculateArea(Shape shape) {
    return switch (shape) {
        case Circle c -> c.area();
        case Square s -> s.area();
        case Triangle t -> t.area();
    };
}
```

In this example, the compiler can verify that all possible subclasses of `Shape` are handled in the switch expression, making it exhaustive.

Q: What is the difference between `final`, `sealed`, and `non-sealed` subclasses of a sealed class?

A: The differences are:

1. `final` subclasses: Cannot be extended further. They are the end of the inheritance chain.
2. `sealed` subclasses: Can be extended, but only by their own permitted subclasses. This creates a multi-level sealed hierarchy.
3. `non-sealed` subclasses: Can be freely extended by any class, effectively "opening up" the hierarchy at that point.

Example:
```java
public sealed class Vehicle permits Car, Truck, Motorcycle {
    // Vehicle implementation
}

final class Car extends Vehicle {
    // Car implementation, cannot be extended
}

sealed class Truck extends Vehicle permits PickupTruck, SemiTruck {
    // Truck implementation, can only be extended by PickupTruck and SemiTruck
}

non-sealed class Motorcycle extends Vehicle {
    // Motorcycle implementation, can be freely extended
}
```

Q: What are some common pitfalls when working with sealed classes and interfaces?

A: Common pitfalls include:

1. Forgetting to declare permitted subclasses as `final`, `sealed`, or `non-sealed`.
2. Attempting to create a subclass that is not permitted.
3. Declaring permitted subclasses in a different package or module (when not allowed).
4. Overusing sealed classes, which can lead to overly rigid class hierarchies.
5. Not considering the impact on future extensibility of the codebase.

To avoid these pitfalls:
- Always declare the permitted subclasses with the appropriate modifier.
- Keep the sealed class and its permitted subclasses in the same package or module.
- Use sealed classes judiciously, balancing control over the class hierarchy with flexibility for future changes.
- Consider using interfaces or abstract classes for more flexible designs when appropriate.

## Code Examples

- Test: [SealedClassExampleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/sealedclasses/SealedClassExampleTest.java)
- Source: [SealedClassExample.java](src/main/java/com/github/msorkhpar/claudejavatutor/sealedclasses/SealedClassExample.java)