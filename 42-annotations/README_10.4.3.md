# 10.4.3. Annotation Retention Policies (SOURCE, CLASS, RUNTIME)

## Concept Explanation

Every annotation in Java has a retention policy that determines at what stage of the program's lifecycle the annotation
is available. The retention policy is specified using the `@Retention` meta-annotation with one of three values from
the `RetentionPolicy` enum.

**Real-world analogy**: Think of retention policies like different types of ink used on shipping labels:
- **SOURCE** is like pencil marks made during packing — useful for the packer, erased before shipping. The label never
  leaves the warehouse (compiler).
- **CLASS** is like ink that survives shipping but fades before the recipient opens the box. The label is in the package
  (bytecode) but invisible when the item is used (runtime).
- **RUNTIME** is like permanent marker — the label is visible at every stage: packing (compilation), shipping (bytecode),
  and when the recipient opens the box (runtime reflection).

### The Three Retention Policies

1. **`RetentionPolicy.SOURCE`** — The annotation is discarded by the compiler. It exists only in source code and is not
   present in the compiled `.class` file. Used for compile-time checks and IDE hints.
   - Examples: `@Override`, `@SuppressWarnings`

2. **`RetentionPolicy.CLASS`** (default) — The annotation is retained in the `.class` file by the compiler but is NOT
   loaded by the JVM at runtime. It is invisible to reflection. Used by bytecode analysis tools.
   - This is the default if no `@Retention` is specified.

3. **`RetentionPolicy.RUNTIME`** — The annotation is retained in the `.class` file AND loaded by the JVM, making it
   accessible via the Reflection API at runtime. Used by frameworks for runtime behavior.
   - Examples: `@Deprecated`, `@FunctionalInterface`, most custom framework annotations.

## Key Points to Remember

1. If `@Retention` is not specified, the default is `RetentionPolicy.CLASS` — NOT RUNTIME.
2. RUNTIME retention includes everything CLASS retention provides, plus reflection access.
3. CLASS retention includes everything SOURCE retention provides, plus bytecode presence.
4. `SOURCE < CLASS < RUNTIME` in terms of availability scope.
5. Only RUNTIME-retained annotations can be read using `getAnnotation()`, `isAnnotationPresent()`, etc.
6. SOURCE-retained annotations are processed by annotation processors during compilation (JSR 269).
7. CLASS-retained annotations can be read by bytecode engineering tools (ASM, Byte Buddy, CGLIB).
8. Most custom annotations should use RUNTIME retention unless there's a specific reason not to.
9. The `@Target` meta-annotation controls WHERE an annotation can be placed (TYPE, METHOD, FIELD, PARAMETER, etc.) —
   this is orthogonal to retention.

## Relevant Java 21 Features

- **Annotation processing** (JSR 269, `javax.annotation.processing`) — Compile-time annotation processors can process
  SOURCE and CLASS-retained annotations to generate code, validate usage, or produce documentation.
- **TYPE_USE target** (Java 8+) — Enables annotations on any type usage, useful with retention policies for
  compile-time null checking (`@NonNull`) or runtime type validation.
- **Record patterns and sealed classes** — Annotations on record components are propagated based on `@Target`, and their
  retention policies determine whether they're available for compile-time or runtime processing.
- Modern build tools and IDEs leverage CLASS-retained annotations for code analysis without runtime overhead.

## Common Pitfalls and How to Avoid Them

1. **Forgetting to set RUNTIME retention for reflection-based annotations**: The most common mistake.
   ```java
   // BUG: Default CLASS retention — invisible to reflection!
   @Target(ElementType.METHOD)
   @interface Transactional { }

   // FIX: Explicit RUNTIME retention
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.METHOD)
   @interface Transactional { }
   ```

2. **Trying to read SOURCE-retained annotations at runtime**: They simply don't exist after compilation.
   ```java
   @Retention(RetentionPolicy.SOURCE)
   @interface CompileCheck { }

   // This will ALWAYS return false
   method.isAnnotationPresent(CompileCheck.class); // false
   ```

3. **Confusing `getAnnotation()` with `getDeclaredAnnotation()`**:
   - `getAnnotation()` includes inherited annotations (if `@Inherited`)
   - `getDeclaredAnnotation()` only returns directly-applied annotations
   ```java
   // Parent has @Auditable (which is @Inherited)
   Child.class.getAnnotation(Auditable.class);         // returns parent's annotation
   Child.class.getDeclaredAnnotation(Auditable.class);  // returns null
   ```

4. **Assuming CLASS retention annotations are available via reflection**: They are embedded in bytecode but NOT loaded
   by the JVM's classloader into reflection data.

