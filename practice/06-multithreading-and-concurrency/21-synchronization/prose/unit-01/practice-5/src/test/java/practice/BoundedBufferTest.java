package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BoundedBufferTest {

    private interface Body {
        void run() throws Exception;
    }

    private static Thread daemon(Body body, AtomicReference<Throwable> thrown) {
        Thread thread = new Thread(() -> {
            try {
                body.run();
            } catch (Throwable t) {
                thrown.set(t);
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /** Whether the thread is inside Object.wait, called from the named BoundedBuffer method. */
    private static boolean inWait(Thread thread, String method) {
        boolean waiting = false;
        boolean inMethod = false;
        for (StackTraceElement frame : thread.getStackTrace()) {
            waiting |= frame.getClassName().equals("java.lang.Object") && frame.getMethodName().startsWith("wait");
            inMethod |= frame.getClassName().equals(BoundedBuffer.class.getName()) && frame.getMethodName().equals(method);
        }
        return waiting && inMethod;
    }

    /** Waits, at most 5 s, until the thread waits inside the named method, or has ended; returns whether it waits. */
    private static boolean waits(Thread thread, String method) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!inWait(thread, method) && thread.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(1);
        }
        return inWait(thread, method);
    }

    @Test
    void putThenTake() throws InterruptedException {
        BoundedBuffer<String> buffer = new BoundedBuffer<>(2);
        buffer.put("a");
        assertThat(buffer.size()).isEqualTo(1);
        assertThat(buffer.take()).isEqualTo("a");
        assertThat(buffer.size()).isZero();
    }

    @Test
    void takesInArrivalOrder() throws InterruptedException {
        BoundedBuffer<String> buffer = new BoundedBuffer<>(3);
        buffer.put("a");
        buffer.put("b");
        buffer.put("c");
        assertThat(buffer.take()).isEqualTo("a");
        assertThat(buffer.take()).isEqualTo("b");
        assertThat(buffer.take()).isEqualTo("c");
    }

    @Test
    void putWaitsWhileFull() throws InterruptedException {
        BoundedBuffer<String> buffer = new BoundedBuffer<>(1);
        buffer.put("x");
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread producer = daemon(() -> buffer.put("y"), thrown);
        assertThat(waits(producer, "put")).as("put on a full buffer waits").isTrue();
        assertThat(buffer.size()).isEqualTo(1);
        assertThat(buffer.take()).isEqualTo("x");
        producer.join(5_000);
        assertThat(producer.isAlive()).as("the waiting put finished after a take").isFalse();
        assertThat(thrown.get()).isNull();
        assertThat(buffer.take()).isEqualTo("y");
    }

    @Test
    void takeWaitsWhileEmpty() throws InterruptedException {
        BoundedBuffer<String> buffer = new BoundedBuffer<>(2);
        AtomicReference<String> taken = new AtomicReference<>();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread consumer = daemon(() -> taken.set(buffer.take()), thrown);
        assertThat(waits(consumer, "take")).as("take on an empty buffer waits").isTrue();
        buffer.put("z");
        consumer.join(5_000);
        assertThat(thrown.get()).isNull();
        assertThat(taken.get()).isEqualTo("z");
    }

    @Test
    void aWaitingTakeCanBeInterrupted() throws InterruptedException {
        BoundedBuffer<String> buffer = new BoundedBuffer<>(2);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread consumer = daemon(buffer::take, thrown);
        assertThat(waits(consumer, "take")).as("take on an empty buffer waits").isTrue();
        consumer.interrupt();
        consumer.join(5_000);
        assertThat(consumer.isAlive()).as("the interrupted take is still waiting").isFalse();
        assertThat(thrown.get()).isInstanceOf(InterruptedException.class);
    }
}
