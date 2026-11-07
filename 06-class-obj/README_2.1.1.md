# 2.1.1 Class Structure and Components

One release of the billing service brought three tickets. The gateway client logs
`connecting to https://null:8443`, although its host field is set two lines down. A heap dump
shows the session cache of report rows holding a 50 MB PDF per row. Every scheduled retry ran
with the last attempt number. Constructors are unit 2.1.2, static members 2.1.3, inheritance 2.1.4.

## Initializers Run Top to Bottom, After super() and Before the Constructor Body

Predict the five lines `new Account()` prints:

```java
class Base {
    Base() {
        System.out.println("Base constructor");
    }
}

class Account extends Base {
    private String a = log("field a");
    {
        log("instance block");
    }
    private String b = log("field b");

    Account() {
        super();
        log("constructor body");
    }

    static String log(String s) {
        System.out.println(s);
        return s;
    }
}
```

```text
Base constructor
field a
instance block
field b
constructor body
```

javac copies the field initializers and instance blocks, in source order, into every constructor
that calls `super(...)`, explicitly or implicitly, right after that call. A constructor that starts
with `this(...)` gets no copy, so they run once per object. Until its initializer runs, a field holds
its default: `null`, `0` or `false` (unit 1.1.2).

### Spot the Problem: A Field Read Before Its Initializer Sees Its Default

The gateway client:

```java
class Endpoint {
    final String url = "https://" + host() + ":" + port();
    private final String host = System.getProperty("api.host", "api.example.com");
    private final int port = 8443;

    String host() { return host; }
    int port() { return port; }
}
```

Printing `new Endpoint().url`:

```text
https://null:8443
```

`url` is initialized first, while `host` is still `null`. Naming `host` directly there is an
`illegal forward reference`, but javac doesn't follow method calls. `port` is right because it is
a *constant variable* (`final`, primitive or `String`, constant initializer), and javac inlines
`8443` at every read. An initializer that calls a method is never constant.

A superclass constructor runs before all of this, so an overridden method it calls sees the same
defaults:

```java
import java.util.ArrayList;
import java.util.List;

abstract class Handler {
    final List<String> routes = new ArrayList<>();

    Handler() {
        routes.addAll(paths());                      // runs OrderHandler.paths()
    }

    abstract List<String> paths();
}

class OrderHandler extends Handler {
    private final String tenant;

    OrderHandler(String tenant) {
        this.tenant = tenant;
    }

    @Override
    List<String> paths() {
        return List.of("/" + tenant + "/orders");
    }
}
```

```text
[/null/orders]
```

`new OrderHandler("acme")` registered `/null/orders`, so every request for `/acme/orders` got a
404. Don't call an overridable method from a constructor or an initializer: build the routes on
first use, or have the subclass pass its values to `super(...)`.

> **Java 25 differs.** Flexible constructor bodies (JEP 513, final in Java 25) allow
> `this.tenant = tenant;` before `super();`, and the same handler prints `[/acme/orders]`. With
> `--release 21` javac rejects it.

<!-- practice 1 -->

## An Inner Class's Instance Holds Its Outer Object; a Static Nested Class's Doesn't

Rows of a report are cached in the user's session, which serializes them:

```java
import java.io.Serializable;

class Report {                                       // not Serializable
    private final byte[] pdf = new byte[50_000_000];
    private final String currency;

    Report(String currency) { this.currency = currency; }

    class Row implements Serializable {              // inner
        final String sku;
        final long cents;
        Row(String sku, long cents) { this.sku = sku; this.cents = cents; }
        String price() { return cents / 100.0 + " " + currency; }
    }

    static class StaticRow implements Serializable { // static nested
        final String sku;
        final long cents;
        final String currency;
        StaticRow(String sku, long cents, String currency) {
            this.sku = sku; this.cents = cents; this.currency = currency;
        }
        String price() { return cents / 100.0 + " " + currency; }
    }

    String currency() { return currency; }
}
```

We cache one row of each kind, set the `report` variable to `null`, call `System.gc()` and
serialize the row (the same result in 10 runs out of 10):

```text
inner:  report collected: false
inner:  java.io.NotSerializableException: Report
static: report collected: true
static: row serialized
```

An inner row is created as `report.new Row(...)`, and `price()` reads the report's `currency`. To
make that work, javac gives `Row` a hidden field, `this$0`, that refers to the report. The cached
row keeps the report's 50 MB reachable for the session's lifetime, and serialization follows
`this$0` to the non-serializable `Report`. Making `Report` serializable would
silence the exception and write the PDF into every session.

