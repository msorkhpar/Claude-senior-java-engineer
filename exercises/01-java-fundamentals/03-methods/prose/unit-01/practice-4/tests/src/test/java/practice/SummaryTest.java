package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SummaryTest {

    @Test
    void summarisesTheItems() {
        assertThat(Summary.summary(List.of("a", "b", "c"))).isEqualTo("3 items: a, b, c");
        assertThat(Summary.summary(List.of("x", "y"))).isEqualTo("2 items: x, y");
    }

    @Test
    void oneItemIsSingular() {
        assertThat(Summary.summary(List.of("pen"))).isEqualTo("1 item: pen");
    }

    @Test
    void anEmptyListIsNothing() {
        assertThat(Summary.summary(List.of())).isEqualTo("nothing to process");
    }

    @Test
    void nullIsNothing() {
        assertThat(Summary.summary(null)).isEqualTo("nothing to process");
    }
}
