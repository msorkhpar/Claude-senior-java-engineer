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

    public static long balance(String account, List<Transaction> transactions) {
        long total = 0;
        for (Transaction t : transactions) {
            total += effect(account, t);
        }
        return total;
    }

    private static long effect(String account, Transaction t) {
        return switch (t) {
            case Deposit d -> d.account().equals(account) ? d.cents() : 0;
            case Withdrawal w -> w.account().equals(account) ? -w.cents() : 0;
            case Transfer tr -> (tr.to().equals(account) ? tr.cents() : 0)
                    - (tr.from().equals(account) ? tr.cents() : 0);
        };
    }
}
