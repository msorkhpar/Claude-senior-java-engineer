# 1.5.4 Switch Patterns in Java (Java 19+)

## Concept Explanation

Switch patterns, introduced as a preview feature in Java 19 and enhanced in subsequent versions, represent a significant
improvement in Java's pattern matching capabilities. This feature extends the switch statement and expression to work
with patterns, allowing for more expressive and concise code when dealing with complex data structures and type
hierarchies.

Switch patterns enable developers to combine type checking, type casting, and data extraction into a single, readable
construct. This feature is particularly useful when working with sealed classes, records, and other hierarchical data
structures.

## Key Points to Remember

1. Switch patterns work with both switch statements and switch expressions.
2. They support type patterns, guarded patterns, and null cases.
3. Pattern variables introduced in case labels are in scope for that case's code block.
4. The compiler performs exhaustiveness checking for switch expressions and statements with no default clause.
5. Switch patterns work well with sealed classes and records, providing compile-time safety for exhaustive matching.

## Relevant Java Features

- Introduced as a preview feature in Java 19
- Enhanced in Java 20 and 21
- Works in conjunction with sealed classes (Java 17+) and records (Java 16+)

## Common Pitfalls and How to Avoid Them

1. **Forgetting exhaustiveness**: Ensure all possible cases are covered or include a default case.
2. **Overlapping patterns**: Be cautious of patterns that may overlap, as the first matching pattern is selected.
3. **Unintended fall-through**: Unlike traditional switches, pattern cases don't fall through by default.

## Best Practices and Optimization Techniques

1. Use switch patterns for type-based dispatching in object-oriented hierarchies.
2. Combine with sealed classes for compile-time exhaustiveness checking.
3. Prefer switch expressions over statements for more concise and less error-prone code.
4. Use guards to add additional conditions to patterns.

## Edge Cases and Their Handling

1. Null handling: Explicitly handle null cases or use a default case.
2. Subtype relationships: Be aware of the order of cases when dealing with subtypes.

## Interview-specific Insights

- Interviewers may ask about the benefits of switch patterns over traditional instanceof checks and casts.
- You might be asked to refactor existing code to use switch patterns for improved readability and maintainability.
- Understanding the interaction between switch patterns and sealed classes is crucial.

Q: What are the main benefits of using switch patterns over traditional instanceof checks and casts?

A: Switch patterns offer several advantages over traditional instanceof checks and casts:

1. Conciseness: They combine type checking, casting, and variable binding into a single, readable construct.
2. Exhaustiveness: The compiler can check if all possible cases are covered, reducing the risk of runtime errors.
3. Pattern matching: They allow for more complex matching, including nested patterns and guards.
4. Readability: The code becomes more declarative and easier to understand, especially for complex type hierarchies.
5. Safety: They eliminate the need for explicit casting, reducing the risk of ClassCastExceptions.

Here's a comparison:

Traditional approach:

```java
if (shape instanceof Circle) {
    Circle circle = (Circle) shape;
    return "Circle with radius " + circle.radius();
} else if (shape instanceof Rectangle) {
    Rectangle rectangle = (Rectangle) shape;
    return "Rectangle with width " + rectangle.width() + " and height " + rectangle.height();
} // ... and so on
```

Switch pattern approach:

```java
return switch (shape) {
    case Circle c -> "Circle with radius " + c.radius();
    case Rectangle r -> "Rectangle with width " + r.width() + " and height " + r.height();
    // ... and so on
};
```

The switch pattern approach is clearly more concise and readable.

Q: How do switch patterns interact with sealed classes, and why is this interaction important?

A: Switch patterns work particularly well with sealed classes due to their complementary nature:

1. Exhaustiveness checking: Sealed classes define a fixed set of possible subtypes. When used with switch patterns,
2. the compiler can ensure that all possible subtypes are handled, providing compile-time safety.

2. Pattern matching: Switch patterns can match against the specific subtypes of a sealed class, allowing for type-safe,
3. expressive code.

3. Extensibility control: Sealed classes restrict which classes can extend them, and switch patterns provide a natural
4. way to handle all permitted subtypes.

4. Design clarity: The combination encourages clear hierarchies and promotes thinking about all possible cases in the
5. domain model.

Example:

```java
sealed interface Vehicle permits Car, Truck, Motorcycle {}
record Car(int doors) implements Vehicle {}
record Truck(double cargoCapacity) implements Vehicle {}
record Motorcycle(boolean hasSidecar) implements Vehicle {}

String describeVehicle(Vehicle v) {
    return switch (v) {
        case Car c -> "Car with " + c.doors() + " doors";
        case Truck t -> "Truck with " + t.cargoCapacity() + " ton capacity";
        case Motorcycle m -> "Motorcycle" + (m.hasSidecar() ? " with sidecar" : "");
    };
}
```

In this example, the compiler ensures that all subtypes of Vehicle are handled, and will raise an error if a new subtype
is added to the sealed interface without updating the switch pattern.

Q: Can you explain how guards work in switch patterns and provide an example of their use?

A: Guards in switch patterns allow you to add additional conditions to a case, providing more fine-grained control over
pattern matching. They are introduced using the `when` keyword followed by a boolean expression.

Key points about guards:

1. They allow for more specific matching beyond just the type.
2. Guards are evaluated only if the pattern matches.
3. They can reference pattern variables introduced in the case label.

Example:

```java
String classifyNumber(Object obj) {
    return switch (obj) {
        case Integer i when i < 0 -> "Negative integer";
        case Integer i when i == 0 -> "Zero";
        case Integer i when i > 0 && i <= 100 -> "Small positive integer";
        case Integer i -> "Large positive integer";
        case Double d when d.isNaN() -> "Not a number";
        case Double d when d.isInfinite() -> "Infinite";
        case Double d -> "Finite double";
        default -> "Not a number type";
    };
}
```

In this example, guards are used to further classify integers based on their value, and doubles based on their special
properties. This allows for much more expressive and precise pattern matching than would be possible with type patterns
alone.

Q: How does null handling work with switch patterns, and what are the best practices for dealing with null values?

A: Switch patterns provide explicit support for handling null values. Here are the key points and best practices:

1. Null case: You can explicitly handle null using `case null ->`.
2. Placement: The null case, if present, is typically placed last (but before any default case).
3. Exhaustiveness: Including a null case contributes to exhaustiveness checking.
4. Default case: If you don't explicitly handle null and don't have a default case, a NullPointerException will be
   thrown for null inputs.

Best practices:

1. Always consider null handling in your switch patterns.
2. Explicitly handle null if it's a valid input in your domain.
3. If null is not expected, you might omit the null case to fail fast with a NullPointerException.
4. Use a default case to handle both null and any future subtypes, if appropriate.

Example:

```java
String describeObject(Object obj) {
    return switch (obj) {
        case String s -> "String of length " + s.length();
        case Integer i -> "Integer: " + i;
        case Long l -> "Long: " + l;
        case null -> "Null object";
        default -> "Unknown object type";
    };
}
```

In this example, null is explicitly handled. If you remove the null case and the default case, a null input would result
in a NullPointerException.

## Code Examples

-
Test: [SwitchPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/SwitchPatternTest.java)
- Source: [SwitchPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/SwitchPattern.java)
