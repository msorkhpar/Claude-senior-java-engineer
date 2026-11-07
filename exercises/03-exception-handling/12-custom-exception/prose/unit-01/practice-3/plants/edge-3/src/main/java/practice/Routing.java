package practice;

import java.util.List;

/**
 * Picks the route of a parcel: tries each rule in turn, and the first rule that applies decides.
 * A rule that doesn't apply says so by throwing NoRouteException, which route() catches before it
 * tries the next rule. Most parcels are refused by most rules.
 */
public class Routing {

    /** Thrown by a rule that doesn't apply to a parcel. Caught inside route(), never by its callers. */
    public static class NoRouteException extends RuntimeException {
        public NoRouteException(String rule, String parcelId) {
            super("rule " + rule + " does not apply to parcel " + parcelId, null, false, false);
        }
    }

    /** What route()'s callers receive when no rule applies. */
    public static class UnroutableParcelException extends RuntimeException {
        public UnroutableParcelException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** One routing rule: returns the hub for the parcel, or throws NoRouteException. */
    public interface Rule {
        String hubFor(String parcelId, String postcode);
    }

    private final List<Rule> rules;

    public Routing(List<Rule> rules) {
        this.rules = rules;
    }

    /** The hub the parcel goes to. */
    public String route(String parcelId, String postcode) {
        NoRouteException lastMiss = null;
        for (Rule rule : rules) {
            try {
                return rule.hubFor(parcelId, postcode);
            } catch (NoRouteException miss) {
                lastMiss = miss;
            }
        }
        throw unroutable(parcelId, lastMiss);
    }

    /** The failure route() reports when no rule applies; lastMiss is the last rule's refusal. */
    RuntimeException unroutable(String parcelId, NoRouteException lastMiss) {
        return new UnroutableParcelException("no rule routes parcel " + parcelId, null);
    }
}