5. **Not considering annotation retention when choosing a processing strategy**: Use SOURCE + annotation processor for
   compile-time code generation; use RUNTIME + reflection for framework behavior.

## Best Practices and Optimization Techniques

1. **Choose the right retention for your use case**:
   - SOURCE: Compile-time checks, code generation via annotation processors, IDE hints
   - CLASS: Bytecode analysis tools, dependency injection frameworks using bytecode manipulation
   - RUNTIME: Reflection-based frameworks, validation, serialization, REST endpoint mapping

2. **Default to RUNTIME** for custom annotations unless you have a specific reason to use SOURCE or CLASS.

3. **Use SOURCE retention for compile-time-only annotations** to avoid unnecessary bytecode and runtime overhead.

4. **Document the retention policy** in annotation Javadoc, especially if it's not RUNTIME, so users understand when
   the annotation is available.

5. **Combine with appropriate `@Target`** to prevent misuse and provide clear error messages when the annotation is
   applied to an incorrect element.

6. **For performance-sensitive code**, prefer SOURCE retention with annotation processing over RUNTIME retention with
   reflection, as reflection has higher overhead.

## Edge Cases and Their Handling

1. **Local variable annotations**: Even with RUNTIME retention, annotations on local variables are NOT accessible
   via reflection. The JVM does not retain local variable annotations in a reflection-accessible form.

2. **Parameter annotations with RUNTIME retention**: These ARE accessible via `Method.getParameterAnnotations()`.

3. **Annotations on type uses**: TYPE_USE annotations with RUNTIME retention are accessible via
   `AnnotatedType` objects.

4. **Multiple annotations of the same type**: Use `@Repeatable` with RUNTIME retention to access all instances via
   `getAnnotationsByType()`.

5. **Annotations on inherited methods**: Method annotations are NOT inherited by overriding methods (regardless of
   `@Inherited`). Each override must declare its own annotations.

6. **Annotation interfaces themselves have annotations**: You can read meta-annotations from an annotation's class
   object: `MyAnnotation.class.getAnnotation(Retention.class)`.

## Interview-specific Insights

Interviewers frequently test:

- Whether candidates know the three retention policies and their differences
- What the DEFAULT retention is (CLASS, not RUNTIME — a common mistake)
- Whether SOURCE-retained annotations can be read at runtime (no)
- The relationship between retention policy and reflection
- When to choose each retention policy for real-world scenarios
- Understanding `getAnnotation()` vs. `getDeclaredAnnotation()` vs. `getAnnotationsByType()`

Common tricky questions:
- "What is the default retention policy?" (CLASS)
- "Can you read @Override at runtime?" (No — SOURCE retention)
- "Can you read @Deprecated at runtime?" (Yes — RUNTIME retention)
- "Where are CLASS-retained annotations visible?" (In bytecode but not via reflection)

## Interview Q&A Section

**Q1: What are the three annotation retention policies and when would you use each?**

```text
A1: Java provides three retention policies that control annotation lifetime:

1. SOURCE (RetentionPolicy.SOURCE):
   - Discarded by the compiler — not present in .class files
   - Use for: compile-time checks, IDE hints, annotation processors
   - Examples: @Override, @SuppressWarnings
   - Advantage: Zero bytecode/runtime overhead

2. CLASS (RetentionPolicy.CLASS):
   - Retained in .class files but NOT loaded by the JVM at runtime
   - This is the DEFAULT if no @Retention is specified
   - Use for: bytecode analysis tools, build-time processing
   - Examples: Some internal JDK annotations
   - Advantage: Available for offline bytecode analysis without runtime cost

3. RUNTIME (RetentionPolicy.RUNTIME):
   - Retained in .class files AND loaded by the JVM — accessible via reflection
   - Use for: frameworks that process annotations at runtime (Spring, Jakarta EE, etc.)
   - Examples: @Deprecated, @FunctionalInterface, @Entity, @Autowired
   - Advantage: Full runtime accessibility for dynamic behavior

Decision guide:
- Need compile-time checking only? → SOURCE
- Need bytecode-level tooling? → CLASS
- Need runtime reflection? → RUNTIME (most common for custom annotations)
```

```java
import java.lang.annotation.*;

// SOURCE: Only for compiler — e.g., documentation hint
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@interface ReviewNeeded {
    String reason();
}

// CLASS: For bytecode tools — not reflection-accessible
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@interface GeneratedCode {
    String generator();
}

// RUNTIME: For framework processing — reflection-accessible
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Cacheable {
    String cacheName() default "default";
    int ttlSeconds() default 300;
}

// Demonstration
class Service {
    @ReviewNeeded(reason = "Optimize SQL query")
    @Cacheable(cacheName = "users", ttlSeconds = 600)
    public List<User> getUsers() { return List.of(); }
}

// At runtime
Method m = Service.class.getDeclaredMethod("getUsers");
m.isAnnotationPresent(ReviewNeeded.class);  // false (SOURCE)
m.isAnnotationPresent(Cacheable.class);     // true  (RUNTIME)
```

