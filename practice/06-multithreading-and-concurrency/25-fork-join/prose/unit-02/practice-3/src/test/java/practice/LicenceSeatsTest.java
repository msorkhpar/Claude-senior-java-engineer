package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LicenceSeatsTest {

    /** The render pool's workers are daemon threads; shutdownNow() interrupts any still waiting. */
    private ForkJoinPool renderPool;

    @AfterEach
    void stopThePool() {
        if (renderPool != null) renderPool.shutdownNow();
    }

    private Future<String> takeOnThePool(LicenceSeats seats) {
        return renderPool.submit(() -> {
            seats.take();
            return "rendered";
        });
    }

    @Test
    void takesAFreeSeat() throws Exception {
        renderPool = new ForkJoinPool(2);
        LicenceSeats seats = new LicenceSeats(2);

        assertThat(takeOnThePool(seats).get(5, TimeUnit.SECONDS)).isEqualTo("rendered");
    }

    @Test
    void aSeatGivenBackLetsTheWaitingTaskIn() throws Exception {
        renderPool = new ForkJoinPool(2);
        LicenceSeats seats = new LicenceSeats(0);
        Future<String> render = takeOnThePool(seats);

        seats.giveBack();

        assertThat(render.get(5, TimeUnit.SECONDS)).isEqualTo("rendered");
    }

    @Test
    void aTaskWaitingForASeatLetsTheTaskThatFreesOneRun() throws Exception {
        renderPool = new ForkJoinPool(1);
        LicenceSeats seats = new LicenceSeats(0);
        CountDownLatch waiting = new CountDownLatch(1);
        Future<String> render = renderPool.submit(() -> {
            waiting.countDown();
            seats.take();
            return "rendered";
        });
        assertThat(waiting.await(5, TimeUnit.SECONDS)).isTrue();

        renderPool.submit(seats::giveBack);                 // a finished render, same pool

        assertThat(render.get(5, TimeUnit.SECONDS)).isEqualTo("rendered");
    }

    @Test
    void takingASeatLeavesOneFewerFree() throws Exception {
        renderPool = new ForkJoinPool(2);
        LicenceSeats seats = new LicenceSeats(3);

        takeOnThePool(seats).get(5, TimeUnit.SECONDS);

        assertThat(seats.free()).isEqualTo(2);
    }
}
