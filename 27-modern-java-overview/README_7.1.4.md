# 7.1.4. Java 17 (LTS)

## Concept Explanation

Java 17, released in September 2021, is a Long-Term Support (LTS) release and one of the most significant Java releases for enterprise adoption. It finalized many features previewed in earlier releases (sealed classes, pattern matching for instanceof) and introduced the enhanced pseudo-random number generator (PRNG) API while deprecating the Security Manager for removal.

**Real-world analogy**: Java 17 is like a city's infrastructure upgrade — the experimental elevated train line (sealed classes, pattern matching) becomes an official permanent route, the outdated toll booths (Security Manager) are marked for demolition, and the random number dispatch center (PRNG API) gets a modern, pluggable architecture that can accommodate new algorithms without rebuilding the entire system.

The key features of Java 17 are:
1. **Enhanced Pseudo-Random Number Generators (JEP 356)** — a unified, pluggable API for random number generation
2. **Deprecation of the Security Manager (JEP 411)** — signaling the end of an era
3. **Sealed Classes (JEP 409, finalized)** — restricting class hierarchies
4. **Pattern Matching for instanceof (JEP 394, finalized)** — type-safe casting
5. **Strong encapsulation of JDK internals (JEP 403)** — `--illegal-access` no longer available

## Key Points to Remember

- Java 17 is an LTS release — enterprise support until at least 2029 (vendor-dependent).
- `RandomGenerator` is the new common interface for all random number generators.
- `RandomGeneratorFactory` provides a pluggable, algorithm-based factory for creating generators.
- Available algorithms include `L64X128MixRandom`, `Xoroshiro128PlusPlus`, `Xoshiro256PlusPlus`, and more.
- `JumpableGenerator` and `LeapableGenerator` interfaces allow advancing generator state for parallel streams.
- The old `java.util.Random` now implements `RandomGenerator` — backward compatible.
- `SecureRandom` also implements `RandomGenerator`.
- Security Manager is deprecated for removal; modern alternatives include containers, OS-level security, and JPMS.
- Sealed classes restrict which classes can extend or implement a type — enabling exhaustive switch in later versions.
- `--illegal-access=permit` was removed in Java 17; reflective access to JDK internals requires explicit `--add-opens`.

## Relevant Java 21 Features

- **RandomGenerator** is the foundation for random number usage in virtual thread contexts (Java 21).
- **Sealed classes** are essential for exhaustive pattern matching in Java 21 switch expressions — the compiler verifies all subtypes are covered.
- **Pattern matching for instanceof** is used ubiquitously in Java 21 code, especially combined with record patterns and switch.
- Security Manager removal continues — Java 21 further restricts its use. Container-based isolation is the standard.
- Strong encapsulation is tightened further; Java 21 applications must use `--add-opens` for any reflective access to internal APIs.

## Common Pitfalls and How to Avoid Them

1. **Using `java.util.Random` in multi-threaded code without thread safety**:
   ```java
   // WRONG: sharing a single Random across threads causes contention
   static final Random shared = new Random();
   // Multiple threads calling shared.nextInt() — poor performance

   // RIGHT: use ThreadLocalRandom or a splittable generator
   int value = ThreadLocalRandom.current().nextInt(100);

   // Or use the new API with a thread-safe algorithm:
   RandomGenerator gen = RandomGeneratorFactory.of("L64X128MixRandom").create();
   ```

2. **Hardcoding algorithm names without checking availability**:
   ```java
   // WRONG: algorithm might not exist on all JVMs
   RandomGenerator gen = RandomGeneratorFactory.of("NonExistentAlgorithm").create();

   // RIGHT: check availability first
   boolean available = RandomGeneratorFactory.all()
       .anyMatch(f -> f.name().equals("L64X128MixRandom"));
   ```

3. **Using Security Manager in new code**:
   ```java
   // WRONG: Security Manager is deprecated for removal
   @SuppressWarnings("removal")
   System.setSecurityManager(new SecurityManager());

   // RIGHT: use container-level or OS-level security
   // Docker, Kubernetes, SELinux, AppArmor, or JPMS modules
   ```

