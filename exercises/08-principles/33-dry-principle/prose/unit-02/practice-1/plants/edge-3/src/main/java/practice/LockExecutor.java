package practice;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/** The lock-try-finally pattern, written once. */
public final class LockExecutor {

    private final Lock lock;

    /** An executor that guards every action with this lock. */
    public LockExecutor(Lock lock) {
        this.lock = Objects.requireNonNull(lock, "lock must not be null");
    }

    /** Runs the action while holding the lock, and returns its result. */
    public <T> T withLock(Supplier<T> action) {
        Objects.requireNonNull(action, "action must not be null");
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /** Runs the action while holding the lock. */
    public void withLockRun(Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /** A bank account whose every operation goes through one LockExecutor. */
    public static final class Account {

        private final LockExecutor locker;
        private int balance;

        /** An account guarded by this executor, with this opening balance. */
        public Account(LockExecutor locker, int initialBalance) {
            this.locker = Objects.requireNonNull(locker, "locker must not be null");
            this.balance = initialBalance;
        }

        /** Adds the amount and returns the new balance. */
        public int deposit(int amount) {
            return locker.withLock(() -> {
                balance += amount;
                return balance;
            });
        }

        /** Takes the amount away and returns the new balance. */
        public int withdraw(int amount) {
            return locker.withLock(() -> {
                balance -= amount;
                return balance;
            });
        }

        /** The current balance. */
        public int getBalance() {
            return balance;
        }
    }
}