**Q2: What is the default retention policy if @Retention is not specified?**

```text
A2: The default retention policy is RetentionPolicy.CLASS.

This is a frequently misunderstood point — many developers assume the default is RUNTIME
or SOURCE. The default CLASS retention means:

1. The annotation IS written into the .class bytecode file by the compiler
2. The annotation is NOT loaded by the JVM classloader at runtime
3. The annotation is NOT accessible via reflection (isAnnotationPresent returns false)

This default exists for historical reasons and is useful for bytecode analysis tools, but
it's rarely what you want for custom annotations. As a result:

- ALWAYS explicitly specify @Retention on custom annotations
- For framework/library annotations, RUNTIME is almost always correct
- For compile-time-only checks, SOURCE is appropriate
- CLASS is rarely the intentional choice for custom annotations

This is a very common interview trap question.
```

```java
// NO @Retention specified — defaults to CLASS
@Target(ElementType.METHOD)
@interface DefaultRetention { }

// Explicitly RUNTIME
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ExplicitRuntime { }

class Demo {
    @DefaultRetention
    @ExplicitRuntime
    public void myMethod() { }
}

// At runtime
Method m = Demo.class.getDeclaredMethod("myMethod");

// DEFAULT (CLASS) — NOT available at runtime!
System.out.println(m.isAnnotationPresent(DefaultRetention.class)); // false

// EXPLICIT RUNTIME — available at runtime
System.out.println(m.isAnnotationPresent(ExplicitRuntime.class));  // true
```

**Q3: How do you read annotations at runtime using reflection?**

```text
A3: Java's Reflection API provides several methods to read RUNTIME-retained annotations:

On Class, Method, Field, Constructor, and Parameter objects:

1. isAnnotationPresent(Class<A>) — checks if an annotation of the given type exists
2. getAnnotation(Class<A>) — returns the annotation instance (or null)
3. getAnnotations() — returns all annotations (including inherited)
4. getDeclaredAnnotations() — returns only directly-applied annotations
5. getAnnotationsByType(Class<A>) — returns all instances (including repeatable)

For parameters specifically:
- Method.getParameterAnnotations() — returns a 2D array of annotations

Important distinctions:
- getAnnotation() respects @Inherited for class-level annotations
- getDeclaredAnnotation() ignores inheritance
- getAnnotationsByType() unwraps repeatable annotation containers
```

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@interface Info {
    String description();
    int priority() default 0;
}

@Info(description = "Core service", priority = 1)
class MyService {
    @Info(description = "Primary data", priority = 2)
    private String data;

    @Info(description = "Process data", priority = 3)
    public void process() { }
}

// Reading annotations at runtime
Class<?> clazz = MyService.class;

// Class-level
Info classInfo = clazz.getAnnotation(Info.class);
System.out.println(classInfo.description()); // "Core service"

// Method-level
Method method = clazz.getDeclaredMethod("process");
Info methodInfo = method.getAnnotation(Info.class);
System.out.println(methodInfo.priority()); // 3

// Field-level
Field field = clazz.getDeclaredField("data");
field.setAccessible(true);
Info fieldInfo = field.getAnnotation(Info.class);
System.out.println(fieldInfo.description()); // "Primary data"

// All annotations on a class
Annotation[] all = clazz.getAnnotations();
// Includes inherited annotations (if any parent has @Inherited annotations)
```

**Q4: What is the difference between getAnnotation() and getDeclaredAnnotation()?**

```text
A4: These two methods differ in how they handle annotation inheritance:

