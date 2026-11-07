package practice;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

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
        // TODO: Run the queries on the pool and put each panel on the screen as described above.
        //       The panels of quick queries must not wait for the slow ones.
        throw new IllegalStateException("TODO: load");
    }
}
