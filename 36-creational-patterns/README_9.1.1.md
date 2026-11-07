# 9.1.1. Singleton Pattern

The partner's payment API allows 100 calls a minute per API key. The service keeps the budget in
`PartnerQuota`, a singleton, so that every thread draws from one counter. Last Tuesday the partner
throttled the key for an hour: its logs show 200 calls in one minute, all from one process.
Somewhere in that process a second `PartnerQuota` existed, although its constructor is private.

A hand-written singleton can be copied in three ways, and none of the usual forms closes all
three. The examples use this quota (all outputs are from Java 21):

```java
import java.util.concurrent.atomic.AtomicInteger;

final class Quota {
    static final Quota INSTANCE = new Quota();
    private final AtomicInteger left = new AtomicInteger(100);
    private Quota() {}
    boolean tryTake() { return left.getAndDecrement() > 0; }
    int left() { return Math.max(left.get(), 0); }
}
```

## A Private Constructor Stops new, and Nothing Else

Spend this minute's budget, then call the private constructor through reflection:

```java
while (Quota.INSTANCE.tryTake()) { }
Constructor<Quota> c = Quota.class.getDeclaredConstructor();
c.setAccessible(true);
Quota copy = c.newInstance();
System.out.println("INSTANCE left: " + Quota.INSTANCE.left()
        + ", copy == INSTANCE: " + (copy == Quota.INSTANCE) + ", copy left: " + copy.left());
```

```text
INSTANCE left: 0, copy == INSTANCE: false, copy left: 100
```

`private` is checked by the compiler and by the access check that `setAccessible(true)` switches
off. Whoever holds the copy has another 100 calls. DI containers and JSON mappers that build
objects reflectively make the same call.

The defence is a constructor that refuses to run twice: it reads the instance field, which must
already be set when anyone else calls it.

```java
final class GuardedQuota {
    static final GuardedQuota INSTANCE = new GuardedQuota();
    private GuardedQuota() {
        if (INSTANCE != null) throw new IllegalStateException("GuardedQuota already exists");
    }
}

final class LazyQuota {
    private static LazyQuota instance;
    private LazyQuota() {
        if (instance != null) throw new IllegalStateException("LazyQuota already exists");
    }
    static synchronized LazyQuota getInstance() {
        if (instance == null) instance = new LazyQuota();
        return instance;
    }
}
```

Reflection against each, the lazy one before anything has called `getInstance()` (the guard's
exception arrives as the cause of an `InvocationTargetException`):

```text
guarded: java.lang.IllegalStateException: GuardedQuota already exists
lazy: reflected first, then getInstance() == it: false
```

`newInstance` initializes `GuardedQuota` first, and class initialization builds `INSTANCE`, so the
reflective call finds the field set and fails. The lazy field is still `null` when reflection comes
first, so the copy gets through, and `getInstance()` builds another. A guard on a boolean flag
lets a reflection-first copy through too, after which `getInstance()` throws; and reflection can
reset the flag. It can't reset a
`static final` field: `Field.set` on `INSTANCE` threw `IllegalAccessException: Can not set static
final GuardedQuota field GuardedQuota.INSTANCE to null value`.

> **Named modules refuse `setAccessible`.** If the singleton's package is in a named module that
> doesn't `opens` it, `setAccessible(true)` from another module throws
> `InaccessibleObjectException`, as it does for JDK classes (unit 2.2.1). On the class path
> nothing stops it.

## Deserialization Makes a Second Instance Without Running the Constructor

The quota is `Serializable` because it travels in a cached session. It has the guard from above,
and its constructor prints a line. We take 60 calls, write `INSTANCE` to an `ObjectOutputStream`,
take 40 more, and read the bytes back:

```text
  constructor ran
copy == INSTANCE: false, INSTANCE left: 0, copy left: 40
```

