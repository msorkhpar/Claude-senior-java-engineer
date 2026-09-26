package practice;

public final class TableSize {

    private TableSize() {
    }

    /** The smallest power of two at least requested; 1 for zero or less. */
    public static int tableSizeFor(int requested) {
        if (requested <= 0) {
            return 1;
        }
        return Integer.highestOneBit(requested) << 1;
    }
}
