package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Price alerts for the trading screen. The market feed calls {@link #publish} on its own thread
 * for every tick; users add and remove alerts from the UI threads at any moment.
 */
public class AlertRegistry {

    /** One price update from the feed. */
    public record Tick(String symbol, long priceCents) {}

    // TODO: decide how the alerts are held.
    private final List<Consumer<Tick>> alerts = new ArrayList<>();

    /** Adds an alert; it gets every tick published from now on. Called from UI threads. */
    public void subscribe(Consumer<Tick> alert) {
        // TODO: add the alert, so that it can come and go while the feed publishes.
        throw new IllegalStateException("TODO: subscribe");
    }

    /** Removes an alert. Called from UI threads. */
    public void unsubscribe(Consumer<Tick> alert) {
        // TODO: remove the alert.
        throw new IllegalStateException("TODO: unsubscribe");
    }

    /** Hands the tick to every alert and returns how many alerts it reached. Called by the feed. */
    public int publish(Tick tick) {
        // TODO: hand the tick to every alert, whatever the UI threads do meanwhile.
        throw new IllegalStateException("TODO: publish");
    }
}
