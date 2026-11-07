package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Price alerts for the trading screen. The market feed calls {@link #publish} on its own thread
 * for every tick; users add and remove alerts from the UI threads at any moment.
 */
public class AlertRegistry {

    /** One price update from the feed. */
    public record Tick(String symbol, long priceCents) {}

    // TODO: decide how the alerts are held.
    private final List<Consumer<Tick>> alerts = new CopyOnWriteArrayList<>();

    /** Adds an alert; it gets every tick published from now on. Called from UI threads. */
    public void subscribe(Consumer<Tick> alert) {
        alerts.add(alert);
    }

    /** Removes an alert. Called from UI threads. */
    public void unsubscribe(Consumer<Tick> alert) {
        alerts.remove(alert);
    }

    /** Hands the tick to every alert and returns how many alerts it reached. Called by the feed. */
    public int publish(Tick tick) {
        int reached = 0;
        for (Consumer<Tick> alert : alerts) {
            alert.accept(tick);
            reached++;
        }
        return reached;
    }
}
