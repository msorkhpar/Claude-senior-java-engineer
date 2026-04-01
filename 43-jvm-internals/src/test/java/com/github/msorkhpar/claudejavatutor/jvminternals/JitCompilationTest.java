package com.github.msorkhpar.claudejavatutor.jvminternals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JIT Compilation Tests")
class JitCompilationTest {

    @Nested
    @DisplayName("Inlining Demo")
    class InliningDemoTest {

        @Test
        @DisplayName("Should compute square correctly")
        void testSquare() {
            assertThat(JitCompilation.InliningDemo.square(5)).isEqualTo(25);
            assertThat(JitCompilation.InliningDemo.square(0)).isEqualTo(0);
            assertThat(JitCompilation.InliningDemo.square(-3)).isEqualTo(9);
        }

        @Test
        @DisplayName("Should compute double value correctly")
        void testDoubleValue() {
            assertThat(JitCompilation.InliningDemo.doubleValue(5)).isEqualTo(10);
            assertThat(JitCompilation.InliningDemo.doubleValue(0)).isEqualTo(0);
            assertThat(JitCompilation.InliningDemo.doubleValue(-3)).isEqualTo(-6);
        }

        @Test
        @DisplayName("Should compute with inlining same result as monolithic")
        void testInliningEquivalence() {
            for (int x = -10; x <= 10; x++) {
                int inlined = JitCompilation.InliningDemo.computeWithInlining(x);
                int monolithic = JitCompilation.InliningDemo.monolithicComputation(x);
                assertThat(inlined).as("x=%d", x).isEqualTo(monolithic);
            }
        }

        @Test
        @DisplayName("Should compute clean computation same as monolithic")
        void testCleanComputationEquivalence() {
            for (int x = -10; x <= 10; x++) {
                int clean = JitCompilation.InliningDemo.cleanComputation(x);
                int monolithic = JitCompilation.InliningDemo.monolithicComputation(x);
                assertThat(clean).as("x=%d", x).isEqualTo(monolithic);
            }
        }

        @Test
        @DisplayName("Should add two numbers correctly")
        void testAdd() {
            assertThat(JitCompilation.InliningDemo.add(3, 5)).isEqualTo(8);
        }

        @Test
        @DisplayName("Should add three numbers correctly via deep inlining")
        void testAddThree() {
            assertThat(JitCompilation.InliningDemo.addThree(1, 2, 3)).isEqualTo(6);
        }

