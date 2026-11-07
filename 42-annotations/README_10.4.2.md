# 10.4.2. Custom Annotation Creation

A support agent with no roles voided a customer's payment. The method is guarded:

```java
@Role("ADMIN") @Role("AUDITOR")
public void voidPayment() { }
```

and the guard, written when every method had one role, reads `getAnnotation(Role.class)` and lets
the call through when it finds no `@Role`. `refund()`, which carries a single `@Role("ADMIN")`,
refuses the same agent. Both methods compile, but javac stores their annotations differently.

Every annotation this page reads by reflection is `@Retention(RetentionPolicy.RUNTIME)`;
what the other retentions keep is unit 10.4.3. Reading annotations inside javac with a processor is
unit 10.4.4, and the built-in ones such as `@Override` are unit 10.4.1.

## An Element Holds a Compile-Time Constant of a Few Types

An annotation type is declared with `@interface`. Which of these elements compile?

```java
import java.util.List;

@interface Cache {
    Object key();                                        // 1
    List<String> regions();                              // 2
    Integer maxEntries();                                // 3
    String owner() default null;                         // 4
    long ttlMillis() default System.currentTimeMillis(); // 5
    String[][] tiers();                                  // 6
}
```

```text
Cache.java:7: error: element value must be a constant expression
    String owner() default null;                         // 4
                           ^
Cache.java:8: error: expression not allowed as annotation value
    long ttlMillis() default System.currentTimeMillis(); // 5
                                                     ^
Cache.java:4: error: invalid type for annotation interface element
    Object key();                                        // 1
    ^
Cache.java:5: error: invalid type for annotation interface element
    List<String> regions();                              // 2
        ^
Cache.java:6: error: invalid type for annotation interface element
    Integer maxEntries();                                // 3
    ^
Cache.java:9: error: invalid type for annotation interface element
    String[][] tiers();                                  // 6
          ^
6 errors
```

None compiles. An element's type is a primitive, `String`, `Class`, an enum, another annotation, or a
one-dimensional array of one of these. Every value, the defaults included, is fixed when javac runs:
the class file stores it as a constant, an enum constant's name, a class name, a nested
annotation or an array of these, so the JVM can rebuild it without calling the code that
computed it. A `List`, an `Integer` or a
call has no such form. `null` has none either, so "not set" needs a sentinel: `""`, `-1`, or
`Void.class` for a `Class` element.

The value must be a constant expression in the language's sense, and a `static final` field is not
always one:

```java
final class Limits {
    static final int MAX_ATTEMPTS = 3;
    static final int FROM_ENV = Integer.parseInt("3");
    static final Integer BOXED = 3;
}

@interface Retry {
    int attempts() default Limits.MAX_ATTEMPTS;   // compiles
    long backoffMs() default 60 * 1000;           // compiles
    int fromEnv() default Limits.FROM_ENV;        // error
    int boxed() default Limits.BOXED;             // error
}
```

```text
Retry.java:10: error: element value must be a constant expression
    int fromEnv() default Limits.FROM_ENV;        // error
                                ^
Retry.java:11: error: element value must be a constant expression
    int boxed() default Limits.BOXED;             // error
                              ^
2 errors
```

A team that wants the retry count to come from configuration can't put it in the annotation, as a
default or at the use site. The annotation names a key (`@Retry(attemptsKey = "orders.retries")`),
and the code that reads the annotation looks the value up.

## Defaults Live in the Annotation Type, and Only value Can Go Unnamed

A small web library declares `@Endpoint`, and a service uses it:

```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint {
    String value();                    // the path
    String method() default "GET";
    int timeoutMs() default 2000;
    String[] produces() default {"application/json"};
}
```

```java
import java.lang.reflect.Method;

public class Orders {
    @Endpoint("/orders")
    public void list() { }

    @Endpoint(value = "/orders", method = "POST", produces = "text/csv")
    public void export() { }

    public static void main(String[] args) throws Exception {
        for (String name : new String[] {"list", "export"}) {
            Method m = Orders.class.getMethod(name);
            Endpoint e = m.getAnnotation(Endpoint.class);
            System.out.println(name + ": " + e);
            System.out.println("  " + e.method() + " " + e.value() + ", timeout " + e.timeoutMs() + " ms");
        }
    }
}
```

