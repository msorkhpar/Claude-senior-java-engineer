# 10.5.3. JIT (Just-In-Time) Compilation

After each deploy, the quote service answers its first requests several times slower than the
rest. On some days latency steps up again for a moment when a tenant's fee plug-in is loaded,
although no code on the hot path changed. And a microbenchmark in the same repository reports
that the hash at the heart of the quote cache costs 0.01 ns per call, less than one CPU cycle.

All three come from the JIT compiler: when it compiles, what it assumes while compiling, and what
it is allowed to leave out. Every output below is from Java 21 (Temurin 21.0.12) on Linux x64, on
one 20-thread laptop CPU that was also running other work, so only the ratios between timings
mean anything.

## A Method Starts in the Interpreter and Is Compiled in Tiers Once Its Counters Say It Is Hot

The JVM first runs a method's bytecode (unit 10.5.2) in the interpreter, which counts the method's
calls and its loop iterations. When those counts cross a threshold, it queues the method for
compilation on a background thread and keeps interpreting until the machine code is ready. This
program calls `score` ten million times per round, for five rounds:

```java
public class Warmup {
    record Weights(int factor) {}

    static final Weights WEIGHTS = new Weights(31);

    static int score(int x) {                          // the hot method
        return (x * WEIGHTS.factor()) ^ (x >>> 3);
    }

    static long round() {                              // one round: ten million calls
        long sum = 0;
        for (int i = 0; i < 10_000_000; i++) sum += score(i);
        return sum;
    }

    public static void main(String[] args) {
        long total = 0;
        for (int r = 1; r <= 5; r++) {
            long t0 = System.nanoTime();
            total += round();
            System.out.printf("round %d: %,7d us%n", r, (System.nanoTime() - t0) / 1000);
        }
        System.out.println("total " + total);          // the result is used
    }
}
```

`java -XX:+PrintCompilation Warmup`, trimmed to this class's methods:

```text
15    6       1       Warmup$Weights::factor (5 bytes)
15    7       3       Warmup::score (13 bytes)
15    8       4       Warmup::score (13 bytes)
16    7       3       Warmup::score (13 bytes)   made not entrant
17    9 %     3       Warmup::round @ 4 (26 bytes)
17   10       3       Warmup::round (26 bytes)
17   11 %     4       Warmup::round @ 4 (26 bytes)
20    9 %     3       Warmup::round @ 4 (26 bytes)   made not entrant
24   11 %     4       Warmup::round @ 4 (26 bytes)   made not entrant
46  130 %     4       Warmup::round @ 4 (26 bytes)
49  131       4       Warmup::round (26 bytes)
50   10       3       Warmup::round (26 bytes)   made not entrant
```

The columns are milliseconds since the JVM started, a compile id, attributes and the tier. Tiers 1 to 3 are C1, the fast compiler; tier 4 is C2, the optimising one.
`score` went from the interpreter to tier 3, which is C1 code that keeps counting and records
which branches and types it sees, then to tier 4, which C2 compiled from that profile. The getter
`factor()` went straight to tier 1, plain C1 code without profiling: HotSpot treats a method
that only returns a field as trivial. Tier 2, C1 with counters only, stands in for tier 3 when C2
falls behind. It never showed up for this class in ten runs; with a single C2 thread
(`-XX:CICompilerCount=2`), it did for some of the JDK's own methods, such as
`StringUTF16::getChar`.

**"made not entrant" does not mean something failed.** Compile 7, the tier-3 `score`, was made not
entrant a millisecond after compile 8 installed tier 4: new calls stop entering the old code and
go to the new one. The log prints the same words when compiled code is thrown away because an
assumption broke. What tells them apart is whether a newer compile of the same method was already
taking calls, as compile 8 was here.

`round` is called only five times, but its loop runs ten million times inside the first call.
A `%` line is an **on-stack replacement** (OSR) compile: machine code for the loop starting at
bytecode 4, which the running call jumps into mid-loop. Without it, round 1's loop would stay
interpreted to the end.

## Until the Compiled Code Is Installed, the Same Loop Runs Tens of Times Slower

