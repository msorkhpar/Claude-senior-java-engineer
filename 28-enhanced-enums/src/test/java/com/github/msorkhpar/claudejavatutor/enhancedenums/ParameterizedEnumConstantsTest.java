package com.github.msorkhpar.claudejavatutor.enhancedenums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Parameterized Enum Constants Tests")
class ParameterizedEnumConstantsTest {

    @Nested
    @DisplayName("Planet Enum")
    class PlanetTest {

        @Test
        @DisplayName("Should calculate surface gravity for Earth")
        void testEarthSurfaceGravity() {
            double gravity = ParameterizedEnumConstants.Planet.EARTH.surfaceGravity();
            assertThat(gravity).isBetween(9.7, 9.9);
        }

        @Test
        @DisplayName("Should calculate surface weight on Earth")
        void testSurfaceWeightOnEarth() {
            double weight = ParameterizedEnumConstants.Planet.EARTH.surfaceWeight(75.0);
            assertThat(weight).isBetween(730.0, 745.0);
        }

        @Test
        @DisplayName("Should find planet with strongest gravity")
        void testStrongestGravity() {
            assertThat(ParameterizedEnumConstants.Planet.strongestGravity())
                    .isEqualTo(ParameterizedEnumConstants.Planet.JUPITER);
        }

        @Test
        @DisplayName("Should find planets with gravity in range")
        void testGravityInRange() {
            List<ParameterizedEnumConstants.Planet> result =
                    ParameterizedEnumConstants.Planet.withGravityInRange(3.0, 5.0);

            assertThat(result).contains(ParameterizedEnumConstants.Planet.MARS);
            assertThat(result).doesNotContain(ParameterizedEnumConstants.Planet.JUPITER);
        }

