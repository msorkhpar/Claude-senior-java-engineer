package practice;

import java.time.Instant;

public final class EpochStamp {

    private EpochStamp() {
    }

    /** The instant as decimal seconds since the epoch, with nine decimal places. */
    public static String encode(Instant instant) {
        throw new UnsupportedOperationException("write encode");
    }

    /** The instant a stamp names. */
    public static Instant decode(String stamp) {
        throw new UnsupportedOperationException("write decode");
    }
}
