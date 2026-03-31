# 10.3.2. Accessing Fields, Methods, and Constructors

## Concept Explanation

Once you have a `Class` object through introspection, the next step is to access the internals of that class -- its fields, methods, and constructors. The `java.lang.reflect` package provides `Field`, `Method`, and `Constructor` classes that represent these members and allow you to read/write field values, invoke methods, and create instances at runtime, even when those members are private.

**Real-world analogy**: If class introspection is like reading the blueprint of a building (knowing how many rooms, doors, and windows it has), then accessing fields, methods, and constructors is like actually opening doors, turning on lights, and rearranging furniture inside the building -- even if some doors are locked (private), you have a master key (`setAccessible(true)`).

The Reflection API provides two families of accessor methods:

| Family | Scope | Access Level |
|--------|-------|-------------|
| `getFields()`, `getMethods()`, `getConstructors()` | Includes inherited | Public only |
| `getDeclaredFields()`, `getDeclaredMethods()`, `getDeclaredConstructors()` | This class only | All access levels |

Key operations:
- **Field**: `field.get(obj)` reads a value, `field.set(obj, value)` writes a value
- **Method**: `method.invoke(obj, args...)` invokes the method
- **Constructor**: `constructor.newInstance(args...)` creates a new instance

All three require `setAccessible(true)` to bypass Java access control when accessing non-public members.

## Key Points to Remember

- `setAccessible(true)` disables Java language access checking, allowing access to private members.
- `Field.get(null)` and `Field.set(null, value)` work for static fields (pass `null` as the instance).
- `Method.invoke(null, args)` invokes a static method.
- Primitive types are auto-boxed/unboxed when reading/writing fields or invoking methods via reflection.
- `getDeclaredField()` does **not** search superclasses; you must walk the hierarchy manually.
- Modifying `final` fields via reflection is possible but strongly discouraged -- the JVM may have already inlined the value.
- `Constructor.newInstance()` wraps any exception thrown by the constructor in `InvocationTargetException`.
- `Method.invoke()` also wraps checked and unchecked exceptions in `InvocationTargetException`.
- `getParameterTypes()` returns the formal parameter types of a method or constructor.
- Since Java 8, `Parameter.getName()` returns meaningful names only if compiled with `-parameters` flag.

## Relevant Java 21 Features

- **Module access control**: In Java 9+, `setAccessible(true)` is subject to module boundaries. Accessing private members of classes in other modules requires `--add-opens` on the command line or an `opens` directive in `module-info.java`.
- **Record components**: Records expose their components via `getRecordComponents()`, providing a cleaner alternative to field reflection for records.
- **MethodHandles.privateLookupIn()**: A more performant and module-aware alternative to `setAccessible(true)` for trusted access.
- **VarHandle** (Java 9+): Provides fine-grained, type-safe field access with atomic operation support, serving as a modern replacement for some `Field`-based reflection patterns.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to call `setAccessible(true)` for private members**

   ```java
   // WRONG: throws IllegalAccessException
   Field field = Person.class.getDeclaredField("name");
   String name = (String) field.get(person);

   // CORRECT
   Field field = Person.class.getDeclaredField("name");
   field.setAccessible(true);
   String name = (String) field.get(person);
   ```

2. **Using `getField()` to find private fields**

   ```java
   // WRONG: NoSuchFieldException for private field
   Field field = Person.class.getField("name"); // only finds public!

   // CORRECT: use getDeclaredField for private fields
   Field field = Person.class.getDeclaredField("name");
   ```

3. **Not unwrapping `InvocationTargetException`**

   ```java
   // WRONG: catching the wrong exception
   try {
       method.invoke(target, args);
   } catch (RuntimeException e) {
       // Won't catch exceptions from the invoked method!
   }

   // CORRECT: unwrap InvocationTargetException
   try {
       method.invoke(target, args);
   } catch (InvocationTargetException e) {
       Throwable cause = e.getCause(); // The actual exception
       if (cause instanceof RuntimeException re) throw re;
       throw new RuntimeException(cause);
   }
   ```

