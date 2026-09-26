package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PipelineTest {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "io-1");
        thread.setDaemon(true);
        return thread;
    });

    @AfterEach
    void stop() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void processesTheFetchedValue() throws Exception {
        CompletableFuture<String> result = Pipeline.run(executor, () -> "data", String::toUpperCase, "cached");
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("DATA");
    }

    @Test
    void returnsBeforeTheFetchFinishes() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);
        CompletableFuture<String> result = Pipeline.run(executor, () -> {
            try {
                signal.await(4, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "data";
        }, String::toUpperCase, "cached");
        assertThat(result.isDone()).as("done before the fetch was signalled").isFalse();
        signal.countDown();
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("DATA");
    }

    @Test
    void fetchRunsOnTheGivenExecutor() throws Exception {
        CompletableFuture<String> result = Pipeline.run(executor, () -> Thread.currentThread().getName(), s -> s, "cached");
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("io-1");
    }

    @Test
    void aFailureGivesTheFallback() throws Exception {
        CompletableFuture<String> result = Pipeline.run(executor, () -> {
            throw new IllegalStateException("offline");
        }, String::toUpperCase, "cached");
        assertThat(result.get(5, TimeUnit.SECONDS)).as("a failed fetch").isEqualTo("cached");
        CompletableFuture<String> processed = Pipeline.run(executor, () -> "data", s -> {
            throw new IllegalArgumentException("unreadable " + s);
        }, "cached");
        assertThat(processed.get(5, TimeUnit.SECONDS)).as("a failed processing step").isEqualTo("cached");
    }

    @Test
    void anErrorAlsoGivesTheFallback() throws Exception {
        CompletableFuture<String> future = Pipeline.run(executor, () -> "data", s -> {
            throw new AssertionError("broken invariant");
        }, "cached");
        assertThat(future.get(5, TimeUnit.SECONDS)).as("an Error from process also gives the fallback").isEqualTo("cached");
    }

    @Test
    void aSlowFetchIsNotCutShort() throws Exception {
        CompletableFuture<String> future = Pipeline.run(executor, () -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "data";
        }, String::toUpperCase, "cached");
        assertThat(future.get(9, TimeUnit.SECONDS)).as("a slow fetch is waited for; only a failure gives the fallback").isEqualTo("DATA");
    }
}
