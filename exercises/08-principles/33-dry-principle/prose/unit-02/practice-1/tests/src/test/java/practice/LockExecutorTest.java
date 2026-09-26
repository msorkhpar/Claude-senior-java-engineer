package practice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.*;

class LockExecutorTest {

    /** A ReentrantLock that counts how often it was acquired. */
    static final class CountingLock extends ReentrantLock {
        int acquired;

        @Override
        public void lock() {
            acquired++;
            super.lock();
        }
    }

    @Test
    void runsEachActionWhileHoldingTheLock() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        LockExecutor executor = new LockExecutor(lock);
        int value = executor.withLock(() -> {
            assertThat(lock.isHeldByCurrentThread()).isTrue();
            return 1234;
        });
        assertThat(value).isEqualTo(1234);
        boolean[] ran = {false};
        executor.withLockRun(() -> ran[0] = lock.isHeldByCurrentThread());
        assertThat(ran[0]).isTrue();
        assertThat(lock.isLocked()).isFalse();
        LockExecutor.Account account = new LockExecutor.Account(new LockExecutor(new ReentrantLock()), 1000);
        assertThat(account.deposit(500)).isEqualTo(1500);
        assertThat(account.withdraw(300)).isEqualTo(1200);
        assertThat(account.getBalance()).isEqualTo(1200);
    }

    @Test
    void theLockIsReleasedWhenTheActionThrows() throws Exception {
        ReentrantLock lock = new ReentrantLock();
        LockExecutor executor = new LockExecutor(lock);
        assertThatThrownBy(() -> executor.withLock(() -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(lock.isLocked()).as("after withLock threw").isFalse();
        assertThatThrownBy(() -> executor.withLockRun(() -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(lock.isLocked()).as("after withLockRun threw").isFalse();
    }

    @Test
    void aNullActionIsRefusedBeforeLocking() throws Exception {
        CountingLock lock = new CountingLock();
        LockExecutor executor = new LockExecutor(lock);
        assertThatThrownBy(() -> executor.withLock(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> executor.withLockRun(null)).isInstanceOf(NullPointerException.class);
        assertThat(lock.acquired).as("times the lock was taken").isZero();
    }

    @Test
    void everyAccountOperationTakesTheLock() throws Exception {
        CountingLock lock = new CountingLock();
        LockExecutor.Account account = new LockExecutor.Account(new LockExecutor(lock), 1000);
        account.deposit(200);
        account.withdraw(50);
        assertThat(account.getBalance()).isEqualTo(1150);
        assertThat(lock.acquired).as("deposit, withdraw and getBalance each take the lock").isEqualTo(3);
    }

    @Test
    void theUpdateHappensUnderTheLock() throws Exception {
        java.util.List<Long> storedAtUnlock = new java.util.ArrayList<>();
        LockExecutor.Account[] holder = new LockExecutor.Account[1];
        ReentrantLock watching = new ReentrantLock() {
            @Override
            public void unlock() {
                if (holder[0] != null) {
                    for (java.lang.reflect.Field f : LockExecutor.Account.class.getDeclaredFields()) {
                        Class<?> type = f.getType();
                        if (type == int.class || type == long.class || Number.class.isAssignableFrom(type)) {
                            f.setAccessible(true);
                            try {
                                Object value = f.get(holder[0]);
                                if (value != null) {
                                    storedAtUnlock.add(((Number) value).longValue());
                                }
                            } catch (IllegalAccessException e) {
                                throw new AssertionError(e);
                            }
                        }
                    }
                }
                super.unlock();
            }
        };
        holder[0] = new LockExecutor.Account(new LockExecutor(watching), 1000);
        storedAtUnlock.clear();
        holder[0].deposit(200);
        assertThat(storedAtUnlock).as("the balance field held 1200 when deposit released the lock").contains(1200L);
    }

    @Test
    void depositReturnsTheBalanceItMade() throws Exception {
        LockExecutor.Account[] holder = new LockExecutor.Account[1];
        boolean[] armed = {false};
        ReentrantLock interleaving = new ReentrantLock() {
            @Override
            public void unlock() {
                super.unlock();
                if (armed[0] && !isHeldByCurrentThread()) {
                    armed[0] = false;
                    Thread other = new Thread(() -> holder[0].withdraw(50));
                    other.start();
                    try {
                        other.join(5_000);
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        };
        holder[0] = new LockExecutor.Account(new LockExecutor(interleaving), 1000);
        armed[0] = true;
        assertThat(holder[0].deposit(200)).as("the balance deposit made, not one read after unlocking").isEqualTo(1200);
        assertThat(holder[0].getBalance()).isEqualTo(1150);
    }
}
