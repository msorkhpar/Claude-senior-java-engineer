# 2.1.2 Creating Objects and Using Constructors in Java

One release of the alerting service brought three tickets. The subscriptions endpoint fails with
`NoSuchMethodException: Subscription.<init>()`, although nobody removed a constructor. Rules
created with the short constructor accept a negative limit, or never match their metric. An alert
whose constructor threw an exception keeps logging `ALERT null`. Unit 2.1.1 covers the order in
which initializers run, and an override called from a superclass constructor.

## Declaring Any Constructor Takes Away the Default One

A class that declares no constructor gets a *default constructor*: no parameters, the class's own
access, and a body that only calls `super()`. Someone added one constructor to `Subscription`:

```java
class Subscription {
    private String topic;
    private String channel;

    Subscription(String topic) {                     // added for the new admin screen
        this.topic = topic;
        this.channel = "email";
    }
}
```

JPA providers, and JSON mappers by default, build objects through the no-argument constructor:

```java
Subscription s = Subscription.class.getDeclaredConstructor().newInstance();
```

```text
java.lang.NoSuchMethodException: Subscription.<init>()
```

Declaring a constructor removes the default one. javac doesn't check reflective calls, so the
failure waited for the first request. A subclass with no constructor of its own stops compiling, because its default
constructor calls `super()`:

```text
error: constructor Subscription in class Subscription cannot be applied to given types;
```

A class that must be built both ways declares the no-argument constructor itself.

## Overloaded Constructors Chain with this(...) to One That Checks and Assigns

The rule class had one constructor that checks its arguments. A two-argument one was added later,
with the assignments copied:

```java
import java.time.Duration;
import java.util.Objects;

class AlertRule {
    private final String metric;
    private final double limit;
    private final Duration window;

    AlertRule(String metric, double limit, Duration window) {
        if (!(limit > 0)) throw new IllegalArgumentException("limit must be positive: " + limit);
        this.metric = Objects.requireNonNull(metric, "metric").strip().toLowerCase();
        this.limit = limit;
        this.window = Objects.requireNonNull(window, "window");
    }

    AlertRule(String metric, double limit) {         // added later, assignments copied
        this.metric = metric;
        this.limit = limit;
        this.window = Duration.ofMinutes(5);
    }

    @Override public String toString() { return "AlertRule[" + metric + " > " + limit + " over " + window + "]"; }
}
```

Pause here: what does `new AlertRule(" CPU", -1)` build?

```text
copied:  AlertRule[ CPU > -1.0 over PT5M]
chained: java.lang.IllegalArgumentException: limit must be positive: -1.0
```

The copied constructor skipped the check and the normalisation. A limit of -1 is below every
reading, and a rule for `" CPU"` never matches the `cpu` readings. `ChainedRule` is the same class
with its short constructor delegating:

```java
ChainedRule(String metric, double limit) {
    this(metric, limit, Duration.ofMinutes(5));
}
```

Every overload then ends in the one constructor that checks and assigns, and a check added there
covers them all.

A `this(...)` or `super(...)` call must be the first statement. Parsing a spec such as `"cpu>90"`
before delegating fails with `call to this must be first statement in constructor`. The arguments
can't read instance fields either: `this(defaultLimit)` fails with `cannot reference defaultLimit
before supertype constructor has been called`. Compute arguments with static methods inside the
call:

```java
ChainedRule(String spec) {                        // "cpu>90"
    this(metricOf(spec), limitOf(spec));
}
```

```text
chained: ChainedRule[cpu > 90.0 over PT5M]
```

Unit 2.1.1 shows what Java 25 relaxes.

<!-- practice 1 -->

## A Constructor That Hands Out this Lets Others Use a Half-Built Object

The price feed sends each new subscriber the last price at once. An alert, whose `symbol` and
`limit` fields are `final` and whose `onPrice` prints an alert above `limit`, subscribes itself in
its constructor:

```java
PriceAlert(PriceFeed feed, String symbol, double limit) {
    feed.subscribe(this);
    if (!(limit > 0)) throw new IllegalArgumentException("limit must be positive: " + limit);
    this.symbol = symbol;
    this.limit = limit;
}
```

The last price was 101.5. We create an alert at 120, then one at -5, then publish 102.0:

```text
new PriceAlert(feed, "ACME", 120):
  ALERT null at 101.5
new PriceAlert(feed, "ACME", -5):
  ALERT null at 101.5
  limit must be positive: -5.0
listeners: 2; publish(102.0):
  ALERT null at 102.0
```

The feed called `onPrice` before the fields were assigned, so it read `null` and `0.0`, even
though both are `final`. The second constructor threw and `new` returned nothing, but the feed
already held the object. It answers every price for as long as the feed lives. The feed holds a
reference, not a copy: on the same thread, every field the constructor assigned before throwing
shows through it.

Moving `subscribe` to the last line helps only until a subclass adds fields (unit 2.1.1). Build
the object first, and publish it from a static factory:

```java
private SafeAlert(String symbol, double limit) {
    if (!(limit > 0)) throw new IllegalArgumentException("limit must be positive: " + limit);
    this.symbol = symbol;
    this.limit = limit;
}

