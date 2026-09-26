package practice;

import java.util.concurrent.RecursiveAction;

public final class SmoothAction extends RecursiveAction {

    private final int[] source;
    private final int[] dest;
    private final int start;
    private final int end;
    private final int threshold;

    public SmoothAction(int[] source, int[] dest, int start, int end, int threshold) {
        this.source = source;
        this.dest = dest;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
    }

    @Override
    protected void compute() {
        int length = end - start;
        if (length <= threshold) {
            int last = source.length - 1;
            for (int i = start; i < end; i++) {
                int before = i == 0 ? 0 : source[i - 1];
                int after = i == last ? 0 : source[i + 1];
                dest[i] = (before + source[i] + after) / 3;
            }
            return;
        }
        int mid = start + length / 2;
        invokeAll(new SmoothAction(source, dest, start, mid, threshold),
                new SmoothAction(source, dest, mid, end, threshold));
    }
}