4. **Confusing primitive and wrapper types in method lookup**

   ```java
   // WRONG: NoSuchMethodException if method takes int, not Integer
   Method m = clazz.getDeclaredMethod("add", Integer.class, Integer.class);

   // CORRECT: use primitive type for primitive parameters
   Method m = clazz.getDeclaredMethod("add", int.class, int.class);
   ```

5. **Modifying final fields and expecting consistent behavior**

   ```java
   // DANGEROUS: JVM may inline final field values
   Field field = ImmutableConfig.class.getDeclaredField("host");
   field.setAccessible(true);
   field.set(config, "newhost"); // May not be visible through getter due to inlining
   ```

## Best Practices and Optimization Techniques

1. **Cache `Field`, `Method`, and `Constructor` objects** -- looking them up is expensive; invoking a cached one is relatively cheap.
2. **Call `setAccessible(true)` once** during initialization, not on every invocation.
3. **Prefer `MethodHandle` or `VarHandle`** over raw reflection for performance-sensitive code.
4. **Always handle `InvocationTargetException`** by unwrapping the cause.
5. **Use `getDeclaredMethod()` with exact parameter types** to avoid ambiguity.
6. **Avoid modifying `final` fields** -- use constructor-based injection or builder patterns instead.
7. **Document reflective access** in your codebase so maintenance developers understand why access controls are bypassed.
8. **Consider module descriptors** when your code must run on the module path; add necessary `opens` directives.

## Edge Cases and Their Handling

1. **Static fields**: Pass `null` as the object instance to `field.get(null)` and `field.set(null, value)`.
2. **Inherited fields**: `getDeclaredField()` does not find inherited fields. Walk the hierarchy:
   ```java
   Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
       Class<?> current = clazz;
       while (current != null) {
           try { return current.getDeclaredField(name); }
           catch (NoSuchFieldException e) { current = current.getSuperclass(); }
       }
       throw new NoSuchFieldException(name);
   }
   ```
3. **Varargs methods**: When invoking a varargs method via reflection, you must pass the varargs as an array.
4. **Null field values**: `field.get(obj)` returns `null` for reference-type fields that have not been initialized. For primitive fields, it returns the zero value (0, false, etc.).
5. **Overloaded methods**: `getDeclaredMethod()` requires exact parameter types. If the method is overloaded, you must specify the correct signature.
6. **Private constructors** (e.g., singleton pattern): `setAccessible(true)` on the constructor lets you bypass the restriction, which is how testing frameworks create instances of such classes.

## Interview-specific Insights

Interviewers focus on:

- Whether you understand the security implications of `setAccessible(true)`.
- Your ability to distinguish between `getDeclaredXxx()` and `getXxx()` families.
- Knowledge of `InvocationTargetException` and proper unwrapping.
- The primitive vs wrapper type issue in method parameter matching.
- Real-world use cases: serialization frameworks (Jackson, Gson), ORM (Hibernate), testing (Mockito), and dependency injection (Spring).
- How the module system affects reflective access in modern Java.

Common tricky interview questions:

- "Can you modify a `private final` field using reflection?" (Answer: Yes technically, but the JVM may inline the value, making the change invisible.)
- "What exception wraps exceptions thrown by a reflectively invoked method?" (Answer: `InvocationTargetException`)
- "How does Spring inject dependencies into private fields?" (Answer: via `Field.setAccessible(true)` and `Field.set()`)

## Interview Q&A Section

**Q1: How do you read and write private fields using reflection?**

```text
A1: To access a private field via reflection:

1. Obtain the Field object using getDeclaredField() (not getField(), which
   only finds public fields).
2. Call field.setAccessible(true) to bypass access control.
3. Use field.get(instance) to read or field.set(instance, value) to write.

For static fields, pass null as the instance argument.

Important caveats:
- In modules (Java 9+), setAccessible may throw InaccessibleObjectException
  if the module does not open the package.
- Modifying final fields is technically possible but unreliable due to JVM
  inlining optimizations.
- This is how frameworks like Hibernate, Jackson, and Spring access private
  fields for ORM mapping, serialization, and dependency injection.
```

