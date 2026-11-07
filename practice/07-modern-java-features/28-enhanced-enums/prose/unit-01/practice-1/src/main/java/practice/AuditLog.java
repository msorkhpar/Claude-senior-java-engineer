package practice;

/**
 * Writes one audit line for each field of an order that changed. The line names the type of the
 * new value, and when the old and the new value are not of the same type, it says so instead.
 */
public class AuditLog {

    /** How an order reached us. */
    public enum Channel { WEB, PHONE, PARTNER }

    /** The discount on an order. Two of them compute their amount in a body of their own. */
    public enum Discount {
        NONE,
        LOYALTY {
            @Override
            long off(long cents) { return cents / 20; }
        },
        VOUCHER {
            @Override
            long off(long cents) { return Math.min(cents, 1_000); }
        };

        long off(long cents) { return 0; }
    }

    /** An amount of money in cents. */
    public record Amount(long cents) { }

    /** The audit line for one changed field. */
    public static String line(String field, Object before, Object after) {
        if (!sameType(before, after)) {
            return field + ": type changed from " + typeName(before) + " to " + typeName(after);
        }
        return field + ": " + typeName(after) + " " + before + " -> " + after;
    }

    /** The name of the value's type as the audit log shows it, for example "Channel" or "Amount". */
    static String typeName(Object value) {
        // TODO: Every value of one enum must show that enum's name, whichever constant it is.
        //       Values that aren't enum constants show the simple name of their class.
        throw new IllegalStateException("TODO: typeName");
    }

    /** Whether two values are of the same type, so that a change between them is an ordinary edit. */
    static boolean sameType(Object a, Object b) {
        // TODO: Two constants of one enum are the same type, whichever constants they are; constants
        //       of two different enums are not. Two records of one record class are the same type.
        throw new IllegalStateException("TODO: sameType");
    }
}
