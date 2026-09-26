package practice;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

public final class MergeSortTask extends RecursiveTask<int[]> {

    private final int[] array;
    private final int threshold;

    public MergeSortTask(int[] array, int threshold) {
        this.array = array;
        this.threshold = threshold;
    }

    @Override
    protected int[] compute() {
        if (array.length <= 1) {
            return array; // nothing to sort
        }
        if (array.length <= threshold) {
            int[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(copy);
            return copy;
        }
        int mid = array.length / 2;
        MergeSortTask left = new MergeSortTask(Arrays.copyOfRange(array, 0, mid), threshold);
        MergeSortTask right = new MergeSortTask(Arrays.copyOfRange(array, mid, array.length), threshold);
        left.fork();
        int[] rightSorted = right.compute();
        int[] leftSorted = left.join();
        return merge(leftSorted, rightSorted);
    }

    private static int[] merge(int[] a, int[] b) {
        int[] result = new int[a.length + b.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) {
                result[k++] = a[i++];
            } else {
                result[k++] = b[j++];
            }
        }
        while (i < a.length) {
            result[k++] = a[i++];
        }
        while (j < b.length) {
            result[k++] = b[j++];
        }
        return result;
    }
}
