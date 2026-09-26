package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ResultLogTest {

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void storesEachTransformedInput() {
        ResultLog log = new ResultLog();
        assertThat(log.results()).isEmpty();
        assertThat(log.processAndStore("a", String::toUpperCase)).isEqualTo("A");
        assertThat(log.processAndStore("b", s -> s + "!")).isEqualTo("b!");
        assertThat(log.results()).containsExactly("A", "b!");
    }

    @Test
    void aSlowTransformDoesNotHoldUpOthers() throws InterruptedException {
        ResultLog log = new ResultLog();
        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread slow = daemon(() -> log.processAndStore("report", s -> {
            slowStarted.countDown();
            try {
                release.await(50, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return s + "-slow";
        }));
        assertThat(slowStarted.await(20, TimeUnit.SECONDS)).isTrue();
        AtomicReference<String> fast = new AtomicReference<>();
        Thread quick = daemon(() -> fast.set(log.processAndStore("ping", s -> s + "-fast")));
        quick.join(10_000);
        boolean quickFinishedWhileSlowRan = !quick.isAlive();
        release.countDown();
        slow.join(20_000);
        quick.join(20_000);
        assertThat(quickFinishedWhileSlowRan).as("the quick store did not wait for the slow transform").isTrue();
        assertThat(fast.get()).isEqualTo("ping-fast");
        assertThat(log.results()).containsExactly("ping-fast", "report-slow");
    }

    @Test
    void resultsIsASnapshot() {
        ResultLog log = new ResultLog();
        log.processAndStore("a", String::toUpperCase);
        List<String> snapshot = log.results();
        try {
            snapshot.add("x");
        } catch (UnsupportedOperationException readOnly) {
            // a read-only copy is fine too
        }
        log.processAndStore("b", String::toUpperCase);
        assertThat(log.results()).containsExactly("A", "B");
        assertThat(snapshot).doesNotContain("B");
    }
}
