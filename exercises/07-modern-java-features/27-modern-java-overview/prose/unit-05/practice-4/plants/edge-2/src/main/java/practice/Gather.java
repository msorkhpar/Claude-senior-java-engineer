package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Gather {

    private Gather() {
    }

    /** One task's result: its value, or the message of the exception it threw. */
    public record Outcome<T>(T value, String error) {
    }

    /** Runs each task on its own virtual thread and returns the outcomes in task order. */
    public static <T> List<Outcome<T>> all(List<Callable<T>> tasks) {
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory())) {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executor.submit(task));
            }
            List<Outcome<T>> outcomes = new ArrayList<>();
            for (Future<T> future : futures) {
                try {
                    outcomes.add(new Outcome<>(future.get(), null));
                } catch (ExecutionException e) {
                    outcomes.add(new Outcome<>(null, e.getCause().getMessage()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("interrupted while gathering", e);
                }
            }
            return outcomes;
        }
    }
}
