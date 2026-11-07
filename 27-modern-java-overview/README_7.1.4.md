# 7.1.4. Java 17 (LTS)

An order service moves from Java 11 to 17. On 11 its log started with one line nobody read,
`WARNING: An illegal reflective access operation has occurred`, naming a serialization helper.
On 17 the service doesn't start: the same helper calls `setAccessible(true)` on a private field of
a JDK class and gets `InaccessibleObjectException`.

That is the change in 17 most likely to stop an upgrade. Sealed classes (2.5.x), records (2.4.x),
pattern matching for `instanceof` (1.5.2) and `Stream.toList()` (4.4.4) have their own units; the
Reference lists them. Outputs are from Java 21; behaviour specific to 17 is cited
by its JEP and was not run here.

## Java 17 Refuses Deep Reflection into the JDK, and --illegal-access Is Gone

This program reads the private `size` field of an `ArrayList`, the way serialization and mocking
libraries used to read JDK internals:

```java
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.List;

public class Encap {
    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>(List.of("a", "b"));
        Field size = ArrayList.class.getDeclaredField("size");
        System.out.println("trySetAccessible: " + size.trySetAccessible());
        try {
            size.setAccessible(true);
            System.out.println("size = " + size.get(list));
        } catch (InaccessibleObjectException e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        Field theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        System.out.println("sun.misc.Unsafe.theUnsafe: " + (theUnsafe.get(null) != null));
    }
}
```

```text
$ java Encap
trySetAccessible: false
InaccessibleObjectException: Unable to make field private int java.util.ArrayList.size accessible: module java.base does not "opens java.util" to unnamed module @15db9742
sun.misc.Unsafe.theUnsafe: true
$ java --illegal-access=permit Encap
OpenJDK 64-Bit Server VM warning: Ignoring option --illegal-access=permit; support was removed in 17.0
trySetAccessible: false
InaccessibleObjectException: Unable to make field private int java.util.ArrayList.size accessible: module java.base does not "opens java.util" to unnamed module @15db9742
sun.misc.Unsafe.theUnsafe: true
```

Since Java 9 the JDK is split into modules, and `java.base` doesn't *open* its packages: code
outside may use their public API but may not reflect on their private members. Up to 15 the JVM
let such access through and printed the warning from the opening, once, on the first access. Java 16 denied it by default,
and `--illegal-access=permit` still turned it back on (JEP 396). Java 17 removed the option
(JEP 403), so the JVM ignores it with the warning above.

Public members of exported packages are as reachable as before, and `sun.misc.Unsafe` stays open
on purpose: it lives in the `jdk.unsupported` module, which opens `sun.misc` (JEP 260).

`InaccessibleObjectException` is unchecked and is not a `SecurityException`. Reflective code
written before 9 typically catches `SecurityException` and `IllegalAccessException`, so this one
escapes both catches and, usually at startup, stops the application.

## Open One Package by Name, or Stop Reaching Inside

When a library can't be upgraded yet, open each package it needs, by name, on the command line:

```text
$ java --add-opens java.base/java.util=ALL-UNNAMED Encap
trySetAccessible: true
size = 2
sun.misc.Unsafe.theUnsafe: true
# MANIFEST.MF of encap.jar: Add-Opens: java.base/java.util
$ java -jar encap.jar
trySetAccessible: true
size = 2
sun.misc.Unsafe.theUnsafe: true
```

`--add-opens module/package=target` opens one package to one module; `ALL-UNNAMED` means the
class path. An executable jar can carry the same in its manifest's `Add-Opens` attribute, which
`java -jar` honours. Your own `module-info.java` can't do it: a module opens only its own
packages.

An opened package works only while the JDK's private fields stay as the library expects, and the
JDK promises nothing about them. `String`'s private `value` was a `char[]` up to Java 8 and has
been a `byte[]` since 9 (JEP 254). The lasting fix is to stop reaching inside: call `list.size()`,
move to a library version that uses public API, or use a `VarHandle` on your own fields instead of
`Unsafe`.