4. **Not making sealed hierarchies exhaustive**:
   ```java
   // WRONG: sealed class with missing permits clause
   // public sealed class Shape { } // compile error — must specify permits

   // RIGHT: explicitly declare permitted subtypes
   public sealed class Shape permits Circle, Rectangle, Triangle { }
   public final class Circle extends Shape { }
   public final class Rectangle extends Shape { }
   public non-sealed class Triangle extends Shape { }
   ```

5. **Confusing seeded and unseeded generators**:
   ```java
   // Seeded generators are reproducible (same seed → same sequence)
   RandomGenerator seeded1 = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
   RandomGenerator seeded2 = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
   // seeded1.nextInt() == seeded2.nextInt() — true!

   // Unseeded generators use entropy — not reproducible
   RandomGenerator unseeded = RandomGeneratorFactory.of("L64X128MixRandom").create();
   ```

## Best Practices and Optimization Techniques

- **Choose the right PRNG algorithm**: `L64X128MixRandom` for general use, `Xoroshiro128PlusPlus` for speed, `SecureRandom` for cryptographic needs.
- **Use `JumpableGenerator`** for parallel streams to ensure statistically independent subsequences.
- **Prefer `RandomGenerator` as the parameter type** in APIs — it accepts `Random`, `SecureRandom`, `ThreadLocalRandom`, and all new generators.
- **Use seeded generators in tests** for reproducibility; unseeded generators in production for unpredictability.
- **Replace `java.util.Random` with the new API** in new code for better algorithm selection and performance.
- **Adopt sealed classes** for domain models where the type hierarchy is closed (e.g., AST nodes, state machines, result types).
- **Plan migration away from Security Manager** — audit any code that checks or installs a SecurityManager.

## Edge Cases and Their Handling

- **Empty algorithm name**: `RandomGeneratorFactory.of("")` throws `IllegalArgumentException`.
- **Seeded generators with seed 0**: Works fine — 0 is a valid seed for most algorithms.
- **Sealed class with no subtypes**: Legal but unusual — `sealed class Empty permits {}` is a compile error; you need at least one permitted subtype.
- **Sealed interface with records**: Records can be permitted subtypes of sealed interfaces — commonly used for algebraic data types.
- **RandomGenerator.ints(0, 0, 100)**: Returns an empty IntStream (count = 0) — no exception.
- **Negative count in stream methods**: `generator.ints(-1, 0, 100)` throws `IllegalArgumentException`.

## Interview-specific Insights

Key interview topics around Java 17:
- **"Why was the Security Manager deprecated?"** — Shows understanding of modern security architecture (containers, JPMS, OS-level isolation).
- **"Compare the old Random API with the new RandomGenerator API."** — Tests knowledge of algorithm selection, thread safety, and reproducibility.
- **"What are sealed classes and why do they matter?"** — Links to exhaustive pattern matching, algebraic data types, and domain modeling.
- **"What's the significance of Java 17 being an LTS release?"** — Understanding enterprise adoption patterns, migration strategies, and support timelines.
- **"How do you handle the removal of --illegal-access?"** — Tests migration knowledge for libraries using reflection on JDK internals.

## Interview Q&A Section

### Q1: Explain the new RandomGenerator API in Java 17 and its advantages.

```text
Java 17 introduced a unified random number generation API (JEP 356) centered around the
RandomGenerator interface. Key advantages:

1. Unified interface: RandomGenerator is implemented by Random, SecureRandom,
   ThreadLocalRandom, and all new generators — polymorphic code.
2. Algorithm selection: RandomGeneratorFactory.of("algorithmName") lets you choose
   the best algorithm for your use case.
3. Jumpable/Leapable generators: JumpableGenerator and LeapableGenerator interfaces
   allow advancing state by large steps — essential for parallel stream correctness.
4. Splittable generators: SplittableGenerator creates independent sub-generators
   for fork/join parallelism.
5. Better statistical properties: New algorithms like L64X128MixRandom and
   Xoroshiro128PlusPlus have better statistical quality than java.util.Random.
6. Discoverable: RandomGeneratorFactory.all() lists all available algorithms.
```

