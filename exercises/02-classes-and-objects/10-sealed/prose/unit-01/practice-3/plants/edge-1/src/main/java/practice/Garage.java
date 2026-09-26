package practice;

public class Garage {

    public interface Vehicle {
    }

    public record Car(String model) implements Vehicle {
    }

    public record Motorcycle(String brand) implements Vehicle {
    }

    public record Truck(int capacity) implements Vehicle {
    }

    public static int wheels(Vehicle v) {
        if (v instanceof Car) {
            return 4;
        }
        if (v instanceof Motorcycle) {
            return 2;
        }
        return 6;
    }

    public static long tollCents(Vehicle v) {
        if (v instanceof Car) {
            return 250;
        }
        if (v instanceof Motorcycle) {
            return 100;
        }
        return 500 + 100L * (((Truck) v).capacity() / 1000);
    }
}
