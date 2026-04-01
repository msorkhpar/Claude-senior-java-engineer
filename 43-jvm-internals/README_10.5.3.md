# 10.5.3. JIT (Just-In-Time) Compilation

## Concept Explanation

Just-In-Time (JIT) compilation is one of the JVM's most powerful performance optimization features. It bridges the gap
between interpreted bytecode execution and native machine code performance by dynamically compiling frequently executed
bytecode ("hot code") into optimized native machine code at runtime.

**Real-world analogy**: Imagine you're a translator at an international conference. Initially, you translate each
sentence one at a time as speakers talk (interpretation). But you notice certain speakers repeat the same phrases often.
So you prepare pre-translated cards for those common phrases (C1 compilation -- quick but basic optimization). For the
keynote speaker who talks the longest and most frequently, you prepare a complete, polished translation of their entire
speech in advance (C2 compilation -- aggressive optimization). This is exactly how the JIT compiler works -- it
identifies "hot" code and progressively optimizes it.

### How JIT Compilation Works

The HotSpot JVM uses a **tiered compilation** system (default since Java 8):

1. **Level 0 -- Interpreter**: Bytecode is interpreted directly. Fast startup but slow execution.
2. **Level 1 -- C1 with full optimization (simple)**: Quick compilation with basic optimizations.
3. **Level 2 -- C1 with invocation/backedge counters**: C1 compilation plus profiling data collection.
4. **Level 3 -- C1 with full profiling**: C1 compilation with comprehensive profiling.
5. **Level 4 -- C2 (Server Compiler)**: Aggressive optimization using profiling data from level 3.

The typical path for hot methods is: Level 0 -> Level 3 -> Level 4.

### Key JIT Optimizations

1. **Method Inlining**: Replaces method calls with the method body, eliminating call overhead and enabling further
   optimizations. The single most impactful optimization.
2. **Escape Analysis**: Determines if an object escapes the method/thread scope. If not, it can be stack-allocated or
   eliminated entirely.
3. **Loop Optimizations**: Loop unrolling, loop-invariant code motion, range check elimination.
4. **Dead Code Elimination**: Removes code that can never be reached or whose results are never used.
5. **Null Check Elimination**: Removes redundant null checks based on dominance analysis.
6. **Devirtualization**: Converts virtual method calls to direct calls when only one implementation exists.
7. **Intrinsics**: Replaces certain method calls with hand-optimized native code (e.g., `Math.min`, `String.equals`,
   `Arrays.copyOf`).
8. **Speculative Optimizations**: Makes optimistic assumptions based on profiling data, with "uncommon traps" as
   fallback.

### Deoptimization

When the JVM's speculative assumptions become invalid, it must **deoptimize** -- revert from compiled native code back
to interpreted execution. Common triggers:

- A class is loaded that invalidates a monomorphic call site assumption
- An uncommon trap is hit (e.g., a branch that was never taken during profiling)
- Code is replaced via hot-swap (debugging)

## Key Points to Remember

1. The JIT compiler only kicks in after a method has been called enough times (compilation threshold).
2. **Tiered compilation** (default since Java 8) uses C1 for quick compilation and C2 for aggressive optimization.
3. **Method inlining** is the most important JIT optimization -- it enables most other optimizations.
4. **Escape analysis** can eliminate heap allocations entirely (scalar replacement).
5. The JIT compiler uses **profiling data** (collected during interpretation and C1 execution) to make optimization
   decisions.
6. **Deoptimization** can occur when assumptions made during compilation become invalid.
7. The **on-stack replacement (OSR)** mechanism allows the JVM to switch from interpreted to compiled code mid-execution
   (e.g., inside a long-running loop).
8. GraalVM offers an alternative JIT compiler written in Java, enabling advanced optimizations.

## Relevant Java 21 Features

- **Tiered compilation is default**: All modern JVMs use tiered compilation, combining C1 and C2 compilers.
- **GraalVM integration**: GraalVM's compiler can be used as a drop-in replacement for C2 via
  `-XX:+UseJVMCICompiler`.
- **Segmented code cache**: Since Java 9, the code cache is divided into segments (non-method, profiled, non-profiled)
  for better management.
- **Compact object headers (Project Lilliput)**: Experimental in Java 21, reduces object header size, improving cache
  efficiency for JIT-compiled code.
