# 10.3.1. Class and Object Introspection

## Concept Explanation

Class and object introspection is the ability to examine the structure, properties, and metadata of classes and objects at runtime. Through the Java Reflection API (primarily in `java.lang.reflect`), a program can discover a class's name, superclass, interfaces, fields, methods, constructors, modifiers, and annotations without knowing any of this information at compile time.

**Real-world analogy**: Imagine you receive a sealed box with no label. Introspection is like using an X-ray machine to examine its contents, discover what compartments it has, what materials it is made of, and how those compartments are connected -- all without opening the box or knowing what was put inside at the factory.

At the heart of reflection is the `java.lang.Class` object. Every loaded type in the JVM has exactly one `Class` instance. You can obtain it three ways:

1. **`object.getClass()`** -- from any instance at runtime
2. **`TypeName.class`** -- a compile-time class literal
3. **`Class.forName("fully.qualified.Name")`** -- loading a class dynamically by name

Once you have a `Class` object, you can query the full class hierarchy (superclasses, interfaces), modifiers (public, abstract, final, static), whether it is an interface, enum, record, array, or primitive, and enumerate its declared fields, methods, and constructors.

## Key Points to Remember

- Every type in Java (including primitives, arrays, and `void`) has a corresponding `Class` object.
- `Class.forName()` triggers class loading and static initialization; the other two approaches do not trigger loading of a new class.
- `getInterfaces()` returns only directly declared interfaces; to get all inherited interfaces you must walk the hierarchy.
- `getDeclaredFields()`/`getDeclaredMethods()` return members declared in that class only (including private), while `getFields()`/`getMethods()` return all public members including inherited ones.
- `getModifiers()` returns an `int` bitmask; use `java.lang.reflect.Modifier` helper methods to decode it.
- `isInstance(obj)` is the reflective equivalent of the `instanceof` operator.
- `getComponentType()` returns the element type of an array class, or `null` for non-array types.
- `getSuperclass()` returns `null` for `Object`, interfaces, primitives, and `void`.

## Relevant Java 21 Features

- **Records** (`clazz.isRecord()`, `clazz.getRecordComponents()`): Java 16+ introduced first-class record support in the reflection API, letting you discover record component names and types.
- **Sealed classes** (`clazz.isSealed()`, `clazz.getPermittedSubclasses()`): Java 17+ added methods to inspect sealed class hierarchies reflectively.
- **Pattern matching for `instanceof`**: While not a reflection feature per se, it works alongside reflection when you need runtime type narrowing combined with compile-time pattern matching.
- **Module system** (`clazz.getModule()`): Since Java 9, the module system gates reflective access; `--add-opens` may be needed for deep reflection into JDK internals.

## Common Pitfalls and How to Avoid Them

1. **Confusing `getDeclaredFields()` with `getFields()`**

   `getFields()` only returns **public** fields (including inherited ones). `getDeclaredFields()` returns all fields declared in the class itself, regardless of access modifier, but does **not** include inherited fields.

   ```java
   // WRONG: Expecting to see private fields
   Field[] fields = clazz.getFields(); // Only public!

   // CORRECT: To see all fields of a class including private
   Field[] fields = clazz.getDeclaredFields();
   ```

2. **Forgetting that `Class.forName()` throws a checked exception**

   ```java
   // WRONG: Uncaught checked exception
   Class<?> c = Class.forName("com.example.MyClass");

   // CORRECT: Handle ClassNotFoundException
   try {
       Class<?> c = Class.forName("com.example.MyClass");
   } catch (ClassNotFoundException e) {
       // handle gracefully
   }
   ```

3. **Assuming `getSuperclass()` always returns a class**

   For interfaces, primitives, and `Object` itself, `getSuperclass()` returns `null`.

   ```java
   Class<?> superclass = Serializable.class.getSuperclass(); // null!
   Class<?> primSuper = int.class.getSuperclass();            // null!
   ```

4. **Not walking the full hierarchy for interface discovery**

   `getInterfaces()` only returns directly declared interfaces. To find all interfaces (including those inherited from superclasses), you must traverse the class hierarchy recursively.

5. **Module access restrictions in Java 9+**

   Reflective access to non-exported packages in named modules will throw `IllegalAccessException` at runtime. Use `--add-opens` JVM flags or module declarations to grant access.

## Best Practices and Optimization Techniques

1. **Cache `Class` objects** -- they are singletons per classloader and safe to cache.
2. **Prefer `instanceof` / pattern matching over reflective type checks** when the type is known at compile time.
3. **Use `Class.isInstance(obj)` instead of manual `getClass()` comparisons** for polymorphic checks.
4. **Walk the hierarchy only once** and cache the result if you need all interfaces or all fields including inherited ones.
5. **Avoid `Class.forName()` in hot loops** -- it involves classloader lookups and synchronization.
6. **Use `getSimpleName()` for logging** and `getName()` when you need the canonical form for serialization or persistence.

## Edge Cases and Their Handling