The constructor ran once, during class initialization. Deserialization never calls it: it
allocates the object, runs the no-arg constructor of the first superclass that isn't
`Serializable` (here `Object`), and fills the fields from the stream. The guard never runs, and
the copy resumes with the 40 calls it had when it was written.

When a serializable class declares `readResolve`, `readObject` returns what that method returns
instead of the object it just built:

```java
private final transient AtomicInteger left = new AtomicInteger(100);

@java.io.Serial
private Object readResolve() {
    return INSTANCE;
}
```

This time we take 60, write, take 20 more, and read back:

```text
with readResolve: back == INSTANCE: true, left: 20
```

The caller gets the live instance with the live count. Whatever the stream held is discarded with
the object built from it, which is why `left` is `transient` here: there is no point writing state
that is thrown away on arrival.

<!-- practice 1 -->

## Each Class Loader Has Its Own Singleton, Enum Included

A static field belongs to a class, and a class is identified by its name and the loader that
defined it. Two loaders that each define `Quota` from the same bytes have two classes, two static
fields and two budgets. Application servers give each web application its own loader, and plugin
hosts give each plugin one, so a library jar copied into two of them is loaded twice.

Public copies of `Quota` and of an enum `QuotaEnum` with the same counter are compiled into a
directory, `pluginJar`, which is also on the application's class path. Two `URLClassLoader`s read
it, each with the platform loader as parent, so neither can find the class through its parent, as
in an isolated plugin host:

```java
ClassLoader platform = ClassLoader.getPlatformClassLoader();
try (URLClassLoader a = new URLClassLoader(pluginJar, platform);
     URLClassLoader b = new URLClassLoader(pluginJar, platform)) {
    for (String name : new String[] {"Quota", "QuotaEnum"}) {
        Object fromA = a.loadClass(name).getField("INSTANCE").get(null);
        Object fromB = b.loadClass(name).getField("INSTANCE").get(null);
        System.out.println(name + ": same instance in A and B? " + (fromA == fromB));
    }
    Object fromA = a.loadClass("Quota").getField("INSTANCE").get(null);
    try {
        Quota q = (Quota) fromA;
    } catch (ClassCastException e) {
        System.out.println(e.getMessage());
    }
}
```

```text
Quota: same instance in A and B? false
QuotaEnum: same instance in A and B? false
class Quota cannot be cast to class Quota (Quota is in unnamed module of loader java.net.URLClassLoader @5c647e05; Quota is in unnamed module of loader 'app')
```

The enum gives no protection here: its constant is a static field like any other. The cast fails
because the `Quota` named in the program's own source is the application loader's class, a third one.

Loaders delegate to their parent before defining a class themselves. With the application loader
as parent, which has the directory on its class path, the class is defined once, whichever loader
is asked:

```text
parent = app loader: same instance? true, == Quota.INSTANCE? true
```

So a singleton meant to be one per process lives in a jar that only the shared parent loads (the
server's or the host's `lib`), and plugins must not ship their own copy. A host that loads plugin
classes child-first, to let each plugin bring its own library versions, has to send the shared
API packages to the parent explicitly.

<!-- practice 2 -->

## The Holder Idiom Chooses When the Instance Is Built; Only the Enum Refuses Copies

One program applies the three attacks to five forms, reflection first, before anything has called
`getInstance()`. Each form is `Serializable` and has a `getInstance()`. `PlainQuota` is the eager
class with a private constructor, `ResolvedQuota` adds the guard and `readResolve`, `HolderQuota`
keeps its instance in a nested class, `GuardedHolderQuota` is that holder with the same two
additions, and `EnumQuota` is a one-constant enum:

```java
import java.io.Serializable;

public final class GuardedHolderQuota implements Serializable {
    private GuardedHolderQuota() {
        if (Holder.INSTANCE != null) throw new IllegalStateException("already exists");
    }
    private static final class Holder {
        static final GuardedHolderQuota INSTANCE = new GuardedHolderQuota();
    }
    public static GuardedHolderQuota getInstance() { return Holder.INSTANCE; }
    @java.io.Serial
    private Object readResolve() { return Holder.INSTANCE; }
}
```

