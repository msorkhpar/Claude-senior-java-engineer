package practice;
import java.util.Arrays;
import java.util.concurrent.RecursiveTask;
public final class MergeSortTask extends RecursiveTask<int[]> {
    private final int[] array; private final int threshold;
    public MergeSortTask(int[] array, int threshold) { this.array = array; this.threshold = threshold; }
    @Override protected int[] compute() {
        if (array.length == 0) return array; if (array.length <= threshold) { int[] c = array.clone(); Arrays.sort(c); return c; }
        int mid = array.length / 2;
        MergeSortTask left = new MergeSortTask(Arrays.copyOfRange(array, 0, mid), threshold);
        MergeSortTask right = new MergeSortTask(Arrays.copyOfRange(array, mid, array.length), threshold);
        left.fork();
        int[] r = right.compute();
        int[] l = left.join();
        int[] out = new int[l.length + r.length];
        int i = 0, j = 0, k = 0;
        while (i < l.length && j < r.length) out[k++] = l[i] <= r[j] ? l[i++] : r[j++];
        while (i < l.length) out[k++] = l[i++];
        while (j < r.length) out[k++] = r[j++];
        return out;
    }
}
