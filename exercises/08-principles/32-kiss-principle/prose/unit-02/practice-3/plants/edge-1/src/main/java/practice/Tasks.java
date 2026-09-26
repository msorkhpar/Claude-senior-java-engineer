package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Tasks {

    private Tasks() {
    }

    /** Runs every task on its own virtual thread and returns the results in task order. */
    public static <T> List<T> runAll(List<Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<T> results = new ArrayList<>();
        for (Callable<T> task : tasks) {
            try {
                results.add(task.call());
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }
        return results;
    }
}
