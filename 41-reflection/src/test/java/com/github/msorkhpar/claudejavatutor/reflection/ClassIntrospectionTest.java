package com.github.msorkhpar.claudejavatutor.reflection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Class and Object Introspection Tests")
class ClassIntrospectionTest {

    @Nested
    @DisplayName("Class Object Acquisition")
    class ClassObjectTests {

        @Test
        @DisplayName("Should obtain Class object via getClass()")
        void testGetClass() {
            String str = "Hello";
            Class<?> clazz = str.getClass();

            assertThat(clazz.getName()).isEqualTo("java.lang.String");
            assertThat(clazz.getSimpleName()).isEqualTo("String");
        }

        @Test
        @DisplayName("Should obtain Class object via .class literal")
        void testClassLiteral() {
            Class<?> clazz = String.class;

            assertThat(clazz.getName()).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("Should obtain Class object via Class.forName()")
        void testClassForName() throws ClassNotFoundException {
            Class<?> clazz = ClassIntrospection.loadClassByName("java.util.ArrayList");

            assertThat(clazz.getSimpleName()).isEqualTo("ArrayList");
        }

        @Test
        @DisplayName("Should throw ClassNotFoundException for invalid class name")
        void testClassForNameInvalid() {
            assertThatThrownBy(() -> ClassIntrospection.loadClassByName("com.nonexistent.Bogus"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("Should obtain Class for primitive types via .class")
        void testPrimitiveClassLiterals() {
            assertThat(int.class.getName()).isEqualTo("int");
            assertThat(int.class.isPrimitive()).isTrue();
            assertThat(Integer.class.isPrimitive()).isFalse();
        }

        @Test
        @DisplayName("getClass returns three equivalent Class objects")
        void testGetClassObjectThreeWays() {
            List<Class<?>> classes = ClassIntrospection.getClassObjectThreeWays("hello");

            assertThat(classes).hasSize(3);
            assertThat(classes).allSatisfy(c -> assertThat(c).isEqualTo(String.class));
        }
    }

    @Nested
    @DisplayName("Class Name Retrieval")
    class ClassNameTests {

        @Test
        @DisplayName("Should return fully qualified class name")
        void testGetClassName() {
            String result = ClassIntrospection.getClassName("hello");
            assertThat(result).isEqualTo("java.lang.String");
        }

        @Test
        @DisplayName("Should return simple class name")
        void testGetSimpleClassName() {
            String result = ClassIntrospection.getSimpleClassName(42);
            assertThat(result).isEqualTo("Integer");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null in getClassName")
        void testGetClassNameNull() {
            assertThatThrownBy(() -> ClassIntrospection.getClassName(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw NullPointerException for null in getSimpleClassName")
        void testGetSimpleClassNameNull() {
            assertThatThrownBy(() -> ClassIntrospection.getSimpleClassName(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return inner class name correctly")
        void testInnerClassName() {
            var circle = new ClassIntrospection.Circle("red", 5.0);
            assertThat(ClassIntrospection.getSimpleClassName(circle)).isEqualTo("Circle");
        }
    }

    @Nested
    @DisplayName("Interface Discovery")
    class InterfaceTests {

        @Test
        @DisplayName("Should find directly declared interfaces on Circle")
        void testDeclaredInterfacesOnCircle() {
            List<String> interfaces = ClassIntrospection.getDeclaredInterfaces(ClassIntrospection.Circle.class);
            assertThat(interfaces).containsExactly("Resizable");
        }

        @Test
        @DisplayName("Should find directly declared interfaces on Shape")
        void testDeclaredInterfacesOnShape() {
            List<String> interfaces = ClassIntrospection.getDeclaredInterfaces(ClassIntrospection.Shape.class);
            assertThat(interfaces).containsExactly("Drawable");
        }

        @Test
        @DisplayName("Should return empty list for class with no interfaces")
        void testNoInterfaces() {
            List<String> interfaces = ClassIntrospection.getDeclaredInterfaces(Object.class);
            assertThat(interfaces).isEmpty();
        }

        @Test
        @DisplayName("Should find all interfaces including inherited ones")
        void testAllInterfaces() {
            Set<String> allInterfaces = ClassIntrospection.getAllInterfaces(ClassIntrospection.Circle.class);
            assertThat(allInterfaces).contains("Resizable", "Drawable");
        }
    }

    @Nested
    @DisplayName("Class Hierarchy")
    class HierarchyTests {

        @Test
        @DisplayName("Should return full hierarchy for Circle")
        void testCircleHierarchy() {
            List<String> hierarchy = ClassIntrospection.getClassHierarchy(ClassIntrospection.Circle.class);
            assertThat(hierarchy).containsExactly("Circle", "Shape", "Object");
        }

        @Test
        @DisplayName("Should return hierarchy for Object class")
        void testObjectHierarchy() {
            List<String> hierarchy = ClassIntrospection.getClassHierarchy(Object.class);
            assertThat(hierarchy).containsExactly("Object");
        }

        @Test
        @DisplayName("Should return hierarchy for interface")
        void testInterfaceHierarchy() {
            List<String> hierarchy = ClassIntrospection.getClassHierarchy(ClassIntrospection.Drawable.class);
            // Interfaces have no superclass
            assertThat(hierarchy).containsExactly("Drawable");
        }
    }

    @Nested
    @DisplayName("Class Modifiers and Properties")
    class ModifierTests {

        @Test
        @DisplayName("Should detect abstract class modifiers")
        void testAbstractClassModifiers() {
            String modifiers = ClassIntrospection.getClassModifiers(ClassIntrospection.Shape.class);
            assertThat(modifiers).contains("abstract");
            assertThat(modifiers).contains("public");
            assertThat(modifiers).contains("static");
        }

        @Test
        @DisplayName("Should detect final class modifiers")
        void testFinalClassModifiers() {
            String modifiers = ClassIntrospection.getClassModifiers(ClassIntrospection.ImmutablePoint.class);
            assertThat(modifiers).contains("final");
        }

        @Test
        @DisplayName("Should identify interface correctly")
        void testIsInterface() {
            Map<String, Boolean> props = ClassIntrospection.getClassProperties(ClassIntrospection.Drawable.class);
            assertThat(props.get("isInterface")).isTrue();
            assertThat(props.get("isAbstract")).isTrue(); // Interfaces are abstract
        }

        @Test
        @DisplayName("Should identify enum correctly")
        void testIsEnum() {
            Map<String, Boolean> props = ClassIntrospection.getClassProperties(Thread.State.class);
            assertThat(props.get("isEnum")).isTrue();
        }

        @Test
        @DisplayName("Should identify array correctly")
        void testIsArray() {
            Map<String, Boolean> props = ClassIntrospection.getClassProperties(int[].class);
            assertThat(props.get("isArray")).isTrue();
            assertThat(props.get("isPrimitive")).isFalse();
        }

        @Test
        @DisplayName("Should identify primitive correctly")
        void testIsPrimitive() {
            Map<String, Boolean> props = ClassIntrospection.getClassProperties(int.class);
            assertThat(props.get("isPrimitive")).isTrue();
        }

        @Test
        @DisplayName("Should identify record correctly")
        void testIsRecord() {
            record SampleRecord(String name, int age) {}
            Map<String, Boolean> props = ClassIntrospection.getClassProperties(SampleRecord.class);
            assertThat(props.get("isRecord")).isTrue();
        }
    }

    @Nested
    @DisplayName("Package Information")
    class PackageTests {

        @Test
        @DisplayName("Should return package name for standard class")
        void testPackageName() {
            String pkg = ClassIntrospection.getPackageName(String.class);
            assertThat(pkg).isEqualTo("java.lang");
        }

        @Test
        @DisplayName("Should return package name for project class")
        void testProjectPackageName() {
            String pkg = ClassIntrospection.getPackageName(ClassIntrospection.class);
            assertThat(pkg).isEqualTo("com.github.msorkhpar.claudejavatutor.reflection");
        }
    }

    @Nested
    @DisplayName("Instance Checking")
    class InstanceCheckTests {

        @Test
        @DisplayName("Should check isInstance for direct type")
        void testIsInstanceDirect() {
            ClassIntrospection.Circle circle = new ClassIntrospection.Circle("blue", 3.0);
            assertThat(ClassIntrospection.isInstanceOf(circle, ClassIntrospection.Circle.class)).isTrue();
        }

        @Test
        @DisplayName("Should check isInstance for supertype")
        void testIsInstanceSupertype() {
            ClassIntrospection.Circle circle = new ClassIntrospection.Circle("blue", 3.0);
            assertThat(ClassIntrospection.isInstanceOf(circle, ClassIntrospection.Shape.class)).isTrue();
        }

        @Test
        @DisplayName("Should check isInstance for interface")
        void testIsInstanceInterface() {
            ClassIntrospection.Circle circle = new ClassIntrospection.Circle("blue", 3.0);
            assertThat(ClassIntrospection.isInstanceOf(circle, ClassIntrospection.Drawable.class)).isTrue();
            assertThat(ClassIntrospection.isInstanceOf(circle, ClassIntrospection.Resizable.class)).isTrue();
        }

        @Test
        @DisplayName("Should return false for null object")
        void testIsInstanceNull() {
            assertThat(ClassIntrospection.isInstanceOf(null, String.class)).isFalse();
        }

        @Test
        @DisplayName("Should return false for unrelated type")
        void testIsInstanceUnrelatedType() {
            assertThat(ClassIntrospection.isInstanceOf("hello", Integer.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("Array Component Type")
    class ArrayTests {

        @Test
        @DisplayName("Should return component type of int array")
        void testIntArrayComponentType() {
            Class<?> componentType = ClassIntrospection.getArrayComponentType(int[].class);
            assertThat(componentType).isEqualTo(int.class);
        }

        @Test
        @DisplayName("Should return component type of String array")
        void testStringArrayComponentType() {
            Class<?> componentType = ClassIntrospection.getArrayComponentType(String[].class);
            assertThat(componentType).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should return null for non-array class")
        void testNonArrayComponentType() {
            Class<?> componentType = ClassIntrospection.getArrayComponentType(String.class);
            assertThat(componentType).isNull();
        }
    }

    @Nested
    @DisplayName("Field and Method Names Discovery")
    class DiscoveryTests {

        @Test
        @DisplayName("Should find declared field names of Circle")
        void testDeclaredFieldNames() {
            List<String> fields = ClassIntrospection.getDeclaredFieldNames(ClassIntrospection.Circle.class);
            assertThat(fields).contains("radius");
            // Should NOT contain inherited fields
            assertThat(fields).doesNotContain("color", "area");
        }

        @Test
        @DisplayName("Should find public method names including inherited ones")
        void testPublicMethodNames() {
            List<String> methods = ClassIntrospection.getPublicMethodNames(ClassIntrospection.Circle.class);
            assertThat(methods).contains("draw", "resize", "getRadius", "getColor");
            // Should contain Object methods
            assertThat(methods).contains("toString", "equals", "hashCode");
        }

        @Test
        @DisplayName("Should find constructor parameter counts")
        void testConstructorParameterCounts() {
            List<Integer> counts = ClassIntrospection.getConstructorParameterCounts(ClassIntrospection.Circle.class);
            assertThat(counts).contains(2); // (String, double)
        }

        @Test
        @DisplayName("Should find declared fields of Shape including private")
        void testShapeDeclaredFields() {
            List<String> fields = ClassIntrospection.getDeclaredFieldNames(ClassIntrospection.Shape.class);
            assertThat(fields).contains("color", "area");
        }
    }
}
