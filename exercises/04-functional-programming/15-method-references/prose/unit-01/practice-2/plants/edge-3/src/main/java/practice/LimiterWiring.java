package practice;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Wires an API gateway's rate limiter and its nightly export to the gateway's settings. */
public class LimiterWiring {

    public record Settings(int requestsPerSecond, String region) {}

    /** The settings in force. An operator's reload replaces them with a new Settings object. */
    public static final class SettingsHolder {
        private volatile Settings current;

        public Settings current() {
            return current;
        }

        public void reload(Settings next) {
            current = next;
        }
    }

    /** Admits at most limit.getAsInt() requests per one-second window. */
    public static final class RateLimiter {
        private final IntSupplier limit;
        private int admittedThisWindow;

        RateLimiter(IntSupplier limit) {
            this.limit = limit;
        }

        public synchronized boolean tryAdmit() {
            if (admittedThisWindow >= limit.getAsInt()) return false;
            admittedThisWindow++;
            return true;
        }

        public synchronized void nextWindow() {
            admittedThisWindow = 0;
        }
    }

    /** Built once, when the gateway starts; possibly before the first settings are loaded. */
    public static RateLimiter limiter(SettingsHolder holder) {
        return new RateLimiter(liveLimit(holder));
    }

    /** Built when an export run starts; the run calls the supplier for every file header. */
    public static Supplier<String> exportHeaderRegion(SettingsHolder holder) {
        return regionAtStart(holder);
    }

    // The limit the rate limiter checks before every request. The limiter is built once, at
    // startup, sometimes before the first settings are loaded, and must keep up with every
    // reload after that.
    static IntSupplier liveLimit(SettingsHolder holder) {
        return () -> holder.current().requestsPerSecond();
    }

    // The region an export run writes into each file header. Every header of one run must name
    // the region that was in force when the run started, even if an operator reloads mid-run.
    static Supplier<String> regionAtStart(SettingsHolder holder) {
        return () -> holder.current().region();
    }
}
