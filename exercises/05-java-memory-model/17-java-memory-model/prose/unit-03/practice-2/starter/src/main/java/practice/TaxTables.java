package practice;

import java.util.Map;

/**
 * VAT rates by country, fetched from the tax service the first time a checkout needs them. Every
 * checkout thread calls rates(), a few hundred thousand times a second at peak.
 */
public class TaxTables {

    /** Fetches the current tables. Takes seconds, and the tax service bills every call. */
    public interface Loader {
        Rates load();
    }

    /** The tables as fetched: a version and the rate per country, in basis points. */
    public static class Rates {
        String version;
        Map<String, Integer> basisPointsByCountry;

        public Rates(String version, Map<String, Integer> basisPointsByCountry) {
            this.version = version;
            this.basisPointsByCountry = basisPointsByCountry;
        }

        public String version() {
            return version;
        }

        public int basisPoints(String country) {
            return basisPointsByCountry.get(country);
        }
    }

    private final Object lock = new Object();
    private final Loader loader;
    // TODO: rates() reads this field on every call. Decide how it must be declared.
    private Rates rates;

    public TaxTables(Loader loader) {
        this.loader = loader;
    }

    /** The tables, fetched on the first call; later calls return the same tables. */
    public Rates rates() {
        // TODO: Fetch the tables on the first call and keep them. Two checkouts can make the
        //       first call at the same moment, and the tax service must be called once. After
        //       that, checkouts must not queue for anything: return the tables you kept.
        throw new IllegalStateException("TODO: rates");
    }
}
