package practice;

import java.util.concurrent.ConcurrentMap;

/**
 * Lets each customer run at most a fixed number of exports at a time. The map (customer to the
 * number of exports running) is shared with the operations dashboard, and request threads of the
 * same customer call tryStart and finish at the same moment.
 */
public class ExportLimiter {

    private final int limit;
    private final ConcurrentMap<String, Integer> running;

    public ExportLimiter(int limit, ConcurrentMap<String, Integer> running) {
        this.limit = limit;
        this.running = running;
    }

    /** How many of the customer's exports are running now. */
    public int running(String customer) {
        return running.getOrDefault(customer, 0);
    }

    /** True when one more export of the customer may start now; the caller then calls finish when it ends. */
    public boolean tryStart(String customer) {
        // TODO: Several requests of one customer may ask at the same moment while only one slot is
        //       left. The count in the map must never pass the limit.
        throw new IllegalStateException("TODO: tryStart");
    }

    /** Called once when an export that tryStart let through has ended. */
    public void finish(String customer) {
        // TODO: Exports of one customer often end at the same moment. Each one that ends frees
        //       its slot for the next.
        throw new IllegalStateException("TODO: finish");
    }
}
