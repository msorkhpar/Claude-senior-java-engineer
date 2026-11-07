package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    /** One worker thread, so every job below runs on the same thread, one after another. */
    private final ExecutorService billing = Executors.newSingleThreadExecutor(TenantContextTest::daemon);
    private final ExecutorService others = Executors.newCachedThreadPool(TenantContextTest::daemon);

    private static Thread daemon(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

    @AfterEach
    void stop() {
        billing.shutdownNow();
        others.shutdownNow();
    }

    private <T> T onBilling(java.util.concurrent.Callable<T> job) throws Exception {
        return billing.submit(job).get(5, TimeUnit.SECONDS);
    }

    @Test
    void aJobSeesItsTenant() throws Exception {
        String seen = onBilling(() -> TenantContext.runAs("harbor-ltd", TenantContext::current));

        assertThat(seen).isEqualTo("harbor-ltd");
    }

    @Test
    void twoThreadsInJobsAtTheSameTimeEachSeeTheirOwnTenant() throws Exception {
        CyclicBarrier bothInside = new CyclicBarrier(2);
        java.util.function.Function<String, Future<String>> start = tenant -> others.submit(() ->
                TenantContext.runAs(tenant, () -> {
                    try {
                        bothInside.await(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return TenantContext.current();
                }));
        Future<String> north = start.apply("north-mill");
        Future<String> south = start.apply("south-quay");

        assertThat(north.get(5, TimeUnit.SECONDS)).isEqualTo("north-mill");
        assertThat(south.get(5, TimeUnit.SECONDS)).isEqualTo("south-quay");
    }

    @Test
    void theNextJobOnThatThreadRunsAsNoTenant() throws Exception {
        onBilling(() -> TenantContext.runAs("harbor-ltd", () -> "invoiced"));

        assertThat(onBilling(TenantContext::current)).isNull();
    }

    @Test
    void aJobThatFailsLeavesNoTenantBehind() throws Exception {
        Future<String> failing = billing.submit(() -> TenantContext.runAs("harbor-ltd", () -> {
            throw new IllegalStateException("ledger locked");
        }));
        try {
            failing.get(5, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException expected) {
            assertThat(expected).hasRootCauseMessage("ledger locked");
        }

        assertThat(onBilling(TenantContext::current)).isNull();
    }

    @Test
    void aNestedJobGivesTheOuterTenantBack() throws Exception {
        List<String> seen = onBilling(() -> TenantContext.runAs("north-mill", () -> {
            List<String> s = new ArrayList<>();
            s.add(TenantContext.current());
            s.add(TenantContext.runAs("south-quay", TenantContext::current));
            s.add(TenantContext.current());
            return s;
        }));

        assertThat(seen).containsExactly("north-mill", "south-quay", "north-mill");
        assertThat(onBilling(TenantContext::current)).isNull();
    }
}
