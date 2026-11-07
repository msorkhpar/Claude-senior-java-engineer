package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The writers one ingest run keeps open, one per partition. A writer is opened the first time a
 * row for its partition arrives, and stays open until the run is closed.
 */
public class PartitionWriters implements AutoCloseable {

    /** Appends rows to one partition's file in the object store. */
    public interface Writer extends AutoCloseable {
        void write(String row) throws Exception;

        @Override
        void close() throws Exception;
    }

    /** Opens the writer for a partition. */
    public interface Opener {
        Writer open(String partition) throws Exception;
    }

    private final Opener opener;
    private final Map<String, Writer> writers = new LinkedHashMap<>();

    public PartitionWriters(Opener opener) {
        this.opener = opener;
    }

    /** Appends a row to its partition, opening that partition's writer if this is its first row. */
    public void write(String partition, String row) throws Exception {
        Writer writer = writers.get(partition);
        if (writer == null) {
            writer = opener.open(partition);
            writers.put(partition, writer);
        }
        writer.write(row);
    }

    /** The writers opened so far, in the order they were opened. */
    List<Writer> opened() {
        return new ArrayList<>(writers.values());
    }

    /** Ends the run: closes every writer it opened. */
    @Override
    public void close() throws Exception {
        List<Writer> opened = opened();
        Exception failure = null;
        for (int i = opened.size() - 1; i >= 0; i--) {
            try {
                opened.get(i).close();
            } catch (Exception e) {
                if (failure == null) failure = e;
            }
        }
        if (failure != null) throw failure;
    }
}
