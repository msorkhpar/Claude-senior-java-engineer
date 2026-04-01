package com.github.msorkhpar.claudejavatutor.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Custom Annotation Creation Tests")
class CustomAnnotationCreationTest {

    @Nested
    @DisplayName("Marker Annotation (@ThreadSafe)")
    class MarkerAnnotationTest {

        @Test
        @DisplayName("@ThreadSafe should be present on annotated class")
        void testMarkerAnnotationPresent() {
            assertThat(CustomAnnotationCreation.UserService.class
                    .isAnnotationPresent(CustomAnnotationCreation.ThreadSafe.class)).isTrue();
        }

        @Test
        @DisplayName("@ThreadSafe should NOT be present on unannotated class")
        void testMarkerAnnotationAbsent() {
            assertThat(String.class
                    .isAnnotationPresent(CustomAnnotationCreation.ThreadSafe.class)).isFalse();
        }

        @Test
        @DisplayName("Marker annotation should have no elements")
        void testMarkerAnnotationHasNoElements() {
            Method[] methods = CustomAnnotationCreation.ThreadSafe.class.getDeclaredMethods();

            assertThat(methods).isEmpty();
        }
    }

    @Nested
    @DisplayName("Single-Value Annotation (@Author)")
    class SingleValueAnnotationTest {

