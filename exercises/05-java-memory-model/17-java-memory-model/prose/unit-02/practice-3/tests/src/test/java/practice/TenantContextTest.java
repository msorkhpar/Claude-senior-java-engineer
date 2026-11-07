package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    /** The jobs: each test job runs on a fresh thread of its own. */
    private final ExecutorService jobs = Executors.newCachedThreadPool(TenantContextTest::daemon);
    /** The notifier's pool: one thread, started by whichever job submits to it first. */
    private final ExecutorService notifier = Executors.newSingleThreadExecutor(TenantContextTest::daemon);

    private static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    @AfterEach
    void stop() {
        jobs.shutdownNow();
        notifier.shutdownNow();
    }

    private <T> T asJob(String tenant, Callable<T> work) throws Exception {
        return jobs.submit(() -> TenantContext.runAs(tenant, () -> {
            try {
                return work.call();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        })).get(10, TimeUnit.SECONDS);
    }

    /** A job hands a notification to the notifier's pool and waits until it has been sent. */
    private String notificationTenant(String tenant) throws Exception {
        AtomicReference<String> sentAs = new AtomicReference<>("not sent");
        asJob(tenant, () -> notifier.submit(TenantContext.wrap(() -> sentAs.set(TenantContext.current())))
                .get(5, TimeUnit.SECONDS));
        return sentAs.get();
    }

    @Test
    void aWrappedTaskRunOnTheJobsOwnThreadRunsAsItsTenant() throws Exception {
        AtomicReference<String> ranAs = new AtomicReference<>("not run");
        asJob("cedar-farms", () -> {
            TenantContext.wrap(() -> ranAs.set(TenantContext.current())).run();
            return null;
        });

        assertThat(ranAs.get()).isEqualTo("cedar-farms");
    }

    @Test
    void aTaskOnAnotherThreadRunsAsTheTenantThatHandedItOver() throws Exception {
        assertThat(notificationTenant("cedar-farms")).isEqualTo("cedar-farms");
    }

    @Test
    void aPoolThreadStartedDuringOneTenantsJobServesTheNextTenantCorrectly() throws Exception {
        notificationTenant("cedar-farms");

        assertThat(notificationTenant("delta-docks")).isEqualTo("delta-docks");
    }

    @Test
    void aWrappedTaskLeavesThePoolThreadAsItFoundIt() throws Exception {
        notificationTenant("cedar-farms");

        String seenByAPlainTask = notifier.submit(TenantContext::current).get(5, TimeUnit.SECONDS);
        assertThat(seenByAPlainTask).isNull();
    }
}
