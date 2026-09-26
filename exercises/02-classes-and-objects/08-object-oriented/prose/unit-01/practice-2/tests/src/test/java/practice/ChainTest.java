package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChainTest {

    @Test
    void aSportsCarRunsEveryConstructorTopDown() {
        List<String> log = new ArrayList<>();
        new Chain.SportsCar("Ferrari", log);
        assertThat(log).containsExactly("Vehicle Ferrari", "Car 2 doors", "SportsCar");
        assertThat(new Chain.SportsCar("Porsche", new ArrayList<>()).describe()).isEqualTo("Porsche sports car with 2 doors");
    }

    @Test
    void aPlainCarDelegatesToTheFullConstructor() {
        List<String> log = new ArrayList<>();
        Chain.Car car = new Chain.Car("Fiat", log);
        assertThat(log).hasSize(2);
        assertThat(car.doors).isEqualTo(4);
    }

    @Test
    void aPlainCarStillRecordsItsOwnStep() {
        List<String> log = new ArrayList<>();
        new Chain.Car("Fiat", log);
        assertThat(log).containsExactly("Vehicle Fiat", "Car 4 doors");
    }
}
