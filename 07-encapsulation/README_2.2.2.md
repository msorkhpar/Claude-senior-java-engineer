# 2.2.2 Getter and Setter Methods in Java

## Concept Explanation

Getter and setter methods, also known as accessor and mutator methods, are a fundamental aspect of encapsulation in
Java. They provide controlled access to the private fields of a class, allowing you to implement data hiding while still
providing a way to read and modify the object's state.

- **Getter methods** (accessors) retrieve the value of a private field.
- **Setter methods** (mutators) set or update the value of a private field.

## Key Points to Remember

1. Getters and setters provide controlled access to private fields.
2. They allow you to implement validation logic when setting values.
3. They enable you to change the internal representation of data without affecting the public interface.
4. Naming conventions: getters start with "get" (or "is" for boolean), setters start with "set".
5. They are part of the JavaBeans specification, which is important for many Java frameworks.

## Relevant Java 21 Features

- Record classes (introduced in Java 14, standard in Java 16+) provide a concise way to create immutable data classes
  with implicit getters.

## Common Pitfalls and How to Avoid Them

1. **Redundant getters and setters**: Avoid creating them for all fields without considering their necessity.
2. **Lack of validation in setters**: Always validate input in setters to maintain object integrity.
3. **Returning mutable objects**: Be cautious when returning mutable objects from getters to prevent unintended
   modifications.
4. **Performance overhead**: In performance-critical code, direct field access might be preferred.

## Best Practices and Optimization Techniques

1. Use meaningful names that reflect the field's purpose.
2. Implement validation logic in setters to ensure data integrity.
3. Consider making fields final and providing only getters for immutable objects.
4. Use the @Getter and @Setter annotations from Project Lombok to reduce boilerplate code (in appropriate scenarios).
5. For collections, consider returning unmodifiable views in getters.

## Edge Cases and Their Handling

1. Null values: Decide on a policy for handling null (reject, accept, or provide a default).
2. Immutable fields: Provide only getters for fields that shouldn't change after object creation.
3. Derived properties: Implement getters for properties that are calculated from other fields.

## Interview-specific Insights

- Be prepared to explain the benefits of using getters and setters over public fields.
- Understand how getters and setters relate to encapsulation and data hiding.
- Know how to implement thread-safe getters and setters in concurrent environments.

## Interview Q&A Section

Q1: Why use getter and setter methods instead of public fields?

```text
A1: Getter and setter methods provide several advantages over public fields:

1. Encapsulation: They allow you to hide the internal representation of an object's state.
2. Validation: Setters can include logic to validate input before setting the field.
3. Flexibility: You can change the internal implementation without affecting the public interface.
4. Access control: You can make a field read-only by providing only a getter.
5. Derived properties: You can implement getters for properties that are calculated on-the-fly.
6. Thread safety: In multi-threaded environments, you can implement synchronized access.
7. Debugging: You can set breakpoints in getters/setters to track access and modifications.

Using public fields would negate these benefits and violate the principle of encapsulation.
```

Q2: How would you implement a thread-safe getter and setter for a counter?

```java
public class ThreadSafeCounter {
    private volatile int count = 0;

    public synchronized void incrementCount() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
```

Q3: How can you prevent returning mutable objects from getters?

```java
public class Person {
    private List<String> hobbies = new ArrayList<>();

    public List<String> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }

    public void addHobby(String hobby) {
        hobbies.add(hobby);
    }
}
```

Q4: What is the difference between a getter that returns a primitive and one that returns an object?

```text
A4: The main differences are:

1. Primitives are passed by value, while objects are passed by reference.
2. Returning a primitive always gives a copy of the value, so the caller can't modify the original.
3. Returning an object gives a reference to the original object, potentially allowing modification unless precautions are taken (like returning an immutable view or a defensive copy).
4. Primitive getters don't need to handle null values, while object getters might need to consider null checks.
5. Performance-wise, returning primitives is generally faster as it doesn't involve object creation or reference handling.
```

Example:

```java
public class Example {
    private int age;  // primitive
    private Date birthDate;  // object

    public int getAge() {
        return age;  // Safe, returns a copy of the value
    }

    public Date getBirthDate() {
        return new Date(birthDate.getTime());  // Returns a defensive copy
    }
}
```

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/encapsulation/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/encapsulation/Person.java)
