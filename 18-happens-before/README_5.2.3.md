# 5.2.3. Proper Synchronization to Ensure Memory Visibility

The shipping service reloads its rate table every five minutes. A scheduler thread builds a new
`RateTable`, fills it from the rates service and assigns it to a plain field, `rates`; every request
thread reads that field to price a parcel. In review, someone asks for `volatile`. The author answers
that a reference write is atomic, so a request sees either the old table or the new one, and that two
years of load tests never priced a parcel at zero.

Both facts are true, and the field is still a bug. Handing an object to another thread takes a
happens-before edge (unit 5.2.1) from the writes that build it to the reads that use it, unless the
object is built so that it needs none.

## A Reference Written Without an Edge Can Arrive Before the Writes That Built the Object

The JLS makes every read and write of a reference atomic (JLS 17.7): a reader gets the old reference
or the new one, never half of each. That says nothing about the object behind it. The scheduler
thread writes the table's fields and then the reference, and with no edge between those writes and
the request thread's reads, the JLS lets the request thread see the reference without the fields.
It may read them at their default values, `null`, `0` or `false`, which every field holds before a
constructor writes it. It may also keep reading the old reference for as long as it runs, because
the JIT may read a plain field once (unit 6.2.2).

We tried to catch the half-built case. One class, with one `final` field and one plain one:

```java
final class Rate {
    final int zone;                 // final: JLS 17.5 guarantees it through a race
    long centsPerKg;                // plain: no guarantee

    Rate(int zone, long centsPerKg) {
        this.zone = zone;
        this.centsPerKg = centsPerKg;
    }
}
```

In each of five rounds, the main thread creates 20 million rates and stores each reference into an array slot with a plain
write. A reader thread reads each slot with an opaque read (`VarHandle.getOpaque`), which the JIT must
perform every time but which orders nothing, waits if the writer hasn't reached the slot yet, and
checks both fields:

```java
Thread reader = new Thread(() -> {
    for (int i = 0; i < n; i++) {
        Object o = SLOT.getOpaque(slots, i);  // no ordering, and the JIT can't hoist it
        if (o == null) {
            seen[2]++;                        // got here before the writer
            while ((o = SLOT.getOpaque(slots, i)) == null) Thread.onSpinWait();
        }
        Rate r = (Rate) o;
        if (r.zone == 0) seen[0]++;
        if (r.centsPerKg == 0) seen[1]++;
    }
});
reader.start();
for (int i = 0; i < n; i++) {
    slots[i] = new Rate(i % 9 + 1, 400 + i % 300);   // a plain write of the reference
}
```

```text
round 1: 20,000,000 rates, 2,276,738 read as soon as written: zone==0 0, centsPerKg==0 0
round 2: 20,000,000 rates, 2,064,368 read as soon as written: zone==0 0, centsPerKg==0 0
round 3: 20,000,000 rates, 2,401,396 read as soon as written: zone==0 0, centsPerKg==0 0
round 4: 20,000,000 rates, 2,292,359 read as soon as written: zone==0 0, centsPerKg==0 0
round 5: 20,000,000 rates, 2,668,244 read as soon as written: zone==0 0, centsPerKg==0 0
```

Of 100 million racy publications, the reader reached more than ten million slots before the writer
and spun until the reference appeared, and it never saw a field at its default. A half-built object
is allowed by the JLS and was not observed here. A test that passes says nothing about the next JIT
or the next CPU.

> **Hardware differs.** These runs were on x86-64 with Java 21. x86 processors are documented to
> keep one store from passing another, which leaves the reordering to the JIT; processors with a
> weaker memory model, such as ARM, leave the race more room. We didn't run on ARM, so the result
> there is unknown.

What a half-built table would cost: a request that reads the table's rates before they arrive
answers "no rate for FR" for a country that has one.

## Safe Publication Is an Edge From the Object's Construction to the Read of Its Reference

