package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The push tokens registered for one user's devices. Request threads register tokens, revoke
 * them, and rotate them when a device's token is renewed; the sender takes a snapshot for each
 * notification.
 */
public class DeviceTokens {

    private final Object lock = new Object();
    private final List<String> tokens;

    public DeviceTokens() {
        this(Collections.synchronizedList(new ArrayList<>()));
    }

    /** The store keeps this list to itself. The tests use it to watch what happens inside. */
    DeviceTokens(List<String> store) {
        this.tokens = store;
    }

    /**
     * Registers the token unless it is already registered. Returns whether it was added.
     * However many threads register the same token at once, it ends up in the store once.
     */
    public boolean register(String token) {
        synchronized (lock) {
            if (tokens.contains(token)) return false;
            tokens.add(token);
            return true;
        }
    }

    /**
     * A device's token was renewed: replaces oldToken by newToken, only if oldToken is registered
     * and newToken isn't. Returns whether it replaced anything; when it returns false the store
     * is unchanged. No other operation of this class ever sees the store halfway through.
     */
    public boolean rotate(String oldToken, String newToken) {
        if (!revoke(oldToken)) return false;
        return register(newToken);
    }

    public boolean revoke(String token) {
        synchronized (lock) {
            return tokens.remove(token);
        }
    }

    public List<String> snapshot() {
        synchronized (lock) {
            return List.copyOf(tokens);
        }
    }
}
