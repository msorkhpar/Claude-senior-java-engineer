package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GarageTest {

    @Test
    void usesEachVehicle() {
        assertThat(Garage.use(new Garage.Car())).isEqualTo("drive");
        assertThat(Garage.use(new Garage.Bicycle())).isEqualTo("pedal");
        assertThat(Garage.use(new Garage.Vehicle() { })).isEqualTo("unknown");
    }

    @Test
    void anElectricCarAlsoCharges() {
        assertThat(Garage.use(new Garage.ElectricCar())).isEqualTo("drive+charge");
    }

    @Test
    void nullIsUnknown() {
        assertThat(Garage.use(null)).isEqualTo("unknown");
    }
}
