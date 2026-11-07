package practice;

/**
 * Starts the response to an incident. Severity 1 is the worst: it pages the on-call engineer,
 * and also does everything severity 2 does. Severity 2 posts to the incident channel, and also
 * does everything severity 3 does. Severity 3 opens a ticket, and also does what severity 4
 * does. Severity 4 only writes to the incident log.
 */
public class Escalation {

    /** What the desk can do, in the order a severity-1 incident does it. */
    public interface Actions {
        void pageOnCall();
        void postToChannel();
        void openTicket();
        void log();
    }

    private final Actions actions;

    public Escalation(Actions actions) {
        this.actions = actions;
    }

    /** Reports an incident from a monitor; the monitor's own name goes into the log. */
    public void report(String monitor, int severity) {
        if (monitor == null || monitor.isBlank()) {
            throw new IllegalArgumentException("an incident needs the name of its monitor");
        }
        escalate(severity);
    }

    /**
     * Runs the actions of this severity, most urgent first. A severity outside 1 to 4 is
     * rejected with IllegalArgumentException before anything is done.
     */
    void escalate(int severity) {
        // TODO: Run the actions this severity calls for, most urgent first: each severity does
        //       its own action and then everything the next lower severity does. Reject a
        //       severity outside 1 to 4 with IllegalArgumentException before doing anything.
        throw new IllegalStateException("TODO: escalate");
    }
}
