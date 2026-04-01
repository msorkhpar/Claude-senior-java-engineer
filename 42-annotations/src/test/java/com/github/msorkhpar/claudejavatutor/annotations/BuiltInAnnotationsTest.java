package com.github.msorkhpar.claudejavatutor.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Built-in Annotations Tests")
class BuiltInAnnotationsTest {

    @Nested
    @DisplayName("@Override Behavior")
    class OverrideTest {

        @Test
        @DisplayName("Subclass should override parent method behavior")
        void testOverrideChangesMethodBehavior() {
            BuiltInAnnotations.Animal animal = new BuiltInAnnotations.Animal();
            BuiltInAnnotations.Dog dog = new BuiltInAnnotations.Dog();

            assertThat(animal.speak()).isEqualTo("...");
            assertThat(dog.speak()).isEqualTo("Woof!");
        }

        @Test
        @DisplayName("Overridden methods should have polymorphic behavior")
        void testPolymorphicBehavior() {
            BuiltInAnnotations.Animal animal = new BuiltInAnnotations.Dog();

            assertThat(animal.speak()).isEqualTo("Woof!");
            assertThat(animal.describe()).isEqualTo("I am a dog");
        }

        @Test
        @DisplayName("Interface implementation with @Override should work correctly")
        void testInterfaceOverride() {
            BuiltInAnnotations.Greetable greeter = new BuiltInAnnotations.FriendlyGreeter();

            assertThat(greeter.greet("World")).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("@Override should be present on overridden methods via reflection")
        void testOverrideAnnotationPresent() throws NoSuchMethodException {
            Method speakMethod = BuiltInAnnotations.Dog.class.getDeclaredMethod("speak");

            // @Override has SOURCE retention, so it's NOT available at runtime
            assertThat(speakMethod.isAnnotationPresent(Override.class)).isFalse();
        }

        @Test
        @DisplayName("Should handle null-safe greet")
        void testGreeterWithNullName() {
            BuiltInAnnotations.FriendlyGreeter greeter = new BuiltInAnnotations.FriendlyGreeter();

            // The method concatenates — null becomes string "null"
            assertThat(greeter.greet(null)).isEqualTo("Hello, null!");
        }

        @Test
        @DisplayName("Should handle empty string greet")
        void testGreeterWithEmptyName() {
            BuiltInAnnotations.FriendlyGreeter greeter = new BuiltInAnnotations.FriendlyGreeter();

            assertThat(greeter.greet("")).isEqualTo("Hello, !");
        }
    }

    @Nested
    @DisplayName("@Deprecated Behavior")
    class DeprecatedTest {

        @Test
        @DisplayName("Deprecated method should still work functionally")
        @SuppressWarnings("deprecation")
        void testDeprecatedMethodStillWorks() {
            BuiltInAnnotations.LegacyApi api = new BuiltInAnnotations.LegacyApi();

            assertThat(api.oldMethod()).isEqualTo("old result");
        }

        @Test
        @DisplayName("New method should return updated result")
        void testNewMethodReplacesOld() {
            BuiltInAnnotations.LegacyApi api = new BuiltInAnnotations.LegacyApi();

            assertThat(api.newMethod()).isEqualTo("new result");
        }

        @Test
        @DisplayName("@Deprecated annotation should have correct 'since' and 'forRemoval' values")
        void testDeprecatedMetadata() throws NoSuchMethodException {
            Method oldMethod = BuiltInAnnotations.LegacyApi.class.getDeclaredMethod("oldMethod");
            Deprecated deprecated = oldMethod.getAnnotation(Deprecated.class);

            assertThat(deprecated).isNotNull();
            assertThat(deprecated.since()).isEqualTo("2.0");
            assertThat(deprecated.forRemoval()).isTrue();
        }

