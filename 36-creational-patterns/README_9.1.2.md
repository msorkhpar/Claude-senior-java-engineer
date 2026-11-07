# 9.1.2. Factory Method Pattern

A metrics library ships `public Tag(String name)`, and every service calls `new Tag("region")` on
each request. A heap histogram of one service shows about a million `Tag` objects carrying three
distinct names. The maintainers want one shared object per name. A constructor can't give them
that: `new` always allocates, so the fix is a method in its place, and every call site in every
service has to change.

Java has two things called a factory method: a **static factory** returns an instance in place of
a constructor, and the **GoF Factory Method** is an overridable method that base-class code calls
to create an object, so a subclass decides what gets created.

## new Always Allocates; a Static Factory Can Hand Back an Object It Already Made

The library's fix keeps one instance per declared name and makes the constructor private:

```java
import java.util.Map;

public final class Tag {
    private static final Map<String, Tag> DECLARED = Map.of(
            "http.method", new Tag("http.method"),
            "http.status", new Tag("http.status"),
            "region", new Tag("region"));

    private final String name;

    private Tag(String name) {
        this.name = name;
    }

    /** One shared Tag per declared name; any other name gets a new one each time. */
    public static Tag of(String name) {
        Tag shared = DECLARED.get(name);
        return shared != null ? shared : new Tag(name);
    }

    public String name() {
        return name;
    }
}
```

A million calls with the three names, each built with `new String(...)` so that no two are the
same string object, counting distinct objects:

```text
new Tag(...):  1000000 objects
Tag.of(...):   3 objects
```

Sharing is safe only because a `Tag` can't change: if one caller could rename a shared tag, every
other caller would see it. Names that aren't declared get a fresh object and aren't remembered.
A tag name can come from a request header, and a cache keyed by caller input grows with every new
value until the heap is full.

The JDK made the same move for the wrappers. `Integer.valueOf` caches -128 to 127 (unit 1.1.1),
and the constructor is now marked for removal:

```text
Boxes.java:3: warning: [removal] Integer(int) in Integer has been deprecated and marked for removal
        Integer a = new Integer(1000), b = new Integer(1000);
                    ^
```

The same program, run as is and then with `-XX:AutoBoxCacheMax=1000`:

```text
new Integer(1000) == new Integer(1000):         false
Integer.valueOf(1000) == Integer.valueOf(1000): false
```

```text
new Integer(1000) == new Integer(1000):         false
Integer.valueOf(1000) == Integer.valueOf(1000): true
```

The class files are the same; whether `valueOf(1000)` returns a shared object was decided when the
JVM started. A constructor's result is fixed by the language; a factory's only by its javadoc, which for `valueOf` promises -128 to 127 and says it "may cache other values". Code
that compares a factory's results with `==` relies on whatever the javadoc leaves open.

<!-- practice 1 -->

## A Static Factory Picks the Class From Its Input, So the Class Can Change Without Callers Noticing

`EnumSet.noneOf` declares that it returns an `EnumSet`, an abstract class. Here it is called for
an enum of 64 permissions and one of 65:

```java
EnumSet<Perm64> a = EnumSet.noneOf(Perm64.class);
EnumSet<Perm65> b = EnumSet.noneOf(Perm65.class);
System.out.println("64 constants: " + a.getClass().getName());
System.out.println("65 constants: " + b.getClass().getName());
```

```text
64 constants: java.util.RegularEnumSet
65 constants: java.util.JumboEnumSet
```

`RegularEnumSet` keeps the set in one `long`, one bit per constant, which caps it at 64.
`JumboEnumSet` keeps a `long[]`. Both are package-private, so no caller can name either; the
factory chooses by the enum's size, at every call. Once a 65th permission is deployed, every
`EnumSet` of that enum is a `JumboEnumSet`, and no caller's source changes. Had `EnumSet` exposed
`new RegularEnumSet<>(Perm.class)`, each call site would be compiled against the 64-bit class, and
adding a constant would mean editing all of them. `List.of` picks by size the same way (unit
2.1.2).

The price falls on code that looks at the class: a `getClass()` check, a cast to a concrete type
or a serializer keyed by class name breaks when the factory picks another one. A factory promises
its declared type, and the class behind it can change between inputs and between JDK releases.

## A Constructor Can't Say How It Reads Its Arguments; a Factory's Name Can

`BigDecimal` has a constructor and a static factory that both take a `double`:

```java
BigDecimal byConstructor = new BigDecimal(0.1);
BigDecimal byFactory = BigDecimal.valueOf(0.1);
System.out.println("new BigDecimal(0.1):     " + byConstructor);
System.out.println("BigDecimal.valueOf(0.1): " + byFactory);
BigDecimal limit = new BigDecimal("0.1");
System.out.println("constructor over the 0.1 limit: " + (byConstructor.compareTo(limit) > 0));
System.out.println("factory over the 0.1 limit:     " + (byFactory.compareTo(limit) > 0));
System.out.println("new BigDecimal(2.675) to cents:     " + new BigDecimal(2.675).setScale(2, RoundingMode.HALF_UP));
System.out.println("BigDecimal.valueOf(2.675) to cents: " + BigDecimal.valueOf(2.675).setScale(2, RoundingMode.HALF_UP));
```

