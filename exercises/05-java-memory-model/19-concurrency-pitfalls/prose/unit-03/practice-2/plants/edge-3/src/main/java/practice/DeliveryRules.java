package practice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
            Set<String> blocked = new HashSet<>(current.blockedDomains);
            blocked.add(domain);
            current = new Rules(blocked, current.maxSizeKb);
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

    /** An outgoing message: an id, the recipient's domain and the size in KB. */
    public record Message(String id, String domain, int sizeKb) {}

    /** Hands a message to the mail transfer agent. */
    public interface Transport {
        void send(Message message);
    }

    /**
     * One send thread's loop over a batch of queued messages. In production the rules come from
     * {@code store::current}.
     */
    public static final class Sender {
        private final Supplier<Rules> rules;
        private final Transport transport;

        public Sender(Supplier<Rules> rules, Transport transport) {
            this.rules = rules;
            this.transport = transport;
        }

        /** Sends, in order, each message the rules allow, and returns how many it sent. */
        public int sendAll(List<Message> batch) {
            int sent = 0;
            for (Message m : batch) {
                if (rules.get().isBlocked(m.domain()) || m.sizeKb() > rules.get().maxSizeKb(m.domain())) continue;
                transport.send(m);
                sent++;
            }
            return sent;
        }
    }
}
