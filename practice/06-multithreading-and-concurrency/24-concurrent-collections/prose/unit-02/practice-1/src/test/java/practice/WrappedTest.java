package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WrappedTest {

    private static String word(String s) {
        return new String(s.toCharArray());
    }

    private static List<String> abc() {
        return Collections.synchronizedList(new ArrayList<>(List.of(word("a"), word("b"), word("c"))));
    }

    /** Whether waiter is blocked on a monitor that holder owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    @Test
    void mapsAndCopies() {
        List<String> syncList = abc();
        List<String> upper = Wrapped.mapAll(syncList, String::toUpperCase);
        assertThat(upper).containsExactly("A", "B", "C");
        assertThat(Wrapped.mapAll(syncList, String::length)).containsExactly(1, 1, 1);
        List<String> copy = Wrapped.snapshot(syncList);
        assertThat(copy).containsExactly("a", "b", "c");
        assertThat(syncList).containsExactly("a", "b", "c");
    }

    /**
     * While f runs on the first element, a writer thread calls add("d"). f then waits (at most 3 s)
     * until the writer is either blocked on a lock this thread holds, or has finished its add.
     */
    @Test
    void holdsTheWrapperLockWhileIterating() throws InterruptedException {
        List<String> syncList = abc();
        AtomicReference<Thread> writer = new AtomicReference<>();
        List<String> answer = Wrapped.mapAll(syncList, s -> {
            if (writer.get() == null) {
                Thread w = new Thread(() -> syncList.add(word("d")));
                w.setDaemon(true);
                writer.set(w);
                w.start();
                Thread me = Thread.currentThread();
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                while (System.nanoTime() < deadline
                        && w.getState() != Thread.State.TERMINATED
                        && !(w.getState() == Thread.State.BLOCKED && blockedBy(w, me))) {
                    Thread.onSpinWait();
                }
            }
            return s.toUpperCase();
        });
        writer.get().join(5_000);
        assertThat(writer.get().isAlive()).as("the writer finished after the walk").isFalse();
        assertThat(answer).containsExactly("A", "B", "C");
        assertThat(syncList).containsExactly("a", "b", "c", "d");
    }

    @Test
    void snapshotIsACopy() {
        List<String> syncList = abc();
        List<String> copy = Wrapped.snapshot(syncList);
        syncList.add(word("d"));
        assertThat(copy).containsExactly("a", "b", "c");
    }
}
