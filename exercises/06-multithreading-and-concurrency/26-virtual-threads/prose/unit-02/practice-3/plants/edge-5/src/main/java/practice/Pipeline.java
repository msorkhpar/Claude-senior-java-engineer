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

    private Pipeline() {
    }

    public static List<String> run(List<String> items, Function<String, String> stage, int workers) {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(items.size());
        Thread.ofVirtual().start(() -> {
            try {
                for (String item : items) {
                    queue.put(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        for (int i = 0; i < workers; i++) {
            Thread.ofVirtual().start(() -> {
                try {
                    while (true) {
                        String item = queue.take();
                        results.add(stage.apply(item));
                        done.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        try {
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>(results);
    }
}
