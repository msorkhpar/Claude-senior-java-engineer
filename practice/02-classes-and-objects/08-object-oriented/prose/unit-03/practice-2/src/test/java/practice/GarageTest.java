package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GarageTest {

    @Test
    void aCarDoesWhatBothTypesPromise() {
        Garage.Vehicle vehicle = new Garage.Car("Toyota");
        Garage.Drivable drivable = new Garage.Car("Toyota");
        assertThat(vehicle.start()).isEqualTo("Toyota car is starting.");
        assertThat(drivable.accelerate()).isEqualTo("Toyota car is accelerating.");
        assertThat(drivable.brake()).isEqualTo("Toyota car is braking.");
        assertThat(vehicle).isInstanceOf(Garage.Drivable.class);
        assertThat(drivable).isInstanceOf(Garage.Vehicle.class);
    }

    @Test
    void stopIsInheritedFromVehicle() {
        Garage.Vehicle vehicle = new Garage.Car("Toyota");
        assertThat(vehicle.stop()).isEqualTo("Toyota vehicle is stopping.");
    }

    @Test
    void honkIsTheInterfacesDefault() {
        Garage.Drivable drivable = new Garage.Car("Honda");
        assertThat(drivable.honk()).isEqualTo("Honk honk!");
    }
}
