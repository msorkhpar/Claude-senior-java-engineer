package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WorkBufferTest {

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /**
     * Waits, at most 20 s, until t has finished or is parked inside WorkBuffer.put on a
     * java.util.concurrent lock or condition, or in Object.wait on a monitor; true if parked there.
     */
    private static boolean parked(Thread t) {
        return parked(t, "put");
    }

    /** As parked(t), for the named WorkBuffer method. */
    private static boolean parked(Thread t, String method) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            Thread.State state = t.getState();
            if (state == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = THREADS.getThreadInfo(t.threadId(), Integer.MAX_VALUE);
            if ((state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING)
                    && info != null && info.getLockInfo() != null && inside(info, method)
                    && (info.getLockInfo().getClassName().startsWith("java.util.concurrent.")
                        || waitsOnMonitor(info))) {
                return true;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return false;
    }

    private static boolean inside(ThreadInfo info, String method) {
        for (StackTraceElement frame : info.getStackTrace()) {
            if (frame.getClassName().equals("practice.WorkBuffer") && frame.getMethodName().equals(method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean waitsOnMonitor(ThreadInfo info) {
        for (StackTraceElement frame : info.getStackTrace()) {
            if (frame.getClassName().equals("java.lang.Object") && frame.getMethodName().startsWith("wait")) {
                return true;
            }
        }
        return false;
    }

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    @Test
    void handsItemsOverInOrder() throws InterruptedException {
        WorkBuffer<String> buffer = new WorkBuffer<>(2);
        buffer.put("a");
        buffer.put("b");
        assertThat(buffer.size()).isEqualTo(2);
        assertThat(buffer.take()).isEqualTo("a");
        assertThat(buffer.take()).isEqualTo("b");
        assertThat(buffer.produced()).isEqualTo(2);
        assertThat(buffer.consumed()).isEqualTo(2);
        assertThat(buffer.size()).isZero();

        AtomicReference<String> received = new AtomicReference<>();
        Thread consumer = daemon(() -> {
            try {
                received.set(buffer.take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        buffer.put("c");
        consumer.join(20_000);
        assertThat(consumer.isAlive()).isFalse();
        assertThat(received.get()).isEqualTo("c");
        assertThat(buffer.offer("d", 1_000)).isTrue();
        assertThat(buffer.poll(1_000)).isEqualTo("d");
    }

    @Test
    void aFullBufferMakesPutWait() throws InterruptedException {
        WorkBuffer<String> buffer = new WorkBuffer<>(1);
        buffer.put("a");
        Thread producer = daemon(() -> {
            try {
                buffer.put("b");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(parked(producer)).as("put waits on a full buffer").isTrue();
        assertThat(buffer.take()).isEqualTo("a");
        producer.join(20_000);
        assertThat(producer.isAlive()).isFalse();
        assertThat(buffer.take()).as("the waiting item was added, not dropped").isEqualTo("b");
        assertThat(buffer.produced()).isEqualTo(2);
    }

    @Test
    void offerGivesUpWhenFull() throws InterruptedException {
        WorkBuffer<String> buffer = new WorkBuffer<>(1);
        assertThat(buffer.poll(50)).isNull();
        assertThat(buffer.offer("a", 50)).isTrue();
        assertThat(buffer.offer("b", 50)).as("no room appeared").isFalse();
        assertThat(buffer.size()).isEqualTo(1);
        assertThat(buffer.produced()).isEqualTo(1);
        assertThat(buffer.consumed()).isZero();
    }

    @Test
    void offerWaitsForRoomWithinItsTimeout() throws InterruptedException {
        WorkBuffer<String> buffer = new WorkBuffer<>(1);
        buffer.put("a");
        AtomicReference<Boolean> added = new AtomicReference<>();
        Thread producer = daemon(() -> {
            try {
                added.set(buffer.offer("b", 30_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(parked(producer, "offer")).as("offer waits on a full buffer, within its timeout").isTrue();
        assertThat(buffer.take()).isEqualTo("a");
        producer.join(20_000);
        assertThat(producer.isAlive()).isFalse();
        assertThat(added.get()).as("room appeared within the timeout").isTrue();
        assertThat(buffer.take()).isEqualTo("b");
    }

    @Test
    void pollWaitsForAnItemWithinItsTimeout() throws InterruptedException {
        WorkBuffer<String> buffer = new WorkBuffer<>(1);
        AtomicReference<String> received = new AtomicReference<>();
        Thread consumer = daemon(() -> {
            try {
                received.set(buffer.poll(30_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertThat(parked(consumer, "poll")).as("poll waits on an empty buffer, within its timeout").isTrue();
        buffer.put("z");
        consumer.join(20_000);
        assertThat(consumer.isAlive()).isFalse();
        assertThat(received.get()).as("an item appeared within the timeout").isEqualTo("z");
        assertThat(buffer.consumed()).isEqualTo(1);
    }
}
