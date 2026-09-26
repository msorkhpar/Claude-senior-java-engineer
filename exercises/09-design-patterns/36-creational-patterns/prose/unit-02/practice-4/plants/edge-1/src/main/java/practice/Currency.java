package practice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class Currency {

    private static final Pattern CODE = Pattern.compile("[A-Z]{3}");
    private static final Map<String, Currency> CACHE = new java.util.IdentityHashMap<>();

    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    /** Returns the one cached Currency for a three-capital-letter code. */
    public static Currency of(String code) {
        if (code == null || !CODE.matcher(code).matches()) {
            throw new IllegalArgumentException("not a currency code: " + code);
        }
        return CACHE.computeIfAbsent(code, Currency::new);
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
