# 10.3.1. Class and Object Introspection

An in-house event bus calls every method annotated `@Subscribe` that it finds with
`getDeclaredMethods()`. Someone changes `OrderHandler` to implement a new interface,
`Handler<OrderPlaced>`, and nothing else. From the next deploy every order ships twice, and
publishing a `PaymentFailed` event logs a `ClassCastException` from `OrderHandler`, a class that
never mentions payments. Nobody touched the bus.

A `Class` object is the JVM's description of one loaded type, and `getClass()`, a class literal
(`Circle.class`) and `Class.forName` all reach the same one. Reading and writing fields through it
is unit 10.3.2, creating objects and calling methods is 10.3.3, what it costs is 10.3.4, and
`java.lang.reflect.Proxy` is unit 9.2.3. Every output is from Java 21.

## getMethods and getDeclaredMethods Answer Different Questions, and Neither Lists Everything

`Circle` extends an abstract `Shape`:

```java
abstract class Shape {
    private final String color;
    Shape(String color) { this.color = color; }
    public String getColor() { return color; }
    protected abstract double area();
}

class Circle extends Shape {
    private final double radius;
    Circle(String color, double radius) { super(color); this.radius = radius; }
    public double getRadius() { return radius; }
    @Override protected double area() { return Math.PI * radius * radius; }
    void validate() { if (radius <= 0) throw new IllegalStateException(); }
}
```

Before reading on: which of the two calls lists `getColor`, and which lists `validate`?

```text
getMethods():         [equals, getClass, getColor, getRadius, hashCode, notify, notifyAll, toString, wait, wait, wait]
getDeclaredMethods(): [area, getRadius, validate]
```

(The names are sorted for printing; the next section explains why that is needed.)
`getMethods()` answers "what can any caller invoke on a `Circle`": public methods only, inherited ones included, down to
`Object`'s. `getDeclaredMethods()` answers "what does `Circle`'s own class file declare": every
access level, nothing inherited. So `validate()` is missing from the first list and `getColor()`
from the second, and the protected `area()` appears only in the second, because `Circle`
overrides it.

A tool that must see every method a type's classes declare, whatever its access, walks the chain
itself: call `getDeclaredMethods()` on the class, then on `getSuperclass()`, until that returns
`null`. It sees overridden methods twice, once in each class that declares them, and it misses
interface default methods unless it also visits `getInterfaces()`. The same split holds for
fields (`getFields` / `getDeclaredFields`) and constructors, except that a class inherits no
constructors.

## Reflection Returns Members in No Particular Order

A CSV export takes one column per accessor of `Invoice`, a class whose five methods are declared
in the order `id()`, `customer()`, `amount()`, `currency()`, `status()`:

```java
for (Method m : Invoice.class.getDeclaredMethods()) {   // one CSV column per accessor
    header.add(m.getName());
    row.add((String) m.invoke(invoice));
}
```

We run it on the same JDK, the second time with class data sharing off (`-Xshare:off`). Class
data sharing is an archive of pre-parsed JDK class data that the JVM maps at startup:

```text
$ java Order
id,status,currency,amount,customer
INV-7,PAID,EUR,120.00,Jane Doe
$ java -Xshare:off Order
customer,amount,currency,id,status
Jane Doe,120.00,EUR,INV-7,PAID
```

Neither follows the source. The javadoc of both methods says the elements "are not sorted and are
not in any particular order". Twenty launches of each command, header line only:

```text
# 20 launches, -Xshare:off (header only)
      1 customer,amount,currency,id,status
     19 id,status,customer,amount,currency
# 20 launches, default
     20 id,status,currency,amount,customer
```

With the archive on, the order was the same on every launch, which is what makes the dependency
easy to ship: the export passes every test on one JVM setup, then a different JDK build, container
image or flag reorders the columns, and a consumer that reads by position books `Jane Doe` as the
invoice id. Without the archive it changed between two launches of the same command. JUnit 4 met
this with test methods and fixes the order itself since 4.11; its `MethodSorters.JVM` option warns
that "the order from the JVM may vary from run to run".

> **Why it moves.** HotSpot sorts a class's methods by the memory address of each name in the
> JVM's symbol table, and the shared archive changes where names live. That is an implementation
> detail and may change; the javadoc promises nothing.

When order matters, take it from something that defines one: sort by name, read an annotation
that carries a position, or, for records, use the components (two sections down).

## The Compiler Adds Methods You Never Wrote: Bridges and Lambda Bodies

Here is `OrderHandler` from the opening, with a lambda field it uses to skip test orders:

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Predicate;

@Retention(RetentionPolicy.RUNTIME)
@interface Subscribe {}