        @Test
        @DisplayName("@Author should have correct value")
        void testAuthorAnnotationValue() {
            CustomAnnotationCreation.Author author =
                    CustomAnnotationCreation.UserService.class.getAnnotation(CustomAnnotationCreation.Author.class);

            assertThat(author).isNotNull();
            assertThat(author.value()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("@Author annotation should be absent on non-annotated class")
        void testAuthorAnnotationAbsent() {
            CustomAnnotationCreation.Author author =
                    CustomAnnotationCreation.AdminService.class.getDeclaredAnnotation(CustomAnnotationCreation.Author.class);

            assertThat(author).isNull();
        }
    }

    @Nested
    @DisplayName("Multi-Value Annotation (@ApiEndpoint)")
    class MultiValueAnnotationTest {

        @Test
        @DisplayName("@ApiEndpoint should have all specified values")
        void testApiEndpointWithAllValues() {
            CustomAnnotationCreation.ApiEndpoint endpoint =
                    CustomAnnotationCreation.getEndpointInfo(CustomAnnotationCreation.UserService.class, "listUsers");

            assertThat(endpoint).isNotNull();
            assertThat(endpoint.path()).isEqualTo("/users");
            assertThat(endpoint.method()).isEqualTo("GET");
            assertThat(endpoint.description()).isEqualTo("List all users");
            assertThat(endpoint.version()).isEqualTo(2);
            assertThat(endpoint.produces()).containsExactly("application/json");
        }

        @Test
        @DisplayName("@ApiEndpoint should use default values for unspecified elements")
        void testApiEndpointWithDefaults() {
            CustomAnnotationCreation.ApiEndpoint endpoint =
                    CustomAnnotationCreation.getEndpointInfo(CustomAnnotationCreation.UserService.class, "createUser");

            assertThat(endpoint).isNotNull();
            assertThat(endpoint.method()).isEqualTo("POST");
            assertThat(endpoint.description()).isEmpty(); // default ""
            assertThat(endpoint.version()).isEqualTo(1); // default 1
            assertThat(endpoint.produces()).containsExactly("application/json", "application/xml");
        }

        @Test
        @DisplayName("@ApiEndpoint should return null for non-annotated method")
        void testApiEndpointOnNonAnnotatedMethod() {
            CustomAnnotationCreation.ApiEndpoint endpoint =
                    CustomAnnotationCreation.getEndpointInfo(CustomAnnotationCreation.UserService.class, "getName");

            assertThat(endpoint).isNull();
        }

        @Test
        @DisplayName("@ApiEndpoint should return null for non-existent method")
        void testApiEndpointOnNonExistentMethod() {
            CustomAnnotationCreation.ApiEndpoint endpoint =
                    CustomAnnotationCreation.getEndpointInfo(CustomAnnotationCreation.UserService.class, "nonExistent");

            assertThat(endpoint).isNull();
        }
    }

    @Nested
    @DisplayName("Repeatable Annotation (@Role)")
    class RepeatableAnnotationTest {

        @Test
        @DisplayName("Should read multiple @Role annotations from listUsers method")
        void testMultipleRoles() {
            CustomAnnotationCreation.Role[] roles =
                    CustomAnnotationCreation.getRoles(CustomAnnotationCreation.UserService.class, "listUsers");

            assertThat(roles).hasSize(2);
            assertThat(Arrays.stream(roles).map(CustomAnnotationCreation.Role::value))
                    .containsExactly("ADMIN", "MANAGER");
        }

        @Test
        @DisplayName("Should read single @Role annotation from createUser method")
        void testSingleRole() {
            CustomAnnotationCreation.Role[] roles =
                    CustomAnnotationCreation.getRoles(CustomAnnotationCreation.UserService.class, "createUser");

            assertThat(roles).hasSize(1);
            assertThat(roles[0].value()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Should return empty array for non-annotated method")
        void testNoRoles() {
            CustomAnnotationCreation.Role[] roles =
                    CustomAnnotationCreation.getRoles(CustomAnnotationCreation.UserService.class, "getName");

            assertThat(roles).isEmpty();
        }

        @Test
        @DisplayName("Should return empty array for non-existent method")
        void testRolesForNonExistentMethod() {
            CustomAnnotationCreation.Role[] roles =
                    CustomAnnotationCreation.getRoles(CustomAnnotationCreation.UserService.class, "missing");

            assertThat(roles).isEmpty();
        }
    }

    @Nested
    @DisplayName("Field Annotations (@NotEmpty, @Range)")
    class FieldAnnotationTest {

        @Test
        @DisplayName("@NotEmpty should be present on name field")
        void testNotEmptyAnnotationPresent() throws NoSuchFieldException {
            Field nameField = CustomAnnotationCreation.UserService.class.getDeclaredField("name");
            CustomAnnotationCreation.NotEmpty notEmpty = nameField.getAnnotation(CustomAnnotationCreation.NotEmpty.class);

            assertThat(notEmpty).isNotNull();
            assertThat(notEmpty.message()).isEqualTo("Name is required");
        }

        @Test
        @DisplayName("@Range should have correct min and max on age field")
        void testRangeAnnotationValues() throws NoSuchFieldException {
            Field ageField = CustomAnnotationCreation.UserService.class.getDeclaredField("age");
            CustomAnnotationCreation.Range range = ageField.getAnnotation(CustomAnnotationCreation.Range.class);

            assertThat(range).isNotNull();
            assertThat(range.min()).isEqualTo(0);
            assertThat(range.max()).isEqualTo(150);
            assertThat(range.message()).isEqualTo("Age must be between 0 and 150");
        }
    }

    @Nested
    @DisplayName("@Inherited Annotation (@Auditable)")
    class InheritedAnnotationTest {

        @Test
        @DisplayName("Parent class should have @Auditable directly")
        void testParentHasAuditable() {
            CustomAnnotationCreation.Auditable auditable =
                    CustomAnnotationCreation.UserService.class.getAnnotation(CustomAnnotationCreation.Auditable.class);

            assertThat(auditable).isNotNull();
            assertThat(auditable.level()).isEqualTo("DEBUG");
        }

        @Test
        @DisplayName("Subclass should inherit @Auditable from parent")
        void testSubclassInheritsAuditable() {
            CustomAnnotationCreation.Auditable auditable =
                    CustomAnnotationCreation.AdminService.class.getAnnotation(CustomAnnotationCreation.Auditable.class);

            assertThat(auditable).isNotNull();
            assertThat(auditable.level()).isEqualTo("DEBUG");
        }

        @Test
        @DisplayName("Non-inherited annotation should NOT be on subclass")
        void testNonInheritedAnnotationNotOnSubclass() {
            // @Author is NOT @Inherited, so AdminService should not have it
            CustomAnnotationCreation.Author author =
                    CustomAnnotationCreation.AdminService.class.getAnnotation(CustomAnnotationCreation.Author.class);

            assertThat(author).isNull();
        }
    }

    @Nested
    @DisplayName("@Documented Annotation (@ApiVersion)")
    class DocumentedAnnotationTest {

        @Test
        @DisplayName("@ApiVersion should have correct major and minor values")
        void testApiVersionValues() {
            CustomAnnotationCreation.ApiVersion version =
                    CustomAnnotationCreation.UserService.class.getAnnotation(CustomAnnotationCreation.ApiVersion.class);

            assertThat(version).isNotNull();
            assertThat(version.major()).isEqualTo(2);
            assertThat(version.minor()).isEqualTo(1);
        }

        @Test
        @DisplayName("@ApiVersion should itself be annotated with @Documented")
        void testApiVersionIsDocumented() {
            assertThat(CustomAnnotationCreation.ApiVersion.class
                    .isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Validation Processor")
    class ValidationProcessorTest {

        @Test
        @DisplayName("Should pass validation for valid object")
        void testValidObject() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("Alice", 30);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for empty name")
        void testEmptyNameValidation() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("", 30);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).fieldName()).isEqualTo("name");
            assertThat(errors.get(0).message()).isEqualTo("Name is required");
        }

        @Test
        @DisplayName("Should fail validation for null name")
        void testNullNameValidation() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService(null, 30);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).fieldName()).isEqualTo("name");
        }

        @Test
        @DisplayName("Should fail validation for age out of range (negative)")
        void testNegativeAgeValidation() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("Alice", -1);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).fieldName()).isEqualTo("age");
            assertThat(errors.get(0).message()).isEqualTo("Age must be between 0 and 150");
        }

        @Test
        @DisplayName("Should fail validation for age out of range (too high)")
        void testTooHighAgeValidation() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("Alice", 200);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).fieldName()).isEqualTo("age");
        }

        @Test
        @DisplayName("Should report multiple validation errors")
        void testMultipleValidationErrors() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("", -5);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(2);
        }

        @Test
        @DisplayName("Should pass validation for boundary age values")
        void testBoundaryAgeValues() {
            CustomAnnotationCreation.UserService serviceZero = new CustomAnnotationCreation.UserService("A", 0);
            CustomAnnotationCreation.UserService serviceMax = new CustomAnnotationCreation.UserService("B", 150);

            assertThat(CustomAnnotationCreation.validate(serviceZero)).isEmpty();
            assertThat(CustomAnnotationCreation.validate(serviceMax)).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation for whitespace-only name")
        void testWhitespaceNameValidation() {
            CustomAnnotationCreation.UserService service = new CustomAnnotationCreation.UserService("   ", 25);

            List<CustomAnnotationCreation.ValidationError> errors = CustomAnnotationCreation.validate(service);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).fieldName()).isEqualTo("name");
        }
    }
}
