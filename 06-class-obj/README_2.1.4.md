# 2.1.4 Inheritance Basics in Java

Inheritance is a fundamental concept in object-oriented programming (OOP) that allows a class to inherit properties and
methods from another class. This mechanism promotes code reuse and establishes a relationship between a more general 
class (superclass or parent class) and a more specialized class (subclass or child class).

## Key Concepts

1. **Superclass and Subclass**:
    - A superclass (or base class) is the class being inherited from.
    - A subclass (or derived class) is the class that inherits from the superclass.

2. **`extends` Keyword**:
    - Used to create a subclass that inherits from a superclass.
    - Syntax: `class Subclass extends Superclass { ... }`

3. **Inherited Members**:
    - Subclasses inherit all non-private members (fields, methods, nested classes) from the superclass.
    - Constructors are not inherited, but the subclass constructor can call the superclass constructor using `super()`.

4. **Method Overriding**:
    - Subclasses can provide specific implementations of methods defined in the superclass.
    - Use `@Override` annotation for clarity and to catch errors.

5. **`super` Keyword**:
    - Used to refer to the superclass's members or constructor.
    - Useful when you want to call the superclass version of an overridden method.

6. **Access Modifiers in Inheritance**:
    - `public` and `protected` members are inherited and accessible in subclasses.
    - `default` (package-private) members are inherited only if the subclass is in the same package.
    - `private` members are not inherited.

7. **The Object Class**:
    - All classes in Java implicitly inherit from `java.lang.Object` if no other superclass is specified.

## Best Practices

1. Use inheritance to model "is-a" relationships.
2. Favor composition over inheritance when appropriate.
3. Design for inheritance or prohibit it (make the class `final`).
4. Don't override methods in constructors.
5. Use the `@Override` annotation when overriding methods.

## Common Pitfalls

1. Overusing inheritance, leading to deep and complex hierarchies.
2. Breaking encapsulation by exposing internal details of the superclass.
3. Violating the Liskov Substitution Principle.
4. Forgetting to call `super()` in subclass constructors when necessary.

## Java 21 Features Relevant to Inheritance

- Sealed Classes (preview feature in earlier versions, standard in Java 17+):
    - Allow you to restrict which classes can inherit from a superclass.
    - Useful for creating a closed set of subclasses.

## Interview Insights

- Be prepared to discuss the advantages and disadvantages of inheritance.
- Understand the difference between inheritance and composition.
- Know how to use `super` in various contexts.
- Be familiar with method overriding rules and best practices.

## Q&A Section

Q1: What is the difference between `extends` and `implements` in Java?
A:
`extends` is used for class inheritance, where a subclass inherits from a superclass. A class can extend only one superclass.
`implements` is used to implement interfaces. A class can implement multiple interfaces.
Example:
```java
   class Dog extends Animal implements Runnable, Comparable<Dog> {
       // Dog inherits from Animal and implements Runnable and Comparable interfaces
   }
```

Q2: Can you override private methods in Java?
A: No, private methods cannot be overridden in Java. They are not inherited by subclasses, so there's no way to override them. 
You can, however, define a method with the same name in the subclass, but it will be a new method, not an override.

Q3: What is the purpose of the `super` keyword in Java?
A:
The `super` keyword in Java is used to refer to the superclass (parent class). It has several uses:
1. To call the superclass constructor: super();
2. To access superclass methods: super.methodName();
3. To access superclass fields: super.fieldName; 
It's particularly useful when you want to extend the functionality of a parent class method instead of completely replacing it.

Q4: Explain the concept of method overriding with an example.
A:

Method overriding occurs when a subclass defines a method with the same name, return type, and parameters as a method
in its superclass. The overridden method in the subclass takes precedence over the superclass method.

Example:
```java
   class Animal {
       public void makeSound() {
           System.out.println("The animal makes a sound");
       }
   }

   class Dog extends Animal {
       @Override
       public void makeSound() {
           System.out.println("The dog barks");
       }
   }

```
In this example, Dog overrides the makeSound() method from Animal, providing its own implementation.

Q5: What is the Liskov Substitution Principle, and how does it relate to inheritance?
A:
   ```text
   The Liskov Substitution Principle (LSP) states that objects of a superclass should be replaceable with objects of 
   its subclasses without affecting the correctness of the program. In other words, a subclass should be usable through
    its base class interface without the need for the user to know the difference.

   This principle is crucial for inheritance because it ensures that inheritance hierarchies are designed correctly. 
   Violating LSP can lead to unexpected behavior and bugs when using polymorphism.

   For example, if you have a Rectangle class and a Square class that extends Rectangle, the Square class should be
    usable anywhere a Rectangle is expected without causing issues. If changing the width of a Square also changes its 
   height (to maintain the square property), it violates LSP because it behaves differently from what would be expected of a Rectangle.
   ```

## Code Examples

- Test: [InheritanceExampleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/InheritanceExampleTest.java)
- Source: [InheritanceExample.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/InheritanceExample.java)
