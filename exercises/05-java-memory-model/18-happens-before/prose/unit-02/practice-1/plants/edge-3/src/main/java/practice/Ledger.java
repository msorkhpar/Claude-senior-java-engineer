package practice;

import java.util.concurrent.locks.ReentrantLock;

public final class Ledger {

    private final ReentrantLock lock = new ReentrantLock();
    private long balance;

    /** Adds amount, then runs whileLocked while still holding the ledger's lock. */
    public void deposit(long amount, Runnable whileLocked) {
        lock.lock();
        balance += amount;
        whileLocked.run();
        lock.unlock();
    }

    /** Takes amount off the balance, or throws IllegalStateException when the balance is smaller. */
    public void withdraw(long amount) {
        lock.lock();
        if (balance < amount) {
            throw new IllegalStateException("insufficient funds");
        }
        balance -= amount;
        lock.unlock();
    }

    /** Returns the balance. */
    public long balance() {
        lock.lock();
        long now = balance;
        lock.unlock();
        return now;
    }
}
