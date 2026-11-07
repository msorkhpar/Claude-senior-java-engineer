package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tells the CI server's plug-ins about each finished build: the chat notifier, the dashboard,
 * the artifact cleaner. Plug-ins subscribe and unsubscribe at any time, including from inside
 * their own callback.
 */
public class BuildEvents {

    private final List<Consumer<String>> listeners = new ArrayList<>();

    public void subscribe(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<String> listener) {
        listeners.remove(listener);
    }

    public int listenerCount() {
        return listeners.size();
    }

    /** Tells every subscribed plug-in that the build {@code buildId} finished. */
    public void publish(String buildId) {
        for (Consumer<String> listener : listeners) {
            listener.accept(buildId);
        }
    }
}
