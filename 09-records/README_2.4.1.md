# 2.4.1 Defining and Using Records in Java

## Introduction

Records were introduced in Java 14 as a preview feature and became a standard feature in Java 16. They provide a compact
syntax for declaring classes that are transparent holders for shallowly immutable data.

## Key Points

- Records are immutable data classes
- They automatically generate methods like constructor, getters, equals(), hashCode(), and toString()
- Records can have static fields, methods, and nested classes
- They can implement interfaces but cannot extend other classes

## Defining a Record

To define a record, use the `record` keyword followed by the class name and a list of components (fields) in
parentheses:

```java
public record Person(String name, int age) {}
```

This simple declaration creates a class with two final fields, a constructor, getters, and implementations of equals(),
hashCode(), and toString().

## Using Records

You can create and use record instances just like any other class:

```java
Person person = new Person("Alice", 30);
System.out.println(person.name()); // Prints: Alice
System.out.println(person.age());  // Prints: 30
System.out.println(person);        // Prints: Person[name=Alice, age=30]
```

## Custom Constructors

You can define custom constructors in records:

```java
public record Person(String name, int age) {
    public Person {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
}
```

This is called a compact constructor and allows you to validate or modify the input parameters.

## Additional Methods

You can add static or instance methods to records:

```java
public record Person(String name, int age) {
    public boolean isAdult() {
        return age >= 18;
    }

    public static Person createAdult(String name) {
        return new Person(name, 18);
    }
}
```

## Implementing Interfaces

Records can implement interfaces:

```java
public interface Printable {
    void print();
}

public record Person(String name, int age) implements Printable {
    @Override
    public void print() {
        System.out.println("Person: " + name + ", " + age + " years old");
    }
}
```

## Best Practices

1. Use records for simple data carriers
2. Prefer records over classes for DTOs (Data Transfer Objects)
3. Use custom constructors for input validation
4. Avoid adding mutable fields to records

## Common Pitfalls

1. Trying to extend other classes (records can't extend classes other than Record)
2. Attempting to declare non-static fields outside the record header
3. Forgetting that records are final and can't be inherited

## Interview Insights

- Be prepared to explain the benefits of records over traditional classes
- Understand the limitations of records and when to use them
- Know how to implement custom behavior in records while maintaining their immutability

## References

- [JEP 395: Records](https://openjdk.java.net/jeps/395)
- [Java Records Tutorial](https://docs.oracle.com/en/java/javase/16/language/records.html)

## Interview Q&A

Q1: What is a record in Java, and when was it introduced?
A1: A record in Java is a special kind of class that serves as a transparent carrier for immutable data. It was
introduced as a preview feature in Java 14 and became a standard feature in Java 16. Records provide a concise way to
declare classes that are used to encapsulate data, automatically generating methods like constructors, getters,
equals(), hashCode(), and toString().

Q2: How do you define a simple record in Java?
A2: You can define a simple record in Java using the `record` keyword, followed by the record name and a list of
components (fields) in parentheses. Here's an example:

```java
public record Point(int x, int y) {}
```

Q3: What methods are automatically generated for a record?
A3: Java automatically generates the following methods for a record:

- A constructor with parameters for all components
- Getter methods for each component (named after the component)
- equals() method
- hashCode() method
- toString() method

Q4: Can you add custom methods to a record? If so, how?
A4: Yes, you can add custom methods to a record. You can define both instance and static methods in the record body.
Here's an example:

```java
public record Point(int x, int y) {
    public double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }

    public static Point origin() {
        return new Point(0, 0);
    }
}
```

Q5: How can you perform validation in a record constructor?
A5: You can use a compact constructor to perform validation in a record. The compact constructor doesn't need to
explicitly list the record's components. Here's an example:

```java
public record Person(String name, int age) {
    public Person {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
}
```

Q6: Can records inherit from other classes?
A6: No, records cannot inherit from other classes. They implicitly extend the java.lang.Record class and cannot extend
any other class. However, records can implement interfaces.

Q7: How do you access the fields of a record?
A7: You access the fields of a record using automatically generated getter methods. These methods have the same name as
the field. For example:

```java
Point p = new Point(3, 4);
int x = p.x(); // Not p.getX()
int y = p.y(); // Not p.getY()
```

Q8: Can you have mutable fields in a record?
A8: While the components of a record are implicitly final, you can technically have mutable fields in a record by
declaring them separately from the record header. However, this goes against the design principles of records and should
be avoided. Records are intended to be immutable data carriers.

Q9: How do records differ from regular classes in terms of inheritance?
A9: Records differ from regular classes in several ways regarding inheritance:

1. Records are implicitly final and cannot be extended.
2. Records cannot extend other classes (except java.lang.Record, which they extend implicitly).
3. Records can implement interfaces.

Q10: Can you override the automatically generated methods in a record?
A10: Yes, you can override the automatically generated methods in a record. This includes the equals(), hashCode(), and
toString() methods. However, you should carefully consider whether this is necessary, as the automatically generated
methods are usually sufficient. Here's an example of overriding toString():

```java
public record Person(String name, int age) {
    @Override
    public String toString() {
        return "Person named " + name + " is " + age + " years old";
    }
}
```

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/records/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/records/Person.java)