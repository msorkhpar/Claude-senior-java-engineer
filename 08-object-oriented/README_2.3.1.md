# 2.3.1. Inheritance

Inheritance is a fundamental concept in object-oriented programming (OOP) that allows a class to inherit properties 
and methods from another class. This mechanism promotes code reusability, establishes a hierarchical relationship between
classes, and supports the creation of more specialized classes based on existing ones.

## 2.3.1.1. Extending classes and code reusability

Inheritance enables the creation of new classes (subclasses or derived classes) based on existing classes 
(superclasses or base classes). The subclass inherits fields and methods from its superclass, allowing for code reuse
and the addition of new functionality.

Key points:
- Use the `extends` keyword to create a subclass.
- Subclasses inherit all non-private members (fields and methods) from the superclass.
- Constructors are not inherited, but the superclass constructor can be called using `super()`.
- Java supports single inheritance for classes (a class can only extend one superclass).

Example:
```java
public class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void makeSound() {
        System.out.println("The animal makes a sound");
    }
}

public class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }

    // Additional method specific to Dog
    public void wagTail() {
        System.out.println(name + " is wagging its tail");
    }
}
```

## 2.3.1.2. Method overriding and super keyword

Method overriding allows a subclass to provide a specific implementation for a method that is already defined in its 
superclass. This enables the subclass to modify or extend the behavior inherited from the superclass.

Key points:
- The overriding method must have the same name, return type, and parameter list as the overridden method.
- Use the `@Override` annotation to indicate that a method is intended to override a superclass method.
- The `super` keyword is used to call the superclass version of an overridden method.

Example:
```java
public class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }

    @Override
    public void makeSound() {
        System.out.println(name + " barks: Woof! Woof!");
    }

    public void makeAnimalSound() {
        super.makeSound(); // Calls the superclass method
    }
}
```

## 2.3.1.3. Abstract classes and methods

Abstract classes are classes that cannot be instantiated and may contain abstract methods (methods without a body). 
They serve as a base for subclasses and enforce certain methods to be implemented by the subclasses.

Key points:
- Use the `abstract` keyword to declare an abstract class or method.
- Abstract classes can have both abstract and non-abstract methods.
- Subclasses of an abstract class must implement all its abstract methods unless the subclass is also declared abstract.
- Abstract classes can have constructors, fields, and non-abstract methods.

Example:
```java
public abstract class Shape {
    protected String color;

    public Shape(String color) {
        this.color = color;
    }

    // Abstract method
    public abstract double calculateArea();

    // Non-abstract method
    public void displayColor() {
        System.out.println("The shape color is " + color);
    }
}

public class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}
```

## Common Pitfalls and How to Avoid Them

1. Overusing inheritance: Prefer composition over inheritance when possible to avoid tight coupling.
2. Forgetting to call the superclass constructor: Always call `super()` in the subclass constructor when needed.
3. Misusing method overriding: Ensure that overridden methods have the same signature as the superclass method.
4. Violating the Liskov Substitution Principle: Subclasses should be substitutable for their base classes without altering the correctness of the program.

## Best Practices and Optimization Techniques

1. Use inheritance to model "is-a" relationships.
2. Keep the inheritance hierarchy shallow to maintain simplicity.
3. Favor composition over inheritance for code reuse when there isn't a clear "is-a" relationship.
4. Use the `final` keyword to prevent further inheritance when appropriate.
5. Override `toString()`, `equals()`, and `hashCode()` methods when necessary.

## Edge Cases and Their Handling

1. Multiple inheritance: Java doesn't support multiple inheritance for classes. Use interfaces to achieve a similar effect.
2. Diamond problem: This issue is avoided in Java due to single inheritance, but can occur with interfaces (solved using default methods in Java 8+).
3. Constructors in abstract classes: Although abstract classes can't be instantiated, they can have constructors to initialize common state for subclasses.

## Interview-specific Insights

