package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class GrowthTest {

    @Test
    void growsByHalf() {
        assertThat(Growth.capacities(10, 16)).containsExactly(10, 15, 22);
        assertThat(Growth.capacities(10, 100)).containsExactly(10, 15, 22, 33, 49, 73, 109);
    }

    @Test
    void fullArrayDoesNotGrowYet() {
        assertThat(Growth.capacities(10, 10)).containsExactly(10);
        assertThat(Growth.capacities(10, 11)).containsExactly(10, 15);
        assertThat(Growth.capacities(15, 15)).containsExactly(15);
        assertThat(Growth.capacities(1000, 1000)).containsExactly(1000);
    }

    @Test
    void smallCapacitiesGrowByAtLeastOne() {
        assertThat(Growth.capacities(1, 6)).containsExactly(1, 2, 3, 4, 6);
        assertThat(Growth.capacities(0, 1)).containsExactly(0, 1);
    }

    @Test
    void defaultListAllocatesOnFirstAdd() {
        assertThat(Growth.defaultCapacities(0)).containsExactly(0);
        assertThat(Growth.defaultCapacities(1)).containsExactly(0, 10);
        assertThat(Growth.defaultCapacities(11)).containsExactly(0, 10, 15);
    }
}
