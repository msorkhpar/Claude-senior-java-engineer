package com.github.msorkhpar.claudejavatutor.solidprinciples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Liskov Substitution Principle Tests")
class LiskovSubstitutionTest {

    @Nested
    @DisplayName("LSP Violation: Rectangle / Square")
    class ViolationTest {

        @Test
        @DisplayName("Rectangle behaves correctly independently")
        void testRectangle() {
            var rect = new LiskovSubstitution.RectangleViolation(4, 5);
            assertThat(rect.area()).isEqualTo(20);
        }

        @Test
        @DisplayName("Square violates LSP when setWidth is called")
        void testSquareViolatesLsp() {
            LiskovSubstitution.RectangleViolation shape = new LiskovSubstitution.SquareViolation(5);
            shape.setWidth(10);
            // Expectation for rectangle: width=10, height=5, area=50
            // Actual for square: width=10, height=10, area=100 -- LSP violation!
            assertThat(shape.area()).isNotEqualTo(50); // demonstrates the violation
            assertThat(shape.area()).isEqualTo(100);
        }

        @Test
        @DisplayName("Square violates LSP when setHeight is called")
        void testSquareViolatesLspOnSetHeight() {
            LiskovSubstitution.RectangleViolation shape = new LiskovSubstitution.SquareViolation(5);
            shape.setHeight(3);
            // Expected for rectangle: width=5, height=3, area=15
            // Actual for square: width=3, height=3, area=9
            assertThat(shape.area()).isNotEqualTo(15);
            assertThat(shape.area()).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("LSP Correct: Shape Hierarchy")
    class ShapeHierarchyTest {

        @Test
        @DisplayName("Rectangle computes area and perimeter")
        void testRectangle() {
            var rect = new LiskovSubstitution.LspRectangle(4, 6);
            assertThat(rect.area()).isEqualTo(24.0);
            assertThat(rect.perimeter()).isEqualTo(20.0);
            assertThat(rect.description()).isEqualTo("Rectangle[4.0x6.0]");
        }

        @Test
        @DisplayName("Square computes area and perimeter")
        void testSquare() {
            var square = new LiskovSubstitution.LspSquare(5);
            assertThat(square.area()).isEqualTo(25.0);
            assertThat(square.perimeter()).isEqualTo(20.0);
            assertThat(square.description()).isEqualTo("Square[5.0]");
        }

        @Test
        @DisplayName("Circle computes area and perimeter")
        void testCircle() {
            var circle = new LiskovSubstitution.LspCircle(3);
            assertThat(circle.area()).isCloseTo(Math.PI * 9, within(0.001));
            assertThat(circle.perimeter()).isCloseTo(2 * Math.PI * 3, within(0.001));
            assertThat(circle.description()).isEqualTo("Circle[r=3.0]");
        }

        @Test
        @DisplayName("Rectangle rejects negative dimensions")
        void testRectangleNegative() {
            assertThatThrownBy(() -> new LiskovSubstitution.LspRectangle(-1, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Square rejects negative side")
        void testSquareNegative() {
            assertThatThrownBy(() -> new LiskovSubstitution.LspSquare(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Circle rejects negative radius")
        void testCircleNegative() {
            assertThatThrownBy(() -> new LiskovSubstitution.LspCircle(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Zero dimensions should yield zero area")
        void testZeroDimensions() {
            assertThat(new LiskovSubstitution.LspRectangle(0, 5).area()).isEqualTo(0.0);
            assertThat(new LiskovSubstitution.LspSquare(0).area()).isEqualTo(0.0);
            assertThat(new LiskovSubstitution.LspCircle(0).area()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("ShapeAnalyzer - LSP in Action")
    class ShapeAnalyzerTest {

        @Test
        @DisplayName("Should compute total area for mixed shapes")
        void testTotalArea() {
            var analyzer = new LiskovSubstitution.ShapeAnalyzer();
            List<LiskovSubstitution.Shape> shapes = List.of(
                    new LiskovSubstitution.LspRectangle(4, 6),
                    new LiskovSubstitution.LspSquare(5)
            );
            assertThat(analyzer.totalArea(shapes)).isEqualTo(49.0);
        }

        @Test
        @DisplayName("Should compute total perimeter for mixed shapes")
        void testTotalPerimeter() {
            var analyzer = new LiskovSubstitution.ShapeAnalyzer();
            List<LiskovSubstitution.Shape> shapes = List.of(
                    new LiskovSubstitution.LspRectangle(4, 6),
                    new LiskovSubstitution.LspSquare(5)
            );
            assertThat(analyzer.totalPerimeter(shapes)).isEqualTo(40.0);
        }

        @Test
        @DisplayName("Should find largest by area")
        void testLargestByArea() {
            var analyzer = new LiskovSubstitution.ShapeAnalyzer();
            List<LiskovSubstitution.Shape> shapes = List.of(
                    new LiskovSubstitution.LspRectangle(2, 3),
                    new LiskovSubstitution.LspSquare(10),
                    new LiskovSubstitution.LspCircle(1)
            );
            assertThat(analyzer.largestByArea(shapes)).isEqualTo(new LiskovSubstitution.LspSquare(10));
        }

        @Test
        @DisplayName("Should reject null list")
        void testNullList() {
            var analyzer = new LiskovSubstitution.ShapeAnalyzer();
            assertThatThrownBy(() -> analyzer.totalArea(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should reject empty list for largestByArea")
        void testEmptyListLargest() {
            var analyzer = new LiskovSubstitution.ShapeAnalyzer();
            assertThatThrownBy(() -> analyzer.largestByArea(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TaskQueue - LSP in Concurrency")
    class TaskQueueTest {

        @Test
        @DisplayName("BoundedTaskQueue should respect capacity")
        void testBoundedQueue() {
            var queue = new LiskovSubstitution.BoundedTaskQueue<String>(2);
            assertThat(queue.offer("a")).isTrue();
            assertThat(queue.offer("b")).isTrue();
            assertThat(queue.offer("c")).isFalse(); // full
            assertThat(queue.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("BoundedTaskQueue should poll in order")
        void testBoundedQueuePoll() {
            var queue = new LiskovSubstitution.BoundedTaskQueue<String>(3);
            queue.offer("a");
            queue.offer("b");
            assertThat(queue.poll()).isEqualTo("a");
            assertThat(queue.poll()).isEqualTo("b");
            assertThat(queue.poll()).isNull();
        }

        @Test
        @DisplayName("BoundedTaskQueue should reject null items")
        void testBoundedQueueNullItem() {
            var queue = new LiskovSubstitution.BoundedTaskQueue<String>(5);
            assertThatThrownBy(() -> queue.offer(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("BoundedTaskQueue should reject non-positive capacity")
        void testBoundedQueueInvalidCapacity() {
            assertThatThrownBy(() -> new LiskovSubstitution.BoundedTaskQueue<String>(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("UnboundedTaskQueue always accepts items")
        void testUnboundedQueue() {
            var queue = new LiskovSubstitution.UnboundedTaskQueue<String>();
            for (int i = 0; i < 100; i++) {
                assertThat(queue.offer("item-" + i)).isTrue();
            }
            assertThat(queue.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("UnboundedTaskQueue should reject null items")
        void testUnboundedQueueNullItem() {
            var queue = new LiskovSubstitution.UnboundedTaskQueue<String>();
            assertThatThrownBy(() -> queue.offer(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("UnboundedTaskQueue poll returns null when empty")
        void testUnboundedQueuePollEmpty() {
            var queue = new LiskovSubstitution.UnboundedTaskQueue<String>();
            assertThat(queue.poll()).isNull();
            assertThat(queue.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("TaskConsumer works with both queue types (LSP proof)")
        void testTaskConsumerWithBothQueues() {
            // Bounded
            LiskovSubstitution.TaskQueue<String> bounded = new LiskovSubstitution.BoundedTaskQueue<>(10);
            bounded.offer("x");
            bounded.offer("y");
            var consumer1 = new LiskovSubstitution.TaskConsumer<>(bounded);
            assertThat(consumer1.drainAll()).isEqualTo(2);
            assertThat(consumer1.getProcessed()).containsExactly("x", "y");

            // Unbounded
            LiskovSubstitution.TaskQueue<String> unbounded = new LiskovSubstitution.UnboundedTaskQueue<>();
            unbounded.offer("a");
            unbounded.offer("b");
            unbounded.offer("c");
            var consumer2 = new LiskovSubstitution.TaskConsumer<>(unbounded);
            assertThat(consumer2.drainAll()).isEqualTo(3);
            assertThat(consumer2.getProcessed()).containsExactly("a", "b", "c");
        }
    }
}
