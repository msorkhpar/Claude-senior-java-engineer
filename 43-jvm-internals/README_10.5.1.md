# 10.5.1. JVM Architecture (Class Loader, Execution Engine, Runtime Data Areas)

The billing service hangs on about one start in thirty. The last log line is "warming caches", the
CPU is idle, and the orchestrator kills the instance when its health check times out. On another
start it came up, and for an hour it sent invoices with 0 % VAT. Nobody changed the code between
those starts. Two classes, `Rates` and `Tariff`, read each other's static fields while they
initialise, and which class was touched first, and by how many threads at once, decided each
outcome.

Every output below is from Java 21 (Temurin 21.0.12) on Linux x64.

## A Class Is Loaded When Code First Needs It, and Initialised Only When Code First Uses It

An export service has three renderers. This run exports CSV only:

```java
public class Export {
    public static void main(String[] args) {
        System.out.println("main starts");
        Renderer r;
        if (args.length > 0) {
            r = new PdfRenderer();
        } else {
            r = new CsvRenderer();
        }
        r.render();
    }

    static void archive() {                 // never called
        new XlsxRenderer().render();
    }
}

abstract class Renderer {
    static { System.out.println("  init Renderer"); }
    abstract void render();
}

class CsvRenderer extends Renderer {
    static { System.out.println("  init CsvRenderer"); }
    void render() { System.out.println("csv written"); }
}

class PdfRenderer extends Renderer {
    static { System.out.println("  init PdfRenderer"); }
    void render() { System.out.println("pdf written"); }
}

class XlsxRenderer extends Renderer {
    static { System.out.println("  init XlsxRenderer"); }
    void render() { System.out.println("xlsx written"); }
}
```

Which of the renderer classes does the JVM load, and whose static block runs?
`-Xlog:class+load` prints a line for each class the JVM loads. Of about 450 lines, 440 are JDK
classes mapped from the class-data-sharing archive (`source: shared objects file`) and a few more
come from the JDK's own modules (`source: jrt:/...`); with those filtered out:

```text
$ java -Xlog:class+load -cp out Export | grep -v "jrt:/\|shared objects"
[0.026s][info][class,load] Export source: file:/path/to/project/out/
[0.027s][info][class,load] Renderer source: file:/path/to/project/out/
[0.027s][info][class,load] PdfRenderer source: file:/path/to/project/out/
[0.027s][info][class,load] CsvRenderer source: file:/path/to/project/out/
main starts
  init Renderer
  init CsvRenderer
csv written
```

`XlsxRenderer` was never loaded: `archive()` never ran, and checking it needed nothing from
`XlsxRenderer`. `PdfRenderer` was loaded before `main` printed anything, yet its static block never
ran. Before `main` runs, the verifier checks `Export`'s bytecode; to prove that a `PdfRenderer` or a
`CsvRenderer` may be stored in a `Renderer` variable, it loads all three (the JVM specification
allows verification to load classes, JVMS §5.4.1). Loading reads the class file and creates the
`Class` object. **Initialisation**, which runs the static field initialisers and static blocks,
waits for the first `new`, call of a static method the class declares, or read or write of a
static field that isn't a compile-time constant (JLS §12.4.1). A superclass is initialised before
its subclass, so `Renderer` printed first.

> **An interface type changes what the verifier loads.** With `Renderer` as an interface, the same
> run loads `Renderer` before `main` and `CsvRenderer` only after "main starts", when `new` runs.
> `PdfRenderer` is never loaded. Don't rely on either: which classes the verifier
> loads is an implementation detail.

A static block is therefore not start-up code: a class whose initialiser opens a connection pool
runs it on the first request that uses the class, in that request's thread, and fails that request
if it throws. Unit 10.3.3 shows `Class.forName(name)` forcing initialisation, and a failed
initialiser turning every later use into `NoClassDefFoundError`.

## Each Loader Asks Its Parent First, So the JDK's Classes Always Come from the JDK's Loaders

Three loaders are built in. `getClassLoader()` names the one that defined a class:

```java
for (Class<?> c : new Class<?>[] {String.class, Connection.class, Who.class}) {
    System.out.printf("%-20s defined by %s%n", c.getName(), c.getClassLoader());
}
ClassLoader app = Who.class.getClassLoader();
System.out.println("app's parent:      " + app.getParent());
System.out.println("platform's parent: " + app.getParent().getParent());
```

```text
java.lang.String     defined by null
java.sql.Connection  defined by jdk.internal.loader.ClassLoaders$PlatformClassLoader@61bbe9ba
Who                  defined by jdk.internal.loader.ClassLoaders$AppClassLoader@639fee48
app's parent:      jdk.internal.loader.ClassLoaders$PlatformClassLoader@61bbe9ba
platform's parent: null
```

