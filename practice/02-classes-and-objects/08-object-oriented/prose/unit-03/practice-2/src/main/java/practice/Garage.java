package practice;

public class Garage {

    public abstract static class Vehicle {
        protected final String brand;

        public Vehicle(String brand) {
            throw new UnsupportedOperationException("write the Vehicle constructor");
        }

        public abstract String start();

        public String stop() {
            throw new UnsupportedOperationException("write Vehicle.stop");
        }
    }

    public interface Drivable {
        String accelerate();

        String brake();

        default String honk() {
            throw new UnsupportedOperationException("write Drivable.honk");
        }
    }

    public static class Car extends Vehicle implements Drivable {

        public Car(String brand) {
            super(brand);
        }

        @Override
        public String start() {
            throw new UnsupportedOperationException("write Car.start");
        }

        @Override
        public String accelerate() {
            throw new UnsupportedOperationException("write Car.accelerate");
        }

        @Override
        public String brake() {
            throw new UnsupportedOperationException("write Car.brake");
        }
    }
}
