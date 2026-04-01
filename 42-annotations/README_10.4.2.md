# 10.4.2. Custom Annotation Creation

## Concept Explanation

Java allows developers to define their own annotations using the `@interface` keyword. Custom annotations are a powerful
metadata mechanism that enables you to attach structured information to code elements (classes, methods, fields,
parameters, etc.) and process that metadata at compile time or runtime.

**Real-world analogy**: Custom annotations are like custom labels or tags in a warehouse system. While the warehouse
comes with standard labels (like "FRAGILE" or "THIS SIDE UP" — analogous to built-in annotations), you can create your
own labels specific to your business needs (like "PRIORITY-1 SHIPPING" or "TEMPERATURE-SENSITIVE"). These custom labels
carry structured information and trigger specific handling processes.

Custom annotations consist of:
- **Declaration**: Using `@interface` to define the annotation type
- **Elements**: The annotation's members (look like methods but act as named parameters)
- **Meta-annotations**: Annotations on the annotation itself that control its behavior (`@Target`, `@Retention`,
  `@Documented`, `@Inherited`, `@Repeatable`)

### Types of Custom Annotations

1. **Marker annotations**: No elements — simply mark presence (e.g., `@ThreadSafe`)
2. **Single-value annotations**: One element named `value` — allows shorthand usage (e.g., `@Author("Jane")`)
3. **Multi-value annotations**: Multiple elements with optional defaults (e.g., `@ApiEndpoint(path="/users", method="GET")`)
4. **Repeatable annotations**: Can be applied multiple times to the same element (e.g., `@Role("ADMIN") @Role("MANAGER")`)

## Key Points to Remember

1. Annotation elements can only be: primitives, `String`, `Class`, enums, annotations, or arrays of these.
2. Element defaults are specified with the `default` keyword: `String value() default "unknown";`.
3. If the only element is named `value`, the name can be omitted: `@Author("Jane")` instead of `@Author(value = "Jane")`.
4. Annotations cannot extend other annotations or interfaces.
5. Annotation elements cannot have `null` as a default — use sentinel values like `""` or `-1` instead.
6. `@Inherited` only works on class-level annotations — method and field annotations are never inherited.
7. `@Repeatable` requires a container annotation that holds an array of the repeatable annotation.
8. `@Documented` causes the annotation to appear in Javadoc-generated documentation.
9. Annotation instances are immutable — once applied, their values cannot be changed.

## Relevant Java 21 Features

- **Repeatable annotations** (Java 8+): Allow the same annotation to appear multiple times on a single element.
- **TYPE_USE and TYPE_PARAMETER targets** (Java 8+): Enable annotations on generic type arguments and type uses
  (e.g., `@NonNull String`, `List<@NonNull String>`).
- **Record components** can be annotated, and annotations propagate to the corresponding field, accessor method, and
  constructor parameter based on `@Target`.
- **Sealed classes** work well with custom annotations for marking the permitted hierarchy.
- Modern frameworks (Spring, Jakarta EE, Micronaut) rely heavily on custom annotations processed at compile time or
  runtime for dependency injection, validation, and endpoint mapping.

## Common Pitfalls and How to Avoid Them

1. **Using `null` as a default value**: Annotation elements cannot default to `null`.
   ```java
   // COMPILATION ERROR
   @interface MyAnnotation {
       String value() default null; // Not allowed!
   }

   // FIX: Use a sentinel value
   @interface MyAnnotation {
       String value() default "";
   }
   ```

2. **Forgetting @Retention(RUNTIME) for reflection-based processing**: The default retention is CLASS, which is NOT
   available at runtime.
   ```java
   // BAD: Default CLASS retention — invisible to reflection
   @interface MyAnnotation { }

   // GOOD: Explicit RUNTIME retention
   @Retention(RetentionPolicy.RUNTIME)
   @interface MyAnnotation { }
   ```

