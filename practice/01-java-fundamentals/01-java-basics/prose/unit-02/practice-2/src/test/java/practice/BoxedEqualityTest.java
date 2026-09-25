package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxedEqualityTest {

    @Test
    void comparesSmallValues() {
        assertThat(BoxedEquality.sameValue(Integer.valueOf(5), Integer.valueOf(5))).isTrue();
        assertThat(BoxedEquality.sameValue(Integer.valueOf(5), Integer.valueOf(6))).isFalse();
        assertThat(BoxedEquality.sameValue(Integer.valueOf(-128), Integer.valueOf(-128))).isTrue();
    }

    @Test
    void comparesValuesOutsideTheCache() {
        assertThat(BoxedEquality.sameValue(Integer.valueOf(1000), Integer.valueOf(1000))).isTrue();
        assertThat(BoxedEquality.sameValue(Integer.valueOf(128), Integer.valueOf(128))).isTrue();
    }

    @Test
    void handlesNull() {
        assertThat(BoxedEquality.sameValue(null, null)).isTrue();
        assertThat(BoxedEquality.sameValue(null, 5)).isFalse();
        assertThat(BoxedEquality.sameValue(5, null)).isFalse();
    }
}
