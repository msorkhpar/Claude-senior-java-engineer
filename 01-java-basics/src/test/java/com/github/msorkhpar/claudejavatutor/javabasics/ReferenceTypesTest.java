package com.github.msorkhpar.claudejavatutor.javabasics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;

class ReferenceTypesTest {

    @Test
    void testClassReferenceType() {
        ReferenceTypes.Person person = new ReferenceTypes.Person("Alice", 30);
        assertThat(person.getName()).isEqualTo("Alice");
        assertThat(person.getAge()).isEqualTo(30);
    }

    @Test
    void testInterfaceReferenceType() {
        ReferenceTypes.Printable printable = new ReferenceTypes.PersonRecord("Bob", 25);
        assertThat(printable).isInstanceOf(ReferenceTypes.Printable.class);
    }

    @Test
    void testEnumReferenceType() {
        ReferenceTypes.DayOfWeek day = ReferenceTypes.DayOfWeek.MONDAY;
        assertThat(day).isEqualTo(ReferenceTypes.DayOfWeek.MONDAY);
    }

    @Test
    void testRecordType() {
        ReferenceTypes.PersonRecord record = new ReferenceTypes.PersonRecord("Charlie", 35);
        assertThat(record.name()).isEqualTo("Charlie");
        assertThat(record.age()).isEqualTo(35);
    }

    @Test
    void testNullReference() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReferenceTypes.demonstrateNullReference();

        assertThat(outContent.toString()).contains("Null Pointer Exception caught");
    }

    @Test
    void testAliasing() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReferenceTypes.demonstrateAliasing();

        assertThat(outContent.toString()).contains("Person1 age: 31");
    }

    @Test
    void testPatternMatchingForInstanceof() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        ReferenceTypes.Person person = new ReferenceTypes.Person("David", 40);
        ReferenceTypes.printPersonInfo(person);

        assertThat(outContent.toString()).contains("Name: David, Age: 40");
    }

    @Test
    void testCircularReference() {
        ReferenceTypes.demonstrateCircularReference();
        // This test just ensures that the method runs without throwing an exception
    }

    @Test
    void testDeepVsShallowCopy() throws IOException, ClassNotFoundException {
        ReferenceTypes.Person original = new ReferenceTypes.Person("Eve", 28);
        original.addFriend(new ReferenceTypes.Person("Friend", 30));

        ReferenceTypes.Person shallowCopy = ReferenceTypes.shallowCopy(original);
        ReferenceTypes.Person deepCopy = ReferenceTypes.deepCopy(original);

        assertThat(shallowCopy.getName()).isEqualTo(original.getName());
        assertThat(deepCopy.getName()).isEqualTo(original.getName());

        original.setName("Modified Eve");

        assertThat(shallowCopy.getName()).isNotEqualTo(original.getName());
        assertThat(deepCopy.getName()).isNotEqualTo(original.getName());
    }

    @Test
    void testOptionalUsage() {
        ReferenceTypes.Person person = new ReferenceTypes.Person("Frank", 45);
        assertThat(ReferenceTypes.getPersonName(person)).isEqualTo("Frank");
        assertThat(ReferenceTypes.getPersonName(null)).isEqualTo("Unknown");
    }

    @Test
    void testGarbageCollection() {
        ReferenceTypes.demonstrateGarbageCollection();
        // This test just ensures that the method runs without throwing an exception
    }

    @Test
    void testSealedClasses() {
        ReferenceTypes referenceTypes = new ReferenceTypes();
        ReferenceTypes.Shape circle = referenceTypes.new Circle("Red", 5);
        ReferenceTypes.Shape square = referenceTypes.new Square("Blue", 4);
        ReferenceTypes.Shape triangle = referenceTypes.new Triangle("Green", 3, 4);

        assertThat(circle.getColor()).isEqualTo("Red");
        assertThat(square.getColor()).isEqualTo("Blue");
        assertThat(triangle.getColor()).isEqualTo("Green");

        assertThat(circle.area()).isCloseTo(78.54, within(0.01));
        assertThat(square.area()).isEqualTo(16.0);
        assertThat(triangle.area()).isEqualTo(6.0);
    }

    @Test
    void testPatternMatchingWithSealedClasses() {
        ReferenceTypes referenceTypes = new ReferenceTypes();
        ReferenceTypes.Shape circle = referenceTypes.new Circle("Red", 5);
        ReferenceTypes.Shape square = referenceTypes.new Square("Blue", 4);
        ReferenceTypes.Shape triangle = referenceTypes.new Triangle("Green", 3, 4);

        assertThat(ReferenceTypes.describeShape(circle)).isEqualTo("A circle with radius 5.0");
        assertThat(ReferenceTypes.describeShape(square)).isEqualTo("A square with side 4.0");
        assertThat(ReferenceTypes.describeShape(triangle)).isEqualTo("A triangle with area 6.0");
    }
}