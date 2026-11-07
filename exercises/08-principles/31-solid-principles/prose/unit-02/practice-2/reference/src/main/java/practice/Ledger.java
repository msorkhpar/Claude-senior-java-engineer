package practice;

import java.util.List;

public final class Ledger {

    private Ledger() {
    }

    /** One line of an account's ledger. Amounts are positive cents. */
    public sealed interface Entry permits Deposit, Withdrawal, Fee {
    }

    public record Deposit(long cents) implements Entry {
    }

    public record Withdrawal(long cents) implements Entry {
    }

    public record Fee(long cents, String reason) implements Entry {
    }

    /** The account's balance after every entry, starting from zero. */
    static long balanceCents(List<? extends Entry> entries) {
        long balance = 0;
        for (Entry e : entries) {
            balance += switch (e) {
                case Deposit d -> d.cents();
                case Withdrawal w -> -w.cents();
                case Fee f -> -f.cents();
            };
        }
        return balance;
    }

    /** The entry as the monthly statement prints it: "+12.00 deposit", "-3.50 fee: card replacement". */
    static String statementLine(Entry e) {
        return switch (e) {
            case Deposit d -> money('+', d.cents()) + " deposit";
            case Withdrawal w -> money('-', w.cents()) + " withdrawal";
            case Fee f -> money('-', f.cents()) + " fee: " + f.reason();
        };
    }

    private static String money(char sign, long cents) {
        return sign + String.format("%d.%02d", cents / 100, cents % 100);
    }
}
