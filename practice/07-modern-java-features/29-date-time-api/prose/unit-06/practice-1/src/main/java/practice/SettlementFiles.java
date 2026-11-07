package practice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Names the payment files the nightly settlement job uploads to the bank. The bank's gateway
 * files each upload under the date and time in its name.
 */
public class SettlementFiles {

    /** The name of the file for a run whose cut-off is {@code cutOff}. */
    public static String fileName(LocalDateTime cutOff) {
        return "payments_" + stamp(cutOff) + ".txt";
    }

    /**
     * The part of the name between "payments_" and ".txt": the cut-off's date and its time to the
     * minute, as the bank's specification describes them.
     */
    static String stamp(LocalDateTime cutOff) {
        // TODO: return the stamp the bank's specification asks for (see the statement).
        throw new IllegalStateException("TODO: stamp(cutOff)");
    }
}