- **Profile-guided optimization improvements**: Better profiling data collection for virtual thread workloads.

## Common Pitfalls and How to Avoid Them

1. **Benchmarking without warmup**: JIT compilation occurs progressively; cold code is much slower than warmed-up code.
   ```java
   // WRONG: Measuring cold performance
   long start = System.nanoTime();
   result = compute(data);
   long elapsed = System.nanoTime() - start; // Includes interpretation + compilation

   // RIGHT: Warm up first, then measure
   for (int i = 0; i < 10_000; i++) {
       compute(data); // Warmup iterations
   }
   long start = System.nanoTime();
   result = compute(data); // Now measures JIT-compiled code
   long elapsed = System.nanoTime() - start;

   // BEST: Use JMH (Java Microbenchmark Harness) which handles warmup properly
   ```
   **Solution**: Always use JMH for microbenchmarks, or manually warm up code before measuring.

2. **Megamorphic call sites killing performance**: Having too many implementations at a call site prevents
   devirtualization.
   ```java
   // Monomorphic (1 type) -- JIT can devirtualize and inline
   Shape s = new Circle();
   s.draw(); // Always Circle.draw() -- optimized

   // Bimorphic (2 types) -- JIT can handle with type check
   Shape s = random ? new Circle() : new Square();
   s.draw(); // Two possibilities -- still optimizable

   // Megamorphic (3+ types) -- JIT gives up on devirtualization
   Shape s = getRandomShape(); // Circle, Square, Triangle, Hexagon...
   s.draw(); // Virtual dispatch every time -- slower
   ```
   **Solution**: Minimize polymorphism at hot call sites. Use sealed classes to help the JIT.

3. **Methods too large to inline**: HotSpot won't inline methods larger than 325 bytes (default) or called methods
   larger than 35 bytes (frequent inlining threshold).
   ```java
   // This large method won't be inlined
   public int computeEverything(int input) {
       // 500+ lines of code
   }
   ```
   **Solution**: Keep methods small and focused. Use `-XX:MaxInlineSize` and
   `-XX:FreqInlineSize` for tuning.

4. **Escape analysis defeated by external references**:
   ```java
   // Object escapes -- cannot be stack-allocated
   public Point getPoint() {
       Point p = new Point(1, 2);
       return p; // p escapes the method
   }

   // Object does NOT escape -- can be stack-allocated or eliminated
   public int getDistance() {
       Point p = new Point(3, 4);
       return p.x * p.x + p.y * p.y; // p never escapes
   }
   ```
   **Solution**: Design APIs so that intermediate objects don't escape hot methods.

## Best Practices and Optimization Techniques

1. **Write simple, idiomatic code**: The JIT compiler is tuned to optimize common Java patterns. Clever tricks often
   inhibit optimization.
2. **Keep hot methods small**: Smaller methods are more likely to be inlined, which enables cascading optimizations.
3. **Use final and sealed classes**: They help the JIT devirtualize method calls.
4. **Favor immutable objects in hot paths**: They enable better escape analysis and elimination.
5. **Use `@ForceInline` and `@DontInline`** (JDK-internal, for JDK developers) -- for application code, structure
   methods to be naturally inline-friendly.
6. **Monitor JIT behavior**: Use `-XX:+PrintCompilation` to see what's being compiled, and
   `-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining` to see inlining decisions.
7. **Use JMH for benchmarking**: It handles warmup, dead code elimination prevention, and compiler blackholes correctly.
8. **Avoid reflection in hot paths**: Reflective calls are harder for the JIT to optimize.

## Edge Cases and Their Handling

1. **Code cache exhaustion**: When the code cache is full, no more methods can be JIT-compiled, and performance degrades.
   Monitor with `-XX:+PrintCodeCache` and tune with `-XX:ReservedCodeCacheSize`.
2. **Deoptimization storms**: Rapid deoptimization/recompilation cycles can cause performance oscillation. Diagnose with
   `-XX:+TraceDeoptimization`.
3. **OSR compilation edge cases**: On-stack replacement in deeply nested loops can produce suboptimal code compared to
   regular compilation.
4. **Native method boundaries**: JIT cannot optimize across JNI boundaries. Consider the Foreign Function & Memory API
   (Java 21) as an alternative.
