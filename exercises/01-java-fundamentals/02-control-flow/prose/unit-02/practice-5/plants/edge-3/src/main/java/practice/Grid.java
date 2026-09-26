package practice;

public final class Grid {

    private Grid() {
    }

    /** Returns {row, col} of the first cell equal to target, or {-1, -1}. */
    public static int[] find(int[][] grid, int target) {
        int[] found = new int[2];
        search:
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == target) {
                    found = new int[]{row, col};
                    break search;
                }
            }
        }
        return found;
    }
}
