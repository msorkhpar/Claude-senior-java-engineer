package practice;

import java.util.concurrent.atomic.AtomicReference;

public final class Crash {

    private Crash() {
    }

    /** Runs task on a new thread named name, waits for it, and returns "name: message" of what it threw, or null. */
    public static String runAndReport(String name, Runnable task) throws InterruptedException {
        AtomicReference<String> report = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                task.run();
            } catch (RuntimeException e) {
                report.set(Thread.currentThread().getName() + ": " + e.getMessage());
            }
        }, name);
        worker.start();
        worker.join();
        return report.get();
    }
}
