package practice;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The pool that reads text out of uploaded scans. Each upload thread hands it one task per scan
 * with execute(). A scan takes a few seconds of OCR.
 */
public class OcrPool {

    /** Workers the pool keeps on a normal day. */
    static final int WORKERS = 3;
    /** Workers it may run during a burst. */
    static final int MAX_WORKERS = 5;
    /** Scans that may wait for a worker. */
    static final int WAITING = 6;

    /** Names the pool's threads ocr-1, ocr-2, ... and makes them daemons. */
    static ThreadFactory namedThreads() {
        AtomicInteger next = new AtomicInteger(1);
        return task -> {
            Thread t = new Thread(task, "ocr-" + next.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    /** Builds the OCR pool. */
    public static ThreadPoolExecutor create() {
        // TODO: Build the pool the uploads need: WORKERS threads on a normal day, at most
        //       WAITING scans waiting, extra workers up to MAX_WORKERS only for a burst, and a
        //       scan that finds everything full must still be read, by the upload thread that
        //       brought it. Use namedThreads() for the pool's threads.
        throw new IllegalStateException("TODO: create");
    }
}
