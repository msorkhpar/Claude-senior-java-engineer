package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TollBoothTest {

    @Test
    void chargesEachKindOfVehicle() {
        assertThat(TollBooth.toll(new TollBooth.Car())).isEqualTo(5);
        assertThat(TollBooth.toll(new TollBooth.PickupTruck())).isEqualTo(7);
        assertThat(TollBooth.toll(new TollBooth.Motorcycle())).isEqualTo(2);
    }

    @Test
    void aSemiTruckPaysPerAxle() {
        assertThat(TollBooth.toll(new TollBooth.SemiTruck(3))).isEqualTo(12);
        assertThat(TollBooth.toll(new TollBooth.SemiTruck(5))).isEqualTo(20);
    }

    @Test
    void aScooterPaysAsAMotorcycle() {
        assertThat(TollBooth.toll(new TollBooth.Scooter())).isEqualTo(2);
    }
}
