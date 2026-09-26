package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LedgerTest {

    private static Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

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

    /** Starts a deposit of 100 whose hook holds the lock until release is counted down. */
    private static Thread holdDeposit(Ledger ledger, CountDownLatch release) throws InterruptedException {
        CountDownLatch inside = new CountDownLatch(1);
        Thread depositor = daemon(() -> ledger.deposit(100, () -> {
            inside.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        assertThat(inside.await(5, TimeUnit.SECONDS)).as("the deposit reached its hook").isTrue();
        return depositor;
    }

    @Test
    void keepsTheBalance() {
        Ledger ledger = new Ledger();
        ledger.deposit(100, () -> { });
        ledger.withdraw(30);
        assertThat(ledger.balance()).isEqualTo(70);
        assertThatIllegalStateException().isThrownBy(() -> ledger.withdraw(500));
        assertThat(ledger.balance()).isEqualTo(70);
    }

    @Test
    void balanceWaitsForADeposit() throws Exception {
        Ledger ledger = new Ledger();
        CountDownLatch release = new CountDownLatch(1);
        Thread depositor = holdDeposit(ledger, release);
        AtomicLong seen = new AtomicLong(-1);
        try {
            Thread reader = daemon(() -> seen.set(ledger.balance()));
            assertThat(blockedBy(reader, depositor)).as("balance() waits for the deposit's lock").isTrue();
            release.countDown();
            reader.join(5_000);
            assertThat(seen.get()).isEqualTo(100);
        } finally {
            release.countDown();
        }
    }

    @Test
    void withdrawWaitsForADeposit() throws Exception {
        Ledger ledger = new Ledger();
        CountDownLatch release = new CountDownLatch(1);
        Thread depositor = holdDeposit(ledger, release);
        try {
            Thread taker = daemon(() -> ledger.withdraw(10));
            assertThat(blockedBy(taker, depositor)).as("withdraw() waits for the deposit's lock").isTrue();
            release.countDown();
            taker.join(5_000);
            assertThat(ledger.balance()).isEqualTo(90);
        } finally {
            release.countDown();
        }
    }

    @Test
    void aRefusedWithdrawalReleasesTheLock() throws Exception {
        Ledger ledger = new Ledger();
        ledger.deposit(20, () -> { });
        assertThatIllegalStateException().isThrownBy(() -> ledger.withdraw(50));
        Thread other = daemon(() -> ledger.deposit(5, () -> { }));
        other.join(2_000);
        assertThat(other.isAlive()).as("another thread's deposit got the lock").isFalse();
        assertThat(ledger.balance()).isEqualTo(25);
    }
}
