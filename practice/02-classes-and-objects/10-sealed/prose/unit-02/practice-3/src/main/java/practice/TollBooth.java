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

    /** Car 5, PickupTruck 7, SemiTruck 4 per axle, any Motorcycle 2. */
    public static int toll(Vehicle v) {
        throw new UnsupportedOperationException("write toll");
    }
}
