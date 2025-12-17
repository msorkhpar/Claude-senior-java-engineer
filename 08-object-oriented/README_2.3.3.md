# 2.3.3 Abstraction in Java

## Concept Overview

Abstraction is a fundamental principle in object-oriented programming that allows developers to hide complex
implementation details and expose only the essential features of an object. It's about creating a simplified view of
an object that represents its core functionality without getting bogged down in the specifics of how that functionality
is achieved.

In Java, abstraction is primarily achieved through abstract classes and interfaces. These constructs allow developers
to define a common structure and behavior for a group of related classes without specifying all the details.

## Focusing on Essential Features and Behavior

Abstraction involves identifying the core attributes and behaviors of an object that are relevant to the problem at
hand. This process helps in:

1. Simplifying complex systems by breaking them down into manageable parts.
2. Hiding unnecessary details from the user of the class.
3. Focusing on what an object does rather than how it does it.

Example:
Consider a `Shape` abstraction. While different shapes (circle, square, triangle) have unique properties, they all share
some common behavior, like calculating area or perimeter. Abstraction allows us to define these common behaviors without
specifying how each shape implements them.

```java
public abstract class Shape {
    public abstract double calculateArea();
    public abstract double calculatePerimeter();
}
```

## Interfaces as Contracts

Interfaces in Java serve as contracts that specify what a class must do, without dictating how it should do it. They
define a set of abstract methods that implementing classes must provide concrete implementations for.

Key points about interfaces:

1. They can contain only abstract methods (prior to Java 8) and constants.
2. From Java 8 onwards, they can also include default and static methods.
3. A class can implement multiple interfaces, allowing for a form of multiple inheritance.

Example:

```java
public interface Drawable {
    void draw();
    default void display() {
        System.out.println("Displaying the drawable object");
    }
}
```

## Abstract Classes vs. Interfaces

While both abstract classes and interfaces are used for abstraction, they have some key differences:

1. **Multiple Inheritance**: A class can implement multiple interfaces but can extend only one abstract class.
2. **State**: Abstract classes can have instance variables (state), while interfaces cannot (except for constants).
3. **Method Implementation**: Abstract classes can have both abstract and concrete methods, while interfaces (prior to
   Java 8) could only have abstract methods.
4. **Constructor**: Abstract classes can have constructors, interfaces cannot.
5. **Access Modifiers**: Abstract class methods can have any access modifier, while interface methods are implicitly
   public and abstract.

Example of an abstract class:

```java
public abstract class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public abstract void makeSound();

    public void eat() {
        System.out.println(name + " is eating.");
    }
}
```

## Key Points to Remember

1. Abstraction helps in managing complexity by hiding unnecessary details.
2. Use interfaces when you want to define a contract for behavior without implementation.
3. Use abstract classes when you want to provide a common base implementation for a group of related classes.
4. From Java 8, interfaces can have default and static methods, blurring the line between interfaces and abstract
   classes.
5. Abstraction promotes code reusability and makes the system more maintainable.

## Common Pitfalls and How to Avoid Them

1. **Overuse of Abstraction**: Don't create abstractions for every possible scenario. Use them when there's a clear need
   for hiding complexity or defining a common structure.
2. **Leaky Abstractions**: Ensure that your abstraction doesn't expose unnecessary implementation details.
3. **Ignoring the Liskov Substitution Principle**: Ensure that derived classes can be used interchangeably with their
   base classes without affecting the correctness of the program.

## Best Practices

1. Design interfaces and abstract classes based on behavior, not on properties.
2. Keep interfaces small and focused (Interface Segregation Principle).
3. Use abstract classes to provide a common base implementation for related classes.
4. Prefer composition over inheritance when possible to achieve better flexibility.
5. Use interfaces for defining types and abstract classes for providing partial implementations.

## Interview Questions and Answers

1. Q: What is abstraction in Java, and why is it important?
   A: Abstraction in Java is a process of hiding the implementation details and showing only functionality to the user.
   It's important because:
   ```text
   - It reduces complexity by hiding unnecessary details
   - It enhances code reusability
   - It allows focusing on what an object does rather than how it does it
   - It provides a clear separation between the interface and implementation of a class
   ```

2. Q: How do you achieve abstraction in Java?
   A: Abstraction in Java is primarily achieved through:
   ```text
   1. Abstract classes: These are classes that cannot be instantiated and may contain abstract methods.
   2. Interfaces: These define a contract of methods that implementing classes must provide.
   Both abstract classes and interfaces allow you to define a common structure for related classes
   without specifying all the details of implementation.
   ```

3. Q: What's the difference between an interface and an abstract class?
   A: The main differences are:
   ```text`
    1. Multiple Inheritance: A class can implement multiple interfaces but extend only one abstract class.
    2. State: Abstract classes can have instance variables, interfaces cannot (except constants).
    3. Method Implementation: Abstract classes can have both abstract and concrete methods, while interfaces
       (prior to Java 8) could only have abstract methods.
    4. Constructor: Abstract classes can have constructors, interfaces cannot.
    5. Access Modifiers: Abstract class methods can have any access modifier, while interface methods are
       implicitly public and abstract.
   ```

4. Q: Can you provide an example of how abstraction might be used in a real-world application?
   A: Certainly. Here's an example of how abstraction might be used in a payment processing system:

   ```java
   public interface PaymentProcessor {
       boolean processPayment(double amount);
       void refund(double amount);
   }

   public class CreditCardProcessor implements PaymentProcessor {
       @Override
       public boolean processPayment(double amount) {
           // Implementation for credit card payment
           return true;
       }

       @Override
       public void refund(double amount) {
           // Implementation for credit card refund
       }
   }

   public class PayPalProcessor implements PaymentProcessor {
       @Override
       public boolean processPayment(double amount) {
           // Implementation for PayPal payment
           return true;
       }

       @Override
       public void refund(double amount) {
           // Implementation for PayPal refund
       }
   }
   ```

   In this example, `PaymentProcessor` is an interface that abstracts the concept of processing payments. Different
   payment methods (credit card, PayPal) implement this interface, providing their specific implementations.
   The rest of the application can work with the `PaymentProcessor` interface without needing to know the details of
   how each payment method is processed.

5. Q: How does abstraction contribute to code maintainability and scalability?
   A: Abstraction contributes to maintainability and scalability in several ways:
   ```text
   1. Separation of Concerns: By hiding implementation details, abstraction allows developers to focus on
      high-level functionality without getting bogged down in low-level details.
   2. Easier Updates: Changes to the implementation of an abstract class or interface don't affect the
      classes that use it, as long as the public interface remains the same.
   3. Code Reusability: Abstract classes and interfaces promote code reuse by defining common structures
      and behaviors that can be shared across multiple concrete classes.
   4. Flexibility: Abstraction allows for easy addition of new implementations without affecting existing code.
   5. Reduced Complexity: By hiding unnecessary details, abstraction makes the codebase easier to understand
      and maintain.
   ```

These questions and answers cover the key aspects of abstraction in Java, including its importance, implementation,
and practical applications. They also touch on the differences between abstract classes and interfaces, which is a
common topic in Java interviews.

## Code Examples

- Test: [AbstractionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/oo/AbstractionTest.java)
- Source: [Vehicle.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Vehicle.java)
- Source: [Drivable.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Drivable.java)
- Source: [Car.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Car.java)
