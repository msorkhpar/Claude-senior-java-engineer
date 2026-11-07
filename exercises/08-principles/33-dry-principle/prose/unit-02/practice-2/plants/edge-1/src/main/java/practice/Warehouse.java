package practice;

import java.util.Map;

/** Stock in the bins of one warehouse. Every move between bins, and every stock count, goes through this class. */
public class Warehouse {

    /** One storage bin. Each bin is thread-safe on its own. */
    public interface Bin {
        int count();

        /** Removes qty units if the bin holds that many, and says whether it did. */
        boolean take(int qty);

        void add(int qty);
    }

    /** The bin used in production. */
    public static final class Shelf implements Bin {
        private int units;

        public Shelf(int units) {
            this.units = units;
        }

        @Override
        public synchronized int count() {
            return units;
        }

        @Override
        public synchronized boolean take(int qty) {
            if (units < qty) return false;
            units -= qty;
            return true;
        }

        @Override
        public synchronized void add(int qty) {
            units += qty;
        }
    }

    private final Map<String, Bin> bins;

    public Warehouse(Map<String, Bin> bins) {
        this.bins = Map.copyOf(bins);
    }

    public int count(String bin) {
        return bins.get(bin).count();
    }

    /** Moves qty units from one bin to another; moves nothing, and returns false, if the source holds fewer. */
    boolean move(String from, String to, int qty) {
        if (!bins.get(from).take(qty)) return false;
        bins.get(to).add(qty);
        return true;
    }

    /** The units in all bins together, for the hourly stock count. */
    synchronized int total() {
        int sum = 0;
        for (Bin bin : bins.values()) sum += bin.count();
        return sum;
    }
}
