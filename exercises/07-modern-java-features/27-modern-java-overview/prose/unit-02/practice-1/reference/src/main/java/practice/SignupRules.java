package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        return username != null && reserved.contains(username);
    }

    /**
     * The blocked domains for the nightly audit export. The export takes its time writing them
     * out while new blocks keep arriving, and it must write the list as it was when it asked.
     */
    synchronized List<String> blockedDomains() {
        return List.copyOf(blockedDomains);
    }

    /**
     * The limits as one "name=value" line per limit, joined with "\n", for the status page.
     * Monitoring compares the page after every restart with the one before it.
     */
    static String limitsReport(Map<String, Integer> limits) {
        return new TreeMap<>(limits).entrySet().stream()
                .map(limit -> limit.getKey() + "=" + limit.getValue())
                .collect(Collectors.joining("\n"));
    }
}
