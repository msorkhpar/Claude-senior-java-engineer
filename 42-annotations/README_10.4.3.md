# 10.4.3. Annotation Retention Policies (SOURCE, CLASS, RUNTIME)

A payments team writes its own `@Transactional` and a small interceptor that opens a transaction
around every method carrying it. The build is clean, even with `-Xlint:all`, and the tests of the
transfer logic pass. In production a transfer fails between the debit and the credit, and the
debit stays. The interceptor's log has no `BEGIN` for any call: it never found the annotation.

An annotation's **retention policy**, `@Retention(RetentionPolicy.X)` on its declaration, decides
whether it reaches the class file and whether reflection sees it (unit 10.4.2 covers the rest of a
declaration). Every output below is from Java 21 (Temurin 21.0.12).

## SOURCE Stops at the Compiler, CLASS Stops in the Class File, Only RUNTIME Reaches Reflection

Three annotations that differ only in their retention, on one method:

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;

@Retention(RetentionPolicy.SOURCE)  @interface AuditedSource  { String value(); }
@Retention(RetentionPolicy.CLASS)   @interface AuditedClass   { String value(); }
@Retention(RetentionPolicy.RUNTIME) @interface AuditedRuntime { String value(); }

class Payments {
    @AuditedSource("refunds") @AuditedClass("refunds") @AuditedRuntime("refunds")
    void refund(long cents) { }
}

public class Retentions {
    public static void main(String[] args) throws Exception {
        Method refund = Payments.class.getDeclaredMethod("refund", long.class);
        System.out.println("AuditedSource:  " + refund.getAnnotation(AuditedSource.class));
        System.out.println("AuditedClass:   " + refund.getAnnotation(AuditedClass.class));
        System.out.println("AuditedRuntime: " + refund.getAnnotation(AuditedRuntime.class));
        System.out.println("all:            " + Arrays.toString(refund.getDeclaredAnnotations()));
    }
}
```

```text
AuditedSource:  null
AuditedClass:   null
AuditedRuntime: @AuditedRuntime("refunds")
all:            [@AuditedRuntime("refunds")]
```

`javap -v -p -cp out Payments` shows where the other two went (the method's part of its output):

```text
  void refund(long);
    descriptor: (J)V
    flags: (0x0000)
    Code:
      stack=0, locals=3, args_size=2
         0: return
      LineNumberTable:
        line 12: 0
    RuntimeVisibleAnnotations:
      0: #14(#15=s#16)
        AuditedRuntime(
          value="refunds"
        )
    RuntimeInvisibleAnnotations:
      0: #18(#15=s#16)
        AuditedClass(
          value="refunds"
        )
```

javac wrote `AuditedRuntime` into a `RuntimeVisibleAnnotations` attribute, the one reflection
reads. `AuditedClass` went into `RuntimeInvisibleAnnotations`, value and all, which reflection
doesn't read. `AuditedSource` isn't in the class file: the name appears nowhere in `javap -v`'s
output, constant pool included.

The other two have their own readers. SOURCE annotations are for the compiler and for annotation
processors: `@Override` (unit 10.4.1) is SOURCE, and a processor run over this file (unit 10.4.4
builds one) reports all three annotations on `refund`. CLASS annotations are for tools that read
class files, as `javap` just did.

The costly misreading is that CLASS means "compile time only". A CLASS annotation ships in every
class file that carries it, with its values: `value="refunds"` above. A note such as
`@Review("fraud check skipped for partner 12")` with CLASS retention can be read by anyone who runs
`javap -v` on the jar; only SOURCE retention keeps it out.

<!-- practice 1 -->

## An Annotation Without @Retention Is CLASS, and Reflection Returns null for It

The opening's `@Transactional`, with the interceptor reduced to its check:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Target(ElementType.METHOD)
@interface Transactional { }                       // no @Retention

class TransferService {
    @Transactional
    void transfer(String from, String to, long cents) {
        System.out.println("  debit " + from + ", credit " + to + ": " + cents);
    }
}

public class TxDemo {
    /** What a framework does before calling a method: look for the annotation. */
    static void call(Object target, Method m, Object... args) throws Exception {
        boolean tx = m.isAnnotationPresent(Transactional.class);
        if (tx) System.out.println("  BEGIN");
        m.invoke(target, args);
        if (tx) System.out.println("  COMMIT");
    }

    public static void main(String[] args) throws Exception {
        Method transfer = TransferService.class.getDeclaredMethod(
                "transfer", String.class, String.class, long.class);
        System.out.println("getAnnotation: " + transfer.getAnnotation(Transactional.class));
        System.out.println("@Retention on Transactional: " + Transactional.class.getAnnotation(Retention.class));
        call(new TransferService(), transfer, "acct-17", "acct-42", 12_500L);
    }
}
```

