package com.github.msorkhpar.claudejavatutor.patternmatching;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import net.datafaker.Faker;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RecordPatternTest {

    private static final Faker faker = new Faker();

    @Test
    void describeShape_shouldDescribeRectangle() {
        var rectangle = new RecordPattern.Rectangle(
                new RecordPattern.Point(1, 2),
                new RecordPattern.Point(4, 6)
        );
        assertThat(RecordPattern.describeShape(rectangle))
                .isEqualTo("Rectangle from (1,2) to (4,6)");
    }

    @Test
    void describeShape_shouldDescribeCircle() {
        var circle = new RecordPattern.Circle(
                new RecordPattern.Point(3, 4),
                5
        );
        assertThat(RecordPattern.describeShape(circle))
                .isEqualTo("Circle at (3,4) with radius 5");
    }

    @Test
    void describeShape_shouldHandleUnknownShape() {
        assertThat(RecordPattern.describeShape("Not a shape"))
                .isEqualTo("Unknown shape");
    }

    @ParameterizedTest
    @MethodSource("provideRectangles")
    void isUnitSquare_shouldCorrectlyIdentifyUnitSquares(RecordPattern.Rectangle rectangle, boolean expected) {
        assertThat(RecordPattern.isUnitSquare(rectangle)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideRectangles() {
        return Stream.of(
                Arguments.of(new RecordPattern.Rectangle(new RecordPattern.Point(0, 0), new RecordPattern.Point(1, 1)), true),
                Arguments.of(new RecordPattern.Rectangle(new RecordPattern.Point(2, 2), new RecordPattern.Point(3, 3)), true),
                Arguments.of(new RecordPattern.Rectangle(new RecordPattern.Point(0, 0), new RecordPattern.Point(2, 2)), false),
                Arguments.of(new RecordPattern.Rectangle(new RecordPattern.Point(0, 0), new RecordPattern.Point(1, 2)), false)
        );
    }

    @Test
    void describeShape_withRandomData_shouldNotThrowException() {
        for (int i = 0; i < 100; i++) {
            var shape = faker.options().option(
                    new RecordPattern.Rectangle(
                            new RecordPattern.Point(faker.number().numberBetween(-100, 100), faker.number().numberBetween(-100, 100)),
                            new RecordPattern.Point(faker.number().numberBetween(-100, 100), faker.number().numberBetween(-100, 100))
                    ),
                    new RecordPattern.Circle(
                            new RecordPattern.Point(faker.number().numberBetween(-100, 100), faker.number().numberBetween(-100, 100)),
                            faker.number().numberBetween(1, 100)
                    ),
                    "Random String"
            );
            assertThat(RecordPattern.describeShape(shape)).isNotNull();
        }
    }
}