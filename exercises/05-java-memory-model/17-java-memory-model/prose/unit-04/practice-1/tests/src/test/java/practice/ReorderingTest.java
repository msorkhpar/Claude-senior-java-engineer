package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.Reordering.Kind.*;
import static practice.Reordering.canSwap;

class ReorderingTest {

    private static Reordering.Op op(Reordering.Kind kind, String target) {
        // Targets are built at run time, so comparing them with == instead of equals cannot pass.
        return new Reordering.Op(kind, new String(target));
    }

    @Test
    void swapsIndependentPlainAccesses() {
        assertThat(canSwap(op(WRITE, "a"), op(WRITE, "b"))).isTrue();
        assertThat(canSwap(op(READ, "b"), op(READ, "a"))).isTrue();
        assertThat(canSwap(op(WRITE, "a"), op(READ, "b"))).isTrue();
        assertThat(canSwap(op(READ, "a"), op(WRITE, "b"))).isTrue();
        assertThat(canSwap(op(READ, "a"), op(READ, "a"))).isTrue();
    }

    @Test
    void keepsAccessesToOneVariableInOrder() {
        assertThat(canSwap(op(WRITE, "a"), op(READ, "a"))).isFalse();
        assertThat(canSwap(op(READ, "a"), op(WRITE, "a"))).isFalse();
        assertThat(canSwap(op(WRITE, "a"), op(WRITE, "a"))).isFalse();
    }

    @Test
    void aVolatileWriteKeepsEarlierAccessesBeforeIt() {
        assertThat(canSwap(op(WRITE, "result"), op(VOLATILE_WRITE, "ready"))).isFalse();
        assertThat(canSwap(op(READ, "result"), op(VOLATILE_WRITE, "ready"))).isFalse();
        assertThat(canSwap(op(VOLATILE_WRITE, "ready"), op(WRITE, "result"))).isTrue();
        assertThat(canSwap(op(VOLATILE_WRITE, "ready"), op(VOLATILE_READ, "done"))).isFalse();
    }

    @Test
    void aVolatileReadKeepsLaterAccessesAfterIt() {
        assertThat(canSwap(op(VOLATILE_READ, "ready"), op(READ, "result"))).isFalse();
        assertThat(canSwap(op(VOLATILE_READ, "ready"), op(WRITE, "result"))).isFalse();
        assertThat(canSwap(op(READ, "result"), op(VOLATILE_READ, "ready"))).isTrue();
        assertThat(canSwap(op(VOLATILE_READ, "ready"), op(VOLATILE_READ, "done"))).isFalse();
    }

    @Test
    void synchronizedIsARoachMotel() {
        assertThat(canSwap(op(LOCK, "m"), op(WRITE, "x"))).isFalse();
        assertThat(canSwap(op(READ, "x"), op(UNLOCK, "m"))).isFalse();
        assertThat(canSwap(op(WRITE, "x"), op(LOCK, "m"))).isTrue();
        assertThat(canSwap(op(UNLOCK, "m"), op(READ, "x"))).isTrue();
        assertThat(canSwap(op(UNLOCK, "m"), op(LOCK, "n"))).isFalse();
    }

    @Test
    void aVolatileAccessNeverSwapsWithAMonitorOperation() {
        assertThat(canSwap(op(VOLATILE_WRITE, "ready"), op(LOCK, "m"))).isFalse();
        assertThat(canSwap(op(UNLOCK, "m"), op(VOLATILE_READ, "ready"))).isFalse();
        assertThat(canSwap(op(VOLATILE_READ, "ready"), op(LOCK, "m"))).isFalse();
        assertThat(canSwap(op(UNLOCK, "m"), op(VOLATILE_WRITE, "ready"))).isFalse();
        assertThat(canSwap(op(WRITE, "x"), op(LOCK, "m"))).isTrue();
    }
}
