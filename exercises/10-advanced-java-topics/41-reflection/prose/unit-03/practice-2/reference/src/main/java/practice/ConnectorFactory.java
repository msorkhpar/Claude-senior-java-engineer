package practice;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts the integration connectors (warehouse, billing, shipping, ...) when the order service
 * boots. Each connector's constructor opens its connection, and may fail when the other side is
 * down.
 */
public class ConnectorFactory {

    /** What every connector class implements. */
    public interface Connector {
        String name();
    }

    /** A connector that could not be started. */
    public static class ConnectorException extends Exception {
        public ConnectorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final List<String> skipped = new ArrayList<>();

    /** Starts every connector it can, skipping the ones that can't be started. */
    public List<Connector> createAll(List<Class<? extends Connector>> types) {
        List<Connector> started = new ArrayList<>();
        for (Class<? extends Connector> type : types) {
            try {
                started.add(create(type));
            } catch (ConnectorException e) {
                skipped.add(type.getSimpleName() + ": " + e.getCause());
            }
        }
        return started;
    }

    /** One line per connector that createAll skipped, with the reason. */
    public List<String> skipped() {
        return List.copyOf(skipped);
    }

    public Connector create(Class<? extends Connector> type) throws ConnectorException {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException unchecked) throw unchecked;
            if (cause instanceof Error error) throw error;
            throw new ConnectorException(type.getSimpleName() + " could not start", cause);
        } catch (ReflectiveOperationException e) {
            throw new ConnectorException("cannot create " + type.getName(), e);
        }
    }
}
