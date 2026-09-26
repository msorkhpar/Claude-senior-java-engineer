package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RemovalTest {

    @Test
    void removesAValueEqualToItsIndex() {
        List<Integer> values = new ArrayList<>(List.of(0, 1, 2));
        assertThat(Removal.removeValue(values, 1)).isTrue();
        assertThat(values).containsExactly(0, 2);
    }

    @Test
    void removesByValueNotByPosition() {
        List<Integer> values = new ArrayList<>(List.of(10, 20, 30));
        assertThat(Removal.removeValue(values, 20)).isTrue();
        assertThat(values).containsExactly(10, 30);
        List<Integer> more = new ArrayList<>(List.of(3, 2, 1));
        Removal.removeValue(more, 1);
        assertThat(more).containsExactly(3, 2);
    }

    @Test
    void aMissingValueRemovesNothing() {
        List<Integer> values = new ArrayList<>(List.of(0, 1, 2));
        assertThat(Removal.removeValue(values, 9)).isFalse();
        assertThat(values).containsExactly(0, 1, 2);
    }

    @Test
    void onlyTheFirstEqualElementGoes() {
        List<Integer> values = new ArrayList<>(List.of(5, 7, 5));
        Removal.removeValue(values, 5);
        assertThat(values).containsExactly(7, 5);
    }

    @Test
    void largeValuesAreComparedByValue() {
        List<Integer> values = new ArrayList<>(List.of(1000, 2000, 1000));
        assertThat(Removal.removeValue(values, 2000)).isTrue();
        assertThat(values).containsExactly(1000, 1000);
        assertThat(Removal.removeValue(values, 1000)).isTrue();
        assertThat(values).containsExactly(1000);
    }
}
