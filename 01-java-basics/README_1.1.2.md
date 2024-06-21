# 1.1.2. Reference Data Types

Reference data types in Java are complex data types that hold references (memory addresses) to objects rather than the actual data. Unlike primitive types, reference types are created using the `new` keyword (with some exceptions like String literals) and are stored in the heap memory.

## Key Points:
- Reference types are classes, interfaces, arrays, and enums.
- They store addresses pointing to objects in memory.
- Default value is `null`.
- Can be used to create user-defined types.
- Subject to garbage collection when no longer referenced.

## Relevant Java 21 Features:
- Pattern Matching for `instanceof` (JEP 394)
- Record Classes (JEP 395) - a concise way to declare classes that are transparent holders for shallowly immutable data
- Sealed Classes (JEP 409) - restricting which classes may extend or implement them

## Common Pitfalls:
1. Null Pointer Exceptions (NPE)
2. Memory leaks due to lingering references
3. Unintended aliasing (multiple references to the same object)
4. Overuse of inheritance leading to complex hierarchies (mitigated by sealed classes)

## Best Practices:
1. Use null checks or Optional to avoid NPEs
2. Properly manage object lifecycles
3. Understand and use immutability when appropriate
4. Leverage the new record feature for data-centric classes
5. Use sealed classes to create well-defined class hierarchies


## Edge Cases:
1. Circular references
2. Deep vs shallow copying
3. Serialization and deserialization of complex object graphs

## Sealed Classes:
Sealed classes, introduced in Java 17, provide a way to restrict which other classes or interfaces may extend or implement them. This feature allows for more precise control over class hierarchies.

Key aspects of sealed classes:
- Declared using the `sealed` modifier
- Use the `permits` clause to specify allowed subclasses
- Subclasses must be declared as either `final`, `sealed`, or `non-sealed`
- Enhance API design by clearly defining valid subtypes
- Work well with pattern matching in switch expressions

Example of a sealed class hierarchy:

```java
public sealed class Shape permits Circle, Square, Triangle {
   // Common shape properties and methods
}

public final class Circle extends Shape {
   private double radius;
   // Circle-specific implementation
}

public final class Square extends Shape {
   private double side;
   // Square-specific implementation
}

public non-sealed class Triangle extends Shape {
   private double base;
   private double height;
   // Triangle-specific implementation
}
```
Benefits of sealed classes:

- Improved type safety and compiler checks
- Support for exhaustive pattern matching
- Better modeling of domain concepts
- Middle ground between open inheritance and final classes

## Interview Questions:

Q1: What is the difference between primitive data types and reference data types in Java?

A1: Primitive data types and reference data types in Java differ in several important ways:

1. Storage:
   - Primitive types store the actual value directly in memory.
   - Reference types store a reference (memory address) to an object in memory.

2. Memory allocation:
   - Primitive types are stored in the stack memory.
   - Reference types are stored in the heap memory.

3. Default values:
   - Primitive types have default values (e.g., 0 for int, false for boolean).
   - Reference types have a default value of null.

4. Size:
   - Primitive types have a fixed size depending on the type (e.g., int is always 32 bits).
   - Reference types can vary in size and can be much larger.

5. Usage:
   - Primitive types are used for simple values like numbers or boolean flags.
   - Reference types are used for more complex data structures and objects.

6. Performance:
   - Operations on primitive types are generally faster.
   - Reference types involve an extra step of dereferencing, which can impact performance.

7. Nullable:
   - Primitive types cannot be null.
   - Reference types can be null.

8. Methods:
   - Primitive types don't have methods.
   - Reference types (objects) can have methods.

Here's a code example to illustrate some of these differences:

```java
public class DataTypeComparison {
    public static void main(String[] args) {
        // Primitive type
        int primitiveInt = 5;
        
        // Reference type
        Integer referenceInt = new Integer(5);
        
        // Null assignment
        // primitiveInt = null; // This would cause a compilation error
        referenceInt = null; // This is allowed
        
        // Default values
        int defaultPrimitive; // Will be initialized to 0
        Integer defaultReference; // Will be initialized to null
        
        // Method call
        // primitiveInt.toString(); // This would cause a compilation error
        if (referenceInt != null) {
            referenceInt.toString(); // This is allowed and calls a method on the object
        }
    }
}
```
In this example, we can see that:
- The primitive int is directly assigned a value, while the Integer object is created using the new keyword.
- We can assign null to the reference type but not to the primitive type.
- The reference type allows method calls, while the primitive type doesn't.

Understanding these differences is crucial for efficient memory management, performance optimization, and avoiding 
common pitfalls like null pointer exceptions in Java programming.

Q2: How does garbage collection work with reference types in Java?
A2: Garbage collection in Java is an automatic memory management process that deals with reference types. Here's how it works:

Object Creation:
- When an object is created, Java allocates memory for it in the heap.
Reference Counting:
- The JVM keeps track of how many references point to each object.
Unreachable Objects:
- An object becomes eligible for garbage collection when it's no longer reachable from the program's root set (active threads, static fields, etc.).
Garbage Collection Process:
- The garbage collector periodically runs to identify and remove unreachable objects.
- It uses various algorithms (like mark-and-sweep or copying collection) to identify unreachable objects.
- Memory occupied by unreachable objects is reclaimed and can be reused.


Finalization:
- Before reclaiming memory, the garbage collector calls the object's finalize() method if it's implemented.

Here's a simple example to illustrate:
```java
public class GarbageCollectionDemo {
    public static void main(String[] args) {
        createObjects();
        System.gc(); // Request garbage collection (for demonstration purposes)
    }

    static void createObjects() {
        MyObject obj1 = new MyObject("Object 1");
        MyObject obj2 = new MyObject("Object 2");
        
        obj1 = null; // obj1 is now eligible for garbage collection
        // obj2 will be eligible for GC when createObjects() method exits
    }

    static class MyObject {
        private String name;

        MyObject(String name) {
            this.name = name;
            System.out.println(name + " created");
        }

        @Override
        protected void finalize() {
            System.out.println(name + " is being garbage collected");
        }
    }
}
```
In this example:
- We create two MyObject instances.
- We set obj1 to null, making it eligible for garbage collection.
- When createObjects() method exits, obj2 also becomes eligible for GC as it goes out of scope.
- We call System.gc() to suggest a garbage collection (note that in real applications, explicitly calling gc() is generally not recommended).
- The finalize() method (though its use is discouraged in modern Java) demonstrates when objects are being collected.

Key points to remember:
- Garbage collection only deals with heap memory, where reference types are stored.
- It's non-deterministic; you can't predict exactly when it will occur.
- Modern JVMs use sophisticated algorithms to minimize the performance impact of GC.
- Proper management of object references is crucial to assist the garbage collector and prevent memory leaks.


## Code Examples

- Test: [ReferenceTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javabasics/ReferenceTypesTest.java)
- Source: [ReferenceTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/ReferenceTypes.java)