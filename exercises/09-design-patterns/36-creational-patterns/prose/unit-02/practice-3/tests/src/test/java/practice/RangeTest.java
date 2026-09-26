package practice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangeTest {

    @Test
    void iteratesFromStartToEndExclusive() {
        List<Integer> seen = new ArrayList<>();
        for (int value : new Range(3, 7)) {
            seen.add(value);
        }

        assertThat(seen).containsExactly(3, 4, 5, 6);
    }

    @Test
    void eachIteratorIsIndependent() {
        Range range = new Range(0, 3);
        Iterator<Integer> first = range.iterator();
        first.next();
        first.next();
        Iterator<Integer> second = range.iterator();

        assertThat(second.next()).isEqualTo(0);
        assertThat(first.next()).isEqualTo(2);
        assertThat(second.next()).isEqualTo(1);
    }

    @Test
    void nextPastTheEndThrows() {
        Iterator<Integer> it = new Range(1, 2).iterator();
        it.next();

        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void anEmptyRangeHasNoElements() {
        Iterator<Integer> it = new Range(5, 5).iterator();

        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(() -> new Range(5, 4)).isInstanceOf(IllegalArgumentException.class);
    }
}