1. **Primitive types**: `int.class` is a valid `Class` object but `isPrimitive()` is `true`, it has no fields, no methods, and `getSuperclass()` returns `null`.
2. **Array types**: `int[].class` has `isArray() == true` and `getComponentType() == int.class`. Multi-dimensional arrays are arrays of arrays.
3. **Anonymous classes**: Have an empty simple name (`getSimpleName()` returns `""`).
4. **Lambda classes**: Synthetic classes with `isSynthetic() == true`.
5. **`void.class`**: A valid `Class` object with `isPrimitive() == true`.
6. **Null input**: Always guard against `null` before calling `getClass()` -- it will throw `NullPointerException`.

## Interview-specific Insights

Interviewers commonly probe:

- Whether you understand the three ways to obtain a `Class` object and the semantic differences between them.
- The distinction between `getDeclaredXxx()` and `getXxx()` families of methods.
- How the class hierarchy traversal works and why `getInterfaces()` is shallow.
- Real-world scenarios: plugin systems, serialization frameworks, ORM mapping, and dependency injection all rely on introspection.
- Module system restrictions and how they affect reflective access.

Tricky questions to expect:

- "What does `int.class == Integer.TYPE` evaluate to?" (Answer: `true`)
- "Can you get a `Class` object for `void`?" (Answer: yes, `void.class` or `Void.TYPE`)
- "Does `Class.forName()` call the static initializer?" (Answer: yes, by default)

## Interview Q&A Section

**Q1: What are the three ways to obtain a Class object in Java, and how do they differ?**

```text
A1: The three ways are:

1. object.getClass() -- Called on an instance at runtime. Returns the actual runtime
   class, which may be a subclass of the declared type. Requires a non-null instance.

2. TypeName.class -- A compile-time class literal. Does not require an instance, does not
   trigger class loading (the class is already loaded when the literal is compiled), and
   works for primitives (int.class) and void (void.class).

3. Class.forName("fully.qualified.Name") -- Loads the class dynamically by its
   fully-qualified name at runtime. Triggers static initialization by default. Throws
   ClassNotFoundException if the class cannot be found.

Key differences:
- .getClass() gives the runtime type, which may differ from the declared type due to polymorphism.
- .class is resolved at compile time and is the most efficient.
- Class.forName() is the most dynamic but also the slowest and can fail at runtime.
```

```java
// Example demonstrating all three approaches
public class ClassObjectDemo {
    public static void main(String[] args) throws ClassNotFoundException {
        // 1. From an instance
        String str = "Hello";
        Class<?> c1 = str.getClass(); // java.lang.String

        // 2. Class literal
        Class<?> c2 = String.class;

        // 3. Dynamic loading
        Class<?> c3 = Class.forName("java.lang.String");

        // All three refer to the same Class object
        assert c1 == c2 && c2 == c3; // true
    }
}
```

**Q2: What is the difference between `getDeclaredFields()` and `getFields()`?**

```text
A2: These methods differ in two important dimensions -- visibility and inheritance:

getDeclaredFields():
- Returns ALL fields declared in the class itself (public, protected, default, private).
- Does NOT include inherited fields from superclasses.
- Use this when you need to inspect the class's own structure.

getFields():
- Returns ONLY public fields.
- INCLUDES inherited public fields from superclasses and interfaces.
- Use this when you want to discover the publicly accessible field API.

The same pattern applies to getMethods()/getDeclaredMethods() and
getConstructors()/getDeclaredConstructors().

To get all fields (including private inherited fields), you must walk the
class hierarchy:

  Class<?> current = clazz;
  while (current != null) {
      Field[] fields = current.getDeclaredFields();
      // process fields...
      current = current.getSuperclass();
  }
```

```java
public class FieldDiscoveryDemo {
    static class Parent {
        public String publicField = "public";
        private String privateField = "private";
    }

    static class Child extends Parent {
        public int childPublic = 1;
        private int childPrivate = 2;
    }

    public static void main(String[] args) {
        // getDeclaredFields -- own fields only, all access levels
        Field[] declared = Child.class.getDeclaredFields();
        // [childPublic, childPrivate]

        // getFields -- public fields, including inherited
        Field[] publicFields = Child.class.getFields();
        // [childPublic, publicField]
    }
}
```

**Q3: How do you traverse the complete class hierarchy of a class?**

```text
A3: Java does not provide a single method to get the complete hierarchy. You must
walk up the chain using getSuperclass() in a loop:

1. Start with the target class.
2. Call getSuperclass() to get the parent.
3. Repeat until getSuperclass() returns null (reached Object or an interface/primitive).

For interfaces, you also need getInterfaces() at each level, and interfaces
themselves can extend other interfaces, so this becomes a recursive traversal
(essentially a graph traversal since a class can implement multiple interfaces
and interfaces can extend multiple interfaces).

Important: getSuperclass() returns null for:
- java.lang.Object
- Interfaces
- Primitives (int.class, etc.)
- void.class
```

