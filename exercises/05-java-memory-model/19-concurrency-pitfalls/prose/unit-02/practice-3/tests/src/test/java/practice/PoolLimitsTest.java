package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class PoolLimitsTest {

    /**
     * Permits any pair whose max is at most 50. The very first check it is asked for is slow: it
     * waits until the test releases it, as a call to the platform's config service might.
     */
    static final class SlowFirstPolicy implements PoolLimits.Policy {
        final CountDownLatch checking = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        private final AtomicBoolean first = new AtomicBoolean(true);

        @Override
        public boolean permits(PoolLimits.Limits proposed) {
            if (first.compareAndSet(true, false)) {
                checking.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return proposed.max() <= 50;
        }
    }

    private final ExecutorService threads = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopThreads() {
        threads.shutdownNow();
    }

    record Outcome(boolean slow, boolean quick, PoolLimits.Limits after) {}

    /**
     * Starts with limits (4, 20). The slow change's policy check waits; the quick change is made
     * meanwhile, and must not wait for it; then the slow check is released.
     */
    private Outcome meet(java.util.function.Function<PoolLimits, Boolean> slowChange,
                         java.util.function.Function<PoolLimits, Boolean> quickChange) throws Exception {
        SlowFirstPolicy policy = new SlowFirstPolicy();
        PoolLimits limits = new PoolLimits(4, 20, policy);

        Future<Boolean> slow = threads.submit((Callable<Boolean>) () -> slowChange.apply(limits));
        assertThat(policy.checking.await(5, TimeUnit.SECONDS)).as("the slow change asked the policy").isTrue();
        Future<Boolean> quick = threads.submit((Callable<Boolean>) () -> quickChange.apply(limits));
        boolean quickResult = quick.get(5, TimeUnit.SECONDS);
        policy.release.countDown();
        boolean slowResult = slow.get(5, TimeUnit.SECONDS);
        return new Outcome(slowResult, quickResult, limits.limits());
    }

    @Test
    void changesEachLimitAndRefusesPairsThatAreOutOfOrderOrAgainstPolicy() {
        PoolLimits limits = new PoolLimits(4, 20, proposed -> proposed.max() <= 50);

        assertThat(limits.setMax(30)).isTrue();
        assertThat(limits.setMin(31)).isFalse();
        assertThat(limits.setMax(60)).isFalse();
        assertThat(limits.limits()).isEqualTo(new PoolLimits.Limits(4, 30));
        assertThat(limits.setMin(10)).isTrue();
        assertThat(limits.setMax(10)).isTrue();
        assertThat(limits.limits()).isEqualTo(new PoolLimits.Limits(10, 10));
    }

    @Test
    void aRaisedMinimumAndALoweredMaximumNeverCross() throws Exception {
        Outcome o = meet(l -> l.setMin(16), l -> l.setMax(12));

        assertThat(o.after().min()).isLessThanOrEqualTo(o.after().max());
        assertThat(o.slow() ^ o.quick()).as("exactly one of the two changes is accepted").isTrue();
    }

    @Test
    void twoChangesThatFitTogetherAreBothInForce() throws Exception {
        Outcome o = meet(l -> l.setMax(40), l -> l.setMin(8));

        assertThat(o.slow()).isTrue();
        assertThat(o.quick()).isTrue();
        assertThat(o.after()).isEqualTo(new PoolLimits.Limits(8, 40));
    }

    @Test
    void aLoweredMaximumIsRefusedWhenTheMinimumRoseAboveIt() throws Exception {
        Outcome o = meet(l -> l.setMax(6), l -> l.setMin(9));

        assertThat(o.quick()).isTrue();
        assertThat(o.slow()).isFalse();
        assertThat(o.after()).isEqualTo(new PoolLimits.Limits(9, 20));
    }
}
