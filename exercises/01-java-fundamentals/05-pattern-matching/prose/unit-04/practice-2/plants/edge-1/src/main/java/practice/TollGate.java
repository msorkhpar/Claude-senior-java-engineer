package practice;

public final class TollGate {

    /** A vehicle at the toll gate. */
    public sealed interface Vehicle permits Car, Truck, Motorcycle {
    }

    /** A car with a number of doors. */
    public record Car(int doors) implements Vehicle {
    }

    /** A truck with a cargo capacity in tonnes. */
    public record Truck(double cargoCapacity) implements Vehicle {
    }

    /** A motorcycle, with or without a sidecar. */
    public record Motorcycle(boolean hasSidecar) implements Vehicle {
    }

    private TollGate() {
    }

    /** Returns the toll for a vehicle. */
    public static int toll(Vehicle vehicle) {
        return switch (vehicle) {
            case Car c -> 5;
            case Truck t -> 10 + 2 * (int) t.cargoCapacity();
            case Motorcycle m -> m.hasSidecar() ? 3 : 2;
        };
    }
}
