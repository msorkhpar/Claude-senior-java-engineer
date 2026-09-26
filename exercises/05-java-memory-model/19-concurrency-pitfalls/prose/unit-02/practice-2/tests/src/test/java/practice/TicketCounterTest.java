package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TicketCounterTest {

    @Test
    void sellsUpToCapacity() {
        TicketCounter two = new TicketCounter(TicketCounter.Cell.atomic(), 2);
        assertThat(two.sell()).isTrue();
        assertThat(two.sell()).isTrue();
        assertThat(two.sell()).isFalse();
        assertThat(two.sold()).isEqualTo(2);
    }

    @Test
    void aLostCompareAndSetIsRetried() {
        // Another thread sells a ticket just before this sale's first compare-and-set.
        InterferingCell cell = new InterferingCell(3);
        TicketCounter counter = new TicketCounter(cell, 10);
        assertThat(counter.sell()).as("the sale succeeds on a retry").isTrue();
        assertThat(counter.sold()).isEqualTo(5);
    }

    @Test
    void aRetryChecksTheCapacityAgain() {
        // One ticket is left, and another thread sells it just before this sale's compare-and-set.
        InterferingCell cell = new InterferingCell(9);
        TicketCounter counter = new TicketCounter(cell, 10);
        assertThat(counter.sell()).as("the sale is refused: the last ticket is gone").isFalse();
        assertThat(counter.sold()).isEqualTo(10);
    }

    /** A cell whose first compare-and-set loses to a sale made by another thread. */
    static final class InterferingCell implements TicketCounter.Cell {
        private int value;
        private boolean interfered;

        InterferingCell(int start) {
            this.value = start;
        }

        @Override
        public int get() {
            return value;
        }

        @Override
        public boolean compareAndSet(int expected, int next) {
            if (!interfered) {
                interfered = true;
                value++; // the other thread's sale lands first
                return false;
            }
            if (value != expected) {
                return false;
            }
            value = next;
            return true;
        }
    }
}
