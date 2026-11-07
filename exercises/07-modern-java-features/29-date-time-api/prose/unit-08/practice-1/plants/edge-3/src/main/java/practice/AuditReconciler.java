package practice;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Compares the payment times the service recorded with the copies an audit library keeps. The
 * service hands the library each time as {@code Date.from(recorded)}, and the library returns
 * its copies as {@code java.util.Date}.
 */
public class AuditReconciler {

    /** The ids, in the order of {@code recorded}, whose audit copy is missing or was changed. */
    public List<String> changed(Map<String, Instant> recorded, Map<String, Date> audited) {
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, Instant> payment : recorded.entrySet()) {
            Date copy = audited.get(payment.getKey());
            if (copy == null || !matches(payment.getValue(), copy)) {
                changed.add(payment.getKey());
            }
        }
        return changed;
    }

    /** Whether {@code copy} is exactly what the audit library was given for {@code recorded}. */
    boolean matches(Instant recorded, Date copy) {
        return Math.abs(copy.getTime() - recorded.toEpochMilli()) <= 1;
    }

    /** The time the audit library holds after it was given {@code recorded}. */
    Instant asStored(Instant recorded) {
        return recorded.truncatedTo(ChronoUnit.MILLIS);
    }
}