Code that must reflect on objects it doesn't own, such as a debug dump or a generic serializer,
should expect the refusal. `trySetAccessible()` returns `false` instead of throwing, as the first
line of the output shows, and the code can fall back to `toString()` or skip the field.

<!-- practice 1 -->

## RandomGenerator Names the Algorithm, and Replay Needs the Algorithm and the Seed

Java 17 added `java.util.random` (JEP 356): a root interface, `RandomGenerator`, and a factory that
finds algorithms by name. With `--release 16` the package doesn't exist:

```text
$ javac --release 16 Rng.java
Rng.java:4: error: package java.util.random does not exist
import java.util.random.RandomGenerator;
```

Two generators with the same algorithm and seed, one with another algorithm, and the old `Random`:

```java
RandomGenerator a = RandomGeneratorFactory.of("L64X128MixRandom").create(2024L);
RandomGenerator b = RandomGeneratorFactory.of("L64X128MixRandom").create(2024L);
RandomGenerator c = RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(2024L);
System.out.println("L64X128MixRandom(2024)     " + a.ints(5, 0, 100).boxed().toList());
System.out.println("L64X128MixRandom(2024)     " + b.ints(5, 0, 100).boxed().toList());
System.out.println("Xoroshiro128PlusPlus(2024) " + c.ints(5, 0, 100).boxed().toList());
System.out.println("Random(2024)               " + new Random(2024).ints(5, 0, 100).boxed().toList());
```

```text
L64X128MixRandom(2024)     [90, 33, 59, 12, 92]
L64X128MixRandom(2024)     [90, 33, 59, 12, 92]
Xoroshiro128PlusPlus(2024) [33, 61, 15, 44, 41]
Random(2024)               [50, 26, 86, 10, 75]
```

A sequence is fixed by the algorithm *and* the seed, so a test, a simulation or a chaos run that
must replay records both. Two shortcuts lose one of them:

- `RandomGenerator.of("L64X128MixRandom")` takes no seed; it is seeded differently every time.
- `RandomGenerator.getDefault()` picks the algorithm for you. On 21 it is `L32X64MixRandom`, and
  its javadoc says there is no guarantee it stays the same over time.

Names are case-sensitive: `L64x128MixRandom` throws `IllegalArgumentException`.

`Random`, `SecureRandom`, `SplittableRandom` and `ThreadLocalRandom` all implement
`RandomGenerator`, so a method that takes a `RandomGenerator` accepts any of them, and a test can
pass a seeded one.

### Spot the Problem: A Trimmed Runtime Has No Algorithms

A team builds a small runtime with `jlink`, letting `jdeps` choose the modules. `Jl` prints the
class of `RandomGenerator.of("Random")`, then asks for `"L64X128MixRandom"`:

```text
$ jdeps --print-module-deps Jl.class
java.base
$ jlink --add-modules java.base --output base-only
$ base-only/bin/java Jl
java.util.Random
Exception in thread "main" java.lang.IllegalArgumentException: No implementation of the random number generator algorithm "L64X128MixRandom" is available
```

The interface is in `java.base`, but on 21 the new algorithms live in the `jdk.random` module,
found at run time through `ServiceLoader`. `jdeps` sees no reference to it, so the image ships
without it, and only the legacy names resolve. Add `--add-modules jdk.random` to the `jlink` call.

## A Generator Shared Across Threads Is Either Slow or Wrong

`Random` is thread-safe: it updates its seed with a compare-and-set. The new algorithms are not,
as their javadoc says. Four threads draw 250,000 `long`s each from the generator they are given,
and we count every value that came out a second time:

```text
shared L64X128MixRandom      170,228 repeated values out of 1,000,000
shared java.util.Random            0 repeated values out of 1,000,000
one L64X128MixRandom each          0 repeated values out of 1,000,000
ThreadLocalRandom.current()        0 repeated values out of 1,000,000
```

