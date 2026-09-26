package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CsvTest {

    @Test
    void joinsTheItems() {
        assertThat(Csv.join(List.of("a", "b", "c"))).startsWith("a, b, c");
        assertThat(Csv.join(List.of("x", "y"))).startsWith("x, y");
    }

    @Test
    void noSeparatorTrails() {
        assertThat(Csv.join(List.of("a", "b", "c"))).isEqualTo("a, b, c");
        assertThat(Csv.join(List.of("solo"))).isEqualTo("solo");
    }

    @Test
    void anEmptyListIsEmpty() {
        assertThat(Csv.join(List.of())).isEmpty();
    }
}
