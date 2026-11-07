package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The rules the sign-up endpoint checks before it creates an account. Domains are blocked while
 * the service runs; the audit export and the status page read the rules from other threads.
 */
public class SignupRules {

    private final Set<String> reserved;
    private final List<String> blockedDomains = new ArrayList<>();

    /** @param reserved user names no customer may register, built once at startup */
    public SignupRules(Set<String> reserved) {
        this.reserved = Set.copyOf(reserved);
    }

    public synchronized void block(String domain) {
        blockedDomains.add(domain);
    }

    public synchronized boolean isBlocked(String domain) {
        return blockedDomains.contains(domain);
    }

    /**
     * True when no customer may register {@code username}. The sign-up form may arrive without
     * the field, and then {@code username} is null.
     */
    boolean isReserved(String username) {
        // TODO: Answer from the reserved names given at startup. A request without the field
        //       is not a reserved name; it is rejected later, by the form validation.
        throw new IllegalStateException("TODO: isReserved");
    }

    /**
     * The blocked domains for the nightly audit export. The export takes its time writing them
     * out while new blocks keep arriving, and it must write the list as it was when it asked.
     */
    synchronized List<String> blockedDomains() {
        // TODO: Hand the export the blocked domains, in the order they were blocked. Blocks made
        //       after this call must not show up in what the export writes, and the export must
        //       not be able to change the rules through what it gets.
        throw new IllegalStateException("TODO: blockedDomains");
    }

    /**
     * The limits as one "name=value" line per limit, joined with "\n", for the status page.
     * Monitoring compares the page after every restart with the one before it.
     */
    static String limitsReport(Map<String, Integer> limits) {
        // TODO: One "name=value" line per limit, ordered by name, so that the same limits give
        //       the same page after every restart.
        throw new IllegalStateException("TODO: limitsReport");
    }
}