```text
list: @Endpoint(method="GET", produces={"application/json"}, timeoutMs=2000, value="/orders")
  GET /orders, timeout 2000 ms
export: @Endpoint(method="POST", produces={"text/csv"}, timeoutMs=2000, value="/orders")
  POST /orders, timeout 2000 ms
```

`getAnnotation` returns an object of the annotation type; each element call returns the use-site
value or the default. A single value for an array element needs
no braces (`produces = "text/csv"`).

Now the library's next release lowers the default to `int timeoutMs() default 500;`. We recompile
`Endpoint.java` alone and run the same `Orders.class`:

```text
list: @Endpoint(method="GET", produces={"application/json"}, timeoutMs=500, value="/orders")
  GET /orders, timeout 500 ms
export: @Endpoint(method="POST", produces={"text/csv"}, timeoutMs=500, value="/orders")
  POST /orders, timeout 500 ms
```

The class file of `Orders` records only the elements written at the use site. The default is stored
once, in `Endpoint.class`, and filled in when the annotation is read. Upgrading the library jar
changes the timeout of every endpoint that didn't set one, with no rebuild of the service, so a
changed default belongs in the release notes.

`@Endpoint("/orders")` leaves out the name `value`. That is the only element whose name may go,
and only when every other element has a default. Declare `@Endpoint` with no default for `method`,
and the shorthand fails:

```java
@interface Endpoint {
    String value();
    String method();
}

class Users {
    @Endpoint("/users")
    void list() { }
}
```

```text
Endpoint.java:7: error: annotation @Endpoint is missing a default value for the element 'method'
    @Endpoint("/users")
    ^
1 error
```

## @Target Is Checked by javac at Every Use

A timing aspect wraps methods annotated `@Timed`, and someone writes `@Timed` on a field. With
`@Target(ElementType.METHOD)`, javac refuses it:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@interface Timed { }

class Checkout {
    @Timed
    private int attempts;

    @Timed
    void pay() { }
}
```

```text
Timed.java:8: error: annotation interface not applicable to this kind of declaration
    @Timed
    ^
1 error
```

Without `@Target`, the field compiles, the aspect never looks at fields, and nothing is timed. An annotation type with no `@Target` is accepted on every declaration, a type parameter
included, and in no type context:

```java
import java.util.List;

@interface Tag { }

class Box<@Tag T> {
    @Tag List<String> names;
    List<@Tag String> ids;
}
```

```text
Tag.java:7: error: annotation @Tag not applicable in this type context
    List<@Tag String> ids;
         ^
1 error
```

Annotations written on a type, such as `List<@NonNull String>`, need `ElementType.TYPE_USE` in
the target.

## A Repeated Annotation Is Stored Inside Its Container

The opening's service and its guard:

```java
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

public class Repeat {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Roles.class)
    @interface Role { String value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Roles { Role[] value(); }

    static class Payments {
        @Role("ADMIN")
        public void refund() { }

        @Role("ADMIN") @Role("AUDITOR")
        public void voidPayment() { }
    }

    /** The guard as first written: no @Role means anyone may call. */
    static boolean allowed(Method m, Set<String> granted) {
        Role role = m.getAnnotation(Role.class);
        return role == null || granted.contains(role.value());
    }

    public static void main(String[] args) throws Exception {
        Set<String> agent = Set.of("SUPPORT");
        for (String name : new String[] {"refund", "voidPayment"}) {
            Method m = Payments.class.getMethod(name);
            System.out.println(name + ", support agent allowed: " + allowed(m, agent));
        }
        System.out.println();
        for (String name : new String[] {"refund", "voidPayment"}) {
            Method m = Payments.class.getMethod(name);
            System.out.println(name + ":");
            System.out.println("  getAnnotation(Role.class)          " + m.getAnnotation(Role.class));
            System.out.println("  getAnnotation(Roles.class)         " + m.getAnnotation(Roles.class));
            System.out.println("  getAnnotationsByType(Role.class)   " + Arrays.toString(m.getAnnotationsByType(Role.class)));
            System.out.println("  getAnnotations()                   " + Arrays.toString(m.getAnnotations()));
        }
    }
}
```

```text
refund, support agent allowed: false
voidPayment, support agent allowed: true

