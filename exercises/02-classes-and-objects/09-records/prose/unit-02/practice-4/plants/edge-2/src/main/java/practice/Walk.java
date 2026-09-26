package practice;

import java.util.HashSet;
import java.util.Set;

public final class Walk {

    /** One cell of the grid. */
    public record Cell(int row, int col) {}

    private Walk() {
    }

    /** The first cell the walk enters a second time, or null if there is none. */
    public static Cell firstRevisit(String path) {
        Set<Cell> seen = new HashSet<>();
        Cell last = null;
        Cell here = new Cell(0, 0);
        seen.add(here);
        for (char step : path.toCharArray()) {
            here = switch (step) {
                case 'U' -> new Cell(here.row() - 1, here.col());
                case 'D' -> new Cell(here.row() + 1, here.col());
                case 'L' -> new Cell(here.row(), here.col() - 1);
                case 'R' -> new Cell(here.row(), here.col() + 1);
                default -> throw new IllegalArgumentException("not a step: " + step);
            };
            if (!seen.add(here)) {
                last = here;
            }
        }
        return last;
    }
}
