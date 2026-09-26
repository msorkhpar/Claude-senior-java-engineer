package practice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class Fanout {

    private Fanout() {
    }

    /** Runs every fetch at once, each on its own virtual thread; results in key order. */
    public static List<String> fetchAll(List<String> keys, Function<String, String> fetch)
            throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("write fetchAll");
    }
}
