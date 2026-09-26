package practice;

public final class Stats {

    /** The total of the values. Safe to call from many threads at once on one Stats. */
    public long sum(Iterable<Integer> values) {
        long total = 0;
        for (int v : values) {
            total += v;
        }
        return total;
    }
}
