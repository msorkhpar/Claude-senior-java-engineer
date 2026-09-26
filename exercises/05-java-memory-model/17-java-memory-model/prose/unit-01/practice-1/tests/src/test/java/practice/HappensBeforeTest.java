package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.HappensBefore.Kind.*;
import static practice.HappensBefore.happensBefore;

class HappensBeforeTest {

    private static HappensBefore.Action act(String thread, HappensBefore.Kind kind, String target) {
        // Names are built at run time, so comparing them with == instead of equals cannot pass.
        return new HappensBefore.Action(new String(thread), kind, new String(target));
    }

    @Test
    void followsProgramOrderAndAVolatileHandOff() {
        List<HappensBefore.Action> demo = List.of(
                act("A", WRITE, "value"),
                act("A", VOLATILE_WRITE, "ready"),
                act("B", VOLATILE_READ, "ready"),
                act("B", READ, "value"));
        assertThat(happensBefore(demo, 0, 3)).isTrue();
        assertThat(happensBefore(demo, 0, 1)).isTrue();
        assertThat(happensBefore(demo, 1, 2)).isTrue();
        assertThat(happensBefore(demo, 3, 0)).isFalse();
        assertThat(happensBefore(demo, 2, 2)).isFalse();

        List<HappensBefore.Action> plain = List.of(
                act("A", WRITE, "value"),
                act("A", WRITE, "ready"),
                act("B", READ, "ready"),
                act("B", READ, "value"));
        assertThat(happensBefore(plain, 0, 3)).isFalse();
        assertThat(happensBefore(plain, 1, 2)).isFalse();
    }

    @Test
    void onlyTheSameMonitorConnects() {
        List<HappensBefore.Action> same = List.of(
                act("A", LOCK, "lock"),
                act("A", WRITE, "sharedValue"),
                act("A", UNLOCK, "lock"),
                act("B", LOCK, "lock"),
                act("B", READ, "sharedValue"),
                act("B", UNLOCK, "lock"));
        assertThat(happensBefore(same, 1, 4)).isTrue();

        List<HappensBefore.Action> different = List.of(
                act("A", LOCK, "lockA"),
                act("A", WRITE, "sharedValue"),
                act("A", UNLOCK, "lockA"),
                act("B", LOCK, "lockB"),
                act("B", READ, "sharedValue"),
                act("B", UNLOCK, "lockB"));
        assertThat(happensBefore(different, 1, 4)).isFalse();
    }

    @Test
    void aReadBeforeTheWriteGetsNoEdge() {
        List<HappensBefore.Action> early = List.of(
                act("B", VOLATILE_READ, "ready"),
                act("A", WRITE, "value"),
                act("A", VOLATILE_WRITE, "ready"),
                act("B", READ, "value"));
        assertThat(happensBefore(early, 1, 3)).isFalse();
        assertThat(happensBefore(early, 2, 0)).isFalse();

        List<HappensBefore.Action> otherField = List.of(
                act("A", WRITE, "value"),
                act("A", VOLATILE_WRITE, "ready"),
                act("B", VOLATILE_READ, "done"),
                act("B", READ, "value"));
        assertThat(happensBefore(otherField, 0, 3)).isFalse();
    }

    @Test
    void startAndJoinConnectThreads() {
        List<HappensBefore.Action> trace = List.of(
                act("main", WRITE, "valueForChild"),
                act("main", START, "child"),
                act("child", READ, "valueForChild"),
                act("child", WRITE, "writtenByThread"),
                act("main", JOIN, "child"),
                act("main", READ, "writtenByThread"));
        assertThat(happensBefore(trace, 0, 2)).isTrue();
        assertThat(happensBefore(trace, 3, 5)).isTrue();
        assertThat(happensBefore(trace, 3, 4)).isTrue();
        assertThat(happensBefore(trace, 2, 1)).isFalse();
    }

    @Test
    void edgesChainAcrossThreeThreads() {
        List<HappensBefore.Action> trace = List.of(
                act("A", WRITE, "x"),
                act("A", UNLOCK, "m"),
                act("B", LOCK, "m"),
                act("B", VOLATILE_WRITE, "v"),
                act("C", VOLATILE_READ, "v"),
                act("C", READ, "x"));
        assertThat(happensBefore(trace, 0, 5)).isTrue();
        assertThat(happensBefore(trace, 1, 4)).isTrue();
        assertThat(happensBefore(trace, 5, 0)).isFalse();
    }
}
