package practice;

import java.util.Map;

/**
 * Fills the support team's message templates. A template names a value as {name}; the names
 * are chosen by the team and may contain dots, such as {invoice.no}.
 */
public class MessageTemplate {

    private final String template;

    public MessageTemplate(String template) {
        this.template = template;
    }

    /** The template with every {name} replaced by its value; names without a value stay as they are. */
    public String render(Map<String, String> values) {
        // TODO: Fill each value of the map into the template, using fill(...).
        throw new IllegalStateException("TODO: render");
    }

    /** {@code text} with every {name} replaced by {@code value}. */
    static String fill(String text, String name, String value) {
        // TODO: Put the value in place of every {name} in the text. A value is shown exactly as
        //       the customer's data holds it.
        throw new IllegalStateException("TODO: fill");
    }
}
