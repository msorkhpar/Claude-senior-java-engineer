# 9.2.3. Proxy Pattern

The order service wraps its inventory client in a retry proxy: one `InvocationHandler` that tries
a call up to three times when it fails with `IOException`. Since the release, the warehouse logs
show exactly one call per stock check, even during a network blip. The order code catches
`IOException` and shows "unavailable", and it now fails those requests with
`java.lang.reflect.UndeclaredThrowableException`, an exception nobody on the team throws.

A proxy implements the same interface as the object it stands for and controls how calls reach
it. `java.lang.reflect.Proxy` builds one at run time, with one handler for every call.

## One InvocationHandler Receives Every Call on Every Method of the Proxied Interfaces

The hand-written metrics decorator of unit 8.4.3 repeats the same `try`/`finally` in each of its
four methods. A dynamic proxy writes it once:

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/** One handler that times every call on any interface. */
final class Timing {
    static <T> T timed(T target, Class<T> type) {
        InvocationHandler handler = (proxy, method, args) -> {
            long start = System.nanoTime();
            try {
                return method.invoke(target, args);
            } finally {
                System.out.printf("  %s.%s took %d µs%n", type.getSimpleName(), method.getName(),
                        (System.nanoTime() - start) / 1_000);
            }
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }
}
```

`Inventory` has `int stock(String sku)` and `void reserve(String sku, int qty)`, both declaring
`IOException`. `timed` wraps it, and also a `String` seen as a `CharSequence`:

```java
Inventory inventory = Timing.timed(new WarehouseInventory(0), Inventory.class);
inventory.stock("A-100");
inventory.stock("A-100");
inventory.reserve("A-100", 2);

CharSequence sku = Timing.timed("B-200", CharSequence.class);   // String is final
System.out.println("length " + sku.length() + ", first " + sku.charAt(0));

System.out.println("class: " + inventory.getClass().getName()
        + ", instanceof WarehouseInventory: " + (inventory instanceof WarehouseInventory));
try {
    Proxy.newProxyInstance(String.class.getClassLoader(), new Class<?>[] {String.class},
            (p, m, args) -> null);
} catch (IllegalArgumentException e) {
    System.out.println("proxy of String: " + e);
}
```

```text
  Inventory.stock took 2154 µs
  Inventory.stock took 25 µs
  Inventory.reserve took 3615 µs
  CharSequence.length took 613 µs
  CharSequence.charAt took 714 µs
length 5, first B
class: $Proxy0, instanceof WarehouseInventory: false
proxy of String: java.lang.IllegalArgumentException: java.lang.String is not an interface
```

`newProxyInstance` generates a class (`$Proxy0`) implementing the listed interfaces. Each of its
methods passes `invoke` the proxy, the `Method` and the arguments (`null` when there are none).
The handler names no interface, so it works for any, the JDK's included. The first call to each method was slow because `Method.invoke` sets itself up on first
use.

The proxy is not a subclass of the target: `type.cast` to the interface succeeds, a cast to
`WarehouseInventory` throws `ClassCastException`, and `Proxy` refuses a class outright. Proxying a class takes a subclass
generated as bytecode, which is what CGLIB (copied inside Spring) and Byte Buddy's subclassing do,
and a final class such as `String` can't have one. Unit 10.3.3 covers `Method` and reflection
itself.

## equals, hashCode and toString Reach the Handler Too, So the Handler Decides the Proxy's Identity

The timing proxy now wraps a stock listener, `timed = Timing.timed(new ReorderListener(),
StockListener.class)`, whose `toString` is `reorder-listener`. It is registered in an `ArrayList`,
printed in a log line, and removed when its screen closes:

```java
List<StockListener> listeners = new ArrayList<>();
listeners.add(timed);
System.out.println("registered " + timed);
boolean removed = listeners.remove(timed);
System.out.println("removed: " + removed + ", still registered: " + listeners.size());
```

Once through `Timing`, once through `TimingFixed`, whose handler begins with the `switch` shown
further down:

```text
forwards everything:
  StockListener.toString took 277 µs
registered reorder-listener
  StockListener.equals took 905 µs
removed: false, still registered: 1
answers Object's methods:
registered timed reorder-listener
removed: true, still registered: 0
```

The proxy sends `equals`, `hashCode` and `toString` to `invoke`, with `Object` as the method's
declaring class; `getClass` and `Object`'s other methods it doesn't. The first handler forwards
them like any call. `remove(timed)` asked the proxy `equals(timed)`, the handler asked the target,
and the target, whose `equals` is `Object`'s identity check, said no. The screen is gone and its
listener stays registered, receiving every stock change.

The second handler answers the three methods before timing anything:

```java
if (method.getDeclaringClass() == Object.class) {
    return switch (method.getName()) {
        case "equals" -> proxy == args[0];
        case "hashCode" -> System.identityHashCode(proxy);
        default -> "timed " + target;                 // toString
    };
}
```

Identity by reference makes a proxy equal to itself and to nothing else. A handler may choose
otherwise, say two proxies over one target being equal, as long as `hashCode` agrees.

The `proxy` argument is the proxy, so whatever the handler does with it comes back through
`invoke`. A handler that logs `"calling " + method.getName() + " on " + proxy` calls `toString`
on the proxy, which calls the handler again:

```text
printing proxy inside invoke: java.lang.StackOverflowError
```

<!-- practice 1 -->

## Method.invoke Wraps Whatever the Target Throws, So the Handler Must Unwrap It

The retry handler from the opening compiles without a warning. Find why it never retries:

```java
InvocationHandler handler = (proxy, method, args) -> {
    for (int attempt = 1; ; attempt++) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            if (!(e instanceof IOException) || attempt == ATTEMPTS) throw e;
            System.out.println("  retrying after: " + e.getMessage());
        }
    }
};
```

The order code calls `stock` through it and prints the result, or the exception and its cause
chain, then how many calls reached the warehouse. `WarehouseInventory(2)` fails its first two
calls with `IOException("connection reset")`, and a blank SKU fails with
`IllegalArgumentException`:

```text
naive, 2 resets: java.lang.reflect.UndeclaredThrowableException
    caused by java.lang.reflect.InvocationTargetException
    caused by java.io.IOException: connection reset
    calls reaching the warehouse: 1