5. **Uncommon traps**: If a branch that was assumed "never taken" is suddenly taken, the JVM deoptimizes and recompiles
   with updated profiling data.

## Interview-specific Insights

Interviewers often focus on:

- Understanding tiered compilation and the C1/C2 compiler distinction
- Explaining key optimizations: inlining, escape analysis, devirtualization
- Knowledge of when and why deoptimization occurs
- How to use JIT compiler flags for debugging and tuning
- Understanding why benchmarking requires warmup
- The relationship between code structure and JIT optimization

Common tricky questions:
- "Can you explain why a short method might run faster than an equivalent longer method?"
- "What is escape analysis and how can it eliminate heap allocations?"
- "Why might adding a new class to your application make unrelated code slower?"

## Interview Q&A Section

**Q1: Explain tiered compilation in the HotSpot JVM.**

```text
A1: Tiered compilation is the default compilation strategy in HotSpot JVM (since Java 8)
that combines two JIT compilers for optimal performance:

C1 (Client Compiler):
- Fast compilation, basic optimizations
- Generates reasonably optimized code quickly
- Collects profiling data for C2
- Three sub-levels: Level 1 (no profiling), Level 2 (basic), Level 3 (full profiling)

C2 (Server Compiler):
- Slow compilation, aggressive optimizations
- Uses profiling data from C1 to make speculative optimizations
- Produces highly optimized native code
- Level 4

The typical compilation path:
1. Method starts as interpreted (Level 0)
2. After enough invocations, C1 compiles it with profiling (Level 3)
3. After more invocations and sufficient profiling data, C2 recompiles (Level 4)

Benefits of tiered compilation:
- Fast startup (C1 compiles quickly for early warmup)
- High peak performance (C2 optimizes hot code aggressively)
- Better profiling data (C1 collects data while providing decent performance)
- Automatic adaptation (no need to choose -client vs -server)

You can control this with:
- -XX:+TieredCompilation (on by default)
- -XX:-TieredCompilation (disable, use only C2)
- -XX:TieredStopAtLevel=N (stop at level N)
```

```java
// Observing JIT compilation with flags:
// java -XX:+PrintCompilation TieredCompilationDemo

public class TieredCompilationDemo {
    public static int hotMethod(int x) {
        return x * x + 2 * x + 1;
    }

    public static void main(String[] args) {
        // Level 0: Interpreted for first few hundred calls
        for (int i = 0; i < 100; i++) {
            hotMethod(i);
        }

        // Level 3: C1 compiled with profiling after ~hundreds of calls
        for (int i = 0; i < 10_000; i++) {
            hotMethod(i);
        }

        // Level 4: C2 compiled with aggressive optimizations after ~10,000+ calls
        for (int i = 0; i < 100_000; i++) {
            hotMethod(i);
        }
    }
}
```

**Q2: What is method inlining and why is it the most important JIT optimization?**

```text
A2: Method inlining replaces a method call with the body of the called method at the
call site. It is considered the most important JIT optimization because:

1. Eliminates call overhead:
   - No need to create a new stack frame
   - No parameter passing overhead
   - No return value handling

2. Enables cascading optimizations:
   - Constant propagation: If arguments are constants, the inlined code can be
     simplified at compile time
   - Dead code elimination: Unreachable branches in the inlined code can be removed
   - Escape analysis: Objects that don't escape the inlined scope can be
     stack-allocated or eliminated
   - Loop optimizations: Inlined loop bodies can be unrolled or vectorized

3. Improves data locality:
   - Inlined code is physically closer in the code cache
   - Better CPU cache utilization

HotSpot inlining heuristics:
- MaxInlineSize (default 35 bytes): Methods smaller than this are always inlined
- FreqInlineSize (default 325 bytes): Frequently called methods up to this size
- MaxInlineLevel (default 15): Maximum depth of inlining
- Methods marked as 'final' or in 'final' classes are easier to inline

Why small methods matter:
- Getter/setter methods (1-5 bytes) are always inlined
- Small utility methods get inlined, making abstraction "free"
- Large methods block inlining of their callers
```

