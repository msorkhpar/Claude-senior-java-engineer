package practice;

public class Vehicle {
    protected String plate;

    protected Vehicle() {
        this.plate = "UNREGISTERED";
    }

    public Vehicle(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("A plate is required");
        }
        this.plate = plate;
    }

    public String getPlate() {
        return plate;
    }

    public static class Truck extends Vehicle {
        private final int capacityTonnes;

        public Truck(String plate, int capacityTonnes) {
            this.plate = plate;
            if (capacityTonnes < 1) {
                throw new IllegalArgumentException("Capacity must be at least 1 tonne");
            }
            this.capacityTonnes = capacityTonnes;
        }

        public int getCapacity() {
            return capacityTonnes;
        }

        public String describe() {
            return "Truck " + plate + " (" + capacityTonnes + "t)";
        }
    }
}
