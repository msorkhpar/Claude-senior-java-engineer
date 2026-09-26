package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CollectorTest {

    @Test
    void countsTheItemsAdded() {
        List<String> target = new ArrayList<>(List.of("a"));
        assertThat(Collector.addAll(target, "b", "c")).isEqualTo(2);
        assertThat(Collector.addAll(new ArrayList<>(), "z")).isEqualTo(1);
    }

    @Test
    void theCallersListReceivesTheItems() {
        List<String> target = new ArrayList<>(List.of("a"));
        Collector.addAll(target, "b", "c");
        assertThat(target).containsExactly("a", "b", "c");
    }

    @Test
    void nullItemsAreSkipped() {
        List<String> target = new ArrayList<>();
        assertThat(Collector.addAll(target, "x", null, "y")).isEqualTo(2);
        assertThat(target).containsExactly("x", "y");
    }
}