```text
getAnnotation: null
@Retention on Transactional: null
  debit acct-17, credit acct-42: 12500
```

`javac -Xlint:all` printed nothing for this file. The interceptor can't tell an invisible
annotation from no annotation, so nothing fails. `javap -v` shows the annotation in the class file, under
`RuntimeInvisibleAnnotations`.

The `Retention` javadoc gives the rule: with no `@Retention` on the declaration, the policy is
`CLASS`. Asking the annotation type for its `@Retention` returns `null`, not `CLASS`, so code that
checks a policy has to treat `null` as `CLASS` itself. Adding `@Retention(RUNTIME)` fixes it:

```diff
 @Target(ElementType.METHOD)
-@interface Transactional { }                       // no @Retention
+@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
+@interface Transactional { }
```

```text
getAnnotation: @Transactional()
@Retention on Transactional: @java.lang.annotation.Retention(RUNTIME)
  BEGIN
  debit acct-17, credit acct-42: 12500
  COMMIT
```

A test that calls `transfer()` directly passes with either declaration. Only a test that goes
through the interceptor and checks that a transaction was opened tells them apart.

<!-- practice 2 -->

## javac Applies Retention When It Compiles the Annotated Class, and Reflection Checks It Again

Suppose the fix goes into a shared library: `Audited` changes from CLASS to RUNTIME and the library
is released, but the service whose `Orders` class carries `@Audited` isn't rebuilt. `Audited.java`
is the two lines `import java.lang.annotation.*;` and
`@Retention(RetentionPolicy.CLASS) @interface Audited { }`, with `RUNTIME` in the second step.
We compile all three files, then change and recompile `Audited` alone, then recompile `Orders`,
then set `Audited` back to CLASS and recompile it alone. `Check` prints the annotation on
`Orders.cancel` after each step:

```java
class Orders {
    @Audited void cancel(long id) { }
}
```

```java
public class Check {
    public static void main(String[] args) throws Exception {
        System.out.println(Orders.class.getDeclaredMethod("cancel", long.class).getAnnotation(Audited.class));
    }
}
```

```text
== all three compiled, Audited is CLASS
null
== Audited recompiled as RUNTIME, Orders not
null
== Orders recompiled
@Audited()
== Audited recompiled as CLASS again, Orders not
null
```

javac chose the attribute for `@Audited` when it compiled `Orders`, from the retention `Audited`
had at that moment. Recompiling `Audited` doesn't touch `Orders.class`, which still says invisible.
The last step shows that reflection checks the type too: `Orders.class` now has `@Audited` under
`RuntimeVisibleAnnotations`, but the loaded `Audited` is CLASS, so reflection returns `null`. An
annotation reaches reflection only when the class file's attribute and the annotation type's
current retention both say RUNTIME.

So raising a retention to RUNTIME takes effect only after every user is recompiled. Lowering it
hides the annotation from reflection at once, but its values stay in old class files until they
are recompiled.

## A RUNTIME Annotation Whose Class Is Missing at Run Time Disappears Without an Error

An annotation's jar is often on the compile class path and missing from the deployment: a
`provided` dependency the server was supposed to supply, or a plug-in class loader that can't see
the API. Here a new `Audited`, RUNTIME and with a value, is on `Orders` and on `cancel`. `Scan`
reads the annotations the way a framework's class scanner does. Then
`Audited.class` is deleted and `Scan` runs again:

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Audited { String value(); }
```

```java
@Audited("orders")
class Orders {
    @Audited("cancellations")
    void cancel(long id) { System.out.println("  cancel(" + id + ") ran"); }
}
```

```java
import java.lang.reflect.Method;
import java.util.Arrays;

/** Scans by reflection without naming the annotation type. */
public class Scan {
    public static void main(String[] args) throws Exception {
        Class<?> orders = Class.forName("Orders");
        Method cancel = orders.getDeclaredMethod("cancel", long.class);
        System.out.println("class:  " + Arrays.toString(orders.getDeclaredAnnotations()));
        System.out.println("method: " + Arrays.toString(cancel.getDeclaredAnnotations()));
        var o = orders.getDeclaredConstructor().newInstance();
        cancel.invoke(o, 7L);
    }
}
```

```text
== with Audited.class
class:  [@Audited("orders")]
method: [@Audited("cancellations")]
  cancel(7) ran
== without Audited.class
class:  []
method: []
  cancel(7) ran