```text
new BigDecimal(0.1):     0.1000000000000000055511151231257827021181583404541015625
BigDecimal.valueOf(0.1): 0.1
constructor over the 0.1 limit: true
factory over the 0.1 limit:     false
new BigDecimal(2.675) to cents:     2.67
BigDecimal.valueOf(2.675) to cents: 2.68
```

The constructor reads the exact binary value of the `double`, which is not 0.1 (unit 1.1.1).
`valueOf` reads the `double` the way `Double.toString` prints it, which is the shortest decimal
that reads back as the same `double`, so a `double` written as 0.1 prints as 0.1. A fee of 0.1 built with `new` is
over a limit of 0.1, and a price of 2.675 rounded half-up comes out a cent short. Both readings
have uses, and both take one `double`, so only one of them could be a constructor: constructors
have no names of their own, so two with the same parameter list can't coexist (unit 2.1.2 shows
the compile error). For amounts, use the `String` constructor or `valueOf(long, int)`;
`valueOf(double)` is right only when the `double` came straight from a decimal literal.

## The GoF Factory Method Is a Creation Hook That Base-Class Code Calls and a Subclass Overrides

A listener task waits in `ServerSocket.accept()`, and on shutdown we cancel its `Future`. Unit
6.1.3 showed that a platform thread in `accept()` isn't woken by `interrupt()`, so `cancel(true)`
returns `true` and the worker stays where it is. Closing the socket would end the wait, but
`cancel` belongs to the `Future`, and `submit` creates the `Future` by wrapping the task in a
`FutureTask` (unit 6.4.1). It does that through a protected method, `newTaskFor`, which a subclass
can override:

```java
import java.util.concurrent.*;

/** A task that waits on something interrupt() can't reach, and knows how to close it. */
interface Closing<T> extends Callable<T> {
    void close();
}

class ClosingExecutor extends ThreadPoolExecutor {
    ClosingExecutor(int threads) {
        super(threads, threads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {    // submit() calls this
        if (!(task instanceof Closing<T> closing)) return super.newTaskFor(task);
        return new FutureTask<>(task) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                closing.close();                                      // what interrupt() can't do
                return super.cancel(mayInterruptIfRunning);
            }
        };
    }
}
```

The listener is a `Closing<String>` whose `close()` closes its server socket. The caller's code
is the same for both pools: `Future<String> f = pool.submit(listener)`, then,
once the listener is in `accept()`, `f.cancel(true)`, `shutdown()` and a 500 ms
`awaitTermination`:

```text
Executors.newFixedThreadPool(1): cancel=true, worker ended within 500 ms: false
  Executors.newFixedThreadPool(1) accept() ended: java.net.SocketException: Socket closed
  new ClosingExecutor(1) accept() ended: java.net.SocketException: Socket closed
new ClosingExecutor(1)        : cancel=true, worker ended within 500 ms: true
```

The first pool's worker left `accept()` only when the program closed the socket itself after the
wait; in the subclass, `cancel` closed it. `submit`, `invokeAll` and `invokeAny` are written once,
in `AbstractExecutorService`, and call `newTaskFor`; the subclass changed the product without
touching them or any caller. The base class owns the code that uses the object, and a subclass
owns the choice of object, where a static factory makes that choice inside one method.

`submit(Runnable)` goes through the other overload, `newTaskFor(Runnable, T)`. In a pool whose
override of the `Callable` overload returns its own product for every task, `submit(() -> "x")`
got that product and `submit(() -> { })` a plain `FutureTask`. A subclass overrides every overload
its callers can reach. A creation hook called from the base class's constructor runs before the subclass's fields
are assigned (unit 2.1.1); `newTaskFor` runs at each `submit`, after construction.

<!-- practice 2 -->

## Each Product Costs a Subclass, and the Hook Reaches Only Objects Built From It

The service gets its pools from `Executors`, and the next release moves the listeners to virtual
threads. Each pool's class, and whether it is a `ClosingExecutor` (the column `hook overridden`):

```text
java.util.concurrent.ThreadPoolExecutor          hook overridden: false
java.util.concurrent.ThreadPoolExecutor          hook overridden: false
java.util.concurrent.ThreadPerTaskExecutor       hook overridden: false
ClosingExecutor                                  hook overridden: true
```

