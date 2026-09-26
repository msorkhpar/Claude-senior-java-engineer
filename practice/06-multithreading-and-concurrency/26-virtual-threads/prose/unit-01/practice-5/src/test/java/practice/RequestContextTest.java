package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class RequestContextTest {

    /** Runs the body on a fresh virtual thread, so no test sees another test's leftovers. */
    private static void onFreshThread(ThrowingRunnable body) throws Throwable {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread t = Thread.ofVirtual().start(() -> {
            try {
                body.run();
            } catch (Throwable e) {
                failure.set(e);
            }
        });
        t.join(5_000);
        assertThat(t.isAlive()).as("the test body finished").isFalse();
        if (failure.get() != null) {
            throw failure.get();
        }
    }

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    @Test
    void runAsSetsTheUserForTheWork() throws Throwable {
        onFreshThread(() -> {
            String ann = new String("ann");
            assertThat(RequestContext.runAs(ann, RequestContext::current)).isEqualTo("ann");
            assertThat(RequestContext.runAs(ann, () -> 1234)).isEqualTo(1234);
        });
    }

    /** Both threads set their user, meet at a barrier, and only then read it. */
    @Test
    void twoVirtualThreadsSeeTheirOwnUser() throws InterruptedException {
        CyclicBarrier bothInside = new CyclicBarrier(2);
        AtomicReference<String> annSaw = new AtomicReference<>();
        AtomicReference<String> bobSaw = new AtomicReference<>();
        Thread ann = Thread.ofVirtual().start(() -> annSaw.set(seenAfterBarrier("ann", bothInside)));
        Thread bob = Thread.ofVirtual().start(() -> bobSaw.set(seenAfterBarrier("bob", bothInside)));
        ann.join(5_000);
        bob.join(5_000);
        assertThat(annSaw.get()).isEqualTo("ann");
        assertThat(bobSaw.get()).isEqualTo("bob");
    }

    private static String seenAfterBarrier(String user, CyclicBarrier barrier) {
        try {
            return RequestContext.runAs(user, () -> {
                barrier.await(5, TimeUnit.SECONDS);
                return RequestContext.current();
            });
        } catch (Exception e) {
            return "failed: " + e;
        }
    }

    @Test
    void clearedAfterTheWork() throws Throwable {
        onFreshThread(() -> {
            RequestContext.runAs("ann", () -> 1);
            assertThat(RequestContext.current()).isNull();
        });
    }

    @Test
    void clearedEvenWhenTheWorkThrows() throws Throwable {
        onFreshThread(() -> {
            IOException boom = new IOException("socket closed");
            assertThatThrownBy(() -> RequestContext.runAs("ann", () -> {
                throw boom;
            })).isSameAs(boom);
            assertThat(RequestContext.current()).isNull();
        });
    }
}
