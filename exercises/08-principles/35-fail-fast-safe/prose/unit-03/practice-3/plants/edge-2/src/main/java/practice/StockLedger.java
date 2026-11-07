package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Units of one product in each of a chain's warehouses. Transfers move units between warehouses
 * from several threads; the stock-take report lists every warehouse and the total.
 */
public class StockLedger {

    /** One line of the report. */
    public record Line(String warehouse, int units) {}

    private final Map<String, Integer> stock;

    public StockLedger(Map<String, Integer> opening) {
        stock = new HashMap<>(opening);
    }

    /** Moves units from one warehouse to another. Called by the transfer threads. */
    public synchronized void move(String from, String to, int units) {
        stock.merge(from, -units, Integer::sum);
        stock.merge(to, units, Integer::sum);
    }

    /** Hands one line per warehouse to the printer and returns the total of the lines. */
    public synchronized long report(Consumer<Line> printer) {
        long total = 0;
        for (Map.Entry<String, Integer> e : stock.entrySet()) {
            printer.accept(new Line(e.getKey(), e.getValue()));
            total += e.getValue();
        }
        return total;
    }
}
