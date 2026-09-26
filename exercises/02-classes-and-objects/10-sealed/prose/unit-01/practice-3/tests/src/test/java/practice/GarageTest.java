package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GarageTest {

    @Test
    void wheelsAndTollsForEachVehicle() {
        Garage.Vehicle car = new Garage.Car("Tesla");
        Garage.Vehicle motorcycle = new Garage.Motorcycle("Harley-Davidson");
        Garage.Vehicle truck = new Garage.Truck(3000);
        assertThat(Garage.wheels(car)).isEqualTo(4);
        assertThat(Garage.wheels(motorcycle)).isEqualTo(2);
        assertThat(Garage.wheels(truck)).isEqualTo(6);
        assertThat(Garage.tollCents(car)).isEqualTo(250);
        assertThat(Garage.tollCents(motorcycle)).isEqualTo(100);
        assertThat(Garage.tollCents(truck)).isEqualTo(800);
    }

    @Test
    void vehicleIsSealedToTheThreeRecords() {
        assertThat(Garage.Vehicle.class.isSealed()).isTrue();
        assertThat(Garage.Vehicle.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(Garage.Car.class, Garage.Motorcycle.class, Garage.Truck.class);
    }

    @Test
    void onlyFullTonnesAreCharged() {
        assertThat(Garage.tollCents(new Garage.Truck(10000))).isEqualTo(1500);
        assertThat(Garage.tollCents(new Garage.Truck(3500))).isEqualTo(800);
        assertThat(Garage.tollCents(new Garage.Truck(999))).isEqualTo(500);
    }
}
