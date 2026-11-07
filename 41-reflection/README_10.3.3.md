# 10.3.3. Dynamic Object Creation and Method Invocation

At 14:00 a config change sets `exporter=com.example.export.CvsExporter`. The service restarts,
the health checks pass, and the deploy is marked green. At 02:00 the nightly export dies with
`ClassNotFoundException: com.example.export.CvsExporter`. Somebody swapped two letters, and
nothing failed for twelve hours.

Creating an object or calling a method from a name moves checks the compiler would have made to
run time. A factory built on this is unit 9.1.2, `ServiceLoader` is unit 8.1.5, and `Proxy` is
unit 9.2.3.

## A Class Named in Configuration Fails at First Use, Unless Startup Resolves It

The service keeps the class name and loads the class when the first report is exported (the
demo exports right after startup):

```java
public String export(String report) throws ReflectiveOperationException {
    Exporter e = (Exporter) Class.forName(className).getDeclaredConstructor().newInstance();
    return e.export(report);
}
```

With the right name and with the typo:

```text
$ java com.example.export.Main lazy com.example.export.CsvExporter
started, exporter = com.example.export.CsvExporter
02:00 nightly export:
sales-2026-11.csv
$ java com.example.export.Main lazy com.example.export.CvsExporter
started, exporter = com.example.export.CvsExporter
02:00 nightly export:
Exception in thread "main" java.lang.ClassNotFoundException: com.example.export.CvsExporter
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
```

The same typo in source never reaches a deploy:

```text
$ javac Typo.java
Typo.java:4: error: cannot find symbol
    Exporter exporter = new CvsExporter();
                            ^
  symbol:   class CvsExporter
  location: class Typo
1 error
```

A name in a string is checked only when a class loader is asked for it. If startup doesn't ask,
the first request that needs the class does: here a batch job twelve hours after the deploy,
when rolling back means another deploy.

Resolve the name at startup and check its type:

```java
public CheckedService(String name) throws ClassNotFoundException {
    exporterClass = Class.forName(name, false, CheckedService.class.getClassLoader())
                         .asSubclass(Exporter.class);
    System.out.println("started, exporter = " + exporterClass.getName());
}
```

```text
$ java com.example.export.Main checked com.example.export.CvsExporter
Exception in thread "main" java.lang.ClassNotFoundException: com.example.export.CvsExporter
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
$ java com.example.export.Main checked com.example.export.ReportJob
Exception in thread "main" java.lang.ClassCastException: class com.example.export.ReportJob
	at java.base/java.lang.Class.asSubclass(Class.java:4104)
	at com.example.export.CheckedService.<init>(CheckedService.java:9)
```

`ReportJob` exists but isn't an `Exporter`, and `asSubclass` checks that against the class
object. The cast `(Class<? extends Exporter>) Class.forName(name, false, loader)` compiles with an unchecked warning and checks
nothing, because generics are erased: `ReportJob` would pass startup and fail with a
`ClassCastException` at the line that takes its first instance as an `Exporter`, before any
`Exporter` method is called. The message of
`asSubclass` names only the class, so wrap it with the config key that named it.

## Class.forName(name) Runs the Static Initialiser; the Three-Argument Form Waits for First Use

The startup check now touches every configured plug-in. Which of their code runs? Three exporters
in the default package have a static block each. `PdfExporter` and `CsvExporter` print a line,
and `XlsxExporter` reads a setting it requires:

```java
class XlsxExporter {
    static final String TEMPLATE_DIR = required("XLSX_TEMPLATES");

    static String required(String name) {
        String value = System.getenv(name);
        if (value == null) throw new IllegalStateException(name + " is not set");
        return value;
    }
}
```

The program reaches each one differently, then calls `forName("XlsxExporter")` twice:

```java
System.out.println("PdfExporter.class:");
Class<?> literal = PdfExporter.class;
System.out.println("forName(name, false, loader):");
Class<?> lazy = Class.forName("PdfExporter", false, loader);
System.out.println("first instance:");
lazy.getDeclaredConstructor().newInstance();
System.out.println("forName(name):");
Class.forName("CsvExporter");
```

```text
PdfExporter.class:
forName(name, false, loader):
first instance:
  PdfExporter: static block runs
forName(name):
  CsvExporter: static block runs
forName on a class whose static block fails:
  java.lang.ExceptionInInitializerError
    caused by java.lang.IllegalStateException: XLSX_TEMPLATES is not set
  java.lang.NoClassDefFoundError: Could not initialize class XlsxExporter
    caused by java.lang.ExceptionInInitializerError: Exception java.lang.IllegalStateException: XLSX_TEMPLATES is not set [in thread "main"]
```