Every safe way to hand over an object has the same shape. The writer builds the object, then does a
release action: a volatile write, an unlock, a `put` into a concurrent collection. The reader does the
matching acquire (a volatile read, a lock, a `get` or `take`) and only then reads the object's
fields. The construction comes before the release in the writer's program order, the release
happens-before the acquire, and the acquire comes before the field reads, so by transitivity the
reader sees the object as it was built. These are the idioms, from a program in which each one
handed a rate table to another thread:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/** A mutable table: only a safe publication makes its contents visible to another thread. */
final class RateTable {
    final Map<String, Long> centsPerKg = new HashMap<>();

    static RateTable load(long fr) {
        RateTable t = new RateTable();
        t.centsPerKg.put("DE", 450L);
        t.centsPerKg.put("FR", fr);
        return t;
    }
}

final class Holder {
    static final RateTable STARTUP = RateTable.load(520L);              // static initializer

    volatile RateTable current;                                      // volatile field
    final AtomicReference<RateTable> latest = new AtomicReference<>(); // AtomicReference

    private RateTable guarded;                                       // a lock, on both sides
    synchronized void put(RateTable t) { guarded = t; }
    synchronized RateTable get() { return guarded; }

    final ConcurrentMap<String, RateTable> byRegion = new ConcurrentHashMap<>(); // concurrent map
    final BlockingQueue<RateTable> handoff = new LinkedBlockingQueue<>();      // blocking queue
}
```

For the opening's reload, a `volatile` field costs a
request one volatile read and never makes it wait for the scheduler. `AtomicReference` does the same
and adds `compareAndSet` for writers that race each other. The lock works too, but every request then
takes it and waits whenever a reload holds it. A concurrent map fits tables kept per region, and a
queue fits an object handed to one consumer. Unit 5.2.2 quotes each class's javadoc promise.

The reader must take the same path: a request that reads
`guarded` without the lock gets no edge, even though the writer locked (unit 5.1.3). And the edge
covers the writes made before the release: a change to the table after it is published needs an
edge of its own (unit 5.3.3).

### A Static Initializer Publishes to Every Thread, and Fails for Every Thread

A class is initialized once, under a lock that every thread using the class synchronizes on
(JLS 12.4.2). A thread that finds the initializer running in another thread waits for it, so
whatever the initializer wrote during initialization is visible to every user of the class with no `volatile` at all. Here
the initializer holds while it calls the rates service, a request arrives meanwhile, and a second
class's initializer fails:

```java
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ClassInit {
    static final CountDownLatch loading = new CountDownLatch(1);
    static final CountDownLatch loaded = new CountDownLatch(1);

    static final class Rates {
        static final Map<String, Long> CENTS_PER_KG;
        static {
            loading.countDown();
            await(loaded);                                    // the call to the rates service
            CENTS_PER_KG = Map.of("DE", 450L, "FR", 520L, "NL", 410L);
        }
    }

    static final class FailedRates {
        static final Map<String, Long> CENTS_PER_KG = fetch();
        static Map<String, Long> fetch() {
            throw new IllegalStateException("rates service timed out");
        }
    }

    public static void main(String[] args) throws Exception {
        Thread startup = new Thread(() -> System.out.println("startup sees " + Rates.CENTS_PER_KG.size() + " rates"));
        startup.start();
        loading.await();                                      // startup is inside the initializer
        Thread request = new Thread(() -> System.out.println("request sees FR=" + Rates.CENTS_PER_KG.get("FR")));
        request.start();
        request.join(300);
        System.out.println("request 300 ms later: alive=" + request.isAlive() + ", state=" + request.getState());
        loaded.countDown();
        startup.join();
        request.join();

        for (int call = 1; call <= 2; call++) {
            try {
                FailedRates.CENTS_PER_KG.get("DE");
            } catch (Throwable t) {
                System.out.println("call " + call + ": " + t);
            }
        }
    }

    static void await(CountDownLatch latch) {
        try { latch.await(); } catch (InterruptedException e) { throw new IllegalStateException(e); }
    }
}
```

```text
request 300 ms later: alive=true, state=RUNNABLE
request sees FR=520
startup sees 3 rates
call 1: java.lang.ExceptionInInitializerError
call 2: java.lang.NoClassDefFoundError: Could not initialize class ClassInit$FailedRates
```

The request waited, then saw the whole map. While it waited, `getState()` said `RUNNABLE`, and a
thread dump (`jcmd <pid> Thread.print`) of the same wait showed `RUNNABLE` with the line `- waiting
on the Class initialization monitor for ...`. A startup stuck in an initializer shows up only in
those frames.

A class whose initializer threw is marked as failed and is never initialized
again: the first caller gets `ExceptionInInitializerError`, and every later use in that class loader
gets `NoClassDefFoundError` (on Java 21 its cause names the first exception). If the rates service
times out once during startup, pricing fails until the process restarts. Keep static initializers to
work that can't fail, and load anything remote into a `volatile` field that a retry can set again.
The lazy holder idiom, a nested class whose only member is the static field, runs the initializer at
the first use of that class (JLS 12.4.1) and fails the same way.

## final Fields Are Guaranteed Through a Race, and Nothing Else in the Object Is

`final` has a guarantee beyond the edges of unit 5.2.1 (JLS 17.5). An object is completely
initialized when its constructor finishes, and a thread that sees a reference to it only after that
"is guaranteed to see the correctly initialized values for that object's final fields", even when
the reference arrived through a data race. The guarantee extends to the objects and arrays reached
through a final field, which the reader sees at least as up to date as the field: a `final Map`
filled in the constructor arrives filled.

In `Rate`, a reader that got the reference through the plain slot is guaranteed `zone`, not
`centsPerKg`. The run saw both, so only the JLS separates them. `String` relies on it: JLS 17.5 gives it as the example of an object
that its final fields keep immutable without synchronization, so a string handed over through a race
reads the same in every thread.

The guarantee is about the object's own fields, not the field that holds the reference. The
opening's `rates` can't be `final`, because the scheduler reassigns it; it needs one of the idioms
above. And it covers only what the constructor wrote before it finished, which leaves two ways to
lose it: a constructor that hands out `this` before it finishes, and a final field that points at
something changed afterwards.

## A Constructor That Lets this Escape Hands Out an Object Before It Is Built

### Spot the Problem: Registered on the Last Line

A metrics library samples every gauge once when it is registered, as a baseline. Each gauge registers
itself on the last line of the base class's constructor, and every field is `final`:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Records each gauge it is given, and samples it once as a baseline. */
final class Registry {
    final List<Gauge> gauges = new ArrayList<>();

    void register(Gauge g) {
        gauges.add(g);
        System.out.println("baseline " + g.name() + " = " + g.value());
    }
}

abstract class Gauge {
    private final String name;

    protected Gauge(String name, Registry registry) {
        this.name = name;
        registry.register(this);                 // the last line: the gauge looks ready
    }

    String name() { return name; }

    abstract long value();
}

final class QueueDepth extends Gauge {
    private final BlockingQueue<?> queue;

    QueueDepth(BlockingQueue<?> queue, Registry registry) {
        super("orders.queue.depth", registry);
        this.queue = queue;
    }

    @Override
    long value() { return queue.size(); }
}

class Escape {
    public static void main(String[] args) {
        BlockingQueue<String> orders = new ArrayBlockingQueue<>(100);
        orders.add("order-1");
        orders.add("order-2");
        Registry registry = new Registry();
        try {
            new QueueDepth(orders, registry);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("the registry holds " + registry.gauges.size() + " gauge: " + registry.gauges.get(0).name());
    }
}
```

