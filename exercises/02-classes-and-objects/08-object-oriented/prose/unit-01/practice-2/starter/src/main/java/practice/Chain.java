package practice;

import java.util.List;

public class Chain {

    public static class Vehicle {
        protected final String brand;

        public Vehicle(String brand, List<String> log) {
            throw new UnsupportedOperationException("write the Vehicle constructor");
        }
    }

    public static class Car extends Vehicle {
        protected final int doors;

        public Car(String brand, int doors, List<String> log) {
            super(brand, log); // replace if you need to
            throw new UnsupportedOperationException("write the full Car constructor");
        }

        /** A four-door car. */
        public Car(String brand, List<String> log) {
            super(brand, log); // replace: hand over to the full constructor
            throw new UnsupportedOperationException("write the short Car constructor");
        }
    }

    public static class SportsCar extends Car {

        public SportsCar(String brand, List<String> log) {
            super(brand, log); // replace if you need to
            throw new UnsupportedOperationException("write the SportsCar constructor");
        }

        public String describe() {
            throw new UnsupportedOperationException("write describe");
        }
    }
}