The same program with the JIT off (`-Xint`), with C1 only (`-XX:TieredStopAtLevel=1`), and with the
defaults, three JVMs each (the script joins each JVM's five lines into one):

```text
### -Xint run 1
round 1: 202,929 us round 2: 185,668 us round 3: 185,615 us round 4: 183,388 us round 5: 186,502 us 
### -Xint run 2
round 1: 184,432 us round 2: 192,424 us round 3: 189,043 us round 4: 188,896 us round 5: 184,932 us 
### -Xint run 3
round 1: 182,570 us round 2: 192,310 us round 3: 183,785 us round 4: 185,834 us round 5: 186,698 us 
### -XX:TieredStopAtLevel=1 run 1
round 1:   9,551 us round 2:   6,305 us round 3:   6,302 us round 4:   6,318 us round 5:   6,321 us 
### -XX:TieredStopAtLevel=1 run 2
round 1:   9,640 us round 2:   6,298 us round 3:   6,305 us round 4:   6,345 us round 5:   6,343 us 
### -XX:TieredStopAtLevel=1 run 3
round 1:   9,845 us round 2:   6,307 us round 3:   6,651 us round 4:   8,919 us round 5:   6,318 us 
### default run 1
round 1:   8,947 us round 2:   8,798 us round 3:   4,845 us round 4:   4,848 us round 5:   4,803 us 
### default run 2
round 1:   6,958 us round 2:   7,412 us round 3:   3,299 us round 4:   3,313 us round 5:   3,302 us 
### default run 3
round 1:   6,773 us round 2:  10,367 us round 3:   4,782 us round 4:   4,233 us round 5:   3,289 us 
```

Interpreted, a round takes about 185 ms. C2's code takes 3.3 to 4.8 ms, 38 to 56 times less, and
C1's 6.3 ms. A request path that runs a few thousand methods pays the interpreter's price on each
of them until each one crosses its threshold. That is the slow start after a deploy, and why
services send warm-up traffic before taking real traffic.

Round 2 of the default runs is no faster than round 1, and slower in two of three. In the log,
compile 11, C2's loop code, was made not entrant at 24 ms, when round 1 left its loop.
`-Xlog:deoptimization=debug` names the reason (from another JVM, so the time differs):

```text
[0.036s][debug][deoptimization] cid=  11 osr level=4 Warmup.round()J trap_bci=7 osr_bci=4 unstable_if reinterpret
```

Bytecode 7 is the loop's exit test. In every iteration profiled before C2 compiled the loop, the
test had failed, so C2 compiled no code for leaving the loop, only a trap back to the interpreter.
Round 2 started in the tier-3 code (compile 10), got a new loop compile (130) at 46 ms, and the
tier-4 `round` that new calls enter (compile 131) arrived only at 49 ms.

<!-- practice 3 -->

## Compiled Code Rests on Assumptions, and a Class Loaded Later Can Make It Not Entrant

C2 compiles for the program it has seen so far. When `Fee.apply` has one implementation among the
loaded classes, C2 calls it directly and inlines it, and records a dependency: if a class that
overrides `apply` is loaded, this code is wrong. Here a subclass is loaded in round 6, the way a
plug-in would be, and never called:

```java
static class Fee {
    long apply(long cents) { return cents + 30; }
}

static class WaivedFee extends Fee {             // loaded in round 6, never called
    @Override long apply(long cents) { return cents; }
}

static long total(Fee fee, int n) {              // fee.apply has one target until round 6
    long sum = 0;
    for (int i = 0; i < n; i++) sum += fee.apply(i);
    return sum;
}
```

Each round calls `total(fee, 10_000)` a thousand times with one `Fee`, and round 6 starts with
`Class.forName("LateClass$WaivedFee")`, the binary name of the nested class. The rounds, printed with the JVM's uptime, and the log lines
for `total` from 89 ms on:

```text
85 ms  round  5   3,369 us
90 ms  round  6   8,854 us
99 ms  round  7   3,392 us
```

```text
89   88 %     4       LateClass::total @ 5 (29 bytes)   made not entrant
89   89       4       LateClass::total (29 bytes)   made not entrant
90  174 %     4       LateClass::total @ 5 (29 bytes)
93  175       4       LateClass::total (29 bytes)
```

Loading `WaivedFee` invalidated both tier-4 compiles of `total` at 89 ms, and round 6 took 2.6 times
as long as round 5 (in all three JVMs) until C2 had compiled `total` again for a hierarchy with two
implementations.

A second program, `LateInterface`, makes `Fee` an interface with `final` implementations. There the
same load changed nothing: no line for `total` after the load, and round 6 took as long as round
5. C2 had compiled that call with a check on the receiver's class rather than a dependency. In a
variant that also passes the new `Surcharge` to `total` in round 6, that first call failed the
check. Its non-OSR log lines and the round:

```text
74   90       4       LateInterface::total (31 bytes)   made not entrant
74  177       4       LateInterface::total (31 bytes)
74 ms  round  6  Surcharge  6,007 us
```

In both cases C2 recompiled within a few milliseconds and the rounds after returned to their
earlier times. A call site that keeps seeing three or more classes, none dominant, is megamorphic
and isn't inlined, which unit 4.1.3 shows bringing back boxing.

<!-- practice 1 -->

## A Benchmark Measures Nothing When the JIT Can Drop the Work or Fold Its Input

The hash from the opening, and three loops that time it:

```java
static long mix(long x) {                          // a 64-bit hash finaliser
    x ^= x >>> 33;
    x *= 0xff51afd7ed558ccdL;
    x ^= x >>> 33;
    x *= 0xc4ceb9fe1a85ec53L;
    return x ^ (x >>> 33);
}

static long discarded(int n) {
    for (int i = 0; i < n; i++) mix(i);            // result thrown away
    return 0;
}

static long consumed(int n) {
    long sum = 0;
    for (int i = 0; i < n; i++) sum += mix(i);     // every result reaches the return value
    return sum;
}

static long constantInput(int n) {
    long sum = 0;
    for (int i = 0; i < n; i++) sum += mix(42);    // used, but the input never changes
    return sum;
}
```

Each loop runs in its own JVM, for ten rounds of ten million calls, and prints the median of rounds
6 to 10. The returned sums are added up and printed. Before reading on: which of the three costs
what `mix` costs?

```text
### default
discarded      0.00 ns/call   (sink 0)
discarded      0.00 ns/call   (sink 0)
discarded      0.00 ns/call   (sink 0)
consumed       0.41 ns/call   (sink -5689120754516562996)
consumed       0.42 ns/call   (sink -5689120754516562996)
consumed       0.41 ns/call   (sink -5689120754516562996)
constantInput  0.01 ns/call   (sink 9141548895097605120)
constantInput  0.01 ns/call   (sink 9141548895097605120)
constantInput  0.01 ns/call   (sink 9141548895097605120)
### -Xint
discarded      17.58 ns/call   (sink 0)
discarded      17.83 ns/call   (sink 0)
discarded      18.71 ns/call   (sink 0)
consumed       18.53 ns/call   (sink -5689120754516562996)
consumed       18.47 ns/call   (sink -5689120754516562996)
consumed       18.30 ns/call   (sink -5689120754516562996)
constantInput  18.42 ns/call   (sink 9141548895097605120)
constantInput  18.31 ns/call   (sink 9141548895097605120)
constantInput  18.09 ns/call   (sink 9141548895097605120)
```

Only `consumed`. The interpreter does all the work in all three loops, at about 18 ns per call. C2
inlined `mix` into each loop. In `discarded` nothing used its result, so it deleted the
computation and then the empty loop (unit 10.3.4 shows the same for a reflective call).
`constantInput` uses every result, and still costs nothing: `mix(42)` has the same value on
every iteration, so C2 folded it to a constant while compiling. A related freedom lets the JIT
read a plain field once outside a spin loop (unit 6.2.2).

A measurement can be trusted only when the JIT can't predict the work: every result reaches
something it can't see through (the returned sum, printed at the end), and every input varies the
way production inputs do. JMH, the OpenJDK project's benchmark harness, consumes results and runs
fresh JVMs and warm-up rounds for you, but the inputs are still yours to choose. It wasn't
available for this page, so the loops do it by hand. When a number looks too good, `-Xint` shows
how much work there was.

<!-- practice 2 -->

## Summary

- **A method starts interpreted and is compiled in tiers once its counters say it is hot.**
  Tier 3 profiles, tier 4 is C2's code built from that profile, and "made not entrant" on a
  lower tier is usually a replacement.
- **Until compiled code is installed, the same loop runs 38 to 56 times slower here.** Warm-up
  is slow for each method separately, and an OSR compile thrown away at a loop's exit can make
  the second round slower than the first.
- **Compiled code rests on assumptions about the loaded classes and the types seen.** Loading a
  subclass that overrides a method, or calling with a new class, makes the code not entrant and
  costs a step in latency until C2 compiles again.
- **A benchmark measures nothing when the JIT can drop the work or fold its input.** Consume every
  result, vary the input, and compare with `-Xint` when a number looks too good.

## Reference

**Tiers (HotSpot, Java 21, tiered compilation on by default):**

| Tier | Code | Seen on this page |
|---|---|---|
| 0 | interpreter | every method at first; `-Xint` keeps it there |
| 1 | C1, no profiling | `Weights::factor`, a getter |
| 2 | C1, call and loop counters only | JDK methods, with `-XX:CICompilerCount=2` |
| 3 | C1, full profiling | `score` and `round` before C2 |
| 4 | C2, from the profile | `score`, `round`, `total` |

**`-XX:+PrintCompilation` columns:** milliseconds since start, compile id, attributes (`%` OSR,
`n` native wrapper), tier, `Class::method`, `@ bci` for OSR, bytecode size, then
`made not entrant` when the code stops taking new calls.

**Thresholds and limits (`java -XX:+PrintFlagsFinal -version`):**

| Flag | Value |
|---|---|
| `Tier3InvocationThreshold`, `Tier3CompileThreshold`, `Tier3BackEdgeThreshold` | 200, 2000, 60000 |
| `Tier4InvocationThreshold`, `Tier4CompileThreshold`, `Tier4BackEdgeThreshold` | 5000, 15000, 40000 |
| `MaxInlineSize` (largest method inlined at a call site that isn't hot, in bytes) | 35 |
| `FreqInlineSize` (largest method inlined at a hot call site, in bytes) | 325 |
| `CICompilerCount` (compiler threads, sized from the CPU count) | 12 on this machine |

**Flags used on this page:**

| Flag | Effect |
|---|---|
| `-XX:+PrintCompilation` | one line per compile and per code made not entrant |
| `-Xlog:deoptimization=debug` | one line per trap taken in compiled code, with its reason |
| `-Xint` | interpreter only |
| `-XX:TieredStopAtLevel=1` | C1 only, without profiling |

## Challenges

1. Your service takes traffic 10 seconds after start. Using `-XX:+PrintCompilation`, how would you
   find out whether its hot paths reach tier 4 before then, and what would you send as warm-up
   traffic?
2. A rules engine loads tenants' rule classes on demand under traffic. Run `LateClass` with ten
   subclasses loaded one per round. Does every load cost a step, or only the first?
3. Write a benchmark for `String.hashCode` on strings of varying length. What does the JDK's
   caching of a string's hash do to your measurement, and how do you keep it honest?

## Design Note: Why HotSpot Compiles for the Program It Has Seen

C2 compiles a method for the classes loaded and the types seen so far, instead of for every
program that could still happen. That lets it call one implementation directly, inline it, leave
out the code for a branch that was never taken, and compute a value once that never changed. A
compiler that had to be right for every future class could inline a virtual call only behind a
type check on every call, and would compile every branch the profile never saw.

The price is that compiled code can become wrong, so the JVM keeps the means to throw it away:
dependencies on the class hierarchy, traps where it guessed, and the interpreter to fall back to.
Performance then depends on history: the same method is fast or slow depending on what ran
before it.

## Code Examples

- Test: [JitCompilationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JitCompilationTest.java)
- Source: [JitCompilation.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JitCompilation.java)
