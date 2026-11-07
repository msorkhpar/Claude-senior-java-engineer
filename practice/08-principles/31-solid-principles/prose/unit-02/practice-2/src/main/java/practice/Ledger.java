package practice;

import java.util.List;

public final class Ledger {

    private Ledger() {
    }

    /** One line of an account's ledger. Amounts are positive cents. */
    public interface Entry {
    }

    public record Deposit(long cents) implements Entry {
    }

    public record Withdrawal(long cents) implements Entry {
    }

    public record Fee(long cents, String reason) implements Entry {
    }

    /** The account's balance after every entry, starting from zero. */
    static long balanceCents(List<? extends Entry> entries) {
        throw new IllegalStateException("TODO: balance after every entry");
    }

    /** The entry as the monthly statement prints it: "+12.00 deposit", "-3.50 fee: card replacement". */
    static String statementLine(Entry e) {
        throw new IllegalStateException("TODO: the statement's line for one entry");
    }

    private static String money(char sign, long cents) {
        return sign + String.format("%d.%02d", cents / 100, cents % 100);
    }
}
