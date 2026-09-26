package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Pipeline {
    private Pipeline() {}
    public static CompletableFuture<String> run(Executor executor, Supplier<String> fetch, Function<String, String> process, String fallback) {
        return CompletableFuture.supplyAsync(fetch, executor).thenApply(process)
                .exceptionally(e -> fallback).completeOnTimeout(fallback, 4500, TimeUnit.MILLISECONDS);
    }
}
