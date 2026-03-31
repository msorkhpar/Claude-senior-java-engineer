package com.github.msorkhpar.claudejavatutor.solidprinciples;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Demonstrates the Open/Closed Principle (OCP).
 * Software entities should be open for extension but closed for modification.
 */
public class OpenClosed {

    // ========== VIOLATION EXAMPLE ==========

    /**
     * Violates OCP: Adding a new shape requires modifying this class.
     */
    public static class AreaCalculatorViolation {
        public double calculateArea(String shapeType, double... dimensions) {
            return switch (shapeType) {
                case "circle" -> Math.PI * dimensions[0] * dimensions[0];
                case "rectangle" -> dimensions[0] * dimensions[1];
                case "triangle" -> 0.5 * dimensions[0] * dimensions[1];
                default -> throw new IllegalArgumentException("Unknown shape: " + shapeType);
            };
        }
    }

    // ========== CORRECT EXAMPLE: OCP Applied ==========

    /**
     * Open for extension: new shapes implement this interface without modifying existing code.
     */
    public sealed interface Shape permits Circle, Rectangle, Triangle, Parallelogram {
        double area();
        String name();
    }

    public record Circle(double radius) implements Shape {
        public Circle {
            if (radius < 0) throw new IllegalArgumentException("Radius cannot be negative");
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public String name() {
            return "Circle";
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        public Rectangle {
            if (width < 0 || height < 0) throw new IllegalArgumentException("Dimensions cannot be negative");
        }

        @Override
        public double area() {
            return width * height;
        }

        @Override
        public String name() {
            return "Rectangle";
        }
    }

    public record Triangle(double base, double height) implements Shape {
        public Triangle {
            if (base < 0 || height < 0) throw new IllegalArgumentException("Dimensions cannot be negative");
        }

        @Override
        public double area() {
            return 0.5 * base * height;
        }

        @Override
        public String name() {
            return "Triangle";
        }
    }

    /**
     * Extension: adding Parallelogram without modifying any existing shape or calculator.
     */
    public record Parallelogram(double base, double height) implements Shape {
        public Parallelogram {
            if (base < 0 || height < 0) throw new IllegalArgumentException("Dimensions cannot be negative");
        }

        @Override
        public double area() {
            return base * height;
        }

        @Override
        public String name() {
            return "Parallelogram";
        }
    }

    /**
     * Closed for modification: works with any Shape implementation.
     */
    public static class AreaCalculator {
        public double totalArea(List<Shape> shapes) {
            if (shapes == null) {
                throw new IllegalArgumentException("Shapes list cannot be null");
            }
            return shapes.stream()
                    .mapToDouble(Shape::area)
                    .sum();
        }

        public Map<String, Double> areaByType(List<Shape> shapes) {
            if (shapes == null) {
                throw new IllegalArgumentException("Shapes list cannot be null");
            }
            return shapes.stream()
                    .collect(Collectors.groupingBy(
                            Shape::name,
                            Collectors.summingDouble(Shape::area)
                    ));
        }
    }

    // ========== OCP IN CONCURRENT PROGRAMMING ==========

    /**
     * Strategy interface for processing tasks -- open for extension.
     */
    public interface TaskProcessor<T, R> {
        R process(T input);
    }

    /**
     * A concrete processor: squares a number.
     */
    public static class SquareProcessor implements TaskProcessor<Integer, Long> {
        @Override
        public Long process(Integer input) {
            return (long) input * input;
        }
    }

    /**
     * A concrete processor: cubes a number.
     */
    public static class CubeProcessor implements TaskProcessor<Integer, Long> {
        @Override
        public Long process(Integer input) {
            return (long) input * input * input;
        }
    }

    /**
     * Thread-safe pipeline that is closed for modification but open for extension
     * via different TaskProcessor implementations.
     */
    public static class ConcurrentPipeline<T, R> {
        private final TaskProcessor<T, R> processor;
        private final ReentrantLock lock = new ReentrantLock();
        private final List<R> results = new ArrayList<>();

        public ConcurrentPipeline(TaskProcessor<T, R> processor) {
            this.processor = Objects.requireNonNull(processor, "Processor cannot be null");
        }

        public R processItem(T input) {
            R result = processor.process(input);
            lock.lock();
            try {
                results.add(result);
            } finally {
                lock.unlock();
            }
            return result;
        }

        public List<R> processAll(List<T> inputs) throws InterruptedException, ExecutionException {
            if (inputs == null) {
                throw new IllegalArgumentException("Inputs list cannot be null");
            }
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<R>> futures = inputs.stream()
                        .map(input -> executor.submit(() -> processItem(input)))
                        .toList();

                List<R> collected = new ArrayList<>();
                for (Future<R> future : futures) {
                    collected.add(future.get());
                }
                return collected;
            }
        }

        public List<R> getResults() {
            lock.lock();
            try {
                return List.copyOf(results);
            } finally {
                lock.unlock();
            }
        }
    }

    // ========== OCP WITH DECORATORS ==========

    /**
     * Base validator interface -- open for extension via decoration.
     */
    public interface Validator<T> {
        boolean isValid(T value);
        String getDescription();
    }

    /**
     * Validates non-null strings.
     */
    public static class NonNullValidator implements Validator<String> {
        @Override
        public boolean isValid(String value) {
            return value != null;
        }

        @Override
        public String getDescription() {
            return "NonNull";
        }
    }

    /**
     * Validates non-empty strings. Extends validation chain without modifying NonNullValidator.
     */
    public static class NonEmptyValidator implements Validator<String> {
        private final Validator<String> delegate;

        public NonEmptyValidator(Validator<String> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public boolean isValid(String value) {
            return delegate.isValid(value) && !value.isEmpty();
        }

        @Override
        public String getDescription() {
            return delegate.getDescription() + " + NonEmpty";
        }
    }

    /**
     * Validates minimum length strings. Further extends validation.
     */
    public static class MinLengthValidator implements Validator<String> {
        private final Validator<String> delegate;
        private final int minLength;

        public MinLengthValidator(Validator<String> delegate, int minLength) {
            this.delegate = Objects.requireNonNull(delegate);
            this.minLength = minLength;
        }

        @Override
        public boolean isValid(String value) {
            return delegate.isValid(value) && value.length() >= minLength;
        }

        @Override
        public String getDescription() {
            return delegate.getDescription() + " + MinLength(" + minLength + ")";
        }
    }
}
