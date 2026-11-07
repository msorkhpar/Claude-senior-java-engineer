# 2.2.1 Public, Private, Protected, and Default Access Modifiers in Java

A plugin team extends the reporting library's `Report` class in their own package and declares
`String header()` to put their brand on the export. It compiles, `-Xlint:all` included, and every
export still says `REPORT`.

The compiler decides access from where the code sits: in which class, in which package, in a
subclass or not.

## Private Is Checked per Class, Not per Object

A frozen account refuses deposits. Pause on `transferTo`: does it run that check?

```java
public class Account {
    private long balance;
    private boolean frozen;

    public Account(long balance) { this.balance = balance; }

    public void freeze() { frozen = true; }

    public void deposit(long cents) {
        if (frozen) throw new IllegalStateException("account is frozen");
        balance += cents;
    }

    public void transferTo(Account target, long cents) {
        balance -= cents;
        target.balance += cents;              // another Account's private field: this compiles
    }

    public long balance() { return balance; }

    public static void main(String[] args) {
        Account from = new Account(10_000);
        Account seized = new Account(0);
        seized.freeze();
        from.transferTo(seized, 2_500);
        System.out.println("seized account balance: " + seized.balance());
    }
}
```

```text
seized account balance: 2500
```

`private` means "only code written inside `Account`", and `transferTo` is such code, whichever
`Account` it touches. That is what lets `equals` and `compareTo` read the other object's fields. It
also means private fields don't protect one object's rules from another object of the same class:
the seized account took the money because `transferTo` wrote its field instead of calling
`target.deposit(cents)`. Go through the other object's methods whenever they enforce something.

Nested classes count as part of the class: an inner class reads its outer object's private
fields, and the outer class calls the inner class's private methods.

> **Before Java 11** the JVM checked `private` per class file, so javac compiled that access
> through generated package-private methods. `javap -p` on an outer class whose inner class reads
> its private field, compiled with `--release 8`, lists `static int access$000(Outer8)`; compiled
> for 21 it lists none, because the JVM now knows the two classes are **nestmates**
> (`Inner.class.getNestHost()` returns the outer class).

<!-- practice 1 -->

## No Modifier Means Package-Private, and a Subclass in Another Package Can't Override It

```java
package com.example.report;

public class Report {
    public String render() {
        return header() + " | 3 rows";
    }

    String header() {                      // no modifier: package-private
        return "REPORT";
    }
}
```

```java
package com.example.plugins;

import com.example.report.Report;

public class SalesReport extends Report {
    String header() {                      // looks like an override
        return "SALES Q3";
    }

    public static void main(String[] args) {
        System.out.println(new SalesReport().render());
    }
}
```

```text
REPORT | 3 rows
```

A method overrides only a method it can access. `SalesReport` can't see `Report.header()`, so its
`header()` is a new, unrelated method, and `render()` still calls the original. Had it been an
override, `render()` would have called the subclass's version. `@Override` on the subclass method makes it a compile error:

```text
src2/com/example/plugins/SalesReport.java:6: error: method does not override or implement a method from a supertype
    @Override
    ^
```

A library author who wants subclasses elsewhere to replace `header()` declares it `protected`.