3. **Using complex types as annotation elements**: Only primitives, String, Class, enums, annotations, and arrays thereof
   are allowed.
   ```java
   // COMPILATION ERROR: List is not a valid annotation element type
   @interface BadAnnotation {
       List<String> values(); // Not allowed!
   }

   // FIX: Use an array
   @interface GoodAnnotation {
       String[] values();
   }
   ```

4. **Expecting @Inherited to work on methods**: `@Inherited` only applies to class-level annotations. Method and field
   annotations are never automatically inherited by subclasses.

5. **Forgetting the container annotation for @Repeatable**: Every repeatable annotation needs a container.
   ```java
   // Need BOTH the repeatable annotation AND its container
   @Repeatable(Roles.class)
   @interface Role { String value(); }

   @interface Roles { Role[] value(); } // Container
   ```

## Best Practices and Optimization Techniques

1. **Always specify `@Retention` explicitly** — don't rely on the default (CLASS), which is rarely what you want.
2. **Always specify `@Target`** — restricts where the annotation can be used, preventing misuse.
3. **Use `@Documented`** for public API annotations so they appear in Javadoc.
4. **Provide meaningful default values** for optional elements to reduce boilerplate at usage sites.
5. **Name the primary element `value`** if there's a single most-important element — enables shorthand syntax.
6. **Validate annotation values at processing time**, not just at usage time.
7. **Keep annotations focused** — each annotation should represent one concern (SRP applies to annotations too).
8. **Use enums for constrained choices** instead of String elements to prevent typos:
   ```java
   enum HttpMethod { GET, POST, PUT, DELETE }

   @interface Endpoint {
       HttpMethod method() default HttpMethod.GET; // Better than String
   }
   ```

## Edge Cases and Their Handling

1. **Empty arrays as defaults**: Allowed and useful — `String[] tags() default {}`.
2. **Annotation element of annotation type**: You can nest annotations — `@Constraint(validatedBy = @Validator(...))`.
3. **Class<?> elements**: Useful for specifying types — `Class<?> implementation() default Void.class`.
4. **Repeatable annotations accessed via container**: When using `getAnnotation()`, you get the container; use
   `getAnnotationsByType()` to get individual repeatable annotations.
5. **Annotations on local variables**: Only available with SOURCE retention — the JVM does not preserve local variable
   annotations at runtime.
6. **Inheriting annotations with `@Inherited`**: Only the annotation from the most-specific parent is inherited; if both
   parent and grandparent have the annotation, only the parent's is seen.

## Interview-specific Insights

Interviewers often focus on:

- The syntax and structure of custom annotation declarations
- Understanding which types are valid as annotation elements (and which are not)
- The role of meta-annotations (`@Target`, `@Retention`, `@Inherited`, `@Documented`, `@Repeatable`)
- How `@Inherited` works (and its limitations — class-level only)
- The difference between `getAnnotation()` and `getAnnotationsByType()` for repeatable annotations
- Practical use cases: validation frameworks, ORM mapping, REST endpoint definitions

Common tricky questions:
- "Can annotations extend other annotations?" (No)
- "Can you use null as a default value?" (No)
- "Does @Inherited work on method annotations?" (No)

## Interview Q&A Section

**Q1: How do you create a custom annotation in Java?**

```text
A1: A custom annotation is declared using the @interface keyword. The declaration includes:

1. Meta-annotations that control the annotation's behavior:
   - @Retention — when the annotation is available (SOURCE, CLASS, or RUNTIME)
   - @Target — where the annotation can be applied (TYPE, METHOD, FIELD, etc.)
   - @Documented — whether it appears in Javadoc
   - @Inherited — whether subclasses inherit it (class-level only)

2. Elements (which look like abstract methods but act as named parameters):
   - Must return: primitive, String, Class, enum, annotation, or array thereof
   - Can have default values
   - If named "value" and it's the only required element, the name can be omitted at usage

3. The annotation is then used by prefixing @ to its name at applicable locations.
```

