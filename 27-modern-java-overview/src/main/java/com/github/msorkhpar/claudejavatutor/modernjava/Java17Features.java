package com.github.msorkhpar.claudejavatutor.modernjava;

import java.security.SecureRandom;
import java.util.*;
import java.util.random.*;
import java.util.stream.*;

/**
 * Demonstrates key features introduced in Java 17 (LTS).
 * Covers: Enhanced Pseudo-Random Number Generators (JEP 356),
 * Deprecation of the Security Manager (JEP 411).
 */
public class Java17Features {

    // ========== Enhanced Pseudo-Random Number Generators ==========

    /**
     * Demonstrates the new RandomGenerator interface - a common interface for all PRNGs.
     */
    public static int generateRandomInt(RandomGenerator generator, int bound) {
        return generator.nextInt(bound);
    }

    /**
     * Demonstrates creating different random generator implementations.
     */
    public static RandomGenerator createGenerator(String algorithm) {
        return RandomGeneratorFactory.of(algorithm).create();
    }

    /**
     * Demonstrates creating a seeded random generator for reproducibility.
     */
    public static RandomGenerator createSeededGenerator(String algorithm, long seed) {
        return RandomGeneratorFactory.of(algorithm).create(seed);
    }

    /**
     * Lists all available random generator algorithms.
     */
    public static List<String> listAvailableAlgorithms() {
        return RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates generating a stream of random integers.
     */
    public static List<Integer> generateRandomInts(RandomGenerator generator, int count, int origin, int bound) {
        return generator.ints(count, origin, bound)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates generating random doubles in a range.
     */
    public static List<Double> generateRandomDoubles(RandomGenerator generator, int count,
                                                      double origin, double bound) {
        return generator.doubles(count, origin, bound)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates generating random longs.
     */
    public static List<Long> generateRandomLongs(RandomGenerator generator, int count,
                                                  long origin, long bound) {
        return generator.longs(count, origin, bound)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates generating Gaussian (normal) distributed values.
     */
    public static List<Double> generateGaussianValues(RandomGenerator generator, int count) {
        return DoubleStream.generate(generator::nextGaussian)
                .limit(count)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * Demonstrates the jumpable random generator for parallel streams.
     * Jumpable generators can advance their state by a large number of steps.
     */
    public static List<Integer> generateWithJumpable(long seed, int streamCount, int valuesPerStream) {
        RandomGeneratorFactory<RandomGenerator> factory = RandomGeneratorFactory.of("Xoroshiro128PlusPlus");
        RandomGenerator baseGenerator = factory.create(seed);

        List<Integer> allValues = new ArrayList<>();
        if (baseGenerator instanceof RandomGenerator.JumpableGenerator jumpable) {
            for (int i = 0; i < streamCount; i++) {
                List<Integer> streamValues = jumpable.ints(valuesPerStream, 0, 100)
                        .boxed()
                        .collect(Collectors.toList());
                allValues.addAll(streamValues);
                jumpable.jump();
            }
        }
        return allValues;
    }

    /**
     * Demonstrates RandomGeneratorFactory properties.
     */
    public static Map<String, Object> getGeneratorProperties(String algorithm) {
        RandomGeneratorFactory<?> factory = RandomGeneratorFactory.of(algorithm);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", factory.name());
        props.put("isJumpable", factory.isJumpable());
        props.put("isLeapable", factory.isLeapable());
        props.put("isSplittable", factory.isSplittable());
        props.put("isStreamable", factory.isStreamable());
        props.put("isStatistical", factory.isStatistical());
        props.put("isStochastic", factory.isStochastic());
        return props;
    }

    /**
     * Compares old Random vs new RandomGenerator API.
     */
    public static List<Integer> oldStyleRandom(long seed, int count) {
        Random random = new Random(seed);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(random.nextInt(100));
        }
        return result;
    }

    public static List<Integer> newStyleRandom(long seed, int count) {
        RandomGenerator generator = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
        return generator.ints(count, 0, 100)
                .boxed()
                .collect(Collectors.toList());
    }

    // ========== Deprecation of Security Manager ==========

    /**
     * Demonstrates checking Security Manager status.
     * The Security Manager has been deprecated for removal since Java 17.
     */
    @SuppressWarnings("removal")
    public static boolean isSecurityManagerPresent() {
        return System.getSecurityManager() != null;
    }

    /**
     * Explains the Security Manager deprecation and modern alternatives.
     */
    public static String explainSecurityManagerDeprecation() {
        return """
                Security Manager Deprecation (JEP 411):

                Why deprecated:
                - Rarely used in modern applications
                - Complex to configure correctly
                - Performance overhead
                - Not effective against modern attack vectors
                - Maintenance burden for JDK developers

                Modern alternatives:
                1. Container-level isolation (Docker, Kubernetes)
                2. OS-level security (SELinux, AppArmor)
                3. Module system (JPMS) for encapsulation
                4. Process-level sandboxing
                5. Java agents for monitoring
                6. Static analysis tools for security scanning
                """;
    }

    /**
     * Demonstrates the recommended approach: using module system for access control.
     */
    public static String demonstrateModuleSystemBenefits() {
        return """
                Module System (JPMS) as an alternative:
                - Strong encapsulation of internal APIs
                - Explicit dependencies between modules
                - Reliable configuration at compile and run time
                - No reflection-based attacks on unexported packages
                """;
    }

    // ========== Sealed Classes (finalized in Java 17) ==========
    // Note: Sealed classes were finalized in Java 17, previewed in 15/16

    /**
     * Demonstrates sealed class hierarchy.
     */
    public sealed interface PaymentMethod permits CreditCard, DebitCard, DigitalWallet {
    }

    public record CreditCard(String number, String expiry) implements PaymentMethod {
    }

    public record DebitCard(String number, String pin) implements PaymentMethod {
    }

    public record DigitalWallet(String provider, String email) implements PaymentMethod {
    }

    public static String describePaymentMethod(PaymentMethod method) {
        if (method instanceof CreditCard cc) {
            return "Credit Card ending in " + cc.number().substring(cc.number().length() - 4);
        } else if (method instanceof DebitCard dc) {
            return "Debit Card ending in " + dc.number().substring(dc.number().length() - 4);
        } else if (method instanceof DigitalWallet dw) {
            return "Digital Wallet: " + dw.provider() + " (" + dw.email() + ")";
        }
        throw new IllegalArgumentException("Unknown payment method");
    }
}
