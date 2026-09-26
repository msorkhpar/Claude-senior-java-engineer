package practice;

public class Garage {

    /** Seal me: only Car, Motorcycle and Truck may implement me. */
    public interface Vehicle {
    }

    public record Car(String model) implements Vehicle {
    }

    public record Motorcycle(String brand) implements Vehicle {
    }

    public record Truck(int capacity) implements Vehicle {
    }

    /** Car 4, Motorcycle 2, Truck 6. */
    public static int wheels(Vehicle v) {
        throw new UnsupportedOperationException("write wheels");
    }

    /** Car 250, Motorcycle 100, Truck 500 plus 100 per full 1000 kg of capacity. */
    public static long tollCents(Vehicle v) {
        throw new UnsupportedOperationException("write tollCents");
    }
}