```java
// Marker annotation (no elements)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ThreadSafe { }

// Single-value annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@interface Author {
    String value(); // Named 'value' — allows shorthand
}

// Multi-value annotation with defaults
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ApiEndpoint {
    String path();
    String method() default "GET";
    String description() default "";
    int version() default 1;
    String[] produces() default {"application/json"};
}

// Usage
@ThreadSafe
@Author("Jane Doe")
class UserService {
    @ApiEndpoint(path = "/users", method = "POST", version = 2)
    public String createUser() { return "created"; }
}
```

**Q2: What types can annotation elements return?**

```text
A2: Annotation elements are restricted to a specific set of types. This limitation exists
because annotation values must be compile-time constants that can be embedded in bytecode.

Valid annotation element types:
1. Primitive types: int, long, short, byte, float, double, boolean, char
2. String
3. Class<?> (or Class<? extends SomeType>)
4. Enum types
5. Other annotation types
6. Arrays of any of the above (1D arrays only)

NOT allowed:
- Wrapper types (Integer, Long, etc.) — use primitives instead
- Collections (List, Set, Map) — use arrays instead
- Arbitrary objects — annotations are compile-time constructs
- null — cannot be a default value or assigned value
- Multi-dimensional arrays

The restriction to compile-time constants ensures annotations can be processed efficiently
by the compiler and stored compactly in bytecode.
```

```java
@Retention(RetentionPolicy.RUNTIME)
@interface ComprehensiveAnnotation {
    // Primitives
    int count() default 0;
    boolean enabled() default true;

    // String
    String name() default "default";

    // Class
    Class<?> implementation() default Void.class;

    // Enum
    RetentionPolicy retention() default RetentionPolicy.RUNTIME;

    // Nested annotation
    Deprecated deprecated() default @Deprecated(since = "1.0");

    // Array
    String[] tags() default {};
    int[] priorities() default {1, 2, 3};
}
```

**Q3: What is the difference between @Inherited and non-inherited annotations?**

```text
A3: The @Inherited meta-annotation controls whether a class-level annotation is automatically
inherited by subclasses.

With @Inherited:
- If class A has the annotation, class B extends A automatically "sees" the annotation
- getAnnotation() on class B returns the parent's annotation
- Only works on CLASS-level annotations (not methods, fields, or interfaces)

Without @Inherited (default):
- The annotation only appears on the element it was directly applied to
- Subclasses must explicitly re-apply the annotation

Key limitations:
1. @Inherited does NOT work on interface annotations — implementing an interface does not
   inherit its annotations
2. @Inherited does NOT work on method or field annotations
3. If a subclass declares the same annotation, it shadows (replaces) the parent's
4. getDeclaredAnnotations() only returns directly-applied annotations, even with @Inherited

This is frequently misunderstood in interviews — many candidates assume all annotations
are inherited, or that @Inherited works on methods.
```

```java
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Auditable {
    String level() default "INFO";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Author {
    String value();
}

@Auditable(level = "DEBUG")
@Author("Jane")
class ParentService { }

class ChildService extends ParentService { }

// At runtime:
Auditable a = ChildService.class.getAnnotation(Auditable.class);
// a != null, level = "DEBUG" — inherited!

Author auth = ChildService.class.getAnnotation(Author.class);
// auth == null — @Author is NOT @Inherited
```

**Q4: How do repeatable annotations work?**

```text
A4: Repeatable annotations (Java 8+) allow the same annotation to be applied multiple times
to a single program element. They require two annotation declarations:

1. The repeatable annotation itself, marked with @Repeatable(ContainerAnnotation.class)
2. A container annotation that holds an array of the repeatable annotation

When you apply a repeatable annotation multiple times, the compiler wraps them in the
container annotation. You can access them via:

- getAnnotationsByType(Role.class) — returns the individual annotations (recommended)
- getAnnotation(Roles.class) — returns the container annotation

If only one instance is applied, getAnnotation(Role.class) returns it directly, while
getAnnotation(Roles.class) returns null (no container needed for a single instance).
```

