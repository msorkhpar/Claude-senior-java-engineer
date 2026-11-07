package practice;

import java.util.List;

/**
 * Base class of the router's request handlers. Each subclass says which paths it serves; the
 * router asks every handler for its routes when it registers it, and again on every reload.
 */
public abstract class Handler {

    private List<String> routes;

    /** The paths this handler serves. Subclasses build them from their own fields. */
    protected abstract List<String> paths();

    /** The routes the router registers for this handler. */
    public final List<String> routes() {
        // TODO: Return the routes of this handler, as its paths() describe them. Every handler
        //       must serve the paths its own subclass builds, and a reload must not make the
        //       subclass build them again.
        throw new IllegalStateException("TODO: routes");
    }
}
