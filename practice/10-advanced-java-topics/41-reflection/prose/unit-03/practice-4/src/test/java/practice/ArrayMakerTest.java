package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrayMakerTest {

    @Test
    void fillsResizesAndBuildsGrids() {
        assertThat(ArrayMaker.filled(String.class, 3, "x"))
                .isInstanceOf(String[].class)
                .isEqualTo(new String[] {"x", "x", "x"});
        assertThat(ArrayMaker.filled(int.class, 2, 7)).isInstanceOf(int[].class).isEqualTo(new int[] {7, 7});
        assertThat(ArrayMaker.resize(new String[] {"a", "b"}, 3))
                .isInstanceOf(String[].class)
                .isEqualTo(new String[] {"a", "b", null});
        Object grid = ArrayMaker.grid(String.class, 2, 3);
        assertThat(grid).isInstanceOf(String[][].class);
        assertThat(((String[][]) grid).length).isEqualTo(2);
    }

    @Test
    void resizeKeepsThePrimitiveType() {
        Object resized = ArrayMaker.resize(new int[] {1, 2}, 4);

        assertThat(resized).isInstanceOf(int[].class);
        assertThat((int[]) resized).containsExactly(1, 2, 0, 0);
    }

    @Test
    void shrinkingKeepsTheFirstElements() {
        assertThat(ArrayMaker.resize(new String[] {"a", "b", "c"}, 2)).isEqualTo(new String[] {"a", "b"});
    }

    @Test
    void nullCannotFillAPrimitiveArray() {
        assertThatThrownBy(() -> ArrayMaker.filled(int.class, 2, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gridCreatesEveryRow() {
        int[][] grid = (int[][]) ArrayMaker.grid(int.class, 2, 3);

        assertThat(grid).hasNumberOfRows(2);
        assertThat(grid[0]).hasSize(3);
        assertThat(grid[1]).hasSize(3);
    }
}
