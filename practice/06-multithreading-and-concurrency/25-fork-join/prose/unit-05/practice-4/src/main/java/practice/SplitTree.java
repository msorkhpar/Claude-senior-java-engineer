package practice;

public final class SplitTree {

    public record Shape(int depth, int leaves, int tasks) {
    }

    private SplitTree() {
    }

    /** The depth, leaf count and task count of splitting [0, n) at percent until ranges are at most threshold long. */
    public static Shape shape(int n, int threshold, int percent) {
        throw new UnsupportedOperationException("write shape");
    }
}
