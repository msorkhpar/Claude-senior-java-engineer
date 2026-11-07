package practice;

import java.util.function.IntConsumer;

/**
 * One CI build. The build agent is handed callbacks when the job is scheduled and runs them
 * later, from its own threads: one for each progress report, one when the build is over.
 */
public class BuildJob {

    /** Posts a line to the team's chat channel. */
    public interface Notifier {
        void post(String line);
    }

    private final int number;
    private final String branch;
    private final Notifier notifier;
    private volatile String status = "queued";

    public BuildJob(int number, String branch, Notifier notifier) {
        this.number = number;
        this.branch = branch;
        this.notifier = notifier;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "build #" + number + " on " + branch;
    }

    /** Run for each progress report: posts this job, then the percentage, e.g. "<job>: 40%". */
    IntConsumer progressCallback() {
        // TODO: The agent calls this from its own thread with the percentage done. The line it
        //       posts must name this build, so the team can tell which one is meant.
        throw new IllegalStateException("TODO: progressCallback");
    }

    /** Run once, when the build is over: posts this job and its status at that moment, "<job>: <status>". */
    Runnable completionCallback() {
        // TODO: The agent runs this once, when it is done, possibly minutes after the job was
        //       scheduled. The line must name this build and say how the build ended.
        throw new IllegalStateException("TODO: completionCallback");
    }
}
