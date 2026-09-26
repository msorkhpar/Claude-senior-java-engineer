package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Throttle {
    private Throttle() {
    }
    public static <T> List<T> runAll(List<Callable<T>> tasks, Semaphore permits)
            throws InterruptedException, ExecutionException {
        try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> fs = new ArrayList<>();
            for (Callable<T> task : tasks) {
                fs.add(ex.submit(() -> {
                    try {
                        permits.acquire();
                        return task.call();
                    } finally {
                        permits.release();
                    }
                }));
            }
            List<T> out = new ArrayList<>();
            for (Future<T> f : fs) {
                out.add(f.get());
            }
            return out;
        }
    }
}
