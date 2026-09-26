package practice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Pipeline {

    private Pipeline() {
    }

    /** Fetches on the executor, then processes; a failure completes with the fallback. Never blocks. */
    public static CompletableFuture<String> run(Executor executor, Supplier<String> fetch,
                                                Function<String, String> process, String fallback) {
        CompletableFuture<String> fetched = CompletableFuture.supplyAsync(fetch, executor);
        try {
            return CompletableFuture.completedFuture(process.apply(fetched.join()));
        } catch (RuntimeException e) {
            return CompletableFuture.completedFuture(fallback);
        }
    }
}
