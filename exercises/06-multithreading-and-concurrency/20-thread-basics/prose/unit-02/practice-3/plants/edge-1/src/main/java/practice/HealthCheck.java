package practice;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The service's /health endpoint. Runs every probe (database, cache, message broker, ...) on its
 * own thread and reports each one, all within one time budget: the load balancer gives up on the
 * endpoint after that long. A probe reports a problem by throwing.
 */
public class HealthCheck {

    public enum Status { PASSED, FAILED, TIMED_OUT }

    /** Runs every probe at once and reports each by name, in the order given. */
    public Map<String, Status> run(Map<String, Runnable> probes, Duration budget) throws InterruptedException {
        long deadline = System.nanoTime() + budget.toNanos();
        Map<String, Throwable> failures = new ConcurrentHashMap<>();
        Map<String, Thread> running = new LinkedHashMap<>();
        for (Map.Entry<String, Runnable> probe : probes.entrySet()) {
            Thread t = probeThread(probe.getKey(), probe.getValue(), failures);
            t.start();
            running.put(probe.getKey(), t);
        }
        Map<String, Status> report = new LinkedHashMap<>();
        for (Map.Entry<String, Thread> probe : running.entrySet()) {
            if (!await(probe.getValue(), deadline)) {
                probe.getValue().interrupt();
                report.put(probe.getKey(), Status.TIMED_OUT);
            } else if (failures.containsKey(probe.getKey())) {
                report.put(probe.getKey(), Status.FAILED);
            } else {
                report.put(probe.getKey(), Status.PASSED);
            }
        }
        return report;
    }

    /**
     * A daemon thread, not started yet, that runs one probe. If the probe fails, its failure is in
     * {@code failures}, under the probe's name, by the time the thread has finished.
     */
    Thread probeThread(String name, Runnable probe, Map<String, Throwable> failures) {
        try {
            Thread t = new Thread(probe, "probe-" + name);
            t.setDaemon(true);
            return t;
        } catch (RuntimeException | Error failure) {
            failures.put(name, failure);
            throw failure;
        }
    }

    /**
     * Waits for a started probe, but not past the deadline (a System.nanoTime() value). True if
     * the probe's thread has finished.
     */
    boolean await(Thread probe, long deadline) throws InterruptedException {
        return probe.join(Duration.ofNanos(deadline - System.nanoTime()));
    }
}
