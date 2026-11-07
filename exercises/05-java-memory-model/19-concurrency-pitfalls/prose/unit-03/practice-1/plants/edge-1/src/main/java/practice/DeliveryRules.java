package practice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The rules the mail service's send threads apply to every outgoing message, and the store the
 * admin API changes them through.
 */
public class DeliveryRules {

    /** One version of the rules: which recipient domains are blocked, and each domain's size limit. */
    public static final class Rules {
        static final int DEFAULT_MAX_SIZE_KB = 10_240;

        private final Set<String> blockedDomains;
        private final Map<String, Integer> maxSizeKb;

        Rules(Set<String> blockedDomains, Map<String, Integer> maxSizeKb) {
            this.blockedDomains = blockedDomains;
            this.maxSizeKb = maxSizeKb;
        }

        public boolean isBlocked(String domain) {
            return blockedDomains.contains(domain);
        }

        public int maxSizeKb(String domain) {
            return maxSizeKb.getOrDefault(domain, DEFAULT_MAX_SIZE_KB);
        }
    }

    /**
     * Holds the rules in force. Send threads call current() for every message; the admin API calls
     * the other methods, from one thread at a time.
     */
    public static final class Store {
        private volatile Rules current = new Rules(new HashSet<>(), new HashMap<>());

        public Rules current() {
            return current;
        }

        /** Stops all mail to the domain. */
        public void block(String domain) {
            current.blockedDomains.add(domain);           // the field is volatile, so this is seen
        }

        /** Lets mail to the domain through again. */
        public void unblock(String domain) {
            Set<String> blocked = new HashSet<>(current.blockedDomains);
            blocked.remove(domain);
            current = new Rules(blocked, current.maxSizeKb);
        }

        /** Sets the largest message, in KB, that may be sent to the domain. */
        public void setMaxSizeKb(String domain, int kb) {
            Map<String, Integer> limits = new HashMap<>(current.maxSizeKb);
            limits.put(domain, kb);
            current = new Rules(current.blockedDomains, limits);
        }
    }
}
