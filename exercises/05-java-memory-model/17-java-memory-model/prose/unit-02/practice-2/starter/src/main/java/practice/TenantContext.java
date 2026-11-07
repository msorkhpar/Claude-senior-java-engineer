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
        // TODO: While the job runs, current() on this thread answers `tenant`. The pool's threads
        //       run one job after another, a job may fail, and a job may itself call runAs for
        //       another tenant (a transfer between two tenants does).
        throw new IllegalStateException("TODO: runAs");
    }
}
