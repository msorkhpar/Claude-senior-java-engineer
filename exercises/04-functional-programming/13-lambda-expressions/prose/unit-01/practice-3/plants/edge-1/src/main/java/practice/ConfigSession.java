package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * One user session of the admin console. While it is open it hears about every configuration
 * change, so its screens can refresh; a closed session must hear nothing and leave no trace in
 * the watcher, which lives as long as the server.
 */
public class ConfigSession {

    /** The server-wide watcher. Given: don't change it. */
    public static final class ConfigWatcher {
        private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

        public void subscribe(Consumer<String> listener) {
            listeners.add(listener);
        }

        public void unsubscribe(Consumer<String> listener) {
            listeners.remove(listener);
        }

        public void changed(String key) {
            for (Consumer<String> listener : listeners) {
                listener.accept(key);
            }
        }

        public int listenerCount() {
            return listeners.size();
        }
    }

    private final ConfigWatcher watcher;
    private final List<String> heard = new ArrayList<>();

    public ConfigSession(ConfigWatcher watcher) {
        this.watcher = watcher;
    }

    void onChange(String key) {
        heard.add(key);
    }

    /** The keys this session has heard about, in order. */
    public List<String> heard() {
        return heard;
    }

    /** From now on, this session hears about every change. */
    public void open() {
        watcher.subscribe(key -> onChange(key));
    }

    /** From now on, this session hears nothing, and the watcher holds nothing of it. */
    public void close() {
        watcher.unsubscribe(key -> onChange(key));
    }
}
