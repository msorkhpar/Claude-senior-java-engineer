package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RanksTest {

    private static ConcurrentSkipListSet<String> fruit() {
        ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();
        for (String s : new String[] {"banana", "apple", "date", "cherry"}) {
            set.add(new String(s.toCharArray()));
        }
        return set;
    }

    @Test
    void readsRangesInSortedOrder() {
        ConcurrentSkipListSet<String> set = fruit();
        assertThat(Ranks.between(set, "apple", "c")).containsExactly("apple", "banana");
        assertThat(Ranks.between(set, "b", "d")).containsExactly("banana", "cherry");
        assertThat(Ranks.ends(set)).containsExactly("apple", "date");
    }

    @Test
    void includesTheUpperBound() {
        ConcurrentSkipListSet<String> set = fruit();
        assertThat(Ranks.between(set, "apple", "cherry")).containsExactly("apple", "banana", "cherry");
        assertThat(Ranks.between(set, "date", "date")).containsExactly("date");
    }

    @Test
    void emptySetHasNoEnds() {
        assertThat(Ranks.ends(new ConcurrentSkipListSet<>())).isEmpty();
        ConcurrentSkipListSet<String> one = new ConcurrentSkipListSet<>(List.of("kiwi"));
        assertThat(Ranks.ends(one)).containsExactly("kiwi", "kiwi");
        assertThat(one).as("ends reads the set without changing it").containsExactly("kiwi");
        ConcurrentSkipListSet<String> set = fruit();
        assertThat(Ranks.ends(set)).containsExactly("apple", "date");
        assertThat(set).as("ends reads the set without changing it").hasSize(4);
    }

    @Test
    void rangeIsACopy() {
        ConcurrentSkipListSet<String> set = fruit();
        NavigableSet<String> range = Ranks.between(set, "apple", "c");
        set.add(new String("blueberry".toCharArray()));
        set.remove("apple");
        assertThat(range).containsExactly("apple", "banana");
    }
}
