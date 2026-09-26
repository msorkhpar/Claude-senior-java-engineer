package practice;

import java.util.List;

public final class EventStore {

    /** Given: the receiver. */
    public static final class Account {
        private long balance;

        public long balance() {
            return balance;
        }

        void deposit(long amount) {
            balance += amount;
        }

        void withdraw(long amount) {
            if (amount > balance) {
                throw new IllegalStateException("Insufficient funds: " + balance + " < " + amount);
            }
            balance -= amount;
        }
    }

    /** Given: a command with the time it was issued. */
    public sealed interface AccountCommand permits Deposit, Withdraw {
        void applyTo(Account account);

        long timestamp();
    }

    public record Deposit(long amount, long timestamp) implements AccountCommand {
        @Override
        public void applyTo(Account account) {
            account.deposit(amount);
        }
    }

    public record Withdraw(long amount, long timestamp) implements AccountCommand {
        @Override
        public void applyTo(Account account) {
            account.withdraw(amount);
        }
    }

    public EventStore(Account account) {
        throw new UnsupportedOperationException("TODO");
    }

    public void append(AccountCommand command) {
        throw new UnsupportedOperationException("TODO");
    }

    public Account replay() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<AccountCommand> eventsSince(long timestamp) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<AccountCommand> events() {
        throw new UnsupportedOperationException("TODO");
    }
}
