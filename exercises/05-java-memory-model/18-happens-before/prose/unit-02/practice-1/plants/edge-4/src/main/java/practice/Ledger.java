package practice;

public final class Ledger {

    private long balance;

    /** Adds amount, then runs whileLocked while still holding the ledger's lock. */
    public void deposit(long amount, Runnable whileLocked) {
        synchronized (this) {
            balance += amount;
            whileLocked.run();
        }
    }

    /** Takes amount off the balance, or throws IllegalStateException when the balance is smaller. */
    public void withdraw(long amount) {
        synchronized (this) {
            if (balance < amount) {
                throw new IllegalStateException("insufficient funds");  // the monitor is released
            }
            balance -= amount;
        }
    }

    /** Returns the balance. */
    public long balance() {
        synchronized (this) {
            return balance;
        }
    }
}
