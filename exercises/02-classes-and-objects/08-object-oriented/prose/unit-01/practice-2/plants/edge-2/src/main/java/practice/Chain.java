package practice;

import java.util.List;

public class Chain {

    public static class Vehicle {
        protected final String brand;

        public Vehicle(String brand, List<String> log) {
            this.brand = brand;
            log.add("Vehicle " + brand);
        }
    }

    public static class Car extends Vehicle {
        protected final int doors;

        public Car(String brand, int doors, List<String> log) {
            super(brand, log);
            this.doors = doors;
            log.add("Car " + doors + " doors");
        }

        /** A four-door car. */
        public Car(String brand, List<String> log) {
            super(brand, log);
            this.doors = 4;
        }
    }

    public static class SportsCar extends Car {

        public SportsCar(String brand, List<String> log) {
            super(brand, 2, log);
            log.add("SportsCar");
        }

        public String describe() {
            return brand + " sports car with " + doors + " doors";
        }
    }
}
