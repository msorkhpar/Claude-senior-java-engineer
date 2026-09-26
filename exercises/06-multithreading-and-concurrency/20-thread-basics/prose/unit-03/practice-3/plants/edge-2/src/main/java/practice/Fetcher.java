package practice;

import java.util.ArrayList;
import java.util.List;

public final class Fetcher {

    private Fetcher() {
    }

    /** One blocking fetch. */
    @FunctionalInterface
    public interface Fetch {
        String get() throws InterruptedException;
    }

    /** Runs each fetch in order and returns the results; an interrupt stops it and reaches the caller. */
    public static List<String> fetchAll(List<Fetch> fetches) throws InterruptedException {
        List<String> results = new ArrayList<>();
        InterruptedException first = null;
        for (Fetch fetch : fetches) {
            try {
                results.add(fetch.get());
            } catch (InterruptedException e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        if (first != null) {
            throw first;
        }
        return results;
    }
}
