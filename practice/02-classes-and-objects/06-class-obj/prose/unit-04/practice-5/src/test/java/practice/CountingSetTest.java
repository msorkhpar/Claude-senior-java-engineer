package practice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountingSetTest {

    @Test
    void countsEveryAttemptedAdd() {
        CountingSet<String> set = new CountingSet<>(new HashSet<>());
        assertThat(set.add("a")).isTrue();
        assertThat(set.add("a")).isFalse();
        assertThat(set.add("b")).isTrue();
        assertThat(set.addCount()).isEqualTo(3);
        assertThat(set.size()).isEqualTo(2);
        assertThat(set.contains("a")).isTrue();
        assertThat(set.contains("c")).isFalse();
    }

    @Test
    void addAllCountsEachElementOnce() {
        CountingSet<String> set = new CountingSet<>(new HashSet<>());
        set.addAll(List.of("x", "y", "z"));
        assertThat(set.addCount()).isEqualTo(3);
        assertThat(set.size()).isEqualTo(3);
    }

    @Test
    void itWritesThroughToTheWrappedSet() {
        Set<String> inner = new HashSet<>();
        CountingSet<String> set = new CountingSet<>(inner);
        set.add("a");
        set.addAll(List.of("b", "c"));
        assertThat(inner).containsExactlyInAnyOrder("a", "b", "c");
    }
}
