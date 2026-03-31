package com.github.msorkhpar.claudejavatutor.enhancedenums;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Demonstrates parameterized enum constants - enums whose constants carry constructor parameters,
 * abstract method implementations, and behavior that varies per constant.
 */
public class ParameterizedEnumConstants {

    // --- Example 1: Enum with constructor parameters and behavior ---

    /**
     * Planet enum with physical properties and computed values.
     */
    public enum Planet {
        MERCURY(3.303e+23, 2.4397e6),
        VENUS(4.869e+24, 6.0518e6),
        EARTH(5.976e+24, 6.37814e6),
        MARS(6.421e+23, 3.3972e6),
        JUPITER(1.9e+27, 7.1492e7),
        SATURN(5.688e+26, 6.0268e7),
        URANUS(8.686e+25, 2.5559e7),
        NEPTUNE(1.024e+26, 2.4746e7);

        private final double mass;    // in kilograms
        private final double radius;  // in meters

        static final double G = 6.67300E-11;

        Planet(double mass, double radius) {
            this.mass = mass;
            this.radius = radius;
        }

        public double mass() {
            return mass;
        }

        public double radius() {
            return radius;
        }

        public double surfaceGravity() {
            return G * mass / (radius * radius);
        }

        public double surfaceWeight(double otherMass) {
            return otherMass * surfaceGravity();
        }

        /**
         * Returns the planet with the strongest gravity.
         */
        public static Planet strongestGravity() {
            return Arrays.stream(values())
                    .max(Comparator.comparingDouble(Planet::surfaceGravity))
                    .orElseThrow();
        }

        /**
         * Finds planets with surface gravity within a given range.
         */
        public static List<Planet> withGravityInRange(double min, double max) {
            return Arrays.stream(values())
                    .filter(p -> p.surfaceGravity() >= min && p.surfaceGravity() <= max)
                    .collect(Collectors.toList());
        }
    }

    // --- Example 2: Enum with abstract methods per constant ---

    /**
     * Arithmetic operation enum where each constant implements its own behavior.
     */
    public enum MathOperation {
        ADD("+") {
            @Override
            public double apply(double a, double b) {
                return a + b;
            }
        },
        SUBTRACT("-") {
            @Override
            public double apply(double a, double b) {
                return a - b;
            }
        },
        MULTIPLY("*") {
            @Override
            public double apply(double a, double b) {
                return a * b;
            }
        },
        DIVIDE("/") {
            @Override
            public double apply(double a, double b) {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            }
        },
        MODULUS("%") {
            @Override
            public double apply(double a, double b) {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a % b;
            }
        };

        private final String symbol;

        MathOperation(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        public abstract double apply(double a, double b);

        /**
         * Finds an operation by its symbol.
         */
        public static Optional<MathOperation> fromSymbol(String symbol) {
            return Arrays.stream(values())
                    .filter(op -> op.symbol.equals(symbol))
                    .findFirst();
        }

        /**
         * Formats a computation as a human-readable string.
         */
        public String format(double a, double b) {
            return String.format("%.2f %s %.2f = %.2f", a, symbol, b, apply(a, b));
        }
    }

    // --- Example 3: Enum with BiFunction parameter for strategy ---

    /**
     * String transformation strategies as parameterized enum constants.
     */
    public enum TextTransform {
        UPPER_CASE("Upper Case", String::toUpperCase),
        LOWER_CASE("Lower Case", String::toLowerCase),
        TRIM("Trim", String::trim),
        REVERSE("Reverse", s -> new StringBuilder(s).reverse().toString()),
        CAPITALIZE("Capitalize", s -> s.isEmpty() ? s :
                Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase()),
        SNAKE_CASE("Snake Case", s -> s.trim().replaceAll("\\s+", "_").toLowerCase()),
        NO_OP("No-Op", UnaryOperator.identity());

        private final String displayName;
        private final UnaryOperator<String> transformer;

        TextTransform(String displayName, UnaryOperator<String> transformer) {
            this.displayName = displayName;
            this.transformer = transformer;
        }

        public String displayName() {
            return displayName;
        }

        public String apply(String input) {
            if (input == null) {
                throw new IllegalArgumentException("Input must not be null");
            }
            return transformer.apply(input);
        }

