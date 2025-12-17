package com.github.msorkhpar.claudejavatutor.patternmatching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import net.datafaker.Faker;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SwitchPatternTest {

    private static final Faker faker = new Faker();

    @ParameterizedTest
    @MethodSource("provideShapesForProcessing")
    void testProcessShape(Shape shape, String expected) {
        assertThat(SwitchPattern.processShape(shape)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideShapesForProcessing() {
        return Stream.of(
                Arguments.of(new Circle(5), "Circle with radius 5.0"),
                Arguments.of(new Rectangle(4, 6), "Rectangle with width 4.0 and height 6.0"),
                Arguments.of(new Triangle(3, 4), "Triangle with base 3.0 and height 4.0"),
                Arguments.of(null, "No shape provided")
        );
    }

    @ParameterizedTest
    @MethodSource("provideShapesForAreaCalculation")
    void testCalculateArea(Shape shape, double expectedArea) {
        assertThat(SwitchPattern.calculateArea(shape)).isCloseTo(expectedArea, within(0.001));
    }

    private static Stream<Arguments> provideShapesForAreaCalculation() {
        return Stream.of(
                Arguments.of(new Circle(2), Math.PI * 4),
                Arguments.of(new Rectangle(3, 4), 12.0),
                Arguments.of(new Triangle(5, 3), 7.5),
                Arguments.of(null, 0.0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideShapesForClassification")
    void testClassifyShape(Shape shape, String expectedClassification) {
        assertThat(SwitchPattern.classifyShape(shape)).isEqualTo(expectedClassification);
    }

    private static Stream<Arguments> provideShapesForClassification() {
        return Stream.of(
                Arguments.of(new Circle(3), "Small circle"),
                Arguments.of(new Circle(7), "Large circle"),
                Arguments.of(new Rectangle(5, 5), "Square"),
                Arguments.of(new Rectangle(4, 6), "Rectangle"),
                Arguments.of(new Triangle(4, 4), "Equilateral triangle"),
                Arguments.of(new Triangle(3, 5), "Triangle"),
                Arguments.of(null, "No shape")
        );
    }

    @Test
    void testProcessShapeWithRandomData() {
        Circle randomCircle = new Circle(faker.number().randomDouble(2, 1, 100));
        String result = SwitchPattern.processShape(randomCircle);
        assertThat(result).startsWith("Circle with radius").contains(String.valueOf(randomCircle.radius()));
    }

    @Test
    void testCalculateAreaWithRandomData() {
        Rectangle randomRectangle = new Rectangle(faker.number().randomDouble(2, 1, 100), faker.number().randomDouble(2, 1, 100));
        double result = SwitchPattern.calculateArea(randomRectangle);
        assertThat(result).isCloseTo(randomRectangle.width() * randomRectangle.height(), within(0.001));
    }
}