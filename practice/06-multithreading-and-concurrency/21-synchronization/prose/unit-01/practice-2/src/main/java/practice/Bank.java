package practice;

public final class Bank {

    private Bank() {
    }

    /** An account; its balance is guarded by the account object itself. */
    public static final class Account {
        private final long id;
        int balance;

        public Account(long id, int balance) {
            this.id = id;
            this.balance = balance;
        }

        public long id() {
            return id;
        }

        public int balance() {
            synchronized (this) {
                return balance;
            }
        }
    }

    /**
     * Moves {@code amount} from {@code from} to {@code to} under both accounts' locks.
     * Calls {@code holdingFirst} once, while holding the first lock and before taking the second.
     */
    public static boolean transfer(Account from, Account to, int amount, Runnable holdingFirst) {
        throw new UnsupportedOperationException("write transfer");
    }
}
