package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompareStrategyTest {

    @Test
    void comparesAndPicksWithEachStrategy() {
        List<String> names = List.of("Charlie", "Alice", "Bob");
        assertThat(names.stream().sorted(CompareStrategy.NATURAL.toComparator()).toList())
                .containsExactly("Alice", "Bob", "Charlie");
        assertThat(names.stream().sorted(CompareStrategy.REVERSE.toComparator()).toList())
                .containsExactly("Charlie", "Bob", "Alice");
        assertThat(CompareStrategy.NATURAL.min(names)).isEqualTo("Alice");
        assertThat(CompareStrategy.REVERSE.min(names)).isEqualTo("Charlie");
        assertThat(CompareStrategy.NATURAL.max(List.of(3, 7, 5))).isEqualTo(7);
        assertThat(CompareStrategy.BY_STRING.max(List.of("b", "a"))).isEqualTo("b");
    }

    @Test
    void byStringComparesTheText() {
        assertThat(CompareStrategy.BY_STRING.min(List.of(9, 10, 100))).isEqualTo(10);
        assertThat(CompareStrategy.BY_STRING.compare(9, 10)).isPositive();
        assertThat(CompareStrategy.BY_STRING.compare(null, "a")).isPositive();
        assertThat(CompareStrategy.BY_STRING.max(List.of("a", "B"))).isEqualTo("a");
    }

    @Test
    void naturalRefusesAValueThatIsNotComparable() {
        assertThatThrownBy(() -> CompareStrategy.NATURAL.compare(new Object(), new Object()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> CompareStrategy.NATURAL.compare("x", new Object()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> CompareStrategy.REVERSE.compare(new Object(), "x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void anEmptyListHasNoMin() {
        assertThatThrownBy(() -> CompareStrategy.NATURAL.min(List.of())).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> CompareStrategy.BY_STRING.max(List.of())).isInstanceOf(NoSuchElementException.class);
    }
}