        @Test
        @DisplayName("Should return empty list for impossible gravity range")
        void testGravityInRangeEmpty() {
            List<ParameterizedEnumConstants.Planet> result =
                    ParameterizedEnumConstants.Planet.withGravityInRange(100.0, 200.0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return mass and radius")
        void testMassAndRadius() {
            assertThat(ParameterizedEnumConstants.Planet.EARTH.mass()).isGreaterThan(0);
            assertThat(ParameterizedEnumConstants.Planet.EARTH.radius()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should have 8 planets")
        void testAllPlanets() {
            assertThat(ParameterizedEnumConstants.Planet.values()).hasSize(8);
        }
    }

    @Nested
    @DisplayName("MathOperation Enum")
    class MathOperationTest {

        @Test
        @DisplayName("Should add two numbers")
        void testAdd() {
            assertThat(ParameterizedEnumConstants.MathOperation.ADD.apply(3, 4)).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should subtract two numbers")
        void testSubtract() {
            assertThat(ParameterizedEnumConstants.MathOperation.SUBTRACT.apply(10, 3)).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should multiply two numbers")
        void testMultiply() {
            assertThat(ParameterizedEnumConstants.MathOperation.MULTIPLY.apply(3, 4)).isEqualTo(12.0);
        }

        @Test
        @DisplayName("Should divide two numbers")
        void testDivide() {
            assertThat(ParameterizedEnumConstants.MathOperation.DIVIDE.apply(10, 4)).isEqualTo(2.5);
        }

        @Test
        @DisplayName("Should throw on division by zero")
        void testDivideByZero() {
            assertThatThrownBy(() -> ParameterizedEnumConstants.MathOperation.DIVIDE.apply(10, 0))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("Division by zero");
        }

        @Test
        @DisplayName("Should throw on modulus by zero")
        void testModulusByZero() {
            assertThatThrownBy(() -> ParameterizedEnumConstants.MathOperation.MODULUS.apply(10, 0))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("Should compute modulus correctly")
        void testModulus() {
            assertThat(ParameterizedEnumConstants.MathOperation.MODULUS.apply(10, 3)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should find operation by symbol")
        void testFromSymbol() {
            assertThat(ParameterizedEnumConstants.MathOperation.fromSymbol("+"))
                    .contains(ParameterizedEnumConstants.MathOperation.ADD);
            assertThat(ParameterizedEnumConstants.MathOperation.fromSymbol("^"))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should format computation string")
        void testFormat() {
            String result = ParameterizedEnumConstants.MathOperation.ADD.format(3, 4);
            assertThat(result).isEqualTo("3.00 + 4.00 = 7.00");
        }

        @Test
        @DisplayName("Should have correct symbols")
        void testSymbols() {
            assertThat(ParameterizedEnumConstants.MathOperation.ADD.symbol()).isEqualTo("+");
            assertThat(ParameterizedEnumConstants.MathOperation.SUBTRACT.symbol()).isEqualTo("-");
            assertThat(ParameterizedEnumConstants.MathOperation.MULTIPLY.symbol()).isEqualTo("*");
            assertThat(ParameterizedEnumConstants.MathOperation.DIVIDE.symbol()).isEqualTo("/");
            assertThat(ParameterizedEnumConstants.MathOperation.MODULUS.symbol()).isEqualTo("%");
        }

        @Test
        @DisplayName("Should handle negative numbers")
        void testNegativeNumbers() {
            assertThat(ParameterizedEnumConstants.MathOperation.ADD.apply(-3, -4)).isEqualTo(-7.0);
            assertThat(ParameterizedEnumConstants.MathOperation.MULTIPLY.apply(-3, 4)).isEqualTo(-12.0);
        }
    }

    @Nested
    @DisplayName("TextTransform Enum")
    class TextTransformTest {

        @Test
        @DisplayName("Should transform to upper case")
        void testUpperCase() {
            assertThat(ParameterizedEnumConstants.TextTransform.UPPER_CASE.apply("hello"))
                    .isEqualTo("HELLO");
        }

        @Test
        @DisplayName("Should transform to lower case")
        void testLowerCase() {
            assertThat(ParameterizedEnumConstants.TextTransform.LOWER_CASE.apply("HELLO"))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("Should trim whitespace")
        void testTrim() {
            assertThat(ParameterizedEnumConstants.TextTransform.TRIM.apply("  hello  "))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("Should reverse string")
        void testReverse() {
            assertThat(ParameterizedEnumConstants.TextTransform.REVERSE.apply("hello"))
                    .isEqualTo("olleh");
        }

        @Test
        @DisplayName("Should capitalize first letter")
        void testCapitalize() {
            assertThat(ParameterizedEnumConstants.TextTransform.CAPITALIZE.apply("hello world"))
                    .isEqualTo("Hello world");
        }

        @Test
        @DisplayName("Should handle empty string in capitalize")
        void testCapitalizeEmpty() {
            assertThat(ParameterizedEnumConstants.TextTransform.CAPITALIZE.apply(""))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should convert to snake case")
        void testSnakeCase() {
            assertThat(ParameterizedEnumConstants.TextTransform.SNAKE_CASE.apply("Hello World"))
                    .isEqualTo("hello_world");
        }

        @Test
        @DisplayName("NO_OP should return input unchanged")
        void testNoOp() {
            assertThat(ParameterizedEnumConstants.TextTransform.NO_OP.apply("hello"))
                    .isEqualTo("hello");
        }

        @Test
        @DisplayName("Should throw on null input")
        void testNullInput() {
            assertThatThrownBy(() -> ParameterizedEnumConstants.TextTransform.UPPER_CASE.apply(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should chain multiple transforms")
        void testApplyAll() {
            String result = ParameterizedEnumConstants.TextTransform.applyAll(
                    "  Hello World  ",
                    ParameterizedEnumConstants.TextTransform.TRIM,
                    ParameterizedEnumConstants.TextTransform.UPPER_CASE
            );
            assertThat(result).isEqualTo("HELLO WORLD");
        }

        @Test
        @DisplayName("applyAll should throw on null input")
        void testApplyAllNull() {
            assertThatThrownBy(() -> ParameterizedEnumConstants.TextTransform.applyAll(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should have display names")
        void testDisplayNames() {
            assertThat(ParameterizedEnumConstants.TextTransform.UPPER_CASE.displayName())
                    .isEqualTo("Upper Case");
        }
    }

    @Nested
    @DisplayName("HttpStatus Enum")
    class HttpStatusTest {

        @Test
        @DisplayName("Should categorize success statuses")
        void testSuccessStatuses() {
            assertThat(ParameterizedEnumConstants.HttpStatus.OK.isSuccess()).isTrue();
            assertThat(ParameterizedEnumConstants.HttpStatus.CREATED.isSuccess()).isTrue();
            assertThat(ParameterizedEnumConstants.HttpStatus.NO_CONTENT.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should categorize client error statuses")
        void testClientErrors() {
            assertThat(ParameterizedEnumConstants.HttpStatus.BAD_REQUEST.isClientError()).isTrue();
            assertThat(ParameterizedEnumConstants.HttpStatus.NOT_FOUND.isClientError()).isTrue();
            assertThat(ParameterizedEnumConstants.HttpStatus.NOT_FOUND.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Should categorize server error statuses")
        void testServerErrors() {
            assertThat(ParameterizedEnumConstants.HttpStatus.INTERNAL_SERVER_ERROR.isServerError()).isTrue();
            assertThat(ParameterizedEnumConstants.HttpStatus.SERVICE_UNAVAILABLE.isServerError()).isTrue();
        }

        @Test
        @DisplayName("Should look up status by code")
        void testFromCode() {
            assertThat(ParameterizedEnumConstants.HttpStatus.fromCode(200))
                    .contains(ParameterizedEnumConstants.HttpStatus.OK);
            assertThat(ParameterizedEnumConstants.HttpStatus.fromCode(404))
                    .contains(ParameterizedEnumConstants.HttpStatus.NOT_FOUND);
            assertThat(ParameterizedEnumConstants.HttpStatus.fromCode(999))
                    .isEmpty();
        }

        @Test
        @DisplayName("Should filter by category")
        void testByCategory() {
            List<ParameterizedEnumConstants.HttpStatus> serverErrors =
                    ParameterizedEnumConstants.HttpStatus.byCategory(
                            ParameterizedEnumConstants.HttpStatus.Category.SERVER_ERROR);

            assertThat(serverErrors).hasSize(3);
            assertThat(serverErrors).allMatch(ParameterizedEnumConstants.HttpStatus::isServerError);
        }

        @Test
        @DisplayName("Should have correct code and reason")
        void testCodeAndReason() {
            assertThat(ParameterizedEnumConstants.HttpStatus.OK.code()).isEqualTo(200);
            assertThat(ParameterizedEnumConstants.HttpStatus.OK.reason()).isEqualTo("OK");
        }
    }

    @Nested
    @DisplayName("WorkSchedule Enum")
    class WorkScheduleTest {

        @Test
        @DisplayName("Should identify work days")
        void testWorkDays() {
            assertThat(ParameterizedEnumConstants.WorkSchedule.MONDAY.isWorkDay()).isTrue();
            assertThat(ParameterizedEnumConstants.WorkSchedule.SATURDAY.isWorkDay()).isTrue();
            assertThat(ParameterizedEnumConstants.WorkSchedule.SUNDAY.isWorkDay()).isFalse();
        }

        @Test
        @DisplayName("Should identify required days")
        void testRequiredDays() {
            List<ParameterizedEnumConstants.WorkSchedule> required =
                    ParameterizedEnumConstants.WorkSchedule.requiredDays();

            assertThat(required).hasSize(5);
            assertThat(required).doesNotContain(
                    ParameterizedEnumConstants.WorkSchedule.SATURDAY,
                    ParameterizedEnumConstants.WorkSchedule.SUNDAY
            );
        }

        @Test
        @DisplayName("Should return Optional start/end times")
        void testOptionalTimes() {
            assertThat(ParameterizedEnumConstants.WorkSchedule.MONDAY.startTime())
                    .contains("09:00");
            assertThat(ParameterizedEnumConstants.WorkSchedule.SUNDAY.startTime())
                    .isEmpty();
            assertThat(ParameterizedEnumConstants.WorkSchedule.SUNDAY.endTime())
                    .isEmpty();
        }

        @Test
        @DisplayName("Should find schedule for day of week")
        void testForDayOfWeek() {
            assertThat(ParameterizedEnumConstants.WorkSchedule.forDayOfWeek(DayOfWeek.FRIDAY))
                    .isEqualTo(ParameterizedEnumConstants.WorkSchedule.FRIDAY);
        }

        @Test
        @DisplayName("Friday should have earlier end time")
        void testFridayEndTime() {
            assertThat(ParameterizedEnumConstants.WorkSchedule.FRIDAY.endTime())
                    .contains("16:00");
        }

        @Test
        @DisplayName("forToday should return a valid schedule")
        void testForToday() {
            ParameterizedEnumConstants.WorkSchedule today =
                    ParameterizedEnumConstants.WorkSchedule.forToday();
            assertThat(today).isNotNull();
            assertThat(today.dayOfWeek()).isEqualTo(java.time.LocalDate.now().getDayOfWeek());
        }
    }
}