```java
// Inlining demonstration
public class InliningDemo {
    // These small methods will be inlined by the JIT
    public static int square(int x) {
        return x * x;
    }

    public static int doubleIt(int x) {
        return x * 2;
    }

    // After inlining, this becomes equivalent to: return x * x + x * 2 + 1
    public static int compute(int x) {
        return square(x) + doubleIt(x) + 1;
    }

    // Polymorphic methods may not be inlined (megamorphic call sites)
    interface Shape {
        int area();
    }

    static final class Circle implements Shape {
        int r;
        Circle(int r) { this.r = r; }
        @Override public int area() { return (int)(Math.PI * r * r); }
    }

    // With sealed classes, JIT knows all implementations -- helps inlining
    sealed interface SealedShape permits SealedCircle, SealedSquare {
        int area();
    }
    static final class SealedCircle implements SealedShape {
        int r;
        SealedCircle(int r) { this.r = r; }
        @Override public int area() { return (int)(Math.PI * r * r); }
    }
    static final class SealedSquare implements SealedShape {
        int s;
        SealedSquare(int s) { this.s = s; }
        @Override public int area() { return s * s; }
    }
}
```

**Q3: What is escape analysis and how does it optimize memory allocation?**

```text
A3: Escape analysis is a JIT compiler optimization that determines the dynamic scope
of object references -- specifically, whether an object can be accessed outside the
method or thread that created it.

Three escape states:
1. NoEscape: Object is only used within the creating method and never stored in a
   field or passed to another method.
2. ArgEscape: Object is passed as an argument to a method but doesn't escape beyond
   that call (e.g., passed to a method that doesn't store it).
3. GlobalEscape: Object is stored in a static field, returned from the method, or
   otherwise accessible globally.

Optimizations enabled:
1. Scalar Replacement (NoEscape):
   - Instead of allocating the object on the heap, its fields become local variables
   - No allocation, no garbage collection needed
   - Example: Point p = new Point(x, y); return p.x + p.y;
     Becomes: return x + y; (Point object eliminated entirely)

2. Stack Allocation (NoEscape):
   - Object is allocated on the thread's stack instead of the heap
   - Automatically deallocated when the method returns
   - No GC pressure at all
   - Note: HotSpot primarily uses scalar replacement rather than stack allocation

3. Lock Elision (NoEscape/ArgEscape):
   - If an object doesn't escape the thread, its synchronization can be removed
   - synchronized(localObj) becomes a no-op if localObj doesn't escape

4. Lock Coarsening (related optimization):
   - Multiple adjacent synchronized blocks on the same object are merged

Limitations:
- Only works for objects that don't escape
- Complex control flow can prevent escape analysis
- Large objects may not benefit (exceeds optimization thresholds)
- Requires method inlining first (analysis must see the full scope)
```

```java
// Escape analysis scenarios
public class EscapeAnalysisDemo {
    record Point(int x, int y) {}

    // NoEscape -- Point can be scalar-replaced (eliminated)
    public static int sumCoordinates(int x, int y) {
        Point p = new Point(x, y); // JIT may eliminate this allocation
        return p.x() + p.y();     // Becomes just: return x + y;
    }

    // GlobalEscape -- Point escapes via return; cannot be eliminated
    public static Point createPoint(int x, int y) {
        return new Point(x, y); // Must be heap-allocated
    }

    // NoEscape with lock elision
    public static int synchronizedNoEscape() {
        Object lock = new Object(); // Doesn't escape
        synchronized (lock) {        // Lock can be elided
            return 42;
        }
    }

    // ArgEscape -- depends on what the called method does
    public static void argEscape(int x, int y) {
        Point p = new Point(x, y);
        System.out.println(p); // p escapes to println
    }
}
```

**Q4: What is deoptimization and when does it occur?**

```text
A4: Deoptimization is the process of reverting from JIT-compiled native code back to
interpreted execution. It occurs when the assumptions made during JIT compilation
become invalid.

When deoptimization occurs:
1. Class loading invalidates assumptions:
   - JIT assumed a method was monomorphic (only one implementation)
   - A new class is loaded that provides another implementation
   - The compiled code's devirtualization is no longer valid

2. Uncommon traps are hit:
   - JIT compiled code based on profiling that showed a branch was never taken
   - When that branch is taken for the first time, the compiled code can't handle it
   - Example: null check that was never triggered during profiling

3. Type profile changes:
   - JIT optimized for specific types seen during profiling
   - A new type appears at runtime

4. Hot code replacement:
   - Debugger replaces code at runtime (JVMTI)

5. Speculative optimizations fail:
   - Bounds check elimination assumed array was always in bounds
   - An out-of-bounds access occurs

The deoptimization process:
1. JVM identifies the current point in compiled code
2. Reconstructs the interpreter state (stack frame, local variables)
3. Transfers execution to the interpreter
4. The method may be recompiled later with updated profiling data

Impact on performance:
- Deoptimization itself is expensive (state reconstruction)
- Repeated deopt/recompile cycles ("deoptimization storms") are very costly
- Usually temporary -- recompilation with better data resolves it
```