```java
// Container annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@interface Roles {
    Role[] value();
}

// Repeatable annotation
@Repeatable(Roles.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@interface Role {
    String value();
}

// Usage: Multiple @Role on one method
class Service {
    @Role("ADMIN")
    @Role("MANAGER")
    public void sensitiveOperation() { }
}

// Reading repeatable annotations
Method m = Service.class.getDeclaredMethod("sensitiveOperation");
Role[] roles = m.getAnnotationsByType(Role.class);
// roles = [@Role("ADMIN"), @Role("MANAGER")]

Roles container = m.getAnnotation(Roles.class);
// container.value() = [@Role("ADMIN"), @Role("MANAGER")]
```

**Q5: How can you build a simple validation framework using custom annotations?**

```text
A5: A validation framework using annotations typically consists of three parts:

1. Constraint annotations: Custom annotations that define validation rules
   (e.g., @NotEmpty, @Range, @Pattern)

2. Validated classes: Domain objects annotated with constraint annotations on their fields

3. Validator processor: A class that uses reflection to read annotations from fields,
   extract their parameters, and apply the validation logic

The validator iterates over declared fields, checks for annotation presence using
field.isAnnotationPresent(), reads annotation values, and compares against the actual
field value. Validation errors are collected and returned.

This is essentially how frameworks like Jakarta Bean Validation (Hibernate Validator)
work, though they add layers of caching, custom validators, groups, and message
interpolation.
```

```java
// Step 1: Define constraint annotations
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotEmpty {
    String message() default "Field must not be empty";
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Range {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
    String message() default "Value out of range";
}

// Step 2: Annotate domain class
class User {
    @NotEmpty(message = "Name is required")
    private String name;

    @Range(min = 0, max = 150, message = "Invalid age")
    private int age;

    User(String name, int age) { this.name = name; this.age = age; }
}

// Step 3: Validator processor
record ValidationError(String field, String message) {}

List<ValidationError> validate(Object obj) {
    List<ValidationError> errors = new ArrayList<>();
    for (Field field : obj.getClass().getDeclaredFields()) {
        field.setAccessible(true);
        if (field.isAnnotationPresent(NotEmpty.class)) {
            Object val = field.get(obj);
            if (val == null || (val instanceof String s && s.isBlank())) {
                errors.add(new ValidationError(field.getName(),
                    field.getAnnotation(NotEmpty.class).message()));
            }
        }
        // ... similar for @Range
    }
    return errors;
}
```

**Q6: Can annotations extend other annotations or classes?**

```text
A6: No. Annotations in Java cannot extend other annotations, classes, or interfaces. Every
annotation type implicitly extends java.lang.annotation.Annotation, and this is the only
inheritance relationship allowed.

This means:
- You cannot create an annotation hierarchy (e.g., @HttpMethod extending @Annotation)
- You cannot share elements between annotations via inheritance
- Each annotation type is standalone

Workarounds for "annotation inheritance":
1. Composition: Use a meta-annotation pattern — annotate your annotation with another
   annotation (like Spring's @Component annotated with @Indexed)
2. Annotation element of annotation type: Include one annotation as an element of another
3. Processing logic: Handle the "inheritance" in your annotation processor by checking
   for multiple annotation types

This limitation exists because annotations are designed to be simple metadata containers,
not full-featured types with inheritance hierarchies.
```

```java
// This is NOT allowed:
// @interface HttpGet extends HttpMethod { } // COMPILATION ERROR

// Workaround 1: Composition via meta-annotations
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@interface HttpMethod {
    String value();
}

@HttpMethod("GET")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface GetMapping {
    String path();
}

// Workaround 2: Annotation as element
@Retention(RetentionPolicy.RUNTIME)
@interface Constraint {
    Class<?> validatedBy();
}

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyValidator.class)
@interface NotEmpty {
    String message() default "must not be empty";
}
```

## Code Examples

- Test: [CustomAnnotationCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/CustomAnnotationCreationTest.java)
- Source: [CustomAnnotationCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/CustomAnnotationCreation.java)
