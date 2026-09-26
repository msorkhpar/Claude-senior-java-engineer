package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public final class Fanout {

    private Fanout() {
    }

    /** Runs every fetch at once, each on its own virtual thread; results in key order. */
    public static List<String> fetchAll(List<String> keys, Function<String, String> fetch)
            throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();
            for (String key : keys) {
                futures.add(executor.submit(() -> fetch.apply(key)));
            }
            List<String> results = new ArrayList<>();
            for (Future<String> future : futures) {
                results.add(future.get());
            }
            return results;
        }
    }
}
