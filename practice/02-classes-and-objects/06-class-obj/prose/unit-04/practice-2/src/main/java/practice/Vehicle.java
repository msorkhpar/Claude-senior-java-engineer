package practice;

public class Vehicle {

    protected Vehicle() {
        throw new UnsupportedOperationException("write this constructor");
    }

    public Vehicle(String plate) {
        throw new UnsupportedOperationException("write this constructor");
    }

    public String getPlate() {
        throw new UnsupportedOperationException("write getPlate");
    }

    public static class Truck extends Vehicle {

        public Truck(String plate, int capacityTonnes) {
            super(plate);
        }

        public int getCapacity() {
            throw new UnsupportedOperationException("write getCapacity");
        }

        public String describe() {
            throw new UnsupportedOperationException("write describe");
        }
    }
}
