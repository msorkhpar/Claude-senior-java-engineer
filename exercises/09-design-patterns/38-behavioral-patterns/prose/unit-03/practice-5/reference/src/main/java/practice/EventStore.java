package practice;

import java.util.ArrayList;
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

    private final Account account;
    private final List<AccountCommand> log = new ArrayList<>();

    public EventStore(Account account) {
        this.account = account;
    }

    public void append(AccountCommand command) {
        command.applyTo(account);
        log.add(command);
    }

    public Account replay() {
        Account rebuilt = new Account();
        for (AccountCommand command : log) {
            command.applyTo(rebuilt);
        }
        return rebuilt;
    }

    public List<AccountCommand> eventsSince(long timestamp) {
        return log.stream().filter(c -> c.timestamp() > timestamp).toList();
    }

    public List<AccountCommand> events() {
        return List.copyOf(log);
    }
}