Among a million draws from 2^64 possible values, a correct generator practically never repeats.
The shared one let several threads read the same state before any of them wrote it back, and they
produced the same numbers: 17 % of the draws were repeats, with no exception. For request ids that
means collisions; for sampling, a skewed sample.

The shared `Random` gives no repeats, but every draw is a compare-and-set on one field. Eight
threads drew 10 million `int`s each on a 20-CPU machine:

```text
shared java.util.Random       3041 ms
ThreadLocalRandom.current()     49 ms
```

`ThreadLocalRandom.current()` returns a generator whose state lives in the calling thread. Its
javadoc asks for the form `ThreadLocalRandom.current().nextInt(...)` at each use. Storing the
result in a field breaks this:

```java
static final ThreadLocalRandom JITTER = ThreadLocalRandom.current();   // captured once, by main
```

Two worker threads, started fresh in each run, draw four values from `JITTER`, then four from
`current()`. The program ran twice:

```text
run 1
Thread-0  stored [690, 827, 189, 189]  current() [979, 911, 524, 322]
Thread-1  stored [609, 852, 298, 239]  current() [1, 174, 262, 501]
run 2
Thread-0  stored [690, 827, 189, 189]  current() [48, 240, 763, 590]
Thread-1  stored [609, 852, 298, 239]  current() [160, 318, 869, 352]
```

The stored instance reads the calling thread's state, and a thread that never called `current()`
has never had it seeded. Its numbers then depend only on the thread's id, so with the same JVM
flags and CPU count they are the same after every restart. Used as retry jitter, every identical
pod of a service backs off by the same delays after a deploy, and the retries arrive together.

For a seeded run on several threads, give each thread its own generator, derived from the run's
seed so it replays too.

<!-- practice 2 -->

## The Security Manager Still Compiles, Warns, and Since 18 Refuses to Install

JEP 411 deprecated the Security Manager for removal in 17. It was the in-process sandbox for
applets and plugin hosts. This program asks whether one is installed, then installs one:

```java
System.out.println("installed before: " + (System.getSecurityManager() != null));
try {
    System.setSecurityManager(new SecurityManager());
    System.out.println("installed after: " + (System.getSecurityManager() != null));
} catch (UnsupportedOperationException e) {
    System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
}
```

```text
$ javac Sm.java
Sm.java:3: warning: [removal] getSecurityManager() in System has been deprecated and marked for removal
        System.out.println("installed before: " + (System.getSecurityManager() != null));
                                                         ^
Sm.java:5: warning: [removal] SecurityManager in java.lang has been deprecated and marked for removal
            System.setSecurityManager(new SecurityManager());
                                          ^
Sm.java:5: warning: [removal] setSecurityManager(SecurityManager) in System has been deprecated and marked for removal
            System.setSecurityManager(new SecurityManager());
                  ^
Sm.java:6: warning: [removal] getSecurityManager() in System has been deprecated and marked for removal
            System.out.println("installed after: " + (System.getSecurityManager() != null));
                                                            ^
4 warnings
$ javac --release 11 Sm.java
$ java Sm
installed before: false
UnsupportedOperationException: The Security Manager is deprecated and will be removed in a future release
```

The `[removal]` warnings appear without any `-Xlint` option, one per use. Compiled with
`--release 11`, the same source gives none, because `javac` checks it against the 11 API, where
nothing here was deprecated. A build pinned to an old release hides the change until the code runs
on the new one.

At run time the change came one release later. On 17, `setSecurityManager` still installed it,
with a warning (JEP 411, not run here). Since 18 it throws, as above, unless the JVM starts with
`-Djava.security.manager=allow`; on 24 no flag enables it (JEP 486). A plugin host upgraded to 21
with a catch-all around that call runs its plugins with no sandbox at all. JEP 411 gives the
reasons: few applications used it, and its permission model was brittle and hard to program
against. Most deployments now isolate with containers or operating-system processes instead.