```java
public class PrivateFieldAccess {
    private static class Secret {
        private String code = "original";
    }

    public static void main(String[] args) throws Exception {
        Secret secret = new Secret();

        // Read private field
        Field field = Secret.class.getDeclaredField("code");
        field.setAccessible(true);
        String value = (String) field.get(secret);
        System.out.println("Read: " + value); // "original"

        // Write private field
        field.set(secret, "modified");
        System.out.println("After write: " + field.get(secret)); // "modified"
    }
}
```

**Q2: How do you invoke a private method using reflection?**

```text
A2: Invoking a private method via reflection follows a similar pattern:

1. Get the Method object using getDeclaredMethod(name, parameterTypes...).
2. Call method.setAccessible(true).
3. Call method.invoke(instance, arguments...).

The return value is Object (auto-boxed for primitives). For void methods,
invoke() returns null.

If the invoked method throws an exception, it is wrapped in
InvocationTargetException. Always unwrap it to get the actual cause.

For static methods, pass null as the instance.
```

```java
public class PrivateMethodInvocation {
    private static class Calculator {
        private int secretMultiply(int a, int b) {
            return a * b;
        }
    }

    public static void main(String[] args) throws Exception {
        Calculator calc = new Calculator();

        Method method = Calculator.class.getDeclaredMethod(
                "secretMultiply", int.class, int.class);
        method.setAccessible(true);

        int result = (int) method.invoke(calc, 6, 7);
        System.out.println("Result: " + result); // 42

        // Handle exception from invoked method
        try {
            method.invoke(calc, null, null); // Will throw NPE inside
        } catch (InvocationTargetException e) {
            System.out.println("Cause: " + e.getCause().getClass().getSimpleName());
        }
    }
}
```

**Q3: What is the difference between `Constructor.newInstance()` and `Class.newInstance()`?**

```text
A3: Class.newInstance() (deprecated since Java 9, removed in Java 17+):
- Only invokes the no-arg constructor.
- Propagates checked exceptions directly, violating the compile-time checked
  exception mechanism.
- Cannot handle constructors that throw checked exceptions properly.

Constructor.newInstance():
- Can invoke any constructor (with any parameter combination).
- Wraps all exceptions in InvocationTargetException, maintaining proper
  exception handling semantics.
- Works with setAccessible(true) for private constructors.
- Is the recommended approach.

Always prefer Constructor.newInstance() over Class.newInstance().
```

```java
public class ConstructorComparison {
    private static class MyClass {
        private final String name;

        public MyClass() { this.name = "default"; }
        public MyClass(String name) { this.name = name; }
        private MyClass(String name, int id) { this.name = name + "#" + id; }
    }

    public static void main(String[] args) throws Exception {
        // Using Constructor.newInstance() -- RECOMMENDED
        Constructor<MyClass> noArg = MyClass.class.getDeclaredConstructor();
        MyClass obj1 = noArg.newInstance();

        Constructor<MyClass> paramCtor = MyClass.class.getDeclaredConstructor(String.class);
        MyClass obj2 = paramCtor.newInstance("Alice");

        // Even private constructors
        Constructor<MyClass> privateCtor = MyClass.class.getDeclaredConstructor(
                String.class, int.class);
        privateCtor.setAccessible(true);
        MyClass obj3 = privateCtor.newInstance("Bob", 42);
    }
}
```

**Q4: Why is the primitive vs wrapper type distinction important in reflection method lookup?**

```text
A4: When looking up methods or constructors via reflection, you must specify
the EXACT parameter types. Java's auto-boxing does NOT apply during reflective
lookups.

A method declared as: void process(int x)
Must be looked up as: getDeclaredMethod("process", int.class)
NOT as:              getDeclaredMethod("process", Integer.class)

The latter will throw NoSuchMethodException because int.class != Integer.class.

This is a very common source of bugs in reflection code. The same applies to
all primitive/wrapper pairs: boolean/Boolean, byte/Byte, char/Character,
short/Short, long/Long, float/Float, double/Double.

Frameworks that use reflection (like Spring, Jackson) handle this by checking
both primitive and wrapper types when resolving methods.
```

