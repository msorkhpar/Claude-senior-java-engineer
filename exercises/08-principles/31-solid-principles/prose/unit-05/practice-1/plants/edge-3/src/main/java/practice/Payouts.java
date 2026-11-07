package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Pays approved insurance claims and keeps an audit line for every decision. */
public final class Payouts {

    private Payouts() {
    }

    /** One claim, as the claims team approved or declined it. */
    public record Claim(String id, String account, long cents, boolean approved) {
    }

    /** Moves money to an account and returns the bank's reference for the transfer. */
    public interface Transfers {
        String send(String account, long cents);
    }

    /** Keeps one line per payout decision, for the auditors. */
    public interface AuditTrail {
        void record(String line);
    }

    /** The production transfers: the bank's API, at an address taken from the environment. */
    public static final class BankTransfers implements Transfers {
        private final String url;

        public BankTransfers() {
            url = System.getenv("BANK_URL");
            if (url == null) throw new IllegalStateException("BANK_URL is not set");
        }

        public String send(String account, long cents) {
            throw new UnsupportedOperationException("would POST to " + url);
        }
    }

    /**
     * How older parts of the service find shared objects: a global map filled in at start-up.
     * Being retired; nothing new should look things up here.
     */
    public static final class Registry {
        private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

        private Registry() {
        }

        public static <T> void put(Class<T> type, T service) {
            services.put(type, service);
        }

        public static <T> T get(Class<T> type) {
            Object found = services.get(type);
            if (found == null) throw new IllegalStateException("nothing registered for " + type.getSimpleName());
            return type.cast(found);
        }
    }

    /** The payout rules. */
    public static final class PayoutPolicy {
        private final Transfers transfers;
        private final AuditTrail audit;

        public PayoutPolicy(Transfers transfers, AuditTrail audit) {
            this.transfers = Objects.requireNonNull(transfers, "transfers");
            this.audit = audit;
        }

        /** Pays every approved claim with a positive amount, and returns the bank references in order. */
        public List<String> pay(List<Claim> claims) {
            List<String> references = new ArrayList<>();
            for (Claim claim : claims) {
                if (claim.approved() && claim.cents() > 0) {
                    references.add(payOne(claim));
                } else {
                    skip(claim);
                }
            }
            return references;
        }

        /** Pays one claim and records "paid <claim id> <reference>". Returns the reference. */
        String payOne(Claim claim) {
            String reference = transfers.send(claim.account(), claim.cents());
            audit.record("paid " + claim.id() + " " + reference);
            return reference;
        }

        /** Records "skipped <claim id>" for a claim that is not paid. */
        void skip(Claim claim) {
            audit.record("skipped " + claim.id());
        }
    }
}
