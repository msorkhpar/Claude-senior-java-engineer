# 4.1.1. Introduction to Lambda Expressions

A trading dashboard's heap grows a few megabytes an hour. The heap dump shows 10,000 listeners
attached to the price feed, one for every quote widget ever opened, although each widget's
`close()` unsubscribes with exactly the text it subscribed with:

```java
void open() { feed.subscribe(q -> onPrice(q)); }
void close() { feed.unsubscribe(q -> onPrice(q)); }
```

The answer follows from what a lambda is: an object, of a type its
context picks, compiled into a method of the enclosing class, holding copies of what it captured.
Every output below is from Java 21.

## A Lambda Is an Object of Whatever Interface Its Context Expects

A lambda has no type of its own. The same text becomes an instance of whichever functional
interface the place it appears in asks for, its **target type**. Here `Greeting` is a
one-method interface of our own, `String text()`:

```java
Supplier<String> s = () -> "ready";
Callable<String> c = () -> "ready";
Greeting g = () -> "ready";
for (Object o : new Object[] {s, c, g}) {
    System.out.println(Arrays.toString(o.getClass().getInterfaces()));
}
System.out.println(c instanceof Supplier);
```

```text
[interface java.util.function.Supplier]
[interface java.util.concurrent.Callable]
[interface Target$Greeting]
false
```

The `Callable` is not a `Supplier`,
although its body is the same. A cast, `(Supplier<String>) c`, compiles, since one interface type
may be cast to another, and throws `ClassCastException` when it runs. Where nothing
names an interface, there is no lambda:

```java
var f = () -> "ready";
Object o = () -> "ready";
```

```text
NoTarget.java:3: error: cannot infer type for local variable f
        var f = () -> "ready";
            ^
  (lambda expression needs an explicit target-type)
NoTarget.java:4: error: incompatible types: Object is not a functional interface
        Object o = () -> "ready";
                   ^
```

The interface's single abstract method also gives the lambda its parameter types and return
type. Which contexts supply a target, and what to do when two overloads both could, is unit
4.1.2; the interfaces themselves are unit 4.1.3.

## The Compiler Turns a Lambda Into a Private Method and an invokedynamic Call

A lambda is often described as a shorter anonymous class. `Jobs` holds one of each:

```java
public class Jobs {
    public static void main(String[] args) {
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                System.out.println("anonymous");
            }
        };
        Runnable lambda = () -> System.out.println("lambda");
        anonymous.run();
        lambda.run();
        System.out.println(anonymous.getClass().getName());
        System.out.println(lambda.getClass().getName() + " hidden=" + lambda.getClass().isHidden());
    }
}
```

```text
$ ls *.class
Jobs$1.class
Jobs.class
$ javap -p Jobs
Compiled from "Jobs.java"
public class Jobs {
  public Jobs();
  public static void main(java.lang.String[]);
  private static void lambda$main$0();
}
$ javap -c -p Jobs   (excerpt)
       0: new           #7                  // class Jobs$1
       4: invokespecial #9                  // Method Jobs$1."<init>":()V
       8: invokedynamic #10,  0             // InvokeDynamic #0:run:()Ljava/lang/Runnable;
$ java Jobs
anonymous
lambda
Jobs$1
Jobs$$Lambda/0x00007192a8000c30 hidden=true
```

The anonymous class is `Jobs$1.class`, created with `new`; the lambda has no file. Its body became `lambda$main$0`, a private method of `Jobs`, and the place
where it stood became an `invokedynamic`. The first time that instruction runs, the JVM calls its
bootstrap method, `LambdaMetafactory.metafactory` (listed in `javap -v` under
`BootstrapMethods`), which spins a hidden class implementing `Runnable` whose `run()` calls
`lambda$main$0`. That class's name, `Jobs$$Lambda/0x…`, is what `toString()`, a heap dump or a
profiler shows, and it is not stable between runs. Stack traces hide it and show `lambda$main$0`.

<!-- practice 4 -->

## A Lambda Copies the Locals It Uses, So They Must Be Effectively Final

A retry job counts what it resent, and a report names its region:

```java
int resent = 0;
failed.forEach(id -> resent++);

String region = "eu-west";
Runnable report = () -> System.out.println(region);
region = "us-east";
```

```text
Retries.java:7: error: local variables referenced from a lambda expression must be final or effectively final
        failed.forEach(id -> resent++);
                             ^
Retries.java:10: error: local variables referenced from a lambda expression must be final or effectively final
        Runnable report = () -> System.out.println(region);
                                                   ^
```

