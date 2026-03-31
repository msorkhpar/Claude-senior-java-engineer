# 10.3.3. Dynamic Object Creation and Method Invocation

## Concept Explanation

Dynamic object creation and method invocation take reflection beyond mere inspection into active use. Instead of calling constructors and methods at compile time with known types, you can create objects and invoke methods entirely at runtime, driven by configuration, user input, or discovery. This is the foundation of plugin architectures, dependency injection containers, ORM frameworks, serialization libraries, and remote method invocation systems.

**Real-world analogy**: Imagine a universal remote control that can operate any electronic device -- you do not need to know the brand or model at the time the remote is manufactured. You simply point it at a device, discover its capabilities at runtime (what buttons it responds to), and press the appropriate ones. Dynamic reflection works the same way: your code does not need compile-time knowledge of the class it will create or the method it will call.

The key mechanisms are:

1. **Dynamic instantiation**: `Constructor.newInstance(args...)` or `Class.forName("...").getDeclaredConstructor().newInstance()`
2. **Dynamic method invocation**: `Method.invoke(target, args...)`
3. **Dynamic array creation**: `java.lang.reflect.Array.newInstance(componentType, length)`
4. **Dynamic proxies**: `java.lang.reflect.Proxy.newProxyInstance(loader, interfaces, handler)` creates an implementation of one or more interfaces at runtime

## Key Points to Remember

- `Class.forName()` combined with `Constructor.newInstance()` is the standard pattern for creating objects from configuration strings.
- `Method.invoke()` auto-boxes primitives and unwraps return values. Exceptions thrown by the target method are wrapped in `InvocationTargetException`.
- The `java.lang.reflect.Array` class provides static methods for creating and manipulating arrays reflectively.
- `java.lang.reflect.Proxy` creates dynamic proxy instances that implement specified interfaces and delegate all method calls to an `InvocationHandler`.
- Dynamic proxies only work with **interfaces**, not concrete classes. For class-based proxies, use CGLIB or Byte Buddy.
- The `InvocationHandler.invoke()` method receives the proxy instance, the `Method` object, and the arguments array. For methods with no arguments, the args array is `null`.
- Factory patterns and service locators commonly use reflection for decoupled, configurable object creation.
- Dependency injection containers resolve and wire dependencies at runtime using reflection.

## Relevant Java 21 Features

- **`MethodHandles.lookup()`**: A more performant alternative to `Method.invoke()` for dynamic invocation.
- **Virtual threads**: Dynamic proxy handlers can leverage virtual threads for non-blocking invocation.
- **`ServiceLoader`**: The preferred mechanism for plugin discovery in modular Java, reducing the need for raw `Class.forName()`.
- **Record constructors**: Records have a canonical constructor that can be discovered and invoked reflectively via `getRecordComponents()`.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to handle `InvocationTargetException` in `Method.invoke()`**

   ```java
   // WRONG: Losing the actual exception
   try {
       method.invoke(target, args);
   } catch (Exception e) {
       throw e; // This throws InvocationTargetException, not the real exception
   }

   // CORRECT: Unwrap the cause
   try {
       method.invoke(target, args);
   } catch (InvocationTargetException e) {
       throw new RuntimeException(e.getCause());
   }
   ```

2. **Passing `null` for zero-argument varargs in dynamic proxy handlers**

   ```java
   // In InvocationHandler.invoke(), args is null (not empty array) for zero-arg methods
   @Override
   public Object invoke(Object proxy, Method method, Object[] args) {
       // WRONG: args.length will throw NPE for zero-arg methods
       // CORRECT:
       int argCount = (args != null) ? args.length : 0;
   }
   ```

3. **Using `Proxy.newProxyInstance()` with a concrete class**

   ```java
   // WRONG: IllegalArgumentException - concrete class is not an interface
   Proxy.newProxyInstance(loader, new Class<?>[]{ArrayList.class}, handler);

   // CORRECT: Use interfaces only
   Proxy.newProxyInstance(loader, new Class<?>[]{List.class}, handler);
   ```

4. **Type mismatch when setting array elements reflectively**

   ```java
   Object arr = Array.newInstance(int.class, 5);
   // WRONG: passing a String to an int array
   // Array.set(arr, 0, "hello"); // throws IllegalArgumentException

   // CORRECT:
   Array.set(arr, 0, 42);
   ```

5. **Not checking for the correct constructor parameter types**

   ```java
   // WRONG: NoSuchMethodException if constructor takes (String) not (Object)
   clazz.getDeclaredConstructor(Object.class);

   // CORRECT: Match the exact declared types
   clazz.getDeclaredConstructor(String.class);
   ```

## Best Practices and Optimization Techniques

