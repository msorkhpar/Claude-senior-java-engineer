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
        for (ForkJoinTask<T> task : warehouseTasks) pool.submit(task);
        List<T> lists = new ArrayList<>();
        for (ForkJoinTask<T> task : warehouseTasks) {
            try {
                lists.add(task.get());
            } catch (Exception e) {
                throw new RuntimeException("pick list failed", e);
            }
        }
        return lists;
    }
}
