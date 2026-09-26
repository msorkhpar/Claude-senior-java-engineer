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
            for (int i = 0; i < source.length; i++) {
                if (i == 0 || i == last) {
                    dest[i] = source[i];
                } else {
                    dest[i] = (source[i - 1] + source[i] + source[i + 1]) / 3;
                }
            }
            return;
        }
        int mid = start + length / 2;
        invokeAll(new SmoothAction(source, dest, start, mid, threshold),
                new SmoothAction(source, dest, mid, end, threshold));
    }
}
