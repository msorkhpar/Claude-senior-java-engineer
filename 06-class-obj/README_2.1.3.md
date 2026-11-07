# 2.1.3 Instance and Static Members in Java

A week of tickets from the billing service: invoices printed with another invoice's date; the
gzip codec missing from the registry in production but present in every test; a hotfix that cut a
client timeout from 30 seconds to 5 and changed nothing; a standard price list that charges no
VAT; and after one mistyped property, `NoClassDefFoundError` on every price request until a
restart. Each one traces to a static field or a static initializer.

## A Static Field Is One Variable Shared by Every Object and Every Thread

An instance field has one copy per object. A static field has one copy per class, and every
object, every method and every thread reads and writes that copy. The invoice service formats its
dates with one formatter:

```java
import java.text.SimpleDateFormat;
import java.util.Date;

class InvoiceDates {
    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd");

    static String day(Date d) { return DAY.format(d); }
}
```

Four threads each format their own date 100,000 times; the last line repeats the test with a
`DateTimeFormatter` in the static field:

```text
SimpleDateFormat: 333106 wrong dates, 0 exceptions in 400000 calls
  2024-01-31 printed as 2024-03-14
  2024-12-01 printed as 2024-03-14
DateTimeFormatter: 0 wrong dates, 0 exceptions in 400000 calls
```

`final` fixes the reference, not the object it points to. `SimpleDateFormat.format` stores the
date in the formatter's own `Calendar` field and then reads the fields back to print them; between
the two, another thread stores its date. The javadoc warns: "Date formats are not synchronized."
No thread asked for `2024-03-14`: it comes from a `Calendar` that another thread was overwriting,
and nothing was logged.

A static field is safe to share when what it holds is immutable, like `DateTimeFormatter` or a
constant, or built for concurrent use, like `AtomicLong`. Anything else in a static field needs
the locking of unit 6.2.1. Tests share it too: tests that run in one JVM, through one class
loader, see one copy, so a test that changes a static field changes it for the tests after it.

<!-- practice 1 -->

## A Class Is Initialized on Its First Active Use, Not When Its Name Appears

Static field initializers and `static` blocks run once, when the class is *initialized*. Each
codec registers itself from its static block:

```java
import java.util.Map;
import java.util.TreeMap;

class Codecs {
    static final Map<String, Codec> BY_NAME = new TreeMap<>();

    static void register(String name, Codec codec) { BY_NAME.put(name, codec); }
}

class Codec {
    static int created;                               // declared here, not in Gzip
    static { System.out.println("  Codec initialized"); }
}

class Gzip extends Codec {
    static final String NAME = "gzip";                // a constant variable
    static { System.out.println("  Gzip initialized"); Codecs.register(NAME, new Gzip()); }
    static String name() { return NAME; }
}
```

Which of these statements, each run in a fresh JVM, fills the registry?

```text
Gzip.class:
  codecs: []
Class.forName("Gzip", false, loader):
  codecs: []
new Gzip[8]:
  codecs: []
Gzip.NAME:
  codecs: []
Gzip.created:
  Codec initialized
  codecs: []
Gzip.name():
  Codec initialized
  Gzip initialized
  codecs: [gzip]
new Gzip():
  Codec initialized
  Gzip initialized
  codecs: [gzip]
Class.forName("Gzip"):
  Codec initialized
  Gzip initialized
  codecs: [gzip]
```

The JVM initializes a class on its first *active use*, such as `new`, a call to a static method it
declares, a read or write of a static field it declares (unless it is a constant variable, next
section), `Class.forName(name)`, or the initialization of a subclass. Its superclass is initialized first.
`Gzip.created` names `Gzip`, but the field is declared in `Codec`, so only `Codec` runs. A class
literal, `forName` with `false` and an array type load the class without initializing it, and
reading a constant doesn't touch it at all.

The billing service built its registry from `List.of(Gzip.class, Zstd.class)`, and in production
it stayed empty. The tests passed because one test ran `new Gzip()` first, and in a shared JVM that
initialized `Gzip` for every test after it. Don't depend on a side effect of initialization: let
the registry name its codecs, or find them with `ServiceLoader`.

## A Constant Variable Is Compiled Into the Code That Reads It

Unit 2.1.1 defined a *constant variable*: a `final` primitive or `String` whose initializer is a
constant expression. A client reads two limits from a library:

```java
public class Limits {
    public static final int TIMEOUT_SECONDS = 30;           // constant variable
    public static final Integer MAX_RETRIES = 3;            // not one: Integer is not primitive or String
    static { System.out.println("Limits initialized"); }
}
```

```java
public class Client {
    public static void main(String[] args) {
        System.out.println("timeout: " + Limits.TIMEOUT_SECONDS);
        System.out.println("retries: " + Limits.MAX_RETRIES);
    }
}
```

The hotfix changes them to 5 and 1, and only `Limits` is recompiled, as when a library jar is
replaced and the application jar isn't. The two runs, then `javap -c Client`:

```text
--- both compiled
timeout: 30
Limits initialized
retries: 3
--- only Limits recompiled
timeout: 30
Limits initialized
retries: 1
       3: ldc           #15                 // String timeout: 30
      11: getstatic     #23                 // Field Limits.MAX_RETRIES:Ljava/lang/Integer;
```

javac copied `30` into `Client` (folded into `"timeout: 30"`), so a new `Limits` can't change it
and reading it doesn't initialize `Limits`. Only the `MAX_RETRIES` read, a real `getstatic`, prints
`Limits initialized`. A value that can change apart from the code that reads it, such as a timeout
or a version string, shouldn't be a public constant variable: expose it through a static method.

