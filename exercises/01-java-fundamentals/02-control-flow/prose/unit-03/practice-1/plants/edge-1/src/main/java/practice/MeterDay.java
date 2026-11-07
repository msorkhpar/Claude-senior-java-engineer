package practice;

/**
 * Summarises one day of readings from one electricity meter, one reading per minute. The billing
 * job stores the summary; the support team reads the longest outage from it.
 */
public class MeterDay {

    /** A minute in which the meter sent nothing. */
    public static final int GAP = -1;

    /** The day's readings in watt-hours, in order; GAP where the meter sent nothing. */
    public interface Readings {
        int size();

        int get(int minute);
    }

    /** What billing stores: energy used, minutes missing, and the longest run of missing minutes. */
    public record Summary(long totalWh, int gaps, int longestOutage) {}

    /** The line the support dashboard shows for one meter. */
    public static String report(String meter, Readings readings) {
        Summary s = summarise(readings);
        return meter + ": " + s.totalWh() + " Wh, " + s.gaps() + " minutes missing, longest outage "
                + s.longestOutage() + " min";
    }

    /** The day's summary. */
    static Summary summarise(Readings readings) {
        long totalWh = 0;
        int gaps = 0;
        int outage = 0;
        int longestOutage = 0;
        int minute = 0;
        while (minute < readings.size()) {
            int wh = readings.get(minute);
            if (wh == GAP) {
                gaps++;
                outage++;
                longestOutage = Math.max(longestOutage, outage);
                continue;
            }
            outage = 0;
            totalWh += wh;
            minute++;
        }
        return new Summary(totalWh, gaps, longestOutage);
    }
}
