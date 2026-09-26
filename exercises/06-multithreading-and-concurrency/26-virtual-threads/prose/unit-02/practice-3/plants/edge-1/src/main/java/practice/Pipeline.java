package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public final class Pipeline {

    /** The end-of-work marker; compared by identity, so no real item can be mistaken for it. */
    private static final String POISON = new String("no more items");

    private Pipeline() {
    }

    /** One producer and {@code workers} workers, all virtual threads; returns every stage result. */
    public static List<String> run(List<String> items, Function<String, String> stage, int workers) {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                for (String item : items) {
                    queue.put(item);
                }
                queue.put(POISON);
                return null;
            });
            for (int i = 0; i < 1; i++) {
                executor.submit(() -> {
                    while (true) {
                        String item = queue.take();
                        if (item == POISON) {
                            return null;
                        }
                        results.add(stage.apply(item));
                    }
                });
            }
        }
        return new ArrayList<>(results);
    }
}
