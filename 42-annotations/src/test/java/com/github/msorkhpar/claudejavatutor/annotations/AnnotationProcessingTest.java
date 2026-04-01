package com.github.msorkhpar.claudejavatutor.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Annotation Processing Tests")
class AnnotationProcessingTest {

    @Nested
    @DisplayName("Lifecycle Processor (@PostConstruct, @PreDestroy)")
    class LifecycleProcessorTest {

        @Test
        @DisplayName("@PostConstruct methods should be invoked in order")
        void testPostConstructOrder() throws Exception {
            AnnotationProcessing.SampleService service = new AnnotationProcessing.SampleService();

            AnnotationProcessing.LifecycleProcessor.initialize(service);

            assertThat(service.getInitLog()).containsExactly("init", "warmUpCache");
        }

        @Test
        @DisplayName("@PreDestroy methods should be invoked in order")
        void testPreDestroyOrder() throws Exception {
            AnnotationProcessing.SampleService service = new AnnotationProcessing.SampleService();

            AnnotationProcessing.LifecycleProcessor.destroy(service);

            assertThat(service.getInitLog()).containsExactly("flushBuffers", "closeConnections");
        }

        @Test
        @DisplayName("Full lifecycle: init then destroy should log all phases")
        void testFullLifecycle() throws Exception {
            AnnotationProcessing.SampleService service = new AnnotationProcessing.SampleService();

            AnnotationProcessing.LifecycleProcessor.initialize(service);
            AnnotationProcessing.LifecycleProcessor.destroy(service);

            assertThat(service.getInitLog())
                    .containsExactly("init", "warmUpCache", "flushBuffers", "closeConnections");
        }

        @Test
        @DisplayName("Should handle class with no lifecycle annotations")
        void testNoLifecycleAnnotations() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();

