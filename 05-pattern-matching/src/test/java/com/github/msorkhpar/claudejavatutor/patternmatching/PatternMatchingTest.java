package com.github.msorkhpar.claudejavatutor.patternmatching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

class PatternMatchingTest {

    @Test
    void processShape_withSquare_shouldReturnCorrectDescription() {
        PatternMatching.Square square = new PatternMatching.Square(5);
        String result = PatternMatching.processShape(square);
        assertThat(result).isEqualTo("Square with side length: 5.0");
    }

    @Test
    void processShape_withCircle_shouldReturnCorrectDescription() {
        PatternMatching.Circle circle = new PatternMatching.Circle(3);
        String result = PatternMatching.processShape(circle);
        assertThat(result).isEqualTo("Circle with radius: 3.0");
    }

    @Test
    void processShape_withRectangle_shouldReturnCorrectDescription() {
        PatternMatching.Rectangle rectangle = new PatternMatching.Rectangle(4, 6);
        String result = PatternMatching.processShape(rectangle);
        assertThat(result).isEqualTo("Rectangle with width: 4.0 and height: 6.0");
    }

    @Test
    void processShape_withUnknownShape_shouldReturnUnknownShape() {
        String result = PatternMatching.processShape("Not a shape");
        assertThat(result).isEqualTo("Unknown shape");
    }

    @ParameterizedTest
    @MethodSource("provideShapesForAreaCalculation")
    void calculateArea_withValidShapes_shouldReturnCorrectArea(Object shape, double expectedArea) {
        double area = PatternMatching.calculateArea(shape);
        assertThat(area).isCloseTo(expectedArea, within(0.001));
    }

    @Test
    void calculateArea_withUnknownShape_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> PatternMatching.calculateArea("Not a shape"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown shape");
    }

    private static Stream<Arguments> provideShapesForAreaCalculation() {
        return Stream.of(
                Arguments.of(new PatternMatching.Square(5), 25.0),
                Arguments.of(new PatternMatching.Circle(3), 28.274),
                Arguments.of(new PatternMatching.Rectangle(4, 6), 24.0)
        );
    }
}