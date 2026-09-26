package practice;

public final class Garage {

    /** Anything that moves people. */
    public interface Vehicle {
    }

    /** A car drives. */
    public static class Car implements Vehicle {
        public String drive() {
            return "drive";
        }
    }

    /** An electric car drives, and also charges. */
    public static class ElectricCar extends Car {
        public String charge() {
            return "charge";
        }
    }

    /** A bicycle is pedalled. */
    public static class Bicycle implements Vehicle {
        public String pedal() {
            return "pedal";
        }
    }

    private Garage() {
    }

    /** Returns what the vehicle does. */
    public static String use(Vehicle vehicle) {
        throw new UnsupportedOperationException("write use");
    }
}
