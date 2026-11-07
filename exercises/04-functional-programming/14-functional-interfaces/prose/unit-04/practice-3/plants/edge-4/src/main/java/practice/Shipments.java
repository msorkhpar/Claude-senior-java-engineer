package practice;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The shipment desk's filters. Tracking references are pasted in by hand from carrier e-mails;
 * a shipment whose carrier is null has not been handed to a carrier yet.
 */
public class Shipments {

    public record Shipment(String trackingRef, String carrier) {}

    /** Two capital letters, nine digits, two capital letters, for example RR123456785FR. */
    static final Pattern TRACKING_REF = Pattern.compile("[A-Z]{2}\\d{9}[A-Z]{2}");

    /** Whether a pasted text is a tracking reference and nothing else. */
    static Predicate<String> validRef() {
        return TRACKING_REF.asMatchPredicate();
    }

    /** The shipments with this carrier; a null carrier selects the shipments not handed over yet. */
    static Predicate<Shipment> byCarrier(String carrier) {
        return shipment -> shipment.carrier().equals(carrier);
    }

    /** The pasted references the desk must send back for correction. */
    public static List<String> rejected(List<String> pasted) {
        return pasted.stream().filter(validRef().negate()).toList();
    }

    public static List<Shipment> withCarrier(List<Shipment> shipments, String carrier) {
        return shipments.stream().filter(byCarrier(carrier)).toList();
    }
}
