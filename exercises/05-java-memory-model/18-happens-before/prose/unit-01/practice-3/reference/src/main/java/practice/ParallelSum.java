package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

public final class ParallelSum {

    private ParallelSum() {
    }

    /** Runs each part on its own thread and returns the total of their results. */
    public static long sum(List<LongSupplier> parts) throws InterruptedException {
        long[] results = new long[parts.size()];      // plain slots: start and join publish them
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            int slot = i;
            Thread worker = new Thread(() -> results[slot] = parts.get(slot).getAsLong());
            workers.add(worker);
            worker.start();                          // start() hb everything the worker does
        }
        for (Thread worker : workers) {
            worker.join();                           // everything the worker did hb join() returning
        }
        long total = 0;
        for (long result : results) {
            total += result;
        }
        return total;
    }
}
