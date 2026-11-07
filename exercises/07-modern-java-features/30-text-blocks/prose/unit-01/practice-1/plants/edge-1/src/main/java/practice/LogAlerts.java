package practice;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Picks the error lines out of an application log for the on-call alert, and says where each
 * host's nightly logs are archived on the Windows file server.
 */
public class LogAlerts {

    /** The lines of {@code log} that report an error, in their original order. */
    public static List<String> errorLines(List<String> log) {
        Pattern error = errorWord();
        return log.stream().filter(line -> error.matcher(line).find()).toList();
    }

    /** The pattern that finds the word ERROR standing on its own in a log line. */
    static Pattern errorWord() {
        return Pattern.compile("\bERROR\b");
    }

    /** The share that holds {@code host}'s nightly logs. */
    static String shareFor(String host) {
        return "\\\\" + host + "\\logs\\nightly";
    }
}
