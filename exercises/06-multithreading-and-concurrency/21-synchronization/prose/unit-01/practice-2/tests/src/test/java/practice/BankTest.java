package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BankTest {

    private static final Runnable NOTHING = () -> { };

    /** Whether {@code waiter} is blocked on a lock that {@code holder} holds. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    @Test
    void movesMoney() {
        Bank.Account a = new Bank.Account(1, 100);
        Bank.Account b = new Bank.Account(2, 50);
        assertThat(Bank.transfer(a, b, 30, NOTHING)).isTrue();
        assertThat(a.balance()).isEqualTo(70);
        assertThat(b.balance()).isEqualTo(80);
    }

    /**
     * Each transfer, once it holds its first lock, waits (at most 6 s) until the other transfer
     * also holds its first lock, or is blocked on the lock this one holds, or has finished. With a lock order
     * the second transfer can only be parked; without one, both hold a lock and both go on.
     */
    @Test
    void oppositeTransfersBothFinish() throws InterruptedException {
        Bank.Account a = new Bank.Account(1, 100);
        Bank.Account b = new Bank.Account(2, 50);
        AtomicBoolean[] holding = {new AtomicBoolean(), new AtomicBoolean()};
        AtomicReferenceArray<Thread> threads = new AtomicReferenceArray<>(2);
        AtomicInteger hookCalls = new AtomicInteger();
        AtomicBoolean[] result = {new AtomicBoolean(), new AtomicBoolean()};
        for (int i = 0; i < 2; i++) {
            int me = i;
            Runnable hook = () -> {
                hookCalls.incrementAndGet();
                holding[me].set(true);
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(6);
                while (System.nanoTime() < deadline) {
                    Thread other = threads.get(1 - me);
                    if (holding[1 - me].get() || (other != null
                            && (other.getState() == Thread.State.TERMINATED || blockedBy(other, Thread.currentThread())))) {
                        return;
                    }
                    Thread.onSpinWait();
                }
            };
            Thread t = new Thread(() -> result[me].set(me == 0
                    ? Bank.transfer(a, b, 30, hook)
                    : Bank.transfer(b, a, 20, hook)));
            t.setDaemon(true);
            threads.set(i, t);
        }
        threads.get(0).start();
        threads.get(1).start();
        threads.get(0).join(5_000);
        threads.get(1).join(5_000);
        assertThat(threads.get(0).isAlive() || threads.get(1).isAlive())
                .as("the two transfers are deadlocked").isFalse();
        assertThat(hookCalls.get()).as("holdingFirst runs once per transfer").isEqualTo(2);
        assertThat(result[0].get() && result[1].get()).isTrue();
        assertThat(a.balance()).isEqualTo(90);
        assertThat(b.balance()).isEqualTo(60);
    }

    @Test
    void refusesAnOverdraft() {
        Bank.Account a = new Bank.Account(1, 10);
        Bank.Account b = new Bank.Account(2, 50);
        assertThat(Bank.transfer(a, b, 30, NOTHING)).isFalse();
        assertThat(a.balance()).isEqualTo(10);
        assertThat(b.balance()).isEqualTo(50);
    }

    @Test
    void refusesATransferToItself() {
        Bank.Account a = new Bank.Account(1, 100);
        assertThat(Bank.transfer(a, a, 10, NOTHING)).isFalse();
        assertThat(a.balance()).isEqualTo(100);
    }
}
