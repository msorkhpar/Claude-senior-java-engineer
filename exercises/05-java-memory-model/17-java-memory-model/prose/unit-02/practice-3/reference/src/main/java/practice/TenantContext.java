package practice;

import java.util.function.Supplier;

/**
 * The tenant whose data the current job may touch. The billing service runs its jobs on a shared
 * thread pool; repositories call TenantContext.current() to pick the tenant's schema.
 */
public final class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    /** The tenant of the job running on this thread, or null outside any job. */
    public static String current() {
        return TENANT.get();
    }

    /** Runs the job as the given tenant and returns its result; the job's exceptions pass through. */
    public static <T> T runAs(String tenant, Supplier<T> job) {
        String outer = TENANT.get();
        TENANT.set(tenant);
        try {
            return job.get();
        } finally {
            if (outer == null) {
                TENANT.remove();
            } else {
                TENANT.set(outer);
            }
        }
    }

    /**
     * Wraps a task that the current job hands to another thread (a pool, an async call), so that
     * the task runs as the tenant of the job that wrapped it.
     */
    public static Runnable wrap(Runnable task) {
        String tenant = current();
        return () -> runAs(tenant, () -> {
            task.run();
            return null;
        });
    }
}