refund:
  getAnnotation(Role.class)          @Repeat.Role("ADMIN")
  getAnnotation(Roles.class)         null
  getAnnotationsByType(Role.class)   [@Repeat.Role("ADMIN")]
  getAnnotations()                   [@Repeat.Role("ADMIN")]
voidPayment:
  getAnnotation(Role.class)          null
  getAnnotation(Roles.class)         @Repeat.Roles({@Repeat.Role("ADMIN"), @Repeat.Role("AUDITOR")})
  getAnnotationsByType(Role.class)   [@Repeat.Role("ADMIN"), @Repeat.Role("AUDITOR")]
  getAnnotations()                   [@Repeat.Roles({@Repeat.Role("ADMIN"), @Repeat.Role("AUDITOR")})]
```

`@Repeatable(Roles.class)` lets you write `@Role` more than once. When you do, javac writes a
single `@Roles({@Role("ADMIN"), @Role("AUDITOR")})` into the class file instead (`javap -v`
shows only `Roles` on `voidPayment`), and no `@Role` is directly on the method.
`getAnnotation(Role.class)` returns `null`, and the guard opened the method to anyone. A single
`@Role` is stored as itself, so `getAnnotation(Roles.class)` is `null` there. Code that reads only
the container breaks on the other half of the methods.

`getAnnotationsByType(Role.class)` looks through the container and returns every `@Role` in the
order written, for one or for many, so code that reads a repeatable annotation should use it.

javac checks the container against the repeatable annotation, among other rules: it needs a
`value()` element of the repeatable type's array, a retention at least as long, and targets that
are a subset of the repeatable type's. Forget its `@Retention`, and it defaults to `CLASS`:

```java
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Roles.class)
@interface Role { String value(); }

@interface Roles { Role[] value(); }   // no @Retention: CLASS
```

```text
Role.java:4: error: retention of containing annotation interface (Roles) is shorter than the retention of repeatable annotation interface (Role)
@Repeatable(Roles.class)
^
1 error
```

<!-- practice 1 -->

## @Inherited Reaches a Class Only from Its Superclasses

An audit interceptor logs every call to a class carrying `@Audited` and reads the level from it:

```java
import java.lang.annotation.*;

public class Inherit {
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface Audited { String level() default "INFO"; }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Owner { String value(); }

    @Audited(level = "FULL") interface PaymentApi { }
    @Owner("payments-team")
    @Audited(level = "FULL") static class BaseService { @Audited public void pay() { } }
    @Audited(level = "BASIC") static class CardService extends BaseService { }

    static class StripeService extends CardService { @Override public void pay() { } }
    static class LedgerService implements PaymentApi { }

    static void show(Class<?> c) {
        System.out.printf("%-14s getAnnotation: %-36s getDeclaredAnnotation: %s%n", c.getSimpleName(),
                c.getAnnotation(Audited.class), c.getDeclaredAnnotation(Audited.class));
    }