- Be prepared to explain the difference between inheritance and composition.
- Understand when to use abstract classes vs. interfaces.
- Be able to discuss the pros and cons of inheritance and its impact on code design.
- Know how to handle common inheritance-related issues like method hiding and constructor chaining.

## QA Section

Q1: What is the difference between method overriding and method overloading?


A1:
Method overriding and method overloading are both forms of polymorphism in Java, but they work differently:

Method Overriding:
- Occurs between a superclass and its subclass.
- The method in the subclass has the same name, return type, and parameter list as the method in the superclass.
- It's used to provide a specific implementation of a method that is already defined in the superclass.
- It's resolved at runtime (dynamic polymorphism).

Method Overloading:
- Occurs within the same class or between a superclass and subclass.
- Methods have the same name but different parameter lists (different number or types of parameters).
- It's used to define multiple methods with the same name but different functionalities.
- It's resolved at compile-time (static polymorphism).

Example of overriding:
```java
class Animal {
    void makeSound() { System.out.println("Animal sound"); }
}
class Dog extends Animal {
    @Override
    void makeSound() { System.out.println("Dog barks"); }
}

Example of overloading:
class Calculator {
    int add(int a, int b) { return a + b; }
    double add(double a, double b) { return a + b; }
}
```


Q2: Can you explain the concept of abstract classes and when to use them?


A2:
Abstract classes in Java are classes that cannot be instantiated and may contain abstract methods (methods without a body).
They are used to define a common interface or partial implementation for a group of related subclasses.

Key characteristics of abstract classes:
1. Declared using the 'abstract' keyword.
2. Can have both abstract and non-abstract (concrete) methods.
3. Can have constructors, instance variables, and static methods.
4. Subclasses must implement all abstract methods unless they are also declared abstract.

When to use abstract classes:
1. When you want to provide a common interface and some shared functionality for a group of related classes.
2. When you have a partial implementation that you want subclasses to complete.
3. When you want to declare non-public members, which is not possible with interfaces.
4. When you need to define a template for a group of subclasses, enforcing certain methods to be implemented.

Example:
```java
abstract class Shape {
    protected String color;
    
    public Shape(String color) {
        this.color = color;
    }
    
    public abstract double calculateArea();
    
    public void displayColor() {
        System.out.println("Color: " + color);
    }
}

class Circle extends Shape {
    private double radius;
    
    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}
```
In this example, Shape is an abstract class that defines a common structure for all shapes, including a color and a method to calculate area. The Circle class extends Shape and provides a specific implementation for calculating its area.



Q3: How does the `super` keyword work in Java, and what are its uses?


A3:
The 'super' keyword in Java is used to refer to the superclass (parent class) of the current class. It has several uses:

1. Calling superclass constructors:
   Used in a subclass constructor to call a constructor of its superclass.
   Example:
```java
   class Animal {
       Animal(String name) { ... }
   }
   class Dog extends Animal {
       Dog(String name) {
           super(name); // Calls Animal constructor
       }
   }
```
2. Accessing superclass methods:
   Used to call a method in the superclass that has been overridden in the subclass.
   Example:
```java
    class Animal {
       void makeSound() { ... }
   }
   class Dog extends Animal {
       @Override
       void makeSound() {
           super.makeSound(); // Calls Animal's makeSound
           System.out.println("Woof");
       }
   }
```

3. Accessing superclass fields:
   Used to access a field in the superclass that might be hidden by a field with the same name in the subclass.
   Example:
```java
    class Animal {
       protected int age = 0;
   }
   class Dog extends Animal {
       private int age = 5;
       void printAge() {
           System.out.println("Dog's age: " + this.age);
           System.out.println("Animal's age: " + super.age);
       }
   }
```

Key points:
- 'super()' must be the first statement in a subclass constructor if used.
- 'super' can't be used in a static context.
- It's useful for maintaining the functionality of the superclass when overriding methods.


## Code Examples

- Test: [ShapeTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/oo/ShapeTest.java)
- Source: [Shape.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Shape.java)
- Source: [Circle.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Circle.java)
- Source: [Rectangle.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Rectangle.java)