naive, blank sku: java.lang.reflect.UndeclaredThrowableException
    caused by java.lang.reflect.InvocationTargetException
    caused by java.lang.IllegalArgumentException: blank sku
    calls reaching the warehouse: 1
```

`Method.invoke` wraps anything the target throws, checked or not, in
`InvocationTargetException`. So `e` is never an `IOException`, and the first failure ends the
loop. `catch (IOException e)` doesn't compile ("exception IOException is never thrown in body of
corresponding try statement"), and `catch (Exception e)` is the quick way to silence the error.
The fix unwraps:

```java
} catch (InvocationTargetException e) {
    Throwable cause = e.getCause();            // what the target threw
    if (!(cause instanceof IOException) || attempt == ATTEMPTS) throw cause;
    System.out.println("  retrying after: " + cause.getMessage());
}
```

```text
  retrying after: connection reset
  retrying after: connection reset
unwrapping, 2 resets: stock 42
    calls reaching the warehouse: 3
  retrying after: connection reset
  retrying after: connection reset
unwrapping, 5 resets: unavailable (connection reset)
    calls reaching the warehouse: 3
unwrapping, blank sku: java.lang.IllegalArgumentException: blank sku
    calls reaching the warehouse: 1
```

Two resets are absorbed. After five, the caller gets the last `IOException`, which `stock`
declares, and its own `catch` shows "unavailable". The blank SKU is not retried and arrives as
itself. The timing handler of the first section has the same flaw: every exception the warehouse
throws through it reaches the caller wrapped twice.

## A Checked Exception the Interface Method Doesn't Declare Reaches the Caller as UndeclaredThrowableException

`invoke` is declared `throws Throwable`, so the compiler lets a handler throw anything, and the
proxy checks what comes out against the interface method. An unchecked exception, or a checked
one the method declares, passes through unchanged. Any other checked exception is wrapped in
`UndeclaredThrowableException`, a `RuntimeException`, because a caller compiled against
`int stock(String) throws IOException` can't be handed a checked exception it was never told
about. The leaked `InvocationTargetException` above was one. The retry library's own checked
exception, thrown when it gives up, is another:

```java
if (!(cause instanceof IOException)) throw cause;
if (attempt == ATTEMPTS) throw new RetriesExhaustedException(ATTEMPTS, cause);   // extends Exception
```

```text
givingUp, 5 resets: java.lang.reflect.UndeclaredThrowableException
    caused by Retry$RetriesExhaustedException: gave up after 3 attempts
    caused by java.io.IOException: connection reset
    calls reaching the warehouse: 3
```

Every caller's `catch (IOException e)` is skipped, and in a web service the request fails with
a server error instead of showing "unavailable". A handler throws the target's own exception, one
the called method declares (`method.getExceptionTypes()` lists them), or an unchecked one. A retry
library that wants a type of its own, and can't know what each interface declares, makes it a
`RuntimeException`.

<!-- practice 2 -->

## A Call the Target Makes on this Never Reaches the Proxy

A handler opens a transaction around each method marked `@Tx`, a runtime-retained annotation
it finds on the interface method the proxy passes it. Only `place` is marked. A batch method on
the same class places several orders:

```java
interface Orders {
    void placeAll(List<String> ids);
    @Tx void place(String id);
}

