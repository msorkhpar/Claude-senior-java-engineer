package practice;

public final class SplitTree {

    public record Shape(int depth, int leaves, int tasks) {
    }

    private SplitTree() {
    }

    /** The depth, leaf count and task count of splitting [0, n) at percent until ranges are at most threshold long. */
    public static Shape shape(int n, int threshold, int percent) {
        return walk(0, n, threshold, percent, 0);
    }

    private static Shape walk(int start, int end, int threshold, int percent, int level) {
        int length = end - start;
        if (length <= threshold) {
            return new Shape(level, 1, 1);
        }
        int split = start + (int) ((long) length * percent / 100);
        if (split <= start) {
            split = start + 1;
        }
        Shape left = walk(start, split, threshold, percent, level + 1);
        Shape right = walk(split, end, threshold, percent, level + 1);
        return new Shape(Math.max(left.depth(), right.depth()), left.leaves() + right.leaves(), left.tasks() + right.tasks() + 1);
    }
}
