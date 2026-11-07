# 10.3.4. Reflection Performance Considerations

An event bus finds its `@Subscribe` methods at startup and calls them with `Method.invoke`, a few
hundred thousand times a second. A pull request replaces each `Method` with a `MethodHandle`,
"because reflection is slow". The reviewer answers that since JDK 18 `Method.invoke` runs on
method handles anyway (JEP 416), so the change buys nothing. Which one is right depends on a
detail neither of them mentions: where the `Method` or the handle is kept.

## A Loop Whose Result Nobody Reads Measures Nothing

Every number on this page comes from one harness, `Bench`, run with JDK 21 on one machine (a
20-thread laptop CPU that was running other work). JMH wasn't available, so `Bench` does by hand what
JMH does, as described below. It calls `Price.at(int)`, which returns `base + qty * 3`, one way per
JVM. Two of those ways:

```java
static long direct(Price p, int n) {
    long sum = 0;
    for (int i = 0; i < n; i++) sum += p.at(i);
    return sum;
}

static long directDiscarded(Price p, int n) {
    for (int i = 0; i < n; i++) p.at(i);           // result thrown away
    return 0;
}
```

Each JVM runs 10 rounds of 20 million calls (1 million for the lookup row below), treats the first five as warm-up, and reports the
median of the other five in nanoseconds per call, with the bytes the thread allocated per call
(`getThreadAllocatedBytes`, as in unit 4.1.3). The returned sums are added up and printed. A
driver, `Forks`, starts three JVMs per way. Before reading on: what does the second loop cost per
call?

```text
way                 flags                                     ns/call in 3 JVMs  bytes/call
direct                                                          0.22   0.21   0.24      0.0
directDiscarded                                                 0.00   0.00   0.00      0.0
```

Zero: the JIT saw that no one used the result and deleted the loop. The first loop shows a direct
call the JIT can compile into its caller: about 0.2 ns, a few instructions. A reflective variant that discarded its result could be deleted the same way and would look as fast,
so every variant below adds its result to the sum.

We ran the whole table three times and paste one of the two quieter runs. The ratios between rows
held in all three; in the run made while the machine was busiest, the reflective rows were up to
about twice as slow, and the direct and constant-handle rows hardly moved. Read the numbers as ratios on one machine, not as costs to quote.

## Looking a Method Up Costs More Than Calling It, So Look Up Once

A mapper that calls `getClass().getMethod("get" + name)` for every field of every object pays for
the lookup on every call:

```java
static long lookupEachCall(Price p, int n) throws Exception {
    long sum = 0;
    for (int i = 0; i < n; i++) sum += (int) Price.class.getMethod("at", int.class).invoke(p, i);
    return sum;
}
```

`invokeField` looks the `Method` up once and keeps it in a static field. `accessibleEachCall`
keeps it too, and calls `setAccessible(true)` before every call:

```text
invokeField                                                     5.72   5.45   4.89     32.0
accessibleEachCall                                              6.83   5.09   4.60     32.0
lookupEachCall                                                 26.89  27.12  26.64    200.2
```

The lookup costs about 22 ns and 168 bytes, four times the call it precedes. `getMethod` searches
the class's public methods and hands back a new `Method` every time:

```java
Method first = Price.class.getMethod("at", int.class);
Method second = Price.class.getMethod("at", int.class);
System.out.println("same object: " + (first == second) + ", equals: " + first.equals(second));
first.setAccessible(true);
System.out.println("first accessible: " + first.isAccessible() + ", second accessible: " + second.isAccessible());
```

```text
same object: false, equals: true
first accessible: true, second accessible: false
```

The accessible flag belongs to the copy, so a cache should store the `Method` after
`setAccessible(true)`. Setting it skips the access check inside `invoke`, which saved about
0.3 ns with a `Method` in a static final field (the next section explains why that one is faster)
and 0.5 to 0.9 ns with one in a plain field. Repeating `setAccessible(true)` before every call
(`accessibleEachCall` above) cost at most half a nanosecond more, within the spread between JVMs:

```text
invokeConstant                                                  1.13   1.18   1.36     16.0
invokeConstant      setAccessible(true)                         0.90   0.92   0.84     16.0
invokeField                                                     5.72   5.45   4.89     32.0
invokeField         setAccessible(true)                         4.49   4.60   5.36     32.0
```

The saving is under a nanosecond: use `setAccessible` to reach non-public members (unit 10.3.2),
not as an optimisation. Look members up
once, at startup or on first use, and key the cache by class, name and parameter types:
`at(int)` and `at(long)` are two methods, and two classes can share a simple name.

<!-- practice 1 -->

## Since JDK 18, Method.invoke Is Fast Only When the JIT Sees a Constant

`Bench` keeps a `Method` for `at` in four places:

```java
static final Method AT;                 // a constant to the JIT
static Method at;                       // the same Method, in a field the JIT can't trust
static final Holder IN_CLASS;           // Holder: a class with a field  final Method m
static final HolderRecord IN_RECORD;    // record HolderRecord(Method m)
```

and calls `(int) AT.invoke(p, i)`, `at.invoke(...)`, `IN_CLASS.m.invoke(...)` and
`IN_RECORD.m().invoke(...)`:

```text
invokeConstant                                                  1.13   1.18   1.36     16.0
invokeField                                                     5.72   5.45   4.89     32.0
invokeClassFinal                                                4.64   4.95   4.92     32.0
invokeRecordFinal                                               1.15   1.16   1.73     16.0
```

A `Method` calls its target through a method handle kept in its accessor. A stack
walk inside `at`, called through `invoke`, shows the accessor:

```text
call 20:
  jdk.internal.reflect.DirectMethodHandleAccessor.invoke
  java.lang.reflect.Method.invoke
  Which.main
```

When the `Method` is a constant to the JIT, the JIT can follow it to the accessor and the handle
and compile the call to `at` into the loop; the timings suggest that is what happened, though one
`Integer` survives. A static final field is a constant, and so is a record component read
through one: HotSpot trusts the final fields of records. It doesn't trust an ordinary `final`
instance field, which reflection can write (unit 10.3.2), so `IN_CLASS.m` costs as much as a
non-final field. The call then goes through a handle the JIT can't see into, four to five times
slower, and allocates an extra `Integer`. A framework that keeps its `Method`s in maps, or in
objects it built at startup, is in the slow rows, the event bus included.

## A Method Handle Obeys the Same Rule, and invokeExact Never Boxes

The same two places with a `MethodHandle` and `(int) handle.invokeExact(p, i)`:

```text
handleConstant                                                  0.33   0.23   0.27      0.0
handleField                                                     1.84   1.72   1.88      0.0
```

In a static final field the handle costs what a direct call costs. In a plain field it is seven
times slower than that, still three times faster than a `Method` in a field, and it allocates
nothing. So the pull request should help the event bus, whose handlers sit in a map, by about 3 ns a
call, an estimate: we measured one target, without the map lookup (Challenge 3). A static final
`Method` would be faster still, but handlers registered at run time can't each have a static
field.

The bytes column is boxing. `invoke(Object obj, Object... args)` receives the `int` as an
`Integer` inside a new `Object[]`, and returns the result as an `Integer`. `Boxes` calls three
methods through a `Method` in a non-final static field: `echo` returns `x`, `big` returns
`x + 1000`, `small` returns `x & 63`. The first column names the method, the second says whether
the argument is inside the `Integer` cache (-128 to 127). The last line calls `big` through a handle:

```text
$ java Boxes
echo   argument small ->  0.0 bytes/call (sum 945000000)
big    argument small -> 16.0 bytes/call (sum 30945000000)
small  argument big   -> 16.0 bytes/call (sum 945000000)
big    argument big   -> 32.0 bytes/call (sum 75059985000000)
big    argument big, bound handle, invokeExact ->  0.0 bytes/call (sum 75059985000000)
$ java -XX:-DoEscapeAnalysis Boxes
echo   argument small -> 24.0 bytes/call (sum 945000000)
big    argument small -> 40.0 bytes/call (sum 30945000000)
small  argument big   -> 40.0 bytes/call (sum 945000000)
big    argument big   -> 56.0 bytes/call (sum 75059985000000)
big    argument big, bound handle, invokeExact ->  0.0 bytes/call (sum 75059985000000)
```

Each value outside the cache costs a 16-byte `Integer`. Escape analysis removes the 24-byte array,
which never leaves `invoke`; `-XX:-DoEscapeAnalysis` brings it back. A benchmark that passes small loop
counters shows none of the allocation that real ids and amounts cause. The handle was made once from a
`Method`, bound to its receiver, and called with the `int` types it has:

```java
h = MethodHandles.lookup().unreflect(Calc.class.getMethod("big", int.class)).bindTo(c);
// in the loop:
sum += (int) h.invokeExact(i + 1_000);
```

<!-- practice 2 -->

## The Pre-JDK 18 Reflection Won Only While One Method Used the Call Site

JDK 21 still has the old implementation, behind `-Djdk.reflect.useDirectMethodHandle=false`. It
calls through native code for the first calls, then through a class it generates for each
`Method`:

```text
$ java -Djdk.reflect.useDirectMethodHandle=false Which
call 1:
  jdk.internal.reflect.NativeMethodAccessorImpl.invoke0
  jdk.internal.reflect.NativeMethodAccessorImpl.invoke
  jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke
call 20:
  jdk.internal.reflect.GeneratedMethodAccessor1.invoke
  jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke
  java.lang.reflect.Method.invoke
tax, call 20:
  jdk.internal.reflect.GeneratedMethodAccessor2.invoke
```

