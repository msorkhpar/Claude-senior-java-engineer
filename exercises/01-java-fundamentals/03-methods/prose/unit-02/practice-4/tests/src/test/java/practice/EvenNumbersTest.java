package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EvenNumbersTest {

    @Test
    void listsTheEvenNumbers() {
        assertThat(EvenNumbers.first(3)).containsExactly(0, 2, 4);
        assertThat(EvenNumbers.first(1)).containsExactly(0);
    }

    @Test
    void noCountGivesAnEmptyList() {
        assertThat(EvenNumbers.first(0)).isNotNull().isEmpty();
        assertThat(EvenNumbers.first(-5)).isNotNull().isEmpty();
    }

    @Test
    void theListIsTheCallersOwn() {
        java.util.List<Integer> list = EvenNumbers.first(0);
        list.add(42);
        assertThat(list).containsExactly(42);
        assertThat(EvenNumbers.first(0)).isEmpty();
        java.util.List<Integer> two = EvenNumbers.first(2);
        two.add(99);
        assertThat(EvenNumbers.first(2)).containsExactly(0, 2);
    }
}
