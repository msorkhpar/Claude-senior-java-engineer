package practice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public final class EpochStamp {

    private EpochStamp() {
    }

    public static String encode(Instant instant) {
        // the nano count written as it is, never padded to nine digits
        return instant.getEpochSecond() + "." + instant.getNano();
    }

    public static Instant decode(String stamp) {
        BigDecimal value = new BigDecimal(stamp);
        BigDecimal seconds = value.setScale(0, RoundingMode.FLOOR);
        long nanos = value.subtract(seconds).movePointRight(9).longValueExact();
        return Instant.ofEpochSecond(seconds.longValueExact(), nanos);
    }
}