interface Handler<E> {
    void handle(E event);
}

record OrderPlaced(String orderId) {}

class OrderHandler implements Handler<OrderPlaced> {
    private final Predicate<String> isTest = id -> id.startsWith("T-");

    @Subscribe
    @Override
    public void handle(OrderPlaced event) {
        if (!isTest.test(event.orderId())) System.out.println("  ship " + event.orderId());
    }
}
```

Its `getDeclaredMethods()`:

```text
public void OrderHandler.handle(java.lang.Object)  bridge=true  synthetic=true  @Subscribe=true
public void OrderHandler.handle(OrderPlaced)       bridge=false synthetic=false @Subscribe=true
private static boolean OrderHandler.lambda$new$0(java.lang.String) bridge=false synthetic=true  @Subscribe=false
```

After erasure, `Handler.handle` takes an `Object`, and a
`handle(OrderPlaced)` does not override it in the class file. So `javac` adds a **bridge method**
`handle(Object)` that casts its argument to `OrderPlaced` and calls the real one, and it copies the
method's annotations onto the bridge. The lambda's body became a private static method,
`lambda$new$0`. Both are marked synthetic: `isSynthetic()` is `true`, and `isBridge()` picks out
the bridge. Both `handle` methods are declared by `OrderHandler` itself, and `getMethods()` lists
the same two: the interface's abstract `handle` is dropped there, because a method declared by a
class wins over one with the same signature declared by an interface. The annotations go only
onto the bridge; `Handler.handle` still has none.

The bus registered both `handle` methods. Its scan, with a switch to skip bridges:

```java
for (Method m : target.getClass().getDeclaredMethods()) {
    if (skipBridges && m.isBridge()) continue;
    if (m.isAnnotationPresent(Subscribe.class) && m.getParameterCount() == 1) {
        found.add(new Subscription(target, m));
    }
}
```

It then delivers each event to every subscription whose parameter type accepts it. Publishing an
`OrderPlaced`, then a `PaymentFailed`:

```text
skipBridges=false, subscriptions: 2
  ship A-1001
  ship A-1001
  java.lang.ClassCastException: class PaymentFailed cannot be cast to class OrderPlaced (PaymentFailed and OrderPlaced are in unnamed module of loader 'app')
skipBridges=true, subscriptions: 1
  ship A-1001
