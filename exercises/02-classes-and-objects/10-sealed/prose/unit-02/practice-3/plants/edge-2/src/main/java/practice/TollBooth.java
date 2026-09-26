package practice;

public class TollBooth {

    public abstract static sealed class Vehicle permits Car, Truck, Motorcycle {
    }

    public static final class Car extends Vehicle {
    }

    public abstract static sealed class Truck extends Vehicle permits PickupTruck, SemiTruck {
    }

    public static final class PickupTruck extends Truck {
    }

    public static final class SemiTruck extends Truck {
        private final int axles;

        public SemiTruck(int axles) {
            this.axles = axles;
        }

        public int axles() {
            return axles;
        }
    }

    public static non-sealed class Motorcycle extends Vehicle {
    }

    /** Motorcycle is non-sealed, so any class may extend it. */
    public static class Scooter extends Motorcycle {
    }

    public static int toll(Vehicle v) {
        if (v.getClass() == Car.class) {
            return 5;
        }
        if (v.getClass() == PickupTruck.class) {
            return 7;
        }
        if (v.getClass() == SemiTruck.class) {
            return 4 * ((SemiTruck) v).axles();
        }
        if (v.getClass() == Motorcycle.class) {
            return 2;
        }
        throw new IllegalArgumentException("unknown vehicle " + v.getClass().getSimpleName());
    }
}
