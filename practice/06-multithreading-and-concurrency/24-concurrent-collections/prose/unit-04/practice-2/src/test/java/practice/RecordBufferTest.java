package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RecordBufferTest {

    private static Object[] array(ArrayList<?> list) {
        try {
            Field data = ArrayList.class.getDeclaredField("elementData");
            data.setAccessible(true);
            return (Object[]) data.get(list);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot read the array of the buffer's list", e);
        }
    }

    private static int slots(RecordBuffer buffer) {
        return array(buffer.backing()).length;
    }

    private static String record(String kind, int i) {
        return kind + "-" + (700_000 + i * 3);
    }

    private static void addSingles(RecordBuffer buffer, String kind, int count) {
        for (int i = 0; i < count; i++) buffer.add(record(kind, i));
    }

    /** Rows of a frame; remembers the buffer's slots when the first row is read, after prepareFor(). */
    private static final class Rows implements Iterator<String> {
        private final RecordBuffer buffer;
        private final int count;
        private final AtomicInteger slotsAtFirstRow = new AtomicInteger(-1);
        private int next;

        Rows(RecordBuffer buffer, int count) {
            this.buffer = buffer;
            this.count = count;
        }

        @Override public boolean hasNext() { return next < count; }

        @Override public String next() {
            if (next == 0) slotsAtFirstRow.set(slots(buffer));
            return record("frame", next++);
        }
    }

    @Test
    void drainHandsBackSinglesAndFramesInOrder() {
        RecordBuffer buffer = new RecordBuffer();
        buffer.add("audit-1");
        buffer.addFrame(3, List.of("frame-a", "frame-b", "frame-c").iterator());
        buffer.add("audit-2");

        assertThat(buffer.drain()).containsExactly("audit-1", "frame-a", "frame-b", "frame-c", "audit-2");
        assertThat(buffer.size()).isZero();
        buffer.add("audit-3");
        assertThat(buffer.drain()).containsExactly("audit-3");
    }

    @Test
    void aReplayIsDrainedWhole() {
        RecordBuffer buffer = new RecordBuffer();
        addSingles(buffer, "replay", 30_000);
        buffer.addFrame(20_000, new Rows(buffer, 20_000));

        List<String> batch = buffer.drain();

        assertThat(batch).hasSize(50_000);
        assertThat(batch.get(29_999)).isEqualTo(record("replay", 29_999));
        assertThat(batch.get(30_000)).isEqualTo(record("frame", 0));
        assertThat(buffer.size()).isZero();
    }

    @Test
    void afterAReplayTheBufferKeepsOnlyANormalBatchOfSlots() {
        RecordBuffer buffer = new RecordBuffer();
        addSingles(buffer, "replay", 50_000);
        buffer.drain();

        assertThat(slots(buffer)).as("slots after the replay was drained").isLessThanOrEqualTo(RecordBuffer.NORMAL_BATCH);

        int before = slots(buffer);
        addSingles(buffer, "normal", 3_500);
        assertThat(slots(buffer)).as("slots after the next normal batch").isEqualTo(before);
    }

    @Test
    void aNormalBatchReusesTheSameArray() {
        RecordBuffer buffer = new RecordBuffer();
        addSingles(buffer, "normal", 3_000);
        Object[] before = array(buffer.backing());

        buffer.drain();
        addSingles(buffer, "next", 3_900);

        assertThat(array(buffer.backing())).as("the array after a drain and another normal batch").isSameAs(before);
    }

    @Test
    void aFrameOnTopOfRecordsGrowsTheArrayOnlyBeforeItsRows() {
        RecordBuffer buffer = new RecordBuffer();
        addSingles(buffer, "single", 3_000);
        Rows rows = new Rows(buffer, 2_500);

        buffer.addFrame(2_500, rows);

        assertThat(buffer.size()).isEqualTo(5_500);
        assertThat(slots(buffer)).as("slots after the frame").isEqualTo(rows.slotsAtFirstRow.get());
    }
}
