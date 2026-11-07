package practice;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Indexes search terms on a thread of its own. Before it can index anything it loads its
 * synonym table, which can take a while; the search service calls start() at boot and sends the
 * first terms as soon as start() returns.
 */
public class IndexWorker {

    /** Loads the synonym table: may wait for a lock or compute for a while. */
    public interface Loader {
        Map<String, String> load() throws InterruptedException;
    }

    private final Loader loader;
    private final Consumer<String> index;
    private final BlockingQueue<String> terms = new LinkedBlockingQueue<>();
    private final CountDownLatch ready = new CountDownLatch(1);
    private volatile Map<String, String> synonyms;
    private Thread thread;

    public IndexWorker(Loader loader, Consumer<String> index) {
        this.loader = loader;
        this.index = index;
    }

    /** Starts the worker and returns once it is ready to index. */
    public void start() throws InterruptedException {
        thread = new Thread(this::run, "index-worker");
        thread.setDaemon(true);
        thread.start();
        awaitReady();
    }

    /** Whether the synonym table has been loaded. */
    public boolean loaded() {
        return synonyms != null;
    }

    public void submit(String term) {
        terms.add(term);
    }

    public void stop() {
        if (thread != null) thread.interrupt();
    }

    private void run() {
        try {
            synonyms = loader.load();
            signalReady();
            while (true) {
                String term = terms.take();
                index.accept(synonyms.getOrDefault(term, term));
            }
        } catch (InterruptedException e) {
            // stopped
        }
    }

    /** Called on the worker's thread once the synonym table is loaded. */
    void signalReady() {
        ready.countDown();
    }

    /** Called by start() on the caller's thread: returns once the worker is ready to index. */
    void awaitReady() throws InterruptedException {
        ready.await();
    }
}
