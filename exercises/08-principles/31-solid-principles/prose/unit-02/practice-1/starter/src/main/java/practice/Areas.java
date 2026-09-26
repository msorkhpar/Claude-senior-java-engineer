package practice;

import java.util.List;
import java.util.Map;

public final class Areas {

    private Areas() {
    }

    /** The extension point: any shape computes its own area. */
    public interface Shape {
        double area();

        String name();
    }

    public record Circle(double radius) implements Shape {
        public double area() {
            throw new UnsupportedOperationException("write area");
        }

        public String name() {
            throw new UnsupportedOperationException("write name");
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        public double area() {
            throw new UnsupportedOperationException("write area");
        }

        public String name() {
            throw new UnsupportedOperationException("write name");
        }
    }

    public record Triangle(double base, double height) implements Shape {
        public double area() {
            throw new UnsupportedOperationException("write area");
        }

        public String name() {
            throw new UnsupportedOperationException("write name");
        }
    }

    /** Closed for modification: works with any Shape, including ones written later. */
    public static final class AreaCalculator {
        public double totalArea(List<? extends Shape> shapes) {
            throw new UnsupportedOperationException("write totalArea");
        }

        public Map<String, Double> areaByType(List<? extends Shape> shapes) {
            throw new UnsupportedOperationException("write areaByType");
        }
    }
}
