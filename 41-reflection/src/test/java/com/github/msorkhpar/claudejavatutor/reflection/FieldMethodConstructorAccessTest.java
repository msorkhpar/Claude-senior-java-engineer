package com.github.msorkhpar.claudejavatutor.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Accessing Fields, Methods, and Constructors Tests")
class FieldMethodConstructorAccessTest {

    @BeforeEach
    void resetPersonCount() {
        FieldMethodConstructorAccess.Person.resetCount();
    }

    @Nested
    @DisplayName("Field Access")
    class FieldAccessTests {

        @Test
        @DisplayName("Should read private field value")
        void testReadPrivateField() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            Object name = FieldMethodConstructorAccess.readField(person, "name");

            assertThat(name).isEqualTo("Alice");
        }

        @Test
        @DisplayName("Should read public field value")
        void testReadPublicField() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);
            person.email = "alice@example.com";

            Object email = FieldMethodConstructorAccess.readField(person, "email");

            assertThat(email).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("Should write private field value")
        void testWritePrivateField() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            FieldMethodConstructorAccess.writeField(person, "name", "Bob");

            assertThat(person.getName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("Should write private int field")
        void testWritePrivateIntField() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            FieldMethodConstructorAccess.writeField(person, "age", 25);

            assertThat(person.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("Should throw NoSuchFieldException for non-existent field")
        void testReadNonExistentField() {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            assertThatThrownBy(() -> FieldMethodConstructorAccess.readField(person, "nonExistent"))
                    .isInstanceOf(NoSuchFieldException.class);
        }

        @Test
        @DisplayName("Should read static field value")
        void testReadStaticField() throws Exception {
            new FieldMethodConstructorAccess.Person("Alice", 30);

            Object count = FieldMethodConstructorAccess.readStaticField(
                    FieldMethodConstructorAccess.Person.class, "instanceCount");

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Should describe fields with types and modifiers")
        void testDescribeFields() {
            List<String> descriptions = FieldMethodConstructorAccess.describeFields(
                    FieldMethodConstructorAccess.Person.class);

            assertThat(descriptions).anyMatch(d -> d.contains("private") && d.contains("String") && d.contains("name"));
            assertThat(descriptions).anyMatch(d -> d.contains("private") && d.contains("int") && d.contains("age"));
            assertThat(descriptions).anyMatch(d -> d.contains("public") && d.contains("String") && d.contains("email"));
            assertThat(descriptions).anyMatch(d -> d.contains("protected") && d.contains("String") && d.contains("nickname"));
        }

        @Test
        @DisplayName("Should filter fields by modifier")
        void testGetFieldsByModifier() {
            List<String> privateFields = FieldMethodConstructorAccess.getFieldsByModifier(
                    FieldMethodConstructorAccess.Person.class, Modifier.PRIVATE);

            assertThat(privateFields).contains("name", "age", "instanceCount");
            assertThat(privateFields).doesNotContain("email", "nickname");
        }

        @Test
        @DisplayName("Should get public fields by modifier")
        void testGetPublicFieldsByModifier() {
            List<String> publicFields = FieldMethodConstructorAccess.getFieldsByModifier(
                    FieldMethodConstructorAccess.Person.class, Modifier.PUBLIC);

            assertThat(publicFields).contains("email");
        }
    }

    @Nested
    @DisplayName("Method Invocation")
    class MethodInvocationTests {

        @Test
        @DisplayName("Should invoke public method with argument")
        void testInvokePublicMethod() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            Object result = FieldMethodConstructorAccess.invokeMethod(
                    person, "greet", new Class<?>[]{String.class}, "Hi");

            assertThat(result).isEqualTo("Hi, Alice!");
        }

        @Test
        @DisplayName("Should invoke private method")
        void testInvokePrivateMethod() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            Object result = FieldMethodConstructorAccess.invokeMethod(
                    person, "formatInfo", new Class<?>[]{});

            assertThat(result).isEqualTo("Alice (30)");
        }

        @Test
        @DisplayName("Should invoke method with primitive parameters")
        void testInvokeMethodWithPrimitives() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            Object result = FieldMethodConstructorAccess.invokeMethod(
                    person, "add", new Class<?>[]{int.class, int.class}, 5, 3);

            assertThat(result).isEqualTo(8);
        }

        @Test
        @DisplayName("Should invoke private method with argument")
        void testInvokePrivateMethodWithArg() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            Object result = FieldMethodConstructorAccess.invokeMethod(
                    person, "secretMethod", new Class<?>[]{String.class}, "password");

            assertThat(result).isEqualTo("Secret: password");
        }

        @Test
        @DisplayName("Should invoke static method")
        void testInvokeStaticMethod() throws Exception {
            new FieldMethodConstructorAccess.Person("Alice", 30);
            new FieldMethodConstructorAccess.Person("Bob", 25);

            Object count = FieldMethodConstructorAccess.invokeStaticMethod(
                    FieldMethodConstructorAccess.Person.class, "getInstanceCount", new Class<?>[]{});

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw NoSuchMethodException for non-existent method")
        void testInvokeNonExistentMethod() {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);

            assertThatThrownBy(() -> FieldMethodConstructorAccess.invokeMethod(
                    person, "nonExistent", new Class<?>[]{}))
                    .isInstanceOf(NoSuchMethodException.class);
        }

        @Test
        @DisplayName("Should describe method signatures")
        void testDescribeMethodSignatures() {
            List<String> signatures = FieldMethodConstructorAccess.describeMethodSignatures(
                    FieldMethodConstructorAccess.Person.class);

            assertThat(signatures).anyMatch(s -> s.contains("greet") && s.contains("String"));
            assertThat(signatures).anyMatch(s -> s.contains("getName"));
            assertThat(signatures).anyMatch(s -> s.contains("formatInfo"));
        }
    }

    @Nested
    @DisplayName("Constructor Access")
    class ConstructorAccessTests {

        @Test
        @DisplayName("Should create instance using no-arg constructor")
        void testCreateInstanceNoArg() throws Exception {
            var person = FieldMethodConstructorAccess.createInstanceNoArg(
                    FieldMethodConstructorAccess.Person.class);

            assertThat(person).isNotNull();
            assertThat(person.getName()).isEqualTo("Unknown");
            assertThat(person.getAge()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create instance using parameterized constructor")
        void testCreateInstanceWithArgs() throws Exception {
            var person = FieldMethodConstructorAccess.createInstance(
                    FieldMethodConstructorAccess.Person.class,
                    new Class<?>[]{String.class, int.class},
                    "Alice", 30);

            assertThat(person.getName()).isEqualTo("Alice");
            assertThat(person.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should create instance using private constructor")
        void testCreateInstancePrivateConstructor() throws Exception {
            var person = FieldMethodConstructorAccess.createInstance(
                    FieldMethodConstructorAccess.Person.class,
                    new Class<?>[]{String.class},
                    "Alice");

            assertThat(person.getName()).isEqualTo("Alice");
            assertThat(person.getAge()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should throw NoSuchMethodException for non-existent constructor")
        void testCreateInstanceNonExistentConstructor() {
            assertThatThrownBy(() -> FieldMethodConstructorAccess.createInstance(
                    FieldMethodConstructorAccess.Person.class,
                    new Class<?>[]{double.class},
                    3.14))
                    .isInstanceOf(NoSuchMethodException.class);
        }

        @Test
        @DisplayName("Should describe all constructors")
        void testDescribeConstructors() {
            List<String> descriptions = FieldMethodConstructorAccess.describeConstructors(
                    FieldMethodConstructorAccess.Person.class);

            assertThat(descriptions).hasSize(3);
            assertThat(descriptions).anyMatch(d -> d.contains("Person()"));
            assertThat(descriptions).anyMatch(d -> d.contains("Person(String, int)"));
            assertThat(descriptions).anyMatch(d -> d.contains("Person(String)"));
        }
    }

    @Nested
    @DisplayName("Final Field Modification")
    class FinalFieldTests {

        @Test
        @DisplayName("Should modify final field value via reflection")
        void testModifyFinalField() throws Exception {
            var config = new FieldMethodConstructorAccess.ImmutableConfig("localhost", 8080);
            assertThat(config.getHost()).isEqualTo("localhost");

            FieldMethodConstructorAccess.writeFinalField(config, "host", "remotehost");

            // Reading via reflection since the JVM may inline final fields
            Object newHost = FieldMethodConstructorAccess.readField(config, "host");
            assertThat(newHost).isEqualTo("remotehost");
        }

        @Test
        @DisplayName("Should modify final int field value via reflection")
        void testModifyFinalIntField() throws Exception {
            var config = new FieldMethodConstructorAccess.ImmutableConfig("localhost", 8080);

            FieldMethodConstructorAccess.writeFinalField(config, "port", 9090);

            Object newPort = FieldMethodConstructorAccess.readField(config, "port");
            assertThat(newPort).isEqualTo(9090);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null field value")
        void testReadNullFieldValue() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);
            // email is not set, should be null
            Object email = FieldMethodConstructorAccess.readField(person, "email");
            assertThat(email).isNull();
        }

        @Test
        @DisplayName("Should write null to field")
        void testWriteNullToField() throws Exception {
            var person = new FieldMethodConstructorAccess.Person("Alice", 30);
            FieldMethodConstructorAccess.writeField(person, "name", null);
            assertThat(person.getName()).isNull();
        }

        @Test
        @DisplayName("Should handle inherited getDeclaredField - not finding inherited fields")
        void testGetDeclaredFieldDoesNotFindInherited() {
            // getDeclaredField only finds fields declared in the class itself
            assertThatThrownBy(() -> FieldMethodConstructorAccess.Person.class
                    .getDeclaredField("nonExistent"))
                    .isInstanceOf(NoSuchFieldException.class);
        }
    }
}
