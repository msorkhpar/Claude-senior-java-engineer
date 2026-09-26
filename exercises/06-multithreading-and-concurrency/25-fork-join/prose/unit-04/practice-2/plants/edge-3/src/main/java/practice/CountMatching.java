package practice;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
public final class CountMatching extends RecursiveAction {
    private final int[] a; private final int s, e, t, target; private final AtomicInteger c;
    public CountMatching(int[] array, int start, int end, int threshold, int target, AtomicInteger counter) {
        if (counter == null) throw new IllegalArgumentException("counter");
        a = array; s = start; e = end; t = threshold; this.target = target; c = counter;
    }
    @Override protected void compute() {
        if (e - s <= t) {
            int local = 0; for (int i = s; i < e; i++) if (a[i] == target) local++;
            c.addAndGet(local);
            return;
        }
        int mid = s + (e - s) / 2;
        CountMatching l = new CountMatching(a, s, mid, t, target, c), r = new CountMatching(a, mid, e, t, target, c);
        l.fork(); r.fork();
    }
}