1. **Use factory patterns** to encapsulate reflective creation and provide type safety to callers.
2. **Register class mappings** (key -> Class) upfront instead of calling `Class.forName()` repeatedly.
3. **Cache `Constructor` and `Method` objects** -- the lookup is expensive, the invocation is not.
4. **Consider `ServiceLoader`** instead of raw `Class.forName()` for plugin architectures.
5. **Use dynamic proxies judiciously** -- they add a method call overhead per invocation. For hot paths, consider compile-time code generation.
6. **Log proxy method calls** only when debugging; remove or disable in production for performance.
7. **Validate configuration** early (e.g., at startup) rather than failing at runtime when `Class.forName()` fails.
8. **Prefer `MethodHandle`** over `Method.invoke()` for performance-critical dynamic invocation.

## Edge Cases and Their Handling

1. **Abstract classes and interfaces**: Cannot be instantiated directly. `Constructor.newInstance()` will throw `InstantiationException`.
2. **Enum classes**: Constructors are private and reflective instantiation is explicitly blocked by the JVM; `Constructor.newInstance()` throws `IllegalArgumentException`.
3. **Zero-length arrays**: `Array.newInstance(type, 0)` is valid and creates an empty array.
4. **Null elements in arrays**: `Array.set(arr, 0, null)` is valid for reference-type arrays but throws `IllegalArgumentException` for primitive arrays.
5. **Proxy equality**: `proxy.equals(proxy)` delegates to the `InvocationHandler`, so you must handle `equals`, `hashCode`, and `toString` explicitly in your handler.
6. **Circular dependencies in DI**: A simple reflective DI container can enter infinite recursion. Guard with a "currently creating" set.

## Interview-specific Insights

Interviewers focus on:

- Your understanding of when and why to use dynamic object creation (plugin systems, frameworks).
- The dynamic proxy pattern: how `Proxy.newProxyInstance()` works and its limitations (interfaces only).
- How AOP (Aspect-Oriented Programming) frameworks implement cross-cutting concerns using proxies.
- Factory pattern implementation using reflection vs hard-coded switch statements.
- The trade-offs between flexibility (reflection) and type safety / performance (compile-time).

Common tricky questions:

- "How does `Proxy.newProxyInstance()` work internally?" (It generates a class at runtime that implements the given interfaces and delegates to the handler.)
- "Can you create a proxy for a concrete class?" (Not with `java.lang.reflect.Proxy`; you need CGLIB, Byte Buddy, or similar.)
- "What happens if the `InvocationHandler` returns an incompatible type?" (A `ClassCastException` is thrown at the call site.)

## Interview Q&A Section

**Q1: How do you create an object dynamically from a class name string?**

```text
A1: The standard pattern is:

1. Load the class: Class<?> clazz = Class.forName("com.example.MyClass");
2. Get the constructor: Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
3. Make accessible if needed: ctor.setAccessible(true);
4. Create the instance: Object obj = ctor.newInstance(args);

This pattern is used extensively in:
- Plugin systems that load implementations from configuration
- JDBC driver loading (historically)
- Serialization/deserialization frameworks
- Dependency injection containers

Error handling is critical: you must handle ClassNotFoundException,
NoSuchMethodException, InstantiationException, IllegalAccessException,
and InvocationTargetException.
```

```java
public class DynamicCreationDemo {
    interface Plugin {
        String execute();
    }

    static class DefaultPlugin implements Plugin {
        public String execute() { return "Default executed"; }
    }

    public static Plugin loadPlugin(String className) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        return (Plugin) ctor.newInstance();
    }

    public static void main(String[] args) throws Exception {
        String pluginClass = "DynamicCreationDemo$DefaultPlugin"; // from config
        Plugin plugin = loadPlugin(pluginClass);
        System.out.println(plugin.execute()); // "Default executed"
    }
}
```

**Q2: What is a dynamic proxy and how does it work in Java?**

```text
A2: A dynamic proxy is an object created at runtime that implements one or more
interfaces and delegates all method calls to an InvocationHandler.

How it works:
1. You provide an array of interfaces the proxy should implement.
2. You provide an InvocationHandler that defines what happens when any method
   on the proxy is called.
3. Proxy.newProxyInstance() generates a new class at runtime that implements
   those interfaces, where every method body calls handler.invoke().

The InvocationHandler.invoke() method receives:
- proxy: the proxy instance itself
- method: the Method object being called
- args: the arguments (null for zero-arg methods)

Common use cases:
- Logging / tracing (log every method call)
- Transaction management (begin/commit around method calls)
- Lazy loading (defer object creation until first method call)
- Access control (check permissions before delegating)
- Mocking frameworks (Mockito uses proxies internally)

Limitation: Java's built-in Proxy only works with interfaces, not classes.
For class-based proxies, libraries like CGLIB or Byte Buddy are used.
```

