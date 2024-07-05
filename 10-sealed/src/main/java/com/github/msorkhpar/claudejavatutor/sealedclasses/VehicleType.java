package com.github.msorkhpar.claudejavatutor.trycatch;

public sealed interface VehicleType permits Car2, Motorcycle2, Truck2 {
    String getDescription();
    int getWheelCount();
}

final class Car2 implements VehicleType {
    @Override
    public String getDescription() {
        return "A four-wheeled passenger vehicle";
    }

    @Override
    public int getWheelCount() {
        return 4;
    }
}

final class Motorcycle2 implements VehicleType {
    @Override
    public String getDescription() {
        return "A two-wheeled vehicle";
    }

    @Override
    public int getWheelCount() {
        return 2;
    }
}

final class Truck2 implements VehicleType {
    @Override
    public String getDescription() {
        return "A large vehicle for transporting goods";
    }

    @Override
    public int getWheelCount() {
        return 6; // Assuming a standard 6-wheel truck
    }
}

class VehicleRegistry {
    public static String registerVehicle(VehicleType vehicle) {
        return switch (vehicle) {
            case Car2 c -> "Registered a car with " + c.getWheelCount() + " wheels";
            case Motorcycle2 m -> "Registered a motorcycle with " + m.getWheelCount() + " wheels";
            case Truck2 t -> "Registered a truck with " + t.getWheelCount() + " wheels";
        };
    }
}