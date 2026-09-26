package practice;

import java.util.List;

public class Ledger {

    public sealed interface Transaction permits Deposit, Withdrawal, Transfer {
    }

    public record Deposit(String account, long cents) implements Transaction {
    }

    public record Withdrawal(String account, long cents) implements Transaction {
    }

    public record Transfer(String from, String to, long cents) implements Transaction {
    }

    /** The balance of account after every transaction, starting from 0. */
    public static long balance(String account, List<Transaction> transactions) {
        throw new UnsupportedOperationException("write balance");
    }
}
