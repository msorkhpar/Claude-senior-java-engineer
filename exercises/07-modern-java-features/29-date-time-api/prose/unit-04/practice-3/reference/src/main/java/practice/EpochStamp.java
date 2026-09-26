package practice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public final class EpochStamp {

    private EpochStamp() {
    }

    /** The instant as decimal seconds since the epoch, with nine decimal places. */
    public static String encode(Instant instant) {
        // seconds plus a never-negative nano part: -1 s + 0.5 s is -0.5 s
        return BigDecimal.valueOf(instant.getEpochSecond())
                .add(BigDecimal.valueOf(instant.getNano(), 9))
                .toPlainString();
    }

    /** The instant a stamp names. */
    public static Instant decode(String stamp) {
        BigDecimal value = new BigDecimal(stamp);
        BigDecimal seconds = value.setScale(0, RoundingMode.FLOOR);
        long nanos = value.subtract(seconds).movePointRight(9).longValueExact();
        return Instant.ofEpochSecond(seconds.longValueExact(), nanos);
    }
}
