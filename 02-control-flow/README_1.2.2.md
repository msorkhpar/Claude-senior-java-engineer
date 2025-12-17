# 1.2.2 For Loops in Java

## Concept Explanation

For loops in Java provide a compact way to iterate over a range of values or elements in a collection. They are
particularly useful when you know in advance how many times you want to execute a block of code.

## Key Points to Remember

1. Basic syntax: `for (initialization; condition; update) { // code block }`
2. The initialization, condition, and update parts are optional.
3. Multiple variables can be initialized and updated in a for loop.
4. The scope of variables declared in the initialization is limited to the loop.
5. Infinite loops can be created with `for (;;) { // code block }`

## Java 21 Features

1. Enhanced for loop (for-each): `for (Type item : collection) { // code block }`
2. Use with the `var` keyword for local variable type inference (Java 10+).

## Common Pitfalls and How to Avoid Them

1. Off-by-one errors: Be careful with loop bounds, especially when using `<=` or `>=`.
2. Modifying loop variables inside the loop body can lead to unexpected behavior.
3. Forgetting to update the loop variable can result in infinite loops.
4. Using floating-point values for loop counters can cause precision issues.

## Best Practices and Optimization Techniques

1. Use enhanced for loops when iterating over collections or arrays.
2. Avoid unnecessary object creation inside loops.
3. Consider using `StringBuilder` for string concatenation in loops.
4. Extract loop-invariant computations outside the loop.
5. Use appropriate data structures to minimize nested loops.

## Edge Cases and Their Handling

1. Empty collections or arrays: Ensure your code handles these gracefully.
2. Large number of iterations: Be aware of potential integer overflow.
3. Breaking out of nested loops: Use labeled breaks when necessary.

## Interview-specific Insights

1. Be prepared to write loops on a whiteboard or in a code editor without auto-completion.
2. Understand when to use for loops vs. while loops or Java 8+ Stream operations.
3. Be familiar with common loop patterns (e.g., finding max/min, accumulating results).

Q1: What are the three components of a basic for loop in Java?
A1: The three components of a basic for loop in Java are:

1. Initialization: Executed once at the beginning of the loop.
2. Condition: Checked before each iteration.
3. Update: Executed at the end of each iteration.

Here's an example:

```java
for (int i = 0; i < 5; i++) {
    System.out.println(i);
}
```

Q2: How does an enhanced for loop (for-each loop) work in Java?
A2: An enhanced for loop, also known as a for-each loop, is used to iterate over elements of an array or a collection.
It simplifies the syntax and reduces the chance of errors. Here's an example:

```java
int[] numbers = {1, 2, 3, 4, 5};
for (int num : numbers) {
    System.out.println(num);
}
```

Q3: How can you create an infinite loop using a for loop?
A3: An infinite loop can be created using a for loop by omitting all three components of the loop header. Here's an
example:

```java
for (;;) {
    System.out.println("This will run forever");
    // Remember to include a break condition to avoid an actual infinite loop
}
```

Q4: What is the difference between `break` and `continue` in a for loop?
A4:

- `break`: Terminates the loop entirely and transfers control to the statement immediately following the loop.
- `continue`: Skips the rest of the current iteration and moves to the next iteration of the loop.

Example:

```java
for (int i = 0; i < 5; i++) {
    if (i == 2) {
        continue; // Skip printing 2
    }
    if (i == 4) {
        break; // Stop the loop when i is 4
    }
    System.out.println(i);
}
```

Q5: How can you iterate over a 2D array using nested for loops?
A5: To iterate over a 2D array, you can use nested for loops where the outer loop iterates over rows and the inner loop
iterates over columns. Here's an example:

```java
int[][] matrix = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
for (int i = 0; i < matrix.length; i++) {
    for (int j = 0; j < matrix[i].length; j++) {
        System.out.print(matrix[i][j] + " ");
    }
    System.out.println();
}
```

## Code Examples

- Test: [ForLoopTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/ForLoopTest.java)
- Source: [ForLoop.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/ForLoop.java)