`null` is the bootstrap loader, which defines `java.base`. The platform loader defines other JDK
modules such as `java.sql`, and the application loader defines the class path. Asked for a class,
`ClassLoader.loadClass` first returns one this loader has already loaded, then asks its parent, and
only when the parent fails calls its own `findClass`. A request from the application loader for
`java.lang.String` therefore travels to the bootstrap loader, and a `String.class` on the class path
is never read.

A custom loader can't get around that by defining the class itself. This one, a `ClassLoader`
subclass whose `define` method calls `defineClass`, defines `Invoice` from its class file, then
tries the same name again, then a `java.lang` name:

```java
BytesLoader loader = new BytesLoader();
loader.define("Invoice", bytes);
try {
    loader.define("Invoice", bytes);
} catch (LinkageError e) {
    System.out.println(e);
}
try {
    loader.define("java.lang.Invoice", bytes);
} catch (SecurityException e) {
    System.out.println(e);
}
```

```text
java.lang.LinkageError: loader DefineTwice$BytesLoader @42a57993 attempted duplicate class definition for Invoice. (Invoice is in unnamed module of loader DefineTwice$BytesLoader @42a57993, parent loader 'app')
java.lang.SecurityException: Prohibited package name: java.lang
```

## A Class Is Its Name Plus the Loader That Defined It

The report server loads each customer's exporter plugin from its own directory with a
`URLClassLoader`. A plugin implements the host's `Exporter` interface, and its build copies the API
jar into the plugin, so the plugin directory holds `PdfExporter.class` and a copy of
`Exporter.class`. The plugin directory is not on the host's class path:

```java
public interface Exporter {
    String export(String report);
}
```

```java
public class PdfExporter implements Exporter {
    public String export(String report) { return report + ".pdf"; }
}
```

The host tries two parents for the plugin's loader:

```java
URL[] pluginDir = {Path.of("plugin").toUri().toURL()};   // holds PdfExporter and a copy of Exporter
for (ClassLoader parent : List.of(ClassLoader.getPlatformClassLoader(), Host.class.getClassLoader())) {
    try (URLClassLoader loader = new URLClassLoader("plugin", pluginDir, parent)) {
        Class<?> impl = loader.loadClass("PdfExporter");
        Class<?> itsApi = impl.getInterfaces()[0];
        System.out.println("parent: " + parent.getName());
        System.out.println("  PdfExporter defined by " + impl.getClassLoader().getName());
        System.out.println("  its Exporter defined by " + itsApi.getClassLoader().getName()
                + ", the host's Exporter? " + (itsApi == Exporter.class));
        Object plugin = impl.getDeclaredConstructor().newInstance();
        try {
            Exporter exporter = (Exporter) plugin;
            System.out.println("  " + exporter.export("q3-sales"));
        } catch (ClassCastException e) {
            System.out.println("  " + e.getMessage());
        }
    }
}
```

```text
parent: platform
  PdfExporter defined by plugin
  its Exporter defined by plugin, the host's Exporter? false
  class PdfExporter cannot be cast to class Exporter (PdfExporter is in unnamed module of loader 'plugin' @5c647e05; Exporter is in unnamed module of loader 'app')
parent: app
  PdfExporter defined by plugin
  its Exporter defined by app, the host's Exporter? true
  q3-sales.pdf
```

`PdfExporter` implements `Exporter`, and the cast still fails. With the platform loader as parent,
nobody above the plugin loader knows `Exporter`, so the plugin loader defines its own copy, and
`PdfExporter` implements that one. The JVM identifies a class at run time by its name and its
defining loader (JVMS §5.3), so the host's `Exporter` and the plugin's are two unrelated types
that print the same. Unit 9.1.1 shows the same split giving a singleton two instances.

With the application loader as parent, defining `PdfExporter` needs its superinterface, so the
plugin loader is asked for `Exporter`, passes the request up, and the application loader answers
with the host's class. The copy in the plugin directory is never read. The plugin loader is the
*initiating* loader of that `Exporter`, the application loader its *defining* loader, and
`getClassLoader()` reports the defining one.

<!-- practice 1 -->

## Static Initialisers Run in Textual Order, and a Cycle Lets Code Read a Field Before It Is Set

These are the two classes from the billing service:

```java
class Rates {
    static final Tariff DEFAULT_TARIFF = Tariff.STANDARD;
    static final int VAT_PERCENT = Integer.parseInt(System.getProperty("vat", "20"));
}

class Tariff {
    static final Tariff STANDARD = new Tariff(Rates.VAT_PERCENT);
    final int vatPercent;

    Tariff(int vatPercent) { this.vatPercent = vatPercent; }
}
```

`Billing` reads `Rates.DEFAULT_TARIFF` first when given `rates`, and `Tariff.STANDARD` first when
given `tariff`:

