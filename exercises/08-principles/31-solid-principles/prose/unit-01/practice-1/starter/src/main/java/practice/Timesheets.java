package practice;

import java.util.List;

/**
 * The warehouse's timesheets. Payroll pays from them, and floor planning reports from them how
 * long each worker was on the floor.
 */
public class Timesheets {

    /** One shift: minutes between clocking in and clocking out, and the break taken in between. */
    public record Shift(int onSiteMinutes, int breakMinutes) {}

    /** Minutes worked in a shift: time on site less the break. Until this month both teams used it as it is. */
    static int worked(Shift s) {
        return s.onSiteMinutes() - s.breakMinutes();
    }

    /** Payroll: what a worker is paid for a week of shifts. */
    public static class Payroll {
        private final long centsPerHour;

        public Payroll(long centsPerHour) {
            this.centsPerHour = centsPerHour;
        }

        /** Pay in cents for the given shifts. */
        public long pay(List<Shift> shifts) {
            long minutes = 0;
            for (Shift s : shifts) minutes += paidMinutes(s);
            return minutes * centsPerHour / 60;
        }

        /** The minutes of one shift that payroll pays for. */
        int paidMinutes(Shift s) {
            // TODO: Payroll's new agreement, from this month: each shift is paid in whole quarter
            //       hours of time worked, and a part of a quarter hour is not paid.
            throw new IllegalStateException("TODO: paidMinutes");
        }
    }

    /** Floor planning: how long each worker was on the floor, for the weekly cover report. */
    public static class FloorReport {

        /** One line of the report, such as "Jane Doe  15h07". */
        public String line(String worker, List<Shift> shifts) {
            int minutes = 0;
            for (Shift s : shifts) minutes += floorMinutes(s);
            return String.format("%s  %dh%02d", worker, minutes / 60, minutes % 60);
        }

        /** The minutes of one shift that floor planning counts as time on the floor. */
        int floorMinutes(Shift s) {
            // TODO: Floor planning's new rule, from this month: a break shorter than 20 minutes is
            //       taken at the workstation and counts as time on the floor; a longer one does not.
            throw new IllegalStateException("TODO: floorMinutes");
        }
    }
}
