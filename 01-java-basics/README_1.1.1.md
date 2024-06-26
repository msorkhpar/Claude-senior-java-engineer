# 1.1.1 Primitive Data Types

Primitive data types are the most basic data types available in Java. They are the building blocks for data manipulation
and are essential for efficient memory management and performance optimization.

## Key Points:

- Java has 8 primitive data types: byte, short, int, long, float, double, boolean, and char.
- Primitive types are not objects and have no methods.
- Each primitive type has a fixed size in memory.
- They have specific ranges of values they can represent.

## Java 21 Features and Modern Practices:

- While primitive types themselves haven't changed, modern Java encourages the use of var for local variable type
  inference (introduced in Java 10).
- Java 21 introduces enhancements to the switch expressions and pattern matching, which can be used with primitive
  types.

## Common Pitfalls and Best Practices:

1. Integer Overflow: Be aware of the limits of each type to avoid unexpected overflow.

  ```java
  int maxInt = Integer.MAX_VALUE;
int overflow = maxInt + 1; 
  ```

2. Floating-Point Precision: Don't use == for comparing floating-point numbers.

  ```java
    double a = 0.1 + 0.2;
    double b = 0.3;
    // Incorrect: if (a == b)
    // Correct:
    final double EPSILON = 1e-9;
    if(Math.abs(a-b) <EPSILON){
        // Numbers are equal
    }
  ```

3. Division by Zero: Handle potential division by zero for integer types.

  ```java
  int result = (denominator != 0) ? numerator / denominator : 0;
  ```

4. Autoboxing and its performance implications:

    - Autoboxing can impact performance due to additional object creation and memory usage.
    - Wrapper objects consume more memory than their primitive counterparts.
    - Frequent autoboxing/unboxing in loops or large-scale operations can significantly affect performance.

   **When to Use Primitives vs Wrapper Classes:**
    - Use primitives for simple data storage and mathematical operations.
    - Use wrapper classes when working with generic collections or when null is a valid value.
    - Consider performance implications when choosing between primitives and wrappers.

    ```java
    // Primitive usage
    int count = 0;
    
    // Wrapper class usage
    Integer wrappedCount = Integer.valueOf(0);
    
    // Autoboxing
    Integer autoBoxed = 42; // Automatically boxes int to Integer
    
    // Auto-unboxing
    int unboxed = autoboxed; // Automatically unboxes Integer to int
    ```

## Edge Cases:

- Understand the behavior of operations at the extremes of each type's range.
- Be aware of special values like Float.NaN, Double.POSITIVE_INFINITY.

## Best Practices:

- Use primitives in performance-critical code.
- Prefer primitive arrays over wrapper class arrays for better performance.
- Be aware of autoboxing in loops and high-frequency operations.
- Use wrapper classes when working with generics or when null values are needed.

## Interview Insights:

- Be prepared to discuss memory usage of different primitive types.
- Understand autoboxing and its performance implications.
- Know when to use each primitive type and their wrapper classes.

## Code Examples

- Test: [PrimitiveTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/PrimitiveTypesTest.java)
- Source: [PrimitiveTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/PrimitiveTypes.java)
-
Test: [AutoboxingPerformanceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/AutoboxingPerformanceTest.java)
-
Source: [WrapperVsPrimitive.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/WrapperVsPrimitive.java)