With the flag, a `Method` in a field looks two and a half times faster:

```text
invokeField                                                     5.72   5.45   4.89     32.0
invokeField         useDirectMethodHandle=false                 1.97   1.94   2.92     16.0
invokeField         others first                                5.42   5.19   5.43     32.0
invokeField         others first useDirectMethodHandle=false    4.86   4.86   4.86     56.0
```

In the second row one `Method` passes through `invoke`, so the call from
`DelegatingMethodAccessorImpl` always reaches `GeneratedMethodAccessor1`, which the JIT can inline.
"Others first" sends three other classes' `at` methods through the same loop before measuring, as a
framework does with many handlers. That call then sees four generated classes, and the timing and the
56 bytes show the JIT no longer optimising through it: the old implementation is about as fast as
the new one, and allocates the array as well. The new one costs the same either way. Only a
benchmark with one `Method` shows the old implementation ahead. The flag is gone anyway: JDK 25
ignores it (we ran `Which` with it and got `DirectMethodHandleAccessor`).

<!-- practice 3 -->

## Summary

- **A loop whose result nobody reads measures nothing.** The JIT deletes it; use every result,
  warm up, and compare in fresh JVMs.
- **A lookup costs more than the call.** `getMethod` searches and copies on every call; cache the
  `Method` with its accessible flag set, keyed by class, name and parameter types. `setAccessible`
  saves under a nanosecond.
- **`Method.invoke` is fast only on a constant `Method`.** A static final field or a record
  component reached through one; a plain or `final` instance field is four to five times slower.
- **A method handle follows the same rule, and with `invokeExact` it never boxes.** In a field it is still about three
  times faster than a `Method` in a field; `invoke` boxes every primitive outside the `Integer`
  cache.
- **The old implementation only wins a one-method benchmark.** With several `Method`s through one
  call site it is no faster, and JDK 25 has dropped the switch.

## Reference

**Ways to call `Price.at(int)`, JDK 21, one machine, median ns per call in three JVMs:**

| Way | Kept in | ns/call | Bytes/call |
|---|---|---|---|
| direct call | | 0.21–0.24 | 0 |
| `MethodHandle.invokeExact` | static final field | 0.23–0.33 | 0 |
| `Method.invoke` | static final field, or record component through one | 1.1–1.7 | 16 |
| `MethodHandle.invokeExact` | non-final field | 1.7–1.9 | 0 |
| `Method.invoke` | non-final field, or final field of a class | 4.6–5.7 | 32 |
| `getMethod` + `invoke` | looked up each call | 26.6–27.1 | 200 |

**What `Method.invoke(obj, args...)` allocates for an `int`-to-`int` method, `Method` in a non-final field (`Boxes`; a constant `Method` allocated one `Integer` fewer in `Bench`):**

| Object | Bytes | Removed when |
|---|---|---|
| the `Object[]` for the arguments | 24 | escape analysis is on (the default) |
| an `Integer` per argument and for the result | 16 each | the value is in -128..127 (the `Integer` cache) |

**Switches (JDK 21):**

| Option | Effect |
|---|---|
| `-Djdk.reflect.useDirectMethodHandle=false` | the pre-JDK 18 implementation: native accessor for the first calls, then a generated `GeneratedMethodAccessorN` per `Method`; ignored by JDK 25 |
| `-XX:-DoEscapeAnalysis` | turns escape analysis off; shows allocations the JIT normally removes |

## Challenges

1. Rerun `Bench` with `at` taking and returning `long`, and with five arguments instead of one.
   Which rows change, and why does the bytes column grow?
2. Your JSON mapper calls getters through `Method.invoke` from a `Map<Class<?>, List<Method>>`.
   Is it worth converting to method handles, to `LambdaMetafactory`-made functions, or to
   generated code? Decide what you would measure first.
3. `Bench` measures one call site in a quiet loop. Design a measurement closer to the event bus:
   50 handler classes, events of random types. Which rows do you expect to move?

## Design Note: Why JDK 18 Rebuilt Reflection on Method Handles

Before JDK 18, core reflection had call machinery of its own, separate from method handles: native
code for the first calls of each `Method`, then a generated class per hot `Method`. Since JDK 18
it uses method handles, the machinery that `invokedynamic` and lambdas already used, so the JDK
keeps one mechanism instead of two. On our runs the change cost a constant `Method` nothing (1.1 to
1.6 ns with either implementation) and removed the class per `Method`; what it lost is the speed of
a single `Method` held in a field. The design also makes the place you keep a `Method` part of its
cost, which no signature shows: moving a `Method` from a static final field into a map is a
refactoring that makes each call four times slower.

## Code Examples

- Test: [ReflectionPerformanceTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/ReflectionPerformanceTest.java)
- Source: [ReflectionPerformance.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/ReflectionPerformance.java)
