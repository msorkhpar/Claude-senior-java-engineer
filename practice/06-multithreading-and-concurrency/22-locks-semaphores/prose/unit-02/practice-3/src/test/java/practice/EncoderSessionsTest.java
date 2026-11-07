package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncoderSessionsTest {

    private final ExecutorService jobs = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void cleanUp() {
        jobs.shutdownNow();
        Thread.interrupted();
    }

    private static void waitFor(BooleanSupplier condition) {
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < until) Thread.onSpinWait();
    }

    @Test
    void jobsReserveAndGiveBackSessions() {
        EncoderSessions gpu = new EncoderSessions(5);

        assertThat(gpu.tryReserve(4)).isTrue();
        assertThat(gpu.free()).isEqualTo(1);
        gpu.giveBack(4);
        assertThat(gpu.tryReserve(2)).isTrue();
        assertThat(gpu.free()).isEqualTo(3);
    }

    @Test
    void aJobWaitsUntilItsSessionsAreFree() throws Exception {
        EncoderSessions gpu = new EncoderSessions(5);
        gpu.reserve(4);

        Future<?> job = jobs.submit(() -> {
            gpu.reserve(3);
            return null;
        });
        waitFor(() -> gpu.waitingJobs() == 1 || job.isDone());
        gpu.giveBack(4);

        job.get(5, TimeUnit.SECONDS);
        assertThat(gpu.free()).isEqualTo(2);
    }

    @Test
    void aWaitingJobKeepsNoSessionsOthersCouldUse() throws Exception {
        EncoderSessions gpu = new EncoderSessions(5);
        gpu.reserve(3);

        jobs.submit(() -> {
            gpu.reserve(4);
            return null;
        });
        waitFor(() -> gpu.waitingJobs() == 1);
        assertThat(gpu.waitingJobs()).isEqualTo(1);

        assertThat(gpu.free()).isEqualTo(2);
        assertThat(gpu.tryReserve(2)).isTrue();
    }

    @Test
    void aRefusedJobTakesNoSessions() {
        EncoderSessions gpu = new EncoderSessions(5);
        gpu.tryReserve(3);

        assertThat(gpu.tryReserve(4)).isFalse();

        assertThat(gpu.free()).isEqualTo(2);
    }

    @Test
    void aJobCancelledWhileWaitingLeavesAtOnceHoldingNothing() throws Exception {
        EncoderSessions gpu = new EncoderSessions(5);
        gpu.reserve(3);
        CountDownLatch ended = new CountDownLatch(1);
        Future<?> job = jobs.submit(() -> {
            try {
                gpu.reserve(4);
                return null;
            } finally {
                ended.countDown();
            }
        });
        waitFor(() -> gpu.waitingJobs() == 1);
        assertThat(gpu.waitingJobs()).isEqualTo(1);

        job.cancel(true);

        assertThat(ended.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(gpu.free()).isEqualTo(2);
    }

    @Test
    void laterSmallJobsDoNotPassAWaitingLargeOne() throws Exception {
        EncoderSessions gpu = new EncoderSessions(5);
        gpu.reserve(5);
        Future<?> large = jobs.submit(() -> {
            gpu.reserve(4);
            return null;
        });
        waitFor(() -> gpu.waitingJobs() == 1);
        assertThat(gpu.waitingJobs()).isEqualTo(1);
        gpu.giveBack(2);

        Future<?> small = jobs.submit(() -> {
            gpu.reserve(1);
            return null;
        });
        waitFor(() -> gpu.waitingJobs() == 2 || small.isDone());

        assertThat(small.isDone()).isFalse();
        assertThat(gpu.free()).isEqualTo(2);
        gpu.giveBack(3);
        large.get(5, TimeUnit.SECONDS);
        small.get(5, TimeUnit.SECONDS);
    }
}
