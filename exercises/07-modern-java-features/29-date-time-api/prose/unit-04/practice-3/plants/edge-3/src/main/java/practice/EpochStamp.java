package practice;

import java.time.Instant;
import java.util.Locale;

public final class EpochStamp {

    private EpochStamp() {
    }

    public static String encode(Instant instant) {
        // the two parts written side by side, as if the nano part carried the sign
        return String.format(Locale.ROOT, "%d.%09d", instant.getEpochSecond(), instant.getNano());
    }

    public static Instant decode(String stamp) {
        int dot = stamp.indexOf('.');
        return Instant.ofEpochSecond(Long.parseLong(stamp.substring(0, dot)), Long.parseLong(stamp.substring(dot + 1)));
    }
}
