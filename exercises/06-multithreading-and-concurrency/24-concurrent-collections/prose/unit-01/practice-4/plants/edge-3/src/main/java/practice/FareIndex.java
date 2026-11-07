package practice;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * The fares, in cents, currently on sale for one route. Pricing threads add and withdraw fares
 * all the time; the search page asks for the fares in a price range and shows them as a list
 * that stays as it was when the search ran.
 */
public class FareIndex {

    private final ConcurrentSkipListSet<Integer> fares = new ConcurrentSkipListSet<>();

    public void offer(int cents) {
        fares.add(cents);
    }

    public void withdraw(int cents) {
        fares.remove(cents);
    }

    /**
     * The fares from min to max, both included, lowest first, as they are now: fares offered or
     * withdrawn later don't change the answer.
     */
    NavigableSet<Integer> between(int min, int max) {
        return new TreeSet<>(fares.subSet(min, true, max, true));
    }

    /** The fares below the limit, the limit itself not included, lowest first, as they are now. */
    NavigableSet<Integer> under(int limit) {
        return new TreeSet<>(fares.headSet(limit, true));
    }
}
