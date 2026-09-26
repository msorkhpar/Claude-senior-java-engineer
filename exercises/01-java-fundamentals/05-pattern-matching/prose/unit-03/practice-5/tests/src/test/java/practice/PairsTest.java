package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PairsTest {

    @Test
    void labelsTypedPairs() {
        assertThat(Pairs.label(new Pairs.Pair<>("Ada", 36))).isEqualTo("Ada is 36");
        assertThat(Pairs.label(new Pairs.Pair<>(2, 3))).isEqualTo("sum 5");
        assertThat(Pairs.label("Ada")).isEqualTo("not a pair");
        assertThat(Pairs.label(null)).isEqualTo("not a pair");
    }

    @Test
    void theRunTimeTypesDecide() {
        assertThat(Pairs.label(new Pairs.Pair<>(36, "Ada"))).isEqualTo("pair");
        assertThat(Pairs.label(new Pairs.Pair<>("a", "b"))).isEqualTo("pair");
        assertThat(Pairs.label(new Pairs.Pair<>(3.5, 2))).isEqualTo("pair");
    }
}
