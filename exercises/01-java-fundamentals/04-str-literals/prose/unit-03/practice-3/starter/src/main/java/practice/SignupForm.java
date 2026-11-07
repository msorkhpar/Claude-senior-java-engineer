package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cleans the fields of a sign-up form before they are saved. Users type into the form, and paste
 * into it from web pages, word processors and spreadsheets.
 */
public class SignupForm {

    /** The cleaned fields by name, or an IllegalArgumentException naming the first required field that is missing. */
    public static Map<String, String> accept(Map<String, String> raw, List<String> required) {
        Map<String, String> cleaned = new LinkedHashMap<>();
        for (Map.Entry<String, String> field : raw.entrySet()) {
            cleaned.put(field.getKey(), clean(field.getValue()));
        }
        for (String name : required) {
            String value = raw.get(name);
            if (value == null || missing(value)) throw new IllegalArgumentException("missing: " + name);
        }
        return cleaned;
    }

    /** The value without the space-like characters around it; the ones inside it stay. */
    static String clean(String value) {
        // TODO: Remove the characters around the value that show as blank space, whatever the
        //       user typed or pasted; keep the ones between words.
        throw new IllegalStateException("TODO: clean");
    }

    /** Whether the value holds nothing but space-like characters. */
    static boolean missing(String value) {
        // TODO: A value that shows as nothing but blank space counts as missing.
        throw new IllegalStateException("TODO: missing");
    }
}
