package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxedSumTest {

    @Test
    void sumsTheValues() {
        assertThat(BoxedSum.sum(new Integer[]{1, 2, 3})).isEqualTo(6L);
        assertThat(BoxedSum.sum(new Integer[]{-4, 4, 10})).isEqualTo(10L);
        assertThat(BoxedSum.sum(new Integer[0])).isEqualTo(0L);
    }

    @Test
    void skipsMissingValues() {
        assertThat(BoxedSum.sum(new Integer[]{1, null, 3, 4, null})).isEqualTo(8L);
        assertThat(BoxedSum.sum(new Integer[]{null})).isEqualTo(0L);
    }

    @Test
    void theTotalMayExceedAnInt() {
        assertThat(BoxedSum.sum(new Integer[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 2})).isEqualTo(4_294_967_296L);
    }
}
