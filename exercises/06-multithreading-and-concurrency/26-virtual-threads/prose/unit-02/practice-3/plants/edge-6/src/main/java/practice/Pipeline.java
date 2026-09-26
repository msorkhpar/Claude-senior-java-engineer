package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public final class Pipeline {
    private static final String POISON = new String("end");
    private Pipeline() {
    }

    public static List<String> run(List<String> items, Function<String, String> stage, int workers) {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService executor = Executors.newFixedThreadPool(workers + 1)) {
            executor.submit(() -> {
                for (String item : items) {
                    queue.put(item);
                }
                for (int i = 0; i < workers; i++) {
                    queue.put(POISON);
                }
                return null;
            });
            for (int i = 0; i < workers; i++) {
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
