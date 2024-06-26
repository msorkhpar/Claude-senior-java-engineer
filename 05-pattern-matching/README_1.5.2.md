# 1.5.2 Pattern Matching for instanceof (Java 14+)

## Concept Explanation

Pattern matching for `instanceof` is a feature introduced in Java 14 as a preview feature and became standard in Java 
16. It simplifies and enhances the traditional `instanceof` operator by allowing you to declare and initialize a 
17. variable of the tested type in a single step.

This feature reduces boilerplate code and improves readability, especially when working with polymorphic code or when 
you need to perform type-specific operations after an `instanceof` check.

## Key Points to Remember

1. Pattern matching for `instanceof` combines type checking and casting in one step.
2. It eliminates the need for explicit casting after an `instanceof` check.
3. The scope of the pattern variable is limited to the `if` block where it's declared.
4. It works with both classes and interfaces.
5. The pattern variable is effectively final within its scope.

## Relevant Java Features

- Introduced as a preview feature in Java 14
- Standardized in Java 16
- Works well with other modern Java features like switch expressions and records

## Common Pitfalls and How to Avoid Them

1. **Shadowing variables**: Be careful not to shadow existing variables with the pattern variable.
2. **Forgetting the scope**: Remember that the pattern variable is only in scope within the `if` block.
3. **Unnecessary pattern matching**: For simple casts without additional checks, a traditional cast might be clearer.

## Best Practices and Optimization Techniques

1. Use pattern matching when you need to both check the type and use the object as that type.
2. Combine with other modern Java features for more concise and readable code.
3. Consider using pattern matching in switch expressions (Java 17+) for multiple type checks.

## Edge Cases and Their Handling

1. Null values: Pattern matching still requires explicit null checks.
2. Sealed classes: Pattern matching works well with sealed classes and interfaces.

## Interview-specific Insights

- Be prepared to compare and contrast pattern matching with traditional `instanceof` and casting.
- Understand the benefits in terms of code readability and reduced error proneness.
- Be aware of the feature's evolution from Java 14 to 16 and beyond.

Q1: What is the main advantage of pattern matching for instanceof compared to traditional instanceof checks?

A1: The main advantage of pattern matching for instanceof is that it combines type checking and casting into a single
step. This reduces boilerplate code and eliminates the need for explicit casting after an instanceof check. For example:

```java
// Traditional approach
if (obj instanceof Square) {
    Square square = (Square) obj;
    // Use square...
}

// With pattern matching
if (obj instanceof Square square) {
    // Use square directly...
}
```

Q2: How does pattern matching for instanceof affect variable scoping?

A2: The pattern variable introduced in a pattern matching instanceof check is scoped to the if block (or the else block
if the condition is negated). This means the variable is only accessible within that block. For example:

```java
if (obj instanceof Square square) {
    // square is accessible here
    System.out.println(square.getSideLength());
} else {
    // square is not accessible here
    // System.out.println(square.getSideLength()); // This would cause a compilation error
}
// square is not accessible here either
```

Q3: Can you use pattern matching for instanceof with interfaces? Show an example.

A3: Yes, pattern matching for instanceof works with both classes and interfaces. Here's an example:

```java
interface Shape {
    double getArea();
}

class Circle implements Shape {
    private double radius;
    
    // constructor and getArea() implementation...
}

public void processShape(Object obj) {
    if (obj instanceof Shape shape) {
        System.out.println("Area of the shape: " + shape.getArea());
    } else {
        System.out.println("Not a shape");
    }
}
```

Q4: How can pattern matching for instanceof be combined with other modern Java features?

A4: Pattern matching for instanceof can be effectively combined with other modern Java features like switch expressions
and records. Here's an example using switch expressions (Java 17+):

```java
public static String describeShape(Object obj) {
    return switch (obj) {
        case Square square -> "Square with side length: " + square.getSideLength();
        case Circle circle -> "Circle with radius: " + circle.getRadius();
        case Rectangle rectangle -> "Rectangle with width: " + rectangle.getWidth() + " and height: " + rectangle.getHeight();
        default -> "Unknown shape";
    };
}
```

Q5: How does pattern matching for instanceof handle null values?

A5: Pattern matching for instanceof does not handle null values automatically. You still need to perform explicit null 
checks if necessary. For example:

```java
public void processObject(Object obj) {
    if (obj == null) {
        System.out.println("Object is null");
    } else if (obj instanceof String s) {
        System.out.println("String length: " + s.length());
    } else {
        System.out.println("Not a string");
    }
}
```

Q6: Can you explain how pattern matching for instanceof can improve code readability in complex type hierarchies?

A6: Pattern matching for instanceof can significantly improve code readability in complex type hierarchies by reducing 
the amount of boilerplate code and making the intent clearer. Here's an example with a more complex hierarchy:

```java
interface Vehicle { }
class Car implements Vehicle { void drive() { } }
class ElectricCar extends Car { void charge() { } }
class Bicycle implements Vehicle { void pedal() { } }

public void processVehicle(Vehicle vehicle) {
    if (vehicle instanceof ElectricCar electricCar) {
        electricCar.drive();
        electricCar.charge();
    } else if (vehicle instanceof Car car) {
        car.drive();
    } else if (vehicle instanceof Bicycle bicycle) {
        bicycle.pedal();
    } else {
        System.out.println("Unknown vehicle type");
    }
}
```

This code is more readable and less error-prone than the equivalent code using traditional instanceof checks and 
explicit casts.

These examples and explanations cover the key aspects of pattern matching for instanceof, demonstrating its usage, 
benefits, and integration with other Java features.

## Code Examples

- Test: [PatternMatchingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/PatternMatchingTest.java)
- Source: [PatternMatching.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/PatternMatching.java)
