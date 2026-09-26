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
        throw new UnsupportedOperationException("write fetchAll");
    }
}
