package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public final class Fanout {
    private static final int MAX_IN_FLIGHT = 1000;
    private Fanout() {
    }
    public static List<String> fetchAll(List<String> keys, Function<String, String> fetch)
            throws InterruptedException, ExecutionException {
        try (ExecutorService ex = Executors.newFixedThreadPool(MAX_IN_FLIGHT, Thread.ofVirtual().factory())) {
            List<Future<String>> fs = new ArrayList<>();
            for (String k : keys) {
                fs.add(ex.submit(() -> fetch.apply(k)));
            }
            List<String> out = new ArrayList<>();
            for (Future<String> f : fs) {
                out.add(f.get());
            }
            return out;
        }
    }
}