`Class.forName(name)` is `forName(name, true, callerLoader)`: it loads the class and
**initialises** it, which runs its static field initialisers and static blocks. A class literal
and `forName` with `false` only load it, and the initialiser runs at the first instance, static
method call or access to a static field that isn't a compile-time constant.

A failed initialiser is not retried. The class stays unusable, and every later use in that JVM
throws `NoClassDefFoundError: Could not initialize class`; on Java 21 its cause names the first
failure, as above. With one-argument `forName`, an exporter nobody uses tonight stops the service
from starting. With `false` the check confirms the name and the type,
and leaves the exporter's own setup for when it is used.

<!-- practice 1 -->

## Class.newInstance Lets the Constructor's Checked Exception Escape Undeclared

`Class.newInstance()` is shorter than `getDeclaredConstructor().newInstance()`, but deprecated
since Java 9 because of what it does when a constructor throws. A loader creates each plug-in
and skips the ones that fail:

```java
for (Class<?> type : plugins) {
    try {
        Object p = deprecated ? classNewInstance(type)
                              : type.getDeclaredConstructor().newInstance();
        System.out.println("loaded " + p.getClass().getSimpleName());
    } catch (ReflectiveOperationException e) {
        System.out.println("skipped " + type.getSimpleName() + ": " + e.getCause());
    }
}
```

The first plug-in's constructor is `SftpExporter() throws IOException`, and it throws:

```text
$ java LoadAll
skipped SftpExporter: java.io.IOException: sftp.example.com: connection refused
loaded LocalExporter
$ java LoadAll deprecated
Exception in thread "main" java.io.IOException: sftp.example.com: connection refused
	at SftpExporter.<init>(LoadAll.java:27)
	...
	at java.base/java.lang.Class.newInstance(Class.java:727)
	at LoadAll.classNewInstance(LoadAll.java:21)
	at LoadAll.main(LoadAll.java:10)
```

`Constructor.newInstance` wraps whatever the constructor throws in an
`InvocationTargetException`, which is a `ReflectiveOperationException`, so the catch runs and the
loop goes on. `Class.newInstance` declares only `InstantiationException` and
`IllegalAccessException`, and rethrows the constructor's exception as it is. The `IOException`
came out of a method that doesn't declare it, passed the catch and ended `main`, which declares
nothing either. One plug-in that can't reach its server stopped every other one from loading.

The caller can't catch it as an `IOException`, because the compiler sees none in the `try`; only
a broad `catch (Exception e)` sees it:

```text
CatchIo.java:8: error: exception IOException is never thrown in body of corresponding try statement
        } catch (IOException e) {
          ^
1 error
```

`javac -Xlint:deprecation` flags each use (`warning: [deprecation] newInstance() in Class has
been deprecated`). Replace it with `getDeclaredConstructor().newInstance()`, and take
`getCause()` of the `InvocationTargetException` where the constructor's own exception matters.

<!-- practice 2 -->

## A Method Handle Has a Type, and invokeExact Holds the Call Site to It

`Method.invoke` (unit 10.3.2) takes `Object` and `Object...`, so a wrong argument shows only at the call:
`apply(long)` called with `"2500"` throws `IllegalArgumentException: argument type mismatch`.
A `MethodHandle` is looked up with its full type, return type included, and each call is
checked against it. `Discount.apply(long)` returns 90 % of its argument:

```java
MethodHandle discount = MethodHandles.lookup().findVirtual(
        Discount.class, "apply", MethodType.methodType(long.class, long.class));
System.out.println("type: " + discount.type());

Discount d = new Discount();
long cents = (long) discount.invokeExact(d, 2_500L);
System.out.println("invokeExact(Discount, long) -> long: " + cents);

try {
    int narrowed = (int) discount.invokeExact(d, 2_500L);
} catch (Throwable t) { System.out.println("as int: " + t); }

Object rule = d;
try {
    long viaObject = (long) discount.invokeExact(rule, 2_500L);
} catch (Throwable t) { System.out.println("receiver typed Object: " + t); }

try {
    long fromInt = (long) discount.invokeExact(d, 2_500);
} catch (Throwable t) { System.out.println("argument int: " + t); }

System.out.println("invoke() adapts: " + (long) discount.invoke(rule, 2_500));
```

