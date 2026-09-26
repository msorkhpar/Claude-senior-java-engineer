package practice;
import java.util.concurrent.*;
public final class Percent {
    private Percent() {}
    static final class Max extends RecursiveTask<Integer> {
        final int[] v; final int lo, hi, t;
        Max(int[] v, int lo, int hi, int t) { this.v = v; this.lo = lo; this.hi = hi; this.t = t; }
        protected Integer compute() {
            if (hi - lo <= t) { int m = 0; for (int i = lo; i < hi; i++) m = Math.max(m, v[i]); return m; }
            int mid = (lo + hi) >>> 1; Max l = new Max(v, lo, mid, t); l.fork();
            int r = new Max(v, mid, hi, t).compute(); return Math.max(r, l.join());
        }
    }
    static final class Scale extends RecursiveAction {
        final int[] v; final int lo, hi, t, max;
        Scale(int[] v, int lo, int hi, int t, int max) { this.v = v; this.lo = lo; this.hi = hi; this.t = t; this.max = max; }
        protected void compute() {
            if (hi - lo <= t) { for (int i = lo; i < hi; i++) v[i] = (int) ((long) v[i] * 100 / max); return; }
            int mid = (lo + hi) >>> 1; invokeAll(new Scale(v, lo, mid, t, max), new Scale(v, mid, hi, t, max));
        }
    }
    public static void ofMax(ForkJoinPool pool, int[] values, int threshold) {
        if (values.length == 0) return;
        int max = ForkJoinPool.commonPool().invoke(new Max(values, 0, values.length, threshold));
        if (max == 0) return;
        ForkJoinPool.commonPool().invoke(new Scale(values, 0, values.length, threshold, max));
    }
}
