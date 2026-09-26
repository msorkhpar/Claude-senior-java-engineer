package practice;

import java.util.concurrent.locks.ReentrantLock;

public final class Vault {

    private final ReentrantLock lock = new ReentrantLock();
    private int balance;

    /** Starts the vault with {@code balance}. */
    public Vault(int balance) {
        this.balance = balance;
    }

    /** Takes {@code amount} out under the lock and returns the new balance. */
    public int withdraw(int amount) {
        lock.lock();
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("amount must be positive");
            }
            balance -= amount;
            if (balance < 0) {
                throw new IllegalStateException("insufficient funds");
            }
            return balance;
        } finally {
            lock.unlock();
        }
    }

    /** Returns the current balance. */
    public int balance() {
        return balance;
    }

    /** Returns whether the vault's lock is held. */
    public boolean isLocked() {
        return lock.isLocked();
    }
}