The second error is on a line that only reads `region`: a second assignment anywhere, even after
the lambda, makes the local not effectively final. This factory
returns a lambda that outlives the call that made it:

```java
static Supplier<String> label(String region) {
    return () -> "report for " + region;
}
// in main:
Supplier<String> eu = label("eu-west");
System.out.println(eu.get());
System.out.println(Arrays.toString(eu.getClass().getDeclaredFields()));
```

```text
report for eu-west
[private final java.lang.String Snapshot$$Lambda/0x00007fba84000a08.arg$1]
```

When `eu.get()` runs, `label`'s stack frame is gone. The lambda object carries a copy of
`region` in a final field and passes it to the private method (`javap -p` shows
`lambda$label$0(java.lang.String)`). A copy can't follow later assignments, and the lambda may
run on another thread. So Java refuses to capture a variable that is assigned again, rather than let
the copy and the variable disagree. What is copied is the value: for a reference, the reference. The object it points to
can still change, and the lambda sees the change.

That is how the usual workaround works: a one-element array or an `AtomicInteger` is effectively
final, and the lambda changes the object inside. It compiles, but under a parallel stream:

```java
int[] hits = {0};
AtomicInteger atomic = new AtomicInteger();
IntStream.range(0, 1_000_000).parallel().forEach(i -> hits[0]++);
IntStream.range(0, 1_000_000).parallel().forEach(i -> atomic.incrementAndGet());
long counted = IntStream.range(0, 1_000_000).parallel().count();
// repeated three times, printing the three totals
```

```text
run 1: int[] 100,074  AtomicInteger 1,000,000  count() 1,000,000
run 2: int[] 142,131  AtomicInteger 1,000,000  count() 1,000,000
run 3: int[] 109,115  AtomicInteger 1,000,000  count() 1,000,000
```

The array lost most of its updates, a different number each run, with no error. `hits[0]++` reads
the element, adds one and writes it back, so two threads that read the same value write the same
result. `forEach` returned only after every element was processed, as the `AtomicInteger` total
shows. `AtomicInteger`
is correct, but every thread fights over one memory location. Letting the stream compute the
result (`count()`, `sum()`, a collector) needs no shared state at all. Unit 4.1.4 has more on
side effects in lambdas.

<!-- practice 1 -->

## A Lambda Adds No Scope: `this` and Every Name Mean What They Mean Outside It

An anonymous class is a class: its body is a new scope, with its own `this` and its own inherited
members. A lambda's body is in the scope of the method around it. A `Widget` whose `toString()`
returns `Widget[cpu-load]` hands out both:

```java
Runnable anonymous() {
    return new Runnable() {
        @Override
        public void run() {
            System.out.println("anonymous: this=" + this + ", toString()=" + toString());
        }
    };
}

Runnable lambda() {
    return () -> System.out.println("lambda:    this=" + this + ", toString()=" + toString());
}
```

```text
anonymous: this=Widget$1@15db9742, toString()=Widget$1@15db9742
lambda:    this=Widget[cpu-load], toString()=Widget[cpu-load]
```

In the anonymous class, even the unqualified `toString()` found the anonymous object's own
method, inherited from `Object`. In the lambda, both mean the widget, because the body is now `lambda$lambda$0`, an instance method of
`Widget`. A lambda that uses `this` (or any field or instance method) captures the enclosing
object, and reads its fields when it runs, not when it was made. In a `Gauge`:

```java
private String unit = "ms";
Supplier<String> viaField() { return () -> "unit=" + unit; }
Supplier<String> viaLocal() {
    String copy = unit;
    return () -> "unit=" + copy;
}
// after both are made, on the same Gauge g: g.unit = "s";
```

```text
field: unit=s   local copy: unit=ms
```

No new scope also means a lambda can't reuse a name the method already has. An anonymous class's
`apply(String id)` may shadow a local `id`; the lambda can't, as a parameter or a local:

```java
String id = "order-17";
// an anonymous Function's apply(String id) compiles; these two don't:
Function<String, String> lambda = id -> id.toUpperCase();
Runnable block = () -> {
    String id = "order-18";
};
```

```text
Shadow.java:12: error: variable id is already defined in method main(String[])
        Function<String, String> lambda = id -> id.toUpperCase();
                                          ^
Shadow.java:14: error: variable id is already defined in method main(String[])
            String id = "order-18";
                   ^
```

<!-- practice 2 -->

