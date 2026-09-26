package practice;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
public final class AdaptiveSum {
    public interface Probe { void leaf(int start, int end); }
    private AdaptiveSum() {}
    static final class T extends RecursiveTask<Long> {
        final long[] a; final int s, e, t; final Probe p;
        T(long[] a, int s, int e, int t, Probe p) { this.a = a; this.s = s; this.e = e; this.t = t; this.p = p; }
        protected Long compute() {
            if (e - s <= t) { p.leaf(s, e); int sum = 0; for (int i = s; i < e; i++) sum += a[i]; return (long) sum; }
            int mid = s + (e - s) / 2;
            T l = new T(a, s, mid, t, p); l.fork();
            long r = new T(a, mid, e, t, p).compute();
            return l.join() + r;
        }
    }
    public static long sum(ForkJoinPool pool, long[] array, int floor, Probe probe) {
        int threshold = Math.max(floor, array.length / (pool.getParallelism() * 4));
        return pool.invoke(new T(array, 0, array.length, threshold, probe));
    }
}
