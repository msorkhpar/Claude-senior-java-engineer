package practice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The buffer of one writer thread: records collect here and are drained to storage in batches.
 * A normal batch is a few thousand records; a replay after an outage can push hundreds of
 * thousands through the same buffer.
 */
public class RecordBuffer {

    /** The slots a buffer keeps between batches: enough for a normal batch. */
    static final int NORMAL_BATCH = 4_096;

    private ArrayList<String> records = new ArrayList<>(NORMAL_BATCH);

    public void add(String record) {
        records.add(record);
    }

    /** Adds one decoded frame; the frame header gives {@code count}, then the rows follow one by one. */
    public void addFrame(int count, Iterator<String> rows) {
        prepareFor(count);
        for (int i = 0; i < count; i++) {
            records.add(rows.next());
        }
    }

    /** Makes room for {@code incoming} more records before they are added one by one. */
    void prepareFor(int incoming) {
        // TODO: A frame of `incoming` rows is about to be added one row at a time. Make room for
        //       them, so that the array doesn't grow again while they are added.
        throw new IllegalStateException("TODO: prepareFor");
    }

    /** Returns every record added since the last drain, in order, and leaves the buffer empty. */
    public List<String> drain() {
        // TODO: Hand back the records in order and leave the buffer empty. After a replay the buffer
        //       must not keep a replay-sized array, and after a normal batch it should neither
        //       allocate nor grow again for the next one.
        throw new IllegalStateException("TODO: drain");
    }

    public int size() {
        return records.size();
    }

    /** The list the records are kept in; the tests read its array. */
    ArrayList<String> backing() {
        return records;
    }
}
