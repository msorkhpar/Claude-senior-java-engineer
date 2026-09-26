package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BubbleSortTest {

    @Test
    void sortsIntoANewList() {
        BubbleSort<Integer> sort = new BubbleSort<>();

        assertThat(sort.sort(List.of(5, 3, 8, 1, 3))).containsExactly(1, 3, 3, 5, 8);
        assertThat(new BubbleSort<String>().sort(List.of("pear", "apple", "fig"))).containsExactly("apple", "fig", "pear");
        assertThat(sort.name()).isEqualTo("BubbleSort");
    }

    @Test
    void theCallersListIsNotChanged() {
        List<Integer> data = new ArrayList<>(List.of(5, 3, 8, 1, 3));

        List<Integer> sorted = new BubbleSort<Integer>().sort(data);

        assertThat(sorted).containsExactly(1, 3, 3, 5, 8);
        assertThat(data).containsExactly(5, 3, 8, 1, 3);
    }

    @Test
    void aShortListStillComesBackAsANewList() {
        BubbleSort<Integer> sort = new BubbleSort<>();
        List<Integer> one = new ArrayList<>(List.of(420));
        List<Integer> empty = new ArrayList<>();

        List<Integer> fromOne = sort.sort(one);
        List<Integer> fromEmpty = sort.sort(empty);
        fromOne.add(999);
        fromEmpty.add(999);

        assertThat(one).containsExactly(420);
        assertThat(empty).isEmpty();
    }

    @Test
    void nullDataIsRefused() {
        assertThatThrownBy(() -> new BubbleSort<Integer>().sort(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
