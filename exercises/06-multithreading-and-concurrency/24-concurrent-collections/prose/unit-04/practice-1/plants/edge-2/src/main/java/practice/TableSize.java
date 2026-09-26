package practice;

public final class TableSize {

    private TableSize() {
    }

    /** The smallest power of two at least requested; 1 for zero or less. */
    public static int tableSizeFor(int requested) {
        int n = requested - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n + 1;
    }
}
