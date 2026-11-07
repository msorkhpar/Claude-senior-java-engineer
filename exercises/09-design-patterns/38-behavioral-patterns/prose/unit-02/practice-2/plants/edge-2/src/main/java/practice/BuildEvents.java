package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Tells the CI server's plug-ins about each finished build: the chat notifier, the dashboard,
 * the artifact cleaner. Plug-ins subscribe and unsubscribe at any time, including from inside
 * their own callback. Plug-ins are written by other teams, and some of them throw.
 */
public class BuildEvents {

    private final List<Consumer<String>> listeners = new ArrayList<>();
    private final BiConsumer<String, RuntimeException> onPluginFailure;

    /** @param onPluginFailure told the build id and the exception whenever a plug-in fails */
    public BuildEvents(BiConsumer<String, RuntimeException> onPluginFailure) {
        this.onPluginFailure = onPluginFailure;
    }

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
        for (Consumer<String> listener : List.copyOf(listeners)) {
            deliver(listener, buildId);
        }
    }

    /** Hands one build to one plug-in. */
    void deliver(Consumer<String> listener, String buildId) {
        try {
            listener.accept(buildId);
        } catch (RuntimeException e) {
            // a plug-in's failure is the plug-in's problem
        }
    }
}
