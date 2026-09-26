package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class AccountTest {

    @Test
    void depositsWithdrawsAndSnapshots() {
        Account account = new Account();
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(0, 0));
        account.deposit(100);
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(100, 1));
        assertThat(account.withdraw(30)).isTrue();
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(70, 2));
        assertThat(account.withdraw(70)).isTrue();
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(0, 3));
    }

    @Test
    void anOverdraftIsRefused() {
        Account account = new Account();
        account.deposit(70);
        assertThat(account.withdraw(500)).isFalse();
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(70, 1));
    }

    @Test
    void updatesTakeTheAccountsLock() throws InterruptedException {
        Account account = new Account();
        Thread depositor;
        synchronized (account) {
            depositor = daemon(() -> account.deposit(5));
            waitFor(() -> blockedOn(depositor, account) || !depositor.isAlive(), "the deposit to wait or finish");
            assertThat(blockedOn(depositor, account))
                    .as("deposit() waits while another thread holds the account's lock")
                    .isTrue();
        }
        depositor.join(5_000);
        assertThat(account.snapshot()).isEqualTo(new Account.Snapshot(5, 1));
    }

    @Test
    void aSnapshotTakesTheAccountsLock() throws InterruptedException {
        Account account = new Account();
        account.deposit(5);
        Account.Snapshot[] seen = new Account.Snapshot[1];
        Thread reader;
        synchronized (account) {
            reader = daemon(() -> seen[0] = account.snapshot());
            waitFor(() -> blockedOn(reader, account) || !reader.isAlive(), "the snapshot to wait or finish");
            assertThat(blockedOn(reader, account))
                    .as("snapshot() waits while another thread holds the account's lock")
                    .isTrue();
        }
        reader.join(5_000);
        assertThat(seen[0]).isEqualTo(new Account.Snapshot(5, 1));
    }

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static boolean blockedOn(Thread thread, Object monitor) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId());
        return info != null
                && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockInfo() != null
                && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(monitor);
    }

    private static void waitFor(BooleanSupplier condition, String what) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            assertThat(System.nanoTime() < deadline).as("timed out waiting for " + what).isTrue();
            Thread.sleep(1);
        }
    }
}
