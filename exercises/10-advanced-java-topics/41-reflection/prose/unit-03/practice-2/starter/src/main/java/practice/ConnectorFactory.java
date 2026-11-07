package practice;

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

    /**
     * A new connector of {@code type}, made with its no-argument constructor.
     */
    public Connector create(Class<? extends Connector> type) throws ConnectorException {
        // TODO: A connector whose constructor fails because the other side is unreachable
        //       (any checked exception) is reported as a ConnectorException that carries the
        //       constructor's own exception as its cause, so createAll can skip it and log why.
        //       A bug in a constructor (an unchecked exception or an Error) is not "unreachable":
        //       it reaches the caller as it was thrown. A class that can't be instantiated at all
        //       is a ConnectorException too.
        throw new IllegalStateException("TODO: create");
    }
}
