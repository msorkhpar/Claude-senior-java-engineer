package practice;

import java.util.List;

/**
 * Splits a settings file for the admin console. A file opens with a block of comment lines that
 * describes it; the console shows that block as the file's description. Everything after the
 * block is the settings section, which the console rewrites line for line when a value changes,
 * comments and commented-out settings included.
 */
public class SettingsFile {

    /** Whether a line is a comment. */
    static boolean comment(String line) {
        return line.startsWith("#");
    }

    /** The comment block at the top of the file. */
    public static List<String> description(List<String> lines) {
        // TODO: Return the comment block the file opens with.
        throw new IllegalStateException("TODO: description");
    }

    /** Every line after the description, in file order. */
    public static List<String> settings(List<String> lines) {
        // TODO: Return every line after the opening comment block, in file order.
        throw new IllegalStateException("TODO: settings");
    }
}
