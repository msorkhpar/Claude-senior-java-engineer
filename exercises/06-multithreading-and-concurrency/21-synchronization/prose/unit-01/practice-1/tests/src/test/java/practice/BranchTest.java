package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BranchTest {

    private static final Branch.Settings QUICK = name -> "settings of " + name;

    /** A journal that remembers its lines and notices two appends running at the same time. */
    static class Journal implements Branch.Journal {
        final List<String> lines = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger inside = new AtomicInteger();
        final AtomicBoolean overlapped = new AtomicBoolean();
        final CountDownLatch firstInside = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final boolean holdFirst;
        final AtomicBoolean first = new AtomicBoolean(true);

        Journal(boolean holdFirst) {
            this.holdFirst = holdFirst;
        }

        @Override
        public void append(long number, String branch, String customer) {
            if (inside.incrementAndGet() > 1) overlapped.set(true);
            try {
                lines.add(number + " " + branch + " " + customer);
                if (holdFirst && first.getAndSet(false)) {
                    firstInside.countDown();
                    release.await(20, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inside.decrementAndGet();
            }
        }
    }

    private final List<Thread> started = new ArrayList<>();

    Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        started.add(t);
        return t;
    }

    @AfterEach
    void finishThreads() throws InterruptedException {
        for (Thread t : started) t.join(10_000);
    }

    /** Whether waiter is waiting for a monitor that holder owns. */
    static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    static void awaitUntil(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.onSpinWait();
    }

    @Test
    void numbersFollowOneCompanySequenceAcrossBranches() {
        Journal journal = new Journal(false);
        Branch leeds = Branch.open("leeds-m1", QUICK, journal);
        Branch york = Branch.open("york-m1", QUICK, journal);

        long a = leeds.issue("Jane Doe");
        long b = york.issue("John Roe");
        long c = leeds.issue("Ada Poe");

        assertThat(b).isEqualTo(a + 1);
        assertThat(c).isEqualTo(b + 1);
        assertThat(journal.lines).containsExactly(
                a + " leeds-m1 Jane Doe", b + " york-m1 John Roe", c + " leeds-m1 Ada Poe");
    }

    @Test
    void anOpenedBranchKeepsItsSettings() {
        Journal journal = new Journal(false);
        Branch hull = Branch.open("hull-m2", QUICK, journal);
        assertThat(Branch.open("hull-m2", name -> "other", journal)).isSameAs(hull);
        assertThat(hull.settings()).isEqualTo("settings of hull-m2");
        assertThat(hull.issue("Jane Doe")).isPositive();
    }

    @Test
    void twoBranchesNeverIssueAtTheSameTime() {
        Journal journal = new Journal(true);
        Branch leeds = Branch.open("leeds-e1", QUICK, journal);
        Branch york = Branch.open("york-e1", QUICK, journal);
        Thread first = daemon(() -> leeds.issue("Jane Doe"));
        try {
            assertThat(journal.firstInside.await(10, TimeUnit.SECONDS)).isTrue();
            Thread second = daemon(() -> york.issue("John Roe"));
            awaitUntil(() -> journal.overlapped.get() || blockedBy(second, first));
            assertThat(journal.overlapped.get())
                    .as("york issued a number while leeds was still writing its own").isFalse();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            journal.release.countDown();
        }
    }

    @Test
    void twoTillsOfOneBranchNeverIssueAtTheSameTime() {
        Journal journal = new Journal(true);
        Branch leeds = Branch.open("leeds-e2", QUICK, journal);
        Thread first = daemon(() -> leeds.issue("Jane Doe"));
        try {
            assertThat(journal.firstInside.await(10, TimeUnit.SECONDS)).isTrue();
            Thread second = daemon(() -> leeds.issue("John Roe"));
            awaitUntil(() -> journal.overlapped.get() || blockedBy(second, first));
            assertThat(journal.overlapped.get())
                    .as("a second till issued a number while the first was still writing its own").isFalse();
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            journal.release.countDown();
        }
    }

    @Test
    void issuingGoesOnWhileABranchIsBeingOpened() throws InterruptedException {
        Journal journal = new Journal(false);
        Branch leeds = Branch.open("leeds-e3", QUICK, journal);
        CountDownLatch loading = new CountDownLatch(1);
        CountDownLatch loaded = new CountDownLatch(1);
        Branch.Settings slow = name -> {
            loading.countDown();
            try {
                loaded.await(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "settings of " + name;
        };
        Thread opener = daemon(() -> Branch.open("selby-e3", slow, journal));
        try {
            assertThat(loading.await(10, TimeUnit.SECONDS)).isTrue();
            AtomicLong issued = new AtomicLong();
            Thread till = daemon(() -> issued.set(leeds.issue("Jane Doe")));
            awaitUntil(() -> !till.isAlive() || blockedBy(till, opener));
            assertThat(till.isAlive()).as("issuing waited for the branch being opened").isFalse();
            assertThat(issued.get()).isPositive();
        } finally {
            loaded.countDown();
        }
    }
}
