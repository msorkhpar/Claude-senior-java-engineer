package practice;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Reads meter archives from the archive store through a small pool of connections. An archive
 * is text: the line "ARC1", then one record per line.
 */
public class ArchiveReader {

    /** A fixed set of connections to the archive store. */
    public static final class Pool {
        private final Deque<Lease> idle = new ArrayDeque<>();

        /** A pool of {@code size} connections, all reading the given archive. */
        public Pool(int size, String archive) {
            for (int i = 1; i <= size; i++) idle.addLast(new Lease(this, "lease-" + i, archive));
        }

        /** Takes an idle connection; close the lease to give it back. */
        public Lease borrow() {
            Lease lease = idle.pollFirst();
            if (lease == null) throw new IllegalStateException("pool exhausted");
            lease.inUse = true;
            return lease;
        }

        /** The connections waiting to be borrowed, next one first. */
        public List<Lease> idle() {
            return List.copyOf(idle);
        }

        void giveBack(Lease lease) {
            idle.addFirst(lease);
        }
    }

    /** One borrowed connection to the archive store. */
    public static final class Lease implements AutoCloseable {
        private final Pool pool;
        private final String id;
        private final String archive;
        boolean inUse;   // true while somebody holds this lease

        Lease(Pool pool, String id, String archive) {
            this.pool = pool;
            this.id = id;
            this.archive = archive;
        }

        String fetch() {
            return archive;
        }

        @Override
        public String toString() {
            return id;
        }

        /** Gives the connection back to its pool. */
        @Override
        public void close() {
            if (!inUse) return;
            inUse = false;
            pool.giveBack(this);
        }
    }

    /** Reads an archive over a lease. Closing it closes the lease, as a stream decorator does. */
    public static final class Decoder implements AutoCloseable {
        private final Lease lease;
        private final String[] lines;
        private int next = 1;

        /** Fetches the archive and checks its header. */
        public Decoder(Lease lease) throws IOException {
            String[] lines = lease.fetch().split("\n");
            if (!lines[0].equals("ARC1")) throw new IOException("not an archive: " + lines[0]);
            this.lease = lease;
            this.lines = lines;
        }

        /** The next record, or null after the last. */
        public String readRecord() {
            return next < lines.length ? lines[next++] : null;
        }

        @Override
        public void close() {
            lease.close();
        }
    }

    /** The first record of the archive, read over a connection borrowed from the pool. */
    public static String firstRecord(Pool pool) throws IOException {
        try (Lease lease = pool.borrow();
             Decoder decoder = new Decoder(lease)) {
            return decoder.readRecord();
        }
    }
}
