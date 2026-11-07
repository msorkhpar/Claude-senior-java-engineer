package practice;

import java.util.List;

/**
 * Base class of the router's request handlers. Each subclass says which paths it serves; the
 * router asks every handler for its routes when it registers it, and again on every reload.
 */
public abstract class Handler {

    private final List<String> routes = List.copyOf(paths());

    /** The paths this handler serves. Subclasses build them from their own fields. */
    protected abstract List<String> paths();

    /** The routes the router registers for this handler. */
    public final List<String> routes() {
        return routes;
    }
}
