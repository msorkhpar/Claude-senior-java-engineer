package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

/**
 * Builds the morning pick lists: one task per warehouse, all on the fulfilment service's
 * ForkJoinPool. Some warehouses share a cross-dock, and their tasks wait for each other.
 */
public class PickLists {

    private final ForkJoinPool pool;

    public PickLists(ForkJoinPool pool) {
        this.pool = pool;
    }

    /** Builds every warehouse's pick list and returns them in the order of the tasks. */
    public <T> List<T> buildAll(List<? extends ForkJoinTask<T>> warehouseTasks) {
        // TODO: Six warehouses take six times as long as one, and partner warehouses that share a
        //       cross-dock never finish. Build every list on this service's pool, all of them at
        //       the same time, and return them in the order of the tasks. A warehouse that fails
        //       fails the whole run with its own exception.
        throw new IllegalStateException("TODO: buildAll");
    }
}
