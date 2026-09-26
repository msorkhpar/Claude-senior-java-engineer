package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.EmptyStackException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SafeStackTest {

    /** Waits (bounded) until waiter is blocked on a lock that owner holds; false if it never is. */
    private static boolean blockedBy(Thread waiter, Thread owner) {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (waiter.getState() == Thread.State.TERMINATED) {
                return false;
            }
            ThreadInfo info = threads.getThreadInfo(waiter.threadId());
            if (info != null && info.getLockOwnerId() == owner.threadId()) {
                return true;
            }
            Thread.onSpinWait();
        }
        return false;
    }

    @Test
    void popsInReverseOrder() {
        SafeStack<Integer> stack = new SafeStack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.size()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(1);
        assertThat(stack.size()).isZero();
    }

    @Test
    void popOnEmptyThrowsAndKeepsSize() {
        SafeStack<String> stack = new SafeStack<>();
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::pop);
        assertThat(stack.size()).isZero();
        stack.push("a");
        assertThat(stack.pop()).isEqualTo("a");
        assertThatExceptionOfType(EmptyStackException.class).isThrownBy(stack::pop);
        assertThat(stack.size()).isZero();
    }

    @Test
    void growsPastItsFirstCapacity() {
        SafeStack<Integer> stack = new SafeStack<>();
        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }
        assertThat(stack.size()).isEqualTo(100);
        assertThat(stack.pop()).isEqualTo(99);
    }

    @Test
    void everyMethodTakesTheStacksLock() throws Exception {
        SafeStack<Integer> stack = new SafeStack<>();
        stack.push(7);
        AtomicInteger seen = new AtomicInteger(-1);
        Thread reader;
        synchronized (stack) {                 // a caller holds the stack's monitor
            reader = new Thread(() -> seen.set(stack.size()));
            reader.setDaemon(true);
            reader.start();
            assertThat(blockedBy(reader, Thread.currentThread())).as("size() waits for the stack's monitor").isTrue();
            assertThat(seen).hasValue(-1);
        }
        reader.join(5_000);
        assertThat(seen).hasValue(1);
    }
}
