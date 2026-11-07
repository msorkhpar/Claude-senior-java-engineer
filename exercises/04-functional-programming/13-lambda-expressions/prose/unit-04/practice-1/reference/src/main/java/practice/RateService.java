package practice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

/** Exchange rates, cached in memory in front of a rates API that bills every call. */
public class RateService {

    /** The remote rates API: slow, and billed per call. */
    public interface RateApi {
        BigDecimal fetch(String pair);
    }

    /** A debug log that may be switched off. */
    public interface DebugLog {
        /** Logs a message the caller has already built. */
        void debug(String message);

        /** Logs a message, asking the supplier for it only when debug output is on. */
        void debug(Supplier<String> message);
    }

    private final Map<String, BigDecimal> cache = new HashMap<>();
    private final RateApi api;
    private final DebugLog log;
    int snapshotsTaken;

    public RateService(RateApi api, DebugLog log) {
        this.api = api;
        this.log = log;
    }

    /** The rate for a currency pair such as "GBP/JPY". */
    public BigDecimal rate(String pair) {
        BigDecimal rate = cachedOrFetched(pair);
        trace(pair, rate);
        return rate;
    }

    Optional<BigDecimal> cached(String pair) {
        return Optional.ofNullable(cache.get(pair));
    }

    BigDecimal fetchAndCache(String pair) {
        BigDecimal rate = api.fetch(pair);
        cache.put(pair, rate);
        return rate;
    }

    /** A copy of the whole cache, sorted: useful when debugging, and not cheap. */
    String cacheSnapshot() {
        snapshotsTaken++;
        return new TreeMap<>(cache).toString();
    }

    /** The cached rate for the pair, or one fetched from the API (and cached) when there is none. */
    BigDecimal cachedOrFetched(String pair) {
        return cached(pair).orElseGet(() -> fetchAndCache(pair));
    }

    /** Writes a debug line: the pair, its rate and a snapshot of the cache. */
    void trace(String pair, BigDecimal rate) {
        log.debug(() -> pair + " = " + rate + ", cache " + cacheSnapshot());
    }
}