## A Lambda's Identity Is Not Promised: Keep the Instance You Need to Find Again

Each time a lambda expression is evaluated, the runtime decides whether to make a new object.
On HotSpot, a lambda that captures nothing comes back from the same site as the same object every
time, and one that captures anything is allocated fresh:

```java
static Supplier<String> fixed() { return () -> "eu-west"; }
static Supplier<String> of(String region) { return () -> region; }
```

```text
non-capturing, same site: true
capturing, same value:    false
```

The Java Language Specification (§15.27.4) allows a new object or an existing one, so code must
not depend on either. Lambdas don't override `equals`, so `equals` is
`==`, and two lambdas with the same text, or the same lambda evaluated twice with the same
captured value, are not equal.

Now the opening problem. `close()` evaluates a different lambda expression from `open()`: another
class, so another object, which the feed's list has never seen, and `unsubscribe` removes
nothing. Even one shared site wouldn't help, because `q -> onPrice(q)` captures `this` and is new
each time. Keep the instance:

```java
private final Consumer<String> listener = q -> onPrice(q);
void open() { feed.subscribe(listener); }
void close() { feed.unsubscribe(listener); }
```

```text
QuoteWidget: listeners after 10,000 open/close: 10000
FixedWidget: listeners after 10,000 open/close: 0
```

Every leaked listener also holds its widget, through the captured `this`, so the heap keeps
10,000 widgets too. For the same reason a lambda is no use as a map key (found only with the instance
that was put in) and as a lock: a non-capturing lambda may be one object shared by every caller,
and a capturing one is a different lock for each.

<!-- practice 3 -->

## Summary

- **A lambda is an object of the interface its context expects.** With no target type there is
  no lambda, and the same text gives unrelated types in different places.
- **The compiler makes a private method and an `invokedynamic`, not a class file.** The class is
  spun at run time by `LambdaMetafactory`, hidden, with an unstable name.
- **Captured locals are copied, so they must be effectively final.** A mutable holder gets around
  the rule and loses updates under concurrency.
- **A lambda adds no scope.** `this` is the enclosing object, its fields are read when the lambda
  runs, and a lambda can't shadow a local.
- **Lambda identity is unspecified.** Keep the instance you subscribed; never compare lambdas,
  key a map or lock on one.

## Reference

**Lambda and anonymous class (javac and HotSpot 21):**

| | Lambda | Anonymous class |
|---|---|---|
| Implements | the functional interface of its target type | any interface or class, any number of methods |
| Class file from javac | none; body in a private `lambda$<method>$<n>` method | `Outer$1.class` |
| Class at run time | hidden class from `LambdaMetafactory`, name `Outer$$Lambda/0x…` | `Outer$1` |
| `this`, unqualified methods | the enclosing instance and its members | the anonymous object and its inherited members |
| Shadowing a local | compile error | allowed for its own parameters and locals |
| New object per evaluation | unspecified (HotSpot: no if non-capturing, yes if capturing) | always (`new`) |

**javac errors on this page:**

| Code | Error |
|---|---|
| `var f = () -> ...` | `cannot infer type for local variable f (lambda expression needs an explicit target-type)` |
| `Object o = () -> ...` | `incompatible types: Object is not a functional interface` |
| a captured local assigned anywhere | `local variables referenced from a lambda expression must be final or effectively final` |
| lambda parameter or local named like a local | `variable id is already defined in method ...` |

## Challenges

1. Write a class with one capturing and one non-capturing lambda inside a loop of a million
   iterations, and measure allocation with `-Xlog:gc` or a profiler. Does escape analysis remove
   the capturing lambda's allocation once the loop is compiled?
2. A library's `removeListener` takes the listener to remove. Design the API so callers can't get
   the opening problem wrong. What does returning a handle from `addListener` cost?
3. A heap histogram from production shows 40,000 instances of `Orders$$Lambda/0x000071…`. How
   do you find the line of source they came from?

## Design Note: Why Java Captures Values, Not Variables

JavaScript lets a closure assign to the variables around it, so those variables must live on the
heap whenever a closure might capture them. Java keeps locals on the stack and copies what a
lambda captures, which makes a lambda an object with final fields and nothing else. The price is
paid by code that wants a shared counter: the `int[]` holder moves the variable to the heap by
hand, and takes on the thread-safety problem the rule kept out.

## Code Examples

- Test: [LambdaIntroductionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaIntroductionTest.java)
- Source: [LambdaIntroduction.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaIntroduction.java)