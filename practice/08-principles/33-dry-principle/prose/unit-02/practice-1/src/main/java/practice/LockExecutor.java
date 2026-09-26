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
        throw new UnsupportedOperationException("write withLock");
    }

    /** Runs the action while holding the lock. */
    public void withLockRun(Runnable action) {
        throw new UnsupportedOperationException("write withLockRun");
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
            throw new UnsupportedOperationException("write deposit");
        }

        /** Takes the amount away and returns the new balance. */
        public int withdraw(int amount) {
            throw new UnsupportedOperationException("write withdraw");
        }

        /** The current balance. */
        public int getBalance() {
            throw new UnsupportedOperationException("write getBalance");
        }
    }
}