```text
$ java Billing rates
Rates.DEFAULT_TARIFF.vatPercent = 0
$ java Billing tariff
Tariff.STANDARD.vatPercent = 20
Rates.DEFAULT_TARIFF = null
```

Before any initialiser runs, the JVM sets every static field to its default: 0, `false` or `null`.
Initialising `Rates` runs its field initialisers top to bottom. The first one needs `Tariff`, so
`Tariff` is initialised in the middle of `Rates`, and its constructor reads `Rates.VAT_PERCENT`.
`Rates` is already being initialised by this same thread, and the JVM doesn't wait for itself: it
lets the read through (JLS §12.4.2), and the read returns the field's default, 0. Started from the
other end, `Tariff.STANDARD` is still `null` when `Rates` copies it. Each order leaves one wrong
value in a `final` field for the life of the process, and nothing throws during initialisation.

Swapping the two lines in `Rates` fixes the first order and not the second (the `rates` run then
prints 20; the `tariff` run still leaves `DEFAULT_TARIFF` null).
A compile-time constant does fix both: with `static final int VAT_PERCENT = 20;` both orders print
20 and the default tariff is set, because `javac` copies the value into `Tariff` (`bipush 20`) and
reading it initialises nothing. A rate read from configuration can't be a constant, so the lasting
fix is to break the cycle: one of the two classes must not read the other while it initialises.

<!-- practice 2 -->

## Two Threads That Initialise a Cycle from Both Ends Wait for Each Other Forever

A class is initialised once, under a per-class lock: a second thread that needs a class another
thread is initialising waits for it (JLS §12.4.2). Unit 5.2.3 relies on that lock for safe
publication. In the billing service, a request thread touches `Rates` while the cache warmer touches
`Tariff`. To make the timing repeatable, each class gets a static block, above its field
initialisers, that waits on a two-party `CyclicBarrier` until both threads have started; then:

```java
Thread request = new Thread(() -> System.out.println(Rates.DEFAULT_TARIFF), "http-1");
Thread warmer = new Thread(() -> System.out.println(Tariff.STANDARD), "cache-warmer");
```

After `join(2000)` on each thread:

```text
http-1: alive=true, state=RUNNABLE
    at Rates.<clinit>(Deadlock.java:32)
    at Deadlock.lambda$main$0(Deadlock.java:11)
    at java.base/java.lang.Thread.run(Thread.java:1583)
cache-warmer: alive=true, state=RUNNABLE
    at Tariff.<clinit>(Deadlock.java:38)
    at Deadlock.lambda$main$1(Deadlock.java:12)
    at java.base/java.lang.Thread.run(Thread.java:1583)
findDeadlockedThreads: null
```

`http-1` holds `Rates`'s initialisation lock and waits for `Tariff`'s; `cache-warmer` holds
`Tariff`'s and waits for `Rates`'s. On Java 21 both threads report `RUNNABLE`, not `BLOCKED`, and
`ThreadMXBean.findDeadlockedThreads()` (unit 5.3.4) returns `null`, because it tracks only
monitors and `java.util.concurrent` locks. A thread dump (`jcmd <pid> Thread.print`, trimmed)
shows it:

```text
"http-1"
   java.lang.Thread.State: RUNNABLE
	at Rates.<clinit>(Deadlock.java:32)
	- waiting on the Class initialization monitor for Tariff
	at Deadlock.lambda$main$0(Deadlock.java:11)
"cache-warmer"
   java.lang.Thread.State: RUNNABLE
	at Tariff.<clinit>(Deadlock.java:38)
	- waiting on the Class initialization monitor for Rates
	at Deadlock.lambda$main$1(Deadlock.java:12)
```

Without the barrier, the hang needs both threads inside their initialisers at once, which is why
the service hung on some starts and not others. Every later thread that touches either class joins
the wait. Breaking the cycle fixes both failures.

## Each Thread Has Its Own Stack and Program Counter; the Heap and Method Area Are Shared

The JVM specification names six runtime data areas (JVMS §2.5). Three belong to a thread, and they
show in a stack walk. This program recurses twice, then walks its own stack, and prints the top
frame of a second thread that is in `Thread.sleep`:

```java
static void countdown(int n) {
    if (n == 0) {
        StackWalker.getInstance().forEach(f -> System.out.printf("%-10s bci=%-2d line=%d%n",
                f.getMethodName(), f.getByteCodeIndex(), f.getLineNumber()));
        return;
    }
    countdown(n - 1);
}
```

```text
countdown  bci=12 line=15
countdown  bci=19 line=19
countdown  bci=19 line=19
main       bci=25 line=8
sleeper's top frame: java.base/java.lang.Thread.sleep0(Native Method)
```

