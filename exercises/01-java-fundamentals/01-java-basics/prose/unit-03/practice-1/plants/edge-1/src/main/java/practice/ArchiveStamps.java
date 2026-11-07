package practice;

import java.time.Instant;

/**
 * Writes the header line of a document in the legacy archive. The archive's format keeps the time
 * a document was saved as a signed 32-bit count of seconds since 1970-01-01T00:00:00Z.
 */
public class ArchiveStamps {

    /** The header line for a document saved at {@code savedAt}, such as {@code "inv-7@1710496800"}. */
    public String header(String documentId, Instant savedAt) {
        return documentId + "@" + toStampSeconds(savedAt.toEpochMilli());
    }

    /** The archive's stamp for a time given in milliseconds since 1970; always whole seconds here. */
    int toStampSeconds(long epochMillis) {
        // TODO: Return the number of seconds the archive stores for this time. A time the
        //       format can't hold must be refused with an ArithmeticException; it must never be
        //       written down as some other date.
        return (int) (epochMillis / 1000);
    }
}