```

The bridge's parameter type is `Object`, so it accepts every event: each order ships twice, and
every other event reaches the cast inside the bridge. (`Method.invoke` wraps that exception in an
`InvocationTargetException`; unit 10.3.2 covers it.) A narrower return type produces a bridge
too: a class implementing `Supplier<String>` declares `String get()` and a bridge `Object get()`,
two methods with the same name and no parameters. A scan that turns methods into routes, columns
or subscriptions skips `isBridge()` methods, and usually every `isSynthetic()` one.

<!-- practice 1 -->

## Read a Record Through Its Components and a Sealed Type Through Its Permitted Subclasses

A payment is one of a sealed family, and one case is itself sealed:

```java
sealed interface Payment permits Card, Transfer, Wallet {}
record Card(String last4, long cents) implements Payment {}
record Transfer(String iban, long cents) implements Payment {}
sealed interface Wallet extends Payment permits ApplePay, Paypal {}
record ApplePay(String token, long cents) implements Wallet {}
record Paypal(String email, long cents) implements Wallet {}
```

An exporter that treats "no-argument instance methods" as a record's values, next to the record's
own description:

```text
no-arg instance methods of Card: [toString, hashCode, last4, cents]
record components of Card:      [last4, cents]
Payment permits: [Card, Transfer, Wallet]
Wallet permits:  [ApplePay, Paypal]
Card.getPermittedSubclasses(): null, Wallet.getRecordComponents(): null
```

A record's `toString`, `hashCode` and `equals` are real methods in its class file, so the method
scan exports `toString` and `hashCode` columns. `getRecordComponents()` returns exactly the
components, and its javadoc promises them "in the same order that they are declared in the record
header", the only member list with a promised order. Each `RecordComponent` gives the name, the type, the
generic type and `getAccessor()`, the method to call for the value.

`getPermittedSubclasses()` lists only the direct subtypes: `Wallet` appears as a case, and
reaching `ApplePay` takes a second call. Its javadoc leaves the order unspecified, and it returns
`null` for a type that is not sealed, so a recursive walk stops on `null` rather than on an empty
array. A permitted subclass that the class loader cannot load is "silently ignored", so a missing
jar shrinks the list without an error.

<!-- practice 2 -->

## Type Arguments Survive in Declarations, Not in Objects

A settings class has a field `List<Integer> ports = new ArrayList<>()` and a method
`Map<String, List<Integer>> portsByHost()`. What each reflective view reports:

```text
instance:            class java.util.ArrayList
field type:          interface java.util.List
field generic type:  java.util.List<java.lang.Integer>
return type:         interface java.util.Map
generic return type: java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>
element type:        class java.lang.Integer (Class)
same class:          true
type token:          java.util.List<java.lang.String>
```

The object is an `ArrayList`, and an `ArrayList<String>` has the very same `Class`: type arguments
are erased from objects. Declarations keep them. For every field, method and class whose
declaration uses type arguments, the compiler writes the generic signature into the class file.
`getGenericType()`, `getGenericReturnType()` and `getGenericSuperclass()` return it, as a
`ParameterizedType` when the type has arguments, and its `getActualTypeArguments()` gives
`Integer`, a plain `Class`.

The last line is how libraries capture a type that has no field to hang on. `TypeRef<T>` is an
abstract class whose method returns
`((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]`. The
anonymous subclass `new TypeRef<List<String>>() {}` records `List<String>` in its own superclass
signature, and that call reads it back. Called on an ordinary object's class,
`getGenericSuperclass()` reads that class's declaration: for every `ArrayList`, whatever it was
created with, it is `java.util.AbstractList<E>`.

A configuration binder that fills `ports` from the string `"8080,8081"` without reading the
declaration adds `String`s, and the erased list accepts them:

```text
naive:     ports = [8080, 8081]
           class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
signature: ports = [8080, 8081], first port 8080
```

The failure appears in whichever code first reads `int port = ports.get(0)`, far from the binder
that caused it. The second binder read `Integer` from
`getGenericType()` and converted each item.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **`getMethods()` is public and inherited; `getDeclaredMethods()` is this class, every access
  level.** Seeing every class's methods takes a walk up `getSuperclass()`.
- **Members come back in no particular order.** The order shifts with JVM setup; take order from a
  sort, an annotation or record components.
- **The compiler adds bridge and synthetic methods, and bridges carry the annotations.** A scan that
  doesn't skip `isBridge()` registers an implementation of a generic interface method twice, once
  for the erased parameter type.
- **Records and sealed types describe themselves.** `getRecordComponents()` is in header order;
  `getPermittedSubclasses()` is direct subtypes only, unordered, `null` when not sealed.
- **Type arguments live in declarations.** `getGenericType()` and its relatives have them; an
  object's `getClass()` never does.

## Reference

| Call | Returns | Order | Edge cases |
|---|---|---|---|
| `getMethods()` | public methods, own and inherited, from superclasses and superinterfaces | none | static methods of superinterfaces excluded |
| `getDeclaredMethods()` | this class's methods, every access level, bridges and synthetic ones included | none | no `<clinit>`; empty for arrays and primitives |
| `getMethod(name, types...)` | one public method | n/a | with bridges of the same signature, the one with the most specific return type |
| `getRecordComponents()` | the record's components | header order | `null` if not a record |
| `getPermittedSubclasses()` | direct permitted subtypes | unspecified | `null` if not sealed; unloadable ones left out |
| `getSuperclass()` | the direct superclass | n/a | `null` for `Object`, interfaces, primitives, `void` |
| `getInterfaces()` | the directly implemented interfaces | declaration order | not inherited ones |
| `Method.isBridge()` / `isSynthetic()` | added by the compiler | n/a | javac marks every bridge synthetic too |
| `getGenericType()`, `getGenericReturnType()`, `getGenericSuperclass()` | the declared type with its arguments | n/a | a plain `Class` when the declaration has none |

## Challenges

1. A dependency-injection container finds constructors with `getDeclaredConstructors()`. A class
   has two that are both eligible. What does the container do today, and what makes it pick the
   other one next year?
2. Your serializer walks fields with `getDeclaredFields()` up the hierarchy. A subclass declares a
   field with the same name as a private field of its superclass. What should the JSON contain?
3. `Wallet` gains a third case in a jar that one service doesn't ship. What does a tool built on
   `getPermittedSubclasses()` report there, and how would you notice?

## Design Note: Why Signatures Keep Type Arguments and Objects Don't

Generics arrived in Java 5 with erasure, so that a `List<String>` and a pre-generics raw `List`
are the same class at run time and old binaries keep working. The type arguments went into the
`Signature` attribute of each class, field and method, for the compiler compiling against that
class, and reflection exposes the same attribute. Nothing per object records them, which costs
every framework that binds data: it can only learn a type argument from a declaration.

## Code Examples

- Test: [ClassIntrospectionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/ClassIntrospectionTest.java)
- Source: [ClassIntrospection.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/ClassIntrospection.java)