javac 21 adds the field only to an inner or anonymous class that uses its outer instance (reads
its fields or calls its instance methods) or is `Serializable`; lambdas are in the Reference. A
listener registered on a static registry pins its outer object the same way. Declare a nested class `static` unless each instance needs its outer object, and pass
it the values it needs.

<!-- practice 2 -->

## Local Classes and Lambdas Capture Copies of Effectively Final Locals

```java
for (int attempt = 1; attempt <= 3; attempt++) {
    retries.add(() -> System.out.println("retry " + attempt));
}
```

```text
LoopVar.java:8: error: local variables referenced from a lambda expression must be final or effectively final
```

A local variable lives in the method's stack frame, which may be gone by the time the lambda runs.
Java copies the value into the object: a local class that reads a local variable `region` gets a
field `val$region`. If the variable could change, copy and variable would disagree, so the
compiler forbids it. A one-element array satisfies the compiler:

```java
int[] holder = {0};                               // "effectively final" array, mutable element
for (int attempt = 1; attempt <= 3; attempt++) {
    holder[0] = attempt;
    shared.add(() -> System.out.print("retry " + holder[0] + "  "));
}
```

```text
retry 3  retry 3  retry 3  
```

All three lambdas copied a reference to the same array, and read it after the loop. Declare a new variable in
each iteration instead (`int n = attempt;`), which prints `retry 1  retry 2  retry 3`. An
enhanced `for` variable is already new in each iteration.

<!-- practice 3 -->

## A File Has at Most One Public Top-Level Class, and the File Is Named After It

`Invoice.java` declares `public class Invoice` and `public class TaxTable`:

```text
Invoice.java:5: error: class TaxTable is public, should be declared in a file named TaxTable.java
```

With `TaxTable` package-private, `Invoice.java` compiles. `Checkout.java`, in the same package,
uses `TaxTable`. `javac Checkout.java` on a clean checkout:

```text
Checkout.java:3: error: cannot find symbol
        System.out.println(new TaxTable().apply(2000));
                               ^
  symbol:   class TaxTable
  location: class Checkout
```

javac finds a type it hasn't compiled by its name: it looks for `TaxTable.class` or
`TaxTable.java` on the class path (here the current directory). Visibility isn't the problem:
compiled together, both files build. It also compiles once an older `TaxTable.class` exists, which
may be stale. Give each top-level class its own file; a helper only one class uses becomes a
`private static` nested class.

<!-- practice 4 -->

## Summary

- **Initializers run top to bottom, after `super()` and before the constructor body.** A field
  read earlier, from a method or from a superclass constructor, holds its default; a constant
  variable is inlined and hides the bug.
- **An inner class's instance holds its outer object.** It keeps the outer reachable and drags it
  into serialization; a static nested class doesn't.
- **Local classes and lambdas capture copies of effectively final locals.** A shared mutable
  holder brings back one variable for every lambda.
- **A file has at most one public top-level class.** javac finds classes by file name.

## Reference

**Creating an object (Java 21):**

| Step | What runs |
|---|---|
| 1 | memory with every field at its default |
| 2 | the constructor; if it starts with `this(...)`, that constructor first |
| 3 | `super(...)`, up to `Object` |
| 4 | field initializers and instance blocks, in source order |
| 5 | the rest of the constructor body |

**What a nested object keeps of its outer instance (javac 21):**

| Declared as | Field holding the outer instance |
|---|---|
| inner class using the outer instance | `this$0` |
| inner class not using it (static members don't count) | none |
| inner class implementing `Serializable` | `this$0`, even if unused |
| anonymous class in an instance method | `this$0` only if it uses the outer instance or is `Serializable` |
| lambda in an instance method | a captured `this` only if it uses one |
| static nested class | none |

## Challenges

1. Your framework's base class calls `init()` from its constructor. What do you tell the teams who
   override it, and what would you change in the framework?
2. Which inner or anonymous classes in your service outlive their outer object (listeners,
   cached callbacks), and which of those pin a large object?
3. A job captures a mutable `List` that the loop keeps filling. Is that a bug?

## Design Note: Why Java Copies Captured Values

The JVM has no lexical nesting: inner, local and anonymous classes are separate classes, and
javac gives them extra fields (`this$0`, `val$region`) for what they use from outside. Copying a
value instead of sharing the variable keeps the stack frame free to go. The cost shows when
lambdas must count or accumulate: they need a shared holder array, which races once they run on
different threads, or an `AtomicInteger`.

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/Person.java)
