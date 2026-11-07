package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

/**
 * The invoicing module: its annotations, the code that carries them, and the cache settings
 * the cache layer reads from them while the service runs.
 */
public class Invoicing {

    /** A note for the people reviewing the code: why something unusual is the way it is. */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface ReviewNote {
        String value();
    }

    /**
     * Marks classes written by the mapper generator. The build's coverage tool reads the
     * compiled .class files (never the sources) and leaves out every class marked this way.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    @interface Generated {
        String by();
    }

    /** How long the cache layer keeps a method's result, in seconds. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Cached {
        int seconds();
    }

    static class InvoiceService {

        @Cached(seconds = 300)
        @ReviewNote("totals skip rounding for partner 12 until the ledger fix ships")
        String totals(String account) {
            return "totals for " + account;
        }

        @Cached(seconds = 45)
        String history(String account) {
            return "history of " + account;
        }

        String send(String account) {
            return "sent to " + account;
        }
    }

    @Generated(by = "mapper-gen 2.3")
    static class InvoiceMapper {
        String toCsv(String account, long cents) {
            return account + "," + cents;
        }
    }

    /**
     * The cache settings of a class: for every method the class declares with @Cached, its
     * name mapped to the number of seconds, sorted by name.
     */
    static Map<String, Integer> cacheSeconds(Class<?> type) {
        Map<String, Integer> seconds = new TreeMap<>();
        for (Method m : type.getDeclaredMethods()) {
            Cached cached = m.getAnnotation(Cached.class);
            if (cached != null) seconds.put(m.getName(), cached.seconds());
        }
        return seconds;
    }
}