```java
public class HierarchyTraversal {
    public static List<Class<?>> getFullHierarchy(Class<?> clazz) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        return hierarchy;
    }

    public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectInterfaces(clazz, interfaces);
        return interfaces;
    }

    private static void collectInterfaces(Class<?> clazz, Set<Class<?>> result) {
        if (clazz == null) return;
        for (Class<?> iface : clazz.getInterfaces()) {
            result.add(iface);
            collectInterfaces(iface, result); // interfaces can extend interfaces
        }
        collectInterfaces(clazz.getSuperclass(), result);
    }
}
```

**Q4: What does `Class.isInstance(obj)` do and how does it differ from `instanceof`?**

```text
A4: Class.isInstance(obj) is the dynamic equivalent of the instanceof operator.

Similarities:
- Both check whether an object is an instance of a given type.
- Both handle null safely (return false for null).
- Both consider the full type hierarchy (subclasses, implemented interfaces).

Differences:
- instanceof is a compile-time operator; the type on the right must be known at
  compile time.
- isInstance() is a runtime method; the type (Class object) can be determined
  dynamically at runtime.

Use isInstance() when:
- The type to check against is not known at compile time.
- You are writing generic reflection-based code (e.g., a framework, serializer).

Use instanceof (with pattern matching in Java 21) when:
- The type is known at compile time.
- You want type narrowing with pattern variables.
```

```java
public class InstanceCheckDemo {
    public static void main(String[] args) {
        Object obj = "Hello";

        // Compile-time instanceof
        if (obj instanceof String s) {
            System.out.println("Length: " + s.length());
        }

        // Dynamic isInstance -- type determined at runtime
        Class<?> targetType = String.class; // could come from configuration
        if (targetType.isInstance(obj)) {
            System.out.println("Object is a " + targetType.getSimpleName());
        }

        // Null handling
        System.out.println(String.class.isInstance(null)); // false
    }
}
```

**Q5: How do records and sealed classes interact with the Reflection API?**

```text
A5: Java 16+ and 17+ added reflection support for records and sealed classes:

Records (Java 16+):
- Class.isRecord() returns true for record types.
- Class.getRecordComponents() returns an array of RecordComponent objects
  describing each component (name, type, accessor method, annotations).
- Record components maintain their declaration order.
- Records always have a canonical constructor whose parameters match the components.

Sealed classes (Java 17+):
- Class.isSealed() returns true for sealed types.
- Class.getPermittedSubclasses() returns an array of Class objects representing
  the permitted subtypes.
- This is valuable for building exhaustive pattern matching or framework code
  that needs to discover all subtypes of a sealed hierarchy.

Both features enhance the reflection API by making the JVM metadata richer and
more queryable. Frameworks like Jackson, Hibernate, and Spring leverage these
for automatic mapping, serialization, and dependency injection.
```

```java
public class RecordSealedReflection {
    record Point(int x, int y) {}

    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}

    public static void main(String[] args) {
        // Record introspection
        System.out.println(Point.class.isRecord()); // true
        var components = Point.class.getRecordComponents();
        for (var comp : components) {
            System.out.println(comp.getName() + ": " + comp.getType().getSimpleName());
        }
        // Output: x: int, y: int

        // Sealed class introspection
        System.out.println(Shape.class.isSealed()); // true
        Class<?>[] permitted = Shape.class.getPermittedSubclasses();
        for (var sub : permitted) {
            System.out.println("Permitted: " + sub.getSimpleName());
        }
        // Output: Permitted: Circle, Permitted: Rectangle
    }
}
```

**Q6: What happens when you call `Class.forName()` with a class that has a static initializer?**

```text
A6: By default, Class.forName(String) loads the class AND runs its static
initializer (static blocks and static field assignments). This is the
"initialize" variant.

There is an overloaded version:
  Class.forName(String name, boolean initialize, ClassLoader loader)

If you pass initialize=false, the class is loaded but NOT initialized. The
static initializer will run later, when the class is first actively used.

This distinction matters for:
1. Side effects in static initializers (database connections, logging, registrations).
2. Performance: avoiding unnecessary initialization.
3. JDBC: Class.forName("com.mysql.cj.jdbc.Driver") traditionally relied on
   the static initializer to register the driver with DriverManager.

In modern Java (JDBC 4.0+), drivers use ServiceLoader, so explicit Class.forName()
is no longer needed for JDBC drivers.
```

```java
public class ForNameDemo {
    static class Eager {
        static {
            System.out.println("Eager initialized!");
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        // Triggers static initializer
        Class.forName("ForNameDemo$Eager");
        // Output: "Eager initialized!"

        // Does NOT trigger static initializer
        Class.forName("ForNameDemo$Eager", false,
                ForNameDemo.class.getClassLoader());
        // No output (already initialized in this case, but for a fresh class it would defer)
    }
}
```

## Code Examples

- Test: [ClassIntrospectionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/ClassIntrospectionTest.java)
- Source: [ClassIntrospection.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/ClassIntrospection.java)