```java
public class ProxyDemo {
    interface UserService {
        String findUser(int id);
        void deleteUser(int id);
    }

    static class RealUserService implements UserService {
        public String findUser(int id) { return "User#" + id; }
        public void deleteUser(int id) { System.out.println("Deleted: " + id); }
    }

    // Logging proxy that wraps any interface implementation
    @SuppressWarnings("unchecked")
    public static <T> T createLoggingProxy(Class<T> iface, T target) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class<?>[]{iface},
            (proxy, method, args) -> {
                System.out.println("Before: " + method.getName());
                Object result = method.invoke(target, args);
                System.out.println("After: " + method.getName() + " -> " + result);
                return result;
            }
        );
    }

    public static void main(String[] args) {
        UserService real = new RealUserService();
        UserService proxy = createLoggingProxy(UserService.class, real);
        proxy.findUser(42);
        // Before: findUser
        // After: findUser -> User#42
    }
}
```

**Q3: How does the `java.lang.reflect.Array` class work for dynamic array operations?**

```text
A3: The Array class provides static methods for creating and manipulating
arrays when the component type is not known at compile time:

Creation:
- Array.newInstance(componentType, length) -- creates a 1D array
- Array.newInstance(componentType, dimensions...) -- creates multi-dimensional array

Access:
- Array.get(array, index) -- returns element as Object (auto-boxes primitives)
- Array.set(array, index, value) -- sets element (auto-unboxes wrappers)
- Array.getLength(array) -- returns the length

Typed access (avoids boxing):
- Array.getInt(array, index), Array.setInt(array, index, value)
- Similar methods for all primitive types

The Array class is used by frameworks that need to handle arrays generically,
such as serialization libraries that must create arrays of arbitrary types.

Important: Array.newInstance returns Object, not T[]. You must cast carefully.
For a type-safe approach:
  int[] arr = (int[]) Array.newInstance(int.class, 10);
```

```java
public class ArrayReflectionDemo {
    public static void main(String[] args) {
        // Create int array dynamically
        Object intArr = Array.newInstance(int.class, 5);
        Array.setInt(intArr, 0, 100);
        Array.setInt(intArr, 1, 200);
        System.out.println(Array.getInt(intArr, 0)); // 100
        System.out.println(Array.getLength(intArr)); // 5

        // Create String array dynamically
        Object strArr = Array.newInstance(String.class, 3);
        Array.set(strArr, 0, "hello");
        Array.set(strArr, 1, "world");
        String[] typed = (String[]) strArr;
        System.out.println(Arrays.toString(typed)); // [hello, world, null]

        // Create 2D array: int[3][4]
        Object matrix = Array.newInstance(int.class, 3, 4);
        int[][] typedMatrix = (int[][]) matrix;
        typedMatrix[0][0] = 42;
    }
}
```

**Q4: How would you implement a simple reflective factory pattern?**

```text
A4: A reflective factory maps string keys (names, types, or identifiers) to
Class objects and creates instances on demand:

1. Registration phase: Map each key to a Class object.
2. Creation phase: Look up the Class by key, find the constructor, create instance.

Advantages over switch/if-else factory:
- Open/Closed Principle: Add new types by registering, no code changes.
- Configuration-driven: Keys can come from config files, databases, etc.
- Plugin-friendly: Load classes at runtime from external JARs.

Disadvantages:
- No compile-time type checking.
- Errors surface at runtime (ClassNotFoundException, etc.).
- Slightly slower due to reflection overhead.

For production use, consider combining with:
- ServiceLoader for automatic discovery
- Caching of Constructor objects
- Type-safe generics where possible
```

```java
public class ReflectiveFactoryDemo {
    interface Shape {
        double area();
    }

    static class Circle implements Shape {
        public double area() { return Math.PI * 4; } // r=2
    }

    static class Square implements Shape {
        public double area() { return 16; } // side=4
    }

    static class ShapeFactory {
        private final Map<String, Class<? extends Shape>> registry = new HashMap<>();

        public void register(String name, Class<? extends Shape> clazz) {
            registry.put(name.toLowerCase(), clazz);
        }

        public Shape create(String name) throws ReflectiveOperationException {
            Class<? extends Shape> clazz = registry.get(name.toLowerCase());
            if (clazz == null) throw new IllegalArgumentException("Unknown: " + name);
            return clazz.getDeclaredConstructor().newInstance();
        }
    }

    public static void main(String[] args) throws Exception {
        ShapeFactory factory = new ShapeFactory();
        factory.register("circle", Circle.class);
        factory.register("square", Square.class);

        Shape shape = factory.create("circle");
        System.out.println(shape.area()); // ~12.566
    }
}
```

