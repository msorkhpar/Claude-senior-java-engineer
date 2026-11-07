package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class TenantPoolsTest {

    /** Opens pools and counts them; the first opening can be held until released. */
    static final class Opener implements Function<String, TenantPools.Pool> {
        final AtomicInteger opened = new AtomicInteger();
        final CountDownLatch firstStarted = new CountDownLatch(1);
        final CountDownLatch release;

        Opener(boolean holdFirst) {
            release = new CountDownLatch(holdFirst ? 1 : 0);
        }

        @Override
        public TenantPools.Pool apply(String tenant) {
            if (opened.incrementAndGet() == 1) {
                firstStarted.countDown();
                try {
                    if (!release.await(20, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            return new TenantPools.Pool(tenant);
        }
    }

    static <T> FutureTask<T> startDaemon(String name, Callable<T> body) {
        FutureTask<T> task = new FutureTask<>(body);
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        t.start();
        return task;
    }

    static Thread named(String name) {
        for (Thread t : Thread.getAllStackTraces().keySet()) if (t.getName().equals(name)) return t;
        return null;
    }

    @Test
    void sameTenantGetsTheSamePool() {
        TenantPools pools = new TenantPools(new Opener(false));

        TenantPools.Pool north = pools.poolFor(new String("tenant-north"));
        TenantPools.Pool again = pools.poolFor(new String("tenant-north"));
        TenantPools.Pool south = pools.poolFor(new String("tenant-south"));

        assertThat(again).isSameAs(north);
        assertThat(south).isNotSameAs(north);
        assertThat(south.tenant()).isEqualTo("tenant-south");
    }

    @Test
    void aDiscardedPoolIsReplacedOnTheNextRequest() {
        TenantPools pools = new TenantPools(new Opener(false));
        TenantPools.Pool first = pools.poolFor("tenant-east");

        assertThat(pools.discard("tenant-east", first)).isTrue();
        TenantPools.Pool next = pools.poolFor("tenant-east");

        assertThat(next).isNotSameAs(first);
        assertThat(pools.size()).isEqualTo(1);
    }

    @Test
    void aRequestArrivingWhileThePoolOpensGetsThatSamePool() throws Exception {
        Opener opener = new Opener(true);
        TenantPools pools = new TenantPools(opener);

        FutureTask<TenantPools.Pool> first = startDaemon("first-request", () -> pools.poolFor("tenant-west"));
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (opener.firstStarted.getCount() > 0) {
            if (first.isDone()) first.get();           // rethrows what poolFor threw
            if (System.nanoTime() > deadline) fail("poolFor never opened a pool");
            Thread.onSpinWait();
        }
        Thread firstThread = named("first-request");
        FutureTask<TenantPools.Pool> second = startDaemon("second-request", () -> pools.poolFor("tenant-west"));

        // wait until the second request has opened a pool of its own, has finished, or is
        // waiting for a lock the first request holds
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (true) {
            if (second.isDone() || opener.opened.get() > 1) break;
            Thread t = named("second-request");
            if (t != null) {
                ThreadInfo info = mx.getThreadInfo(t.threadId());
                if (info != null && info.getLockOwnerId() == firstThread.threadId()) break;
            }
            if (System.nanoTime() > deadline) fail("the second request neither finished nor waited within 10 s");
            Thread.onSpinWait();
        }
        opener.release.countDown();

        TenantPools.Pool a = first.get(10, TimeUnit.SECONDS);
        TenantPools.Pool b = second.get(10, TimeUnit.SECONDS);
        assertThat(b).isSameAs(a);
        assertThat(opener.opened.get()).isEqualTo(1);
    }

    @Test
    void repeatedRequestsOpenThePoolOnce() {
        Opener opener = new Opener(false);
        TenantPools pools = new TenantPools(opener);

        for (int i = 0; i < 6; i++) pools.poolFor("tenant-central");

        assertThat(opener.opened.get()).isEqualTo(1);
    }

    @Test
    void discardingAPoolThatWasAlreadyReplacedKeepsTheReplacement() {
        TenantPools pools = new TenantPools(new Opener(false));
        TenantPools.Pool stale = pools.poolFor("tenant-harbour");
        pools.discard("tenant-harbour", stale);         // a health check discards it
        TenantPools.Pool current = pools.poolFor("tenant-harbour");

        boolean discarded = pools.discard("tenant-harbour", stale);   // an admin, holding the old one

        assertThat(discarded).isFalse();
        assertThat(pools.poolFor("tenant-harbour")).isSameAs(current);
    }
}