static final class OrderService implements Orders {
    public void placeAll(List<String> ids) {
        for (String id : ids) place(id);           // this.place(id)
    }
    public void place(String id) {
        System.out.println("    placing " + id);
    }
}
```

`orders` is the proxy over an `OrderService`, and the handler prints `BEGIN` and `COMMIT` around
each transaction. `orders.placeAll(List.of("O-7", "O-8"))` prints:

```text
placeAll on the proxy:
    placing O-7
    placing O-8
```

The proxy saw `placeAll`, which isn't marked, and passed it on. Inside the
target, `place(id)` means `this.place(id)`, and `this` is the `OrderService`, which knows nothing
of the proxy. A proxy intercepts only calls made on the proxy reference. For the same reason,
Spring's `@Transactional` on a method called from another method of the same bean is ignored in
Spring's default proxy mode.

The fix is to call `place` through the proxy. The loop moves to an object that holds it:

```java
static final class OrderBatch {
    private final Orders orders;
    OrderBatch(Orders orders) { this.orders = orders; }
    void placeAll(List<String> ids) {
        for (String id : ids) orders.place(id);
    }
}
```

```text
OrderBatch over the proxy:
  BEGIN  place
    placing O-7
  COMMIT place
  BEGIN  place
    placing O-8
  COMMIT place
```

A default method run with `InvocationHandler.invokeDefault` is different: the target never runs,
`this` inside its body is the proxy, and its calls to other interface methods come back through
`invoke`.

<!-- practice 3 -->

## Summary

- **One handler receives every call on the proxied interfaces**, for any interface but only
  interfaces: the proxy is no instance of the target's class, and a class needs a generated
  subclass.
- **`equals`, `hashCode` and `toString` reach the handler.** Forwarding `equals` to a target with
  identity `equals` makes the proxy unequal to itself, so a list can't remove it; printing `proxy`
  inside the handler recurses until `StackOverflowError`.
- **`Method.invoke` wraps the target's exception in `InvocationTargetException`.** A handler that
  doesn't unwrap it retries nothing and changes the type of every failure.
- **A checked exception the method doesn't declare becomes `UndeclaredThrowableException`**, which
  skips the caller's `catch`. Throw what the method declares, or something unchecked.
- **A call on `this` bypasses the proxy.** Calls that must be intercepted go through the proxy
  reference, which is why a self-call in Spring ignores the called method's `@Transactional`.

## Reference

**What reaches `invoke` (Java 21; `Proxy` and `InvocationHandler` javadoc, and this page's runs):**

| Call on the proxy | Reaches `invoke` | Notes |
|---|---|---|
| an interface method | yes | `args` is `null` when the method has no parameters |
| a default method | yes | `InvocationHandler.invokeDefault` (Java 16) runs its body |
| `equals`, `hashCode`, `toString` | yes | `method.getDeclaringClass()` is `Object` |
| `getClass` and `Object`'s other methods | no | |
| a call the target makes on `this` | no | |

**What the caller gets from the handler:**

| The handler | The caller gets |
|---|---|
| throws an unchecked exception or an error | the same object |
| throws a checked exception the interface method declares | the same object |
| throws any other checked exception, `InvocationTargetException` included | `UndeclaredThrowableException` with it as the cause |
| returns `null` for a primitive return type | `NullPointerException` |
| returns a value of another type | `ClassCastException` |

## Challenges

1. The retry proxy wraps an interface whose `reserve` is not idempotent. Which methods should it
   retry, and how would the handler know? Compare an annotation with a list of method names.
2. Put the timing proxy and the retry proxy around the same `Inventory`, in both orders. What does
   the timing line measure in each, and which order do you want on a dashboard?
3. A listener is wrapped and registered twice by mistake, as two proxies. With identity-based
   `equals`, can the second registration be detected? Design an `equals` that can, and say what
   it costs.

## Design Note: Why the JDK Proxies Interfaces Only

`Proxy` arrived in Java 1.3. The class it generates extends `java.lang.reflect.Proxy` and
implements the interfaces you list. Since it extends `Proxy`, it can't extend your class, and an
interface has no state and no constructor, so the proxy only has to answer calls. A subclass-based proxy can't intercept final or private methods, carries the
parent's fields unused, and must either run one of the parent's constructors or skip them,
which takes a library such as Objenesis.

The JDK can wrap an object only for callers that depend on
its interface, the dependency unit 8.1.5 argues for on other grounds. Code that names the class
can't be handed the proxy at all.

## Code Examples

- Implementation: [ProxyPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/ProxyPattern.java)
- Tests: [ProxyPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/structuralpatterns/ProxyPatternTest.java)
