package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The operations dashboard. Each panel runs its own query on the pool, and the screen shows a
 * panel as soon as its query has an answer.
 */
public class Dashboard {

    /** Where panels appear. show() may be called from any thread. */
    public interface Screen {
        void show(String panel, String content);
    }

    private final ExecutorService pool;
    private final Screen screen;

    public Dashboard(ExecutorService pool, Screen screen) {
        this.pool = pool;
        this.screen = screen;
    }

    /**
     * Runs every panel's query on the pool and shows each panel on the screen: its query's answer,
     * or "unavailable" when the query failed. Returns once every panel is on the screen.
     */
    public void load(Map<String, Callable<String>> queries) throws InterruptedException {
        Map<String, Future<String>> answers = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Callable<String>> q : queries.entrySet()) {
            answers.put(q.getKey(), pool.submit(q.getValue()));
        }
        for (Map.Entry<String, Future<String>> a : answers.entrySet()) {
            try {
                screen.show(a.getKey(), a.getValue().get());
            } catch (ExecutionException e) {
                screen.show(a.getKey(), "unavailable");
            }
        }
    }
}