    public static void main(String[] args) throws Exception {
        show(CardService.class);
        show(StripeService.class);
        show(LedgerService.class);
        System.out.println("StripeService.pay() getAnnotation: "
                + StripeService.class.getMethod("pay").getAnnotation(Audited.class));
        System.out.println("StripeService @Owner: " + StripeService.class.getAnnotation(Owner.class));
    }
}
```

```text
CardService    getAnnotation: @Inherit.Audited(level="BASIC")      getDeclaredAnnotation: @Inherit.Audited(level="BASIC")
StripeService  getAnnotation: @Inherit.Audited(level="BASIC")      getDeclaredAnnotation: null
LedgerService  getAnnotation: null                                 getDeclaredAnnotation: null
StripeService.pay() getAnnotation: null
StripeService @Owner: null
```

`StripeService` declares nothing and still gets `BASIC`. When a class has no `@Audited` of its own,
`getAnnotation` asks its superclass, then that one's superclass, and returns the first it finds, so
the nearest declaration wins and `FULL` two levels up is never seen. `getDeclaredAnnotation` skips
that search. `@Owner` has no `@Inherited`, so it stays on `BaseService`.

`LedgerService` implements an audited
interface and isn't audited: the `@Inherited` javadoc says annotations on implemented interfaces
have no effect. `StripeService.pay()` overrides an audited method and isn't audited either:
`@Inherited` has no effect on anything but a class, even though `Audited` allows `METHOD`. A
developer who overrides `pay()` to add a log line has removed its auditing.

A superclass chain has one nearest declaration; a class can implement two interfaces that carry
different values. A library that wants interface or method annotations walks the hierarchy in its
own code, with its own rule for which declaration wins.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **An element holds a compile-time constant** of a primitive, `String`, `Class`, enum,
  annotation or a one-dimensional array of these. No `null`, no collections, nothing computed at
  run time: configuration can't reach an annotation.
- **Defaults live in the annotation type** and are filled in when the annotation is read, so a
  library that changes a default changes every caller that didn't set it. Only `value` may be
  written unnamed, and only when every other element has a default.
- **`@Target` is checked by javac at every use.** Without it, a misplaced annotation compiles and
  is silently ignored by whatever reads it.
- **A repeated annotation is stored inside its container.** `getAnnotation(Role.class)` misses a
  repeated `@Role`, and a guard that reads it opens the method; `getAnnotationsByType` finds every
  one.
- **`@Inherited` reaches a class only from its superclasses**, nearest first. Interfaces, methods
  and fields inherit nothing.

## Reference

**Element types (JLS 9.6.1)**

| Allowed | Not allowed (javac error) |
|---|---|
| `byte` `short` `int` `long` `float` `double` `char` `boolean` | wrappers such as `Integer` |
| `String`, `Class` (also `Class<? extends T>`), an enum, an annotation type | `Object`, `List`, any other class |
| a one-dimensional array of the above | `String[][]`; a `null` value or default |

**Reading annotations (`AnnotatedElement`, Java 21)**

| Method | Sees a repeated annotation inside its container | Sees a class annotation inherited through `@Inherited` |
|---|---|---|
| `getAnnotation(A)` | no | yes |
| `getDeclaredAnnotation(A)` | no | no |
| `getAnnotationsByType(A)` | yes | yes |
| `getDeclaredAnnotationsByType(A)` | yes | no |
| `getAnnotations()` | returns the container itself | yes |

**Container rules checked by javac (Java 21)**

| Container | Error |
|---|---|
| no `value()` element | `Tags is not a valid @Repeatable, no value element method declared` |
| shorter retention | `retention of containing annotation interface (Roles) is shorter than ...` |
| more targets | `containing annotation interface (Tags) is applicable to more targets than ...` |
| another element without a default | `containing annotation interface (Tags3) does not have a default value for element other()` |
| not `@Inherited` while the repeatable type is | `repeatable annotation interface (Tag4) is @Inherited while containing annotation interface (Tags4) is not` |

## Challenges

1. A security library must decide whether a method with no `@Role` is open or closed. Which
   default would have made the opening's bug fail safe, and what would it cost a team adopting the
   library on an existing code base?
2. You need `@Endpoint` timeouts to differ between staging and production. Design the annotation
   and its reader so that no value is compiled into a class file.
3. Write a lookup that finds `@Audited` on an interface method when the implementing method has
   none. Decide what it does when two interfaces disagree, and when a superclass method disagrees
   with an interface.

## Design Note: Annotations Are Constants So the JVM Can Read Them Without Running Code

The element rules on this page follow from one choice: an annotation's values live in the class
file as constants. Reflection rebuilds an annotation from those bytes, a processor reads them
before the program exists, and a tool can read them from the class files without loading a class.

The cost is that an annotation can't express anything that is decided later: an environment's
timeout, a role list from a database, `null`. Those end up as keys and sentinels that some other
code resolves, and the annotation then states only part of what happens. The default is the
exception: it is looked up in the annotation type when the annotation is read, which is why an
upgrade can change it without a rebuild.

## Code Examples

- Test: [CustomAnnotationCreationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/CustomAnnotationCreationTest.java)
- Source: [CustomAnnotationCreation.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/CustomAnnotationCreation.java)
