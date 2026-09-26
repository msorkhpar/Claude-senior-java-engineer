package practice;

public class Garage {

    public sealed interface Vehicle permits Car, Motorcycle, Truck {
    }

    public record Car(String model) implements Vehicle {
    }

    public record Motorcycle(String brand) implements Vehicle {
    }

    public record Truck(int capacity) implements Vehicle {
    }

    public static int wheels(Vehicle v) {
        return switch (v) {
            case Car car -> 4;
            case Motorcycle motorcycle -> 2;
            case Truck truck -> 6;
        };
    }

    public static long tollCents(Vehicle v) {
        return switch (v) {
            case Car car -> 250;
            case Motorcycle motorcycle -> 100;
            case Truck truck -> 500 + 100L * (long) Math.ceil(truck.capacity() / 1000.0);
        };
    }
}
