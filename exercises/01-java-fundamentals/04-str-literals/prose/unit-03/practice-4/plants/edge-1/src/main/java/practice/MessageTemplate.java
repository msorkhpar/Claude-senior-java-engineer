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
        String text = template;
        for (Map.Entry<String, String> value : values.entrySet()) {
            text = fill(text, value.getKey(), value.getValue());
        }
        return text;
    }

    /** {@code text} with every {name} replaced by {@code value}. */
    static String fill(String text, String name, String value) {
        return text.replaceAll("\\{" + java.util.regex.Pattern.quote(name) + "\\}", value);
    }
}
