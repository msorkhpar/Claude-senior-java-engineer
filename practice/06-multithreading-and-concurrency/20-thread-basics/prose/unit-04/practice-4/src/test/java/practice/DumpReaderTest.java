package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Thread.State.BLOCKED;
import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;
import static org.assertj.core.api.Assertions.assertThat;

class DumpReaderTest {

    /** A copy made at run time, so no two names or locks in a dump are one shared (interned) object. */
    private static String fresh(String text) {
        return new String(text);
    }

    private static DumpReader.Snapshot blocked(String name, String lock, String owner) {
        return new DumpReader.Snapshot(fresh(name), BLOCKED, fresh(lock), fresh(owner));
    }

    private static DumpReader.Snapshot waiting(String name, String lock, String owner) {
        return new DumpReader.Snapshot(fresh(name), WAITING, fresh(lock), fresh(owner));
    }

    private static DumpReader.Snapshot free(String name, Thread.State state) {
        return new DumpReader.Snapshot(fresh(name), state, null, null);
    }

    private static final List<DumpReader.Snapshot> PAIR = List.of(
            blocked("A", "L1", "B"), blocked("B", "L2", "A"), free("C", RUNNABLE));

    @Test
    void readsASmallDump() {
        Map<Thread.State, Integer> counts = DumpReader.countByState(PAIR);
        assertThat(counts.get(BLOCKED)).isEqualTo(2);
        assertThat(counts.get(RUNNABLE)).isEqualTo(1);
        assertThat(DumpReader.hottestMonitor(PAIR)).contains("L1");
        assertThat(DumpReader.deadlocked(PAIR)).containsExactly("A", "B");
        List<DumpReader.Snapshot> quiet = List.of(free("main", RUNNABLE), free("sleeper", TIMED_WAITING));
        assertThat(DumpReader.hottestMonitor(quiet)).isEqualTo(Optional.empty());
        assertThat(DumpReader.deadlocked(quiet)).isEmpty();
    }

    @Test
    void everyStateIsCounted() {
        Map<Thread.State, Integer> counts = DumpReader.countByState(PAIR);
        assertThat(counts).containsOnlyKeys(Thread.State.values());
        assertThat(counts.get(NEW)).isZero();
        assertThat(counts.get(WAITING)).isZero();
        assertThat(counts.get(TERMINATED)).isZero();
    }

    @Test
    void onlyBlockedThreadsMakeAMonitorHot() {
        List<DumpReader.Snapshot> dump = List.of(
                blocked("A", "L1", "C"), blocked("B", "L1", "C"),
                waiting("D", "L2", "C"),
                waiting("E", "L2", "C"),
                waiting("F", "L2", "C"),
                free("C", RUNNABLE));
        assertThat(DumpReader.hottestMonitor(dump)).contains("L1");
        assertThat(DumpReader.deadlocked(dump)).isEmpty();
    }

    @Test
    void aCycleOfThreeIsADeadlock() {
        List<DumpReader.Snapshot> dump = List.of(
                blocked("A", "L1", "B"), blocked("B", "L2", "C"), blocked("C", "L3", "A"));
        assertThat(DumpReader.deadlocked(dump)).containsExactly("A", "B", "C");
        assertThat(DumpReader.hottestMonitor(dump)).contains("L1");
    }

    @Test
    void aThreadBehindADeadlockIsNotInIt() {
        List<DumpReader.Snapshot> dump = List.of(
                blocked("A", "L1", "B"), blocked("B", "L2", "A"), free("C", RUNNABLE), blocked("D", "L2", "A"));
        assertThat(DumpReader.deadlocked(dump)).containsExactly("A", "B");
        assertThat(DumpReader.hottestMonitor(dump)).contains("L2");
    }
}
