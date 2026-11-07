package practice;

/** Prices for the subscription shop: the tariffs customers are on, and the VAT they pay. */
public final class Pricing {

    private Pricing() {
    }

    /** The VAT percentage configured for this deployment. */
    static int configuredVat() {
        return Integer.parseInt(System.getProperty("pricing.vat", "20"));
    }

    /** A tariff: its name and the VAT percentage it charges. */
    public static final class Tariff {

        /** The tariff most customers are on, built once when the class is first used. */
        public static final Tariff STANDARD = new Tariff("standard", Rates.vatPercent());

        private final String name;
        private final int vatPercent;

        Tariff(String name, int vatPercent) {
            this.name = name;
            this.vatPercent = vatPercent;
        }

        public String name() {
            return name;
        }

        public int vatPercent() {
            return vatPercent;
        }

        /** The price a customer pays for a net price, both in cents. */
        public long grossCents(long netCents) {
            return netCents + netCents * vatPercent / 100;
        }
    }

    /** The rates the rest of the shop reads. */
    public static final class Rates {

        private static final int VAT_PERCENT = configuredVat();
        private static final Tariff DEFAULT_TARIFF = Tariff.STANDARD;

        /** The VAT percentage every price in this deployment uses. */
        public static int vatPercent() {
            return VAT_PERCENT;
        }

        /** The tariff a new customer starts on: the standard tariff. */
        public static Tariff defaultTariff() {
            return DEFAULT_TARIFF;
        }
    }
}
