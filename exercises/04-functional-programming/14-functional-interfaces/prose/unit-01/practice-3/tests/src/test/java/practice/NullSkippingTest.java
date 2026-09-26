package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class NullSkippingTest {

    @Test
    void forwardsAndCountsEveryItem() {
        List<String> seen = new ArrayList<>();
        assertThat(NullSkipping.forEachCounting(List.of("Alice", "Bob"), seen::add)).isEqualTo(2);
        assertThat(seen).containsExactly("Alice", "Bob");

        List<String> nothing = new ArrayList<>();
        assertThat(NullSkipping.forEachCounting(List.<String>of(), nothing::add)).isZero();
        assertThat(nothing).isEmpty();

        List<String> direct = new ArrayList<>();
        NullSkipping.<String>nullSafe(direct::add).accept("x");
        assertThat(direct).containsExactly("x");
    }

    @Test
    void nullsNeverReachTheAction() {
        List<String> seen = new ArrayList<>();
        Consumer<String> upper = s -> seen.add(s.toUpperCase());

        NullSkipping.nullSafe(upper).accept(null);
        NullSkipping.forEachCounting(Arrays.asList("Alice", null, "Bob", null), upper);

        assertThat(seen).containsExactly("ALICE", "BOB");
    }

    @Test
    void nullsAreNotCounted() {
        List<String> seen = new ArrayList<>();
        int count = NullSkipping.forEachCounting(Arrays.asList(null, "a", null), seen::add);

        assertThat(count).isEqualTo(1);
    }
}