```text
                    reflection                         serialization    second loader
PlainQuota          second instance                    second instance  second instance
ResolvedQuota       refused: IllegalStateException     same instance    second instance
HolderQuota         second instance                    second instance  second instance
GuardedHolderQuota  refused: IllegalStateException     same instance    second instance
EnumQuota           refused: IllegalArgumentException  same instance    second instance
```

The holder idiom answers when and how safely the instance is built. The nested class is
initialized on the first `getInstance()`, under the JVM's class-initialization lock, so it is lazy
and needs no `volatile` (unit 5.1.3, which also shows why double-checked locking needs one). By
itself it protects against none of the three attacks. `GuardedHolderQuota`'s guard holds even when
reflection comes first: the guard's read of `Holder.INSTANCE` starts the holder's initialization,
which builds the real instance (the inner constructor call sees `null`), and the reflective call
then finds the field set and throws.

The enum refuses both copies with no code: `Constructor.newInstance` rejects enum classes, and
an enum is serialized as its name and read back with `valueOf` (unit 7.2.5 runs both). An enum
can't extend a class, and its constant is built as soon as anything initializes the enum, such as
a call to `values()`. Every form here except a lazy synchronized field builds its instance in a
static initializer, and one that throws leaves the class unusable for as long as its class loader
lives (unit 5.2.3). A constructor that calls a remote service belongs in none of them.

## A Static Singleton Carries One Test's State Into the Next

A `PartnerClient` that looks up the quota itself has two tests. Each passes when run alone
(JUnit 5.10.2, methods run in name order):

```java
import java.util.concurrent.atomic.AtomicInteger;

final class PartnerQuota {
    private static final PartnerQuota INSTANCE = new PartnerQuota();
    private final AtomicInteger left = new AtomicInteger(100);
    private PartnerQuota() {}
    static PartnerQuota getInstance() { return INSTANCE; }
    boolean tryTake() { return left.getAndDecrement() > 0; }
}

final class PartnerClient {
    boolean send(String order) {
        return PartnerQuota.getInstance().tryTake();          // then the HTTP call
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class PartnerClientTest {
    @Test
    void refusesTheCallAfterTheHundredth() {
        PartnerClient client = new PartnerClient();
        for (int i = 0; i < 100; i++) assertTrue(client.send("A-" + i));
        assertFalse(client.send("A-100"));
    }

    @Test
    void sendsAnOrder() {
        assertTrue(new PartnerClient().send("B-1"));
    }
}
```

```text
together:
  refusesTheCallAfterTheHundredth(): SUCCESSFUL
  sendsAnOrder(): FAILED (expected: <true> but was: <false>)
```

Every test in one JVM shares the static instance, so the first test spent the budget the second
one needed. Which test fails depends on the order. The usual patch, a `resetForTesting()` method called from `@BeforeEach`, works only while
every test class remembers to call it and while tests run one at a time: with JUnit's parallel
execution on, two tests share the instance while both are running.

The fix is to stop looking the instance up. `PartnerQuota` becomes an ordinary class with a
constructor, `PartnerClient` takes it as a constructor parameter (unit 8.1.5), and `main` builds
the one instance and hands it to everyone who needs it:

```java
import java.util.concurrent.atomic.AtomicInteger;

final class PartnerQuota {
    private final AtomicInteger left;
    PartnerQuota(int perMinute) { left = new AtomicInteger(perMinute); }
    boolean tryTake() { return left.getAndDecrement() > 0; }
}

final class PartnerClient {
    private final PartnerQuota quota;
    PartnerClient(PartnerQuota quota) { this.quota = quota; }
    boolean send(String order) {
        return quota.tryTake();                               // then the HTTP call
    }
}

final class Main {
    public static void main(String[] args) {
        PartnerQuota quota = new PartnerQuota(100);           // the one instance, built once
        PartnerClient orders = new PartnerClient(quota);
        PartnerClient refunds = new PartnerClient(quota);
        System.out.println(orders.send("A-1") + " " + refunds.send("R-1"));
    }
}
```

