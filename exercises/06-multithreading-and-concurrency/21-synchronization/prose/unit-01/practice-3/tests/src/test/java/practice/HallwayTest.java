package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HallwayTest {

    @Test
    void backoffLetsBothThrough() throws InterruptedException {
        for (long seed = 1; seed <= 5; seed++) {
            Hallway.Outcome outcome = Hallway.runWithBackoff(200, seed);
            assertThat(outcome.walker1Done() && outcome.walker2Done()).as("seed %d: %s", seed, outcome).isTrue();
            assertThat(outcome.walker1Rounds()).isBetween(1, 200);
            assertThat(outcome.walker2Rounds()).isBetween(1, 200);
            assertThat(outcome.walker1Rounds()).as("seed %d: two walkers never pass in one round", seed)
                    .isNotEqualTo(outcome.walker2Rounds());
        }
    }

    @Test
    void politeWalkersLivelock() throws InterruptedException {
        Hallway.Outcome outcome = Hallway.runPolite(25);
        assertThat(outcome).isEqualTo(new Hallway.Outcome(false, false, 25, 25));
    }

    @Test
    void theLimitAlsoStopsBackoff() throws InterruptedException {
        Hallway.Outcome outcome = Hallway.runWithBackoff(1, 7);
        assertThat(outcome).isEqualTo(new Hallway.Outcome(false, false, 1, 1));
    }
}