**Q5: How does a dependency injection container use reflection to wire objects?**

```text
A5: A DI container uses reflection in several stages:

1. Scanning: The container scans classes (via classpath scanning or explicit
   registration) to discover which types are available and what their
   dependencies are.

2. Dependency analysis: For each class, the container inspects:
   - Constructor parameters (constructor injection)
   - Fields annotated with @Inject/@Autowired (field injection)
   - Setter methods annotated with @Inject (method injection)

3. Resolution: When creating an instance, the container:
   a. Finds the constructor to use (annotated or single constructor).
   b. Recursively resolves each parameter type.
   c. Calls Constructor.newInstance() with the resolved dependencies.

4. Post-creation wiring:
   a. For field injection: Field.setAccessible(true), Field.set(instance, dep).
   b. For method injection: Method.setAccessible(true), Method.invoke(instance, dep).

5. Lifecycle management: The container may also reflectively invoke
   @PostConstruct and @PreDestroy methods.

The key challenge is handling circular dependencies, scope management
(singleton vs prototype), and ensuring thread safety.
```

```java
public class MiniDI {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    @interface Inject {}

    static class Repository {
        String getData() { return "data from DB"; }
    }

    static class Service {
        private final Repository repo;

        @Inject
        Service(Repository repo) {
            this.repo = repo;
        }

        String process() { return "Processed: " + repo.getData(); }
    }

    static class Container {
        private final Map<Class<?>, Object> singletons = new HashMap<>();

        @SuppressWarnings("unchecked")
        <T> T resolve(Class<T> type) throws Exception {
            if (singletons.containsKey(type)) {
                return (T) singletons.get(type);
            }

            Constructor<?> ctor = findInjectableConstructor(type);
            Object[] args = Arrays.stream(ctor.getParameterTypes())
                    .map(paramType -> {
                        try { return resolve(paramType); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    })
                    .toArray();

            T instance = (T) ctor.newInstance(args);
            singletons.put(type, instance);
            return instance;
        }

        private Constructor<?> findInjectableConstructor(Class<?> type) {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                if (c.isAnnotationPresent(Inject.class)) return c;
            }
            try { return type.getDeclaredConstructor(); }
            catch (NoSuchMethodException e) { throw new RuntimeException(e); }
        }
    }
}
```

**Q6: What are the limitations of `java.lang.reflect.Proxy` and what alternatives exist?**

```text
A6: Limitations of java.lang.reflect.Proxy:

1. Interface-only: Can only proxy interfaces, not concrete or abstract classes.
   If the target type is a class, Proxy cannot help.

2. Performance overhead: Each method call goes through InvocationHandler.invoke(),
   which involves Method object dispatch and argument array creation.

3. No field access: Proxies only intercept method calls, not field reads/writes.

4. Object identity: proxy != target, so identity checks (==) will fail.

5. Method dispatch: equals(), hashCode(), and toString() are delegated to the
   handler, which must handle them explicitly for correct behavior.

Alternatives:
- CGLIB: Creates subclass-based proxies for classes (not just interfaces).
  Used by Spring for class-based AOP.
- Byte Buddy: Modern bytecode generation library. More flexible than CGLIB.
- Java compiler API: Generate and compile code at runtime.
- MethodHandle + LambdaMetafactory: Can create functional interface
  implementations without full proxy overhead.

In Java 21+, the trend is moving toward compile-time code generation
(annotation processors, GraalVM native image) to avoid reflection overhead
in production.
```

```java
public class ProxyLimitations {
    // Interface -- can be proxied with java.lang.reflect.Proxy
    interface Greetable {
        String greet(String name);
    }

    // Concrete class -- CANNOT be proxied with Proxy.newProxyInstance
    static class ConcreteGreeter {
        String greet(String name) { return "Hi, " + name; }
    }

    public static void main(String[] args) {
        // Works: interface-based proxy
        Greetable proxy = (Greetable) Proxy.newProxyInstance(
            Greetable.class.getClassLoader(),
            new Class<?>[]{Greetable.class},
            (p, method, a) -> "Proxied: " + a[0]
        );
        System.out.println(proxy.greet("World")); // "Proxied: World"

        // Does NOT work: cannot proxy a concrete class
        // Proxy.newProxyInstance(loader, new Class<?>[]{ConcreteGreeter.class}, handler);
        // -> IllegalArgumentException: ConcreteGreeter is not an interface
    }
}
```

## Code Examples

- Test: [DynamicCreationInvocationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/DynamicCreationInvocationTest.java)
- Source: [DynamicCreationInvocation.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/DynamicCreationInvocation.java)
