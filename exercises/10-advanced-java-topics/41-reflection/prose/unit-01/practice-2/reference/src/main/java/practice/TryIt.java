package practice;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) {
        // Turn the logger up, so the log lines of your code show under the printed lines.
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s %5$s%n");
        // Only this handler prints, and it leaves out the JDK's own detail.
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler before : root.getHandlers()) {
            root.removeHandler(before);
        }
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFilter(entry -> !String.valueOf(entry.getLoggerName()).matches("(java|javax|jdk|sun|com\\.sun)\\..*"));
        root.setLevel(Level.ALL);
        root.addHandler(handler);
        JDK_EXIT.setLevel(Level.OFF);

        // A stand-in for the collaborator, like the one the tests use for the first main case.
        try {
            System.out.println("columns: " + CsvExport.columns(Invoice.class));
            System.out.println(CsvExport.export(Invoice.class, List.of(new Invoice("INV-31", 4_200, "EUR"))).strip());
            System.out.println("record types: " + CsvExport.recordTypes(Payment.class).stream().map(Class::getSimpleName).sorted().toList());
            System.out.println(CsvExport.exportFamily(Payment.class, List.of(new Card("4111", 12), new Voucher("SPRING"))).strip());
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    record Invoice(String number, int cents, String currency) {}
    sealed interface Payment permits Card, Wallet {}
    record Card(String last4, int month) implements Payment {}
    sealed interface Wallet extends Payment permits Voucher, Points {}
    record Voucher(String code) implements Wallet {}
    record Points(String member, int amount) implements Wallet {}
}