static SafeAlert watch(PriceFeed feed, String symbol, double limit) {
    SafeAlert alert = new SafeAlert(symbol, limit);
    feed.subscribe(alert);
    return alert;
}
```

```text
SafeAlert.watch(feed, "ACME", 120), then (feed, "ACME", -5):
  limit must be positive: -5.0
listeners: 1; publish(102.0):
```

Nothing fires: 102.0 is under 120, and the -5 alert was never subscribed. The private
constructor forces every caller through `watch`.

> **Threads.** The Java Language Specification (§17.5) promises that another thread sees a `final`
> field's assigned value if it got the reference after the constructor finished. A thread started
> in the constructor, or a listener registered there and called from another thread, can see fields
> assigned after that point at their defaults.

<!-- practice 2 -->

## A Static Factory Can Name, Reuse or Choose the Class of What It Returns

Constructors are told apart only by their parameter types, so `Temperature(double celsius)` and
`Temperature(double fahrenheit)` can't both exist: `constructor Temperature(double) is already
defined`. Factories have names:

```java
final class Temperature {
    private final double kelvin;

    private Temperature(double kelvin) {
        this.kelvin = kelvin;
    }

    static Temperature ofCelsius(double celsius) {
        return new Temperature(celsius + 273.15);
    }

    static Temperature ofFahrenheit(double fahrenheit) {
        return new Temperature((fahrenheit - 32) * 5 / 9 + 273.15);
    }
}
```

`new` always returns a new object of exactly the named class (an anonymous `new C() { }` aside).
A factory may return an existing one, or a subclass:

```java
System.out.println("same Currency: " + (Currency.getInstance("EUR") == Currency.getInstance("EUR")));
System.out.println(List.of("x").getClass().getName());
System.out.println(List.of("x", "y", "z").getClass().getName());
```

```text
same Currency: true
java.util.ImmutableCollections$List12
java.util.ImmutableCollections$ListN
```

`Currency` promises one instance per currency; `List.of` promises only an unmodifiable `List`.
`==` compares references (unit 1.1.2), so it is true only when a factory returns the same object.
Code that locks on a factory's result may share the lock with every other caller. Code that checks
`getClass()` breaks when the JDK picks another class. Rely on identity or class only where the
javadoc promises it: `Integer.valueOf` promises its cache only for -128 to 127 (unit 1.1.1).

<!-- practice 3 -->

## Summary

- **Declaring any constructor takes away the default one.** Reflective frameworks then fail at run
  time, and subclasses stop compiling.
- **Overloaded constructors chain with `this(...)` to one that checks and assigns.** A copied
  overload skips the checks; arguments are computed by static methods, since the call comes first.
- **A constructor that hands out `this` lets others use a half-built object.** They see default
  values, and a constructor that throws leaves its escaped object behind. Publish from a factory.
- **A static factory can name, reuse or choose the class of what it returns.** Callers rely on
  identity or class only where it is documented.

## Reference

**Constructor rules (Java 21):**

| Situation | What happens |
|---|---|
| no constructor declared | default constructor: no parameters, the class's access, calls `super()` |
| any constructor declared, none without parameters | no default constructor; `getDeclaredConstructor()` throws `NoSuchMethodException` |
| subclass without a constructor, superclass has no no-arg one | compile error |
| statement before `this(...)` or `super(...)` | compile error |
| instance field read in `this(...)`'s arguments | compile error |
| constructor throws | `new` returns nothing; references handed out before the throw still reach the half-built object |

**`new` versus a static factory:**

| | `new` | static factory |
|---|---|---|
| name | the class's | any |
| result | always a new object | may be cached |
| class of the result | exactly the named class | the declared type or a subtype |
| can run code once the object is built (register it) | no | yes |

## Challenges

1. Your entity needs a no-argument constructor for JPA, but every object built in your own code
   must be valid. How do you keep application code off that constructor?
2. A base class registers `this` with a scheduler in its constructor. What can a subclass do about
   it, and what would you change in the base class?
3. Find a factory in a library you use that returns cached objects. Does its javadoc promise it?

## Design Note: Why a Constructor Can't Choose What It Returns

`new` allocates the object before any constructor code runs, so a constructor can only fill in the
object it was given. It can't return a cached one, pick a subclass, or have a name. That keeps
`new` predictable. The cost is that every class needing more
adds a static factory, and the reader no longer sees from the call whether the object is new.

## Code Examples

- Test: [Person2Test.java](src/test/java/com/github/msorkhpar/claudejavatutor/classobj/Person2Test.java)
- Source: [Person2.java](src/main/java/com/github/msorkhpar/claudejavatutor/classobj/Person2.java)
