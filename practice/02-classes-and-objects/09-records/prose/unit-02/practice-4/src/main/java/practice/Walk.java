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
        throw new UnsupportedOperationException("write firstRevisit");
    }
}
