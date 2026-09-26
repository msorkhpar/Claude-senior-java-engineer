package practice;

public final class ShapeFactory {

    /** A shape the factory creates. */
    public interface Shape {
        double area();
    }

    /** Registers type under name, ignoring case; refuses a type without a no-arg constructor. */
    public void register(String name, Class<? extends Shape> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** A new instance of the class registered under name, in any case. */
    public Shape create(String name) {
        throw new UnsupportedOperationException("TODO");
    }
}