        /**
         * Chains multiple transforms together.
         */
        public static String applyAll(String input, TextTransform... transforms) {
            if (input == null) {
                throw new IllegalArgumentException("Input must not be null");
            }
            String result = input;
            for (TextTransform t : transforms) {
                result = t.apply(result);
            }
            return result;
        }
    }

    // --- Example 4: Enum with multiple constructor parameters and state ---

    /**
     * HTTP status code enum demonstrating rich parameterization.
     */
    public enum HttpStatus {
        OK(200, "OK", Category.SUCCESS),
        CREATED(201, "Created", Category.SUCCESS),
        NO_CONTENT(204, "No Content", Category.SUCCESS),
        BAD_REQUEST(400, "Bad Request", Category.CLIENT_ERROR),
        UNAUTHORIZED(401, "Unauthorized", Category.CLIENT_ERROR),
        FORBIDDEN(403, "Forbidden", Category.CLIENT_ERROR),
        NOT_FOUND(404, "Not Found", Category.CLIENT_ERROR),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error", Category.SERVER_ERROR),
        BAD_GATEWAY(502, "Bad Gateway", Category.SERVER_ERROR),
        SERVICE_UNAVAILABLE(503, "Service Unavailable", Category.SERVER_ERROR);

        public enum Category {
            SUCCESS, CLIENT_ERROR, SERVER_ERROR
        }

        private final int code;
        private final String reason;
        private final Category category;

        private static final Map<Integer, HttpStatus> CODE_MAP;

        static {
            CODE_MAP = Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(HttpStatus::code, s -> s));
        }

        HttpStatus(int code, String reason, Category category) {
            this.code = code;
            this.reason = reason;
            this.category = category;
        }

        public int code() {
            return code;
        }

        public String reason() {
            return reason;
        }

        public Category category() {
            return category;
        }

        public boolean isSuccess() {
            return category == Category.SUCCESS;
        }

        public boolean isClientError() {
            return category == Category.CLIENT_ERROR;
        }

        public boolean isServerError() {
            return category == Category.SERVER_ERROR;
        }

        /**
         * Looks up a status by its numeric code.
         */
        public static Optional<HttpStatus> fromCode(int code) {
            return Optional.ofNullable(CODE_MAP.get(code));
        }

        /**
         * Returns all statuses in a given category.
         */
        public static List<HttpStatus> byCategory(Category category) {
            return Arrays.stream(values())
                    .filter(s -> s.category == category)
                    .collect(Collectors.toList());
        }
    }

    // --- Example 5: Enum implementing multiple interfaces ---

    /**
     * Day schedule enum implementing Comparable behavior with custom parameters.
     */
    public enum WorkSchedule {
        MONDAY(DayOfWeek.MONDAY, "09:00", "17:00", true),
        TUESDAY(DayOfWeek.TUESDAY, "09:00", "17:00", true),
        WEDNESDAY(DayOfWeek.WEDNESDAY, "09:00", "17:00", true),
        THURSDAY(DayOfWeek.THURSDAY, "09:00", "17:00", true),
        FRIDAY(DayOfWeek.FRIDAY, "09:00", "16:00", true),
        SATURDAY(DayOfWeek.SATURDAY, "10:00", "14:00", false),
        SUNDAY(DayOfWeek.SUNDAY, null, null, false);

        private final DayOfWeek dayOfWeek;
        private final String startTime;
        private final String endTime;
        private final boolean required;

        WorkSchedule(DayOfWeek dayOfWeek, String startTime, String endTime, boolean required) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.required = required;
        }

        public DayOfWeek dayOfWeek() {
            return dayOfWeek;
        }

        public Optional<String> startTime() {
            return Optional.ofNullable(startTime);
        }

        public Optional<String> endTime() {
            return Optional.ofNullable(endTime);
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isWorkDay() {
            return startTime != null && endTime != null;
        }

        /**
         * Returns the schedule for today.
         */
        public static WorkSchedule forToday() {
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            return forDayOfWeek(today);
        }

        /**
         * Returns the schedule for a given DayOfWeek.
         */
        public static WorkSchedule forDayOfWeek(DayOfWeek day) {
            return Arrays.stream(values())
                    .filter(ws -> ws.dayOfWeek == day)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No schedule for: " + day));
        }

        /**
         * Returns all required work days.
         */
        public static List<WorkSchedule> requiredDays() {
            return Arrays.stream(values())
                    .filter(WorkSchedule::isRequired)
                    .collect(Collectors.toList());
        }
    }
}
