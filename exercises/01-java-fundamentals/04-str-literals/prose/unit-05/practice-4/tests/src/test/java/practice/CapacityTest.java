package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CapacityTest {

    @Test
    void sizesTheBuilder() {
        StringBuilder sb = Capacity.forLength(20);
        assertThat(sb.capacity()).isEqualTo(20);
        assertThat(sb.length()).isZero();
        assertThat(Capacity.forLength(100).capacity()).isEqualTo(100);
    }

    @Test
    void zeroIsAValidCapacity() {
        assertThat(Capacity.forLength(0).capacity()).isZero();
    }

    @Test
    void aNegativeLengthIsRefused() {
        assertThatThrownBy(() -> Capacity.forLength(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