Each test now builds `new PartnerClient(new PartnerQuota(100))`, and the same two tests pass
together and alone. No static field exists for reflection, serialization or a second loader to
duplicate, and a test that wants an exhausted quota builds one with a limit of 0. Another
`new PartnerQuota(100)` in production code compiles, though, and only review catches it. A DI container's singleton scope is the same arrangement, with the container as `main`.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **A private constructor stops `new`, and nothing else.** Reflection calls it; a guard that reads
  the `static final` instance field refuses the second call, one that reads a lazily set field
  doesn't.
- **Deserialization builds a second instance without running the constructor.** `readResolve`
  returning the instance gives callers the live object and discards the state that was written.
- **Each class loader has its own singleton, enum included.** One per process means one shared
  parent loader and no copy of the class in any child.
- **The holder idiom decides when the instance is built; only the enum refuses copies without
  extra code.** No form survives a second loader, and every static form makes a failing
  constructor fatal for its class.
- **A static singleton carries one test's state into the next.** Build the one instance in `main`
  and pass it through constructors; tests then build their own.

## Reference

**What each form survives (Java 21, run with reflection before first use):**

| Form | Reflection | Serialization | Second class loader |
|---|---|---|---|
| eager `static final`, private constructor | second instance | second instance | second instance |
| eager, guard reading `INSTANCE`, `readResolve` | `InvocationTargetException`, cause `IllegalStateException` | same instance | second instance |
| lazy field, guard reading the field | second instance if reflection comes first | depends on `readResolve` | second instance |
| holder class | second instance | second instance | second instance |
| holder, guard reading `Holder.INSTANCE`, `readResolve` | `InvocationTargetException`, cause `IllegalStateException` | same instance | second instance |
| enum constant | `IllegalArgumentException: Cannot reflectively create enum objects` | same constant; fields not serialized | second instance |

**Serialization hooks (`java.io.Serializable` javadoc):**

| Method | When it runs | Use here |
|---|---|---|
| `private Object readResolve()` | after the object is read, before `readObject` returns | return the existing instance |
| `private Object writeReplace()` | before the object is written | write a small proxy instead |
| `private void readObject(ObjectInputStream)` | while the fields are read | validate; can't swap the result |

## Challenges

1. A singleton is in a library that two web applications on one Tomcat both bundle. Moving the jar
   to Tomcat's shared `lib` makes it one instance. What else changes for those applications, such
   as redeploying one without the other?
2. Your codebase has 40 calls to `Clock.getInstance()`. Plan the move to a constructor-injected
   clock in steps that each leave the build green.
3. A metrics registry is a singleton so that libraries with no access to `main` can register
   gauges. Is that a reason to keep it static? What does the JDK's `ServiceLoader` (unit 8.1.5)
   offer instead?

## Design Note: One Instance Is a Decision of the Program, Not of the Class

The pattern puts two decisions in one class: that the program needs exactly one of something, and
how everyone finds it. The first is usually right. The second, a static field reachable from
anywhere, is the part that reflection, serialization and class loaders duplicate and that tests
can't replace.

Moving the decision to `main` costs the guarantee and some plumbing: every class that needs the
quota has to receive it. The enum keeps the guarantee and the
global access, which suits a stateless strategy or a constant table. For anything with state that
tests need to control, or that must stay one per process across class loaders, one instance built
at the composition root is the form that none of the three copies can reach and every test can
replace.

## Code Examples

- Source: [SingletonPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/SingletonPattern.java)
- Test: [SingletonPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/SingletonPatternTest.java)
