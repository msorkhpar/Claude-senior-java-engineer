# 1.3.4 Method Overloading in Java

## Concept Explanation

Method overloading is a feature in Java that allows a class to have multiple methods with the same name but different
parameters. This enables you to create multiple methods that perform similar operations but with different input types
or number of inputs.

## Key Points to Remember

1. Methods can be overloaded based on:
   - Number of parameters
   - Types of parameters
   - Order of parameters
2. Return type alone is not sufficient for method overloading
3. Overloading happens at compile-time (static polymorphism)
4. It improves code readability and reusability

## Relevant Java 21 Features

While method overloading itself is a long-standing feature in Java, Java 21 introduces pattern matching for switch
statements, which can be useful when working with overloaded methods that handle different types.

## Common Pitfalls and How to Avoid Them

1. Overloading with only return type differences: This is not valid and will result in a compilation error.
2. Ambiguous method calls: When two overloaded methods are equally valid for a given call, it results in a compilation error.
3. Autoboxing and varargs confusion: Be cautious when overloading methods with primitive types and their wrapper classes, especially when varargs are involved.

## Best Practices and Optimization Techniques

1. Use meaningful method names that describe the operation
2. Keep the number of parameters manageable (typically no more than 3-4)
3. Use method overloading for closely related operations
4. Consider using builder pattern or method chaining for complex parameter combinations

## Edge Cases and Their Handling

1. Null arguments: Be careful when overloading methods that can accept null values
2. Widening and boxing conversions: Understand how Java chooses between overloaded methods when dealing with these
3. conversions

## Interview-specific Insights

Interviewers often ask about the differences between overloading and overriding,
and may present scenarios to test understanding of method resolution in overloading.

## Interview Q&A

Q1: What is method overloading, and why is it useful?

A1: Method overloading is a feature in Java that allows a class to have multiple methods with the same name but different parameters. It's useful because it:
1. Improves code readability by using the same method name for related operations.
2. Provides flexibility to call the same method with different types or numbers of arguments.
3. Reduces the need to remember multiple method names for similar operations.

```java
class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
    int add(int a, int b, int c) { return a + b + c; }
}
```

Q2: Can you overload methods based on return type alone?

A2: No, you cannot overload methods based on return type alone. Java uses method signatures (method name and parameter list) to distinguish between overloaded methods. The return type is not part of the method signature. For example, this will result in a compilation error:

```java
class Example {
    int getValue() { return 0; }
    // Compilation error: method getValue() is already defined
    double getValue() { return 0.0; }
}
```

Q3: How does Java resolve calls to overloaded methods?

A3: Java resolves calls to overloaded methods in the following order:
1. Exact match by type
2. Matching with type promotion
3. Matching with autoboxing/unboxing
4. Matching with varargs

If there's ambiguity, the compiler will generate an error. For example:

```java
class OverloadResolution {
    void method(int i) { System.out.println("int"); }
    void method(long l) { System.out.println("long"); }
    void method(Integer i) { System.out.println("Integer"); }

    public static void main(String[] args) {
        OverloadResolution or = new OverloadResolution();
        or.method(5); // Calls method(int)
        or.method(5L); // Calls method(long)
        or.method(Integer.valueOf(5)); // Calls method(Integer)
    }
}
```

Q4: What's the difference between method overloading and method overriding?

A4:
Method Overloading:
- Occurs in the same class
- Methods have the same name but different parameters
- Resolved at compile-time (static polymorphism)
- Return type can be different

Method Overriding:
- Occurs in parent and child classes
- Methods have the same name and parameters
- Resolved at runtime (dynamic polymorphism)
- Return type must be the same or a subtype (covariant return type)

```java
class Parent {
    void display() { System.out.println("Parent"); }
}

class Child extends Parent {
    // Method overriding
    @Override
    void display() { System.out.println("Child"); }

    // Method overloading
    void display(String message) { System.out.println(message); }
}
```

Q5: Can you overload main method in Java?

A5: Yes, you can overload the main method in Java, but the JVM will always call the standard main method signature:

```java
public static void main(String[] args)
```

Other overloaded versions of main won't be called automatically by the JVM but can be called from the standard main method:

```java
public class MainOverload {
    public static void main(String[] args) {
        System.out.println("Standard main");
        main("Overloaded");
    }

    public static void main(String arg) {
        System.out.println("Overloaded main: " + arg);
    }
}
```

These questions and answers cover the key aspects of method overloading that are often discussed in Java interviews.
Remember to not only know the concepts but also be able to explain them clearly and provide relevant examples when asked.

## Code Examples

- Test: [MethodOverloadingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/MethodOverloadingTest.java)
- Source: [MethodOverloading.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/MethodOverloading.java)