`Executors.newFixedThreadPool` and `newCachedThreadPool` build a plain `ThreadPoolExecutor`, which
calls its own `newTaskFor`, not the override. `newVirtualThreadPerTaskExecutor()` returns
`ThreadPerTaskExecutor`, which doesn't extend `AbstractExecutorService` and has no `newTaskFor` at
all. The listener code compiles against all of them unchanged. On the two `ThreadPoolExecutor`s,
`cancel` again leaves the worker in `accept()`. On virtual threads the worker ends anyway: Java 21
closes the socket on an interrupt there (unit 6.1.3). But `close()` never runs, so any other
cleanup in it is skipped.

The count grows too. A second product, say a task that carries the request id into its logs, is
a second subclass with its own `newTaskFor`. A pool that needs both is a third that merges the two
overrides, since a class extends one superclass.

The same JDK class shows the other choice. `ThreadPoolExecutor` makes its threads through a
`ThreadFactory` passed to its constructor, and most `Executors` methods have an overload that takes
one: `Executors.newFixedThreadPool(4, Thread.ofPlatform().name("listener-", 0).factory())` names
the threads with no subclass. It can't change the `Future`, which is Challenge 3. A factory passed
in works with any creator that accepts it, and can be a lambda (unit 4.1.3). It sees only what the creator hands it, where an override can use the
subclass's fields. Keep the hook where the subclass exists for other reasons: every collection must
supply its own `iterator()`, and `AbstractCollection` builds `contains`, `remove` and `toString` on
it. Choosing an implementation from the class or module path is `ServiceLoader` (unit 8.1.5); one
shared instance is unit 9.1.1, and step-by-step construction is unit 9.1.3.

<!-- practice 3 -->

## Summary

- **`new` always allocates; a static factory can hand back an object it already made.** Share
  only immutable objects, never cache by caller input, and use `==` on the results only where the
  javadoc promises identity.
- **A static factory picks the class from its input.** `EnumSet` switches from `RegularEnumSet`
  to `JumboEnumSet` at 65 constants with no caller changed; code that checks the class breaks.
- **A factory's name says how it reads its arguments.** `new BigDecimal(0.1)` and
  `BigDecimal.valueOf(0.1)` take the same `double` and return different numbers.
- **The GoF factory method is a creation hook.** `submit` calls `newTaskFor`, so one override
  changes the `Future` for every caller; every overload callers reach must be overridden.
- **Each product costs a subclass, and the hook reaches only objects of that subclass.** Pools
  from `Executors` aren't your subclass, and the virtual-thread executor has no `newTaskFor` at
  all; a factory passed in reaches any creator that accepts it.

## Reference

**Static factories in the JDK, Java 21:**

| Call | Returns |
|---|---|
| `Integer.valueOf(int)` | a cached instance for -128 to 127 (javadoc); on HotSpot, up to `-XX:AutoBoxCacheMax` |
| `EnumSet.noneOf(type)` | `RegularEnumSet` for up to 64 constants, `JumboEnumSet` above (JDK source, not javadoc) |
| `List.of(...)` | an unmodifiable list; class not specified |
| `BigDecimal.valueOf(double)` | the value `Double.toString(d)` prints; `new BigDecimal(double)` is the exact binary value |

**Creation hooks in the JDK:**

| Hook | Called by | Product |
|---|---|---|
| `AbstractExecutorService.newTaskFor(Callable)` | `submit(Callable)`, `invokeAll`, `invokeAny` | `RunnableFuture` |
| `AbstractExecutorService.newTaskFor(Runnable, T)` | `submit(Runnable)`, `submit(Runnable, T)` | `RunnableFuture` |
| `Collection.iterator()` | `AbstractCollection.contains`, `remove`, `toString`, ... | `Iterator` |

## Challenges

1. Your `Money.of(amount, currency)` could cache zero for every currency. Measure what that saves
   in a service you know, and decide whether callers may then compare with `==` against a
   `Money.ZERO`.
2. A library you can't change exposes a public constructor for a class you'd like to share. What
   can your own code do, and what can't it?
3. Rewrite `ClosingExecutor` so that it works for any `ExecutorService`, the virtual-thread one
   included. What does the caller have to change, and what does `invokeAll` lose?

## Design Note: Why the Hook and the Parameter Both Survive

GoF's Factory Method comes from the application frameworks of the early 1990s, where the
framework's base class ran the algorithm and an application subclassed it to plug in its own
objects. Before lambdas, passing a creation object meant an anonymous class, so a subclass hook
cost about the same; `newTaskFor` dates from Java 6. Since Java 8 a creation function is cheap to pass, and newer JDK code takes one:
`Collectors.toCollection(Supplier)`, `ThreadLocal.withInitial`, `Thread.Builder.factory()`. The
hook stays where the creator and the product change together, as a collection and its iterator
do. Where the products vary on their own, a parameter saves a creator subclass per product.

## Code Examples

- Source: [FactoryMethodPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/FactoryMethodPattern.java)
- Test: [FactoryMethodPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/creationalpatterns/FactoryMethodPatternTest.java)
