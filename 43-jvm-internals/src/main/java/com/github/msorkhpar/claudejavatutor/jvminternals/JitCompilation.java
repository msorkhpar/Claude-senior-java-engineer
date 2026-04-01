package com.github.msorkhpar.claudejavatutor.jvminternals;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntUnaryOperator;

/**
 * Demonstrates JIT (Just-In-Time) compilation concepts including
 * tiered compilation, method inlining, escape analysis, and deoptimization.
 */
public class JitCompilation {

    /**
     * Demonstrates method inlining concepts.
     * Small methods are good candidates for inlining by the JIT compiler.
     */
    public static class InliningDemo {

        /**
         * Tiny method -- always inlined (< 35 bytes of bytecode).
         */
        public static int square(int x) {
            return x * x;
        }

        /**
         * Another tiny method -- will be inlined.
         */
        public static int doubleValue(int x) {
            return x * 2;
        }

        /**
         * Method that calls other small methods.
         * After inlining, the JIT compiles this as: x*x + x*2 + 1
         */
        public static int computeWithInlining(int x) {
            return square(x) + doubleValue(x) + 1;
        }

        /**
         * Demonstrates that breaking code into small methods is "free"
         * due to inlining -- clean code has no performance penalty.
         */
        public static int cleanComputation(int x) {
            int squared = square(x);
            int doubled = doubleValue(x);
            int result = squared + doubled;
            return result + 1;
        }

        /**
         * Equivalent monolithic method for comparison.
         */
        public static int monolithicComputation(int x) {
            return x * x + x * 2 + 1;
        }

        /**
         * Demonstrates deep inlining chain.
         */
        public static int add(int a, int b) {
            return a + b;
        }

        public static int addThree(int a, int b, int c) {
            return add(add(a, b), c);
        }

        public static int addFour(int a, int b, int c, int d) {
            return add(addThree(a, b, c), d);
        }
    }

    /**
     * Demonstrates escape analysis concepts.
     * Objects that don't escape a method can be stack-allocated or eliminated.
     */
    public static class EscapeAnalysisDemo {

        public record Point(int x, int y) {
        }

        /**
         * NoEscape: Point can be scalar-replaced.
         * The JIT eliminates the object allocation entirely.
         */
        public static int sumCoordinates(int x, int y) {
            Point p = new Point(x, y);
            return p.x() + p.y();
        }

        /**
         * GlobalEscape: Point escapes via return.
         * Must be heap-allocated.
         */
        public static Point createPoint(int x, int y) {
            return new Point(x, y);
        }

        /**
         * NoEscape with iteration: The JIT can still optimize
         * when objects don't escape even within loops.
         */
        public static int sumOfDistances(int[] xs, int[] ys) {
            int total = 0;
            for (int i = 0; i < xs.length && i < ys.length; i++) {
                Point p = new Point(xs[i], ys[i]);
                total += Math.abs(p.x()) + Math.abs(p.y());
            }
            return total;
        }

        /**
         * Demonstrates lock elision when object doesn't escape.
         * synchronized on a local object that doesn't escape is a no-op after JIT.
         */
        public static int synchronizedNoEscape(int value) {
            Object lock = new Object();
            synchronized (lock) {
                return value * 2;
            }
        }

        /**
         * Demonstrates that escape analysis requires method inlining first.
         * The JIT must inline helper methods to see that objects don't escape.
         */
        public static int computeDistance(int x1, int y1, int x2, int y2) {
            Point p1 = new Point(x1, y1);
            Point p2 = new Point(x2, y2);
            return manhattanDistance(p1, p2);
        }

        private static int manhattanDistance(Point a, Point b) {
            return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
        }
    }

    /**
     * Demonstrates devirtualization -- how the JIT optimizes virtual method calls.
     */
    public static class DevirtualizationDemo {

        public interface Shape {
            int area();

            String name();
        }

        public static class Circle implements Shape {
            private final int radius;

            public Circle(int radius) {
                this.radius = radius;
            }

            @Override
            public int area() {
                return (int) (Math.PI * radius * radius);
            }

            @Override
            public String name() {
                return "Circle";
            }
        }

        public static class Square implements Shape {
            private final int side;

            public Square(int side) {
                this.side = side;
            }

            @Override
            public int area() {
                return side * side;
            }

            @Override
            public String name() {
                return "Square";
            }
        }

        public static class Triangle implements Shape {
            private final int base;
            private final int height;

            public Triangle(int base, int height) {
                this.base = base;
                this.height = height;
            }

            @Override
            public int area() {
                return (base * height) / 2;
            }

            @Override
            public String name() {
                return "Triangle";
            }
        }

        /**
         * Monomorphic call site: only one Shape type.
         * JIT can devirtualize and inline.
         */
        public static int monomorphicCall(Shape shape) {
            return shape.area();
        }

        /**
         * Computes total area from a list of shapes.
         * If all shapes are the same type, the JIT can devirtualize.
         */
        public static int totalArea(List<Shape> shapes) {
            int total = 0;
            for (Shape shape : shapes) {
                total += shape.area();
            }
            return total;
        }

