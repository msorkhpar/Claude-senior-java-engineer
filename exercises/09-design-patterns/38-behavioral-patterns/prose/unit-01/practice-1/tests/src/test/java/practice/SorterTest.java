package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import practice.Sorter.SortStrategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorterTest {

    private static SortStrategy<Integer> by(String name, Comparator<Integer> order) {
        return new SortStrategy<>() {
            @Override
            public List<Integer> sort(List<Integer> data) {
                List<Integer> copy = new ArrayList<>(data);
                copy.sort(order);
                return copy;
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    private static final SortStrategy<Integer> ASCENDING = by("ascending", Comparator.naturalOrder());
    private static final SortStrategy<Integer> DESCENDING = by("descending", Comparator.reverseOrder());

    @Test
    void sortsWithTheStrategyItHolds() {
        Sorter<Integer> sorter = new Sorter<>(ASCENDING);

        assertThat(sorter.sort(List.of(300, 100, 200))).containsExactly(100, 200, 300);
        assertThat(sorter.getStrategy()).isSameAs(ASCENDING);
    }

    @Test
    void aSwitchedStrategyIsUsedFromTheNextCall() {
        Sorter<Integer> sorter = new Sorter<>(ASCENDING);
        sorter.sort(List.of(3, 1, 2));

        sorter.setStrategy(DESCENDING);

        assertThat(sorter.sort(List.of(3, 1, 2))).containsExactly(3, 2, 1);
        assertThat(sorter.getStrategy()).isSameAs(DESCENDING);
    }

    @Test
    void theSetterRefusesNullToo() {
        Sorter<Integer> sorter = new Sorter<>(ASCENDING);

        assertThatThrownBy(() -> sorter.setStrategy(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(sorter.sort(List.of(3, 1, 2))).containsExactly(1, 2, 3);
    }

    @Test
    void theConstructorRefusesNull() {
        assertThatThrownBy(() -> new Sorter<Integer>(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
