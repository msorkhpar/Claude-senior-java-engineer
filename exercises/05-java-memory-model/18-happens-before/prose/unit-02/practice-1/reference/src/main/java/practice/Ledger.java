package practice;

public final class Ledger {

    private final Object lock = new Object();   // one private, final monitor for all state
    private long balance;

    /** Adds amount, then runs whileLocked while still holding the ledger's lock. */
    public void deposit(long amount, Runnable whileLocked) {
        synchronized (lock) {
            balance += amount;
            whileLocked.run();
        }
    }

    /** Takes amount off the balance, or throws IllegalStateException when the balance is smaller. */
    public void withdraw(long amount) {
        synchronized (lock) {
            if (balance < amount) {
                throw new IllegalStateException("insufficient funds");  // the monitor is released
            }
            balance -= amount;
        }
    }

    /** Returns the balance. */
    public long balance() {
        synchronized (lock) {
            return balance;
        }
    }
}