```java
public class PrimitiveVsWrapper {
    public static class MathOps {
        public int add(int a, int b) { return a + b; }
        public Integer addBoxed(Integer a, Integer b) { return a + b; }
    }

    public static void main(String[] args) throws Exception {
        // Primitive parameter types
        Method m1 = MathOps.class.getDeclaredMethod("add", int.class, int.class);
        int result1 = (int) m1.invoke(new MathOps(), 3, 4); // Works: 7

        // Wrapper parameter types
        Method m2 = MathOps.class.getDeclaredMethod("addBoxed", Integer.class, Integer.class);
        int result2 = (int) m2.invoke(new MathOps(), 3, 4); // Auto-boxes args: 7

        // WRONG: mixing them up
        try {
            MathOps.class.getDeclaredMethod("add", Integer.class, Integer.class);
        } catch (NoSuchMethodException e) {
            System.out.println("Cannot find method with wrapper types for primitive params");
        }
    }
}
```

**Q5: How does the Java module system affect reflective access?**

```text
A5: Since Java 9, the module system restricts reflective access:

1. Modules can declare which packages are "exported" (accessible for normal
   compile-time use) and which are "opened" (accessible for deep reflection).

2. setAccessible(true) will throw InaccessibleObjectException if:
   - The target class is in a named module
   - The package is not opened to the calling module
   - You are trying to access non-public members

3. To grant reflective access:
   - Module declaration: opens com.example.pkg to framework.module;
   - Command line: --add-opens com.example.module/com.example.pkg=ALL-UNNAMED
   - Programmatic: Module.addOpens() (requires the caller to be in the module)

4. The "unnamed module" (classpath code) gets reflective access to all open
   packages by default. But accessing JDK internals (like java.lang) requires
   explicit --add-opens.

5. MethodHandles.privateLookupIn() provides a module-aware alternative that
   requires the target module to open its package to the caller.

This is why many frameworks now emit warnings like "illegal reflective access"
and why module-aware alternatives (MethodHandle, VarHandle) are preferred.
```

```java
// module-info.java
module my.app {
    requires my.framework;
    opens com.myapp.model to my.framework; // Allow deep reflection
}

// Runtime flag alternative:
// java --add-opens my.app/com.myapp.model=my.framework -m my.app/com.myapp.Main
```

**Q6: How does Spring Framework use reflection for dependency injection?**

```text
A6: Spring uses reflection extensively for dependency injection:

1. Bean Discovery: Spring scans packages for classes annotated with
   @Component, @Service, @Repository, etc. using reflection and classpath
   scanning.

2. Constructor Injection: Spring inspects constructors using
   Class.getDeclaredConstructors(), finds the appropriate one (annotated
   with @Autowired or the single constructor), and invokes it via
   Constructor.newInstance() with resolved dependencies.

3. Field Injection: For @Autowired fields, Spring uses
   Field.setAccessible(true) followed by Field.set() to inject the
   dependency directly into private fields.

4. Method Injection: For @Autowired setter methods, Spring uses
   Method.invoke() to call the setter with the resolved dependency.

5. Proxy Creation: Spring creates dynamic proxies (using
   java.lang.reflect.Proxy for interfaces or CGLIB for classes) to add
   AOP behavior like @Transactional, @Cacheable, etc.

This is why Spring applications often need --add-opens flags when running
on the module path.
```

```java
// Simplified Spring-like field injection using reflection
public class SimpleDI {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Inject {}

    static class UserService {
        @Inject
        private UserRepository repository;

        public String findUser() {
            return repository.find();
        }
    }

    static class UserRepository {
        public String find() { return "User found"; }
    }

    public static void injectFields(Object target) throws Exception {
        for (Field field : target.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object dependency = field.getType().getDeclaredConstructor().newInstance();
                field.set(target, dependency);
            }
        }
    }
}
```

## Code Examples

- Test: [FieldMethodConstructorAccessTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/FieldMethodConstructorAccessTest.java)
- Source: [FieldMethodConstructorAccess.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/FieldMethodConstructorAccess.java)
