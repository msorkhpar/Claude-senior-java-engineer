package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

class LogTailerTest {

    /**
     * A stream that behaves like a socket's on a platform thread: a read waits until a line is
     * sent, the remote side ends, the connection fails or the stream is closed, and an interrupt
     * does not end the wait.
     */
    static final class RemoteStream extends InputStream {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition changed = lock.newCondition();
        private final Deque<Byte> bytes = new ArrayDeque<>();
        private boolean ended, closed, readerWaiting;
        private IOException failure;

        void send(String line) {
            lock.lock();
            try {
                for (byte b : (line + "\n").getBytes(StandardCharsets.UTF_8)) bytes.add(b);
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        void end() {
            update(() -> ended = true);
        }

        void fail(IOException e) {
            update(() -> failure = e);
        }

        @Override
        public void close() {
            update(() -> closed = true);
        }

        private void update(Runnable change) {
            lock.lock();
            try {
                change.run();
                changed.signalAll();
            } finally {
                lock.unlock();
            }
        }

        /** Waits until the reader is waiting for data with nothing left to read. */
        boolean awaitReaderWaiting() throws InterruptedException {
            lock.lock();
            try {
                long left = TimeUnit.SECONDS.toNanos(5);
                while (!(readerWaiting && bytes.isEmpty())) {
                    if (left <= 0) return false;
                    left = changed.awaitNanos(left);
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read() throws IOException {
            lock.lock();
            try {
                while (true) {
                    if (closed) throw new IOException("Socket closed");
                    if (failure != null) throw failure;
                    if (!bytes.isEmpty()) return bytes.poll() & 0xff;
                    if (ended) return -1;
                    readerWaiting = true;
                    changed.signalAll();
                    changed.awaitUninterruptibly();
                    readerWaiting = false;
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) return 0;
            int first = read();
            if (first < 0) return -1;
            b[off] = (byte) first;
            return 1;
        }
    }

    private final RemoteStream stream = new RemoteStream();
    private final List<String> lines = Collections.synchronizedList(new ArrayList<>());
    private final List<IOException> pages = Collections.synchronizedList(new ArrayList<>());
    private final CountDownLatch paged = new CountDownLatch(1);
    private final CountDownLatch twoLines = new CountDownLatch(2);
    private final LogTailer tailer = new LogTailer(stream, line -> {
        lines.add(line);
        twoLines.countDown();
    }, e -> {
        pages.add(e);
        paged.countDown();
    });

    @AfterEach
    void freeTheReader() {
        stream.close();
    }

    /** Calls stop() on a thread of its own; true when it returned within five seconds. */
    private boolean stopsWithinFiveSeconds() throws InterruptedException {
        CountDownLatch returned = new CountDownLatch(1);
        Thread stopper = new Thread(() -> {
            try {
                tailer.stop();
                returned.countDown();
            } catch (InterruptedException ignored) {
            }
        });
        stopper.setDaemon(true);
        stopper.start();
        return returned.await(5, TimeUnit.SECONDS);
    }

    @Test
    void handsOnEveryLineUntilTheRemoteSideEnds() throws InterruptedException {
        tailer.start();
        stream.send("10:02:11 GET /cart 200");
        stream.send("10:02:12 POST /checkout 502");
        stream.end();
        assertThat(twoLines.await(5, TimeUnit.SECONDS)).isTrue();

        assertThat(stopsWithinFiveSeconds()).isTrue();
        assertThat(lines).containsExactly("10:02:11 GET /cart 200", "10:02:12 POST /checkout 502");
        assertThat(pages).isEmpty();
    }

    @Test
    void stopReturnsWhileTheReaderWaitsForTheNextLine() throws InterruptedException {
        tailer.start();
        stream.send("10:05:40 GET /health 200");
        assertThat(stream.awaitReaderWaiting()).isTrue();

        assertThat(stopsWithinFiveSeconds()).isTrue();
    }

    @Test
    void stoppingATailerPagesNobody() throws InterruptedException {
        tailer.start();
        stream.send("10:07:03 GET /search?q=lamp 200");
        assertThat(stream.awaitReaderWaiting()).isTrue();

        assertThat(stopsWithinFiveSeconds()).isTrue();
        assertThat(pages).isEmpty();
        assertThat(lines).containsExactly("10:07:03 GET /search?q=lamp 200");
    }

    @Test
    void aBrokenStreamPagesOnCall() throws InterruptedException {
        tailer.start();
        stream.send("10:09:15 GET /orders 200");
        assertThat(stream.awaitReaderWaiting()).isTrue();

        stream.fail(new IOException("Connection reset by peer"));

        assertThat(paged.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(pages).extracting(Throwable::getMessage).containsExactly("Connection reset by peer");
    }
}
