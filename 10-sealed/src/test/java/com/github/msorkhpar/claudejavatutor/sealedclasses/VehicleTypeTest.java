package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VehicleTypeTest {

    @Test
    void testCarProperties() {
        VehicleType car = new Car2();
        assertThat(car.getDescription()).isEqualTo("A four-wheeled passenger vehicle");
        assertThat(car.getWheelCount()).isEqualTo(4);
    }

    @Test
    void testMotorcycleProperties() {
        VehicleType motorcycle = new Motorcycle2();
        assertThat(motorcycle.getDescription()).isEqualTo("A two-wheeled vehicle");
        assertThat(motorcycle.getWheelCount()).isEqualTo(2);
    }

    @Test
    void testTruckProperties() {
        VehicleType truck = new Truck2();
        assertThat(truck.getDescription()).isEqualTo("A large vehicle for transporting goods");
        assertThat(truck.getWheelCount()).isEqualTo(6);
    }

    @Test
    void testVehicleRegistration() {
        assertThat(VehicleRegistry.registerVehicle(new Car2())).isEqualTo("Registered a car with 4 wheels");
        assertThat(VehicleRegistry.registerVehicle(new Motorcycle2())).isEqualTo("Registered a motorcycle with 2 wheels");
        assertThat(VehicleRegistry.registerVehicle(new Truck2())).isEqualTo("Registered a truck with 6 wheels");
    }

    @Test
    void testExhaustivePatternMatching() {
        VehicleType[] vehicles = {new Car2(), new Motorcycle2(), new Truck2()};
        for (VehicleType vehicle : vehicles) {
            String result = VehicleRegistry.registerVehicle(vehicle);
            assertThat(result).contains("Registered");
            assertThat(result).contains("wheels");
        }
    }
}