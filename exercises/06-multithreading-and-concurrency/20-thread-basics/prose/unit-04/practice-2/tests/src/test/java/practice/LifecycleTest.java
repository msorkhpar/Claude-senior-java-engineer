package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.lang.Thread.State.BLOCKED;
import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;
import static org.assertj.core.api.Assertions.assertThat;

class LifecycleTest {

    @Test
    void acceptsRealLivesAndRejectsImpossibleOnes() {
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, TIMED_WAITING, RUNNABLE, TERMINATED))).isTrue();
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, BLOCKED, RUNNABLE, WAITING, RUNNABLE))).isTrue();
        assertThat(Lifecycle.isValid(List.of(NEW))).isTrue();
        assertThat(Lifecycle.isValid(List.of(NEW, TERMINATED))).isFalse();
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, RUNNABLE))).isFalse();
        assertThat(Lifecycle.isValid(List.of())).isFalse();
    }

    @Test
    void blockedCannotGoStraightToWaiting() {
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, BLOCKED, WAITING))).isFalse();
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, BLOCKED, TIMED_WAITING))).isFalse();
    }

    @Test
    void aNotifiedWaiterMayBeBlocked() {
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, WAITING, BLOCKED, RUNNABLE))).isTrue();
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, TIMED_WAITING, BLOCKED, RUNNABLE, TERMINATED))).isTrue();
    }

    @Test
    void newIsFirstAndTerminatedIsLast() {
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, TERMINATED, RUNNABLE))).isFalse();
        assertThat(Lifecycle.isValid(List.of(RUNNABLE, TERMINATED))).isFalse();
        assertThat(Lifecycle.isValid(List.of(NEW, RUNNABLE, NEW))).isFalse();
    }
}
