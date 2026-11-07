package practice;

/**
 * The garage's tariffs. While a car is parked, the barrier app charges its visit in parts: each
 * time it settles a stretch of parked time it calls {@code tariff.charge(visit, minutes)} and books
 * the amount returned. A visit never pays more than its tariff's cap in total.
 */
public class Garage {

    /** One car's stay, from the moment it enters until it leaves. */
    public static final class Visit {
        private final String plate;
        private long chargedCents;

        public Visit(String plate) {
            this.plate = plate;
        }

        public String plate() {
            return plate;
        }
    }

    public enum Tariff {
        STANDARD(250, 1_800),
        RESIDENT(90, 600),
        EVENT(600, 2_400);

        private final long centsPerStartedHour;
        private final long capCents;

        Tariff(long centsPerStartedHour, long capCents) {
            this.centsPerStartedHour = centsPerStartedHour;
            this.capCents = capCents;
        }

        public long capCents() {
            return capCents;
        }

        /**
         * Charges {@code minutes} more of parked time to {@code visit} and returns what this part
         * costs: every started hour of the part at this tariff's rate, but no more than what is
         * left of the visit's cap.
         */
        public long charge(Visit visit, int minutes) {
            long startedHours = (minutes + 59) / 60;
            long amount = Math.min(startedHours * centsPerStartedHour, capCents - visit.chargedCents);
            visit.chargedCents += amount;
            return amount;
        }
    }
}
