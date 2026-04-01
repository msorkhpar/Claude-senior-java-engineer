# 10.4.4. Annotation Processing and Code Generation

## Concept Explanation

Annotation processing refers to the mechanisms by which annotations are read and acted upon. In Java, there are two
primary approaches: **compile-time annotation processing** (using the `javax.annotation.processing` API, also known as
JSR 269) and **runtime annotation processing** (using the Reflection API). Both approaches enable powerful patterns
like code generation, validation, dependency injection, and aspect-oriented programming (AOP).

**Real-world analogy**: Annotation processing is like a building inspection system. Annotations are the blueprints and
labels on building components. Compile-time processors are like inspectors who check plans before construction begins —
they can flag issues and even generate additional construction plans (code generation). Runtime processors are like
inspectors who visit the finished building, read the labels on equipment and systems, and enforce operational rules
(security checks, logging, caching).

### Two Processing Approaches

1. **Compile-time processing** (`javax.annotation.processing.AbstractProcessor`):
   - Runs during compilation via `javac`
   - Can generate new source files, resource files, and compilation errors/warnings
   - Cannot modify existing source files
   - Used by: Lombok, MapStruct, Dagger, AutoValue, Immutables

2. **Runtime processing** (Reflection API + `java.lang.reflect`):
   - Runs when the application executes
   - Reads RUNTIME-retained annotations via reflection
   - Can dynamically alter behavior using proxies, method invocation, field injection
   - Used by: Spring, Jakarta EE, Hibernate, JUnit, Jackson

## Key Points to Remember

1. Compile-time processors extend `AbstractProcessor` and are registered via `META-INF/services`.
2. `@SupportedAnnotationTypes` declares which annotations a processor handles.
3. `@SupportedSourceVersion` declares the latest Java version supported.
4. Compile-time processors can only GENERATE new files, not modify existing source code.
5. Runtime processing requires `setAccessible(true)` for private members — subject to module access rules.
6. `java.lang.reflect.Proxy` creates dynamic proxy instances for interfaces — the backbone of AOP.
7. Runtime annotation processing has performance overhead due to reflection.
8. Compile-time code generation eliminates runtime reflection overhead but increases build complexity.
9. The `InvocationHandler` interface handles method calls on dynamic proxies.
10. Annotation-driven frameworks often combine both approaches: compile-time for validation and code generation,
    runtime for dependency injection and dynamic behavior.

## Relevant Java 21 Features

- **Module system** (Java 9+): Affects `setAccessible()` — private members in other modules may not be accessible
  without explicit `opens` directives.
- **Sealed classes**: Can be combined with annotation processing to enforce patterns on a restricted class hierarchy.
- **Record patterns**: Annotations on record components propagate to constructors, fields, and accessors based on
  `@Target`, enabling rich annotation-driven processing of records.
- **Virtual threads**: Runtime annotation processing (especially proxy-based AOP) works with virtual threads, but
  be aware of pinning when using `synchronized` in proxy handlers.
- Modern annotation processing tools like Google's Auto and Immutables generate efficient, boilerplate-free code at
  compile time.

## Common Pitfalls and How to Avoid Them

1. **Reflection performance in hot paths**: Reflection is slow compared to direct method calls. Cache reflective lookups.
   ```java
   // BAD: Reflecting on every call
   public void process(Object obj) {
       if (obj.getClass().isAnnotationPresent(Cacheable.class)) { ... }
   }

   // GOOD: Cache the reflection result
   private final Map<Class<?>, Boolean> cacheableCache = new ConcurrentHashMap<>();

   public void process(Object obj) {
       boolean cacheable = cacheableCache.computeIfAbsent(
           obj.getClass(), c -> c.isAnnotationPresent(Cacheable.class));
   }
   ```

2. **Forgetting setAccessible(true) for private fields**: Reflection cannot access private members without it.
   ```java
   Field field = clazz.getDeclaredField("name");
   field.setAccessible(true); // Required for private fields
   Object value = field.get(obj);
   ```

