package practice;

import java.util.List;

/**
 * The morning routine of a small warehouse: bring every shelf up to its minimum level, then report
 * how many items the night shift counted. A shelf the night shift didn't reach has a null count.
 */
public class Warehouse {

    private final int[] levels;
    private final List<Integer> counts;

    public Warehouse(int[] levels, List<Integer> counts) {
        this.levels = levels;
        this.counts = counts;
    }

    public String morningReport(int minimum) {
        int raised = raiseToMinimum(levels, minimum);
        return raised + " shelves restocked, " + countedTotal(counts) + " items counted";
    }

    /** Raises every level below minimum to minimum, and returns how many levels it raised. */
    int raiseToMinimum(int[] levels, int minimum) {
        // TODO: Bring every shelf below the minimum up to it, so the next reader of levels sees
        //       the new stock, and return how many shelves you raised.
        throw new IllegalStateException("TODO: raiseToMinimum");
    }

    /** The sum of the counts the night shift recorded. */
    int countedTotal(List<Integer> counts) {
        // TODO: Add up the recorded counts. A shelf nobody counted holds null and adds nothing.
        throw new IllegalStateException("TODO: countedTotal");
    }
}
