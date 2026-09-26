package practice;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkersTest {

    private static final Runnable NOTHING = () -> { };

    @Test
    void makesUnstartedThreadsWithThePrefix() {
        ThreadFactory factory = Workers.factory("db-worker-", false, Thread.NORM_PRIORITY);
        Thread first = factory.newThread(NOTHING);
        Thread second = factory.newThread(NOTHING);
        assertThat(first.getState()).isEqualTo(Thread.State.NEW);
        assertThat(second.getState()).isEqualTo(Thread.State.NEW);
        assertThat(first.getName()).matches("db-worker-\\d+");
        assertThat(second.getName()).matches("db-worker-\\d+").isNotEqualTo(first.getName());
        assertThat(first.isDaemon()).isFalse();
        assertThat(first.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
    }

    @Test
    void namesCountUpFromZero() {
        ThreadFactory factory = Workers.factory("order-processor-", false, Thread.NORM_PRIORITY);
        assertThat(factory.newThread(NOTHING).getName()).isEqualTo("order-processor-0");
        assertThat(factory.newThread(NOTHING).getName()).isEqualTo("order-processor-1");
        assertThat(factory.newThread(NOTHING).getName()).isEqualTo("order-processor-2");
        assertThat(Workers.factory("kafka-consumer-", false, 5).newThread(NOTHING).getName())
                .isEqualTo("kafka-consumer-0");
    }

    @Test
    void daemonStatusIsApplied() {
        Thread monitor = Workers.factory("metrics-", true, Thread.NORM_PRIORITY).newThread(NOTHING);
        assertThat(monitor.isDaemon()).isTrue();
    }

    @Test
    void priorityIsApplied() {
        assertThat(Workers.factory("hi-", false, Thread.MAX_PRIORITY).newThread(NOTHING).getPriority())
                .isEqualTo(Thread.MAX_PRIORITY);
        assertThat(Workers.factory("lo-", false, Thread.MIN_PRIORITY).newThread(NOTHING).getPriority())
                .isEqualTo(Thread.MIN_PRIORITY);
    }

    @Test
    void aBadPriorityIsRefusedUpFront() {
        assertThatThrownBy(() -> Workers.factory("x-", false, 11)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Workers.factory("x-", false, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
