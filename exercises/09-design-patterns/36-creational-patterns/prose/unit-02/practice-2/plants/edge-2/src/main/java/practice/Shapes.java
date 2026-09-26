package practice;

public final class Shapes {

    private Shapes() {
    }

    public sealed interface Shape permits Circle, Square, Triangle {
        double area();
    }

    public record Circle(double radius) implements Shape {
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public record Square(double side) implements Shape {
        public double area() {
            return side * side;
        }
    }

    public record Triangle(double base, double height) implements Shape {
        public double area() {
            return 0.5 * base * height;
        }
    }

    /** Creates the shape named by {@code type}; an unknown type is refused. */
    public static Shape create(String type, double size) {
        if (type == "circle") {
            return new Circle(size);
        }
        if (type == "square") {
            return new Square(size);
        }
        if (type == "triangle") {
            return new Triangle(size, size);
        }
        throw new IllegalArgumentException("Unknown shape: " + type);
    }

    /** Describes a shape: Round r=..., Square s=..., Triangle b=... h=... */
    public static String describe(Shape shape) {
        return switch (shape) {
            case Circle c -> "Round r=" + c.radius();
            case Square s -> "Square s=" + s.side();
            case Triangle t -> "Triangle b=" + t.base() + " h=" + t.height();
        };
    }
}