getAnnotation(Class<A>):
- Returns the annotation if directly present OR inherited (via @Inherited)
- Looks up the class hierarchy for inherited class-level annotations
- For methods and fields, behaves the same as getDeclaredAnnotation()
  (methods/fields don't inherit annotations)

getDeclaredAnnotation(Class<A>):
- Returns the annotation ONLY if directly present on the element
- Ignores @Inherited — never looks at parent classes
- Useful when you need to distinguish between direct and inherited annotations

The same pattern applies to getAnnotations() vs getDeclaredAnnotations():
- getAnnotations() includes inherited class-level annotations
- getDeclaredAnnotations() only returns directly-applied annotations

Important: For methods, fields, and parameters, both methods behave identically because
annotation inheritance only applies at the class level.
```

```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Auditable { String level() default "INFO"; }

@Auditable(level = "DEBUG")
class Parent { }

class Child extends Parent { }

// getAnnotation — includes inherited
Auditable a1 = Child.class.getAnnotation(Auditable.class);
System.out.println(a1);        // @Auditable(level="DEBUG") — found via inheritance
System.out.println(a1.level()); // "DEBUG"

// getDeclaredAnnotation — only direct
Auditable a2 = Child.class.getDeclaredAnnotation(Auditable.class);
System.out.println(a2); // null — not directly on Child

// getAnnotations vs getDeclaredAnnotations
Annotation[] all = Child.class.getAnnotations();       // includes @Auditable
Annotation[] direct = Child.class.getDeclaredAnnotations(); // empty (no direct annotations)
```

**Q5: Can you access annotations on local variables at runtime?**

```text
A5: No, you cannot access annotations on local variables at runtime, even if the annotation
has RUNTIME retention. This is a JVM limitation — the bytecode format and the Reflection API
do not provide a mechanism to retrieve annotations on local variables.

Here's what happens with each retention policy for local variable annotations:
- SOURCE: The annotation is discarded by the compiler (as expected)
- CLASS: The annotation is stored in the class file's LocalVariableTypeTable attribute,
  but the JVM does not make this accessible via standard reflection
- RUNTIME: Same as CLASS for local variables — stored in bytecode but NOT accessible
  via the standard Reflection API

Local variable annotations are primarily useful for:
1. Compile-time annotation processors
2. Static analysis tools (FindBugs, SpotBugs, Checker Framework)
3. IDE plugins for code hints and navigation

If you need runtime access to a "local variable annotation," consider:
- Moving the annotation to the method parameter
- Moving the annotation to a field
- Using a different approach (e.g., method-level annotation with element values)
```

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.LOCAL_VARIABLE)  // Only applies to local variables
@interface Immutable { }

void example() {
    @Immutable
    String name = "hello";

    // There is NO way to read @Immutable at runtime via standard reflection.
    // The Reflection API does not expose local variable annotations.
}

// For parameters (NOT local variables), annotations ARE accessible:
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface Validated { String message() default "Invalid"; }

void process(@Validated(message = "Required") String input) { }

// Parameter annotations can be read:
Method m = getClass().getDeclaredMethod("process", String.class);
Annotation[][] paramAnns = m.getParameterAnnotations();
// paramAnns[0] contains @Validated — this works!
```

**Q6: How does @Target interact with retention policies?**

```text
A6: @Target and @Retention are orthogonal meta-annotations that control different aspects:

@Target controls WHERE an annotation can be applied:
- ElementType.TYPE — classes, interfaces, enums, records
- ElementType.METHOD — methods
- ElementType.FIELD — fields (including enum constants)
- ElementType.PARAMETER — method parameters
- ElementType.CONSTRUCTOR — constructors
- ElementType.LOCAL_VARIABLE — local variables
- ElementType.ANNOTATION_TYPE — other annotations (meta-annotation)
- ElementType.PACKAGE — package declarations
- ElementType.TYPE_PARAMETER — generic type parameters (Java 8+)
- ElementType.TYPE_USE — any type usage (Java 8+)
- ElementType.MODULE — modules (Java 9+)
- ElementType.RECORD_COMPONENT — record components (Java 14+)

@Retention controls WHEN the annotation is available:
- SOURCE, CLASS, or RUNTIME

These are independent — you can combine any @Target with any @Retention. However, some
combinations are more useful than others:
- LOCAL_VARIABLE + RUNTIME: The annotation is in bytecode but NOT accessible via reflection
- PARAMETER + RUNTIME: Accessible via Method.getParameterAnnotations()
- TYPE_USE + RUNTIME: Accessible via AnnotatedType objects

If @Target is not specified, the annotation can be applied to any element (except type
parameters and type uses in some cases).
```

```java
// RUNTIME + multiple targets — most flexible
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@interface RuntimeInfo {
    String description();
}

// SOURCE + METHOD — compile-time method check only
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@interface CheckReturnValue { }

// RUNTIME + PARAMETER — accessible via parameter reflection
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface RequestParam {
    String name();
    boolean required() default true;
}

// RUNTIME + TYPE_USE — annotates the type itself
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@interface NonNull { }

// Usage of TYPE_USE annotation
List<@NonNull String> names;  // Annotates the String type usage
```

## Code Examples

- Test: [RetentionPoliciesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/RetentionPoliciesTest.java)
- Source: [RetentionPolicies.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/RetentionPolicies.java)
