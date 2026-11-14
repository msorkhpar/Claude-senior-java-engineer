package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Run executes this file. Change the calls in main and see what prints. Submit grades. */
public class TryIt {
    /** Held here, so the JDK's own exit notice stays off: a logger nothing holds can be dropped. */
    private static final Logger JDK_EXIT = Logger.getLogger("java.lang.Runtime");

    public static void main(String[] args) throws Exception {
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
            Map<String, Long> catalog = new LinkedHashMap<>();
            PriceImport job = new PriceImport(() -> { }, catalog::put);
            PriceImport.Result result = job.importAll(List.of(row("price_cents", "OIL-01", "2450"),
                    row("price_cents", "OIL-02", "19.90"), row("price_cents", "VIN-07", " 730 "),
                    row("price_cents", "VIN-08", "free")));
            System.out.println("imported: " + result.imported() + ", rejected: " + result.rejected());
            System.out.println("catalog: " + catalog);
            try {
                job.importAll(List.of(row("cost", "OIL-03", "1100")));
                System.out.println("file with a renamed column: imported");
            } catch (RuntimeException failed) {
                System.out.println("file with a renamed column: import failed, " + failed.getClass().getSimpleName());
            }
            int[] calls = {0};
            PriceImport interrupted = new PriceImport(() -> {
                if (++calls[0] == 2) {
                    throw new InterruptedException("deploy");
                }
            }, catalog::put);
            try {
                interrupted.importAll(List.of(row("price_cents", "SALT-1", "300"), row("price_cents", "SALT-2", "320"),
                        row("price_cents", "SALT-3", "340")));
                System.out.println("interrupted import: finished");
            } catch (InterruptedException stoppedByDeploy) {
                System.out.println("interrupted import: ended with InterruptedException, " + stoppedByDeploy.getMessage());
            }
        } catch (RuntimeException stopped) {
            // A method you have not finished, or a bug, stops here with its message, not a build error.
            System.out.println("Run stopped: " + stopped);
        }
    }

    private static Map<String, String> row(String priceColumn, String sku, String cents) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("sku", sku);
        row.put(priceColumn, cents);
        return row;
    }
}