The boundary is the package name, not the jar. On the class path, another jar can declare classes
in `com.example.report` and call `header()` (unless the library's jar is sealed in its manifest):
a `Borrow` class in a separate `plugin.jar` printed `package-private header() from another jar:
REPORT`. Put both jars on the module path, where each becomes an automatic module, and the JVM
refuses to start, because two modules may not share a package:

```text
Error occurred during initialization of boot layer
java.lang.module.ResolutionException: Module report contains package com.example.report, module plugin exports package com.example.report to report
```

## Protected Opens the Package, and Other Packages Only Through the Subclass's Own Type

`protected` is package-private plus subclasses elsewhere. A cache library declares `protected int size` and `protected void evict(String key)`.
`Janitor`, an unrelated class in `com.example.cache`, calls `any.evict("stale")` on a `Cache` and
compiles. A subclass in another package does not:

```java
package com.example.app;

import com.example.cache.Cache;

public class SessionCache extends Cache {
    void cleanUp(SessionCache other, Cache any) {
        evict("s-1");                             // this object: compiles
        other.evict("s-2");                       // another SessionCache: compiles
        any.evict("s-3");                         // a Cache reference: error
        System.out.println(any.size);             // error too
    }
}
```

```text
src/com/example/app/SessionCache.java:9: error: evict(String) has protected access in Cache
        any.evict("s-3");                         // a Cache reference: error
           ^
src/com/example/app/SessionCache.java:10: error: size has protected access in Cache
        System.out.println(any.size);             // error too
                              ^
```

Outside the package, a subclass reaches a protected instance member only through an expression of
its own type or a subtype; the compiler checks the declared type of the expression, not the object
at run time. The variable `any` could hold another vendor's `PriceCache`. A cast to `SessionCache`
compiles. A matching rule stops a subclass in another package from calling `new`
on a protected constructor; `super(...)` and an anonymous subclass `new Cache() { }` compile.

## An Override May Widen Access but Never Narrow It

```java
class Shape {
    public double area() { return 0; }
    protected String label() { return "shape"; }
}

class Square extends Shape {
    double area() { return 4; }                   // narrows public to package-private
    public String label() { return "square"; }    // widens protected to public: fine
}

class Job implements Runnable {
    void run() { }                                // interface methods are public
}
```

```text
Narrow.java:7: error: area() in Square cannot override area() in Shape
    double area() { return 4; }                   // narrows public to package-private
           ^
  attempting to assign weaker access privileges; was public
Narrow.java:12: error: run() in Job cannot implement run() in Runnable
    void run() { }                                // interface methods are public
         ^
  attempting to assign weaker access privileges; was public
```

Code that holds a `Shape` may call `area()` on any `Square` it is given, so a `Square` can't
withdraw it. This breaks upgrades: a library that widens a `protected` hook to `public` breaks
the build of every subclass that overrides it as `protected`.

<!-- practice 3 -->

## setAccessible Opens Private Members of Class-Path Code, Not of the JDK

Reading a private field of another top-level class
through `Field.get` throws `IllegalAccessException` until `setAccessible(true)` is called. For your
own classes on the class path that call succeeds. For the JDK's internals it no longer does:

```java
Field mine = Token.class.getDeclaredField("secret");
mine.setAccessible(true);
System.out.println("own class: " + mine.get(new Token()));

Field jdk = String.class.getDeclaredField("value");
System.out.println("trySetAccessible on String.value: " + jdk.trySetAccessible());
jdk.setAccessible(true);
```

```text
own class: s3cr3t
trySetAccessible on String.value: false
Exception in thread "main" java.lang.reflect.InaccessibleObjectException: Unable to make field private final byte[] java.lang.String.value accessible: module java.base does not "opens java.lang" to unnamed module @2503dbd3
```

`java.base`, the module that holds `java.lang`, `java.util` and `java.time`, opens none of its
packages. Up to Java 15 this access worked
with a warning; Java 16 denied it by default, and Java 17 removed the `--illegal-access` switch that
relaxed it. A serializer or test helper that pokes JDK internals fails on 16 and later, with an unchecked
exception that `catch (ReflectiveOperationException e)` doesn't catch. `--add-opens
java.base/java.lang=ALL-UNNAMED` opens the package from the command line. Without it,
`trySetAccessible()` answers `false` instead of throwing.

`getDeclaredFields()` lists one class's own fields, private ones included. A superclass's private
fields live in the same object but are listed by the superclass's `Class`, and `getFields()`
returns only public fields, inherited ones included. Unit 2.2.3 comes back to what reflection means for data hiding.

<!-- practice 2 -->

## Summary

- **Private is checked per class, not per object.** Code in a class can change any instance's
  private state, so go through the other object's methods when they enforce a rule. Nested classes
  share the same private members.
- **No modifier means package-private, and a subclass in another package can't override it.** The
  "override" is silently a new method; `@Override` catches it.
- **Protected opens the package, and other packages only through the subclass's own type.** A
  `Cache` reference in a subclass elsewhere can't reach `evict()`.
- **An override may widen access but never narrow it.** Widening a hook in a library breaks
  subclasses that override it with the old level.
- **`setAccessible` opens class-path code, not the JDK.** Since Java 17 it throws
  `InaccessibleObjectException` on JDK internals unless the package is opened.

## Reference

**Who can use a member (Java 21):**

| Code that uses it | `private` | none | `protected` | `public` |
|---|---|---|---|---|
| the same top-level class, nested classes included | yes | yes | yes | yes |
| another class in the same package | no | yes | yes | yes |
| a subclass in another package | no | no | instance members through its own type; static ones directly | yes |
| any other class | no | no | no | yes |

**Where each modifier may appear:**

| Declaration | Allowed |
|---|---|
| top-level class or interface | `public` or none |
| member of a class: field, method, constructor, nested type | all four |
| interface field or nested type | `public` (implicit) |
| interface method | `public` (implicit) or `private` (Java 9+) |
| local variable, parameter | none |

## Challenges

1. A partner reaches your library's package-private helper by putting a class in your package on
   the class path. Do you make the helper public, move to modules, or leave it? What breaks for
   them in each case?
2. Find a `protected` field in a codebase you know. Which subclasses write it, and which invariant
   of the base class can they break?
3. A test sets a private field by reflection to prepare state. What does that cost when the field
   is renamed, and what would you change in the class instead?

## Design Note: Why Access Is Checked per Class

javac checks access from the program's text, and the JVM checks it again when code links; in both
the unit is the class, not the object. Privacy per object would stop `equals`, `compareTo` and copy
constructors from reading the other object, which is most of what they do. The cost is that a
reviewer has to look for `other.field =` as carefully as for a public field. Modules (Java 9) added a boundary of the same
kind one level up: a package is exported or not, for every class in it.

## Code Examples

- Test: [AccessModifiersExampleTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/encapsulation/AccessModifiersExampleTest.java)
- Source: [AccessModifiersExample.java](src/main/java/com/github/msorkhpar/claudejavatutor/encapsulation/AccessModifiersExample.java)
