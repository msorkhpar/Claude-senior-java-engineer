package practice;
import java.util.concurrent.RecursiveTask;
public final class MinMaxTask extends RecursiveTask<MinMaxTask.Range> {
    public record Range(int min, int max) {}
    private final int[] a; private final int s, e, t;
    public MinMaxTask(int[] array, int start, int end, int threshold) {
        if (start >= end) throw new IllegalArgumentException("empty range");
        this.a = array; this.s = start; this.e = end; this.t = threshold;
    }
    @Override protected Range compute() {
        if (e - s <= t) {
            int mn = a[0], mx = a[0];
            for (int i = s; i < e; i++) { mn = Math.min(mn, a[i]); mx = Math.max(mx, a[i]); }
            return new Range(mn, mx);
        }
        int mid = s + (e - s) / 2;
        MinMaxTask l = new MinMaxTask(a, s, mid, t); l.fork();
        Range r = new MinMaxTask(a, mid, e, t).compute(); Range lr = l.join();
        return new Range(Math.min(lr.min(), r.min()), Math.max(lr.max(), r.max()));
    }
}
