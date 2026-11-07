package practice;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A request header's name as the gateway received it. The gateway keeps a request's headers in a
 * HashMap keyed by HeaderName, and lists them sorted in its debug log. Two names are the same
 * header only when they are spelled exactly alike.
 */
public final class HeaderName implements Comparable<HeaderName> {

    /** How often equals() has run; the team added it while chasing the gateway's latency. */
    static final AtomicLong EQUALS_CALLS = new AtomicLong();

    private final String value;

    public HeaderName(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        EQUALS_CALLS.incrementAndGet();
        return o instanceof HeaderName other && other.value.equals(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    /** The order of names in the debug log. */
    @Override
    public int compareTo(HeaderName other) {
        return Integer.compare(hashCode(), other.hashCode());
    }
}
