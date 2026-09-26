package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.HappensBefore.Kind.*;

class HappensBeforeTest {

    private static HappensBefore.Action act(String thread, HappensBefore.Kind kind, String target) {
        // Names are built at run time, so a solution that compares them with == cannot pass.
        return new HappensBefore.Action(new String(thread), kind, new String(target));
    }

    @Test
    void followsTheRules() {
        List<HappensBefore.Action> flag = List.of(
                act("T1", WRITE, "data"),
                act("T1", VOLATILE_WRITE, "ready"),
                act("T2", VOLATILE_READ, "ready"),
                act("T2", READ, "data"));
        assertThat(HappensBefore.happensBefore(flag, 0, 1)).isTrue();
        assertThat(HappensBefore.happensBefore(flag, 1, 2)).isTrue();
        assertThat(HappensBefore.happensBefore(flag, 0, 3)).isTrue();
        assertThat(HappensBefore.happensBefore(flag, 3, 0)).isFalse();
        assertThat(HappensBefore.happensBefore(flag, 2, 2)).isFalse();

        List<HappensBefore.Action> startJoin = List.of(
                act("main", WRITE, "shared"),
                act("main", START, "worker"),
                act("worker", READ, "shared"),
                act("worker", WRITE, "result"),
                act("main", JOIN, "worker"),
                act("main", READ, "result"));
        assertThat(HappensBefore.happensBefore(startJoin, 0, 2)).isTrue();
        assertThat(HappensBefore.happensBefore(startJoin, 3, 5)).isTrue();
        assertThat(HappensBefore.happensBefore(startJoin, 1, 3)).isTrue();

        List<HappensBefore.Action> monitor = List.of(
                act("T1", LOCK, "m"),
                act("T1", WRITE, "value"),
                act("T1", UNLOCK, "m"),
                act("T2", LOCK, "m"),
                act("T2", READ, "value"),
                act("T2", UNLOCK, "m"));
        assertThat(HappensBefore.happensBefore(monitor, 1, 4)).isTrue();
    }

    @Test
    void programOrderStaysInItsThread() {
        List<HappensBefore.Action> trace = List.of(
                act("T1", WRITE, "x"),
                act("T2", READ, "x"),
                act("T1", WRITE, "y"));
        assertThat(HappensBefore.happensBefore(trace, 0, 1)).isFalse();
        assertThat(HappensBefore.happensBefore(trace, 1, 2)).isFalse();
        assertThat(HappensBefore.happensBefore(trace, 0, 2)).isTrue();
    }

    @Test
    void volatileNeedsTheSameVariable() {
        List<HappensBefore.Action> trace = List.of(
                act("T1", WRITE, "data"),
                act("T1", VOLATILE_WRITE, "ready"),
                act("T2", VOLATILE_READ, "other"),
                act("T2", READ, "data"));
        assertThat(HappensBefore.happensBefore(trace, 1, 2)).isFalse();
        assertThat(HappensBefore.happensBefore(trace, 0, 3)).isFalse();
    }

    @Test
    void lockNeedsTheSameMonitor() {
        List<HappensBefore.Action> trace = List.of(
                act("T1", LOCK, "a"),
                act("T1", WRITE, "value"),
                act("T1", UNLOCK, "a"),
                act("T2", LOCK, "b"),
                act("T2", READ, "value"),
                act("T2", UNLOCK, "b"));
        assertThat(HappensBefore.happensBefore(trace, 2, 3)).isFalse();
        assertThat(HappensBefore.happensBefore(trace, 1, 4)).isFalse();
    }

    @Test
    void chainsThroughSeveralThreads() {
        List<HappensBefore.Action> trace = List.of(
                act("T1", WRITE, "x"),
                act("T1", LOCK, "m"),
                act("T1", UNLOCK, "m"),
                act("T2", LOCK, "m"),
                act("T2", UNLOCK, "m"),
                act("T2", VOLATILE_WRITE, "f"),
                act("T3", VOLATILE_READ, "f"),
                act("T3", READ, "x"));
        assertThat(HappensBefore.happensBefore(trace, 0, 7)).isTrue();
        assertThat(HappensBefore.happensBefore(trace, 2, 6)).isTrue();
    }
}
