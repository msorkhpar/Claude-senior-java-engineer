# 2.1.1 Class Structure and Components

## Detailed Concept Explanation

In Java, a class is a blueprint for creating objects. It defines the structure and behavior that its objects will have.
Understanding the structure and components of a class is fundamental to object-oriented programming in Java.

A typical Java class consists of the following components:

1. **Class Declaration**: Defines the name of the class and its access modifier.
2. **Fields (Instance Variables)**: Represent the state or properties of the class.
3. **Constructors**: Special methods used to initialize objects of the class.
4. **Methods**: Define the behavior or actions that objects of the class can perform.
5. **Nested Classes and Interfaces**: Classes or interfaces defined within another class.

## Key Points to Remember

- Classes are declared using the `class` keyword.
- The name of the class should match the filename (for public classes).
- Access modifiers (public, protected, default, private) control the visibility of class members.
- Instance variables represent the state of an object.
- Methods define the behavior of objects.
- Constructors have the same name as the class and are used to initialize objects.

## Relevant Java 21 Features

- Records: A compact way to declare classes that are primarily used to store data.
- Sealed Classes: Allow restricting which other classes or interfaces may extend or implement them.

## Common Pitfalls and How to Avoid Them

1. **Overusing public fields**: Encapsulate fields by making them private and providing public getter and setter
   methods.
2. **Forgetting to initialize fields**: Always initialize fields, either at declaration or in constructors.
3. **Not following naming conventions**: Use PascalCase for class names and camelCase for method and variable names.

## Best Practices and Optimization Techniques

1. Follow the Single Responsibility Principle: A class should have only one reason to change.
2. Use meaningful and descriptive names for classes, methods, and variables.
3. Keep classes small and focused on a single task or concept.
4. Use appropriate access modifiers to encapsulate internal details.
5. Favor composition over inheritance when designing class relationships.

## Edge Cases and Their Handling

1. **Circular Dependencies**: Avoid circular dependencies between classes. Use interfaces or redesign the class
   structure if necessary.
2. **Deep Inheritance Hierarchies**: Limit inheritance depth to maintain code clarity and reduce complexity.

## Interview-specific Insights

- Be prepared to explain the difference between instance and static members.
- Understand the importance of encapsulation and how it's achieved in Java.
- Be able to discuss the pros and cons of using nested classes.

Q1: What are the main components of a Java class?

```text
A: The main components of a Java class are:
1. Class declaration
2. Fields (instance variables)
3. Constructors
4. Methods
5. Nested classes and interfaces (optional)

Each component plays a specific role in defining the structure and behavior of objects created from the class.
```

Q2: How would you implement a simple immutable class in Java?

```java
public final class ImmutablePerson {
    private final String name;
    private final int age;

    public ImmutablePerson(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

Q3: What is the difference between instance and static members of a class?

```text
A: Instance members are associated with specific instances (objects) of a class, while static members are associated with the class itself.

Instance members:
- Accessed through object references
- Can access both instance and static members
- Represent the state and behavior of individual objects

Static members:
- Accessed through the class name
- Can only directly access other static members
- Represent shared state and behavior across all instances of the class
- Exist even if no objects of the class have been created
```

Q4: How would you implement a singleton class in Java?

```java
public class Singleton {
    private static Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

Q5: What is the purpose of the `final` keyword when applied to a class, method, or variable?

```text
A: The `final` keyword has different effects depending on where it's used:

1. On a class: The class cannot be inherited (subclassed).
2. On a method: The method cannot be overridden in subclasses.
3. On a variable:
   - For primitives: The value cannot be changed after initialization.
   - For objects: The reference cannot be changed to point to a different object, but the object's internal state can still be modified if it's mutable.

Using `final` can help in creating immutable classes, preventing unintended overriding, and optimizing performance in certain cases.
```

These Q&A examples cover important aspects of class structure and components, including immutability, static vs
instance members, singleton pattern, and the use of the `final` keyword. They provide a good foundation for
understanding and discussing Java class design in an interview setting.

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/Person.java)