        @Test
        @DisplayName("Deprecated method with only 'since' should NOT be marked for removal")
        void testDeprecatedWithoutForRemoval() throws NoSuchMethodException {
            Method legacyCalc = BuiltInAnnotations.LegacyApi.class.getDeclaredMethod("legacyCalculation", int.class, int.class);
            Deprecated deprecated = legacyCalc.getAnnotation(Deprecated.class);

            assertThat(deprecated).isNotNull();
            assertThat(deprecated.since()).isEqualTo("1.5");
            assertThat(deprecated.forRemoval()).isFalse();
        }

        @Test
        @DisplayName("Legacy and modern calculation should produce same result for normal values")
        @SuppressWarnings("deprecation")
        void testLegacyVsModernCalculation() {
            BuiltInAnnotations.LegacyApi api = new BuiltInAnnotations.LegacyApi();

            assertThat(api.legacyCalculation(3, 5)).isEqualTo(api.modernCalculation(3, 5));
        }

        @Test
        @DisplayName("Modern calculation should detect overflow")
        void testModernCalculationOverflow() {
            BuiltInAnnotations.LegacyApi api = new BuiltInAnnotations.LegacyApi();

            assertThatThrownBy(() -> api.modernCalculation(Integer.MAX_VALUE, 1))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("Non-deprecated method should not have @Deprecated annotation")
        void testNonDeprecatedMethod() throws NoSuchMethodException {
            Method newMethod = BuiltInAnnotations.LegacyApi.class.getDeclaredMethod("newMethod");

            assertThat(newMethod.isAnnotationPresent(Deprecated.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("@SuppressWarnings Behavior")
    class SuppressWarningsTest {

        @Test
        @DisplayName("Unsafe cast should work when input is correct type")
        void testUnsafeCastWithCorrectType() {
            BuiltInAnnotations.WarningSuppressionExamples examples = new BuiltInAnnotations.WarningSuppressionExamples();
            List<String> original = List.of("a", "b", "c");

            List<String> result = examples.unsafeCast(original);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Unsafe cast should throw ClassCastException when used with wrong type")
        void testUnsafeCastWithWrongType() {
            BuiltInAnnotations.WarningSuppressionExamples examples = new BuiltInAnnotations.WarningSuppressionExamples();

            // The cast will throw ClassCastException at the point of casting
            assertThatThrownBy(() -> examples.unsafeCast("not a list"))
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should call deprecated method without compilation warnings")
        @SuppressWarnings("deprecation")
        void testCallDeprecatedMethod() {
            BuiltInAnnotations.WarningSuppressionExamples examples = new BuiltInAnnotations.WarningSuppressionExamples();

            assertThat(examples.callDeprecatedMethod()).isEqualTo("old result");
        }

        @Test
        @DisplayName("Raw to typed conversion should work")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void testRawToTyped() {
            BuiltInAnnotations.WarningSuppressionExamples examples = new BuiltInAnnotations.WarningSuppressionExamples();
            List rawList = new ArrayList();
            rawList.add("hello");
            rawList.add("world");

            List<String> result = examples.rawToTyped(rawList);

            assertThat(result).containsExactly("hello", "world");
        }

        @Test
        @DisplayName("Raw to typed with empty list should return empty list")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void testRawToTypedEmptyList() {
            BuiltInAnnotations.WarningSuppressionExamples examples = new BuiltInAnnotations.WarningSuppressionExamples();
            List rawList = new ArrayList();

            List<String> result = examples.rawToTyped(rawList);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("@SuppressWarnings has SOURCE retention — not visible at runtime")
        void testSuppressWarningsNotVisibleAtRuntime() throws NoSuchMethodException {
            Method method = BuiltInAnnotations.WarningSuppressionExamples.class.getDeclaredMethod("unsafeCast", Object.class);

            assertThat(method.isAnnotationPresent(SuppressWarnings.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("@FunctionalInterface Behavior")
    class FunctionalInterfaceTest {

        @Test
        @DisplayName("Transformer should work with lambda expression")
        void testTransformerWithLambda() {
            BuiltInAnnotations.Transformer<String, Integer> lengthTransformer = String::length;

            assertThat(lengthTransformer.transform("hello")).isEqualTo(5);
        }

        @Test
        @DisplayName("Transformer andThen should chain transformations")
        void testTransformerChaining() {
            BuiltInAnnotations.Transformer<String, Integer> length = String::length;
            BuiltInAnnotations.Transformer<Integer, String> toString = i -> "Length: " + i;

            BuiltInAnnotations.Transformer<String, String> chained = length.andThen(toString);

            assertThat(chained.transform("hello")).isEqualTo("Length: 5");
        }

        @Test
        @DisplayName("Validator should work with lambda expression")
        void testValidatorWithLambda() {
            BuiltInAnnotations.Validator<String> notEmpty = s -> s != null && !s.isEmpty();

            assertThat(notEmpty.validate("hello")).isTrue();
            assertThat(notEmpty.validate("")).isFalse();
            assertThat(notEmpty.validate(null)).isFalse();
        }

        @Test
        @DisplayName("TransformerUtils should apply transformation correctly")
        void testTransformerUtils() {
            Integer result = BuiltInAnnotations.TransformerUtils.applyTransformation(
                    "Java", s -> s.length());

            assertThat(result).isEqualTo(4);
        }

        @Test
        @DisplayName("TransformerUtils should apply validation correctly")
        void testValidationUtils() {
            boolean isPositive = BuiltInAnnotations.TransformerUtils.applyValidation(
                    42, n -> n > 0);

            assertThat(isPositive).isTrue();
        }

        @Test
        @DisplayName("@FunctionalInterface annotation should be present at runtime")
        void testFunctionalInterfaceAnnotationPresent() {
            assertThat(BuiltInAnnotations.Transformer.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
            assertThat(BuiltInAnnotations.Validator.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }

        @Test
        @DisplayName("Transformer with null input should handle gracefully based on lambda")
        void testTransformerWithNullInput() {
            BuiltInAnnotations.Transformer<String, String> safeUpperCase =
                    s -> s == null ? "NULL" : s.toUpperCase();

            assertThat(safeUpperCase.transform(null)).isEqualTo("NULL");
            assertThat(safeUpperCase.transform("hello")).isEqualTo("HELLO");
        }
    }

    @Nested
    @DisplayName("@SafeVarargs Behavior")
    class SafeVarargsTest {

        @Test
        @DisplayName("Should create a list from varargs safely")
        void testSafeListOf() {
            List<String> result = BuiltInAnnotations.SafeVarargsExamples.safeListOf("a", "b", "c");

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("Should handle empty varargs")
        void testSafeListOfEmpty() {
            List<String> result = BuiltInAnnotations.SafeVarargsExamples.safeListOf();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single element varargs")
        void testSafeListOfSingleElement() {
            List<Integer> result = BuiltInAnnotations.SafeVarargsExamples.safeListOf(42);

            assertThat(result).containsExactly(42);
        }

        @Test
        @DisplayName("Should group arrays into list of lists")
        void testGroupElements() {
            BuiltInAnnotations.SafeVarargsExamples examples = new BuiltInAnnotations.SafeVarargsExamples();
            String[] arr1 = {"a", "b"};
            String[] arr2 = {"c", "d", "e"};

            @SuppressWarnings("unchecked")
            List<List<String>> result = examples.groupElements(arr1, arr2);

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).containsExactly("a", "b");
            assertThat(result.get(1)).containsExactly("c", "d", "e");
        }

        @Test
        @DisplayName("@SafeVarargs annotation should be present at runtime")
        void testSafeVarargsAnnotationPresent() throws NoSuchMethodException {
            Method method = BuiltInAnnotations.SafeVarargsExamples.class.getDeclaredMethod("safeListOf", Object[].class);

            assertThat(method.isAnnotationPresent(SafeVarargs.class)).isTrue();
        }
    }
}
