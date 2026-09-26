package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GridTest {

    @Test
    void findsTheCell() {
        int[][] grid = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        assertThat(Grid.find(grid, 5)).containsExactly(1, 1);
        assertThat(Grid.find(grid, 9)).containsExactly(2, 2);
        assertThat(Grid.find(grid, 1)).containsExactly(0, 0);
    }

    @Test
    void theFirstMatchWins() {
        int[][] grid = {{1, 7}, {7, 2}, {3, 7}};
        assertThat(Grid.find(grid, 7)).containsExactly(0, 1);
    }

    @Test
    void rowsMayDifferInLength() {
        int[][] grid = {{1}, {2, 3, 4}, {5}};
        assertThat(Grid.find(grid, 4)).containsExactly(1, 2);
        assertThat(Grid.find(grid, 5)).containsExactly(2, 0);
    }

    @Test
    void aMissingValueIsMinusOne() {
        assertThat(Grid.find(new int[][]{{1, 2}, {3, 4}}, 9)).containsExactly(-1, -1);
        assertThat(Grid.find(new int[][]{}, 9)).containsExactly(-1, -1);
    }
}
