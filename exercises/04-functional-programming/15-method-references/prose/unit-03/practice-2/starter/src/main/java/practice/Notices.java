package practice;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Turns operations events into notice texts. Each event names a template and carries one payload:
 * a single value, an Object[] of values from the JSON decoder, or an int[] from the metrics feed.
 * The notifier is configured with a renderer of type BiFunction<String, Object, String>.
 */
public class Notices {

    private final BiFunction<String, Object, String> renderer = Notices::render;

    /** The text of the notice for one event. */
    public String notice(String template, Object payload) {
        return renderer.apply(template, payload);
    }

    /** Fills the template's format specifiers, in order, with the payload's values. */
    static String render(String template, Object payload) {
        // TODO: The notifier used String::format as its renderer, and events that carry several
        //       values failed. Each value of the payload fills the next specifier, whether the
        //       payload is one value, an array of objects, or an array of ints.
        throw new IllegalStateException("TODO: render");
    }
}
