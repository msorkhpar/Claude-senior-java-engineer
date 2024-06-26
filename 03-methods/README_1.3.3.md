# 1.3.3 Pass by Value and Pass by Reference in Java

## Concept Explanation

In Java, understanding how arguments are passed to methods is crucial for writing efficient and bug-free code. Java uses a mechanism called "pass by value" for all its method arguments. However, this can be confusing when dealing with object references.

### Key Points:

1. All arguments in Java are passed by value.
2. For primitives, the value itself is copied.
3. For objects, the reference is copied, but it still points to the same object.
4. Modifying object state within a method affects the original object.
5. Reassigning a reference within a method doesn't affect the original reference.

## Pass by Value for Primitives

When you pass a primitive type to a method, Java creates a copy of the value and passes that copy to the method. Any changes made to the parameter inside the method do not affect the original value outside the method.

## Pass by Value for Objects

When you pass an object to a method, Java creates a copy of the reference to the object and passes that copy to the method. This means:

1. The method receives a copy of the reference, not the original reference.
2. The method can use this reference to modify the object's state.
3. The method cannot change which object the original reference points to.

### Pass by Reference (Sort of)

For objects, Java passes the reference by value. This means that when you pass an object to a method, you're passing a copy of the reference to that object. The method receives its own copy of the reference, but both the original reference and the copy point to the same object in memory.

This behavior can sometimes be mistaken for "pass by reference," but it's important to understand the distinction:
- The method can use the reference to modify the object's state, and these changes will be visible outside the method.
- However, if the method reassigns the reference to a new object, it doesn't affect the original reference outside the method.

## Common Pitfalls

1. Mistaking pass by value of object references for pass by reference.
2. Attempting to reassign an object reference inside a method and expecting it to affect the original reference.

## Best Practices

1. Understand that Java always passes by value, even for object references.
2. Be cautious when modifying objects passed to methods, as these changes will affect the original object.
3. Use return values to communicate changes to primitive types.

## Interview Insights

Interviewers often test understanding of this concept by asking candidates to predict the output of code snippets involving method calls with primitives and objects.


## Tests

Unit tests demonstrating the behavior of pass by value for both primitives and objects can be found in `PassByValueExampleTest.java`.

Now, let's implement the code and tests to demonstrate these concepts.

PassByValueExample.java:

```java
public class PassByValueExample {

    public static void modifyPrimitive(int x) {
        x = 10;
        System.out.println("Inside method: x = " + x);
    }

    public static void modifyObject(StringBuilder sb) {
        sb.append(" World");
        System.out.println("Inside method: sb = " + sb);
    }

    public static void reassignObject(StringBuilder sb) {
        sb = new StringBuilder("New Object");
        System.out.println("Inside reassign method: sb = " + sb);
    }
}
```

PassByValueExampleTest.java:

```java
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PassByValueExampleTest {

    @Test
    void testModifyPrimitive() {
        int x = 5;
        PassByValueExample.modifyPrimitive(x);
        assertThat(x).isEqualTo(5);
    }

    @Test
    void testModifyObject() {
        StringBuilder sb = new StringBuilder("Hello");
        PassByValueExample.modifyObject(sb);
        assertThat(sb.toString()).isEqualTo("Hello World");
    }

    @Test
    void testReassignObject() {
        StringBuilder sb = new StringBuilder("Original");
        PassByValueExample.reassignObject(sb);
        assertThat(sb.toString()).isEqualTo("Original");
    }
}
```

Now, let's go through the Q&A section to reinforce these concepts:

Q1: What is the difference between pass by value and pass by reference?

A1:
```
Pass by value means that a copy of the value is passed to the method. Changes to the parameter inside the method do not affect the original value.

Pass by reference means that a reference to the actual variable is passed to the method. Changes to the parameter inside the method affect the original variable.

Java always uses pass by value, but for objects, it passes the value of the reference to the object, which can lead to confusion.
```

Q2: In Java, is it possible to swap two integer values using a method? Why or why not?

A2:
```
No, it's not possible to swap two integer values using a method in Java without returning a new value or using a wrapper class. This is because Java uses pass by value for primitives. When you pass integers to a method, copies of their values are passed, not references to the original variables. Any changes made to these copies inside the method do not affect the original variables.

Here's an example to illustrate this:

public static void swap(int a, int b) {
    int temp = a;
    a = b;
    b = temp;
}

int x = 5;
int y = 10;
swap(x, y);
// x is still 5, y is still 10

To achieve a swap, you would need to return new values or use a wrapper class or array.
```

Q3: How does pass by value work with object references in Java?

A3:
```
In Java, when you pass an object to a method, you're actually passing the value of the reference to that object. This means:

1. The method receives a copy of the reference, not the original reference itself.
2. The method can use this reference to access and modify the object's state.
3. The method cannot change which object the original reference points to.

For example:

public static void modifyObject(StringBuilder sb) {
    sb.append(" World"); // This modifies the original object
    sb = new StringBuilder("New"); // This only affects the local copy of the reference
}

StringBuilder str = new StringBuilder("Hello");
modifyObject(str);
// str now contains "Hello World", not "New"

The method can modify the object's state (appending " World"), but reassigning the reference (sb = new StringBuilder("New")) only affects the local copy, not the original reference.
```

Q4: What will be the output of the following code?

```java
public class Test {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3};
        changeArray(arr);
        System.out.println(arr[0]);
    }

    public static void changeArray(int[] a) {
        a[0] = 10;
        a = new int[]{4, 5, 6};
    }
}
```

A4:
```
The output will be 10.

Here's why:
1. The array {1, 2, 3} is created and its reference is stored in arr.
2. This reference is passed by value to the changeArray method.
3. a[0] = 10 modifies the original array, changing its first element to 10.
4. a = new int[]{4, 5, 6} creates a new array and assigns it to the local variable a. This doesn't affect the original arr in main.
5. When we print arr[0], we see the modified value 10.

This example demonstrates that while we can modify the contents of the array (because we have a copy of the reference to the same array), we cannot change what the original reference points to.
```

These examples and explanations should provide a comprehensive understanding of pass by value in Java, both for primitives and object references. Remember, the key is to understand that Java always passes by value, but the value being passed for objects is a copy of the reference to the object.

Process finished with exit code 0


## Code Examples

- Test: [PassByValueTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/PassByValueTest.java)
- Source: [PassByValue.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/PassByValue.java)

This example demonstrates that:
1. Changes to the primitive `primitive` inside the method don't affect `number`.
2. Changes to the object's state (`object.append()`) are reflected in `text`.
3. Reassigning `object` inside the method doesn't affect `text`.

These tests demonstrate:
1. Primitives are passed by value, and modifications inside methods don't affect the original value.
2. Objects (like StringBuilder) are passed by value of the reference, allowing modifications to the object's state.
3. Reassigning object references inside methods doesn't affect the original reference.
4. Arrays, being objects, behave similarly to other objects.
5. Strings and wrapper classes (like Integer) behave like primitives due to their immutability.

This implementation and test suite cover the core concepts of pass by value in Java, including common edge cases and
potential misconceptions. It provides a solid foundation for understanding and explaining this important Java concept
in an interview setting.