The queue holds two orders. What does the registry print?

```text
Cannot invoke "java.util.concurrent.BlockingQueue.size()" because "this.queue" is null
the registry holds 1 gauge: orders.queue.depth
```

A subclass constructor runs its superclass constructor before it assigns its own fields (JLS 12.5),
so `Gauge`'s last line is not `QueueDepth`'s. The registry got `this` while `queue` was still `null`
and read a `final` field at its default. The exception ended the constructor, yet the registry kept
the gauge, an object whose construction never finished. All of that happened on one thread. A registry that samples on its own thread is worse off: it saw the reference before the
constructor finished, and JLS 17.5 promises such a thread nothing about any field.

Java 21's compiler checks for this. `javac -Xlint:this-escape` warns "when a constructor invokes a
method that could be overriden in an external subclass" (its help text). With `Gauge` public:

```text
metrics/Gauge.java:8: warning: [this-escape] possible 'this' escape before subclass is fully initialized
        registry.register(this);
                          ^
1 warning
```

On the file above, where `Gauge` is package-private, it printed nothing: no subclass in another
package can extend it, so the lint doesn't look. An escape within one package goes unreported.

The fix is to finish building before anyone can see the object. The constructor only assigns
(`Gauge`'s now takes just the name), and a factory registers what the constructor returned:

```java
private QueueDepth(BlockingQueue<?> queue) {
    super("orders.queue.depth");
    this.queue = queue;
}

static QueueDepth register(BlockingQueue<?> queue, Registry registry) {
    QueueDepth gauge = new QueueDepth(queue);         // every constructor has finished
    registry.register(gauge);
    return gauge;
}
```

```text
baseline orders.queue.depth = 2
```

`this` escapes in less visible ways too, with the same fix. A constructor that starts a thread gives
it an edge for the writes before `start()` only (unit 5.2.1), not for the assignments after it. A
lambda or an inner-class listener that uses a field captures `this`, so subscribing it from a
constructor publishes the object. So does adding `this` to a static collection.

<!-- practice 1 -->

## Immutable Objects Need No Edge; Effectively Immutable Ones Need One Safe Publication

| Kind | What it is | To share it |
|---|---|---|
| immutable | every field `final`, `this` doesn't escape, nothing reachable changes after construction | any reference, even a racy one |
| effectively immutable | may have plain fields, but nobody changes it after publishing it | one safe publication, then no locks |
| mutable | changes after publication | a safe publication, and a lock or a thread-safe class for every later access (unit 5.3.3) |

The `RateTable` of the idioms fills a `HashMap` and is never touched again: effectively immutable.
Behind a `volatile` field it is correct, and requests read it without a lock. To survive a racy
reference too, it must become immutable, and a `record` is not enough on its own:

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Copies {

    record Zone(String name, List<String> countries) {}

    record ViewZone(String name, List<String> countries) {
        ViewZone { countries = Collections.unmodifiableList(countries); }
    }

    record CopiedZone(String name, List<String> countries) {
        CopiedZone { countries = List.copyOf(countries); }
    }

    public static void main(String[] args) {
        List<String> loading = new ArrayList<>(List.of("DE", "FR"));
        Zone zone = new Zone("EU-1", loading);
        ViewZone view = new ViewZone("EU-1", loading);
        CopiedZone copied = new CopiedZone("EU-1", loading);

        loading.add("NL");                                   // the loader goes on filling its list

        System.out.println("record:              " + zone.countries());
        System.out.println("unmodifiableList:    " + view.countries());
        System.out.println("List.copyOf:         " + copied.countries());
        try {
            copied.countries().add("PL");
        } catch (UnsupportedOperationException e) {
            System.out.println("copied.countries().add(\"PL\"): " + e);
        }
    }
}
```

```text
record:              [DE, FR, NL]
unmodifiableList:    [DE, FR, NL]
List.copyOf:         [DE, FR]
copied.countries().add("PL"): java.lang.UnsupportedOperationException
```

A record's fields are final, but `Zone`'s list is the loader's list, and the loader's `add` came
after the constructor. JLS 17.5 covers the list as it was when the constructor finished, so a
request that got the zone through a race may see two countries, three, or an `ArrayList` in the
middle of growing. `Collections.unmodifiableList` is a read-only view of the same list, not a copy.
`List.copyOf` copies inside the constructor, so the copy is reached through a final field and nobody
can change it. A map of lists needs every inner list copied as well: copying only the outer map
leaves the inner lists shared.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **An atomic reference write is not a safe handoff.** Without an edge, a reader may get the
  reference and see the object's fields at their defaults. The JLS allows it; 100 million racy
  publications on x86 didn't show it, so a test can't stand in for the edge.
- **Safe publication is a release by the writer after construction and the matching acquire by the
  reader:** a `volatile` or `AtomicReference` field, one lock on both sides, a concurrent collection,
  a static initializer. A static initializer that throws fails every later use of its class.
- **`final` fields are guaranteed through a race**, with what they reach as of the end of the
  constructor. Plain fields of the same object are not.
- **A constructor that lets `this` escape gives up that guarantee**, and even on one thread shows a
  subclass's final fields at their defaults. Build first, then register or start.
- **Immutable objects can be shared through any reference; effectively immutable ones need one safe
  publication.** A record holding a caller's list is neither until it copies the list.

## Reference

**Publication idioms:**

| Writer publishes with | Reader must use | Source of the edge |
|---|---|---|
| a static initializer | any use of the class | JLS 12.4.2, the initialization lock |
| a write to a `volatile` field | a read of that field | JLS 17.4.4 |
| `AtomicReference.set`, `compareAndSet` | `AtomicReference.get` | `java.util.concurrent.atomic` package docs: volatile memory effects |
| a write while holding monitor `m` | a read while holding `m` | JLS 17.4.4 |
| `put`/`add`/`offer` into a `java.util.concurrent` collection | `get`/`take`/`poll` of that element | `java.util.concurrent` package docs, "Memory Consistency Properties" |
| writes before `Thread.start()` | the started thread | JLS 17.4.4 |

**What a reader of a racily published object is guaranteed (JLS 17.5):**

| Field or object | Guaranteed value |
|---|---|
| a `final` field, object not escaped | the value the constructor wrote |
| an object or array reached through that field | its state when the constructor finished, or later |
| a plain field of the same object | none: the default or the written value |
| anything changed after the constructor finished | none |
| any field, if the reader saw `this` before the constructor finished | none |

**Class initialization (Java 21, observed):**

| Situation | What happens |
|---|---|
| another thread is running the initializer | the caller waits; its state reads `RUNNABLE`, its dump frame `waiting on the Class initialization monitor` |
| the initializer threw | first use: `ExceptionInInitializerError`; every later use: `NoClassDefFoundError: Could not initialize class ...` |

## Challenges

1. The rates service is down for the first minute after a deploy. Design the startup of the
   shipping service so that it serves requests with the last known table meanwhile, and say which
   idiom publishes each table.
2. A JSON library builds your immutable DTOs by setting `final` fields through reflection after the
   constructor has run. Read JLS 17.5.3: what must the library do for a thread that receives such an
   object to see its fields, and what would you rather do?
3. Find a class in your codebase that registers `this` or starts a thread in its constructor. Run
   `javac -Xlint:this-escape` over it. Does the compiler see it, and what would a factory change for
   its callers?

## Design Note: Why final Got Its Own Guarantee

Before Java 5 the memory model gave final fields no special treatment. The JSR-133 FAQ's example is
`String`, which then kept an offset into a shared character array: a thread could see a substring's
offset first as its default, 0, and later as 4, so `"/tmp"` could read first as `"/usr"`. JSR-133
fixed it by freezing final fields when the constructor ends, which lets an immutable class skip
synchronization entirely.

The freeze is tied to the constructor. Nothing the constructor lets out early and nothing changed
later through a final reference is covered, so an immutable class needs both: no escape, and copies
of whatever it was handed.

## Code Examples

- Source: [MemoryVisibility.java](src/main/java/com/github/msorkhpar/claudejavatutor/happensbefore/MemoryVisibility.java)
- Test: [MemoryVisibilityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/happensbefore/MemoryVisibilityTest.java)
