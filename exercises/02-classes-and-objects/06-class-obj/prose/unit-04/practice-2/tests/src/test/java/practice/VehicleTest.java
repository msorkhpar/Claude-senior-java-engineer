package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    @Test
    void aTruckIsRegisteredLikeAnyVehicle() {
        Vehicle.Truck truck = new Vehicle.Truck("AB-123", 12);
        assertThat(truck.getPlate()).isEqualTo("AB-123");
        assertThat(truck.getCapacity()).isEqualTo(12);
        assertThat(truck.describe()).isEqualTo("Truck AB-123 (12t)");
        assertThat(new Vehicle("XY-9").getPlate()).isEqualTo("XY-9");
    }

    @Test
    void aTruckWithABlankPlateIsRefused() {
        assertThatThrownBy(() -> new Vehicle(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Vehicle.Truck(" ", 12)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Vehicle.Truck(null, 12)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aTruckNeedsAPositiveCapacity() {
        assertThatThrownBy(() -> new Vehicle.Truck("AB-123", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Vehicle.Truck("AB-123", -3)).isInstanceOf(IllegalArgumentException.class);
    }
}