            // Should not throw
            AnnotationProcessing.LifecycleProcessor.initialize(service);
            AnnotationProcessing.LifecycleProcessor.destroy(service);
        }
    }

    @Nested
    @DisplayName("Field Injector (@Inject)")
    class FieldInjectorTest {

        @Test
        @DisplayName("Should inject values by annotation value key")
        void testInjectByAnnotationValue() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();
            Map<String, Object> registry = Map.of(
                    "dataSource", "jdbc:mysql://localhost:3306/db",
                    "config", "production"
            );

            AnnotationProcessing.FieldInjector.inject(service, registry);

            assertThat(service.getDataSource()).isEqualTo("jdbc:mysql://localhost:3306/db");
            assertThat(service.getConfig()).isEqualTo("production");
        }

        @Test
        @DisplayName("Should inject value by field name when @Inject has no value")
        void testInjectByFieldName() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();
            Map<String, Object> registry = Map.of("config", "staging");

            AnnotationProcessing.FieldInjector.inject(service, registry);

            assertThat(service.getConfig()).isEqualTo("staging");
            assertThat(service.getDataSource()).isNull(); // Not in registry
        }

        @Test
        @DisplayName("Should leave field null when key not in registry")
        void testInjectMissingKey() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();
            Map<String, Object> registry = Map.of("unrelated", "value");

            AnnotationProcessing.FieldInjector.inject(service, registry);

            assertThat(service.getDataSource()).isNull();
            assertThat(service.getConfig()).isNull();
        }

        @Test
        @DisplayName("Should handle empty registry")
        void testInjectEmptyRegistry() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();

            AnnotationProcessing.FieldInjector.inject(service, Map.of());

            assertThat(service.getDataSource()).isNull();
            assertThat(service.getConfig()).isNull();
        }
    }

    @Nested
    @DisplayName("Timed Proxy (@Timed)")
    class TimedProxyTest {

        @Test
        @DisplayName("Should intercept and log @Timed method calls")
        void testTimedProxyLogging() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            int result = proxy.add(3, 5);

            assertThat(result).isEqualTo(8);
            assertThat(logs).hasSize(1);
            assertThat(logs.get(0).method()).isEqualTo("addition"); // label from @Timed
            assertThat(logs.get(0).result()).isEqualTo(8);
            assertThat(logs.get(0).durationNanos()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should use method name when @Timed has no label")
        void testTimedProxyDefaultLabel() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            proxy.multiply(4, 5);

            assertThat(logs).hasSize(1);
            assertThat(logs.get(0).method()).isEqualTo("multiply");
            assertThat(logs.get(0).result()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should log multiple invocations")
        void testTimedProxyMultipleInvocations() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            proxy.add(1, 2);
            proxy.multiply(3, 4);
            proxy.add(5, 6);

            assertThat(logs).hasSize(3);
        }

        @Test
        @DisplayName("Non-timed methods should work without logging")
        void testNonTimedMethodNoLog() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            proxy.adminReset(); // Not @Timed

            assertThat(logs).isEmpty();
        }
    }

    @Nested
    @DisplayName("Secured Proxy (@RequiresPermission)")
    class SecuredProxyTest {

        @Test
        @DisplayName("Should allow method call when user has required permission")
        void testSecuredProxyWithPermission() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            Set<String> permissions = Set.of("ADMIN", "READ");

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createSecuredProxy(
                    impl, AnnotationProcessing.Calculator.class, permissions);

            // Should not throw
            assertThatCode(() -> proxy.adminReset()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should throw SecurityException when user lacks required permission")
        void testSecuredProxyWithoutPermission() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            Set<String> permissions = Set.of("READ");

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createSecuredProxy(
                    impl, AnnotationProcessing.Calculator.class, permissions);

            assertThatThrownBy(() -> proxy.adminReset())
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Missing permission: ADMIN");
        }

        @Test
        @DisplayName("Non-secured methods should work without any permission")
        void testNonSecuredMethodNoPermissionCheck() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            Set<String> permissions = Set.of(); // empty permissions

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createSecuredProxy(
                    impl, AnnotationProcessing.Calculator.class, permissions);

            // add and multiply are not @RequiresPermission
            assertThat(proxy.add(1, 2)).isEqualTo(3);
            assertThat(proxy.multiply(3, 4)).isEqualTo(12);
        }

        @Test
        @DisplayName("Should throw SecurityException with empty permission set for secured method")
        void testEmptyPermissions() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createSecuredProxy(
                    impl, AnnotationProcessing.Calculator.class, Set.of());

            assertThatThrownBy(() -> proxy.adminReset())
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("Annotation Scanner")
    class AnnotationScannerTest {

        @Test
        @DisplayName("Should scan class-level annotations")
        void testScanClassAnnotations() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(CustomAnnotationCreation.UserService.class);

            assertThat(result.className()).isEqualTo("UserService");
            assertThat(result.classAnnotations())
                    .contains("ThreadSafe", "Author", "Auditable", "ApiVersion");
        }

        @Test
        @DisplayName("Should scan method-level annotations")
        void testScanMethodAnnotations() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(CustomAnnotationCreation.UserService.class);

            assertThat(result.methodAnnotations()).containsKey("listUsers");
            assertThat(result.methodAnnotations().get("listUsers")).contains("ApiEndpoint");
        }

        @Test
        @DisplayName("Should scan field-level annotations")
        void testScanFieldAnnotations() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(CustomAnnotationCreation.UserService.class);

            assertThat(result.fieldAnnotations()).containsKey("name");
            assertThat(result.fieldAnnotations().get("name")).contains("NotEmpty");
            assertThat(result.fieldAnnotations()).containsKey("age");
            assertThat(result.fieldAnnotations().get("age")).contains("Range");
        }

        @Test
        @DisplayName("Should handle class with no annotations")
        void testScanClassWithNoAnnotations() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(String.class);

            // String has no custom runtime annotations on its declared methods/fields we created
            assertThat(result.className()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should scan SampleService lifecycle annotations")
        void testScanSampleService() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(AnnotationProcessing.SampleService.class);

            assertThat(result.methodAnnotations()).containsKey("init");
            assertThat(result.methodAnnotations().get("init")).contains("PostConstruct");
            assertThat(result.methodAnnotations()).containsKey("flushBuffers");
            assertThat(result.methodAnnotations().get("flushBuffers")).contains("PreDestroy");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("Timed proxy should handle zero arguments")
        void testTimedProxyWithNoArgs() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            proxy.adminReset();

            // adminReset is not @Timed, so no log
            assertThat(logs).isEmpty();
        }

        @Test
        @DisplayName("Timed proxy result should match direct invocation")
        void testTimedProxyResultConsistency() {
            AnnotationProcessing.CalculatorImpl impl = new AnnotationProcessing.CalculatorImpl();
            List<AnnotationProcessing.InvocationLog> logs = new ArrayList<>();

            AnnotationProcessing.Calculator proxy = AnnotationProcessing.createTimedProxy(
                    impl, AnnotationProcessing.Calculator.class, logs);

            assertThat(proxy.add(100, 200)).isEqualTo(impl.add(100, 200));
            assertThat(proxy.multiply(7, 8)).isEqualTo(impl.multiply(7, 8));
        }

        @Test
        @DisplayName("Inject with all keys present should set all fields")
        void testInjectAllKeysPresent() throws Exception {
            AnnotationProcessing.InjectableService service = new AnnotationProcessing.InjectableService();
            Map<String, Object> registry = new HashMap<>();
            registry.put("dataSource", "ds");
            registry.put("config", "cfg");

            AnnotationProcessing.FieldInjector.inject(service, registry);

            assertThat(service.getDataSource()).isEqualTo("ds");
            assertThat(service.getConfig()).isEqualTo("cfg");
        }

        @Test
        @DisplayName("Scanner should return empty maps for class with no annotated members")
        void testScannerWithNoAnnotatedMembers() {
            AnnotationProcessing.AnnotationScanResult result =
                    AnnotationProcessing.scanAnnotations(AnnotationProcessing.InjectableService.class);

            // InjectableService has @Inject on fields, so fieldAnnotations should not be empty
            assertThat(result.fieldAnnotations()).isNotEmpty();
            assertThat(result.classAnnotations()).isEmpty();
        }
    }
}
