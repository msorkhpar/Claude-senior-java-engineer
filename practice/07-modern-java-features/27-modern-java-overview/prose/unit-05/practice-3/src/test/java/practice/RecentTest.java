package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class RecentTest {

    @Test
    void newestFirstFromAList() {
        List<String> items = new ArrayList<>(List.of("a", "b", "c", "d"));
        assertThat(Recent.newestFirst(items, 2)).containsExactly("d", "c");
        assertThat(Recent.newestFirst(items, 0)).isEmpty();
        assertThat(Recent.oldest(items)).contains("a");
        assertThat(items).containsExactly("a", "b", "c", "d");
    }

    @Test
    void emptyHasNoOldest() {
        assertThat(Recent.oldest(new ArrayList<String>())).isEmpty();
        assertThat(Recent.newestFirst(new ArrayList<String>(), 3)).isEmpty();
    }

    @Test
    void resultIsASnapshot() {
        List<Integer> items = new ArrayList<>(List.of(1000, 2000, 3000));
        List<Integer> recent = Recent.newestFirst(items, 10);
        assertThat(recent).containsExactly(3000, 2000, 1000);
        items.add(4000);
        items.set(0, 5000);
        assertThat(recent).containsExactly(3000, 2000, 1000);
    }

    @Test
    void worksOnSetsAndDeques() {
        LinkedHashSet<String> set = new LinkedHashSet<>(List.of("x", "y", "z"));
        assertThat(Recent.newestFirst(set, 2)).containsExactly("z", "y");
        assertThat(Recent.oldest(set)).contains("x");
        TreeSet<Integer> sorted = new TreeSet<>(List.of(30, 10, 20));
        assertThat(Recent.newestFirst(sorted, 5)).containsExactly(30, 20, 10);
        ArrayDeque<String> deque = new ArrayDeque<>(List.of("p", "q"));
        assertThat(Recent.newestFirst(deque, 1)).containsExactly("q");
        assertThat(Recent.oldest(deque)).contains("p");
    }
}