<!-- practice 3 -->

## Summary

- **Java 17 refuses deep reflection into the JDK.** `setAccessible(true)` on a private JDK member
  throws the unchecked `InaccessibleObjectException`, and `--illegal-access` is ignored.
- **Open one package by name, or stop reaching inside.** `--add-opens` or the `Add-Opens`
  manifest attribute opens a package; reflective code uses `trySetAccessible()` and falls back.
- **`RandomGenerator` names the algorithm, and replay needs the algorithm and the seed.**
  `RandomGeneratorFactory.of(name).create(seed)` replays; `RandomGenerator.of` and `getDefault()`
  don't. A `jlink` image needs `jdk.random`.
- **A generator shared across threads is either slow or wrong.** The new algorithms repeat values
  under a race, a shared `Random` makes the threads queue, and a stored `ThreadLocalRandom`
  repeats across restarts.
- **The Security Manager is deprecated for removal.** Since 18 it doesn't install by default, and
  `--release 11` hides the warnings.

## Reference

**What Java 17 changed for an upgrade from 11:**

| Change | JEP | On this page or in |
|---|---|---|
| JDK internals strongly encapsulated, `--illegal-access` removed | 403 | this page |
| `java.util.random`: `RandomGenerator`, `RandomGeneratorFactory` | 356 | this page |
| Security Manager deprecated for removal | 411 | this page |
| sealed classes (standard) | 409 | 2.5.x |
| records, pattern matching for `instanceof` (standard in 16) | 395, 394 | 2.4.x, 1.5.2 |
| `Stream.toList()` (16): an unmodifiable list, unlike `collect(Collectors.toList())` | no JEP | 4.4.4 |

**Algorithms on Java 21** (`RandomGeneratorFactory.all()`):

| Kind | Names |
|---|---|
| splittable LXM (LCG plus XBG mix) | `L32X64MixRandom`, `L64X128MixRandom`, `L64X128StarStarRandom`, `L64X256MixRandom`, `L64X1024MixRandom`, `L128X128MixRandom`, `L128X256MixRandom`, `L128X1024MixRandom` |
| jumpable and leapable | `Xoroshiro128PlusPlus`, `Xoshiro256PlusPlus` |
| legacy | `Random`, `SecureRandom`, `SplittableRandom` (splittable) |

**Calls that ran on Java 21:**

| Call | Result |
|---|---|
| `field.trySetAccessible()` on a private member of a non-opened JDK package | `false` |
| `field.setAccessible(true)` on the same | `InaccessibleObjectException` |
| `RandomGenerator.of("NoSuchName")` | `IllegalArgumentException` |
| `ThreadLocalRandom.current().setSeed(1)` | `UnsupportedOperationException` |
| `System.setSecurityManager(...)` without `-Djava.security.manager=allow` | `UnsupportedOperationException` |

## Challenges

1. Your build has 40 dependencies. How do you find every reflective access into the JDK before the
   upgrade rather than in production, and which of them do you fix with `--add-opens`?
2. A load test must replay a failure seen last week, on 16 threads. Design the seeding so that
   adding a seventeenth thread doesn't change what the first sixteen draw.
3. A library you depend on calls `System.getSecurityManager()` on every request. What does that
   cost on 21, and what changes on 24?

## Design Note: Why the JDK Closed Its Internals

For twenty years any jar could read and write the JDK's private fields, and many did, so a change
to the layout of `String` or `ArrayList` broke libraries that had nothing to do with it. Strong
encapsulation gives the JDK back the right to change its internals. The price fell on the
libraries that relied on them: they needed new versions and, in the meantime, `--add-opens` lines
that every application had to copy into its start script. `sun.misc.Unsafe` was left open because
taking it away before libraries could move to `VarHandle` (9) and a public memory API (final in 22)
would have left them with nothing.

## Code Examples

- Implementation: [Java17Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java17Features.java)
- Tests: [Java17FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java17FeaturesTest.java)
