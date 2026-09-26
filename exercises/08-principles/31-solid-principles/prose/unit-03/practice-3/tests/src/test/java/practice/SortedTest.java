package practice;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SortedTest {

    @Test
    void itemsComeBackInOrder() {
        Sorted.SortedCollection<Integer> numbers = new Sorted.SortedArrayList<>();
        numbers.add(3000);
        numbers.add(1000);
        numbers.add(2000);
        assertThat(numbers.getAll()).containsExactly(1000, 2000, 3000);
        assertThat(numbers.size()).isEqualTo(3);
        Sorted.SortedCollection<String> words = new Sorted.SortedArrayList<>();
        for (String w : new String[] {"pear", "apple", "fig"}) {
            words.add(w);
        }
        assertThat(words.getAll()).containsExactly("apple", "fig", "pear");
    }

    @Test
    void duplicatesAreAccepted() {
        Sorted.SortedCollection<Integer> numbers = new Sorted.SortedArrayList<>();
        numbers.add(Integer.parseInt("2000"));
        numbers.add(1000);
        numbers.add(Integer.parseInt("2000"));
        assertThat(numbers.getAll()).containsExactly(1000, 2000, 2000);
        assertThat(numbers.size()).isEqualTo(3);
        Sorted.SortedCollection<BigDecimal> amounts = new Sorted.SortedArrayList<>();
        amounts.add(new BigDecimal("2.0"));
        amounts.add(new BigDecimal("2.00"));
        amounts.add(new BigDecimal("1.5"));
        assertThat(amounts.size()).isEqualTo(3);
        assertThat(amounts.getAll()).extracting(BigDecimal::toPlainString).containsExactly("1.5", "2.0", "2.00");
    }

    @Test
    void nullIsRejected() {
        Sorted.SortedCollection<Integer> numbers = new Sorted.SortedArrayList<>();
        numbers.add(1000);
        assertThatNullPointerException().isThrownBy(() -> numbers.add(null));
        assertThat(numbers.size()).isEqualTo(1);
        Sorted.SortedCollection<Integer> empty = new Sorted.SortedArrayList<>();
        assertThatNullPointerException().isThrownBy(() -> empty.add(null));
        assertThat(empty.size()).isZero();
        assertThat(empty.getAll()).isEmpty();
    }

    @Test
    void theCallerCannotChangeTheReturnedList() {
        Sorted.SortedCollection<Integer> numbers = new Sorted.SortedArrayList<>();
        numbers.add(2000);
        numbers.add(1000);
        List<Integer> all = numbers.getAll();
        assertThatThrownBy(() -> all.add(500)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> all.set(0, 500)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> all.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(all::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> all.sort(Comparator.reverseOrder())).isInstanceOf(UnsupportedOperationException.class);
        assertThat(numbers.getAll()).containsExactly(1000, 2000);
        assertThat(numbers.size()).isEqualTo(2);
    }
}
