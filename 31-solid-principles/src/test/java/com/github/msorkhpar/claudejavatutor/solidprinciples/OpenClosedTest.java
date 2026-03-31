package com.github.msorkhpar.claudejavatutor.solidprinciples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Open/Closed Principle Tests")
class OpenClosedTest {

    @Nested
    @DisplayName("OCP Violation Example")
    class ViolationTest {

        @Test
        @DisplayName("Violation requires switch for each shape type")
        void testViolationCalculatesAreas() {
            var calc = new OpenClosed.AreaCalculatorViolation();
            assertThat(calc.calculateArea("circle", 5.0)).isCloseTo(78.539, within(0.01));
            assertThat(calc.calculateArea("rectangle", 4.0, 6.0)).isEqualTo(24.0);
            assertThat(calc.calculateArea("triangle", 3.0, 8.0)).isEqualTo(12.0);
        }

        @Test
        @DisplayName("Violation throws on unknown shape")
        void testViolationUnknownShape() {
            var calc = new OpenClosed.AreaCalculatorViolation();
            assertThatThrownBy(() -> calc.calculateArea("hexagon", 5.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown shape");
        }
    }

    @Nested
    @DisplayName("Shape Implementations")
    class ShapeTest {

        @Test
        @DisplayName("Circle should compute area correctly")
        void testCircleArea() {
            var circle = new OpenClosed.Circle(5.0);
            assertThat(circle.area()).isCloseTo(Math.PI * 25, within(0.001));
            assertThat(circle.name()).isEqualTo("Circle");
        }

        @Test
        @DisplayName("Circle should reject negative radius")
        void testCircleNegativeRadius() {
            assertThatThrownBy(() -> new OpenClosed.Circle(-1.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Circle with zero radius should have zero area")
        void testCircleZeroRadius() {
            var circle = new OpenClosed.Circle(0.0);
            assertThat(circle.area()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Rectangle should compute area correctly")
        void testRectangleArea() {
            var rect = new OpenClosed.Rectangle(4.0, 6.0);
            assertThat(rect.area()).isEqualTo(24.0);
            assertThat(rect.name()).isEqualTo("Rectangle");
        }

        @Test
        @DisplayName("Rectangle should reject negative dimensions")
        void testRectangleNegative() {
            assertThatThrownBy(() -> new OpenClosed.Rectangle(-1.0, 5.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Triangle should compute area correctly")
        void testTriangleArea() {
            var triangle = new OpenClosed.Triangle(3.0, 8.0);
            assertThat(triangle.area()).isEqualTo(12.0);
            assertThat(triangle.name()).isEqualTo("Triangle");
        }

        @Test
        @DisplayName("Parallelogram should compute area correctly")
        void testParallelogramArea() {
            var para = new OpenClosed.Parallelogram(5.0, 3.0);
            assertThat(para.area()).isEqualTo(15.0);
            assertThat(para.name()).isEqualTo("Parallelogram");
        }

        @Test
        @DisplayName("Parallelogram should reject negative dimensions")
        void testParallelogramNegative() {
            assertThatThrownBy(() -> new OpenClosed.Parallelogram(-1.0, 5.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("AreaCalculator - OCP Applied")
    class AreaCalculatorTest {

        @Test
        @DisplayName("Should calculate total area of mixed shapes")
        void testTotalArea() {
            var calc = new OpenClosed.AreaCalculator();
            List<OpenClosed.Shape> shapes = List.of(
                    new OpenClosed.Rectangle(4.0, 6.0),
                    new OpenClosed.Triangle(3.0, 8.0)
            );
            assertThat(calc.totalArea(shapes)).isEqualTo(36.0);
        }

        @Test
        @DisplayName("Should calculate total area for empty list")
        void testTotalAreaEmpty() {
            var calc = new OpenClosed.AreaCalculator();
            assertThat(calc.totalArea(List.of())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should reject null shapes list")
        void testTotalAreaNull() {
            var calc = new OpenClosed.AreaCalculator();
            assertThatThrownBy(() -> calc.totalArea(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should group areas by shape type")
        void testAreaByType() {
            var calc = new OpenClosed.AreaCalculator();
            List<OpenClosed.Shape> shapes = List.of(
                    new OpenClosed.Rectangle(2.0, 3.0),
                    new OpenClosed.Rectangle(4.0, 5.0),
                    new OpenClosed.Circle(1.0)
            );
            Map<String, Double> byType = calc.areaByType(shapes);

            assertThat(byType).containsEntry("Rectangle", 26.0);
            assertThat(byType.get("Circle")).isCloseTo(Math.PI, within(0.001));
        }

        @Test
        @DisplayName("Should reject null for areaByType")
        void testAreaByTypeNull() {
            var calc = new OpenClosed.AreaCalculator();
            assertThatThrownBy(() -> calc.areaByType(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ConcurrentPipeline - OCP with Concurrency")
    class ConcurrentPipelineTest {

        @Test
        @DisplayName("Should process items using square processor")
        void testSquareProcessor() {
            var pipeline = new OpenClosed.ConcurrentPipeline<>(new OpenClosed.SquareProcessor());
            Long result = pipeline.processItem(5);
            assertThat(result).isEqualTo(25L);
        }

        @Test
        @DisplayName("Should process items using cube processor")
        void testCubeProcessor() {
            var pipeline = new OpenClosed.ConcurrentPipeline<>(new OpenClosed.CubeProcessor());
            Long result = pipeline.processItem(3);
            assertThat(result).isEqualTo(27L);
        }

        @Test
        @DisplayName("Should process all items concurrently")
        void testProcessAll() throws InterruptedException, ExecutionException {
            var pipeline = new OpenClosed.ConcurrentPipeline<>(new OpenClosed.SquareProcessor());
            List<Long> results = pipeline.processAll(List.of(1, 2, 3, 4));
            assertThat(results).containsExactly(1L, 4L, 9L, 16L);
        }

        @Test
        @DisplayName("Should track results")
        void testGetResults() {
            var pipeline = new OpenClosed.ConcurrentPipeline<>(new OpenClosed.SquareProcessor());
            pipeline.processItem(2);
            pipeline.processItem(3);
            assertThat(pipeline.getResults()).containsExactly(4L, 9L);
        }

        @Test
        @DisplayName("Should reject null inputs list")
        void testNullInputs() {
            var pipeline = new OpenClosed.ConcurrentPipeline<>(new OpenClosed.SquareProcessor());
            assertThatThrownBy(() -> pipeline.processAll(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject null processor")
        void testNullProcessor() {
            assertThatThrownBy(() -> new OpenClosed.ConcurrentPipeline<>(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Validator Chain - OCP with Decorators")
    class ValidatorTest {

        @Test
        @DisplayName("NonNullValidator should accept non-null strings")
        void testNonNullValidatorAccepts() {
            var validator = new OpenClosed.NonNullValidator();
            assertThat(validator.isValid("hello")).isTrue();
            assertThat(validator.isValid("")).isTrue();
            assertThat(validator.getDescription()).isEqualTo("NonNull");
        }

        @Test
        @DisplayName("NonNullValidator should reject null")
        void testNonNullValidatorRejectsNull() {
            var validator = new OpenClosed.NonNullValidator();
            assertThat(validator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("NonEmptyValidator should reject empty strings")
        void testNonEmptyValidator() {
            var validator = new OpenClosed.NonEmptyValidator(new OpenClosed.NonNullValidator());
            assertThat(validator.isValid("hello")).isTrue();
            assertThat(validator.isValid("")).isFalse();
            assertThat(validator.isValid(null)).isFalse();
            assertThat(validator.getDescription()).isEqualTo("NonNull + NonEmpty");
        }

        @Test
        @DisplayName("MinLengthValidator should reject short strings")
        void testMinLengthValidator() {
            var validator = new OpenClosed.MinLengthValidator(
                    new OpenClosed.NonEmptyValidator(new OpenClosed.NonNullValidator()), 5
            );
            assertThat(validator.isValid("hello")).isTrue();
            assertThat(validator.isValid("hi")).isFalse();
            assertThat(validator.isValid("")).isFalse();
            assertThat(validator.isValid(null)).isFalse();
            assertThat(validator.getDescription()).isEqualTo("NonNull + NonEmpty + MinLength(5)");
        }
    }
}