3. **Dynamic proxy only works with interfaces**: `java.lang.reflect.Proxy` can only proxy interfaces, not classes.
   For class proxying, use bytecode libraries like CGLIB or Byte Buddy.
   ```java
   // WORKS: Proxying an interface
   Calculator proxy = (Calculator) Proxy.newProxyInstance(...);

   // DOES NOT WORK: Cannot proxy a class with java.lang.reflect.Proxy
   // Use CGLIB or Byte Buddy instead for class proxying
   ```

4. **Module access violations in Java 9+**: Accessing private members of classes in other modules requires `opens`
   directives in `module-info.java`.

5. **Not handling `InvocationTargetException` in proxy handlers**: When a proxied method throws an exception, the proxy
   wraps it in `InvocationTargetException`. Always unwrap it.
   ```java
   try {
       return method.invoke(target, args);
   } catch (InvocationTargetException e) {
       throw e.getCause(); // Unwrap to get the actual exception
   }
   ```

## Best Practices and Optimization Techniques

1. **Prefer compile-time processing** for code generation to avoid runtime reflection overhead.
2. **Cache reflection results** (Method, Field, annotation instances) — they are immutable and thread-safe.
3. **Use `MethodHandles` for performance-critical reflection** — they are faster than `Method.invoke()` after lookup.
4. **Keep proxy handlers simple** — complex logic in `InvocationHandler` makes debugging difficult.
5. **Validate annotations early** — check for required annotations at startup, not during request processing.
6. **Use meaningful error messages** in annotation processors — developers see these during compilation.
7. **Separate concerns** — don't combine logging, caching, and security in a single proxy; use decorator/chain patterns.
8. **Test annotation processors independently** with compile-testing libraries (e.g., Google's `compile-testing`).

## Edge Cases and Their Handling

1. **Proxying methods from Object class**: `toString()`, `equals()`, `hashCode()` are also intercepted by the proxy.
   Handle them explicitly in the `InvocationHandler`.
2. **Proxy with no interfaces**: `Proxy.newProxyInstance` requires at least one interface.
3. **Annotation with Class<?> element at compile time**: Compile-time processors cannot use `getAnnotation()` directly;
   they must use the `javax.lang.model` API and handle `MirroredTypeException`.
4. **Circular dependencies in annotation-driven DI**: Can cause stack overflows during injection. Use lazy initialization
   or provider patterns.
5. **Thread safety of injected values**: If fields are injected reflectively after construction, ensure proper
   happens-before ordering (use volatile or synchronization).
6. **Multiple annotation processors**: Processors run in rounds. A processor in round N can generate files that are
   processed by processors in round N+1.

## Interview-specific Insights

Interviewers commonly test:

- Understanding of how frameworks like Spring use annotations at runtime (reflection + proxies)
- The difference between compile-time and runtime annotation processing
- How dynamic proxies work (`Proxy.newProxyInstance` + `InvocationHandler`)
- Performance implications of reflection-based annotation processing
- How to implement a simple annotation-driven framework (validation, injection, AOP)
- Understanding of annotation processor rounds and generated code
- Module system impact on reflection access

## Interview Q&A Section

**Q1: How does Spring use annotations at runtime to implement dependency injection?**

```text
A1: Spring uses runtime annotation processing via reflection to implement DI:

1. Component Scanning: At startup, Spring scans the classpath for classes annotated with
   @Component, @Service, @Repository, @Controller (all RUNTIME-retained).

2. Bean Registration: For each annotated class, Spring creates a BeanDefinition that
   describes how to instantiate and configure it.

3. Dependency Resolution: Spring inspects constructors, fields, and methods for @Autowired
   or @Inject annotations. It uses reflection to read these annotations and determine
   which dependencies are needed.

4. Instantiation: Spring creates instances using reflection (Constructor.newInstance()) and
   injects dependencies via field injection (Field.set()), setter injection (Method.invoke()),
   or constructor injection.

5. Proxy Creation: For beans requiring AOP (e.g., @Transactional, @Cacheable), Spring creates
   dynamic proxies (JDK Proxy for interfaces, CGLIB for classes) that intercept method calls
   and add cross-cutting behavior.

6. Lifecycle Callbacks: Spring processes @PostConstruct and @PreDestroy annotations to invoke
   lifecycle methods at the appropriate times.

This approach provides flexibility but has runtime overhead from reflection and proxy creation.
Modern alternatives like Micronaut and Quarkus use compile-time processing instead.
```

```java
// Simulated dependency injection using reflection
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Inject {
    String value() default "";
}

class InjectableService {
    @Inject("dataSource")
    private String dataSource;

    @Inject
    private String config; // Uses field name as key
}

// Simple injector implementation
static void inject(Object target, Map<String, Object> registry) throws Exception {
    for (Field field : target.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(Inject.class)) {
            Inject ann = field.getAnnotation(Inject.class);
            String key = ann.value().isEmpty() ? field.getName() : ann.value();
            Object value = registry.get(key);
            if (value != null) {
                field.setAccessible(true);
                field.set(target, value);
            }
        }
    }
}

// Usage
InjectableService svc = new InjectableService();
Map<String, Object> registry = Map.of(
    "dataSource", "jdbc:mysql://localhost/db",
    "config", "production"
);
inject(svc, registry);
// svc.dataSource = "jdbc:mysql://localhost/db"
// svc.config = "production"
```

**Q2: How do dynamic proxies work in Java?**

```text
A2: Java's dynamic proxy mechanism (java.lang.reflect.Proxy) creates proxy objects that
implement specified interfaces and delegate all method calls to an InvocationHandler.

Components:
1. Interface(s): The proxy implements one or more interfaces
2. InvocationHandler: Handles every method call on the proxy
3. Proxy.newProxyInstance(): Factory method that creates the proxy instance

How it works:
1. You call Proxy.newProxyInstance(classLoader, interfaces[], handler)
2. The JVM generates a class at runtime that implements all specified interfaces
3. Every method call on the proxy is routed to handler.invoke(proxy, method, args)
4. The handler can: execute the original method, add behavior before/after, skip it entirely,
   return a different value, or throw an exception

Limitations:
- Can only proxy interfaces, not concrete classes
- For class proxying, use CGLIB (subclass-based) or Byte Buddy
- Every method call has overhead from the handler invocation
- The generated proxy class is created once per interface set but stays in memory

Use cases: AOP (logging, timing, security), mocking frameworks (Mockito), lazy loading
(Hibernate), remote method invocation (RMI).
```

```java
interface Calculator {
    int add(int a, int b);
    int multiply(int a, int b);
    void adminReset();
}

class CalculatorImpl implements Calculator {
    @Timed(label = "addition")
    public int add(int a, int b) { return a + b; }

    @Timed
    public int multiply(int a, int b) { return a * b; }

    @RequiresPermission("ADMIN")
    public void adminReset() { /* reset */ }
}

// Timing proxy
@SuppressWarnings("unchecked")
static <T> T createTimedProxy(T target, Class<T> iface, List<InvocationLog> logs) {
    return (T) Proxy.newProxyInstance(
        iface.getClassLoader(),
        new Class<?>[]{iface},
        (proxy, method, args) -> {
            Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());

            if (targetMethod.isAnnotationPresent(Timed.class)) {
                long start = System.nanoTime();
                Object result = method.invoke(target, args);
                long duration = System.nanoTime() - start;
                logs.add(new InvocationLog(method.getName(), duration, result));
                return result;
            }
            return method.invoke(target, args);
        }
    );
}
```

**Q3: What is a compile-time annotation processor and how does it work?**

```text
A3: A compile-time annotation processor is a Java program that runs during compilation
(as part of javac) and can inspect annotations in source code, generate new files, and
report compilation errors or warnings.

How it works:
1. The processor extends javax.annotation.processing.AbstractProcessor
2. It declares which annotations it supports via @SupportedAnnotationTypes
3. It is registered via META-INF/services/javax.annotation.processing.Processor
4. During compilation, javac discovers and invokes the processor
5. The processor receives a RoundEnvironment containing annotated elements
6. It can use the javax.lang.model API to inspect types, methods, and annotations
7. It can use the Filer API to generate new .java or resource files
8. Processing occurs in rounds — new generated files can trigger additional rounds

Key differences from runtime processing:
- Runs at compile time, not runtime — zero runtime overhead
- Cannot modify existing source files — only generate new ones
- Uses javax.lang.model (mirror API) instead of java.lang.reflect
- Generated code is type-safe and compiled normally

Popular tools using compile-time processing:
- Lombok: Generates boilerplate (getters, setters, constructors)
- MapStruct: Generates type-safe mapper implementations
- Dagger: Generates dependency injection code
- AutoValue/Immutables: Generates value objects
```

```java
// Example compile-time annotation processor (conceptual)
@SupportedAnnotationTypes("com.example.GenerateBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class BuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                generateBuilderClass(typeElement);
            }
        }
        return true; // Claim the annotations
    }

    private void generateBuilderClass(TypeElement typeElement) {
        String className = typeElement.getSimpleName() + "Builder";
        try {
            JavaFileObject file = processingEnv.getFiler()
                .createSourceFile(className);
            try (Writer writer = file.openWriter()) {
                // Write generated builder class source code
                writer.write("public class " + className + " { ... }");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "Failed to generate: " + e.getMessage());
        }
    }
}
```

**Q4: How would you implement a security check using annotation processing and dynamic proxies?**

```text
A4: A security check system using annotations and proxies consists of:

1. A @RequiresPermission annotation (RUNTIME-retained) applied to methods
2. A dynamic proxy that intercepts method calls
3. The proxy reads the annotation, checks the user's permissions, and either
   allows or blocks the call

The proxy's InvocationHandler:
- Looks up the target method on the implementation class
- Checks if @RequiresPermission is present
- If present, compares the required permission against the user's permission set
- Throws SecurityException if the permission is missing
- Proceeds with the invocation if permission is present
- Methods without @RequiresPermission pass through without checks

This is essentially how frameworks like Spring Security implement method-level
security with @PreAuthorize / @Secured annotations, though they use more sophisticated
expression evaluation and role hierarchies.
```

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface RequiresPermission {
    String value();
}

interface UserService {
    String getUser(int id);
    void deleteUser(int id);
}

class UserServiceImpl implements UserService {
    public String getUser(int id) { return "User-" + id; }

    @RequiresPermission("ADMIN")
    public void deleteUser(int id) { /* delete logic */ }
}

// Security proxy
@SuppressWarnings("unchecked")
static <T> T createSecuredProxy(T target, Class<T> iface, Set<String> permissions) {
    return (T) Proxy.newProxyInstance(
        iface.getClassLoader(),
        new Class<?>[]{iface},
        (proxy, method, args) -> {
            Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());

            if (targetMethod.isAnnotationPresent(RequiresPermission.class)) {
                String required = targetMethod.getAnnotation(RequiresPermission.class).value();
                if (!permissions.contains(required)) {
                    throw new SecurityException("Missing permission: " + required);
                }
            }
            return method.invoke(target, args);
        }
    );
}