## Static Initializers Run in Source Order, So an Early One Reads a Later Field's Default

The standard price list is a static instance, and VAT was recently made configurable:

```java
class Pricing {
    static final Pricing STANDARD = new Pricing();
    static final int VAT_PERCENT = Integer.getInteger("vat.percent", 20);

    private final int vatPercent;

    private Pricing() {
        vatPercent = VAT_PERCENT;
    }

    long gross(long netCents) {
        return netCents + netCents * vatPercent / 100;
    }
}
```

```text
VAT_PERCENT:           20
STANDARD.gross(10000): 10000
```

Initialization runs the static initializers top to bottom. `STANDARD` comes first, so its
constructor reads `VAT_PERCENT` while it still holds its default, `0`. With the literal `20`, the
field was a constant variable, inlined into the constructor, and the same code printed `12000`.
Reading it from configuration made it an ordinary field, and standard orders lost their VAT
without an exception. Declare a static instance after every
static field its constructor reads. Two classes whose initializers read each other have the same
problem, and which one sees a default depends on which class the program touches first (see the
Reference).

## A Static Initializer That Throws Leaves the Class Unusable Until the JVM Restarts

The tax table parses its rates when the class is initialized. An operator sets `-Dvat.de=19%`, and
three requests ask for the French rate:

```java
import java.util.Map;

class TaxTable {
    private static final Map<String, Integer> PERCENT = Map.of(
            "DE", Integer.parseInt(System.getProperty("vat.de", "19")),
            "FR", Integer.parseInt(System.getProperty("vat.fr", "20")));

    static int percent(String country) { return PERCENT.get(country); }
}
```

```text
request 1: java.lang.ExceptionInInitializerError
  cause: java.lang.NumberFormatException: For input string: "19%"
request 2: java.lang.NoClassDefFoundError: Could not initialize class TaxTable
  cause: java.lang.ExceptionInInitializerError: Exception java.lang.NumberFormatException: For input string: "19%" [in thread "main"]
request 3: java.lang.NoClassDefFoundError: Could not initialize class TaxTable
  cause: java.lang.ExceptionInInitializerError: Exception java.lang.NumberFormatException: For input string: "19%" [in thread "main"]
```

When an initializer throws, the JVM marks the class as failed and never runs the initializer
again. The first use gets `ExceptionInInitializerError` wrapping the exception (an `Error` is
rethrown as it is); every later use gets `NoClassDefFoundError`, which reads like a classpath
problem, with the first failure named in its cause. A bad German rate took down the French one,
and correcting the property changed nothing until a restart.

Check configuration at startup, where the error can name the setting. Load anything that can
fail, such as a file or a remote call, in a method rather than an initializer: a method that throws
can be called again. Moving the load into a nested holder class, whose static field is built on
first use, makes it lazy but still not retryable.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A static field is one variable shared by every object and every thread.** `final` doesn't
  make what it holds safe to share; keep immutable or concurrent objects there.
- **A class is initialized on its first active use.** A class literal, `forName(..., false, ...)`
  or an inherited static member reached through a subclass doesn't run the subclass's static block.
- **A constant variable is compiled into its readers.** Changing it needs every reader
  recompiled, and reading it initializes nothing.
- **Static initializers run in source order.** A static instance declared before the fields its
  constructor reads sees their defaults.
- **An initializer that throws breaks the class until restart.** First
  `ExceptionInInitializerError`, then `NoClassDefFoundError`; load what can fail in a method.

## Reference

**Other facts about static members (Java 21):**

| Situation | What happens |
|---|---|
| a static method reads an instance field or `this` | compile error: `non-static variable count cannot be referenced from a static context` |
| a static field reached through an instance, `a.hits++` | compiles and changes the one static field; `javac -Xlint:static` warns |
| one class loaded through two class loaders | two classes with two sets of static fields: a counter at 4 in one reads 1 in the other |
| `Fees.CARD = Rates.BASE + 30` and `Rates.BASE = Fees.CARD - 30 + 100` | the class touched second reads the first's default: `Fees` first gives `CARD=100 BASE=70`, `Rates` first `CARD=30 BASE=100` |
| a nested holder class's static field | built on first use, once even when two threads ask together; if it throws, later uses get `NoClassDefFoundError` |
| static members in an inner class | allowed since Java 16 |

## Challenges

1. List the static fields in a service you know whose type is neither immutable nor built for
   concurrent use. Which of them are written after the class is initialized?
2. Your library publishes `public static final String VERSION = "4.2.0"`. Which consumers report a
   stale version after an upgrade, and how would you publish it instead?
3. Forty classes read configuration in static initializers. How would you make a bad value fail
   the deploy instead of the first request that touches the class?

## Design Note: Why Java Initializes Classes Lazily

Initializing a class on first use lets a program start without running code for the thousands of
classes on its classpath that it never touches, and lets a class depend on others without a
declared order. The cost is that the order of static initialization between classes is decided
by the order in which a run happens to touch them, not by the source. An initializer that reads another class,
configuration or the network behaves one way in a test that touched classes in one order and
another way in production, and its failure surfaces far from its cause. That is why frameworks
build shared objects explicitly at startup, in an order they control.

## Code Examples

- Test: [StaticInstanceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/StaticInstanceTest.java)
- Source: [StaticInstance.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/StaticInstance.java)
