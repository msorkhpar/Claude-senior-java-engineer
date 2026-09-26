package practice;

public final class Stats {

    private long total;

    /** The total of the values. Safe to call from many threads at once on one Stats. */
    public synchronized long sum(Iterable<Integer> values) {
        total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }
}
