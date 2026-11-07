package practice;

import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Formatting for the general ledger. Entry ids get the accounting period after them, and
 * amounts are shown on the review screen and written to the month-end CSV export.
 */
public class Ledger {

    /**
     * Returns the function the export applies to every entry id of one period: the id followed by
     * the period, so "JE-118" in period "-P11" becomes "JE-118-P11".
     */
    static Function<String, String> withPeriod(String period) {
        return id -> id.concat(period);
    }

    /** Shows an amount in cents as units and two decimals: 250075 is "2500.75". */
    public static class PlainFormat {
        public String render(long cents) {
            String sign = cents < 0 ? "-" : "";
            long abs = Math.abs(cents);
            return sign + abs / 100 + "." + String.format("%02d", abs % 100);
        }
    }

    /** The review screen's format: amounts at or above the limit carry a review flag. */
    public static class FlaggedFormat extends PlainFormat {
        private final long reviewLimitCents;

        public FlaggedFormat(long reviewLimitCents) {
            this.reviewLimitCents = reviewLimitCents;
        }

        @Override
        public String render(long cents) {
            String amount = super.render(cents);
            return Math.abs(cents) >= reviewLimitCents ? amount + " [review]" : amount;
        }

        /**
         * The function the CSV export uses for its amount column. The column holds the amount
         * exactly as PlainFormat shows it.
         */
        LongFunction<String> exportColumn() {
            return super::render;
        }

        /**
         * The function the review screen uses. It shows amounts the way this format renders them,
         * including any change a team makes by extending this format for its own screen.
         */
        LongFunction<String> screenColumn() {
            return this::render;
        }
    }
}