```java
// List all available algorithms
RandomGeneratorFactory.all()
    .map(f -> f.name() + " (jumpable=" + f.isJumpable() + ")")
    .sorted()
    .forEach(System.out::println);

// Create a specific generator
RandomGenerator gen = RandomGeneratorFactory.of("L64X128MixRandom").create();

// Generate a stream of values
List<Integer> values = gen.ints(10, 1, 101)
    .boxed()
    .toList();

// Use jumpable generator for parallel work
RandomGenerator.JumpableGenerator jumpable =
    (RandomGenerator.JumpableGenerator) RandomGeneratorFactory
        .of("Xoroshiro128PlusPlus").create(42L);

for (int i = 0; i < 4; i++) {
    List<Integer> chunk = jumpable.ints(5, 0, 100).boxed().toList();
    jumpable.jump(); // Advance to a statistically independent subsequence
}
```

### Q2: Why was the Security Manager deprecated in Java 17?

```text
The Security Manager was deprecated for removal (JEP 411) for several reasons:

1. Rarely used: Very few modern applications use SecurityManager. Most cloud/server
   apps never install one.
2. Complex and error-prone: Writing correct security policies requires deep expertise.
   Misconfigured policies either block legitimate operations or fail to prevent attacks.
3. Performance overhead: Every privileged operation checks the SecurityManager,
   adding overhead even when the policy allows everything.
4. Not effective against modern threats: It can't prevent container escapes,
   network-level attacks, or supply-chain compromises.
5. Maintenance burden: Supporting SecurityManager complicates JDK development and
   constrains optimization opportunities.
6. Better alternatives exist: Container isolation (Docker), OS security (SELinux),
   JPMS modules, and process sandboxing are more effective.

Timeline: Deprecated in Java 17, further restricted in later versions, targeted for
removal in a future Java release.
```

```java
// Old approach (deprecated):
@SuppressWarnings("removal")
public void oldSecurityCheck() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        sm.checkRead("/etc/passwd"); // Throws SecurityException if denied
    }
}

// Modern approach: rely on OS/container security + JPMS
// module-info.java
// module myapp {
//     requires java.net.http;
//     exports com.example.api;
//     // Internal packages are NOT exported — strong encapsulation
// }

// Additional defense: validate inputs at system boundaries
public String readFile(Path path) {
    // Validate the path is within allowed directory
    Path normalized = path.normalize();
    if (!normalized.startsWith(ALLOWED_DIR)) {
        throw new SecurityException("Access denied: " + path);
    }
    return Files.readString(normalized);
}
```

### Q3: What are sealed classes and how do they enable exhaustive pattern matching?

```text
Sealed classes (JEP 409) restrict which classes can extend or implement a type.
Declared with the "sealed" keyword and a "permits" clause listing all allowed subtypes.

Subtype modifiers:
- "final" — cannot be further extended
- "sealed" — can be extended by another restricted set
- "non-sealed" — reopens the hierarchy for unrestricted extension

Why they matter for pattern matching:
- The compiler knows ALL possible subtypes at compile time.
- In a switch expression over a sealed type, the compiler can verify exhaustiveness.
- No default/else branch needed — if you cover all permitted subtypes, the switch is complete.
- Adding a new subtype causes compile errors everywhere the switch is used — forces updates.

This creates algebraic data types in Java, similar to Rust enums or Scala sealed traits.
```

```java
// Define a sealed hierarchy
public sealed interface Shape permits Circle, Rectangle, Triangle {}
public record Circle(double radius) implements Shape {}
public record Rectangle(double w, double h) implements Shape {}
public record Triangle(double base, double height) implements Shape {}

// Exhaustive switch — no default needed (Java 21)
public double area(Shape shape) {
    return switch (shape) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.w() * r.h();
        case Triangle t -> 0.5 * t.base() * t.height();
        // No default! Compiler knows these are all the cases.
    };
}

// If you add a new permitted subtype:
// public record Pentagon(double side) implements Shape {}
// The switch above will FAIL to compile — forces you to handle the new case.
```

