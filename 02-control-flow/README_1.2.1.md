# 1.2.1 if/else statements in Java

## Concept Explanation

The `if/else` statement in Java is a fundamental control flow construct that allows you to execute different blocks of code based on specified conditions. It enables your program to make decisions and choose different paths of execution depending on whether certain conditions are true or false.

## Key Points to Remember

1. The basic syntax of an `if` statement:
   ```java
   if (condition) {
       // code to execute if condition is true
   }
   ```

2. The `else` clause is optional and executes when the `if` condition is false:
   ```java
   if (condition) {
       // code to execute if condition is true
   } else {
       // code to execute if condition is false
   }
   ```

3. You can chain multiple conditions using `else if`:
   ```java
   if (condition1) {
       // code for condition1
   } else if (condition2) {
       // code for condition2
   } else {
       // code if no conditions are true
   }
   ```

4. Conditions must evaluate to a boolean value (`true` or `false`).
5. Curly braces `{}` are optional for single-line statements but recommended for clarity.
6. The ternary operator `?:` can be used as a shorthand for simple if/else statements.

## Java 21 Features

While `if/else` statements haven't changed significantly, Java 21 introduces pattern matching in `if` statements (preview feature), allowing for more concise and expressive code:

```java
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}
```

## Common Pitfalls and How to Avoid Them

1. **Forgetting curly braces**: Always use curly braces, even for single-line statements, to prevent logic errors.
2. **Using `=` instead of `==` in conditions**: `=` is assignment, `==` is comparison.
3. **Unreachable code**: Ensure all branches of your `if/else` statements are reachable.
4. **Complex nested conditions**: Use early returns or extract methods to simplify complex nested conditions.

## Best Practices and Optimization Techniques

1. Use `else if` for mutually exclusive conditions to improve readability.
2. Consider using switch statements or expressions for multiple conditions checking against a single variable.
3. For simple conditions, use the ternary operator to make code more concise.
4. Avoid deep nesting of `if/else` statements; refactor into separate methods if necessary.

## Edge Cases and Their Handling

1. **Null checks**: Always check for null before accessing object properties.
2. **Boundary conditions**: Be careful with comparisons involving the edges of ranges.
3. **Empty collections**: Check if a collection is empty before performing operations on its elements.

## Interview-specific Insights

- Be prepared to write `if/else` statements on a whiteboard, paying attention to syntax and logic.
- Understand how to simplify complex conditional logic.
- Be familiar with the ternary operator and when it's appropriate to use it.

## References

- [Java Language Specification - The if Statement](https://docs.oracle.com/javase/specs/jls/se21/html/jls-14.html#jls-14.9)
- [Oracle Java Tutorials - The if-then and if-then-else Statements](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/if.html)
```

Now, let's move on to the Interview Q&A section:

Q1: What is the difference between `if (x == 0)` and `if (0 == x)`? Is there any advantage to either?
```java
int x = 0;
if (x == 0) {
    System.out.println("x is zero");
}

if (0 == x) {
    System.out.println("x is zero (Yoda condition)");
}
```
A1: Both conditions are logically equivalent and will produce the same result. The second form (`0 == x`) is sometimes called a "Yoda condition". In languages like C, where assignment `=` can be mistakenly used instead of comparison `==`, Yoda conditions can prevent accidental assignments. In Java, this is less of an issue because the compiler will catch most of these errors. Generally, the first form (`x == 0`) is more readable and commonly used in Java.

Q2: How can you use `if/else` statements to implement absolute value function?
```java
public static int abs(int number) {
    if (number < 0) {
        return -number;
    } else {
        return number;
    }
}
```
A2: This implementation uses an `if/else` statement to check if the number is negative. If it is, it returns the negation of the number (making it positive). Otherwise, it returns the number as is. This effectively implements the absolute value function.

Q3: What's wrong with the following code, and how would you fix it?
```java
if (score >= 90)
    System.out.println("A");
    System.out.println("Excellent!");
if (score >= 80)
    System.out.println("B");
if (score >= 70)
    System.out.println("C");
else
    System.out.println("Needs improvement");
```
A3: There are several issues with this code:
1. The first `if` statement is missing curly braces, so only the first `println` is part of the `if` block. The "Excellent!" message will always print.
2. The conditions are not mutually exclusive, so multiple grade letters could print for a single score.
3. The `else` is associated with the last `if`, which might not be the intent.

Here's a corrected version:
```java
if (score >= 90) {
    System.out.println("A");
    System.out.println("Excellent!");
} else if (score >= 80) {
    System.out.println("B");
} else if (score >= 70) {
    System.out.println("C");
} else {
    System.out.println("Needs improvement");
}
```

Q4: Explain the ternary operator and provide an example of its usage.
```java
int age = 20;
String status = (age >= 18) ? "Adult" : "Minor";
System.out.println(status);  // Outputs: Adult
```
A4: The ternary operator is a shorthand way of writing a simple if/else statement. It has the form:
```
condition ? expression1 : expression2
```
If the condition is true, expression1 is evaluated and becomes the result. If the condition is false, expression2 is 
evaluated and becomes the result. In the example, we check if age is greater than or equal to 18. If true, "Adult" is
assigned to status; otherwise, "Minor" is assigned.


## Code Examples

- Test: [IfElseTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/IfElseTest.java)
- Source: [IfElse.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/IfElse.java)