```

Reflection returned empty arrays and `cancel` ran, with no exception and no warning. `Orders.class` still holds `Audited(value="cancellations")` under
`RuntimeVisibleAnnotations`; reflection drops an annotation whose type it can't load. If the
annotation meant "audit this", auditing is off, and the first sign is an audit table with no rows.

Naming the missing type does fail: `getAnnotation(Audited.class)` throws
`java.lang.NoClassDefFoundError: Audited` at that line. And when the annotation type is present but
a class named in one of its elements isn't, the annotation comes back and fails only when that
element is read:

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Retry { Class<? extends Exception> on(); int times(); }

class GatewayTimeout extends Exception { }

class Client {
    @Retry(on = GatewayTimeout.class, times = 3)
    void charge() { }
}

public class Named {
    public static void main(String[] args) throws Exception {
        Retry r = Client.class.getDeclaredMethod("charge").getAnnotation(Retry.class);
        System.out.println("annotation: " + r);
        System.out.println("times:      " + r.times());
        System.out.println("on:         " + r.on());
    }
}
```

With `GatewayTimeout.class` deleted:

```text
annotation: @Retry(on=GatewayTimeout.class /* Warning: type not present! */, times=3)
times:      3
Exception in thread "main" java.lang.TypeNotPresentException: Type GatewayTimeout not present
```

The `AnnotatedElement` javadoc specifies this `TypeNotPresentException`, and an
`EnumConstantNotPresentException` for an enum constant that no longer exists. A start-up check
that names each annotation type the service relies on, reads its elements, and asserts that the
annotation is present where it is expected turns a missing jar into a failed start, and catches a
retention that is too short as well.

<!-- practice 3 -->

## Summary

- **SOURCE stops at the compiler, CLASS in the class file, RUNTIME reaches reflection.** A CLASS
  annotation ships with its values; only SOURCE keeps a note out of the jar.
- **No `@Retention` means CLASS.** `getAnnotation` returns `null`, javac doesn't warn, and an
  interceptor silently does nothing.
- **Retention is applied when the annotated class is compiled and checked again when it is
  read.** Raising it to RUNTIME needs every user recompiled.
- **A missing annotation class makes the annotation vanish; the class still loads.** Naming the type
  throws `NoClassDefFoundError`; a missing class inside an element throws
  `TypeNotPresentException` when that element is read.

## Reference

**Where javac 21 writes an annotation (`javap -v`):**

| Placed on | SOURCE | CLASS | RUNTIME | Read at run time with |
|---|---|---|---|---|
| class, method, field | not written | `RuntimeInvisibleAnnotations` | `RuntimeVisibleAnnotations` | `getAnnotation`, `getDeclaredAnnotations` |
| method parameter | not written | `RuntimeInvisibleParameterAnnotations` | `RuntimeVisibleParameterAnnotations` | `Method.getParameterAnnotations()` |
| type use (`List<@X String>`) | not written | `RuntimeInvisibleTypeAnnotations` | `RuntimeVisibleTypeAnnotations` | `getAnnotatedType()` and `AnnotatedType`, for fields, parameters and return types; nothing inside a method body |
| local variable, declaration annotation (`@Target(LOCAL_VARIABLE)`) | not written | not written | not written | nothing |

**Retention of the JDK's own annotations (read with `getAnnotation(Retention.class)`):**

| Retention | Annotations |
|---|---|
| SOURCE | `@Override`, `@SuppressWarnings` |
| RUNTIME | `@Deprecated`, `@FunctionalInterface`, `@SafeVarargs`, `@Retention`, `@Target`, `@Inherited`, `@Documented`, `@Repeatable` |

## Challenges

1. Your service relies on five annotation types from three libraries. Write the start-up check
   that fails when any of them can't be read. Which failures on this page does it catch, and which
   still need a test through the interceptor?
2. A build step leaves `provided` jars out of the container image. How would you find, from the
   class files alone, every annotation type your classes carry that the image doesn't contain?
3. A library wants its annotation's values out of its users' class files without breaking users
   compiled against the RUNTIME version. What do those users' class files and reflection show at
   each step of the change?

## Design Note: Why a Missing Annotation Is Ignored

The language treats an annotation as information attached to a program element, with no effect
at run time of its own (JLS 9.7). The JVM loads a class without loading its annotation types, and
reflection skips the ones it can't load. That lets one class carry RUNTIME annotations for
frameworks that one deployment includes and another leaves out, each needing its jar only where
it is read.

This costs you when an annotation does change behaviour, through an interceptor or a scanner.
Then its absence changes behaviour too, and nothing reports it. A retention that is too short
fails the same way. Neither the compiler nor the JVM can tell that you meant the annotation to
matter, so the check has to come from the code that depends on it: a test through the interceptor,
or a start-up check that names the types it needs.

## Code Examples

- Test: [RetentionPoliciesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/RetentionPoliciesTest.java)
- Source: [RetentionPolicies.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/RetentionPolicies.java)
