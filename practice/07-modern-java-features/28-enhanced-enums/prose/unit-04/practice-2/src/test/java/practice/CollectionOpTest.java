package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionOpTest {

    @Test
    void eachOperationTransformsAList() {
        List<String> longer = CollectionOp.FILTER.execute(List.of("a", "bb", "ccc"), s -> s.length() > 1);
        List<Integer> sorted = CollectionOp.SORT.execute(new ArrayList<>(List.of(3, 1, 2)));
        List<String> distinct = CollectionOp.DISTINCT.execute(List.of("fig", "fig", "pear"));
        List<Integer> reversed = CollectionOp.REVERSE.execute(new ArrayList<>(List.of(1, 2, 3)));
        assertThat(longer).containsExactly("bb", "ccc");
        assertThat(sorted).containsExactly(1, 2, 3);
        assertThat(distinct).containsExactly("fig", "pear");
        assertThat(reversed).containsExactly(3, 2, 1);
        assertThat(CollectionOp.FILTER.execute(List.of(1, 2))).containsExactly(1, 2);
        assertThat(CollectionOp.SORT.execute(new ArrayList<>(List.of(2, 1, 2)))).containsExactly(1, 2, 2);
    }

    @Test
    void theInputListIsNeverChanged() {
        List<Integer> input = new ArrayList<>(List.of(3, 1, 2));
        List<Integer> sorted = CollectionOp.SORT.execute(input);
        assertThat(sorted).containsExactly(1, 2, 3);
        assertThat(input).containsExactly(3, 1, 2);
        List<Integer> reversed = CollectionOp.REVERSE.execute(input);
        assertThat(reversed).containsExactly(2, 1, 3);
        assertThat(input).containsExactly(3, 1, 2);
        List<Integer> all = new ArrayList<>(List.of(4, 5));
        List<Integer> kept = CollectionOp.FILTER.execute(all, n -> true);
        assertThat(kept).isNotSameAs(all);
        List<Integer> back = CollectionOp.REVERSE.execute(all);
        all.add(6);
        assertThat(back).containsExactly(5, 4);
        assertThat(kept).containsExactly(4, 5);
    }

    @Test
    void distinctKeepsFirstOccurrenceOrder() {
        assertThat(CollectionOp.DISTINCT.execute(List.of("pear", "fig", "pear", "apple")))
                .containsExactly("pear", "fig", "apple");
    }
}
