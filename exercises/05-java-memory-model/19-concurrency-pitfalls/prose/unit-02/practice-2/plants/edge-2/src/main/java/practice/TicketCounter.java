package practice;

import java.util.concurrent.atomic.AtomicInteger;

public final class TicketCounter {

    /** A counter cell that can only be changed by compare-and-set. */
    public interface Cell {
        int get();

        boolean compareAndSet(int expected, int next);

        /** A cell backed by an AtomicInteger, starting at 0. */
        static Cell atomic() {
            AtomicInteger value = new AtomicInteger();
            return new Cell() {
                @Override
                public int get() {
                    return value.get();
                }

                @Override
                public boolean compareAndSet(int expected, int next) {
                    return value.compareAndSet(expected, next);
                }
            };
        }
    }

    private final Cell sold;
    private final int capacity;

    public TicketCounter(Cell sold, int capacity) {
        this.sold = sold;
        this.capacity = capacity;
    }

    /** Sells one ticket; false when all tickets are sold. */
    public boolean sell() {
        int current = sold.get();
        if (current >= capacity) {
            return false;
        }
        while (!sold.compareAndSet(current, current + 1)) {
            current = sold.get();
        }
        return true;
    }

    /** Returns how many tickets are sold. */
    public int sold() {
        return sold.get();
    }
}