```java
// Demonstrating deoptimization trigger
public class DeoptimizationDemo {
    interface Processor {
        int process(int input);
    }

    static class SimpleProcessor implements Processor {
        @Override public int process(int input) { return input * 2; }
    }

    static class ComplexProcessor implements Processor {
        @Override public int process(int input) { return input * input + input; }
    }

    public static int useProcessor(Processor p, int input) {
        return p.process(input); // Call site for potential deopt
    }

    public static void main(String[] args) {
        Processor simple = new SimpleProcessor();

        // Phase 1: JIT profiles this as monomorphic (only SimpleProcessor)
        // After warmup, devirtualizes and inlines SimpleProcessor.process()
        for (int i = 0; i < 100_000; i++) {
            useProcessor(simple, i);
        }

        // Phase 2: Introducing ComplexProcessor invalidates the assumption
        // This triggers deoptimization of useProcessor()
        Processor complex = new ComplexProcessor();
        for (int i = 0; i < 100_000; i++) {
            // JIT must now handle bimorphic call site
            useProcessor(i % 2 == 0 ? simple : complex, i);
        }
    }
}
```

**Q5: How do you diagnose JIT compilation issues in production?**

```text
A5: Several tools and flags help diagnose JIT compilation behavior:

Diagnostic Flags:
1. -XX:+PrintCompilation
   - Shows each method as it's compiled
   - Format: timestamp compile_id flags tier method_name size
   - Flags: b=blocking, s=synchronized, !=has exception handler
   - % indicates OSR compilation

2. -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining
   - Shows inlining decisions and reasons for not inlining
   - "too big", "callee is too large", "no static binding"

3. -XX:+PrintCodeCache
   - Shows code cache utilization at shutdown
   - Helps identify if code cache is exhausted

4. -XX:+LogCompilation (produces XML log)
   - Most detailed output, used with JITWatch visualization tool
   - Generates hotspot_pidXXXX.log

5. JFR (Java Flight Recorder):
   - jdk.Compilation event: Method compilation details
   - jdk.CompilerInlining: Inlining decisions
   - jdk.Deoptimization: Deoptimization events
   - Low overhead, safe for production

Tools:
- JITWatch: Visualizes compilation logs and inlining decisions
- JMH: Microbenchmarking with proper warmup handling
- async-profiler: Shows actual compiled code and CPU hot spots
- perf + perf-map-agent: Maps JIT-compiled code to Linux perf events

Key metrics to watch:
- Compilation rate (compiles per second)
- Code cache utilization (%)
- Deoptimization count
- Time spent in compilation threads
```

```java
// JIT diagnostic demonstration
// Run with: java -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions
//           -XX:+PrintInlining JitDiagnostics

public class JitDiagnostics {
    // Small enough to be inlined (< 35 bytes)
    static int smallMethod(int x) {
        return x + 1;
    }

    // Likely too large to be frequently inlined
    static int largeMethod(int x) {
        int result = x;
        for (int i = 0; i < 10; i++) {
            result = result * x + i;
            if (result < 0) result = -result;
            result = result % 1000000;
        }
        return result;
    }

    public static void main(String[] args) {
        // Force compilation by calling many times
        int sum = 0;
        for (int i = 0; i < 100_000; i++) {
            sum += smallMethod(i);  // Will be inlined
            sum += largeMethod(i);  // May not be inlined
        }
        System.out.println("Sum: " + sum);

        // Check code cache stats
        System.out.println("Available processors: " +
            Runtime.getRuntime().availableProcessors());
    }
}
```

## Code Examples

- Test: [JitCompilationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/jvminternals/JitCompilationTest.java)
- Source: [JitCompilation.java](src/main/java/com/github/msorkhpar/claudejavatutor/jvminternals/JitCompilation.java)
