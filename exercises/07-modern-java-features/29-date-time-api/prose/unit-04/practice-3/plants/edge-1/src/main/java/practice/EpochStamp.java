package practice;

import java.math.BigDecimal;
import java.time.Instant;

public final class EpochStamp {

    private EpochStamp() {
    }

    public static String encode(Instant instant) {
        // through toEpochMilli, which drops everything below the millisecond
        return BigDecimal.valueOf(instant.toEpochMilli(), 3).setScale(9).toPlainString();
    }

    public static Instant decode(String stamp) {
        return Instant.ofEpochMilli(new BigDecimal(stamp).movePointRight(3).longValue());
    }
}
