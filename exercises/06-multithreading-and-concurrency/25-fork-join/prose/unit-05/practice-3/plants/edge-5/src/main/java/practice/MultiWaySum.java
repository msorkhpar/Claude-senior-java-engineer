package practice;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
public final class MultiWaySum extends RecursiveTask<Long> {
    public interface Probe { void leaf(int start, int end); }
    private final long[] a; private final int s, e, ways, t; private final Probe p;
    public MultiWaySum(long[] array, int start, int end, int ways, int threshold, Probe probe) { a = array; s = start; e = end; this.ways = ways; t = threshold; p = probe; }
    @Override protected Long compute() {
        int len = e - s;
        if (len < t || ways <= 1) { p.leaf(s, e); long sum = 0; for (int i = s; i < e; i++) sum += a[i]; return sum; }
        int w = Math.min(ways, len); int size = len / w;
        List<MultiWaySum> forked = new ArrayList<>();
        for (int i = 0; i < w - 1; i++) { MultiWaySum c = new MultiWaySum(a, s + i * size, s + i * size + size, 1, t, p); c.fork(); forked.add(c); }
        long total = new MultiWaySum(a, s + (w - 1) * size, e, 1, t, p).compute();
        for (int i = forked.size() - 1; i >= 0; i--) total += forked.get(i).join();
        return total;
    }
}
