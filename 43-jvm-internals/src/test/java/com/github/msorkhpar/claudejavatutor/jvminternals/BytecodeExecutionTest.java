package com.github.msorkhpar.claudejavatutor.jvminternals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Bytecode Execution Tests")
class BytecodeExecutionTest {

    @Nested
    @DisplayName("Operand Stack Demo")
    class OperandStackDemoTest {

        @Test
        @DisplayName("Should add two integers correctly")
        void testAdd() {
            assertThat(BytecodeExecution.OperandStackDemo.add(3, 5)).isEqualTo(8);
        }

        @Test
        @DisplayName("Should handle addition with zero")
        void testAddWithZero() {
            assertThat(BytecodeExecution.OperandStackDemo.add(0, 5)).isEqualTo(5);
            assertThat(BytecodeExecution.OperandStackDemo.add(5, 0)).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle negative addition")
        void testAddNegative() {
            assertThat(BytecodeExecution.OperandStackDemo.add(-3, -5)).isEqualTo(-8);
            assertThat(BytecodeExecution.OperandStackDemo.add(-3, 5)).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle integer overflow in addition")
        void testAddOverflow() {
            assertThat(BytecodeExecution.OperandStackDemo.add(Integer.MAX_VALUE, 1))
                    .isEqualTo(Integer.MIN_VALUE);
        }

        @Test
        @DisplayName("Should compute quadratic correctly")
        void testQuadratic() {
            // x^2 + 2x + 1 = (x+1)^2
            assertThat(BytecodeExecution.OperandStackDemo.quadratic(3)).isEqualTo(16); // 9 + 6 + 1
            assertThat(BytecodeExecution.OperandStackDemo.quadratic(0)).isEqualTo(1);  // 0 + 0 + 1
            assertThat(BytecodeExecution.OperandStackDemo.quadratic(1)).isEqualTo(4);  // 1 + 2 + 1
        }

        @Test
        @DisplayName("Should compute quadratic with negative input")
        void testQuadraticNegative() {
            assertThat(BytecodeExecution.OperandStackDemo.quadratic(-1)).isEqualTo(0); // 1 - 2 + 1
            assertThat(BytecodeExecution.OperandStackDemo.quadratic(-3)).isEqualTo(4); // 9 - 6 + 1
        }

        @Test
        @DisplayName("Should swap and compute correctly")
        void testSwapAndCompute() {
            // After swap: a becomes b, b becomes a; returns a-b (which is original b - original a)
            assertThat(BytecodeExecution.OperandStackDemo.swapAndCompute(3, 5)).isEqualTo(2);  // 5 - 3
            assertThat(BytecodeExecution.OperandStackDemo.swapAndCompute(5, 3)).isEqualTo(-2); // 3 - 5
        }

        @Test
        @DisplayName("Should swap identical values")
        void testSwapIdentical() {
            assertThat(BytecodeExecution.OperandStackDemo.swapAndCompute(7, 7)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should add long values correctly")
        void testLongAdd() {
            assertThat(BytecodeExecution.OperandStackDemo.longAdd(1L, 2L)).isEqualTo(3L);
            assertThat(BytecodeExecution.OperandStackDemo.longAdd(Long.MAX_VALUE - 1, 1))
                    .isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("Should handle long overflow")
        void testLongAddOverflow() {
            assertThat(BytecodeExecution.OperandStackDemo.longAdd(Long.MAX_VALUE, 1))
                    .isEqualTo(Long.MIN_VALUE);
        }

        @Test
        @DisplayName("Should perform mixed arithmetic with type conversion")
        void testMixedArithmetic() {
            assertThat(BytecodeExecution.OperandStackDemo.mixedArithmetic(3, 2.5))
                    .isEqualTo(5.5);
            assertThat(BytecodeExecution.OperandStackDemo.mixedArithmetic(0, 3.14))
                    .isEqualTo(3.14);
        }

        @Test
        @DisplayName("Should handle boolean logical operations")
        void testLogicalOps() {
            // a && b || !a
            assertThat(BytecodeExecution.OperandStackDemo.logicalOps(true, true)).isTrue();   // T&&T || F = T
            assertThat(BytecodeExecution.OperandStackDemo.logicalOps(true, false)).isFalse(); // T&&F || F = F
            assertThat(BytecodeExecution.OperandStackDemo.logicalOps(false, true)).isTrue();  // F&&T || T = T
            assertThat(BytecodeExecution.OperandStackDemo.logicalOps(false, false)).isTrue(); // F&&F || T = T
        }
    }

    @Nested
    @DisplayName("Method Invocation Demo")
    class MethodInvocationDemoTest {

        private final BytecodeExecution.MethodInvocationDemo demo = new BytecodeExecution.MethodInvocationDemo();

        @Test
        @DisplayName("Should invoke virtual method")
        void testVirtualMethod() {
            assertThat(demo.virtualMethod()).isEqualTo("invokevirtual");
        }

        @Test
        @DisplayName("Should invoke static method")
        void testStaticMethod() {
            assertThat(BytecodeExecution.MethodInvocationDemo.staticMethod()).isEqualTo("invokestatic");
        }

        @Test
        @DisplayName("Should demonstrate all invocation types")
        void testAllInvocations() {
            Map<String, String> results = demo.demonstrateAllInvocations();

            assertThat(results)
                    .containsEntry("virtual", "invokevirtual")
                    .containsEntry("static", "invokestatic")
                    .containsEntry("special", "invokespecial")
                    .containsEntry("interface", "invokeinterface")
                    .containsEntry("dynamic", "INVOKEDYNAMIC");
        }

        @Test
        @DisplayName("Should create a new object via constructor invocation")
        void testCreateObject() {
            Object obj = demo.createObject();
            assertThat(obj).isNotNull();
            assertThat(obj.getClass()).isEqualTo(Object.class);
        }
    }

    @Nested
    @DisplayName("Bridge Method Inspector")
    class BridgeMethodInspectorTest {

        private final BytecodeExecution.BridgeMethodInspector inspector =
                new BytecodeExecution.BridgeMethodInspector();

        @Test
        @DisplayName("Should find bridge methods in generic implementations")
        void testFindBridgeMethodsInStringTransformer() {
            List<String> bridges = inspector.findBridgeMethods(BytecodeExecution.StringTransformer.class);
            assertThat(bridges).isNotEmpty();
            assertThat(bridges).anyMatch(m -> m.contains("Object"));
        }

        @Test
        @DisplayName("Should find bridge methods in IntegerTransformer")
        void testFindBridgeMethodsInIntegerTransformer() {
            List<String> bridges = inspector.findBridgeMethods(BytecodeExecution.IntegerTransformer.class);
            assertThat(bridges).isNotEmpty();
        }

        @Test
        @DisplayName("Should find synthetic methods matching bridge methods")
        void testSyntheticMethodsMatchBridges() {
            List<String> bridges = inspector.findBridgeMethods(BytecodeExecution.StringTransformer.class);
            List<String> synthetics = inspector.findSyntheticMethods(BytecodeExecution.StringTransformer.class);
            // Bridge methods are always synthetic
            assertThat(synthetics).containsAll(bridges);
        }

        @Test
        @DisplayName("Should count more total methods than user methods due to bridges")
        void testMethodCounts() {
            int allMethods = inspector.countAllMethods(BytecodeExecution.StringTransformer.class);
            int userMethods = inspector.countUserMethods(BytecodeExecution.StringTransformer.class);
            assertThat(allMethods).isGreaterThan(userMethods);
        }

        @Test
        @DisplayName("Should not find bridge methods in non-generic class")
        void testNoBridgeMethodsInPlainClass() {
            List<String> bridges = inspector.findBridgeMethods(BytecodeExecution.OperandStackDemo.class);
            assertThat(bridges).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transformer (Bridge Method Demo)")
    class TransformerTest {

        @Test
        @DisplayName("StringTransformer should transform to uppercase")
        void testStringTransformer() {
            BytecodeExecution.StringTransformer transformer = new BytecodeExecution.StringTransformer();
            assertThat(transformer.transform("hello")).isEqualTo("HELLO");
        }

        @Test
        @DisplayName("StringTransformer should handle null input")
        void testStringTransformerNull() {
            BytecodeExecution.StringTransformer transformer = new BytecodeExecution.StringTransformer();
            assertThat(transformer.transform(null)).isNull();
        }

        @Test
        @DisplayName("StringTransformer should handle empty string")
        void testStringTransformerEmpty() {
            BytecodeExecution.StringTransformer transformer = new BytecodeExecution.StringTransformer();
            assertThat(transformer.transform("")).isEmpty();
        }

        @Test
        @DisplayName("IntegerTransformer should double the value")
        void testIntegerTransformer() {
            BytecodeExecution.IntegerTransformer transformer = new BytecodeExecution.IntegerTransformer();
            assertThat(transformer.transform(5)).isEqualTo(10);
            assertThat(transformer.transform(0)).isEqualTo(0);
            assertThat(transformer.transform(-3)).isEqualTo(-6);
        }

        @Test
        @DisplayName("IntegerTransformer should handle null input")
        void testIntegerTransformerNull() {
            BytecodeExecution.IntegerTransformer transformer = new BytecodeExecution.IntegerTransformer();
            assertThat(transformer.transform(null)).isNull();
        }

        @Test
        @DisplayName("Should work polymorphically through Transformer interface")
        @SuppressWarnings("unchecked")
        void testPolymorphicTransformer() {
            BytecodeExecution.Transformer<String> transformer = new BytecodeExecution.StringTransformer();
            assertThat(transformer.transform("test")).isEqualTo("TEST");
        }
    }

    @Nested
    @DisplayName("Autoboxing Demo")
    class AutoboxingDemoTest {

        private final BytecodeExecution.AutoboxingDemo demo = new BytecodeExecution.AutoboxingDemo();

        @Test
        @DisplayName("Should autobox int to Integer")
        void testAutobox() {
            Integer result = demo.autobox(42);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should unbox Integer to int")
        void testUnbox() {
            int result = demo.unbox(Integer.valueOf(42));
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should throw NullPointerException when unboxing null")
        void testUnboxNull() {
            assertThatThrownBy(() -> demo.unbox(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should demonstrate Integer cache for values -128 to 127")
        void testIntegerCache() {
            assertThat(demo.isCached(0)).isTrue();
            assertThat(demo.isCached(127)).isTrue();
            assertThat(demo.isCached(-128)).isTrue();
        }

        @Test
        @DisplayName("Should not use cache for values outside -128 to 127")
        void testIntegerCacheOutOfRange() {
            assertThat(demo.isCached(128)).isFalse();
            assertThat(demo.isCached(1000)).isFalse();
        }

        @Test
        @DisplayName("Should add boxed integers correctly")
        void testAddBoxed() {
            assertThat(demo.addBoxed(3, 5)).isEqualTo(8);
            assertThat(demo.addBoxed(0, 0)).isEqualTo(0);
            assertThat(demo.addBoxed(-3, 5)).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw NPE when adding null boxed integers")
        void testAddBoxedNull() {
            assertThatThrownBy(() -> demo.addBoxed(null, 5))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Constant Pool Demo")
    class ConstantPoolDemoTest {

        private final BytecodeExecution.ConstantPoolDemo demo = new BytecodeExecution.ConstantPoolDemo();

        @Test
        @DisplayName("Should intern strings matching literals")
        void testStringInternMatching() {
            assertThat(demo.stringInternDemo("hello")).isTrue();
        }

        @Test
        @DisplayName("Should not intern strings not matching literals")
        void testStringInternNotMatching() {
            assertThat(demo.stringInternDemo("world")).isFalse();
        }

        @Test
        @DisplayName("Should intern dynamically created string matching literal")
        void testStringInternDynamic() {
            String dynamic = new String(new char[]{'h', 'e', 'l', 'l', 'o'});
            assertThat(demo.stringInternDemo(dynamic)).isTrue();
        }

        @Test
        @DisplayName("Should return compile-time constant")
        void testCompileTimeConstant() {
            assertThat(demo.getConstant()).isEqualTo(42);
            assertThat(BytecodeExecution.ConstantPoolDemo.COMPILE_TIME_CONSTANT).isEqualTo(42);
        }

        @Test
        @DisplayName("Should return runtime constant")
        void testRuntimeConstant() {
            assertThat(demo.getRuntimeConstant()).isEqualTo(42);
            assertThat(BytecodeExecution.ConstantPoolDemo.RUNTIME_CONSTANT).isEqualTo(42);
        }

        @Test
        @DisplayName("Should have string constant in constant pool")
        void testStringConstant() {
            assertThat(BytecodeExecution.ConstantPoolDemo.STRING_CONSTANT).isEqualTo("constant");
        }
    }

    @Nested
    @DisplayName("Lambda Desugaring Demo")
    class LambdaDesugaringDemoTest {

        private final BytecodeExecution.LambdaDesugaringDemo demo = new BytecodeExecution.LambdaDesugaringDemo();

        @Test
        @DisplayName("Should create non-capturing lambda that adds correctly")
        void testNonCapturingLambda() {
            IntBinaryOperator op = demo.getNonCapturingLambda();
            assertThat(op.applyAsInt(3, 5)).isEqualTo(8);
            assertThat(op.applyAsInt(0, 0)).isEqualTo(0);
            assertThat(op.applyAsInt(-1, 1)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create capturing lambda with threshold")
        void testCapturingLambda() {
            Predicate<Integer> gt5 = demo.getCapturingLambda(5);
            assertThat(gt5.test(6)).isTrue();
            assertThat(gt5.test(5)).isFalse();
            assertThat(gt5.test(4)).isFalse();
        }

        @Test
        @DisplayName("Should create different capturing lambdas with different thresholds")
        void testDifferentCapturingLambdas() {
            Predicate<Integer> gt5 = demo.getCapturingLambda(5);
            Predicate<Integer> gt10 = demo.getCapturingLambda(10);

            assertThat(gt5.test(7)).isTrue();
            assertThat(gt10.test(7)).isFalse();
        }

        @Test
        @DisplayName("Should create method reference that parses integers")
        void testMethodReference() {
            Function<String, Integer> parser = demo.getMethodReference();
            assertThat(parser.apply("42")).isEqualTo(42);
            assertThat(parser.apply("-1")).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should throw exception for invalid parse via method reference")
        void testMethodReferenceInvalidInput() {
            Function<String, Integer> parser = demo.getMethodReference();
            assertThatThrownBy(() -> parser.apply("abc"))
                    .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("Should verify non-capturing lambda reuse produces same results")
        void testNonCapturingLambdaReuse() {
            assertThat(demo.nonCapturingLambdaReuse()).isTrue();
        }
    }
}