Each call has its own frame with its own `n`, and each frame records the bytecode index of the
call it is waiting on: 12 (the `forEach` call) in the top `countdown` frame, 19 (the recursive
call) in the two below it. The frame that is executing holds the same kind of index, and that is
the thread's program counter. The sleeping thread's top frame is a native method: Java code sees
the frame but no bytecode index, and the native frames beneath it can't be walked.

| Area | One per | Holds | Seen in |
|---|---|---|---|
| pc register | thread | the bytecode the thread is executing; undefined in a native method | `bci` in the stack walk above |
| JVM stack | thread | one frame per call: locals, operand stack | the three `countdown` frames above; `-Xss` and `StackOverflowError`, unit 10.2.1 |
| Native method stack | thread | frames of native code | `sleep0(Native Method)` above |
| Heap | JVM | every object and array | `OutOfMemoryError: Java heap space`, unit 10.2.1; collectors, unit 10.2.2; fields shared between threads, unit 5.1.2 |
| Method area (HotSpot: Metaspace) | JVM | per-class metadata and bytecode, one copy per defining loader | `OutOfMemoryError: Metaspace`, unit 10.2.1; the two `Exporter` classes above |
| Run-time constant pool | class | the class's symbolic references, resolved on first use | `XlsxRenderer`, named in `archive()` and never loaded, above |

The execution engine runs the method area's bytecode on each thread's stack: unit 10.5.2 reads that
bytecode, unit 10.5.3 shows the JIT compiling the hot parts, and unit 10.5.4 inspects a running JVM
with its tools.

<!-- practice 3 -->

## Summary

- **A class is loaded when code first needs it, and initialised only when code first uses it.**
  The verifier can load a class that never runs; a static block runs on the first request that
  uses the class, in that request's thread.
- **Each loader asks its parent first.** `java.*` always comes from the JDK, and the loader you
  ask may not be the one that defines the class.
- **A class is its name plus its defining loader.** A plugin that defines its own copy of the
  host's interface can't be cast to it.
- **Static initialisers run in textual order, and a cycle reads a field before it is set.** The
  result is a default value in a `final` field, different for each first use.
- **Two threads initialising a cycle from both ends hang.** The threads show as `RUNNABLE` and the
  deadlock check misses them; a thread dump names the class initialisation monitor.
- **Stacks and program counters are per thread; the heap and method area are shared.**

## Reference

**The built-in loaders (Java 21):**

| Loader | `getClassLoader()` shows | Defines | Parent |
|---|---|---|---|
| bootstrap | `null` | `java.base` and other core JDK modules, such as `java.logging` | none |
| platform | `ClassLoaders$PlatformClassLoader` | other JDK modules, such as `java.sql` | bootstrap |
| application | `ClassLoaders$AppClassLoader` | the class path | platform |

A custom loader created with a `null` parent delegates to the bootstrap loader.

**What triggers what (JLS §12.4.1, JVMS §5.4):**

| Event | Loads | Initialises |
|---|---|---|
| verification of a method that names the class | may | no |
| `X.class`, `Class.forName(name, false, loader)` | yes | no |
| `new X()`, a static method `X` declares, reading or writing a non-constant static field of `X` | yes | yes |
| reading a compile-time constant of `X` | no | no |
| initialising a subclass of class `X` | yes | yes, `X` first (a superinterface only if it declares default methods) |
| `Class.forName(name)` | yes | yes (unit 10.3.3) |

**Errors on this page:**

| Error | Cause |
|---|---|
| `ClassCastException` whose message names two different loaders | a type defined by both, used across the boundary |
| `LinkageError: attempted duplicate class definition` | one loader defining a name twice |
| `SecurityException: Prohibited package name` | a custom loader's `defineClass` for a `java.*` name |

## Challenges

1. Your service needs a class's static initialiser to fail at start-up, not on the first request.
   Which ways of forcing initialisation are there, and which ones survive a refactoring that moves
   the class?
2. A plugin host loads child-first so each plugin can bring its own JSON library. Which packages do
   you send to the parent, and what breaks if a plugin returns a JSON object to the host?
3. Find a static-initialisation cycle in a code base you know. Which first use gives a default
   value, and could two threads reach it from both ends?

## Design Note: Why Delegation Goes to the Parent First

Asking the parent first means that a name resolves to the same class for every loader below the
one that defines it. The host and every plugin see one `String` and, with the application loader
as parent, one `Exporter`, so objects cross the boundary without casts failing. A loader below the
platform loader can't define a `java.*` class at all, so none can put its own `String` in front of
the JDK's.

The cost is that a child can't override anything its parent has. An application server that lets
each application use its own version of a library inverts the order for most classes, and then
has to list, by hand, every package that must still come from the parent. Each package missing
from that list is a class the server and the application each define, and the first object passed
between them fails a cast, as the plugin's `Exporter` did.

## Code Examples

- Test: [JvmArchitectureTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmArchitectureTest.java)
- Source: [JvmArchitecture.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JvmArchitecture.java)
