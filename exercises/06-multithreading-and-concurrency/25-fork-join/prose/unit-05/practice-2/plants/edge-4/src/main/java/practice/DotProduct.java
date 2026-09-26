package practice;
import java.util.concurrent.RecursiveTask;
public final class DotProduct extends RecursiveTask<Long> {
    public interface Probe { void leaf(int start, int end); }
    private final int[] a, b; private final int s, e, t; private final Probe p;
    public DotProduct(int[] a, int[] b, int start, int end, int threshold, Probe probe) {
        if (a.length < b.length) throw new IllegalArgumentException("b is longer than a");
        this.a = a; this.b = b; s = start; e = end; t = threshold; p = probe;
    }
    @Override protected Long compute() {
        if (e - s <= t) { p.leaf(s, e); long sum = 0; for (int i = s; i < e; i++) sum += (long) a[i] * b[i]; return sum; }
        int mid = s + (e - s) / 2;
        DotProduct l = new DotProduct(a, b, s, mid, t, p); l.fork();
        long r = new DotProduct(a, b, mid, e, t, p).compute();
        return l.join() + r;
    }
}
