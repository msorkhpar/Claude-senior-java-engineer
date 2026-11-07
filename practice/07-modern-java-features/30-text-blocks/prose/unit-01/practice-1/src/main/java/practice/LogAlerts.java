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
        // TODO: A line counts when it holds ERROR as a word of its own: "03:10 ERROR timeout"
        //       does, while "ERRORS=0" and "NOERROR" don't. Return the pattern that tells them apart.
        throw new IllegalStateException("TODO: errorWord");
    }

    /** The share that holds {@code host}'s nightly logs. */
    static String shareFor(String host) {
        // TODO: For host web-07 the share is the UNC path \\web-07\logs\nightly: two backslashes,
        //       the host, then the folders logs and nightly, each after one backslash.
        throw new IllegalStateException("TODO: shareFor");
    }
}
