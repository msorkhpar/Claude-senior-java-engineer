package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TollGateTest {

    @Test
    void chargesCarsAndTrucks() {
        assertThat(TollGate.toll(new TollGate.Car(4))).isEqualTo(5);
        assertThat(TollGate.toll(new TollGate.Car(2))).isEqualTo(5);
        assertThat(TollGate.toll(new TollGate.Truck(3))).isEqualTo(16);
        assertThat(TollGate.toll(new TollGate.Motorcycle(false))).isEqualTo(2);
    }

    @Test
    void aStartedTonneCounts() {
        assertThat(TollGate.toll(new TollGate.Truck(3.5))).isEqualTo(18);
        assertThat(TollGate.toll(new TollGate.Truck(0.1))).isEqualTo(12);
    }

    @Test
    void aSidecarCostsOneMore() {
        assertThat(TollGate.toll(new TollGate.Motorcycle(true))).isEqualTo(3);
    }
}