### Q4: How does Java 17 handle strong encapsulation of JDK internals?

```text
Java 17 (JEP 403) strongly encapsulates JDK internal APIs by default:

History:
- Java 9: introduced JPMS with --illegal-access=permit (default, warns on access)
- Java 16: changed default to --illegal-access=deny
- Java 17: removed --illegal-access entirely

Impact:
- Reflection on JDK internal classes (sun.misc.*, com.sun.*, jdk.internal.*) is
  denied by default.
- Libraries using Unsafe, internal sun.* classes, or reflective access to JDK
  internals must use --add-opens on the command line.
- This affects many frameworks: Spring (older versions), Hibernate, serialization
  libraries, testing frameworks.

Migration strategy:
1. Identify illegal access: run with --illegal-access=warn on Java 16
2. Replace internal APIs with public alternatives (e.g., VarHandle instead of Unsafe)
3. If no public alternative exists, use --add-opens in module-info.java or command line
4. Update frameworks to versions that support Java 17+
```

```java
// Before Java 17: accessing internal API (worked with warnings)
// sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe(); // IllegalAccessError in 17

// After Java 17: use public alternatives
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles;

public class SafeAccess {
    private volatile int counter;

    private static final VarHandle COUNTER;
    static {
        try {
            COUNTER = MethodHandles.lookup()
                .findVarHandle(SafeAccess.class, "counter", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void increment() {
        COUNTER.getAndAdd(this, 1); // Safe alternative to Unsafe.getAndAddInt
    }
}

// If you MUST use internal APIs, add JVM flags:
// java --add-opens java.base/sun.misc=ALL-UNNAMED -jar app.jar
```

### Q5: Compare ThreadLocalRandom with the new RandomGenerator API for concurrent applications.

```text
ThreadLocalRandom (Java 7):
- Thread-confined: each thread has its own instance via ThreadLocalRandom.current()
- No contention: no synchronization needed
- Cannot be seeded (not reproducible)
- Limited to one algorithm (similar to java.util.Random internals)
- Cannot create independent subsequences for parallel streams

New RandomGenerator API (Java 17):
- Algorithm-agnostic: choose from many algorithms
- JumpableGenerator: advance state by 2^64 or 2^128 steps for independent subsequences
- SplittableGenerator: fork into independent generators (used by parallel streams)
- Can be seeded for reproducibility in tests
- Composable: pass RandomGenerator to APIs for dependency injection

Recommendation:
- Simple thread-local random: ThreadLocalRandom is still fine (and now implements RandomGenerator)
- Parallel streams: use SplittableGenerator (e.g., SplittableRandom or L64X128MixRandom)
- Reproducible tests: use seeded RandomGeneratorFactory.of(...).create(seed)
- Cryptographic needs: SecureRandom (also implements RandomGenerator)
```

```java
// ThreadLocalRandom (simple, fast, not reproducible)
int value = ThreadLocalRandom.current().nextInt(1, 100);

// New API: seeded and reproducible
RandomGenerator reproducible = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
int reproducibleValue = reproducible.nextInt(1, 100); // Always the same for seed 42

// New API: parallel-safe with jumpable generator
RandomGenerator.JumpableGenerator jumpGen =
    (RandomGenerator.JumpableGenerator)
        RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(123L);

// Create independent generators for parallel tasks
List<RandomGenerator> perThreadGens = new ArrayList<>();
for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
    perThreadGens.add(jumpGen.copy()); // Copy current state
    jumpGen.jump();                     // Advance to independent subsequence
}

// Polymorphic API: accepts any RandomGenerator
public List<Integer> sample(RandomGenerator rng, int count, int bound) {
    return rng.ints(count, 0, bound).boxed().toList();
}
```

## Code Examples

- Implementation: [Java17Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java17Features.java)
- Tests: [Java17FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java17FeaturesTest.java)
