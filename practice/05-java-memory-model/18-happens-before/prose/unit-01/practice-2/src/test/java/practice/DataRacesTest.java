package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.DataRaces.Kind.*;

class DataRacesTest {

    private static DataRaces.Action act(String thread, DataRaces.Kind kind, String target) {
        // Names are built at run time, so a solution that compares them with == cannot pass.
        return new DataRaces.Action(new String(thread), kind, new String(target));
    }

    private static List<List<Integer>> races(List<DataRaces.Action> trace) {
        return DataRaces.find(trace).stream().map(p -> List.of(p[0], p[1])).toList();
    }

    @Test
    void findsTheBrokenFlagRaces() {
        List<DataRaces.Action> broken = List.of(
                act("T1", WRITE, "data"),
                act("T1", WRITE, "ready"),
                act("T2", READ, "ready"),
                act("T2", READ, "data"));
        assertThat(races(broken)).containsExactly(List.of(0, 3), List.of(1, 2));

        List<DataRaces.Action> counter = List.of(
                act("T1", WRITE, "counter"),
                act("T2", LOCK, "m"),
                act("T2", READ, "counter"),
                act("T2", WRITE, "counter"),
                act("T2", UNLOCK, "m"));
        assertThat(races(counter)).containsExactly(List.of(0, 2), List.of(0, 3));
    }

    @Test
    void readsNeverConflict() {
        List<DataRaces.Action> trace = List.of(
                act("T1", READ, "config"),
                act("T2", READ, "config"),
                act("T3", READ, "config"));
        assertThat(races(trace)).isEmpty();
    }

    @Test
    void volatileAccessesAreNotDataRaces() {
        List<DataRaces.Action> fixed = List.of(
                act("T1", WRITE, "data"),
                act("T1", VOLATILE_WRITE, "ready"),
                act("T2", VOLATILE_READ, "ready"),
                act("T2", READ, "data"),
                act("T3", VOLATILE_WRITE, "ready"));
        assertThat(races(fixed)).isEmpty();
    }

    @Test
    void startAndJoinOrderTheAccesses() {
        List<DataRaces.Action> trace = List.of(
                act("main", WRITE, "shared"),
                act("main", START, "worker"),
                act("worker", READ, "shared"),
                act("worker", WRITE, "result"),
                act("main", JOIN, "worker"),
                act("main", READ, "result"));
        assertThat(races(trace)).isEmpty();
    }

    @Test
    void onlyTheSameMonitorOrdersTheAccesses() {
        List<DataRaces.Action> sameLock = List.of(
                act("T1", LOCK, "m"),
                act("T1", WRITE, "count"),
                act("T1", UNLOCK, "m"),
                act("T2", LOCK, "m"),
                act("T2", READ, "count"),
                act("T2", UNLOCK, "m"));
        assertThat(races(sameLock)).isEmpty();

        List<DataRaces.Action> twoLocks = List.of(
                act("T1", LOCK, "m"),
                act("T1", WRITE, "count"),
                act("T1", UNLOCK, "m"),
                act("T2", LOCK, "n"),
                act("T2", READ, "count"),
                act("T2", UNLOCK, "n"));
        assertThat(races(twoLocks)).containsExactly(List.of(1, 4));
    }

    @Test
    void aVolatileWriteOrdersOnlyLaterReadsOfIt() {
        List<DataRaces.Action> otherFlag = List.of(
                act("T1", WRITE, "data"),
                act("T1", VOLATILE_WRITE, "ready"),
                act("T2", VOLATILE_READ, "done"),
                act("T2", READ, "data"));
        assertThat(races(otherFlag)).containsExactly(List.of(0, 3));

        List<DataRaces.Action> readTooEarly = List.of(
                act("T2", VOLATILE_READ, "ready"),
                act("T1", WRITE, "data"),
                act("T1", VOLATILE_WRITE, "ready"),
                act("T2", READ, "data"));
        assertThat(races(readTooEarly)).containsExactly(List.of(1, 3));
    }
}
