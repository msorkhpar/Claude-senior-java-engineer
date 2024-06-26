# 1.5.3 Record Patterns in Java (Java 19+)

## Concept Explanation

Record patterns, introduced as a preview feature in Java 19 and finalized in Java 21, enhance pattern matching capabilities in Java. They allow for concise and type-safe decomposition of record values, making it easier to work with complex data structures and improving code readability.

Record patterns extend the pattern matching feature to work seamlessly with records, enabling developers to destructure record instances and bind their components to variables in a single, expressive statement.

## Key Points to Remember

1. Record patterns are part of Java's ongoing effort to improve pattern matching.
2. They work in conjunction with the `instanceof` operator and `switch` expressions/statements.
3. Record patterns allow for nested pattern matching, enabling deep destructuring of complex record structures.
4. They provide a more concise and readable alternative to manual field access.
5. Record patterns are type-safe, with compile-time checks ensuring correct usage.

## Relevant Java Features

- Introduced as a preview feature in Java 19
- Finalized in Java 21
- Works in tandem with other pattern matching features like pattern matching for instanceof and switch expressions

## Common Pitfalls and How to Avoid Them

1. **Forgetting that record patterns are only applicable to records**: 
   - Ensure you're working with record types, not regular classes.

2. **Overlooking the possibility of null values**:
   - Always include null checks or use appropriate patterns to handle potential null values.

3. **Mismatching component names**:
   - The variable names in the pattern don't have to match the record's component names, but matching them improves readability.

4. **Neglecting to use var for type inference**:
   - Utilize `var` to let the compiler infer the types of destructured components when appropriate.

## Best Practices and Optimization Techniques

1. Use record patterns to simplify complex nested record structures.
2. Combine record patterns with sealed classes for exhaustive pattern matching.
3. Leverage type inference with `var` to reduce verbosity.
4. Use record patterns in loops and streams for more expressive data processing.
5. Prefer record patterns over manual getter calls for improved readability.

## Edge Cases and Their Handling

1. **Null records**: Always account for potential null values when using record patterns.
2. **Empty records**: Record patterns work with empty records, but be mindful of their limited utility.
3. **Generic records**: Record patterns can be used with generic records, but type erasure applies at runtime.

## Interview-specific Insights

- Understand the motivation behind record patterns and how they improve code quality.
- Be prepared to compare record patterns with traditional approaches to accessing record components.
- Practice combining record patterns with other pattern matching features for comprehensive solutions.
- Consider performance implications, especially with deeply nested patterns.


Q1: What are record patterns in Java and when were they introduced?
A1: Record patterns are a feature in Java that allows for concise and type-safe decomposition of record values. They were introduced as a preview feature in Java 19 and finalized in Java 21. Record patterns extend the pattern matching capabilities in Java, making it easier to work with complex data structures and improving code readability.

Q2: How do record patterns work with the `instanceof` operator?
A2: Record patterns can be used in conjunction with the `instanceof` operator to perform type checking and component extraction in a single step. Here's an example:

```java
if (obj instanceof Point(int x, int y)) {
    System.out.println("X coordinate: " + x);
    System.out.println("Y coordinate: " + y);
}
```

This code checks if `obj` is an instance of `Point` and, if so, extracts its `x` and `y` components into local variables.

Q3: Can you demonstrate nested record patterns?
A3: Certainly! Nested record patterns allow for deep destructuring of complex record structures. Here's an example using the `Rectangle` record from our implementation:

```java
Rectangle rect = new Rectangle(new Point(1, 2), new Point(3, 4));
if (rect instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) {
    System.out.println("Top-left: (" + x1 + "," + y1 + ")");
    System.out.println("Bottom-right: (" + x2 + "," + y2 + ")");
}
```

This code destructures the `Rectangle` and its nested `Point` records in a single pattern.

Q4: How do record patterns improve code readability compared to manual field access?
A4: Record patterns provide a more concise and expressive way to access record components. Compare these two approaches:

Without record patterns:
```java
if (shape instanceof Rectangle) {
    Rectangle rect = (Rectangle) shape;
    int x1 = rect.topLeft().x();
    int y1 = rect.topLeft().y();
    int x2 = rect.bottomRight().x();
    int y2 = rect.bottomRight().y();
    // Use x1, y1, x2, y2
}
```

With record patterns:
```java
if (shape instanceof Rectangle(Point(var x1, var y1), Point(var x2, var y2))) {
    // Use x1, y1, x2, y2 directly
}
```

The record pattern version is more concise and eliminates the need for explicit casting and multiple method calls.

Q5: How can record patterns be used in switch expressions?
A5: Record patterns integrate seamlessly with switch expressions, allowing for powerful pattern matching. Here's an example from our implementation:

```java
String description = switch (shape) {
    case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) ->
        String.format("Rectangle from (%d,%d) to (%d,%d)", x1, y1, x2, y2);
    case Circle(Point(var x, var y), var r) ->
        String.format("Circle at (%d,%d) with radius %d", x, y, r);
    default -> "Unknown shape";
};
```

This switch expression uses record patterns to match and destructure different shapes, providing a concise way to handle various record types.

Q6: What are some best practices when using record patterns?
A6: Some best practices for using record patterns include:
1. Use them to simplify complex nested record structures.
2. Combine them with sealed classes for exhaustive pattern matching.
3. Leverage type inference with `var` to reduce verbosity.
4. Use them in loops and streams for more expressive data processing.
5. Prefer record patterns over manual getter calls for improved readability.

Q7: How do you handle potential null values when using record patterns?
A7: When using record patterns, it's important to account for potential null values. You can do this by including a null check before the pattern match or by using the pattern matching construct itself to handle null cases. For example:

```java
if (shape instanceof Rectangle(Point(var x1, var y1), Point(var x2, var y2))) {
    // Process non-null Rectangle
} else if (shape == null) {
    // Handle null case
} else {
    // Handle other cases
}
```

Or, using a switch expression:

```java
String result = switch (shape) {
    case null -> "Null shape";
    case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) -> 
        String.format("Rectangle from (%d,%d) to (%d,%d)", x1, y1, x2, y2);
    // Other cases...
    default -> "Unknown shape";
};
```

These examples demonstrate how to safely handle null values when working with record patterns.

## Code Examples

- Test: [RecordPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/RecordPatternTest.java)
- Source: [RecordPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/RecordPattern.java)
