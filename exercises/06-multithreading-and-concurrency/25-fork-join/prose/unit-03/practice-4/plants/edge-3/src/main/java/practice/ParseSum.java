package practice;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
public final class ParseSum {
    private ParseSum() {}
    static final class T extends RecursiveTask<Long> {
        final String[] items; final int s, e, t;
        T(String[] items, int s, int e, int t) { this.items = items; this.s = s; this.e = e; this.t = t; }
        protected Long compute() {
            if (e - s <= t) {
                int sum = 0; for (int i = s; i < e; i++) { try { sum += Integer.parseInt(items[i]); } catch (NumberFormatException x) { throw new NumberFormatException("item " + i + " is not a number: " + items[i]); } } return (long) sum;
            }
            int mid = s + (e - s) / 2;
            T l = new T(items, s, mid, t); l.fork();
            long r = new T(items, mid, e, t).compute();
            return l.join() + r;
        }
    }
    public static long total(ForkJoinPool pool, String[] items, int threshold) {
        try {
            return pool.invoke(new T(items, 0, items.length, threshold));
        } catch (NumberFormatException e) {
            Throwable c = e; while (c.getCause() instanceof NumberFormatException) c = c.getCause(); throw new IllegalArgumentException(c.getMessage());
        }
    }
}
