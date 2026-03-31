package com.github.msorkhpar.claudejavatutor.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Dynamic Object Creation and Method Invocation Tests")
class DynamicCreationInvocationTest {

    @Nested
    @DisplayName("Dynamic Object Creation from Class Name")
    class DynamicCreationTests {

        @Test
        @DisplayName("Should create object from fully-qualified class name with no-arg constructor")
        void testCreateFromClassNameNoArg() throws Exception {
            DynamicCreationInvocation.Service service =
                    DynamicCreationInvocation.createFromClassName(
                            "com.github.msorkhpar.claudejavatutor.reflection.DynamicCreationInvocation$Service");

            assertThat(service).isNotNull();
            assertThat(service.getName()).isEqualTo("DefaultService");
        }

        @Test
        @DisplayName("Should create object from class name with constructor arguments")
        void testCreateFromClassNameWithArgs() throws Exception {
            DynamicCreationInvocation.Service service =
                    DynamicCreationInvocation.createFromClassName(
                            "com.github.msorkhpar.claudejavatutor.reflection.DynamicCreationInvocation$Service",
                            new Class<?>[]{String.class},
                            "CustomService");

            assertThat(service.getName()).isEqualTo("CustomService");
        }

        @Test
        @DisplayName("Should throw ClassNotFoundException for invalid class name")
        void testCreateFromInvalidClassName() {
            assertThatThrownBy(() ->
                    DynamicCreationInvocation.createFromClassName("com.nonexistent.FakeClass"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("Should create standard library class from class name")
        void testCreateStandardLibraryClass() throws Exception {
            ArrayList<?> list = DynamicCreationInvocation.createFromClassName("java.util.ArrayList");
            assertThat(list).isNotNull();
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("Dynamic Method Invocation")
    class DynamicInvocationTests {

        @Test
        @DisplayName("Should invoke method dynamically with String argument")
        void testInvokeDynamicWithString() throws Exception {
            var service = new DynamicCreationInvocation.Service("MyService");

            Object result = DynamicCreationInvocation.invokeDynamic(service, "process", "hello");

            assertThat(result).isEqualTo("MyService processed: hello");
        }

        @Test
        @DisplayName("Should invoke getName dynamically with no arguments")
        void testInvokeDynamicNoArgs() throws Exception {
            var service = new DynamicCreationInvocation.Service("TestService");

            Object result = DynamicCreationInvocation.invokeDynamic(service, "getName");

            assertThat(result).isEqualTo("TestService");
        }

        @Test
        @DisplayName("Should invoke method on Greeter implementation")
        void testInvokeDynamicOnInterface() throws Exception {
            var greeter = new DynamicCreationInvocation.EnglishGreeter();

            Object result = DynamicCreationInvocation.invokeDynamic(greeter, "greet", "World");

            assertThat(result).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("Should invoke method with Integer arguments (auto-unboxing)")
        void testInvokeDynamicWithIntegers() throws Exception {
            var calc = new DynamicCreationInvocation.Addition();

            Object result = DynamicCreationInvocation.invokeDynamic(calc, "compute", 5, 3);

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("Should throw NoSuchMethodException for non-existent method")
        void testInvokeDynamicNonExistent() {
            var service = new DynamicCreationInvocation.Service();

            assertThatThrownBy(() -> DynamicCreationInvocation.invokeDynamic(service, "nonExistent"))
                    .isInstanceOf(NoSuchMethodException.class);
        }
    }

    @Nested
    @DisplayName("Dynamic Array Operations")
    class DynamicArrayTests {

        @Test
        @DisplayName("Should create and populate int array dynamically")
        void testCreateIntArray() {
            Object array = DynamicCreationInvocation.createArray(int.class, 5);

            DynamicCreationInvocation.setArrayElement(array, 0, 10);
            DynamicCreationInvocation.setArrayElement(array, 1, 20);
            DynamicCreationInvocation.setArrayElement(array, 2, 30);

            assertThat(DynamicCreationInvocation.getArrayLength(array)).isEqualTo(5);
            assertThat(DynamicCreationInvocation.getArrayElement(array, 0)).isEqualTo(10);
            assertThat(DynamicCreationInvocation.getArrayElement(array, 1)).isEqualTo(20);
            assertThat(DynamicCreationInvocation.getArrayElement(array, 2)).isEqualTo(30);
            assertThat(DynamicCreationInvocation.getArrayElement(array, 3)).isEqualTo(0); // default
        }

        @Test
        @DisplayName("Should create and populate String array dynamically")
        void testCreateStringArray() {
            Object array = DynamicCreationInvocation.createArray(String.class, 3);

            DynamicCreationInvocation.setArrayElement(array, 0, "hello");
            DynamicCreationInvocation.setArrayElement(array, 1, "world");

            assertThat(DynamicCreationInvocation.getArrayLength(array)).isEqualTo(3);
            assertThat(DynamicCreationInvocation.getArrayElement(array, 0)).isEqualTo("hello");
            assertThat(DynamicCreationInvocation.getArrayElement(array, 1)).isEqualTo("world");
            assertThat(DynamicCreationInvocation.getArrayElement(array, 2)).isNull();
        }

        @Test
        @DisplayName("Should throw ArrayIndexOutOfBoundsException for invalid index")
        void testArrayOutOfBounds() {
            Object array = DynamicCreationInvocation.createArray(int.class, 3);

            assertThatThrownBy(() -> DynamicCreationInvocation.setArrayElement(array, 5, 10))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("Should create zero-length array")
        void testZeroLengthArray() {
            Object array = DynamicCreationInvocation.createArray(String.class, 0);
            assertThat(DynamicCreationInvocation.getArrayLength(array)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-array object")
        void testGetLengthOfNonArray() {
            assertThatThrownBy(() -> DynamicCreationInvocation.getArrayLength("not an array"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Dynamic Proxy")
    class DynamicProxyTests {

        @Test
        @DisplayName("Should create logging proxy that logs method calls")
        void testLoggingProxy() {
            var realGreeter = new DynamicCreationInvocation.EnglishGreeter();
            List<String> log = new ArrayList<>();

            DynamicCreationInvocation.Greeter proxy = DynamicCreationInvocation.createLoggingProxy(
                    DynamicCreationInvocation.Greeter.class, realGreeter, log);

            String result = proxy.greet("Alice");

            assertThat(result).isEqualTo("Hello, Alice!");
            assertThat(log).hasSize(2);
            assertThat(log.get(0)).isEqualTo("Calling greet(Alice)");
            assertThat(log.get(1)).isEqualTo("Returned: Hello, Alice!");
        }

        @Test
        @DisplayName("Should create logging proxy for Calculator")
        void testLoggingProxyCalculator() {
            var realCalc = new DynamicCreationInvocation.Addition();
            List<String> log = new ArrayList<>();

            DynamicCreationInvocation.Calculator proxy = DynamicCreationInvocation.createLoggingProxy(
                    DynamicCreationInvocation.Calculator.class, realCalc, log);

            int result = proxy.compute(10, 20);

            assertThat(result).isEqualTo(30);
            assertThat(log).hasSize(2);
            assertThat(log.get(0)).contains("compute");
            assertThat(log.get(1)).contains("30");
        }

        @Test
        @DisplayName("Should create timing proxy that records execution time")
        void testTimingProxy() {
            var realGreeter = new DynamicCreationInvocation.EnglishGreeter();
            Map<String, Long> timings = new HashMap<>();

            DynamicCreationInvocation.Greeter proxy = DynamicCreationInvocation.createTimingProxy(
                    DynamicCreationInvocation.Greeter.class, realGreeter, timings);

            proxy.greet("Bob");

            assertThat(timings).containsKey("greet");
            assertThat(timings.get("greet")).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should proxy multiple calls and record timings")
        void testTimingProxyMultipleCalls() {
            var realCalc = new DynamicCreationInvocation.Multiplication();
            Map<String, Long> timings = new HashMap<>();

            DynamicCreationInvocation.Calculator proxy = DynamicCreationInvocation.createTimingProxy(
                    DynamicCreationInvocation.Calculator.class, realCalc, timings);

            int result1 = proxy.compute(3, 4);
            int result2 = proxy.compute(5, 6);

            assertThat(result1).isEqualTo(12);
            assertThat(result2).isEqualTo(30);
            assertThat(timings).containsKey("compute");
        }
    }

    @Nested
    @DisplayName("Reflective Factory")
    class ReflectiveFactoryTests {

        @Test
        @DisplayName("Should create objects by registered key")
        void testFactory() throws Exception {
            var factory = new DynamicCreationInvocation.ReflectiveFactory<DynamicCreationInvocation.Greeter>();
            factory.register("english", DynamicCreationInvocation.EnglishGreeter.class);
            factory.register("french", DynamicCreationInvocation.FrenchGreeter.class);

            DynamicCreationInvocation.Greeter english = factory.create("english");
            DynamicCreationInvocation.Greeter french = factory.create("french");

            assertThat(english.greet("World")).isEqualTo("Hello, World!");
            assertThat(french.greet("World")).isEqualTo("Bonjour, World!");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for unregistered key")
        void testFactoryUnregisteredKey() {
            var factory = new DynamicCreationInvocation.ReflectiveFactory<DynamicCreationInvocation.Greeter>();

            assertThatThrownBy(() -> factory.create("unknown"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No class registered");
        }
    }

    @Nested
    @DisplayName("Simple DI Container")
    class DIContainerTests {

        @Test
        @DisplayName("Should resolve transient bindings (new instance each time)")
        void testTransientBinding() {
            var container = new DynamicCreationInvocation.SimpleDIContainer();
            container.bind(DynamicCreationInvocation.Service.class, DynamicCreationInvocation.Service::new);

            var service1 = container.resolve(DynamicCreationInvocation.Service.class);
            var service2 = container.resolve(DynamicCreationInvocation.Service.class);

            assertThat(service1).isNotNull();
            assertThat(service2).isNotNull();
            assertThat(service1).isNotSameAs(service2);
        }

        @Test
        @DisplayName("Should resolve singleton bindings (same instance)")
        void testSingletonBinding() {
            var container = new DynamicCreationInvocation.SimpleDIContainer();
            container.bindSingleton(DynamicCreationInvocation.Service.class,
                    () -> new DynamicCreationInvocation.Service("SingletonService"));

            var service1 = container.resolve(DynamicCreationInvocation.Service.class);
            var service2 = container.resolve(DynamicCreationInvocation.Service.class);

            assertThat(service1).isSameAs(service2);
            assertThat(service1.getName()).isEqualTo("SingletonService");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for unbound type")
        void testResolveUnbound() {
            var container = new DynamicCreationInvocation.SimpleDIContainer();

            assertThatThrownBy(() -> container.resolve(String.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("No binding");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle creating EnglishGreeter and using as Greeter interface")
        void testPolymorphicCreation() throws Exception {
            DynamicCreationInvocation.Greeter greeter = DynamicCreationInvocation.createFromClassName(
                    DynamicCreationInvocation.EnglishGreeter.class.getName());

            assertThat(greeter).isInstanceOf(DynamicCreationInvocation.Greeter.class);
            assertThat(greeter.greet("Test")).isEqualTo("Hello, Test!");
        }

        @Test
        @DisplayName("Should handle null arguments in dynamic array")
        void testArrayWithNullValues() {
            Object array = DynamicCreationInvocation.createArray(String.class, 3);
            DynamicCreationInvocation.setArrayElement(array, 0, null);

            assertThat(DynamicCreationInvocation.getArrayElement(array, 0)).isNull();
        }

        @Test
        @DisplayName("Should handle French greeter via logging proxy")
        void testLoggingProxyFrenchGreeter() {
            var frenchGreeter = new DynamicCreationInvocation.FrenchGreeter();
            List<String> log = new ArrayList<>();

            DynamicCreationInvocation.Greeter proxy = DynamicCreationInvocation.createLoggingProxy(
                    DynamicCreationInvocation.Greeter.class, frenchGreeter, log);

            String result = proxy.greet("Claude");

            assertThat(result).isEqualTo("Bonjour, Claude!");
            assertThat(log.get(0)).contains("greet");
        }
    }
}
