# 2.5.3 Use Cases for Sealed Classes and Interfaces

Sealed classes and interfaces, introduced in Java 15 and finalized in Java 17, provide a way to restrict the set of classes or interfaces that can extend or implement them. This feature offers several benefits and has specific use cases in Java programming.

## Key Points

1. Sealed classes and interfaces allow for better control over class hierarchies.
2. They provide a middle ground between final classes and open inheritance.
3. Sealed types are particularly useful in domain modeling and API design.
4. They can improve pattern matching and exhaustiveness checking in switch expressions.

## Use Cases

### 1. Domain Modeling

Sealed classes are excellent for representing finite sets of subtypes in a domain model. For example, in a financial application, you might have a sealed `Transaction` class with specific subtypes like `Deposit`, `Withdrawal`, and `Transfer`.

### 2. State Machines

Sealed classes can effectively model states in a state machine, ensuring that only valid states are defined and transitions are controlled.

### 3. Algebraic Data Types

Sealed classes can be used to implement algebraic data types, common in functional programming, allowing for more expressive and type-safe code.

### 4. API Design

When designing APIs, sealed classes can help control how your classes are used and extended, providing a clear contract for implementers.

### 5. Pattern Matching

Sealed classes work well with pattern matching, especially in switch expressions, allowing for exhaustive checking of all possible subtypes.

## Best Practices

1. Use sealed classes when you have a known, finite set of subclasses.
2. Consider using sealed interfaces for API contracts where you want to limit implementations.
3. Combine sealed classes with records for immutable data structures.
4. Leverage pattern matching in switch expressions for cleaner, more maintainable code.

## Common Pitfalls

1. Overuse of sealed classes can lead to inflexible designs.
2. Forgetting to declare subclasses as either `final`, `sealed`, or `non-sealed`.
3. Not considering future extensibility needs when sealing a class.

## Interview Insights

- Be prepared to discuss the trade-offs between sealed classes and traditional inheritance.
- Understand how sealed classes relate to pattern matching and switch expressions.
- Consider performance implications, especially in terms of compilation and runtime checks.


## Interview Q&A:

Q: What are the main benefits of using sealed classes in Java?
A: The main benefits of using sealed classes in Java are:
1. Restricted inheritance: You can control which classes can extend or implement a sealed class or interface.
2. Better domain modeling: Sealed classes are ideal for representing finite sets of subtypes in a domain model.
3. Enhanced pattern matching: They work well with switch expressions, allowing for exhaustive checking of all possible subtypes.
4. Improved API design: Sealed classes provide a clear contract for implementers when designing APIs.
5. Middle ground between final and open classes: They offer more flexibility than final classes while still maintaining control over the class hierarchy.

Q: How do sealed classes improve pattern matching in switch expressions?
A: Sealed classes improve pattern matching in switch expressions by:
1. Enabling exhaustiveness checking: The compiler can ensure that all possible subtypes are handled in the switch expression.
2. Providing type-safe casting: Each case in the switch can safely cast to the specific subtype without additional checks.
3. Eliminating the need for a default case: When all subtypes are covered, no default case is required, reducing the risk of unhandled cases.

Here's an example:

```java
public String describeVehicle(VehicleType vehicle) {
    return switch (vehicle) {
        case Car c -> "This is a car with " + c.getWheelCount() + " wheels";
        case Motorcycle m -> "This is a motorcycle with " + m.getWheelCount() + " wheels";
        case Truck t -> "This is a truck with " + t.getWheelCount() + " wheels";
    };
}
```

Q: What are some potential drawbacks or limitations of using sealed classes?
A: Some potential drawbacks or limitations of using sealed classes include:
1. Reduced flexibility: Sealing a class hierarchy can make it harder to extend in the future if new subtypes are needed.
2. Increased coupling: Sealed classes and their permitted subclasses must be defined in the same module or package, which can increase coupling between classes.
3. Complexity in large hierarchies: Managing a large number of permitted subclasses can become complex and harder to maintain.
4. Limited use in libraries: Sealed classes may be less useful in public libraries where extensibility is often desired.
5. Learning curve: Developers new to sealed classes may need time to understand and effectively use this feature.

Q: How do sealed classes relate to the concept of algebraic data types in functional programming?
A: Sealed classes in Java provide a way to implement algebraic data types, which are common in functional programming languages. Here's how they relate:

1. Sum types: Sealed classes can represent sum types (also known as tagged unions or discriminated unions) where a type can be one of several variants. Each permitted subclass represents a variant of the type.

2. Product types: When combined with records, sealed classes can effectively model product types, where a type is composed of multiple fields.

3. Pattern matching: Like algebraic data types in functional languages, sealed classes in Java work well with pattern matching, allowing for exhaustive checking of all possible variants.

4. Immutability: Sealed classes, especially when used with records, encourage immutable data structures, which is a key principle in functional programming.

Here's an example of how sealed classes can model an algebraic data type:

```java
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}

record Circle(double radius) implements Shape {
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

record Rectangle(double width, double height) implements Shape {
    @Override
    public double area() {
        return width * height;
    }
}

record Triangle(double base, double height) implements Shape {
    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

// Usage with pattern matching
public String describeShape(Shape shape) {
    return switch (shape) {
        case Circle c -> "Circle with radius " + c.radius();
        case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
        case Triangle t -> "Triangle with base " + t.base() + " and height " + t.height();
    };
}
```

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/sealedclasses/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/sealedclasses/Person.java)