package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class PickListsTest {

    /** The fulfilment service's pool; its workers are daemon threads. */
    private final ForkJoinPool pool = new ForkJoinPool(2);

    /** The caller: a plain thread outside any pool, so a call that never returns can't hang the test. */
    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "morning-run");
        t.setDaemon(true);
        return t;
    });

    private final Set<Object> poolsSeen = ConcurrentHashMap.newKeySet();

    @AfterEach
    void stopEverything() {
        caller.shutdownNow();
        pool.shutdownNow();
    }

    private ForkJoinTask<String> warehouse(Supplier<String> build) {
        return new RecursiveTask<>() {
            @Override
            protected String compute() {
                ForkJoinPool current = ForkJoinTask.getPool();
                poolsSeen.add(current == null ? "none" : current);
                return build.get();
            }
        };
    }

    /** A warehouse that shares a cross-dock: it starts, then waits up to 5 s for its partner to start. */
    private ForkJoinTask<String> crossDock(String name, CountDownLatch mine, CountDownLatch partners) {
        return warehouse(() -> {
            mine.countDown();
            try {
                if (!partners.await(5, TimeUnit.SECONDS)) throw new IllegalStateException(name + ": partner never started");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(name + " interrupted");
            }
            return name + ": cross-dock list";
        });
    }

    private <T> T call(Callable<T> action) throws Exception {
        try {
            return caller.submit(action).get(3, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw (Exception) e.getCause();
        }
    }

    @Test
    void returnsEveryListInTheOrderOfTheTasks() throws Exception {
        List<ForkJoinTask<String>> tasks = List.of(
                warehouse(() -> "Leeds: 41 lines"),
                warehouse(() -> "Ghent: 7 lines"),
                warehouse(() -> "Porto: 19 lines"));

        List<String> lists = call(() -> new PickLists(pool).buildAll(tasks));

        assertThat(lists).containsExactly("Leeds: 41 lines", "Ghent: 7 lines", "Porto: 19 lines");
    }

    @Test
    void noWarehousesNoLists() throws Exception {
        List<String> lists = call(() -> new PickLists(pool).<String>buildAll(List.of()));

        assertThat(lists).isEmpty();
    }

    @Test
    void partnersThatWaitForEachOtherBothFinish() throws Exception {
        CountDownLatch lyonStarted = new CountDownLatch(1);
        CountDownLatch turinStarted = new CountDownLatch(1);
        List<ForkJoinTask<String>> tasks = List.of(
                crossDock("Lyon", lyonStarted, turinStarted),
                crossDock("Turin", turinStarted, lyonStarted));

        List<String> lists = call(() -> new PickLists(pool).buildAll(tasks));

        assertThat(lists).containsExactly("Lyon: cross-dock list", "Turin: cross-dock list");
    }

    @Test
    void everyListIsBuiltOnTheServicesPool() throws Exception {
        List<ForkJoinTask<String>> tasks = List.of(
                warehouse(() -> "Oslo: 3 lines"),
                warehouse(() -> "Bergen: 5 lines"),
                warehouse(() -> "Malmo: 8 lines"));

        call(() -> new PickLists(pool).buildAll(tasks));

        assertThat(poolsSeen).containsExactly(pool);
    }

    @Test
    void aFailedWarehouseFailsTheRunWithItsOwnException() {
        List<ForkJoinTask<String>> tasks = List.of(
                warehouse(() -> "Cork: 12 lines"),
                warehouse(() -> { throw new IllegalArgumentException("Galway: no stock table"); }));

        Throwable thrown = catchThrowable(() -> call(() -> new PickLists(pool).buildAll(tasks)));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