        @Test
        @DisplayName("Should add four numbers correctly via deeper inlining")
        void testAddFour() {
            assertThat(JitCompilation.InliningDemo.addFour(1, 2, 3, 4)).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle edge case: Integer.MAX_VALUE in square")
        void testSquareOverflow() {
            // Overflow is expected behavior for int arithmetic
            int result = JitCompilation.InliningDemo.square(Integer.MAX_VALUE);
            assertThat(result).isEqualTo(Integer.MAX_VALUE * Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Escape Analysis Demo")
    class EscapeAnalysisDemoTest {

        @Test
        @DisplayName("Should sum coordinates correctly (NoEscape scenario)")
        void testSumCoordinates() {
            assertThat(JitCompilation.EscapeAnalysisDemo.sumCoordinates(3, 4)).isEqualTo(7);
            assertThat(JitCompilation.EscapeAnalysisDemo.sumCoordinates(0, 0)).isEqualTo(0);
            assertThat(JitCompilation.EscapeAnalysisDemo.sumCoordinates(-3, 5)).isEqualTo(2);
        }

        @Test
        @DisplayName("Should create point correctly (GlobalEscape scenario)")
        void testCreatePoint() {
            var point = JitCompilation.EscapeAnalysisDemo.createPoint(3, 4);
            assertThat(point).isNotNull();
            assertThat(point.x()).isEqualTo(3);
            assertThat(point.y()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should sum distances correctly")
        void testSumOfDistances() {
            int[] xs = {1, -2, 3};
            int[] ys = {-1, 2, -3};
            // |1|+|-1| + |-2|+|2| + |3|+|-3| = 2 + 4 + 6 = 12
            assertThat(JitCompilation.EscapeAnalysisDemo.sumOfDistances(xs, ys)).isEqualTo(12);
        }

        @Test
        @DisplayName("Should handle empty arrays for sum of distances")
        void testSumOfDistancesEmpty() {
            assertThat(JitCompilation.EscapeAnalysisDemo.sumOfDistances(new int[0], new int[0])).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle unequal array lengths")
        void testSumOfDistancesUnequalArrays() {
            int[] xs = {1, 2, 3, 4, 5};
            int[] ys = {1, 2};
            // Only processes min(5, 2) = 2 elements
            assertThat(JitCompilation.EscapeAnalysisDemo.sumOfDistances(xs, ys)).isEqualTo(6); // 1+1 + 2+2
        }

        @Test
        @DisplayName("Should compute synchronized NoEscape correctly")
        void testSynchronizedNoEscape() {
            assertThat(JitCompilation.EscapeAnalysisDemo.synchronizedNoEscape(21)).isEqualTo(42);
            assertThat(JitCompilation.EscapeAnalysisDemo.synchronizedNoEscape(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should compute manhattan distance correctly")
        void testComputeDistance() {
            assertThat(JitCompilation.EscapeAnalysisDemo.computeDistance(1, 1, 4, 5)).isEqualTo(7);
            assertThat(JitCompilation.EscapeAnalysisDemo.computeDistance(0, 0, 0, 0)).isEqualTo(0);
            assertThat(JitCompilation.EscapeAnalysisDemo.computeDistance(-1, -1, 1, 1)).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Devirtualization Demo")
    class DevirtualizationDemoTest {

        @Test
        @DisplayName("Should compute circle area correctly")
        void testCircleArea() {
            var circle = new JitCompilation.DevirtualizationDemo.Circle(5);
            assertThat(circle.area()).isEqualTo((int) (Math.PI * 25));
            assertThat(circle.name()).isEqualTo("Circle");
        }

        @Test
        @DisplayName("Should compute square area correctly")
        void testSquareArea() {
            var square = new JitCompilation.DevirtualizationDemo.Square(4);
            assertThat(square.area()).isEqualTo(16);
            assertThat(square.name()).isEqualTo("Square");
        }

        @Test
        @DisplayName("Should compute triangle area correctly")
        void testTriangleArea() {
            var triangle = new JitCompilation.DevirtualizationDemo.Triangle(6, 4);
            assertThat(triangle.area()).isEqualTo(12);
            assertThat(triangle.name()).isEqualTo("Triangle");
        }

        @Test
        @DisplayName("Should compute monomorphic call correctly")
        void testMonomorphicCall() {
            JitCompilation.DevirtualizationDemo.Shape shape =
                    new JitCompilation.DevirtualizationDemo.Circle(3);
            assertThat(JitCompilation.DevirtualizationDemo.monomorphicCall(shape))
                    .isEqualTo((int) (Math.PI * 9));
        }

        @Test
        @DisplayName("Should compute total area for monomorphic shapes")
        void testTotalAreaMonomorphic() {
            List<JitCompilation.DevirtualizationDemo.Shape> shapes =
                    JitCompilation.DevirtualizationDemo.createMonomorphicShapes(3);
            int total = JitCompilation.DevirtualizationDemo.totalArea(shapes);
            // Circles with radius 1, 2, 3: PI*1 + PI*4 + PI*9 = PI*14
            assertThat(total).isEqualTo(
                    (int) (Math.PI * 1) + (int) (Math.PI * 4) + (int) (Math.PI * 9)
            );
        }

        @Test
        @DisplayName("Should compute total area for megamorphic shapes")
        void testTotalAreaMegamorphic() {
            List<JitCompilation.DevirtualizationDemo.Shape> shapes =
                    JitCompilation.DevirtualizationDemo.createMegamorphicShapes(3);
            int total = JitCompilation.DevirtualizationDemo.totalArea(shapes);
            assertThat(total).isPositive();
        }

        @Test
        @DisplayName("Should create correct number of monomorphic shapes")
        void testCreateMonomorphicShapes() {
            List<JitCompilation.DevirtualizationDemo.Shape> shapes =
                    JitCompilation.DevirtualizationDemo.createMonomorphicShapes(5);
            assertThat(shapes).hasSize(5);
            assertThat(shapes).allMatch(s -> s instanceof JitCompilation.DevirtualizationDemo.Circle);
        }

        @Test
        @DisplayName("Should create mixed types for megamorphic shapes")
        void testCreateMegamorphicShapes() {
            List<JitCompilation.DevirtualizationDemo.Shape> shapes =
                    JitCompilation.DevirtualizationDemo.createMegamorphicShapes(6);
            assertThat(shapes).hasSize(6);
            long circleCount = shapes.stream()
                    .filter(s -> s instanceof JitCompilation.DevirtualizationDemo.Circle).count();
            long squareCount = shapes.stream()
                    .filter(s -> s instanceof JitCompilation.DevirtualizationDemo.Square).count();
            long triangleCount = shapes.stream()
                    .filter(s -> s instanceof JitCompilation.DevirtualizationDemo.Triangle).count();
            assertThat(circleCount).isEqualTo(2);
            assertThat(squareCount).isEqualTo(2);
            assertThat(triangleCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle empty shape list")
        void testTotalAreaEmpty() {
            assertThat(JitCompilation.DevirtualizationDemo.totalArea(List.of())).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Warmup Demo")
    class WarmupDemoTest {

        @Test
        @DisplayName("Should collect timing samples during warmup measurement")
        void testMeasureWithWarmup() {
            List<Long> timings = JitCompilation.WarmupDemo.measureWithWarmup(
                    x -> x * x + 1, 10_000, 5);
            assertThat(timings).hasSizeGreaterThanOrEqualTo(5);
            assertThat(timings).allMatch(t -> t >= 0);
        }

        @Test
        @DisplayName("Should measure warmup effect with positive durations")
        void testWarmupEffect() {
            var result = JitCompilation.WarmupDemo.measureWarmupEffect(
                    x -> x * x + 2 * x + 1, 50_000);
            assertThat(result.coldNanos()).isPositive();
            assertThat(result.warmNanos()).isPositive();
        }

        @Test
        @DisplayName("Should calculate speedup ratio")
        void testSpeedupRatio() {
            var result = JitCompilation.WarmupDemo.measureWarmupEffect(
                    x -> x * x, 50_000);
            assertThat(result.speedupRatio()).isPositive();
        }

        @Test
        @DisplayName("Should handle zero warmup iterations")
        void testZeroWarmup() {
            var result = JitCompilation.WarmupDemo.measureWarmupEffect(x -> x, 0);
            assertThat(result.coldNanos()).isPositive();
            assertThat(result.warmNanos()).isPositive();
        }
    }

    @Nested
    @DisplayName("Loop Optimization Demo")
    class LoopOptimizationDemoTest {

        @Test
        @DisplayName("Should compute simple sum correctly")
        void testSimpleSum() {
            assertThat(JitCompilation.LoopOptimizationDemo.simpleSum(10)).isEqualTo(55);
            assertThat(JitCompilation.LoopOptimizationDemo.simpleSum(100)).isEqualTo(5050);
            assertThat(JitCompilation.LoopOptimizationDemo.simpleSum(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should compute simple sum for n=1")
        void testSimpleSumOne() {
            assertThat(JitCompilation.LoopOptimizationDemo.simpleSum(1)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should apply loop invariant motion correctly")
        void testLoopInvariantMotion() {
            int[] array = {1, 2, 3, 4, 5};
            // Each element * (3 * 2) = each element * 6
            // 1*6 + 2*6 + 3*6 + 4*6 + 5*6 = 6 + 12 + 18 + 24 + 30 = 90
            assertThat(JitCompilation.LoopOptimizationDemo.loopInvariantMotion(array, 3)).isEqualTo(90);
        }

        @Test
        @DisplayName("Should handle empty array in loop invariant motion")
        void testLoopInvariantMotionEmpty() {
            assertThat(JitCompilation.LoopOptimizationDemo.loopInvariantMotion(new int[0], 5)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should sum array correctly")
        void testSumArray() {
            assertThat(JitCompilation.LoopOptimizationDemo.sumArray(new int[]{1, 2, 3, 4, 5})).isEqualTo(15);
            assertThat(JitCompilation.LoopOptimizationDemo.sumArray(new int[0])).isEqualTo(0);
            assertThat(JitCompilation.LoopOptimizationDemo.sumArray(new int[]{-1, 1})).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle loop with dead code same as without")
        void testLoopWithDeadCode() {
            // Dead code elimination: result should be same as simple loop sum
            // sum of 0..n-1 = n*(n-1)/2
            assertThat(JitCompilation.LoopOptimizationDemo.loopWithDeadCode(5)).isEqualTo(10); // 0+1+2+3+4
            assertThat(JitCompilation.LoopOptimizationDemo.loopWithDeadCode(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should compute large sum without overflow for appropriate n")
        void testSimpleSumLarge() {
            // n = 1000: sum = 1000 * 1001 / 2 = 500500
            assertThat(JitCompilation.LoopOptimizationDemo.simpleSum(1000)).isEqualTo(500500);
        }
    }

    @Nested
    @DisplayName("Intrinsics Demo")
    class IntrinsicsDemoTest {

        @Test
        @DisplayName("Should compute max value correctly")
        void testMaxValue() {
            assertThat(JitCompilation.IntrinsicsDemo.maxValue(3, 5)).isEqualTo(5);
            assertThat(JitCompilation.IntrinsicsDemo.maxValue(5, 3)).isEqualTo(5);
            assertThat(JitCompilation.IntrinsicsDemo.maxValue(5, 5)).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle negative values in max")
        void testMaxValueNegative() {
            assertThat(JitCompilation.IntrinsicsDemo.maxValue(-3, -5)).isEqualTo(-3);
            assertThat(JitCompilation.IntrinsicsDemo.maxValue(Integer.MIN_VALUE, Integer.MAX_VALUE))
                    .isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Should copy array correctly")
        void testCopyArray() {
            int[] source = {1, 2, 3, 4, 5};
            int[] copy = JitCompilation.IntrinsicsDemo.copyArray(source);

            assertThat(copy).isEqualTo(source);
            assertThat(copy).isNotSameAs(source); // Different array object
        }

        @Test
        @DisplayName("Should copy empty array")
        void testCopyEmptyArray() {
            int[] source = {};
            int[] copy = JitCompilation.IntrinsicsDemo.copyArray(source);
            assertThat(copy).isEmpty();
        }

        @Test
        @DisplayName("Should compare strings correctly")
        void testStringEquals() {
            assertThat(JitCompilation.IntrinsicsDemo.stringEquals("hello", "hello")).isTrue();
            assertThat(JitCompilation.IntrinsicsDemo.stringEquals("hello", "world")).isFalse();
            assertThat(JitCompilation.IntrinsicsDemo.stringEquals("", "")).isTrue();
        }

        @Test
        @DisplayName("Should count leading zeros correctly")
        void testLeadingZeros() {
            assertThat(JitCompilation.IntrinsicsDemo.leadingZeros(1)).isEqualTo(31);
            assertThat(JitCompilation.IntrinsicsDemo.leadingZeros(0)).isEqualTo(32);
            assertThat(JitCompilation.IntrinsicsDemo.leadingZeros(Integer.MAX_VALUE)).isEqualTo(1);
            assertThat(JitCompilation.IntrinsicsDemo.leadingZeros(-1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should grow array correctly")
        void testGrowArray() {
            int[] source = {1, 2, 3};
            int[] grown = JitCompilation.IntrinsicsDemo.growArray(source, 5);

            assertThat(grown).hasSize(5);
            assertThat(grown[0]).isEqualTo(1);
            assertThat(grown[1]).isEqualTo(2);
            assertThat(grown[2]).isEqualTo(3);
            assertThat(grown[3]).isEqualTo(0); // Zero-filled
            assertThat(grown[4]).isEqualTo(0);
        }

        @Test
        @DisplayName("Should shrink array correctly")
        void testShrinkArray() {
            int[] source = {1, 2, 3, 4, 5};
            int[] shrunk = JitCompilation.IntrinsicsDemo.growArray(source, 2);

            assertThat(shrunk).hasSize(2);
            assertThat(shrunk).containsExactly(1, 2);
        }
    }
}
