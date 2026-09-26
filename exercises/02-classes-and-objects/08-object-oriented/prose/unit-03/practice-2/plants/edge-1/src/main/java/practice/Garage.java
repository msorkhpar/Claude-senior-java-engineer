package practice;

public class Garage {

    public abstract static class Vehicle {
        protected final String brand;

        public Vehicle(String brand) {
            this.brand = brand;
        }

        public abstract String start();

        public String stop() {
            return brand + " vehicle is stopping.";
        }
    }

    public interface Drivable {
        String accelerate();

        String brake();

        default String honk() {
            return "Honk honk!";
        }
    }

    public static class Car extends Vehicle implements Drivable {

        public Car(String brand) {
            super(brand);
        }

        @Override
        public String start() {
            return brand + " car is starting.";
        }

        @Override
        public String accelerate() {
            return brand + " car is accelerating.";
        }

        @Override
        public String brake() {
            return brand + " car is braking.";
        }

        @Override
        public String stop() {
            return brand + " car is stopping.";
        }
    }
}
