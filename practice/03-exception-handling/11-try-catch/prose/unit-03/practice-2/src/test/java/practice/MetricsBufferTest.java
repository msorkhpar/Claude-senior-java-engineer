package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsBufferTest {

    @Test
    void aDrainLeavesTheBufferCollectingANewBatch() {
        MetricsBuffer buffer = new MetricsBuffer();
        buffer.record("queue.depth=17");
        buffer.record("pool.active=6");

        buffer.drain();
        assertThat(buffer.pendingCount()).isZero();

        buffer.record("queue.depth=4");
        assertThat(buffer.pendingCount()).isEqualTo(1);
    }

    @Test
    void theShipperReceivesTheLinesOfTheBatchItDrained() {
        MetricsBuffer buffer = new MetricsBuffer();
        buffer.record("queue.depth=17");
        buffer.record("pool.active=6");
        buffer.record("db.wait.ms=212");

        List<String> batch = buffer.drain();
        buffer.record("queue.depth=4");

        assertThat(batch).containsExactly("queue.depth=17", "pool.active=6", "db.wait.ms=212");
        assertThat(buffer.drain()).containsExactly("queue.depth=4");
    }
}
