package com.github.msorkhpar.claudejavatutor.solidprinciples;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the Liskov Substitution Principle (LSP).
 * Subtypes must be substitutable for their base types without altering
 * the correctness of the program.
 */
public class LiskovSubstitution {

    // ========== VIOLATION EXAMPLE ==========

    /**
     * Classic LSP violation: Square extends Rectangle but breaks the contract.
     */
    public static class RectangleViolation {
        protected int width;
        protected int height;

        public RectangleViolation(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }

        public int area() {
            return width * height;
        }
    }

    /**
     * LSP violation: Square overrides setWidth/setHeight to keep both dimensions equal.
     * Code that expects independent width/height will break.
     */
    public static class SquareViolation extends RectangleViolation {
        public SquareViolation(int side) {
            super(side, side);
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
            this.height = width; // Violates postcondition: setWidth should only change width
        }

        @Override
        public void setHeight(int height) {
            this.width = height; // Violates postcondition: setHeight should only change height
            this.height = height;
        }
    }

    // ========== CORRECT EXAMPLE: LSP Applied ==========

    /**
     * Proper design: Shape as a sealed interface hierarchy.
     * Each shape fulfills its own contract without breaking substitutability.
     */
    public sealed interface Shape permits LspRectangle, LspSquare, LspCircle {
        double area();
        double perimeter();
        String description();
    }

    public record LspRectangle(double width, double height) implements Shape {
        public LspRectangle {
            if (width < 0 || height < 0) throw new IllegalArgumentException("Dimensions must be non-negative");
        }

        @Override
        public double area() {
            return width * height;
        }

        @Override
        public double perimeter() {
            return 2 * (width + height);
        }

        @Override
        public String description() {
            return "Rectangle[%sx%s]".formatted(width, height);
        }
    }

    public record LspSquare(double side) implements Shape {
        public LspSquare {
            if (side < 0) throw new IllegalArgumentException("Side must be non-negative");
        }

        @Override
        public double area() {
            return side * side;
        }

        @Override
        public double perimeter() {
            return 4 * side;
        }

        @Override
        public String description() {
            return "Square[%s]".formatted(side);
        }
    }

    public record LspCircle(double radius) implements Shape {
        public LspCircle {
            if (radius < 0) throw new IllegalArgumentException("Radius must be non-negative");
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public double perimeter() {
            return 2 * Math.PI * radius;
        }

        @Override
        public String description() {
            return "Circle[r=%s]".formatted(radius);
        }
    }

    /**
     * Works with any Shape -- LSP ensures correctness.
     */
    public static class ShapeAnalyzer {
        public double totalArea(List<Shape> shapes) {
            if (shapes == null) throw new IllegalArgumentException("Shapes list cannot be null");
            return shapes.stream().mapToDouble(Shape::area).sum();
        }

        public double totalPerimeter(List<Shape> shapes) {
            if (shapes == null) throw new IllegalArgumentException("Shapes list cannot be null");
            return shapes.stream().mapToDouble(Shape::perimeter).sum();
        }

        public Shape largestByArea(List<Shape> shapes) {
            if (shapes == null || shapes.isEmpty()) {
                throw new IllegalArgumentException("Shapes list cannot be null or empty");
            }
            return shapes.stream()
                    .max(Comparator.comparingDouble(Shape::area))
                    .orElseThrow();
        }
    }

    // ========== LSP IN CONCURRENT PROGRAMMING ==========

    /**
     * Base interface for a thread-safe task queue.
     * Contract: offer never blocks, poll returns null when empty, size is accurate.
     */
    public interface TaskQueue<T> {
        boolean offer(T item);
        T poll();
        int size();
        boolean isEmpty();
    }

    /**
     * Bounded task queue that respects the contract.
     */
    public static class BoundedTaskQueue<T> implements TaskQueue<T> {
        private final BlockingQueue<T> queue;

        public BoundedTaskQueue(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
            this.queue = new ArrayBlockingQueue<>(capacity);
        }

        @Override
        public boolean offer(T item) {
            if (item == null) throw new NullPointerException("Item cannot be null");
            return queue.offer(item);
        }

        @Override
        public T poll() {
            return queue.poll();
        }

        @Override
        public int size() {
            return queue.size();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    /**
     * Unbounded task queue -- also respects the contract (offer always succeeds).
     * Substitutable for BoundedTaskQueue wherever TaskQueue is expected.
     */
    public static class UnboundedTaskQueue<T> implements TaskQueue<T> {
        private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public boolean offer(T item) {
            if (item == null) throw new NullPointerException("Item cannot be null");
            queue.offer(item);
            count.incrementAndGet();
            return true; // Always succeeds -- consistent with contract
        }

        @Override
        public T poll() {
            T item = queue.poll();
            if (item != null) {
                count.decrementAndGet();
            }
            return item;
        }

        @Override
        public int size() {
            return count.get();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    /**
     * Consumer that works with any TaskQueue implementation.
     * LSP guarantees this code works regardless of which TaskQueue subtype is used.
     */
    public static class TaskConsumer<T> {
        private final TaskQueue<T> queue;
        private final List<T> processed = new CopyOnWriteArrayList<>();

        public TaskConsumer(TaskQueue<T> queue) {
            this.queue = Objects.requireNonNull(queue);
        }

        public int drainAll() {
            int count = 0;
            T item;
            while ((item = queue.poll()) != null) {
                processed.add(item);
                count++;
            }
            return count;
        }

        public List<T> getProcessed() {
            return Collections.unmodifiableList(processed);
        }
    }
}
