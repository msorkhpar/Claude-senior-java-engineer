package practice;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntArrayListTest {

    @Test
    void presentsTheBufferAsAListOfItsValues() {
        int[] buffer = {412, -8, 0, 1290, 37};

        List<Integer> readings = new IntArrayList(buffer);

        assertThat(readings).hasSize(5);
        assertThat(readings.get(3)).isEqualTo(1290);
        assertThat(readings).containsExactly(412, -8, 0, 1290, 37);
        assertThat(readings.contains(-8)).isTrue();
        assertThat(readings.indexOf(37)).isEqualTo(4);
    }

    @Test
    void keepsTheBuffersSizeAndRefusesNull() {
        List<Integer> readings = new IntArrayList(new int[] {5, 6});

        assertThatThrownBy(() -> readings.add(7)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new IntArrayList(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aValueSetThroughTheListIsReadBack() {
        List<Integer> readings = new IntArrayList(new int[] {14, 15, 16});

        readings.set(1, -3);

        assertThat(readings).containsExactly(14, -3, 16);
    }

    @Test
    void theListAndTheBufferAreTheSameData() {
        int[] buffer = {70, 20, 50, 10};
        List<Integer> readings = new IntArrayList(buffer);

        buffer[0] = 90;                                   // the sensor library writes a new sample
        assertThat(readings.get(0)).isEqualTo(90);

        Collections.sort(readings);                       // the analytics code sorts in place
        assertThat(buffer).containsExactly(10, 20, 50, 90);
    }

    @Test
    void setAnswersTheValueItReplaced() {
        int[] buffer = {3, 1, 4, 1, 5, 9, 2};
        List<Integer> readings = new IntArrayList(buffer);

        assertThat(readings.set(2, 40)).isEqualTo(4);

        Collections.reverse(readings);
        assertThat(buffer).containsExactly(2, 9, 5, 1, 40, 1, 3);
        Collections.swap(readings, 0, 6);
        assertThat(buffer).containsExactly(3, 9, 5, 1, 40, 1, 2);
    }
}