// Usage
UserService service = createSecuredProxy(
    new UserServiceImpl(), UserService.class, Set.of("READ"));
service.getUser(1);       // OK — no permission required
service.deleteUser(1);    // SecurityException: Missing permission: ADMIN
```

**Q5: What is the annotation processing "rounds" model?**

```text
A5: Compile-time annotation processing in Java operates in rounds:

Round 1:
- The compiler parses all source files
- Annotation processors are invoked with elements from the original source
- Processors may generate new source files or report errors
- If new files were generated, another round begins

Round 2:
- The compiler parses the newly generated source files
- Processors are invoked again with elements from the generated files
- If more files are generated, yet another round begins

Final Round:
- When no new files are generated, a final round occurs
- Processors are invoked with an empty set of annotated elements
- This is the cleanup round — processors can do final validation

Key rules:
1. A processor's process() method is called once per round
2. Each round only includes newly generated elements
3. Processors should not rely on processing order across different annotation types
4. The RoundEnvironment.processingOver() method returns true in the final round
5. Processors should return true to "claim" their annotations (prevent other processors
   from processing them) or false to allow further processing

This rounds model enables annotation processors to generate code that is itself annotated,
triggering further processing in subsequent rounds.
```

```java
@SupportedAnnotationTypes("com.example.Entity")
public class EntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            // Final round — do cleanup or validation
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            // Round 1: Process original @Entity classes
            // Generate repository interfaces (which might have their own annotations)
            generateRepository((TypeElement) element);
        }

        // Return true to claim @Entity annotations
        return true;
    }

    // Round 2 might process annotations on the generated repository interfaces
    // This continues until no new files are generated
}
```

**Q6: What are the performance considerations of runtime annotation processing vs compile-time?**

```text
A6: Performance is a critical factor when choosing between runtime and compile-time processing:

Runtime annotation processing (reflection):
- Cost: Method.getAnnotation() is relatively expensive (involves synchronization, copying)
- First access: ~100-1000x slower than direct method calls
- Cached access: ~10-50x slower (with proper caching of Method/Field objects)
- Memory: Each reflective lookup creates temporary objects
- JIT: The JIT compiler can partially optimize reflective calls but not as well as direct calls
- Startup: Classpath scanning (e.g., Spring component scan) can slow startup significantly

Compile-time annotation processing:
- Runtime cost: Zero — generated code is compiled normally
- Build cost: Adds time to the compilation phase
- Generated code: Runs at native speed, fully optimizable by JIT
- Startup: No scanning needed — dependencies are resolved at compile time
- Trade-off: More complex build setup, generated code may be harder to debug

Mitigation strategies for runtime processing:
1. Cache Method, Field, and Annotation objects (they're immutable)
2. Use MethodHandles instead of Method.invoke() for hot paths
3. Minimize classpath scanning scope (limit base packages)
4. Use lazy initialization — don't scan everything at startup
5. Consider GraalVM native image which resolves reflection at build time

Frameworks comparison:
- Spring: Primarily runtime (reflection + CGLIB proxies), slower startup
- Micronaut/Quarkus: Primarily compile-time, much faster startup
- Dagger: Compile-time DI code generation, no runtime reflection
```

```java
// Performance comparison example
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

class PerformanceComparison {

    // Direct invocation — fastest
    static int directAdd(int a, int b) { return a + b; }

    // Reflection — slower
    static int reflectiveAdd(Method method, Object target, int a, int b) throws Exception {
        return (int) method.invoke(target, a, b);
    }

    // MethodHandle — middle ground (faster than reflection after warmup)
    static int methodHandleAdd(MethodHandle handle, int a, int b) throws Throwable {
        return (int) handle.invoke(a, b);
    }

    // Best practice: Cache reflection results
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    static Method getCachedMethod(Class<?> clazz, String name, Class<?>... params) {
        String key = clazz.getName() + "." + name;
        return methodCache.computeIfAbsent(key, k -> {
            try {
                Method m = clazz.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
```

## Code Examples

- Test: [AnnotationProcessingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/AnnotationProcessingTest.java)
- Source: [AnnotationProcessing.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/AnnotationProcessing.java)
