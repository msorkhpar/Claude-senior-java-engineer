package practice;

import java.util.concurrent.RecursiveAction;

public final class ScaleAction extends RecursiveAction {

    /** Told the range of each base case, before it is changed. */
    public interface Probe {
        void leaf(int start, int end);
    }

    public ScaleAction(double[] array, int start, int end, int threshold, double factor, double offset, Probe probe) {
        throw new UnsupportedOperationException("write the constructor");
    }

    @Override
    protected void compute() {
        throw new UnsupportedOperationException("write compute");
    }
}
