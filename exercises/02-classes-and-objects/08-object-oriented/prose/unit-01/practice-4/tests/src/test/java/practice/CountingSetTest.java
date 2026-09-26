package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountingSetTest {

    @Test
    void countsEveryElementAdded() {
        CountingSet<String> set = new CountingSet<>();
        assertThat(set.add("a")).isTrue();
        assertThat(set.add("b")).isTrue();
        assertThat(set.add("c")).isTrue();
        assertThat(set.getAddCount()).isEqualTo(3);
        assertThat(set.size()).isEqualTo(3);
        assertThat(set.contains("b")).isTrue();
        assertThat(set.contains("z")).isFalse();
    }

    @Test
    void addAllCountsEachElementOnce() {
        CountingSet<String> set = new CountingSet<>();
        assertThat(set.addAll(List.of("x", "y", "z"))).isTrue();
        assertThat(set.getAddCount()).isEqualTo(3);
        assertThat(set.size()).isEqualTo(3);
    }

    @Test
    void aDuplicateStillCountsAsAnAttempt() {
        CountingSet<String> set = new CountingSet<>();
        set.add("a");
        assertThat(set.add("a")).isFalse();
        assertThat(set.getAddCount()).isEqualTo(2);
        assertThat(set.size()).isEqualTo(1);
    }

    @Test
    void equalElementsAreOneElement() {
        CountingSet<String> set = new CountingSet<>();
        String first = new String("apple");
        String second = new String("apple");
        assertThat(set.add(first)).isTrue();
        assertThat(set.add(second)).isFalse();
        assertThat(set.size()).isEqualTo(1);
        assertThat(set.contains(new String("apple"))).isTrue();
        assertThat(set.getAddCount()).isEqualTo(2);
    }
}