        /**
         * Creates a list of shapes of the given type for testing.
         */
        public static List<Shape> createMonomorphicShapes(int count) {
            List<Shape> shapes = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                shapes.add(new Circle(i + 1));
            }
            return shapes;
        }

        /**
         * Creates a list of shapes with multiple types (megamorphic).
         */
        public static List<Shape> createMegamorphicShapes(int count) {
            List<Shape> shapes = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                shapes.add(switch (i % 3) {
                    case 0 -> new Circle(i + 1);
                    case 1 -> new Square(i + 1);
                    default -> new Triangle(i + 1, i + 2);
                });
            }
            return shapes;
        }
    }

    /**
     * Demonstrates warmup effects and compilation thresholds.
     */
    public static class WarmupDemo {

        /**
         * Measures execution time of a function across multiple iterations,
         * demonstrating the warmup effect.
         */
        public static List<Long> measureWithWarmup(IntUnaryOperator function, int iterations, int samplePoints) {
            List<Long> timings = new ArrayList<>();
            int interval = Math.max(1, iterations / samplePoints);

            for (int i = 0; i < iterations; i++) {
                if (i % interval == 0) {
                    long start = System.nanoTime();
                    for (int j = 0; j < 1000; j++) {
                        function.applyAsInt(j);
                    }
                    long elapsed = System.nanoTime() - start;
                    timings.add(elapsed);
                } else {
                    function.applyAsInt(i);
                }
            }
            return timings;
        }

        /**
         * Demonstrates that the first execution is typically slower than subsequent ones.
         */
        public static record WarmupResult(long coldNanos, long warmNanos) {
            public double speedupRatio() {
                return warmNanos > 0 ? (double) coldNanos / warmNanos : 0;
            }
        }

        /**
         * Runs a computation in cold (no warmup) and warm (after warmup) modes.
         */
        public static WarmupResult measureWarmupEffect(IntUnaryOperator function, int warmupIterations) {
            // Cold run
            long coldStart = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                function.applyAsInt(i);
            }
            long coldNanos = System.nanoTime() - coldStart;

            // Warmup
            for (int i = 0; i < warmupIterations; i++) {
                function.applyAsInt(i);
            }

            // Warm run
            long warmStart = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                function.applyAsInt(i);
            }
            long warmNanos = System.nanoTime() - warmStart;

            return new WarmupResult(coldNanos, warmNanos);
        }
    }

    /**
     * Demonstrates loop optimization concepts (unrolling, invariant code motion).
     */
    public static class LoopOptimizationDemo {

        /**
         * Simple loop sum -- candidate for loop unrolling.
         */
        public static long simpleSum(int n) {
            long sum = 0;
            for (int i = 1; i <= n; i++) {
                sum += i;
            }
            return sum;
        }

        /**
         * Loop with invariant code that the JIT can hoist out of the loop.
         */
        public static int loopInvariantMotion(int[] array, int multiplier) {
            int result = 0;
            for (int i = 0; i < array.length; i++) {
                // multiplier * 2 is loop-invariant -- JIT moves it out
                result += array[i] * (multiplier * 2);
            }
            return result;
        }

        /**
         * Loop with range check that the JIT can eliminate.
         */
        public static int sumArray(int[] array) {
            int sum = 0;
            for (int i = 0; i < array.length; i++) {
                sum += array[i]; // Range check can be eliminated by JIT
            }
            return sum;
        }

        /**
         * Demonstrates that dead code within loops can be eliminated by JIT.
         */
        public static int loopWithDeadCode(int n) {
            int result = 0;
            for (int i = 0; i < n; i++) {
                result += i;
                int unused = i * i; // Dead code -- JIT eliminates this
            }
            return result;
        }
    }

    /**
     * Demonstrates intrinsic methods that the JIT replaces with
     * hand-optimized native code.
     */
    public static class IntrinsicsDemo {

        /**
         * Math.max is a JIT intrinsic -- replaced with optimized native code.
         */
        public static int maxValue(int a, int b) {
            return Math.max(a, b);
        }

        /**
         * System.arraycopy is a JIT intrinsic -- uses optimized memory copy.
         */
        public static int[] copyArray(int[] source) {
            int[] dest = new int[source.length];
            System.arraycopy(source, 0, dest, 0, source.length);
            return dest;
        }

        /**
         * String.equals is a JIT intrinsic -- uses optimized comparison.
         */
        public static boolean stringEquals(String a, String b) {
            return a.equals(b);
        }

        /**
         * Integer.numberOfLeadingZeros is a JIT intrinsic on many architectures.
         */
        public static int leadingZeros(int value) {
            return Integer.numberOfLeadingZeros(value);
        }

        /**
         * Arrays.copyOf uses System.arraycopy internally (intrinsic).
         */
        public static int[] growArray(int[] source, int newLength) {
            return Arrays.copyOf(source, newLength);
        }
    }
}
