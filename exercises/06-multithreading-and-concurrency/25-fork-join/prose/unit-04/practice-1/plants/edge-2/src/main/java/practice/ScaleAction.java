package practice;

import java.util.concurrent.RecursiveAction;

public final class ScaleAction extends RecursiveAction {

    /** Told the range of each base case, before it is changed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    private final double[] array;
    private final int start;
    private final int end;
    private final int threshold;
    private final double factor;
    private final double offset;
    private final Probe probe;

    public ScaleAction(double[] array, int start, int end, int threshold, double factor, double offset, Probe probe) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        this.array = array;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
        this.factor = factor;
        this.offset = offset;
        this.probe = probe;
    }

    @Override
    protected void compute() {
        int length = end - start;
        if (length <= threshold) {
            probe.leaf(start, end);
            for (int i = start; i < end; i++) {
                array[i] = array[i] * factor + offset;
            }
            return;
        }
        int mid = start + length / 2;
        invokeAll(new ScaleAction(array, start, mid + 1, threshold, factor, offset, probe),
                new ScaleAction(array, mid, end, threshold, factor, offset, probe));
    }
}