```text
type: (Discount,long)long
invokeExact(Discount, long) -> long: 2250
as int: java.lang.invoke.WrongMethodTypeException: handle's method type (Discount,long)long but found (Discount,long)int
receiver typed Object: java.lang.invoke.WrongMethodTypeException: handle's method type (Discount,long)long but found (Object,long)long
argument int: java.lang.invoke.WrongMethodTypeException: handle's method type (Discount,long)long but found (Discount,int)long
invoke() adapts: 2250
```

All four `invokeExact` calls compiled. `invokeExact` is signature-polymorphic: `javac` takes the
call site's type from the static types of the arguments and the cast on the result, and checks
nothing. At run time that type must equal the handle's, with no widening, boxing or upcasting.
Without the cast the call site's return type is `Object`, or `void` in a bare statement, and it
fails the same way. `invoke` converts as if through `asType`, which also casts an `Object`
receiver down to `Discount`.

The lookup is checked as well. Asking for an `apply` that returns `int` fails there, before any
call:

```text
lookup with int return: java.lang.NoSuchMethodException: no such method: Discount.apply(long)int/invokeVirtual
```

A handle moves the failure to the lookup, which can run at startup, and to a call site with the
wrong type, which fails on its first call. When the receiver is only known as `Object`, adapt the
handle once with `asType(MethodType.methodType(long.class, Object.class, long.class))` and keep
calling `invokeExact`. Unit 10.3.4 measures what each style costs.

<!-- practice 3 -->

## Summary

- **A class named in configuration fails at its first use.** Resolve every name at startup and
  check its type with `asSubclass`; an unchecked cast of the `Class` checks nothing.
- **`Class.forName(name)` runs the static initialiser.** `forName(name, false, loader)` loads
  without running it. A failed initialiser is never retried, and later uses throw
  `NoClassDefFoundError`.
- **`Class.newInstance` lets a constructor's checked exception escape undeclared.**
  `getDeclaredConstructor().newInstance()` wraps it in `InvocationTargetException`.
- **A method handle has a type.** The lookup checks it, and `invokeExact` throws
  `WrongMethodTypeException` when the call site's type differs; `invoke` adapts.

## Reference

| Call | Initialises the class | When the constructor throws |
|---|---|---|
| `Class.forName(name)` | yes | (no constructor runs) |
| `Class.forName(name, false, loader)` | no | (no constructor runs) |
| `X.class` | no | (no constructor runs) |
| `cls.getDeclaredConstructor().newInstance()` | yes | wrapped in `InvocationTargetException` |
| `cls.newInstance()` (deprecated since 9) | yes | rethrown as is, checked or not |

A static initialiser that throws gives `ExceptionInInitializerError` from whichever of these
initialises the class, and `NoClassDefFoundError` from every later use.

| Lookup or call | Checked when | Failure |
|---|---|---|
| `Class.forName` of a wrong name | that call | `ClassNotFoundException` |
| `cls.asSubclass(T.class)` | that call | `ClassCastException` |
| `(Class<? extends T>) cls` | never (erased) | `ClassCastException` where an instance is used as `T` |
| `lookup.findVirtual(c, name, type)` | at lookup | `NoSuchMethodException`, `IllegalAccessException` |
| `mh.invokeExact(...)` | each call, against the call site's type | `WrongMethodTypeException` |
| `mh.invoke(...)` | each call, after `asType` conversion | `WrongMethodTypeException` if no conversion exists; `ClassCastException` if a downcast fails |

## Challenges

1. Your service reads 40 plug-in names from a table that operators edit while it runs. Where do
   you resolve them, and what happens to a row that fails?
2. A plug-in's static initialiser opens a connection pool. When the database is down at startup,
   who should see the failure: the startup check, the first request, or a health endpoint?
3. Rewrite a `Method.invoke`-based dispatcher with one cached handle per method. Which errors move
   from the first request to startup, and which can't?

## Design Note: Why the Compiler Doesn't Check invokeExact

A method handle's type is a value that exists only after the lookup has run, so `javac` has
nothing to check a call against. It records the call site's type instead, and the JDK makes
`invokeExact` compare that type with the handle's at run time and refuse any difference. An exact
match needs no conversion at the call; `invoke` is for callers that want one. The cost is that a
missing cast compiles and fails on its first call, with a message that names two method types
rather than the mistake in the line.

## Code Examples

- Test: [DynamicCreationInvocationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/DynamicCreationInvocationTest.java)
- Source: [DynamicCreationInvocation.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/DynamicCreationInvocation.java)
