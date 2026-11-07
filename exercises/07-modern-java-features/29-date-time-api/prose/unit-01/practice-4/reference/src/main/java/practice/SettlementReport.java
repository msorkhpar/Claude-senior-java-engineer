package practice;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Totals card payments per business day of the exchange in Tokyo, whatever zone the server runs in. */
public class SettlementReport {

    static final ZoneId EXCHANGE = ZoneId.of("Asia/Tokyo");

    /** A payment as the legacy ledger API returns it. */
    public record Payment(Date at, long cents) {}

    /** Total cents per business day, earliest day first. */
    public static Map<LocalDate, Long> totals(List<Payment> payments) {
        Map<LocalDate, Long> out = new TreeMap<>();
        for (Payment p : payments) {
            out.merge(businessDay(p.at()), p.cents(), Long::sum);
        }
        return out;
    }

    // TODO: the exchange's business day of a payment. A moment falls on the exchange's day, and the
    // report must give the same answer on servers in any zone. Some callers pass the settlement day
    // they read with ResultSet.getDate, a java.sql.Date: that value already names the business day.
    static LocalDate businessDay(Date at) {
        if (at instanceof java.sql.Date day) {
            return day.toLocalDate();
        }
        return Instant.ofEpochMilli(at.getTime()).atZone(EXCHANGE).toLocalDate();
    }
}
