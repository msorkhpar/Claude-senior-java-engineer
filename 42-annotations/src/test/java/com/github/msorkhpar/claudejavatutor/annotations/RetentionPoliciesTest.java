package com.github.msorkhpar.claudejavatutor.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Retention Policies Tests")
class RetentionPoliciesTest {

    @Nested
    @DisplayName("SOURCE Retention Policy")
    class SourceRetentionTest {

        @Test
        @DisplayName("SOURCE retention annotation should NOT be available via reflection")
        void testSourceAnnotationNotAvailable() {
            boolean present = RetentionPolicies.isSourceAnnotationPresent(
                    RetentionPolicies.DemoService.class, "sourceRetained");

            assertThat(present).isFalse();
        }

        @Test
        @DisplayName("@CompileTimeOnly should have SOURCE retention policy")
        void testCompileTimeOnlyRetention() {
            Retention retention = RetentionPolicies.CompileTimeOnly.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.SOURCE);
        }
    }

    @Nested
    @DisplayName("CLASS Retention Policy")
    class ClassRetentionTest {

        @Test
        @DisplayName("CLASS retention annotation should NOT be available via reflection")
        void testClassAnnotationNotAvailable() {
            boolean present = RetentionPolicies.isClassAnnotationPresent(
                    RetentionPolicies.DemoService.class, "classRetained");

            assertThat(present).isFalse();
        }

        @Test
        @DisplayName("@ClassLevelMeta should have CLASS retention policy")
        void testClassLevelMetaRetention() {
            Retention retention = RetentionPolicies.ClassLevelMeta.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.CLASS);
        }
    }

    @Nested
    @DisplayName("RUNTIME Retention Policy")
    class RuntimeRetentionTest {

        @Test
        @DisplayName("RUNTIME retention annotation should be available via reflection on class")
        void testRuntimeAnnotationOnClass() {
            boolean present = RetentionPolicies.hasAnnotation(
                    RetentionPolicies.DemoService.class, RetentionPolicies.RuntimeInfo.class);

            assertThat(present).isTrue();
        }

        @Test
        @DisplayName("RUNTIME annotation should have correct values on class")
        void testRuntimeAnnotationValuesOnClass() {
            Optional<RetentionPolicies.RuntimeInfo> info = RetentionPolicies.getAnnotation(
                    RetentionPolicies.DemoService.class, RetentionPolicies.RuntimeInfo.class);

            assertThat(info).isPresent();
            assertThat(info.get().description()).isEqualTo("Service demonstrating retention policies");
            assertThat(info.get().author()).isEqualTo("Tutorial");
            assertThat(info.get().priority()).isEqualTo(1);
        }

        @Test
        @DisplayName("RUNTIME annotation should be available on methods")
        void testRuntimeAnnotationOnMethod() {
            Optional<RetentionPolicies.RuntimeInfo> info = RetentionPolicies.getMethodAnnotation(
                    RetentionPolicies.DemoService.class, "runtimeRetained", RetentionPolicies.RuntimeInfo.class);

            assertThat(info).isPresent();
            assertThat(info.get().description()).isEqualTo("Fully available at runtime");
            assertThat(info.get().author()).isEqualTo("Demo");
            assertThat(info.get().priority()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return empty Optional for non-existent method")
        void testNonExistentMethod() {
            Optional<RetentionPolicies.RuntimeInfo> info = RetentionPolicies.getMethodAnnotation(
                    RetentionPolicies.DemoService.class, "nonExistent", RetentionPolicies.RuntimeInfo.class);

            assertThat(info).isEmpty();
        }

        @Test
        @DisplayName("@RuntimeInfo should have RUNTIME retention policy")
        void testRuntimeInfoRetention() {
            Retention retention = RetentionPolicies.RuntimeInfo.class.getAnnotation(Retention.class);

            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }
    }

    @Nested
    @DisplayName("Finding Annotated Methods")
    class FindAnnotatedMethodsTest {

        @Test
        @DisplayName("Should find all methods with @RuntimeInfo annotation")
        void testFindAnnotatedMethods() {
            List<String> methods = RetentionPolicies.findAnnotatedMethods(
                    RetentionPolicies.DemoService.class, RetentionPolicies.RuntimeInfo.class);

            assertThat(methods).contains("runtimeRetained");
            // sourceRetained and classRetained should NOT be found (wrong retention)
            assertThat(methods).doesNotContain("sourceRetained", "classRetained");
        }

        @Test
        @DisplayName("Should return empty list when no methods have the annotation")
        void testFindAnnotatedMethodsEmpty() {
            List<String> methods = RetentionPolicies.findAnnotatedMethods(
                    String.class, RetentionPolicies.RuntimeInfo.class);

            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllAnnotations vs getDeclaredAnnotations")
    class AnnotationInheritanceTest {

        @Test
        @DisplayName("getAllAnnotations should return RUNTIME annotations on class")
        void testGetAllAnnotations() {
            Annotation[] annotations = RetentionPolicies.getAllAnnotations(RetentionPolicies.DemoService.class);

            assertThat(annotations).isNotEmpty();
            assertThat(annotations)
                    .anyMatch(a -> a.annotationType() == RetentionPolicies.RuntimeInfo.class);
        }

        @Test
        @DisplayName("getDeclaredAnnotations should return only direct annotations")
        void testGetDeclaredAnnotations() {
            Annotation[] annotations = RetentionPolicies.getDeclaredAnnotations(RetentionPolicies.DemoService.class);

            assertThat(annotations).isNotEmpty();
        }

        @Test
        @DisplayName("SOURCE and CLASS annotations should NOT appear in getAllAnnotations")
        void testSourceAndClassNotInGetAll() {
            Annotation[] annotations = RetentionPolicies.getAllAnnotations(RetentionPolicies.DemoService.class);

            assertThat(annotations)
                    .noneMatch(a -> a.annotationType() == RetentionPolicies.CompileTimeOnly.class)
                    .noneMatch(a -> a.annotationType() == RetentionPolicies.ClassLevelMeta.class);
        }
    }

    @Nested
    @DisplayName("Parameter Annotations")
    class ParameterAnnotationTest {

        @Test
        @DisplayName("Should read @Validated annotation from method parameter")
        void testParameterAnnotation() {
            Optional<RetentionPolicies.Validated> validated = RetentionPolicies.getParameterValidation(
                    RetentionPolicies.DemoService.class, "processInput", 0);

            assertThat(validated).isPresent();
            assertThat(validated.get().message()).isEqualTo("Name required");
        }

        @Test
        @DisplayName("Should return empty for non-annotated parameter index")
        void testNonAnnotatedParameter() {
            // DemoService.sourceRetained() has no parameters
            Optional<RetentionPolicies.Validated> validated = RetentionPolicies.getParameterValidation(
                    RetentionPolicies.DemoService.class, "sourceRetained", 0);

            assertThat(validated).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for out-of-bounds parameter index")
        void testOutOfBoundsParameterIndex() {
            Optional<RetentionPolicies.Validated> validated = RetentionPolicies.getParameterValidation(
                    RetentionPolicies.DemoService.class, "processInput", 5);

            assertThat(validated).isEmpty();
        }
    }

    @Nested
    @DisplayName("Annotation Type Targets")
    class AnnotationTargetTest {

        @Test
        @DisplayName("@NonNull should have TYPE_USE target")
        void testTypeUseTarget() {
            java.lang.annotation.Target target = RetentionPolicies.NonNull.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).contains(java.lang.annotation.ElementType.TYPE_USE);
        }

        @Test
        @DisplayName("@Covariant should have TYPE_PARAMETER target")
        void testTypeParameterTarget() {
            java.lang.annotation.Target target = RetentionPolicies.Covariant.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).contains(java.lang.annotation.ElementType.TYPE_PARAMETER);
        }

        @Test
        @DisplayName("@Validated should have PARAMETER target")
        void testParameterTarget() {
            java.lang.annotation.Target target = RetentionPolicies.Validated.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).contains(java.lang.annotation.ElementType.PARAMETER);
        }

        @Test
        @DisplayName("@Immutable should have LOCAL_VARIABLE target and SOURCE retention")
        void testLocalVariableTarget() {
            java.lang.annotation.Target target = RetentionPolicies.Immutable.class.getAnnotation(java.lang.annotation.Target.class);
            Retention retention = RetentionPolicies.Immutable.class.getAnnotation(Retention.class);

            assertThat(target).isNotNull();
            assertThat(target.value()).contains(java.lang.annotation.ElementType.LOCAL_VARIABLE);
            assertThat(retention.value()).isEqualTo(RetentionPolicy.SOURCE);
        }
    }

    @Nested
    @DisplayName("hasAnnotation Utility")
    class HasAnnotationTest {

        @Test
        @DisplayName("Should return true for present RUNTIME annotation")
        void testHasAnnotationTrue() {
            assertThat(RetentionPolicies.hasAnnotation(
                    RetentionPolicies.DemoService.class, RetentionPolicies.RuntimeInfo.class)).isTrue();
        }

        @Test
        @DisplayName("Should return false for absent annotation")
        void testHasAnnotationFalse() {
            assertThat(RetentionPolicies.hasAnnotation(
                    String.class, RetentionPolicies.RuntimeInfo.class)).isFalse();
        }
    }
}
